/*
 * EnemyModel.java
 *
 * Author: Walker M. White, The Great CS3152-SP23 Team 6
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the platform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class EnemyModel extends CapsuleObstacle implements ContactListener{
    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;

    //<editor-fold desc="FINAL ENEMY VARIABLES">
    /** The factor to multiply by the input */
    private final float force;
    /** The amount to slow the character down */
    private final float damping;
    /** The maximum character speed */
    private final float maxspeed;
    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    /** Cooldown (in animation frames) for shooting */
    private final int shotLimit;
    /** The texture region for the enemy. */
    private TextureRegion enemyTexture;
    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
//    /** The impulse for the character jump */
//    private final float jump_force;
//    /** Cooldown (in animation frames) for jumping */
//    private final int jumpLimit;
    //</editor-fold>

    //<editor-fold desc="CHANGING ENEMY VARIABLES">
    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;
    /** The number of hearts */
    private int hearts;
    /** The maximum number of hearts */
    private final int maxHearts;
    /** The amount of spirit */
    private float spirit;
    /** The maximum amount of spirit */
    private final float maxSpirit;
    /** The current horizontal movement of the character */
    private float movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** Flag indicating whether the player is currently looking down.*/
    private boolean isLookingDown;
    /** Flag indicating whether the character is currently attacking. */
    private boolean isAttacking;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** Whether we are actively shooting */
    private boolean isShooting;
    /** The physics shape of this object */
    private PolygonShape sensorShape;
//    /** How long until we can jump again */
//    private int jumpCooldown;
//    /** Whether we are actively jumping */
//    private boolean isJumping;
//    /** Whether the player is looking up */
//    private boolean isLookUp;
    //</editor-fold>

    //<editor-fold desc="GETTERS AND SETTERS">
    /**
     * Returns the number of hearts the player has remaining
     *
     * @return number of hearts remaining.
     */
    public float getHearts() {
        return hearts;
    }

    /**
     * Sets the number of hearts the player has
     *
     * @param value number of hearts.
     */
    public void setHearts(int value) {
        if (value <= maxHearts){
            hearts = value;
        }
        else {
            hearts = maxHearts;
        }
    }
    /**
     * Returns the amount of spirit the player has
     *
     * @return spirit variable
     */
    public float getSpirit() {
        return spirit;
    }

    /**
     * Sets the number of hearts the player has
     *
     * @param value amount of spirit
     */
    public void setSpirit(float value) {
        if (value <= maxSpirit){
            spirit = value;
        }
        else {
            spirit = maxSpirit;
        }
    }
    /**
     * Returns the player's current form (0: Momo) (1: Chiyo)
     *
     * @return the player's current form.
     */
    public float getForm() {
        return form;
    }

    /**
     * Changes the players form
     */
    public void setForm() {
        if (form == 0){
            form = 1;
        }
        else {
            form = 0;
        }
    }

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
    }
    /**
     * Returns whether the player is currently looking down.
     * @return True if the player is looking down, false otherwise.
     */
    public boolean isLookingDown() {
        return isLookingDown;
    }

    /**
     * Sets whether the player is currently looking down.
     * @param lookingDown True to indicate that the player is looking down, false otherwise.
     */
    public void setLookingDown(boolean lookingDown) {
        isLookingDown = lookingDown;
    }
