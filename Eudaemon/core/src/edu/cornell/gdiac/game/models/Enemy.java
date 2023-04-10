package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.*;
import edu.cornell.gdiac.game.obstacle.*;

import java.util.ArrayList;

public class Enemy extends CapsuleObstacle {
    private float spiritRemain;
    private float velocityH;
    private float velocityV =0;
    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    //#region FINAL FIELDS
    private final float startX;
    private final float startY;

    private final int initialHearts;
    private final int attackPower;

    /**
     * The amount of ticks before the enemy can attack again
     */
    private final int attackCooldown;

    /**
     * The distance the center of the attack is offset from the enemy
     */
    private final float attackOffset;
    private final int hitCooldown;
    /**
     * The scaling factor for the sprite.
     */
    private final Vector2 scale;

    /** The amount to slow the character down */
    private final float damping;

    /** The factor to multiply to the movement */
    private final float force;

    //#endregion

    //#region TEXTURES
    // TODO: Add texture fields (FilmStrip?)
    private final TextureRegion enemyTexture;

    /** The texture for the enemy's blood */
    private final TextureRegion bloodEffectSpriteSheet;
    private final float enemyImageWidth;
    private final float enemyImageHeight;
    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    /**the type of this enemy. currently: goombaAI or fly.*/
    private final String type;
    private final JsonValue enemyData;
    /**
     * The maximum speed that the object can reach.
     */
    private final float maxSpeed;
    private final float goombaSpeedCoefficient=0.2f;
    private final boolean isHit;
    private final boolean isGrounded;
    //#endregion

    //#region NON-FINAL FIELDS
    private int guardianTime =0;
    private ArrayList<Integer> guardianList;

    private float hearts;
    private boolean isFacingRight;
    /**
     * The angle at which the entity is facing, in degrees.
     */
    private int angleFacing;

    /**
     * The offset value along the y-axis.
     */
    private float oyOffset;


    /**
     * The remaining time in seconds until the enemy can attack again.
     */
    private int attackCooldownRemaining;

    private int hitCooldownRemaining;

    /** The physics shape of this object */
    private PolygonShape sensorShape;

    /** The sprite sheet for the basic goomba right walk */
    private TextureRegion basicGoombaRightSpriteSheet;

    /** The sprite sheet for the basic goomba left walk */
    private TextureRegion basicGoombaLeftSpriteSheet;

    /** The sword killing enemy sound.  We only want to play once. */
    private Sound swordKillingSound;

    private long swordKillingSoundId = -1;
    //#endregion

    //#region Getter and Setter
    public int getGuardianTime(){return guardianTime;}
    public ArrayList<Integer> getGuardianList(){return guardianList;}
    public String getType() {
        return type;
    }
    private String getSensorName() {return this.sensorName;}
    public float getSpiritRemain() {
        return spiritRemain;
    }
    public void LossSpirit(float rate){
        this.spiritRemain -=rate;
    }
    /**
     * Retrieves the blood effect sprite sheet of the object.
     *
     * @return The current blood effect sprite sheet.
     */
    public TextureRegion getBloodEffect() {
        return bloodEffectSpriteSheet;
    }
    /**
     * Returns whether enemy is facing right or not
     *
     * @return isFacingRight.
     */
    public boolean getIsFacingRight(){
        return isFacingRight;
    }

    /**
     * Get basicGoombaRightSpriteSheet.
     *
     * @return The basicGoombaRightSpriteSheet TextureRegion.
     */
    public TextureRegion getBasicGoombaRightSpriteSheet() {
        return basicGoombaRightSpriteSheet;
    }

    /**
     * Get basicGoombaLeftSpriteSheet.
     *
     * @return The basicGoombaLeftSpriteSheet TextureRegion.
     */
    public TextureRegion getBasicGoombaLeftSpriteSheet() {
        return basicGoombaLeftSpriteSheet;
    }


    //#endregion

