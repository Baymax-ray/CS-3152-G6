package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
    private final int attackCooldown;
    /**
     * The distance the center of the attack is offset from the player
     */
    private final float attackOffset;
    private final int hitCooldown;

    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion momoTexture;
    private final float momoImageWidth;
    private final float momoImageHeight;
    /**
     * The multiplier used to calculate the width of the hitbox.
     */
    private final float hitboxWidthMult;

    /**
     * The multiplier used to calculate the height of the hitbox.
     */
    private final float hitboxHeightMult;
    /**
     * The radius of the sword used by the player.
     */
    private final float swordRadius;
    /**
     * The sprite sheet containing the sword attack animation frames.
     */
    private final TextureRegion swordSpriteSheet;

    /**
     * The lifespan of the sword attack animation in seconds.
     */
    private final float attackLifespan;
    /**
     * The scaling factor for the sprite.
     */
    private final Vector2 scale;

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

    private int dashCooldownRemaining;
    /**
     * The remaining time in seconds until the player can attack again.
     */
    private int attackCooldownRemaining;
    private int hitCooldownRemaining;

    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;

    //#endregion

    //#region GETTERS AND SETTERS
    /**
     * Gets the radius of the sword hitbox.
     *
     * @return the radius of the sword hitbox
     */
    public float getSwordRadius() {
        return swordRadius;
    }

    /**
     * Gets the scale of the sword sprite.
     *
     * @return the scale of the sword sprite
     */
    public Vector2 getScale() {
        return scale;
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
     * Gets the lifespan of the sword attack animation in seconds.
     *
     * @return the sword attack animation lifespan
     */
    public float getAttackLifespan() {
        return attackLifespan;
    }
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
     * Returns the remaining time in seconds until the player can attack again.
     * @return the remaining time in seconds
     */
    public int getAttackCooldownRemaining() {
        return attackCooldownRemaining;
    }
    //#endregion

    public void draw(GameCanvas canvas) {
        float x = getX();
        float y = getY();

        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight()/2;

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
        body.setGravityScale(2.0f);
        return true;
    }

    public Player(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"), json.getFloat("hitboxWidth"), json.getFloat("hitboxHeight"));
        String momoTextureAsset = json.getString("momoTextureAsset");
        this.momoTexture = new TextureRegion(assets.getEntry(momoTextureAsset, Texture.class));
        this.momoImageWidth = json.getFloat("momoImageWidth");
        this.momoImageHeight = json.getFloat("momoImageHeight");
        this.scale = new Vector2(json.getFloat("drawScaleX"), json.getFloat("drawScaleY"));

        //Position and Movement
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");
        this.dashCooldown = json.getInt("dashCooldown");
        maxSpeed = json.getFloat("maxSpeed");
        horizontalAcceleration = json.getFloat("horizontalAcceleration");
        hitboxWidthMult = json.getFloat("hitboxWidthMult");
        hitboxHeightMult = json.getFloat("hitboxHeightMult");

        //Attacking
        this.attackPower = json.getInt("attackPower");
        this.attackCooldown = json.getInt("attackCooldown");
        this.attackOffset = json.getFloat("attackOffset");
        this.swordRadius = json.getFloat("swordRadius");
        this.attackLifespan = json.getFloat("attackLifespan");
        this.swordSpriteSheet = new TextureRegion(assets.getEntry( "platform:heart", Texture.class));


        //Other Information
        this.maxHearts = json.getInt("maxHearts");
        this.initialHearts = json.getInt("initialHearts");
        this.maxSpirit = json.getFloat("maxSpirit");
        this.initialSpirit = json.getFloat("initialSpirit");
        this.spiritPerSecond = json.getFloat("spiritPerSecond");
        this.startsFacingRight = json.getBoolean("startsFacingRight");
        this.hitCooldown = json.getInt("hitCooldown");

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
        this.dashCooldownRemaining = 0;
        this.attackCooldownRemaining = 0;
        this.hitCooldownRemaining = 0;

        this.texture = momoTexture;
    }

}
