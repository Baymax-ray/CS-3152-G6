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

public class Enemy extends BoxObstacle implements ContactListener {

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

    /** The amount to slow the character down */
    private final float damping;

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
    private String getSensorName() {return this.sensorName;}

    public Enemy(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"), json.getFloat("hitboxWidth"), json.getFloat("hitboxHeight"));
        String TextureAsset = json.getString("TextureAsset");
        //Query the type of this enemy, then query the corresponding data in enemyConstants.json
        this.type=json.getString("type");
        if(this.type.equals("Goomba")){
            System.out.println("enemy creating");
            this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("Goomba");
        }else if (this.type.equals("Fly")){
            this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("Fly");
        }
        else{
            //should never reach here
            this.enemyData=null;
            System.out.println(this.type);
            throw new IllegalArgumentException("Enemy can only be Fly or Goomba");
        }
        this.setWidth(enemyData.getFloat("hitboxWidth"));
        this.setHeight(enemyData.getFloat("hitboxHeight"));


        this.enemyTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
        this.texture = this.enemyTexture;

        //Position and Movement. These two values are stored in constants.json
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");

        //Size
        this.enemyImageWidth = enemyData.getFloat("ImageWidth");
        this.enemyImageHeight = enemyData.getFloat("ImageHeight");
        this.scale = new Vector2(enemyData.getFloat("drawScaleX"), enemyData.getFloat("drawScaleY"));


        this.maxSpeed = enemyData.getFloat("maxSpeed");
        this.hitboxWidthMult = enemyData.getFloat("hitboxWidthMult");
        this.hitboxHeightMult = enemyData.getFloat("hitboxHeightMult");
        this.damping = enemyData.getFloat("damping");

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

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        //System.out.println("trying to draaw");
        float x = getX();
        float y = getY();

        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight()/2;

        float sx = (isFacingRight ? 1 : -1) * enemyImageWidth / this.texture.getRegionWidth();
        //System.out.println(momoImageWidth / this.texture.getRegionWidth());
        float sy = enemyImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }


    /**
     * TODO: change this function to cater for Flying Enemy!
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
        sensorDef.density = enemyData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = enemyData.get("sensor");
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
            forceCache.set(-this.damping*getVX(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= this.maxSpeed) {
            setVX(Math.signum(getVX())*this.maxSpeed);
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

        //System.out.println(bd1.getClass());
        //System.out.println(bd2.getClass());
        // Check if the two objects colliding are an instance of EnemyModel and SwordWheelObstacle
        if ((bd1 instanceof Enemy
                && bd2 instanceof SwordWheelObstacle)
                || (bd2 instanceof Enemy
                && bd1 instanceof SwordWheelObstacle)) {

            // Get references to the EnemyModel and SwordWheelObstacle objects involved in the collision
            Enemy enemy = null;
            SwordWheelObstacle obstacle = null;
            if (bd1 instanceof Enemy) {
                enemy = (Enemy) bd1;
                obstacle = (SwordWheelObstacle) bd2;
            } else {
                enemy = (Enemy) bd2;
                obstacle = (SwordWheelObstacle)bd1;
            }
            // Call a method in EnemyModel to handle the collisions
            enemy.hitBySword();
        }
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
