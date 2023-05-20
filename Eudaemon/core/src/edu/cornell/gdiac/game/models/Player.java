package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;
import edu.cornell.gdiac.game.obstacle.WheelObstacle;
import java.util.Random;

public class Player extends CapsuleObstacle {

    //#region FINAL FIELDS

    private final JsonValue playerData;

    private final float spiritKillingEnemy;
    private final int maxHearts;
    private final float initialHearts;
    private final float maxSpirit;
    private final float initialSpirit;
    private final int attackPower;
    private final float downwardAttackPropelX;
    private final float downwardAttackPropelY;
    /**
     * How much max speed is multiplied by for Chiyo
     */
    private final float chiyoSpeedMult;
    private final boolean startsFacingRight;

    /**
     * The velocity at which the player slides down a wall.
     */
    private float wallSlideVelocity;
    /** The number of ticks required between wall jumps*/
    private final int wallJumpCooldownTicks;

    /**
     * Time in milliseconds during which gravity doesn't affect the player
     */
    private final int dashTime;
    private final int dashCooldown;
    /**
     * The amount of ticks before the palyer can attack again
     */
    private final int attackCooldown;
    /**
     * The amount of ticks the player can final before a large impact is created
     */
    private final int timeForImpact;

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

    /**
     * The horizontal velocity at which the player jumps off a wall.
     */
    private final float wallJumpXVelocity;

    private final float spiritIncreaseRate;
    private final float spiritDecreaseRate;
    private final float spiritIncreaseDist;

    /**
     * The distance the center of the attack is offset from the player
     */
    private final float attackOffset;

    /**
     * The radius of the sword used by the player.
     */
    private final float swordRadius;
    /**
     * The lifespan of the sword attack animation in seconds.
     */
    private final float attackLifespan;

    private final int dashLifespan;

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();

    private final float hitDist;

    private final int iFrames;

    private final float attackDist;

    private final float chiyoJumpTimeMult;

    private final float chiyoHitBoxHeightMult;

    //#endregion

    //#region TEXTURES
    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion momoTexture;
    private final float momoImageWidth;
    private final float momoImageHeight;
    private final TextureRegion chiyoTexture;
    private final float chiyoImageWidth;
    private final float chiyoImageHeight;
    private final TextureRegion momoDiagonalDashTexture;
    private final TextureRegion chiyoSlideTexture;
    /** TextureRegion for Momo's up-dash state */
    private final TextureRegion momoUpDashTexture;
    /** TextureRegion for Momo's down-dash state */
    private final TextureRegion momoDownDashTexture;
    /** TextureRegion for Momo's sliding state */
    private final TextureRegion momoSlideTexture;


    /**
     * The sprite sheet containing the wall hit effect animation frames.
     */
    private final Animation<TextureRegion> hitWallEffect;
    /**
     * The sprite sheet containing the 0 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet0;
    /**
     * The sprite sheet containing the effect animation for impacts
     */
    private final TextureRegion impactEffectSpriteSheet;
    /**
     * The sprite sheet containing the 45 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet45;
    /**
     * The sprite sheet containing the 90 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet90;
    /**
     * The sprite sheet containing the 135 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet135;
    /**
     * The sprite sheet containing the 180 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet180;
    /**
     * The sprite sheet containing the 225 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet225;
    /**
     * The sprite sheet containing the 270 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet270;
    /**
     * The sprite sheet containing the 315 angle sword attack animation frames.
     */
    private final TextureRegion swordEffectSpriteSheet315;


    /**
     * The sprite sheet containing the dash attack animation frames.
     */
    private final TextureRegion dashEffectSpriteSheet;

    /**
     * The sprite sheet containing the spirit drain animation frames.
     */
    private final TextureRegion spiritDrainSpriteSheet;
    /**
     * The animation for the spirit drain animation frames.
     */
    private final Animation<TextureRegion> spiritDrainAnimation;
    /**
     * The sprite sheet containing the momo dash animation frames.
     */
    private final TextureRegion momoDashSpriteSheet;

    /**
     * The sprite sheet containing the momo run animation frames.
     */
    private final TextureRegion momoRunSpriteSheet;

    /**
     * The sprite sheet containing the momo jump animation frames.
     */
    private final TextureRegion momoJumpSpriteSheet;

    /**
     * The sprite sheet containing the chiyo run animation frames.
     */
    private final TextureRegion chiyoRunSpriteSheet;

    /**
     * The sprite sheet containing the chiyo attack animation frames.
     */
    private TextureRegion chiyoAttackSpriteSheet;

    /**
     * The sprite sheet containing the chiyo jump animation frames.
     */
    private TextureRegion chiyoJumpSpriteSheet;

    private TextureRegion regainHealthSpriteSheet;

    //#endregion

