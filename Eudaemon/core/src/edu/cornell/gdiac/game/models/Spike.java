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

public class Spike extends CapsuleObstacle {
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

    /** The sprite sheet for the basic goomba right walk */
    private TextureRegion basicGoombaRightSpriteSheet;

    /** The sprite sheet for the basic goomba left walk */
    private TextureRegion basicGoombaLeftSpriteSheet;

    /** The sword killing enemy sound.  We only want to play once. */
    private Sound swordKillingSound;

    private long swordKillingSoundId = -1;
    //#endregion

    //#region Getter and Setter
    private String getSensorName() {return this.sensorName;}

    /**
     * Retrieves the blood effect sprite sheet of the object.
     *
     * @return The current blood effect sprite sheet.
     */
    public TextureRegion getBloodEffect() {
        return bloodEffectSpriteSheet;
    }


    /**
     * Get basicGoombaRightSpriteSheet.
     *
     * @return The basicGoombaRightSpriteSheet TextureRegion.
     */
    public TextureRegion getBasicGoombaRightSpriteSheet() {
        return basicGoombaRightSpriteSheet;
    }

    /**
     * Get basicGoombaLeftSpriteSheet.
     *
     * @return The basicGoombaLeftSpriteSheet TextureRegion.
     */
    public TextureRegion getBasicGoombaLeftSpriteSheet() {
        return basicGoombaLeftSpriteSheet;
    }


    //#endregion





    public Spike(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"),
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
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");

        //Size
        this.spikeImageWidth = spikeData.getFloat("ImageWidth");
        this.spikeImageHeight = spikeData.getFloat("ImageHeight");
        //this.scale = new Vector2(spikeData.getFloat("drawScaleX"), spikeData.getFloat("drawScaleY"));

        //Sound Effects
        this.swordKillingSound =  Gdx.audio.newSound(Gdx.files.internal("audio/temp-sword-killing.mp3"));

        //Sensor. Wtf is this?
        //used for collision detection
        this.sensorName = "SpikeGroundSensor";

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
        float oy = this.texture.getRegionHeight()/2;

        float sx = spikeImageWidth / this.texture.getRegionWidth();
        float sy = spikeImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
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

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = spikeData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = spikeData.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        super.setBodyTypeToStatic();

        return true;
    }







    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
    }

}
