package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;


public class Billboard extends BoxObstacle {
    private float stringCompleteness;
    private float textSpeed;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private final JsonValue billboardData;
    private final TextureRegion billboardTexture;
    private final float billboardImageWidth;
    private final float billboardImageHeight;
    private float displayImageWidth;
    private float displayImageHeight;
    private final String sensorName;
    private PolygonShape sensorShape;
    private String text;
    private String displayTextureAsset;
    private boolean display;
    /**
     * The texture origin offset value along the y-axis.
     */
    private float oyOffset;
    /**
     * The texture origin offset value along the y-axis.
     */
    private float oxOffset;
    /**
     * The offset value along the x-axis.
     */
    private float xOffset;
    /**
     * The offset value along the y-axis.
     */
    private float yOffset;
    /**
     * The texture scale along the x-axis.
     */
    private float scaleX;
    /**
     * The texture scale along the y-axis.
     */
    private float scaleY;

    private TextureRegion displayTexture;


    public String getSensorName() {return this.sensorName;}

    public void setDisplay(boolean value) { this.display = value; }
    public boolean isDisplay() { return display; }

    public Billboard(JsonValue json, AssetDirectory assets, float x, float y){
        super(x, y,
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxHeight"));
        this.displayTextureAsset = "";
        this.billboardData = assets.getEntry("sharedConstants", JsonValue.class).get("Billboard");
        String TextureAsset = "platform:textBillboard";
        this.billboardTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
        this.text = json.get("properties").get(0).getString("value");
//        if (type.equals("text")) {
//            this.text = json.get("properties").get(0).getString("value");
//            displayTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
//        }
//        else if (type.equals("image")) {
//            this.displayTextureAsset = json.get("properties").get(0).getString("value");
//            displayTexture = new TextureRegion(assets.getEntry(displayTextureAsset, Texture.class));
//        }

        this.setWidth(billboardData.getFloat("hitboxWidth"));
        this.setHeight(billboardData.getFloat("hitboxHeight"));
        this.billboardImageWidth = billboardData.getFloat("ImageWidth");
        this.billboardImageHeight = billboardData.getFloat("ImageHeight");
        this.sensorName = "BillboardSensor";
        this.texture = this.billboardTexture;
        scaleX = billboardData.getFloat("drawScaleX");
        scaleY = billboardData.getFloat("drawScaleY");
        oxOffset = billboardData.getFloat("oxOffset");
        oyOffset = billboardData.getFloat("oyOffset");
        xOffset = billboardData.getFloat("xOffset");
        yOffset = billboardData.getFloat("yOffset");
        this.setX(x + xOffset);
        this.setY(y + yOffset);


        stringCompleteness = 0;
        textSpeed = 50;
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        this.display = false;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float x = getX();
        float y = getY();

        float ox = oxOffset + this.texture.getRegionWidth()/2;
        float oy = oyOffset + this.texture.getRegionHeight()/2;

        float sx = scaleX * billboardImageWidth / this.texture.getRegionWidth();
        float sy = scaleY * billboardImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);

    }

    public void displayDialog(GameCanvas canvas){
        float padding = canvas.getWidth()/30.0f;
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0.75f);
        pix.fill();
        Texture textureSolid = new Texture(pix);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid),
                new float[] {                                                      // Four vertices
                        0, 0,                                                      // Vertex 0         3--2
                        canvas.getWidth() - padding*2, 0,                          // Vertex 1         | /|
                        canvas.getWidth() - padding*2, canvas.getHeight()/4.0f,    // Vertex 2         |/ |
                        0, canvas.getHeight()/4.0f                                 // Vertex 3         0--1
                }, new short[] {
                0, 1, 2,         // Two triangles using vertex indices.
                0, 2, 3          // Take care of the counter-clockwise direction.
        });
        canvas.draw(polyReg, padding, padding);
        //String myText = "This is a sample dialog. This is a sample dialog.\nThis is a sample dialog. \nThis is a sample dialog. This is a sample dialog. This is a sample dialog.";
        String myText = this.text;
        int charCountThisFrame = (int)stringCompleteness;
        if (charCountThisFrame > myText.length()) charCountThisFrame = myText.length();

        glyphLayout = new GlyphLayout(font, myText.substring(0,charCountThisFrame));
        font.draw(canvas.getSpriteBatch(), glyphLayout, padding*1.5f, canvas.getHeight()/4.0f + padding/2);

        //canvas.drawText(myText.substring(0,charCountThisFrame), font, padding*1.5f, canvas.getHeight()/4.0f + padding/2);
    }

    public void displayImage(GameCanvas canvas, Level level) {
        //tile of player character
//        System.out.println(level.getCameraWidth());
//
//        float x = (level.getPlayer().getX() % level.getCameraWidth() * 64) % canvas.getWidth();
//        float y = level.getPlayer().getY() * 64 + canvas.getHeight() / 20.0f;

//        float x = getX();
//        float y = getY();
//        System.out.println("yes");
//
//        canvas.draw(displayTexture, x,y);

        float x = level.getPlayer().getX();
        float y = level.getPlayer().getY() + 2;

        float ox = oxOffset + displayTexture.getRegionWidth()/2;
        float oy = oyOffset + displayTexture.getRegionHeight()/2;

        float sx = scaleX * 0.7f / displayTexture.getRegionWidth();
        float sy = scaleY * 0.7f / displayTexture.getRegionHeight();

        canvas.draw(displayTexture, Color.WHITE, ox, oy, x, y, 0, sx, sy);


    }

    public void aggregateStringCompleteness(float delta) {
        stringCompleteness += textSpeed * delta;
        if (!isDisplay()) stringCompleteness = 0;
    }

    /**
     * TODO: change this function to cater for Flying Enemy!
     * Creates the physics Body(s) for this object, adding them to the world.
     * This method overrides the base method to keep your ship from spinning.
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = billboardData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = billboardData.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        super.body.setType(BodyType.StaticBody);
//        super.setBodyTypeToStatic();

        return true;
    }
}
