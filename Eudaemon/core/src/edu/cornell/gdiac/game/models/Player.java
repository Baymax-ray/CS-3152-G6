package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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

    //#endregion


    //#region NONFINAL FIELDS

    private float x, y; // temporary

    private int hearts;
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

    //#endregion


    public float getX() {
        return startX; // need to implement with body
    }

    public float getY() {
        return startY; // need to implement with body
    }

    public void draw(GameCanvas canvas) {
        canvas.draw(currentTexture, Color.WHITE, 0, 0, x, y, 0, momoImageWidth / currentTexture.getRegionWidth(), momoImageHeight / currentTexture.getRegionHeight());
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

        this.x = startX;
        this.y = startY;
    }

}