    /**
     * normalize the vector and apply it to velocity
     * @param v un-normalized vector
     */
    public void  setVelocity(Vector2 v){
        double m=Math.sqrt(v.x*v.x+v.y*v.y);
        velocityH= (float) (v.x/m);
        velocityV= (float) (v.y/m);
        if (velocityH < 0) {
            isFacingRight = false;
        } else if (velocityH > 0) {
            isFacingRight = true;
        }
    }
    /**shoot a bullet to the direction */
    public void attack(Vector2 v){
        
    }
    public void setMovement(EnemyAction move) {
        if (move==EnemyAction.MOVE_RIGHT){
            velocityH =1*goombaSpeedCoefficient;
            velocityV =0;}
        else if (move==EnemyAction.MOVE_LEFT){
            velocityH =-1*goombaSpeedCoefficient;
            velocityV =0;}
        else if (move == EnemyAction.STAY){
            velocityH = 0;
            velocityV =0;}

        velocityV *= this.force;
        velocityH *= this.force;
        // Change facing if appropriate
        if (velocityH < 0) {
            isFacingRight = false;
        } else if (velocityH > 0) {
            isFacingRight = true;
        }
    }
    public float getVelocityH(){return velocityH;}
    public float getVelocityV(){return velocityV;}
    public int getAttackCooldown(){return attackCooldown;}
    /**
     * Set the y-axis offset value.
     *
     * @param oyOffset The y-axis offset value as a float.
     */
    public void setOyOffset(float oyOffset) {
        this.oyOffset = oyOffset;
    }
    //#endregion


    public Enemy(JsonValue json, AssetDirectory assets) {
        super(json.getFloat("startX"), json.getFloat("startY"),
                assets.getEntry("sharedConstants", JsonValue.class).get((json.getString("type").equals("Goomba")? "Goomba":"Fly")).getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get((json.getString("type").equals("Goomba")? "Goomba":"Fly")).getFloat("hitboxHeight"));
        String TextureAsset = json.getString("TextureAsset");
        String RightMoveAsset = json.getString("RightAsset");
        String LeftMoveAsset = json.getString("LeftAsset");

        //Query the type of this enemy, then query the corresponding data in enemyConstants.json
        this.type=json.getString("type");
        this.guardianList=new ArrayList<>();
        switch (this.type) {
            case "Goomba":
                this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("Goomba");
                break;
            case "Fly":
                this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("Fly");
                break;
            case "FlyGuardian":
                this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("FlyGuardian");
                this.guardianTime = enemyData.getInt("guardianTime");
                JsonValue glist = json.get("guardianList");
                for (int i = 0; i < glist.size(); i++) {
                    this.guardianList.add(glist.getInt(i));
                }
                break;
            case "GoombaGuardian":
                this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("GoombaGuardian");
                this.guardianTime = enemyData.getInt("guardianTime");
                glist = json.get("guardianList");
                for (int i = 0; i < glist.size(); i++) {
                    this.guardianList.add(glist.getInt(i));
                }
                break;
            case "Projectile":
                this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get("Projectile");
                break;

            default:
                //should never reach here
                this.enemyData = null;
                throw new IllegalArgumentException("Enemy can only be Fly or Goomba");
        }
        this.setWidth(enemyData.getFloat("hitboxWidth"));
        this.setHeight(enemyData.getFloat("hitboxHeight"));


        //Texture
        this.enemyTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
        this.texture = this.enemyTexture;
        this.basicGoombaRightSpriteSheet = new TextureRegion(assets.getEntry(RightMoveAsset, Texture.class));
        this.basicGoombaLeftSpriteSheet = new TextureRegion(assets.getEntry(LeftMoveAsset, Texture.class));
        this.bloodEffectSpriteSheet = new TextureRegion(assets.getEntry( "bloodEffect", Texture.class));

        //Position and Movement. These two values are stored in constants.json
        this.startX = json.getFloat("startX");
        this.startY = json.getFloat("startY");

        //Size
        this.enemyImageWidth = enemyData.getFloat("ImageWidth");
        this.enemyImageHeight = enemyData.getFloat("ImageHeight");
        this.scale = new Vector2(enemyData.getFloat("drawScaleX"), enemyData.getFloat("drawScaleY"));

        this.maxSpeed = enemyData.getFloat("maxSpeed");
        this.force = enemyData.getFloat("force");
        this.damping = enemyData.getFloat("damping");

        //Attacking
        this.attackPower = enemyData.getInt("attackPower");
        this.attackCooldown = enemyData.getInt("attackCooldown");
        this.attackOffset = enemyData.getFloat("attackOffset");
        this.hitCooldown = enemyData.getInt("hitCooldown");

        //Sound Effects
        this.swordKillingSound =  Gdx.audio.newSound(Gdx.files.internal("audio/temp-sword-killing.mp3"));

        //Sensor. Wtf is this?
        //used for collision detection
        this.sensorName = "EnemyGroundSensor";

        //Other Information
        this.initialHearts = enemyData.getInt("initialHearts");
        this.hearts = initialHearts;
        this.spiritRemain =enemyData.getFloat(("spiritLimitation"));
        this.isFacingRight = enemyData.getBoolean("startsFacingRight");

        this.isHit = false;
        this.isGrounded = true;
        if(this.type.equals("Fly")||this.type.equals("FlyGuardian")){this.setGravityScale(0);}
        else{this.setGravityScale(40);}
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float x = getX();
        float y = getY();

        float ox = this.texture.getRegionWidth()/2;
        float oy = this.texture.getRegionHeight()/2 + oyOffset;

        float sx = enemyImageWidth / this.texture.getRegionWidth();
        float sy = enemyImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }


