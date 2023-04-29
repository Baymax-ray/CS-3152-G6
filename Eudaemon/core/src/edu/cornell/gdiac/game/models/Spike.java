package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.*;
import edu.cornell.gdiac.game.obstacle.*;

import java.util.ArrayList;

public class Spike extends BoxObstacle {
    //#region FINAL FIELDS
    private final float startX;
    private final float startY;
    private final int attackPower;

    //#endregion

    //#region TEXTURES
    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion spikeTexture;

    /** The texture for the enemy's blood */
    private final TextureRegion bloodEffectSpriteSheet;
    private final float spikeImageWidth;
    private final float spikeImageHeight;
    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    private final JsonValue spikeData;
    //#endregion

    //#region NON-FINAL FIELDS
    /** The physics shape of this object */
    private PolygonShape sensorShape;

    private float rotationAngle;

    private String direction;
    private float offsetX;
    private float offsetY;

    //#endregion

    //#region Getter and Setter
    private String getSensorName() {return this.sensorName;}
    public String getDirection(){return this.direction;}
    //#endregion





    public Spike(JsonValue json, AssetDirectory assets, float x, float y) {
        super(x, y,
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxHeight"));
        String TextureAsset = "platform:spike";
        this.spikeData = assets.getEntry("sharedConstants", JsonValue.class).get("Spike");


        this.setWidth(spikeData.getFloat("hitboxWidth"));
        this.setHeight(spikeData.getFloat("hitboxHeight"));
        this.attackPower = spikeData.getInt("attackPower");

        //Texture
        this.spikeTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
        this.texture = this.spikeTexture;
        this.bloodEffectSpriteSheet = new TextureRegion(assets.getEntry( "bloodEffect", Texture.class));

        //Position and Movement. These two values are stored in constants.json
        this.startX = x + 0.5f;
        this.startY = y - 0.9f;

        //Size
        this.spikeImageWidth = spikeData.getFloat("ImageWidth");
        this.spikeImageHeight = spikeData.getFloat("ImageHeight");
        //this.scale = new Vector2(spikeData.getFloat("drawScaleX"), spikeData.getFloat("drawScaleY"));

        //used for collision detection
        this.sensorName = "SpikeGroundSensor";


        JsonValue properties = json.get("properties");
        this.direction = "Up";
        for (JsonValue property : properties) {
            if (property.getString("name").equals("Direction")) {
                switch (property.getString("value")) {
                    case "Up":
                        this.direction = "Up";
                        this.rotationAngle = 0;
                        this.offsetX = -0.5f;
                        this.offsetY = 1 - getHeight()/2;
                        break;
                    case "Left":
                        this.direction = "Left";
                        this.rotationAngle = (float) (Math.PI/2);
                        this.offsetX = getHeight()/2 - 1;
                        this.offsetY = 0.5f;
                        break;
                    case "Down":
                        this.direction = "Down";
                        this.rotationAngle = (float) (Math.PI);
                        this.offsetX = -0.5f;
                        this.offsetY = getHeight()/2;
                        break;
                    case "Right":
                        this.direction = "Right";
                        this.rotationAngle = (float) (Math.PI * 1.5f);
                        this.offsetX = -getHeight()/2;
                        this.offsetY = 0.5f;
                        break;
                    default:
                        System.out.println("something wrong");
                }
            }
        }
        this.setX(this.getX() - offsetX);
        this.setY(this.getY() - offsetY);
        this.setAngle(rotationAngle);

    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float x = getX();
        float y = getY();

        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight() * this.getHeight()/2;

        float sx = 1.97f * spikeImageWidth / this.texture.getRegionWidth();
        float sy = 1.97f * spikeImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, rotationAngle, sx, sy);
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
        Vector2 sensorCenter = new Vector2(0, 0);

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = spikeData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = spikeData.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, this.getAngle());
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        super.body.setType(BodyType.StaticBody);
//        super.setBodyTypeToStatic();

        return true;
    }

}
