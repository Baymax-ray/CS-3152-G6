package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;

public class Enemy extends CapsuleObstacle implements ContactListener {

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
        super();
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void beginContact(Contact contact) {

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
