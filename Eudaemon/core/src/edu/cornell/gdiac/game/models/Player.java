package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;

public class Player extends BoxObstacle {

    //#region FINAL FIELDS

    private final float startX;
    private final float startY;

    private final int maxHearts;
    private final int initialHearts;
    private final float maxSpirit;
    private final float initialSpirit;
    private final float spiritPerSecond;
    private final int attackPower;

    private final boolean startsFacingRight;

    private final int dashCooldown;
    /**
     * The amount of ticks before the palyer can attack again
     */
    private final int attackCooldown;
    /**
     * The amount of ticks before the palyer can transform again
     */
    private final int transformCooldown;

    /**
     * The amount of ticks before the palyer can jump again
     */
    private final int jumpCooldown;
    private int jumpCooldownRemaining;


    /**
     * The amount of ticks before the palyer can jump when they leave a platform
     */
    private final int coyoteFrames;
    private int coyoteFramesRemaining;

    private final int jumpTolerance;
    private int jumpToleranceRemaining;

    private final float spiritIncreaseRate;
    private final float spiritDecreaseRate;
    private final float spiritIncreaseDist;

    /**
     * The distance the center of the attack is offset from the player
     */
    private final float attackOffset;
    private final int hitCooldown;


    /**
     * The radius of the sword used by the player.
     */
    private final float swordRadius;
    /**
     * The lifespan of the sword attack animation in seconds.
     */
    private final float attackLifespan;

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();

    private final float hitDist;

    private final int iFrames;

    private final float attackDist;

    //#endregion

    //#region TEXTURES
    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion momoTexture;
    private final float momoImageWidth;
    private final float momoImageHeight;
    private final TextureRegion chiyoTexture;
    private final float chiyoImageWidth;
    private final float chiyoImageHeight;
    /**
     * The sprite sheet containing the sword attack animation frames.
     */
    private final TextureRegion swordSpriteSheet;
    /**
     * The sprite sheet containing the dash animation frames.
     */
    private final TextureRegion dashSpriteSheet;
    //#endregion

    //#region NONFINAL FIELDS

    private float hearts;
    private float spirit;

    /**
     * The maximum horizontal speed that the object can reach.
     */
    private float maxSpeed;

    /**
     * The horizontal acceleration of the object.
     */
    private float horizontalAcceleration;

    private boolean isDashing;
    private boolean isJumping;
    private boolean isChiyo;
    private boolean isHit;
    private boolean isGrounded;
    private boolean isFacingRight;
    /**
     * The angle at which the entity is facing, in degrees.
     */
    private int angleFacing;
    private boolean isMovingRight;
    private boolean isMovingLeft;
    private boolean isLookingUp;
    private boolean isLookingDown;
    private boolean jumpPressedInAir;

    private int dashCooldownRemaining;
    /**
     * The remaining time in seconds until the player can attack again.
     */
    private int attackCooldownRemaining;
    /**
     * The remaining time in seconds until the player can transform again.
     */
    private int transformCooldownRemaining;
    private int hitCooldownRemaining;

    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;

    /** The impulse for the character being hit by enemies */
    private final float hit_force;
    /** The amount by which the player should move when dashing*/
    private final float dash;

    private final JsonValue data;

    /** The physics shape of this object */
    private PolygonShape sensorShape;

    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    private final int jumpTime;
    private int jumpTimeRemaining;

    private int iFramesRemaining;

    private boolean isAttacking;
    private int attackLifespanRemaining;

    /**
     * A float representing the max jump velocity of a game object.
     */
    private float maxJumpVelocity;
    /**
     * Accumulates the ticks elapsed since the animation started.
     */
    int currentTicks;
    /**
     * Current frame of sword animation.
     */
    float currentFrame;
    /**
     * The animation object that represents a sequence of TextureRegions.
     */
    Animation<TextureRegion> animation;

    //#endregion

