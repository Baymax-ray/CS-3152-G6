package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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

public class TutorialArea extends BoxObstacle {

    private TextureRegion tutorialTexture;
    private boolean display;
    private PolygonShape sensorShape;
    private final String sensorName;
    private final JsonValue tutorialAreaData;
    private final float tutorialImageWidth;
    private final float tutorialImageHeight;
    /**
     * The texture scale along the x-axis.
     */
    private float scaleX;
    /**
     * The texture scale along the y-axis.
     */
    private float scaleY;
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

    public String getSensorName() {return this.sensorName;}

    public void setDisplay(boolean value) { this.display = value; }
    public boolean isDisplay() { return display; }

    public TutorialArea(JsonValue json, AssetDirectory assets, float x, float y) {
        super(x, y,
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxHeight"));
        this.tutorialAreaData = assets.getEntry("sharedConstants", JsonValue.class).get("TutorialArea");

        String tutorialTextureAsset = json.get("properties").get(0).getString("value");
        this.tutorialTexture = new TextureRegion(assets.getEntry(tutorialTextureAsset, Texture.class));
        this.setWidth(tutorialAreaData.getFloat("hitboxWidth"));
        this.setHeight(tutorialAreaData.getFloat("hitboxHeight"));
        this.tutorialImageWidth = tutorialAreaData.getFloat("ImageWidth");
        this.tutorialImageHeight = tutorialAreaData.getFloat("ImageHeight");
        this.sensorName = "BillboardSensor";
        this.texture = this.tutorialTexture;
        scaleX = tutorialAreaData.getFloat("drawScaleX");
        scaleY = tutorialAreaData.getFloat("drawScaleY");
        oxOffset = tutorialAreaData.getFloat("oxOffset");
        oyOffset = tutorialAreaData.getFloat("oyOffset");
        xOffset = tutorialAreaData.getFloat("xOffset");
        yOffset = tutorialAreaData.getFloat("yOffset");
        this.setX(x + xOffset);
        this.setY(y + yOffset);

        this.display = false;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) { }

    public void displayTutorial(GameCanvas canvas, Level level) {
        float x = level.getPlayer().getX();
        float y = level.getPlayer().getY() + 2;

        float ox = oxOffset + tutorialTexture.getRegionWidth()/2;
        float oy = oyOffset + tutorialTexture.getRegionHeight()/2;

        float sx = scaleX * tutorialImageWidth / tutorialTexture.getRegionWidth();
        float sy = scaleY * tutorialImageHeight / tutorialTexture.getRegionHeight();

        canvas.draw(tutorialTexture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = tutorialAreaData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = tutorialAreaData.get("sensor");
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