    //#region NONFINAL FIELDS
    /**
     * The amount of ticks the player has spent falling
     */
    private int ticksFalling;
    /**
     * The amount of ticks the player has spent since touching the ground
     */
    private int ticksSinceGround;
    /**
     * A boolean variable representing whether the player's right is touching the wall or not.
     */
    private boolean isTouchingWallRight;
    /**
     * A boolean variable representing whether the player's left is touching the wall or not.
     */
    private boolean isTouchingWallLeft;
    /** The amount player's drawing scale is multiplied by on the x-axis. */
    private float sxMult = 1;

    /** The amount player's drawing scale is multiplied by on the y-axis. */
    private float syMult = 1;
    /**
     * The offset value along the x-axis.
     */
    private float oxOffset;

    /**
     * The offset value along the y-axis.
     */
    private float oyOffset;
    private float hearts;
    private float spirit;

    /**
     * The maximum horizontal speed that the object can reach.
     */
    private float maxSpeed;

    /**
     * The horizontal acceleration of the object.
     */
    private final float horizontalAcceleration;

    private boolean isDashing;
    private boolean isJumping;
    private boolean isChiyo;
    private boolean isHit;
    private boolean isGrounded;
    private boolean isFacingRight;
    private boolean dashedInAir;
    private boolean isGainingSpirit;
    private boolean isGainingHealth;
    private Random random;
    private boolean shouldRespawn;
    private Vector2 respawnPosition;

    private Array<Enemy> enemiesInSpiritRange;
    /**
     * The angle at which the entity is facing, in degrees.
     */
    private int angleFacing;

    /**
     * Private boolean to describe whether the player is sliding on a wall.
     */
    private boolean isSliding;

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

    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;

    /** The amount by which the player should move when dashing*/
    private final float dash;

    /** The shape for the ground sensor */
    private PolygonShape groundSensorShape;
    /** The shape for the wall sensor */
    private PolygonShape wallSensorShapeRight;
    /** The shape for the wall sensor */
    private PolygonShape wallSensorShapeLeft;

    /** The shape for the spirit sensor */
    private CircleShape spiritSensorShape;

    /** Identifier to allow us to track the ground sensor in ContactListener */
    private final String groundSensorName;
    /** Identifier to allow us to track the right wall sensor in ContactListener */
    private final String wallSensorNameRight;
    /** Identifier to allow us to track the left wall sensor in ContactListener */
    private final String wallSensorNameLeft;

    /** Identifier to allow us to track the spirit sensor in ContactListener */
    private final String spiritSensorName;

    private final int jumpTime;
    private int jumpTimeRemaining;

    /**iFrames is the frame count of being able to GET attacked again*/
    private int iFramesRemaining;

    private boolean isAttacking;
    private float attackLifespanRemaining;
    private int dashLifespanRemaining;
    private final int dashIFrames;

    /**
     * A float representing the max jump velocity of a game object.
     */
    private final float maxJumpVelocity;
    /**
     * A final float representing the gravity affecting the player.
     */
    private final float playerGravity;

    private long playerDamageSoundId = -1;

    /**The sprite sheet for bullet sprite**/
    private TextureRegion bulletDestroySpriteSheet;
    /** animation for destroying bullet**/
    private final Animation<TextureRegion> bulletDestroyAnimation;

    private TextureRegion transformSpriteSheet;
    private final Animation<TextureRegion> transformAnimation;



    //#endregion

    //#region TEXTURE GETTERS AND SETTERS
    /**
     * Returns the TextureRegion for Momo's up-dash state.
     *
     * @return The TextureRegion for Momo's up-dash state.
     */
    public TextureRegion getMomoUpDashTexture() {
        return momoUpDashTexture;
    }

    /**
     * Returns the TextureRegion for Momo's down-dash state.
     *
     * @return The TextureRegion for Momo's down-dash state.
     */
    public TextureRegion getMomoDownDashTexture() {
        return momoDownDashTexture;
    }

    /**
     * Returns the TextureRegion for Momo's sliding state.
     *
     * @return The TextureRegion for Momo's sliding state.
     */
    public TextureRegion getMomoSlideTexture() {
        return momoSlideTexture;
    }
    /**
     * Gets the texture region for the hit wall effect.
     *
     * @return the texture region for the hit wall effect
     */
    public Animation<TextureRegion> getHitWallEffect() {
        return hitWallEffect;
    }
    /**
     * Returns the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Momo diagonal dash texture.
     *
     * @return the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Momo diagonal dash texture.
     */
    public TextureRegion getMomoDiagonalDashTexture(){
        return momoDiagonalDashTexture;
    }