    //#region TEXTURE GETTERS AND SETTERS
    /**
     * Returns the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Momo texture.
     *
     * @return the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Momo texture.
     */
    public TextureRegion getMomoTexture() {
        return momoTexture;
    }
    /**
     * Returns the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Chiyo texture.
     *
     * @return the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Chiyo texture.
     */
    public TextureRegion getChiyoTexture() {
        return chiyoTexture;
    }
    /**
     * Gets the sprite sheet containing the sword animation frames.
     *
     * @return the sprite sheet containing the sword animation frames
     */
    public TextureRegion getSwordSpriteSheet() {
        return swordSpriteSheet;
    }
    /**
     * Gets the sprite sheet containing the dash animation frames.
     *
     * @return the sprite sheet containing the dash animation frames
     */
    public TextureRegion getDashSpriteSheet() {
        return dashSpriteSheet;
    }
    //#endregion

    //#region GETTERS AND SETTERS
    /**
     * Returns the jump velocity of the game object.
     *
     * @return A float representing the jump velocity.
     */
    public float getJumpVelocity() {
        return maxJumpVelocity;
    }

    /**
     * Gets the radius of the sword hitbox.
     *
     * @return the radius of the sword hitbox
     */
    public float getSwordRadius() {
        return swordRadius;
    }

