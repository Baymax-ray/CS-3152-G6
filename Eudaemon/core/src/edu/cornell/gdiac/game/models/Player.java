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
    /**
     * The amount of ticks before the palyer can attack again
     */
    private final int attackCooldown;
    /**
     * The amount of ticks before the palyer can transform again
     */
    private final int transformCooldown;
    /**
     * The distance the center of the attack is offset from the player
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
     * The radius of the sword used by the player.
     */
    private final float swordRadius;
    /**
     * The lifespan of the sword attack animation in seconds.
     */
    private final float attackLifespan;
    /**
     * The scaling factor for the sprite.
     */
    private final Vector2 scale;

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();

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

    public boolean isGrounded() {return isGrounded;}

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
    //#endregion

    public void draw(GameCanvas canvas) {
        float x = getX();
        float y = getY();

        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight()/2;

        float sx = (isFacingRight ? 1 : -1) * momoImageWidth / this.texture.getRegionWidth();
        float sy = momoImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
        System.out.println(isGrounded());
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
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
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
     * This method is now very immature. It just makes Momo to flash forward.
     * */
    public void dash(){
        int direction = isFacingRight? 1: -1;
        forceCache.set(hit_force * dash * direction, 0);
        body.applyForce(forceCache, getPosition(), true);
    }


    public Player(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"), json.getFloat("hitboxWidth"), json.getFloat("hitboxHeight"));
        String momoTextureAsset = json.getString("momoTextureAsset");
        this.momoTexture = new TextureRegion(assets.getEntry(momoTextureAsset, Texture.class));
        this.momoImageWidth = json.getFloat("momoImageWidth");
        this.momoImageHeight = json.getFloat("momoImageHeight");
        this.chiyoTexture = new TextureRegion(assets.getEntry(json.getString("chiyoTextureAsset"), Texture.class));
        this.chiyoImageWidth = json.getFloat("chiyoImageWidth");
        this.chiyoImageHeight = json.getFloat("chiyoImageHeight");
        this.scale = new Vector2(json.getFloat("drawScaleX"), json.getFloat("drawScaleY"));
        this.swordSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:swordAttack", Texture.class));

        //Position and Movement
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");
        this.dashCooldown = json.getInt("dashCooldown");
        maxSpeed = json.getFloat("maxSpeed");
        horizontalAcceleration = json.getFloat("horizontalAcceleration");
        hitboxWidthMult = json.getFloat("hitboxWidthMult");
        hitboxHeightMult = json.getFloat("hitboxHeightMult");
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
        this.data = json;
        sensorName = "PlayerGroundSensor";
    }

}