    /**
     * Returns the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Chiyo slide texture.
     *
     * @return the {@link com.badlogic.gdx.graphics.g2d.TextureRegion} of the Chiyo Slide texture.
     */
    public TextureRegion getChiyoSlideTexture(){
        return chiyoSlideTexture;
    }

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
    public TextureRegion getSwordEffectSpriteSheet(float direction) {
        if(direction == 0){
            return swordEffectSpriteSheet0;
        }
        else if(direction == 45){
            return swordEffectSpriteSheet45;
        }
        else if(direction == 90){
            return swordEffectSpriteSheet90;
        }
        else if(direction == 135){
            return swordEffectSpriteSheet135;
        }
        else if(direction == 180){
            return swordEffectSpriteSheet180;
        }
        else if(direction == 225){
            return swordEffectSpriteSheet225;
        }
        else if(direction == 270){
            return swordEffectSpriteSheet270;
        }
        else if(direction == 315){
            return swordEffectSpriteSheet315;
        }
        return swordEffectSpriteSheet0;
    }
    /**
     * Gets the sprite sheet containing the dash animation frames.
     *
     * @return the sprite sheet containing the dash animation frames
     */
    public TextureRegion getDashEffectSpriteSheet() {
        return dashEffectSpriteSheet;
    }
    /**
     * Gets the sprite sheet containing the impact animation frames
     *
     * @return the sprite sheet containing the impact animation frames
     */
    public TextureRegion getImpactEffectSpriteSheet() {
        return impactEffectSpriteSheet;
    }
    /**
     * Gets the sprite sheet containing the spirit drain animation frames.
     *
     * @return the sprite sheet containing the spirit drain animation frames
     */
    public TextureRegion getSpiritDrainSpriteSheet() {
        return spiritDrainSpriteSheet;
    }

    /**
     * Gets the animation containing the spirit drain animation frames.
     *
     * @return the sprite sheet containing the spirit drain animation frames
     */
    public Animation<TextureRegion> getSpiritDrainAnimation() {
        return spiritDrainAnimation;
    }

    public TextureRegion getBulletDestroySpriteSheet(){
        return bulletDestroySpriteSheet;
    }
    public Animation<TextureRegion> getBulletDestroyAnimation(){
        return bulletDestroyAnimation;
    }
    public TextureRegion getTransformSpriteSheet(){
        return transformSpriteSheet;
    }
    public Animation<TextureRegion> getTransformAnimation(){
        return transformAnimation;
    }
    /**
     * Get momoDashSpriteSheet.
     *
     * @return The momoDashSpriteSheet TextureRegion.
     */
    public TextureRegion getMomoDashSpriteSheet() {
        return momoDashSpriteSheet;
    }

    /**
     * Get momoRunSpriteSheet.
     *
     * @return The momoRunSpriteSheet TextureRegion.
     */
    public TextureRegion getMomoRunSpriteSheet() {
        return momoRunSpriteSheet;
    }

    /**
     * Get momoJumpSpriteSheet.
     *
     * @return The momoJumpSpriteSheet TextureRegion.
     */
    public TextureRegion getMomoJumpSpriteSheet() {
        return momoJumpSpriteSheet;
    }

    /**
     * Get chiyoRunSpriteSheet.
     *
     * @return The chiyoRunSpriteSheet TextureRegion.
     */
    public TextureRegion getChiyoRunSpriteSheet() {
        return chiyoRunSpriteSheet;
    }

    /**
     * Get chiyoAttackSpriteSheet.
     *
     * @return The chiyoAttackSpriteSheet TextureRegion.
     */
    public TextureRegion getChiyoAttackSpriteSheet() {
        return chiyoAttackSpriteSheet;
    }

    /**
     * Get chiyoJumpSpriteSheet.
     *
     * @return The chiyoJumpSpriteSheet TextureRegion.
     */
    public TextureRegion getChiyoJumpSpriteSheet() {
        return chiyoJumpSpriteSheet;
    }

    public TextureRegion getRegainHealthSpriteSheet() { return regainHealthSpriteSheet; }

    //#endregion

    //#region GETTERS AND SETTERS
    /**
     * Returns the number of ticks for the wall jump cooldown.
     *
     * @return The number of ticks for the wall jump cooldown.
     */
    public int getWallJumpCooldownTicks() {
        return wallJumpCooldownTicks;
    }
    /**
     * Returns the horizontal velocity at which the player jumps off a wall.
     *
     * @return the wall jump X velocity
     */
    public float getWallJumpXVelocity() {
        return wallJumpXVelocity;
    }

    /**
     * Returns the velocity at which the player slides down a wall.
     *
     * @return the wall slide velocity
     */
    public float getWallSlideVelocity() {
        return wallSlideVelocity;
    }

    /**
     * Sets the velocity at which the player slides down a wall.
     *
     * @param wallSlideVelocity the new wall slide velocity
     */
    public void setWallSlideVelocity(float wallSlideVelocity) {
        this.wallSlideVelocity = wallSlideVelocity;
    }

    /**
     * Returns whether the player is currently sliding on a wall.
     *
     * @return true if the player is sliding, false otherwise
     */
    public boolean isSliding() {
        return isSliding;
    }

    /**
     * Sets whether the player is currently sliding on a wall.
     *
     * @param isSliding true if the player is sliding, false otherwise
     */
    public void setSliding(boolean isSliding) {
        this.isSliding = isSliding;
    }