    /**
     * Gets the lifespan of the sword attack animation in seconds.
     *
     * @return the sword attack animation lifespan
     */
    public float getAttackLifespan() {
        return attackLifespan;
    }
    public float getAttackLifespanRemaining() { return attackLifespanRemaining; }
    public void setAttackLifespanRemaining(int value) {attackLifespanRemaining = value;}
    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }
    /**
     * Gets the attack offset.
     *
     * @return the attack offset as a float value
     */
    public float getAttackOffset() {
        return attackOffset;
    }
    /**
     * Gets the attack cooldown in seconds.
     *
     * @return the attack cooldown
     */
    public int getAttackCooldown() {
        return attackCooldown;
    }
    /**
     * Gets the dash cooldown in seconds.
     *
     * @return the dash cooldown
     */
    public int getDashCooldown(){
        return dashCooldown;
    }
    /**
     * Gets the transform cooldown in seconds.
     *
     * @return the attack cooldown
     */
    public int getTransformCooldown() {
        return transformCooldown;
    }
    /**
     * Returns the x-component of the current linear velocity of the body.
     *
     * @return the x-component of the current linear velocity in meters per second
     */
    public float getBodyVelocityX() {
        return body.getLinearVelocity().x;
    }

    /**
     * Returns the y-component of the current linear velocity of the body.
     *
     * @return the y-component of the current linear velocity in meters per second
     */
    public float getBodyVelocityY() {
        return body.getLinearVelocity().y;
    }
    /**
     * Sets the linear velocity of the kinematic body.
     *
     * @param velocityX the x-component of the linear velocity in meters per second
     * @param velocityY the y-component of the linear velocity in meters per second
     */
    public void setVelocity(float velocityX, float velocityY) {
        body.setLinearVelocity(velocityX, velocityY);
    }

    /**
     * Gets the linear velocity of the given body.
     *
     * @return the velocity of the body
     */
    public Vector2 getBodyVelocity() {
        return body.getLinearVelocity();
    }

    /**
     * Returns the maximum horizontal speed that the object can reach.
     *
     * @return the maximum horizontal speed in meters per second
     */
    public float getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Sets the maximum horizontal speed that the object can reach.
     *
     * @param maxSpeed the maximum horizontal speed in meters per second
     */
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * Returns the horizontal acceleration of the object.
     *
     * @return the horizontal acceleration in meters per second squared
     */
    public float getHorizontalAcceleration() {
        return horizontalAcceleration;
    }
    /**
     * Returns whether the player is facing right.
     *
     * @return true if the player is facing right, false otherwise
     */
    public boolean isFacingRight() {
        return isFacingRight;
    }

    /**
     * Sets whether the player is facing right.
     *
     * @param isFacingRight true if the player should face right, false otherwise
     */
    public void setFacingRight(boolean isFacingRight) {
        this.isFacingRight = isFacingRight;
    }
    /**
     * Returns the number of hearts the player has remaining
     *
     * @return number of hearts remaining.
     */
    public float getHearts() {
        return hearts;
    }

    /**
     * Sets the angle at which the entity is facing.
     * @param angle the angle at which the entity is facing, in degrees
     */
    public void setAngleFacing(int angle) {
        if (angle % 45 == 0){
            this.angleFacing = angle;
        }
        else{
            throw new IllegalArgumentException("Invalid direction angle");
        }
    }

    /**
     * Gets the angle at which the entity is facing.
     * @return the angle at which the entity is facing, in degrees
     */
    public int getAngleFacing() {
        return angleFacing;
    }

    /**
     * Sets the number of hearts the player has
     *
     * @param value number of hearts.
     */
    public void setHearts(int value) {
        if (value <= maxHearts) {
            hearts = value;
        } else {
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
     * Sets the remaining time in seconds until the player can attack again.
     * @param attackCooldownRemaining the remaining time in seconds
     */
    public void setAttackCooldownRemaining(int attackCooldownRemaining) {
        this.attackCooldownRemaining = attackCooldownRemaining;
    }

    /**
     * Sets the remaining time in seconds until the player can dash again.
     * @param dashCooldownRemaining the remaining time in seconds
     */
    public void setDashCooldownRemaining(int dashCooldownRemaining){
        this.dashCooldownRemaining = dashCooldownRemaining;
    }

    /**
     * Returns the remaining time in seconds until the player can transform again.
     * @return the remaining time in seconds
     */
    public int getTransformCooldownRemaining() {
        return transformCooldownRemaining;
    }
    /**
     * Sets the remaining time in seconds until the player can transform again.
     * @param transformCooldownRemaining the remaining time in seconds
     */
    public void setTransformCooldownRemaining(int transformCooldownRemaining) {
        this.transformCooldownRemaining = transformCooldownRemaining;
    }

    /**
     * Returns the remaining time in seconds until the player can attack again.
     * @return the remaining time in seconds
     */
    public int getAttackCooldownRemaining() {
        return attackCooldownRemaining;
    }
    /**
     * Returns the remaining time in seconds until the player can dash again.
     * @return the remaining time in seconds
     */

    public int getDashCooldownRemaining(){
        return dashCooldownRemaining;
    }

    /**
     * Returns whether the player is standing on the ground.
     *
     * @return true if the player is standing on the ground, false otherwise
     */
    public boolean isGrounded() {return isGrounded;}

    /**
     * Sets whether the player is standing on the ground.
     *
     * @param value true if the player is standing on the ground, false otherwise
     */
    public void setGrounded(boolean value) {isGrounded = value;}

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
     * Returns the number of frames player can jump again after they jump.
     * @return the time in frames
     */
    public int getJumpCooldown() { return jumpCooldown; }

    /**
     * Returns the remaining number of frames until the player can jump again.
     * @return the remaining time in frames
     */
    public int getJumpCooldownRemaining() { return jumpCooldownRemaining; }

    /**
     * Sets the remaining time in frames until the player can jump again.
     * @param value the remaining time in frames
     */
    public void setJumpCooldownRemaining(int value) { jumpCooldownRemaining = value; }

    /**
     * Returns the number of frames player can jump after they leave a platform
     * @return the time in frames
     */
    public int getCoyoteFrames() { return coyoteFrames; }
    /**
     * Returns the remaining number of frames that the player can jump after the leave a platform.
     * @return the remaining time in frames
     */
    public int getCoyoteFramesRemaining() { return coyoteFramesRemaining; }

    /**
     * Sets the remaining time in frames that the player can jump after they leave a platform.
     * @param value the remaining time in frames
     */
    public void setCoyoteFramesRemaining(int value) { coyoteFramesRemaining = value; }

    /**
     * Returns whether the player pressed jump in air.
     *
     * @return true if the player pressed jump in air, false otherwise
     */
    public boolean getJumpPressedInAir() { return jumpPressedInAir; }

    /**
     * Sets whether the player pressed jump in air.
     *
     * @param value true if the player pressed jump in air, false otherwise
     */
    public void setJumpPressedInAir(boolean value) { jumpPressedInAir = value; }

    /**
     * Returns the number of frames player can jump after they touch the ground if they press jump
     * if the air
     * @return the time in frames
     */
    public int getJumpTolerance() { return jumpTolerance; }

    /**
     * Returns the remaining number of frames that the player can jump when they touch the ground,
     * if they press jump in the air.
     * @return the remaining time in frames
     */
    public int getJumpToleranceRemaining() { return jumpToleranceRemaining; }

    /**
     * Sets the remaining time in frames that the player can jump after they touch the ground if
     * they press jump in the air.
     * @param value the remaining time in frames
     */
    public void setJumpToleranceRemaining(int value) { jumpToleranceRemaining = value; }

    /**
     * Returns whether the player is jumping.
     *
     * @return true if the player is jumping (holding the jump key), false otherwise
     */
    public boolean getIsJumping() { return isJumping; }

    /**
     * Sets whether the player jumping.
     *
     * @param value true if the player is jumping, false otherwise
     */
    public void setIsJumping(boolean value) {isJumping = value;}

    /**
     * Returns the number of frames player can ascend by holding the jump key
     *
     * @return the time in frames
     */
    public int getJumpTime() { return jumpTime; }

    /**
     * Returns the remaining number of frames that the player can ascend by holding the jump key
     *
     * @return the remaining time in frames
     */
    public int getJumpTimeRemaining() {return jumpTimeRemaining; }

    /**
     * Sets the remaining time in frames that the player can ascend by holding the jump key
     *
     * @param value the remaining time in frames
     */
    public void setJumpTimeRemaining(int value) { jumpTimeRemaining = value; }

    public float getSpiritIncreaseRate() { return spiritIncreaseRate; }
    public void increaseSpirit() { setSpirit(Math.min(getSpirit() + getSpiritIncreaseRate(), maxSpirit)); }
    public float getSpiritDecreaseRate() { return spiritDecreaseRate; }
    public void decreaseSpirit() { setSpirit(Math.max(getSpirit() - getSpiritDecreaseRate(), 0)); }
    public float getSpiritIncreaseDist() { return spiritIncreaseDist; }

    public boolean isHit() { return isHit; }
    public void setHit(boolean value) { isHit = value; }

    public float getHitDist() {return hitDist; }

    public int getIFrames() { return iFrames; }
    public int getiFramesRemaining() { return iFramesRemaining; }
    public void setiFramesRemaining(int value) { iFramesRemaining = value; }

    public boolean isAttacking() { return isAttacking;}
    public void setAttacking(boolean value) { isAttacking = value; }

    public float getAttackDist() { return attackDist; }



    //#endregion

    public void draw(GameCanvas canvas) {
        //tile of player character
        float x = getX();
        float y = getY();

        //position of player in tile
        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight()/2 - 100;

        // Scaling Factor of Player - Determined by momoImageWidth/momoImageHeight in constants.json
        float sx = (isFacingRight ? 1 : -1) * momoImageWidth / this.texture.getRegionWidth();
        float sy = momoImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }

    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) return false;
//        body = world.createBody(bodyDef);
//        body.setUserData(this);
//
//        PolygonShape hitbox = new PolygonShape();
//        hitbox.set(new float[]{
//                momoImageWidth * (1-hitboxWidthMult), momoImageHeight * (1-hitboxHeightMult),
//                momoImageWidth * hitboxWidthMult, momoImageHeight * (1-hitboxHeightMult),
//                momoImageWidth * hitboxWidthMult, momoImageHeight * hitboxHeightMult,
//                momoImageWidth * (1-hitboxWidthMult), momoImageHeight * hitboxHeightMult,
//        });
//
//        fixtureDef.shape = hitbox;
//        body.createFixture(fixtureDef);
        body.setGravityScale(5.0f);
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
        System.out.println("true");
        sensorShape.setAsBox(0.6f*getWidth()/2.0f,
                0.05f, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        return true;
    }

    /**
     * Dash, Momo, Dash
     *
     * Implemented 8 directional dash
     * */
    public void dash(){
        if(angleFacing == 0){
            forceCache.set(hit_force * dash * 1, 0);
        }
        else if (angleFacing == 45){
            forceCache.set(hit_force * dash * 1, hit_force * dash * 1);
        }
        else if (angleFacing == 90){
            forceCache.set(0, hit_force * dash * 1);
        }
        else if (angleFacing == 135){
            forceCache.set(hit_force * dash * -1, hit_force * dash * 1);
        }
        else if (angleFacing == 180){
            forceCache.set(hit_force * dash * -1, 0);
        }
        else if (angleFacing == 225){
            forceCache.set(hit_force * dash * -1, hit_force * dash * -1);
        }
        else if (angleFacing == 270){
            forceCache.set(0, hit_force * dash * -1);
        }
        else if (angleFacing == 315){
            forceCache.set(hit_force * dash * 1, hit_force * dash * -1);
        }
        body.applyForce(forceCache, getPosition(), true);
    }

    /**
     * Called when the character is hit by an enemy.
     *
     * This method decrements the number of hearts for the character by 1. If the number of hearts
     * reaches 0, this method destroys the character
     */
    public void hitByEnemy() {

        if (isHit() && hearts > 0){
            hearts--;
            if (hearts > 0) {
                setVelocity(getBodyVelocityX(), 2.0f);
                setiFramesRemaining(getIFrames());
            }

            else this.markRemoved(true);
        }

    }


    public Player(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"), json.getFloat("hitboxWidth"), json.getFloat("hitboxHeight"));
        String momoTextureAsset = json.getString("momo:TextureAsset");
        this.momoTexture = new TextureRegion(assets.getEntry(momoTextureAsset, Texture.class));
        this.momoImageWidth = json.getFloat("momo:ImageWidth");
        this.momoImageHeight = json.getFloat("momo:ImageHeight");
        this.chiyoTexture = new TextureRegion(assets.getEntry(json.getString("chiyo:TextureAsset"), Texture.class));
        this.chiyoImageWidth = json.getFloat("chiyo:ImageWidth");
        this.chiyoImageHeight = json.getFloat("chiyo:ImageHeight");
        this.swordSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:swordAttack", Texture.class));
        this.dashSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:dash", Texture.class));

        //Position and Movement
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");
        this.dashCooldown = json.getInt("dashCooldown");
        maxSpeed = json.getFloat("maxSpeed");
        horizontalAcceleration = json.getFloat("horizontalAcceleration");
        hit_force = json.getFloat( "hit_force");
        dash = json.getFloat("dash", 2000);

        //Attacking
        this.attackPower = json.getInt("attackPower");
        this.attackCooldown = json.getInt("attackCooldown");
        this.transformCooldown = json.getInt("transformCooldown");
        this.attackOffset = json.getFloat("attackOffset");
        this.swordRadius = json.getFloat("swordRadius");
        this.attackLifespan = json.getFloat("attackLifespan");


        //Other Information
        this.maxHearts = json.getInt("maxHearts");
        this.initialHearts = json.getInt("initialHearts");
        this.maxSpirit = json.getFloat("maxSpirit");
        this.initialSpirit = json.getFloat("initialSpirit");
        this.spiritPerSecond = json.getFloat("spiritPerSecond");
        this.startsFacingRight = json.getBoolean("startsFacingRight");
        this.hitCooldown = json.getInt("hitCooldown");
        this.jumpCooldown = json.getInt("jumpCooldown");
        this.coyoteFrames = json.getInt("coyoteTime");
        this.jumpTolerance = json.getInt("jumpTolerance");
        this.jumpTime = json.getInt("jumpTime");
        this.maxJumpVelocity = json.getInt("maxJumpyVelocity");
        this.spiritIncreaseRate = json.getFloat("spiritIncreaseRate");
        this.spiritDecreaseRate = json.getFloat("spiritDecreaseRate");
        this.spiritIncreaseDist = json.getFloat("spiritIncreaseDist");
        this.hitDist = json.getFloat("hit_dist");
        this.iFrames = json.getInt("iFrames");
        this.attackDist = json.getFloat("attack_dist");

        this.isChiyo = false;

        this.hearts = initialHearts;
        this.spirit = initialSpirit;

        this.isDashing = false;
        this.isJumping = false;
        this.isChiyo = false;
        this.isHit = false;
        this.isGrounded = true;
        this.isFacingRight = startsFacingRight;
        this.isLookingUp = false;
        this.isLookingDown = false;
        this.jumpPressedInAir = false;
        this.isAttacking = false;
        this.dashCooldownRemaining = 0;
        this.attackCooldownRemaining = 0;
        this.hitCooldownRemaining = 0;
        this.jumpCooldownRemaining = 0;
        this.coyoteFramesRemaining = 0;
        this.jumpToleranceRemaining = 0;
        this.jumpTimeRemaining = 0;
        this.iFramesRemaining = 0;
        this.attackLifespanRemaining = 0;

        this.texture = momoTexture;
        this.data = json;
        sensorName = "PlayerGroundSensor";
    }

}
