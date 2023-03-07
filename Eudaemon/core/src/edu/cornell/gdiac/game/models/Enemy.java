package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class Enemy {

    //#region FINAL FIELDS

    private final float startX;
    private final float startY;

    private final int maxHearts;
    private final int initialHearts;
    private final int attackPower;

    private final boolean startsFacingRight;

    private final float maxDashCooldown;
    private final float maxAttackCooldown;

    // TODO: Add texture fields (FilmStrip?)

    //#endregion


    //#region NONFINAL FIELDS

    private Vector2 pos;
    private Vector2 vel;

    private int hearts;

    private boolean isDashing;
    private boolean isJumping;
    private boolean isHit;
    private boolean isGrounded;
    private boolean isFacingRight;

    private float dashCooldown;
    private float attackCooldown;

    //TODO: Add texture fields

    //#endregion


    public Enemy(JsonValue json) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
