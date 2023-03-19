package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.game.*;
import edu.cornell.gdiac.game.obstacle.*;

public class Enemy extends CapsuleObstacle implements ContactListener {

    //#region FINAL FIELDS

    // TODO: Add texture fields (FilmStrip?)

    //#endregion


    //#region NONFINAL FIELDS

    private Vector2 pos;
    private Vector2 vel;

    private float movement;
    private final float startX;
    private final float startY;

    private final int maxHearts;
    private final int initialHearts;

    private final int attackPower;

    private final boolean startsFacingRight;

    /**
     * The amount of ticks before the enemy can attack again
     */
    private final int attackCooldown;

    /**
     * The distance the center of the attack is offset from the enemy
     */
    private final float attackOffset;
    private final int hitCooldown;
    /**
     * The multiplier used to calculate the width of the hitbox.
     */
    private final float hitboxWidthMult;

    /**
     * The multiplier used to calculate the height of the hitbox.
     */
    private final float hitboxHeightMult;

    /**
     * The scaling factor for the sprite.
     */
    private final Vector2 scale;

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();

    //#endregion

    //#region TEXTURES
    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion enemyTexture;
    private final float enemyImageWidth;
    private final float enemyImageHeight;
    //#endregion

    //#region NONFINAL FIELDS

    private float hearts;
    /**
     * The maximum horizontal speed that the object can reach.
     */
    private float maxSpeed;

    /**
     * The horizontal acceleration of the object.
     */
    private float horizontalAcceleration;

    private boolean isHit;
    private boolean isGrounded;
    private boolean isFacingRight;
    /**
     * The angle at which the entity is facing, in degrees.
     */
    private int angleFacing;
    private boolean isMovingRight;
    private boolean isMovingLeft;

    /**
     * The remaining time in seconds until the enemy can attack again.
     */
    private int attackCooldownRemaining;

    private int hitCooldownRemaining;


    /** The physics shape of this object */
    private PolygonShape sensorShape;

    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    /**the type of this enemy. currently: goombaAI or fly.*/
    private final String type;

    private final JsonValue enemyData;



    //TODO: Add texture fields

    //#endregion
    public String getType() {
        return type;
    }


    /**
     * @param enemyName: the name of this enemy so as to query its image width and height in json,
     *                 MUST BE IN LOWERCASE!
     *
     * */
    public Enemy(JsonValue json, AssetDirectory assets, String enemyName) {
        super(json.getFloat("startX"), json.getFloat("startY"), json.getFloat("hitboxWidth"), json.getFloat("hitboxHeight"));
        String TextureAsset = json.getString("TextureAsset");
        this.enemyTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));

        //Position and Movement. These two values are stored in constants.json
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");

        //Query the type of this enemy, then query the corresponding data in enemyConstants.json
        this.type=json.getString("type");
        if(this.type == "Goomba"){
            this.enemyData = assets.getEntry("enemyConstants", JsonValue.class).get("Goomba");
        }else {
            //Current: Enemy can only be Fly
            this.enemyData = assets.getEntry("enemyConstants", JsonValue.class).get("Fly");
        }

        //Size
        this.enemyImageWidth = enemyData.getFloat(enemyName+"ImageWidth");
        this.enemyImageHeight = enemyData.getFloat(enemyName+"ImageHeight");
        this.scale = new Vector2(enemyData.getFloat("drawScaleX"), enemyData.getFloat("drawScaleY"));


        this.maxSpeed = enemyData.getFloat("maxSpeed");
        this.hitboxWidthMult = enemyData.getFloat("hitboxWidthMult");
        this.hitboxHeightMult = enemyData.getFloat("hitboxHeightMult");

        //Attacking
        this.attackPower = enemyData.getInt("attackPower");
        this.attackCooldown = enemyData.getInt("attackCooldown");
        this.attackOffset = enemyData.getFloat("attackOffset");
        this.hitCooldown = enemyData.getInt("hitCooldown");

        //Sensor. Wtf is this?
        this.sensorName = "PlayerGroundSensor";

        //Other Information
        this.maxHearts = enemyData.getInt("maxHearts");
        this.initialHearts = enemyData.getInt("initialHearts");
        this.hearts = initialHearts;

        this.startsFacingRight = enemyData.getBoolean("startsFacingRight");

        this.isHit = false;
        this.isGrounded = true;
        this.isFacingRight = startsFacingRight;

    }
    public void setMovement(EnemyAction move) {
        if (move==EnemyAction.MOVE_RIGHT){movement=-1;}
        else if (move==EnemyAction.MOVE_LEFT){movement=1;}
        // Change facing if appropriate
        if (movement < 0) {
            isFacingRight = false;
        } else if (movement > 0) {
            isFacingRight = true;
        }
    }
    public float getMovement(){return movement;}
    public void applyForce() {
        if (!isActive()) {
            return;
        }
        // Don't want to be moving. Damp out player motion
//        if (getMovement() == 0f) {
//            forceCache.set(-getDamping() * getVX(), 0);
//            body.applyForce(forceCache, getPosition(), true);
//        }
//
//        // Velocity too high, clamp it
//        if (Math.abs(getVX()) >= getMaxSpeed()) {
//            setVX(Math.signum(getVX()) * getMaxSpeed());
//        } else {
//            forceCache.set(getMovement(), 0);
//            body.applyForce(forceCache, getPosition(), true);
//        }
    }
        @Override
    public void beginContact(Contact contact) {

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
