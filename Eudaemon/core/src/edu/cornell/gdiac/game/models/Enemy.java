package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.game.*;
import edu.cornell.gdiac.game.obstacle.*;

public class Enemy extends CapsuleObstacle implements ContactListener {

    //#region FINAL FIELDS
    private final String type;
    private final float startX;
    private final float startY;

    private final int maxHearts;
    private final int initialHearts;
    private final int attackPower;

    private final boolean startsFacingRight;

    //private final float maxDashCooldown;
    //private final float maxAttackCooldown;

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
    private float movement;
    private final Vector2 forceCache = new Vector2();

    //TODO: Add texture fields

    //#endregion
    public String getType() {
        return type;
    }


    public Enemy(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"), json.getFloat("hitboxWidth"), json.getFloat("hitboxHeight"));
        String TextureAsset = json.getString("TextureAsset");
        this.texture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
//        this.momoImageWidth = json.getFloat("momoImageWidth");
//        this.momoImageHeight = json.getFloat("momoImageHeight");
//        this.chiyoTexture = new TextureRegion(assets.getEntry(json.getString("chiyoTextureAsset"), Texture.class));
//        this.chiyoImageWidth = json.getFloat("chiyoImageWidth");
//        this.chiyoImageHeight = json.getFloat("chiyoImageHeight");
//        this.scale = new Vector2(json.getFloat("drawScaleX"), json.getFloat("drawScaleY"));
//        this.swordSpriteSheet = new TextureRegion(assets.getEntry( "chiyo:swordAttack", Texture.class));

        //Position and Movement
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");
        this.dashCooldown = json.getInt("dashCooldown");
//        this.maxSpeed = json.getFloat("maxSpeed");
//        hitboxWidthMult = json.getFloat("hitboxWidthMult");
//        hitboxHeightMult = json.getFloat("hitboxHeightMult");
//        hit_force = json.getFloat( "hit_force");
//        dash = json.getFloat("dash", 2000);

        //Attacking
        this.attackPower = json.getInt("attackPower");
        this.attackCooldown = json.getInt("attackCooldown");
//        this.attackOffset = json.getFloat("attackOffset");
//        this.swordRadius = json.getFloat("swordRadius");
//        this.attackLifespan = json.getFloat("attackLifespan");


        //Other Information
        this.type=json.getString("type");
        this.maxHearts = json.getInt("maxHearts");
        this.initialHearts = json.getInt("initialHearts");
        this.hearts = initialHearts;

        this.startsFacingRight = json.getBoolean("startsFacingRight");

        this.isDashing = false;
        this.isJumping = false;
        this.isHit = false;
        this.isGrounded = true;
        this.isFacingRight = startsFacingRight;

    }
    public void setMovement(EnemyAction move) {
        if (move==EnemyAction.MOVE_RIGHT){movement=-1;}
        else if (move==EnemyAction.MOVE_LEFT){movement=1;}
        // Change facing if appropriate
        if (movement < 0) {
            isFacingRight = false;
        } else if (movement > 0) {
            isFacingRight = true;
        }
    }
    public float getMovement(){return movement;}
    public void applyForce() {
        if (!isActive()) {
            return;
        }
        // Don't want to be moving. Damp out player motion
//        if (getMovement() == 0f) {
//            forceCache.set(-getDamping() * getVX(), 0);
//            body.applyForce(forceCache, getPosition(), true);
//        }
//
//        // Velocity too high, clamp it
//        if (Math.abs(getVX()) >= getMaxSpeed()) {
//            setVX(Math.signum(getVX()) * getMaxSpeed());
//        } else {
//            forceCache.set(getMovement(), 0);
//            body.applyForce(forceCache, getPosition(), true);
//        }
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
