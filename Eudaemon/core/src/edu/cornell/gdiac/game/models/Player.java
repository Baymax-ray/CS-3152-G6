package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

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

    private final float maxDashCooldown;
    private final float maxAttackCooldown;
    private final float maxHitCooldown;

    // TODO: Add texture fields (FilmStrip?)

    //#endregion


    //#region NONFINAL FIELDS

    private Vector2 pos;
    private Vector2 vel;

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

    private float dashCooldown;
    private float attackCooldown;
    private float hitCooldown;

    //TODO: Add texture fields

    //#endregion


    public Player(JsonValue json) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