    /**
     * TODO: change this function to cater for Flying Enemy!
     * Creates the physics Body(s) for this object, adding them to the world.
     * This method overrides the base method to keep your ship from spinning.
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = enemyData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = enemyData.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        return true;
    }

    /**
     * Applies the velocity to the body of this dude
     * This method should be called after the velocity attributes are set.
     */
    public void applyVelocity() {
        if (!isActive()) {
            return;
        }
        forceCache = new Vector2(0,0);
        // Don't want to be moving. Damp out player motion
        if (getVelocityH() == 0f) {
            forceCache.set(-this.damping*getVX(),0);
            body.setLinearVelocity(forceCache);
            //body.applyForce(forceCache,getPosition(),true);
        }
        if (getVelocityV() == 0f && this.type.equals("Fly")) {
            forceCache.set(0,-this.damping*getVY());
            body.setLinearVelocity(forceCache);
            //body.applyForce(forceCache,getPosition(),true);
        }


        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= this.maxSpeed) {
            setVX(Math.signum(getVX())*this.maxSpeed);
        }else if (this.type.equals("Fly")&&Math.abs(getVY()) >= this.maxSpeed) {
            setVY(Math.signum(getVY())*this.maxSpeed);
        }

        //if(this.type.equals("Fly")){this.movementV *= this.force; this.movementH *= this.force;}
        forceCache.set(getVelocityH(), getVelocityV());
        body.setLinearVelocity(forceCache);
        //body.applyForce(forceCache,getPosition(),true);
        }



    /**
     * Called when this enemy is hit by a sword.
     * This method decrements the number of hearts for this enemy by 1. If the number of hearts
     * reaches 0, this method destroys the enemy
     */
    public void hitBySword(Player player) {
        hearts--;
        System.out.println("attacked!");
        if (hearts <= 0) {
            this.markRemoved(true);
            this.setActive(false);
            player.increaseSpiritByKill(); //player gain some spirit when the enemy killed
//            System.out.println("kill an enemy!");
        }
        swordKillingSoundId = playSound(swordKillingSound, swordKillingSoundId, 1F);
    }

    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
    }

}
