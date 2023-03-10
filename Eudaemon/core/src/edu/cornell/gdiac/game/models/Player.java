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

    private boolean isDashing;
    private boolean isJumping;
    private boolean isChiyo;
    private boolean isHit;
    private boolean isGrounded;
    private boolean isFacingRight;
    private boolean isLookingUp;
    private boolean isLookingDown;

    private float dashCooldownRemaining;
    private float attackCooldownRemaining;
    private float hitCooldownRemaining;

    private TextureRegion currentTexture;
    /** The player's form: 0 is Momo, 1 is Chiyo */
    private int form;

    //#endregion


    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
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

        //create fixtures for collisions

        // ^ fixtures here
    }

    public Player(JsonValue json, AssetDirectory assets) {
        String momoTextureAsset = json.getString("momoTextureAsset");
        this.momoTexture = new TextureRegion(assets.getEntry(momoTextureAsset, Texture.class));
        this.momoImageWidth = json.getFloat("momoImageWidth");
        this.momoImageHeight = json.getFloat("momoImageHeight");

        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");

        this.maxHearts = json.getInt("maxHearts");
        this.initialHearts = json.getInt("initialHearts");
        this.maxSpirit = json.getFloat("maxSpirit");
        this.initialSpirit = json.getFloat("initialSpirit");
        this.spiritPerSecond = json.getFloat("spiritPerSecond");
        this.attackPower = json.getInt("attackPower");
        this.startsFacingRight = json.getBoolean("startsFacingRight");
        this.dashCooldown = json.getFloat("dashCooldown");
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