    public Array<Enemy> getEnemiesInSpiritRange() {
        return enemiesInSpiritRange;
    }

    public String getSpiritSensorName() {
        return spiritSensorName;
    }

    public boolean isGainingSpirit() {
        return isGainingSpirit;
    }

    public void setGainingSpirit(boolean gainingSpirit) {
        isGainingSpirit = gainingSpirit;
    }

    public boolean isGainingHealth() {return isGainingHealth; }

    public void setGainingHealth(boolean value) { isGainingHealth = value; }

    /**
     * Gets the number of ticks the player spends falling.
     *
     * @return the number of ticks in the air
     */
    public int getTicksFalling() {
        return ticksFalling;
    }

    /**
     * Sets the number of ticks the player spends falling.
     *
     * @param ticksFalling the new number of ticks in the air
     */
    public void setTicksFalling(int ticksFalling) {
        this.ticksFalling = ticksFalling;
    }
    /**
     * Gets the number of ticks the player spends since touching the ground.
     *
     * @return the number of ticks in the air
     */
    public int getTicksSinceGround() {
        return ticksSinceGround;
    }

    /**
     * Sets the number of ticks the player spends falling.
     *
     * @param ticksSinceGround the new number of ticks since touching the ground
     */
    public void setTicksSinceGround(int ticksSinceGround) {
        this.ticksSinceGround = ticksSinceGround;
    }
    /**
     *  Returns the variable representing how many milliseconds gravity is turned off for the player when they dash
     * @return dashtime
     */
    public int getDashTime() {
        return dashTime;
    }
    /**
     * Retrieves if the player is touching the right wall
     *
     * @return The touching right wall state.
     */
    public boolean isTouchingWallRight() {
        return isTouchingWallRight;
    }
    /**
     * Sets if the player is touching the right wall
     */
    public void setTouchingWallRight(boolean slide) {
        isTouchingWallRight = slide;
    }
    /**
     * Retrieves if the player is touching the left wall
     *
     * @return The touching left wall state.
     */
    public boolean isTouchingWallLeft() {
        return isTouchingWallLeft;
    }
    /**Sets if the player is touching the left wall*/
    public void setTouchingWallLeft(boolean slide) {isTouchingWallLeft = slide;}
    /**
     * Gets the value of the chiyo's speed multiplier
     *
     * @return The value of chiyo's speed multiplier.
     */
    public float getChiyoHitBoxHeightMult() {
        return chiyoHitBoxHeightMult;
    }

    /**
     * Gets the value of the chiyo's speed multiplier
     *
     * @return The value of chiyo's speed multiplier.
     */
    public float getChiyoSpeedMult() {
        return chiyoSpeedMult;
    }

    /**
     * Gets the value of the chiyo's speed multiplier
     *
     * @return The value of chiyo's speed multiplier.
     */
    public float getChiyoJumpTimeMult() {
        return chiyoJumpTimeMult;
    }

    /**
     * Gets the value of the sxMult variable.
     *
     * @return The value of the sxMult variable.
     */
    public float getSxMult() {
        return sxMult;
    }

    /**
     * Sets the value of the sxMult variable.
     *
     * @param sxMult The value to set for the sxMult variable.
     */
    public void setSxMult(float sxMult) {
        this.sxMult = sxMult;
    }

    /**
     * Gets the value of the syMult variable.
     *
     * @return The value of the syMult variable.
     */
    public float getSyMult() {
        return syMult;
    }

    /**
     * Sets the value of the syMult variable.
     *
     * @param syMult The value to set for the syMult variable.
     */
    public void setSyMult(float syMult) {
        this.syMult = syMult;
    }
    /**
     * Get the x-axis offset value.
     *
     * @return The x-axis offset value as a float.
     */
    public float getOxOffset() {
        return oxOffset;
    }

    /**
     * Set the x-axis offset value.
     *
     * @param oxOffset The x-axis offset value as a float.
     */
    public void setOxOffset(float oxOffset) {
        this.oxOffset = oxOffset;
    }

    /**
     * Get the y-axis offset value.
     *
     * @return The y-axis offset value as a float.
     */
    public float getOyOffset() {
        return oyOffset;
    }

