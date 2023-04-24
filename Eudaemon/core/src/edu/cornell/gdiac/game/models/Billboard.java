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
    private final String sensorName;
    private PolygonShape sensorShape;
    private String text;
    private boolean display;

    public String getSensorName() {return this.sensorName;}

    public void setDisplay(boolean value) { this.display = value; }
    public boolean isDisplay() { return display; }

    public Billboard(JsonValue json, AssetDirectory assets, float x, float y){
        super(x + 0.5f, y - 0.9f,
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxHeight"));
        String TextureAsset = "platform:spike";
        this.billboardData = assets.getEntry("sharedConstants", JsonValue.class).get("Spike");
        this.setWidth(billboardData.getFloat("hitboxWidth"));
        this.setHeight(billboardData.getFloat("hitboxHeight"));
        this.billboardTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
        this.billboardImageWidth = billboardData.getFloat("ImageWidth");
        this.billboardImageHeight = billboardData.getFloat("ImageHeight");
        this.text = json.getString("text");
        this.sensorName = "BillboardSensor";
        this.texture = this.billboardTexture;


        stringCompleteness = 0;
        textSpeed = 20;
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
        float y = getY() + 0.39f;

        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight()/2;

        float sx = 1.97f * billboardImageWidth / this.texture.getRegionWidth();
        float sy = 1.97f * billboardImageHeight / this.texture.getRegionHeight();

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
        String myText = "This is a sample dialog. This is a sample dialog.\nThis is a sample dialog. \nThis is a sample dialog. This is a sample dialog. This is a sample dialog.";
        int charCountThisFrame = (int)stringCompleteness;
        if (charCountThisFrame > myText.length()) charCountThisFrame = myText.length();

        glyphLayout = new GlyphLayout(font, myText.substring(0,charCountThisFrame));
        font.draw(canvas.getSpriteBatch(), glyphLayout, padding*1.5f, canvas.getHeight()/4.0f + padding/2);

        //canvas.drawText(myText.substring(0,charCountThisFrame), font, padding*1.5f, canvas.getHeight()/4.0f + padding/2);
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