//    /**
//     * Returns true if the dude is actively looking up.
//     *
//     * @return true if the dude is actively looking up.
//     */
//    public boolean isLookUp() {
//        return isLookUp;
//    }
//    /**
//     * Sets whether the dude is actively looking up.
//     *
//     * @param value whether the dude is actively looking up.
//     */
//    public void setLookUp(boolean value) {
//        isLookUp = value;
//
//    }
//
//    /**
//     * Returns true if the dude is actively jumping.
//     *
//     * @return true if the dude is actively jumping.
//     */
//    public boolean isJumping() {
//        return isJumping && isGrounded && jumpCooldown <= 0;
//    }
//    /**
//     * Sets whether the dude is actively jumping.
//     *
//     * @param value whether the dude is actively jumping.
//     */
//    public void setJumping(boolean value) {
//        isJumping = value;
//    }
    /**
     * Returns whether the character is currently attacking.
     * @return True if the character is attacking, false otherwise.
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * Sets whether the character is currently attacking.
     * @param attacking True to indicate that the character is attacking, false otherwise.
     */
    public void setAttacking(boolean attacking) {
        isAttacking = attacking;
    }
    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }
    //</editor-fold>

    //CONSTRUCTOR
    /**
     * Creates a new enemy avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for this dude
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public EnemyModel(JsonValue data, float width, float height, TextureRegion enemy, Vector2 scale) {
        // The shrink factors fit the image to a tighter hitbox
        super(	data.get("posEnemy").getFloat(0),
                data.get("posEnemy").getFloat(1),
                width*data.get("shrink").getFloat( 0 ),
                height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
        //ORIGIN: 1.0
        setFriction(data.getFloat("friction", 0));
        /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        maxHearts = data.getInt("maxHearts", 0);
        maxSpirit = data.getInt("maxSpirit", 0);
        spirit = 1.0f;
        hearts = 1;
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);
//        jump_force = data.getFloat( "jump_force", 0 );
//        jumpLimit = data.getInt( "jump_cool", 0 );
        shotLimit = data.getInt( "shot_cool", 0 );
        sensorName = "EnemyGroundSensor";
        this.data = data;
        this.enemyTexture = enemy;
//        enemyTexture.setRegion(1, 1, 1,1);
//        this.setDrawScale(scale);
        // Gameplay attributes
        isGrounded = false;
        isShooting = false;
//        isJumping = false;
        faceRight = true;

        shootCooldown = 0;
//        jumpCooldown = 0;
        setName("enemy");
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
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
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (getMovement() == 0f) {
            forceCache.set(-getDamping()*getVX(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX())*getMaxSpeed());
        } else {
            forceCache.set(getMovement(),0);
            body.applyForce(forceCache,getPosition(),true);
        }
//        // Jump!
//        if (isJumping()) {
//            forceCache.set(0, jump_force);
//            body.applyLinearImpulse(forceCache,getPosition(),true);
//        }
    }

    /**
     * Called when this enemy is hit by a sword.
     *
     * This method decrements the number of hearts for this enemy by 1. If the number of hearts
     * reaches 0, this method destroys the enemy
     */
    public void hitBySword() {
        hearts--;
        if (hearts <= 0) {
            this.markRemoved(true);
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Apply cooldowns
//        if (isJumping()) {
//            jumpCooldown = jumpLimit;
//        } else {
//            jumpCooldown = Math.max(0, jumpCooldown - 1);
//        }

        if (isShooting()) {
            shootCooldown = shotLimit;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = -(faceRight ? 1.0f : -1.0f);
        canvas.draw(enemyTexture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x-15*effect,getY()*drawScale.y-16,getAngle(),effect * 0.03f,0.03f);
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    //<editor-fold desc="COLLISION">
    /** This method is called when two objects start colliding **/
    @Override
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        System.out.println(bd1.getClass());
        System.out.println(bd2.getClass());
        // Check if the two objects colliding are an instance of EnemyModel and SwordWheelObstacle
        if ((bd1 instanceof EnemyModel
                && bd2 instanceof SwordWheelObstacle)
                || (bd2 instanceof EnemyModel
                && bd1 instanceof SwordWheelObstacle)) {

            // Get references to the EnemyModel and SwordWheelObstacle objects involved in the collision
            EnemyModel enemy = null;
            SwordWheelObstacle obstacle = null;
            if (bd1 instanceof EnemyModel) {
                enemy = (EnemyModel) bd1;
                obstacle = (SwordWheelObstacle) bd2;
            } else {
                enemy = (EnemyModel) bd2;
                obstacle = (SwordWheelObstacle)bd1;
            }
            // Call a method in EnemyModel to handle the collisions
            enemy.hitBySword();
        }
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
    //</editor-fold>
}