    /**
     * Set the y-axis offset value.
     *
     * @param oyOffset The y-axis offset value as a float.
     */
    public void setOyOffset(float oyOffset) {
        this.oyOffset = oyOffset;
    }
    /**
     * Returns the gravity value affecting the player.
     *
     * @return A float representing the player's gravity.
     */
    public float getPlayerGravity() {
        return playerGravity;
    }
    /**
     * Changes the player gravity for the body (not the max player gravity)
     *
     */
    public void setPlayerGravity(float pg) {
        body.setGravityScale(pg);
    }
    /**
     * Returns the jump velocity of the game object.
     *
     * @return A float representing the jump velocity.
     */
    public float getJumpVelocity() {
        if(form == 0) {
            return maxJumpVelocity;
        }
        else{
            return 1.3f * maxJumpVelocity;
        }
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
    public void setAttackLifespanRemaining(float value) {attackLifespanRemaining = value;}

    public int getDashLifespan() {
        return dashLifespan;
    }
    public int getDashLifespanRemaining() { return dashLifespanRemaining; }
    public int getDashIFrames() { return dashIFrames; }
    public void setDashLifespanRemaining(int value) {dashLifespanRemaining = value;}
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
    public int getDashCooldown() {
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
    public void setHearts(float value) {
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
    public void setDashCooldownRemaining(int dashCooldownRemaining) {
        this.dashCooldownRemaining = dashCooldownRemaining;
    }
    /**
     * Retrieves the dash velocity
     *
     * @return The dash velocity
     */
    public float getDash() {
        return dash;
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
    public int getDashCooldownRemaining() {
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
    public String getGroundSensorName() {
        return groundSensorName;
    }
    /**
     * Returns the name of the right wall sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the right wall sensor
     */
    public String getWallSensorNameRight() {
        return wallSensorNameRight;
    }
    /**
     * Returns the name of the left wall sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the left wall sensor
     */
    public String getWallSensorNameLeft() {return wallSensorNameLeft;}

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
     * Gets the amount of ticks in the air before the impact animation.
     *
     * @return the time for impact in ticks
     */
    public int getTimeForImpact() {
        return timeForImpact;
    }
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
    public int getJumpTime() {
        if(form == 0) {
            return jumpTime;
        }
        else{
            return (int) 1.3 * jumpTime;
        }

    }

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
    public void increaseSpiritByKill(){ setSpirit(Math.min(getSpirit() + spiritKillingEnemy, maxSpirit));}
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
    public boolean isDashing() { return isDashing;}
    public void setDashing(boolean value) { isDashing = value; }
    public float getAttackDist() { return attackDist; }
    public boolean dashedInAir() { return dashedInAir; }
    public void setDashedInAir(boolean value) { dashedInAir = value; }
    public float getDownwardAttackPropelX() {return downwardAttackPropelX; }
    public float getDownwardAttackPropelY() {return downwardAttackPropelY; }
    public boolean shouldRespawn() { return shouldRespawn; }
    public void setShouldRespawn(boolean value) { shouldRespawn = value; }
    public Vector2 getRespawnPosition() { return respawnPosition; }


    //#endregion

    public Player(AssetDirectory assets, int startXCoord, int startYCoord, float levelDifficulty) {
        super(startXCoord, startYCoord, assets.getEntry("sharedConstants", JsonValue.class).get("player").getFloat("hitboxWidth"), assets.getEntry("sharedConstants", JsonValue.class).get("player").getFloat("hitboxHeight"));

        //now the player's shared data is separated into sharedConstants.json, we shall not query them
        //from constants.json, as that file only records changing data.
        this.playerData = assets.getEntry("sharedConstants", JsonValue.class).get("Player");

        //Textures & animations
        this.momoTexture = new TextureRegion(assets.getEntry("momo:idle", Texture.class));
        this.momoImageWidth = playerData.getFloat("momo:ImageWidth");
        this.momoImageHeight = playerData.getFloat("momo:ImageHeight");
        this.chiyoTexture = new TextureRegion(assets.getEntry("chiyo:idle", Texture.class));
        this.chiyoImageWidth = playerData.getFloat("chiyo:ImageWidth");
        this.chiyoImageHeight = playerData.getFloat("chiyo:ImageHeight");
        this.momoDiagonalDashTexture = new TextureRegion(assets.getEntry("momo:diagonalDash", Texture.class));
        this.chiyoSlideTexture = new TextureRegion(assets.getEntry("chiyo:slide", Texture.class));
        this.momoSlideTexture = new TextureRegion(assets.getEntry("momo:slide", Texture.class));
        this.momoDownDashTexture = new TextureRegion(assets.getEntry("momo:downDash", Texture.class));
        this.momoUpDashTexture = new TextureRegion(assets.getEntry("momo:upDash", Texture.class));

        TextureRegion hitWallEffectSpriteSheet = new TextureRegion(assets.getEntry("chiyo:hitWallEffect", Texture.class));
        TextureRegion[][] framesHit = hitWallEffectSpriteSheet.split(hitWallEffectSpriteSheet.getRegionWidth() / 30, hitWallEffectSpriteSheet.getRegionHeight());
        hitWallEffect = new Animation<>(0.5f, framesHit[0]);
        this.swordEffectSpriteSheet0 = new TextureRegion(assets.getEntry( "chiyo:swordAttack0", Texture.class));
        this.swordEffectSpriteSheet45 = new TextureRegion(assets.getEntry( "chiyo:swordAttack45", Texture.class));
        this.swordEffectSpriteSheet90 = new TextureRegion(assets.getEntry( "chiyo:swordAttack90", Texture.class));
        this.swordEffectSpriteSheet135 = new TextureRegion(assets.getEntry( "chiyo:swordAttack135", Texture.class));
        this.swordEffectSpriteSheet180 = new TextureRegion(assets.getEntry( "chiyo:swordAttack180", Texture.class));
        this.swordEffectSpriteSheet225 = new TextureRegion(assets.getEntry( "chiyo:swordAttack225", Texture.class));
        this.swordEffectSpriteSheet270 = new TextureRegion(assets.getEntry( "chiyo:swordAttack270", Texture.class));
        this.swordEffectSpriteSheet315 = new TextureRegion(assets.getEntry( "chiyo:swordAttack315", Texture.class));
        this.dashEffectSpriteSheet = new TextureRegion(assets.getEntry( "momo:dashEffect", Texture.class));
        this.spiritDrainSpriteSheet = new TextureRegion(assets.getEntry("momo:spiritDrain", Texture.class));
        TextureRegion[][] frames = spiritDrainSpriteSheet.split(spiritDrainSpriteSheet.getRegionWidth() /14, spiritDrainSpriteSheet.getRegionHeight());
        this.spiritDrainAnimation = new Animation<>(0.5f, frames[0]);

        this.momoDashSpriteSheet = new TextureRegion(assets.getEntry( "momo:dash", Texture.class));
        this.momoRunSpriteSheet = new TextureRegion(assets.getEntry( "momo:run", Texture.class));
        this.momoJumpSpriteSheet = new TextureRegion(assets.getEntry( "momo:jump", Texture.class));
        this.chiyoRunSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:run", Texture.class));
        this.chiyoJumpSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:jump", Texture.class));
        this.chiyoAttackSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:attack", Texture.class));
        this.impactEffectSpriteSheet = new TextureRegion(assets.getEntry( "impactEffect", Texture.class));
        this.bulletDestroySpriteSheet = new TextureRegion(assets.getEntry("bulletDestroy", Texture.class));
        this.regainHealthSpriteSheet = new TextureRegion(assets.getEntry("platform:regainHealth", Texture.class));
        TextureRegion[][] bulletDestroyFrames = bulletDestroySpriteSheet.split(bulletDestroySpriteSheet.getRegionWidth()/16, bulletDestroySpriteSheet.getRegionHeight());
        this.bulletDestroyAnimation = new Animation<>(0.5f, bulletDestroyFrames[0]);
        //Sound Effect
        this.transformSpriteSheet = new TextureRegion(assets.getEntry("player:transform", Texture.class));
        TextureRegion[][] transformFrames = transformSpriteSheet.split(transformSpriteSheet.getRegionWidth()/7, transformSpriteSheet.getRegionHeight());
        this.transformAnimation = new Animation<>(0.5f, transformFrames[0]);

        //Position and Movement
        this.dashCooldown = playerData.getInt("dashCooldownInFrames");
        maxSpeed = playerData.getFloat("maxSpeed");
        horizontalAcceleration = playerData.getFloat("horizontalAcceleration");
        this.dash = playerData.getFloat("dash", 2000);
        this.dashLifespan = playerData.getInt("dashLifespan");
        this.dashTime = playerData.getInt("dashTime");
        this.dashIFrames = playerData.getInt("dashIFrames");
        this.chiyoSpeedMult = playerData.getFloat("chiyoSpeedMult");
        this.chiyoJumpTimeMult = playerData.getFloat("chiyoJumpTimeMult");
        this.chiyoHitBoxHeightMult = playerData.getFloat("chiyoHitboxHeightMult");

        //Sliding
        this.wallSlideVelocity = playerData.getFloat("wallSlideVelocity");
        this.wallJumpXVelocity = playerData.getFloat("wallJumpXVelocity");
        this.wallJumpCooldownTicks = playerData.getInt("wallJumpCooldownTicks");

        //Attacking
        this.attackPower = playerData.getInt("attackPower");
        this.attackCooldown = playerData.getInt("attackCooldown");
        this.timeForImpact = playerData.getInt("timeForImpact");
        this.transformCooldown = playerData.getInt("transformCooldown");
        this.attackOffset = playerData.getFloat("attackOffset");
        this.swordRadius = playerData.getFloat("swordRadius");
        this.attackLifespan = playerData.getFloat("attackLifespan");
        this.spiritKillingEnemy= playerData.getFloat("spiritKillingEnemy");


        //Other Information
        this.maxHearts = playerData.getInt("maxHearts");
//        this.initialHearts = playerData.getInt("initialHearts");
        this.initialHearts = levelDifficulty;
        this.maxSpirit = playerData.getFloat("maxSpirit");
        this.initialSpirit = playerData.getFloat("initialSpirit");
        this.startsFacingRight = playerData.getBoolean("startsFacingRight");
        this.jumpCooldown = playerData.getInt("jumpCooldown");
        this.coyoteFrames = playerData.getInt("coyoteTime");
        this.jumpTolerance = playerData.getInt("jumpTolerance");
        this.jumpTime = playerData.getInt("jumpTime");
        this.maxJumpVelocity = playerData.getInt("maxJumpyVelocity");
        this.playerGravity = playerData.getFloat("playerGravity");
        this.spiritIncreaseRate = playerData.getFloat("spiritIncreaseRate");
        this.spiritDecreaseRate = playerData.getFloat("spiritDecreaseRate");
        this.spiritIncreaseDist = playerData.getFloat("spiritIncreaseDist");
        this.hitDist = playerData.getFloat("hit_dist");
        this.iFrames = playerData.getInt("iFrames");
        this.attackDist = playerData.getFloat("attack_dist");
        this.downwardAttackPropelX = playerData.getFloat("downwardAttackPropelX");
        this.downwardAttackPropelY = playerData.getFloat("downwardAttackPropelY");


        this.isChiyo = false;

        this.hearts = initialHearts;
        this.spirit = 10;

        this.isDashing = false;
        this.isJumping = false;
        this.isChiyo = false;
        this.isHit = false;
        this.isGrounded = true;
        this.isFacingRight = startsFacingRight;
        this.jumpPressedInAir = false;
        this.isAttacking = false;
        this.dashedInAir = false;
        this.dashCooldownRemaining = 0;
        this.attackCooldownRemaining = 0;
        this.jumpCooldownRemaining = 0;
        this.coyoteFramesRemaining = 0;
        this.jumpToleranceRemaining = 0;
        this.jumpTimeRemaining = 0;
        this.iFramesRemaining = 0;
        this.attackLifespanRemaining = 0;
        this.dashLifespanRemaining = 0;

        this.texture = momoTexture;
        groundSensorName = "PlayerGroundSensor";
        wallSensorNameRight = "PlayerWallSensorRight";
        wallSensorNameLeft = "PlayerWallSensorLeft";
        spiritSensorName = "PlayerSpiritSensor";

        //respawn when hit by certain spike
        this.shouldRespawn = false;
        this.respawnPosition = new Vector2();

        this.enemiesInSpiritRange = new Array<>();

        this.random = new Random();
    }

    public void draw(GameCanvas canvas) {
        //tile of player character
        float x = getX();
        float y = getForm() == 0 ? getY() - 0.25f : getY() - 0.45f;

        //position of player in tile
        float ox = this.texture.getRegionWidth()/2 + oxOffset;
        float oy = this.texture.getRegionHeight()/2 + oyOffset;

        // Scaling Factor of Player - Determined by momoImageWidth/momoImageHeight in constants.json
        float sx = sxMult * (isFacingRight ? 1 : -1) * momoImageWidth / this.texture.getRegionWidth();
        float sy = syMult * momoImageHeight / this.texture.getRegionHeight();

        if (isHit() && iFramesRemaining > 0)
            canvas.draw(this.texture, Color.RED, ox, oy, x, y, 0, sx, sy);
        else canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }


    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(groundSensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(spiritSensorShape,Color.CYAN,getX(),getY(),drawScale.x,drawScale.y);
        canvas.drawPhysics(wallSensorShapeRight,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(wallSensorShapeLeft,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }


    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) return false;

        //Set Gravity
        body.setGravityScale(playerGravity);

        // // Ground sensor to represent our feet
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        groundSensorShape = new PolygonShape();
        JsonValue sensorjv = playerData.get("sensor");
        groundSensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = groundSensorShape;
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getGroundSensorName());

        // Wall sensor to represent our front
        Vector2 sensorCenter2 = new Vector2(getWidth()/1.8f, 0);
        FixtureDef sensorDef2 = new FixtureDef();
//        sensorDef2.density = playerData.getFloat("density",0);
        sensorDef2.density = 0;
        sensorDef2.isSensor = true;
        wallSensorShapeRight = new PolygonShape();
        JsonValue sensorjv2 = playerData.get("wallSensor");
        wallSensorShapeRight.setAsBox(sensorjv2.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv2.getFloat("height",0), sensorCenter2, 0.0f);
        sensorDef2.shape = wallSensorShapeRight;
        Fixture sensorFixture2 = body.createFixture( sensorDef2 );
        sensorFixture2.setUserData(getWallSensorNameRight());

        Vector2 sensorCenter3 = new Vector2(-getWidth()/1.8f, 0);
        FixtureDef sensorDef3 = new FixtureDef();
        sensorDef3.density = playerData.getFloat("density",0);
        sensorDef3.isSensor = true;
        wallSensorShapeLeft = new PolygonShape();
        wallSensorShapeLeft.setAsBox(sensorjv2.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv2.getFloat("height",0), sensorCenter3, 0.0f);
        sensorDef3.shape = wallSensorShapeLeft;
        Fixture sensorFixture3 = body.createFixture( sensorDef3 );
        sensorFixture3.setUserData(getWallSensorNameLeft());

//        // Rectangle Sensor for Wall Slides
//        spiritSensorShape = new BoxObstacle();
//        spiritSensorShape.setRadius(getSpiritIncreaseDist());
//        sensorDef.shape = spiritSensorShape;
//
//        Fixture spiritSensorFixture = body.createFixture(sensorDef);
//        spiritSensorFixture.setUserData(getSpiritSensorName());


        //create spirit gain sensor
        spiritSensorShape = new CircleShape();
        spiritSensorShape.setRadius(getSpiritIncreaseDist());
        sensorDef.shape = spiritSensorShape;

        Fixture spiritSensorFixture = body.createFixture(sensorDef);
        spiritSensorFixture.setUserData(getSpiritSensorName());


        return true;
    }

    public void updateGroundSensor(){
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = playerData.getFloat("density",0);
        sensorDef.isSensor = true;
        groundSensorShape = new PolygonShape();
        JsonValue sensorjv = playerData.get("sensor");
        groundSensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = groundSensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getGroundSensorName());
    }

    public void updateWallSensor(){
        Vector2 sensorCenter = new Vector2(getWidth()/2, getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = playerData.getFloat("density",0);
        sensorDef.isSensor = true;
        wallSensorShapeRight = new PolygonShape();
        JsonValue sensorjv = playerData.get("wallSensor");
        wallSensorShapeRight.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = wallSensorShapeRight;
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getWallSensorNameRight());

        Vector2 sensorCenter3 = new Vector2(-getWidth()/2, 0);
        FixtureDef sensorDef3 = new FixtureDef();
        sensorDef3.density = playerData.getFloat("density",0);
        sensorDef3.isSensor = true;
        wallSensorShapeLeft = new PolygonShape();
        wallSensorShapeLeft.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter3, 0.0f);
        sensorDef3.shape = wallSensorShapeLeft;
        Fixture sensorFixture3 = body.createFixture( sensorDef3 );
        sensorFixture3.setUserData(getWallSensorNameLeft());
    }

    /**
     * Called when the character is hit by an enemy.
     * This method decrements the number of hearts for the character by 1. If the number of hearts
     * reaches 0, this method destroys the character
     * @param whichObstacle : the parameter specifying which object the player is hit by:
     *                      0 for Enemy;
     *                      1 for Spike;
     *                      2 for WheelObstacle (bullet);
     *                      3 for SwordWheelObstacle;
     */
    public boolean hitByEnemy(int whichObstacle, Object hitter) {
        if (!isHit() || hearts <= 0)
            return false;

        hearts--;
        if (hearts > 0) {
            float directionX = 0;
            float spikeKnockBackVerticalMult = 0;
            switch (whichObstacle){
                case 0:
                    directionX = ((Enemy) hitter).getX() - this.getX() >= 0? -1: 1; break;
                case 1:
                    Spike spike = (Spike) hitter;
                    if (!spike.getRespawn()) { //knock player back
                        String direction= spike.getDirection();
                        switch (direction){
                            case "Up":
                                directionX=0;
                                spikeKnockBackVerticalMult=2;
                                break;
                            case "Down":
                                directionX=0;
                                spikeKnockBackVerticalMult=-2;
                                break;
                            case "Left":
                                directionX=-1;
                                spikeKnockBackVerticalMult=0;
                                break;
                            case "Right":
                                directionX=1;
                                spikeKnockBackVerticalMult=0;
                                break;
                            default:
                                //should not reach here
                                throw new IllegalArgumentException("Spike direction does not exist: "+direction);
                        }
                    } else { //teleport player to platform
                        this.shouldRespawn = true;
                        this.respawnPosition.x = spike.getRespawnCoordinateX();
                        this.respawnPosition.y = spike.getRespawnCoordinateY();
                    }

                    break;
                case 2:
                    directionX = ((WheelObstacle) hitter).getX() - this.getX() >=0? -1:1;break;
                case 3:
                    directionX = ((SwordWheelObstacle) hitter).getX() - this.getX() >=0? -1:1;break;

                default:
                    directionX = isFacingRight? -1: 1;
            }
            //setVelocity(getBodyVelocityX(), 2.0f);
            setiFramesRemaining(getIFrames());
            Vector2 knockback = new Vector2(directionX * playerData.getFloat("knockbackX"),
                    spikeKnockBackVerticalMult * playerData.getFloat("knockbackY"));
            setVelocity(knockback.x, knockback.y);
        } else this.markRemoved(true);

        return true;
    }

    public void gainHealth(Level thisLevel) {
        if (getHearts() < maxHearts && getHearts() < thisLevel.getLevelDifficulty()) {
            int randomNumber = random.nextInt(5) + 1;
            if (randomNumber == 3) {
                setGainingHealth(true);
                hearts++;
            }
        }
    }


    public void dispose() {}


}
