package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;

public class Player {

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

    private final float dashCooldown;
    private final float attackCooldown;
    private final float hitCooldown;

    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion momoTexture;
    private final float momoImageWidth;
    private final float momoImageHeight;

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

    private float dashCooldownRemaining;
    private float attackCooldownRemaining;
    private float hitCooldownRemaining;

    private TextureRegion currentTexture;
    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;

    //#endregion

    //#region ACTION SET FIELDS
    private boolean jumpPressed;
    private boolean dashPressed;
    private boolean attackPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;
//#endregion

    //#region ACTION SET GETTERS AND SETTERS
    /**
     * Returns whether the jump button is currently pressed.
     *
     * @return true if the jump button is currently pressed, false otherwise
     */
    public boolean isJumpPressed() {
        return jumpPressed;
    }

    /**
     * Sets whether the jump button is currently pressed.
     *
     * @param jumpPressed true if the jump button is currently pressed, false otherwise
     */
    public void setJumpPressed(boolean jumpPressed) {
        this.jumpPressed = jumpPressed;

    }

    /**
     * Returns whether the dash button is currently pressed.
     *
     * @return true if the dash button is currently pressed, false otherwise
     */
    public boolean isDashPressed() {
        return dashPressed;
    }

    /**
     * Sets whether the dash button is currently pressed.
     *
     * @param dashPressed true if the dash button is currently pressed, false otherwise
     */
    public void setDashPressed(boolean dashPressed) {
        this.dashPressed = dashPressed;
    }

    /**
     * Returns whether the attack button is currently pressed.
     *
     * @return true if the attack button is currently pressed, false otherwise
     */
    public boolean isAttackPressed() {
        return attackPressed;
    }

    /**
     * Sets whether the attack button is currently pressed.
     *
     * @param attackPressed true if the attack button is currently pressed, false otherwise
     */
    public void setAttackPressed(boolean attackPressed) {
        this.attackPressed = attackPressed;
    }

    /**
     * Returns whether the left button is currently pressed.
     *
     * @return true if the left button is currently pressed, false otherwise
     */
    public boolean isLeftPressed() {
        return leftPressed;
    }

    /**
     * Sets whether the left button is currently pressed.
     *
     * @param leftPressed true if the left button is currently pressed, false otherwise
     */
    public void setLeftPressed(boolean leftPressed) {
        this.leftPressed = leftPressed;
    }

    /**
     * Returns whether the right button is currently pressed.
     *
     * @return true if the right button is currently pressed, false otherwise
     */
    public boolean isRightPressed() {
        return rightPressed;
    }

    /**
     * Sets whether the right button is currently pressed.
     *
     * @param rightPressed true if the right button is currently pressed, false otherwise
     */
    public void setRightPressed(boolean rightPressed) {
        this.rightPressed = rightPressed;
    }

    /**
     * Returns whether the up button is currently pressed.
     *
     * @return true if the up button is currently pressed, false otherwise
     */
    public boolean isUpPressed() {
        return upPressed;
    }

    /**
     * Sets whether the up button is currently pressed.
     *
     * @param upPressed true if the up button is currently pressed, false otherwise
     */
    public void setUpPressed(boolean upPressed) {
        this.upPressed = upPressed;
    }

    /**
     * Returns whether the down button is currently pressed.
     *
     * @return true if the down button is currently pressed, false otherwise
     */
    public boolean isDownPressed() {
        return downPressed;
    }

    /**
     * Sets whether the down button is currently pressed.
     *
     * @param downPressed true if the down button is currently pressed, false otherwise
     */
    public void setDownPressed(boolean downPressed) {
        this.downPressed = downPressed;
    }

//#endregion

    //#region GETTERS AND SETTERS
    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
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
    //#endregion

    public void draw(GameCanvas canvas) {
        canvas.draw(currentTexture, Color.WHITE, 0, 0, getX(), getY(), 0, momoImageWidth / currentTexture.getRegionWidth(), momoImageHeight / currentTexture.getRegionHeight());
    }

    public void activatePhysics(World world) {
        bodyDef.active = true;
        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape hitbox = new PolygonShape();
        hitbox.set(new float[]{
                0,              0,
                momoImageWidth, 0,
                momoImageWidth, momoImageHeight,
                0,              momoImageHeight,

        });

        fixtureDef.shape = hitbox;
        body.createFixture(fixtureDef);
        body.setGravityScale(0.0f);
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
        this.dashCooldown = json.getFloat("dashCooldown");
        maxSpeed = json.getFloat("maxSpeed");
        horizontalAcceleration = json.getFloat("horizontalAcceleration");

        //Other Information
        this.maxHearts = json.getInt("maxHearts");
        this.initialHearts = json.getInt("initialHearts");
        this.maxSpirit = json.getFloat("maxSpirit");
        this.initialSpirit = json.getFloat("initialSpirit");
        this.spiritPerSecond = json.getFloat("spiritPerSecond");
        this.attackPower = json.getInt("attackPower");
        this.startsFacingRight = json.getBoolean("startsFacingRight");
        this.attackCooldown = json.getFloat("attackCooldown");
        this.hitCooldown = json.getFloat("hitCooldown");

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
