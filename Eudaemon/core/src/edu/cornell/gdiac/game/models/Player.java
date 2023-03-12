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

public class Player{

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

    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;

    //#endregion

    //#region NONFINAL FIELDS

    private Body body;

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

    private TextureRegion currentTexture;
    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;

    //#endregion

    //#region GETTERS AND SETTERS
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

    //#region Other Helper Functions
    /**
     * Add a new sword attack to the world and send it in the right direction.
     */
    private void createSword() {
//        JsonValue swordjv = constants.get("bullet");
//        float offset = swordjv.getFloat("offset",0);
        float currOffset = attackOffset * (this.isFacingRight() ? 1 : -1);
//        float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
//        SwordWheelObstacle sword;
//        float density = swordjv.getFloat("density", 0);
//        if (avatar.isLookUp()){
//            sword = new SwordWheelObstacle(avatar.getX(), avatar.getY()+ offset, radius, avatar, 0.4f, density, scale, swordSpriteSheet);
//        }
//        else if (!avatar.isGrounded() && avatar.isLookingDown()){
//            sword = new SwordWheelObstacle(avatar.getX(), avatar.getY()-offset, radius, avatar, 0.4f, density, scale, swordSpriteSheet);
//        }
//        else {
//            sword = new SwordWheelObstacle(avatar.getX()+ offset, avatar.getY(), radius, avatar, 0.4f, density, scale, swordSpriteSheet);
//        }
//
//        addQueuedObject(sword);
//
//        fireId = playSound( fireSound, fireId );
    }
    //#endregion

    public void draw(GameCanvas canvas) {
        float x = isFacingRight ? getX() : getX()+momoImageWidth;
        float sx = isFacingRight ? momoImageWidth / currentTexture.getRegionWidth() : -1 * momoImageWidth / currentTexture.getRegionWidth();
        canvas.draw(currentTexture, Color.WHITE, 0, 0, x, getY(), 0, sx, momoImageHeight / currentTexture.getRegionHeight());
    }

    public void activatePhysics(World world) {
        bodyDef.active = true;
        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape hitbox = new PolygonShape();
        hitbox.set(new float[]{
                momoImageWidth * (1-hitboxWidthMult),              0,
                momoImageWidth * hitboxWidthMult, 0,
                momoImageWidth * hitboxWidthMult, momoImageHeight * hitboxHeightMult,
                momoImageWidth * (1-hitboxWidthMult), momoImageHeight * hitboxHeightMult,
        });

        fixtureDef.shape = hitbox;
        body.createFixture(fixtureDef);
        body.setGravityScale(2.0f);
        //create fixtures for collisions

        // ^ fixtures here
    }

    public Player(JsonValue json, AssetDirectory assets) {
        String momoTextureAsset = json.getString("momoTextureAsset");
        this.momoTexture = new TextureRegion(assets.getEntry(momoTextureAsset, Texture.class));
        this.momoImageWidth = json.getFloat("momoImageWidth");
        this.momoImageHeight = json.getFloat("momoImageHeight");

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

        this.currentTexture = momoTexture;

        this.bodyDef = new BodyDef();
        bodyDef.active = false;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.x  = startX;
        bodyDef.position.y  = startY;

        this.fixtureDef = new FixtureDef();
    }

}
