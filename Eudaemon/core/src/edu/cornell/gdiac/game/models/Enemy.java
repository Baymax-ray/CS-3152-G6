package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.*;
import edu.cornell.gdiac.game.obstacle.*;

import java.util.ArrayList;

public class Enemy extends CapsuleObstacle {
    private int detectDistance;
    private float spiritRemain;
    private float velocityX;
    private float velocityY = 0;
    /**
     * Cache for internal force calculations
     */
    private Vector2 forceCache = new Vector2();

    //#region FINAL FIELDS
    private final float startX;
    private final float startY;

    private final int attackPower;

    /**
     * The amount of ticks before the enemy can attack again
     */
    private final int attackCooldown;

    /**
     * The distance the center of the attack is offset from the enemy
     */
    private final float attackOffset;
    /**
     * The x-scaling factor for the sprite.
     */
    private float scaleX;
    /**
     * The y-scaling factor for the sprite.
     */
    private float scaleY;

    /**
     * The factor to multiply to the movement
     */
    private final float force;

    //#endregion

    //#region TEXTURES
    // TODO: Add texture fields (FilmStrip?)
    private TextureRegion bulletTexture;

    private final TextureRegion enemyTexture;

    /**
     * The texture for the enemy's blood
     */
    private final TextureRegion bloodEffectSpriteSheet;
    private final Animation<TextureRegion> bloodEffectAnimation;
    //#endregion
    private final float enemyImageWidth;
    private final float enemyImageHeight;
    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private final String sensorName;

    /**
     * the type of this enemy. currently: goombaAI or fly.
     */
    private final String type;
    private final JsonValue enemyData;
    /**
     * The maximum speed that the object can reach.
     */
    private float maxSpeed;
    /**
    The Scalar the speed is multiplied by
     */
    private float speedMult = 0.2f;

    //#region NON-FINAL FIELDS
    private int guardianTime = 0;
    private ArrayList<Integer> guardianList;
    private float hearts;
    private boolean isFacingRight = false;

    /**
     * The offset value along the y-axis.
     */
    private float oyOffset;
    /**
     * The offset value along the y-axis.
     */
    private float oxOffset;
    /**
     * The physics shape of this object
     */
    private PolygonShape sensorShape;

    /**
     * The sprite sheet for the basic goomba right walk
     */
    private TextureRegion moveSpriteSheet;

    /**
     * The sprite sheet for the basic goomba left walk
     */
    private TextureRegion leftMoveSpriteSheet;
    /**
     * A HashMap storing 2D TextureRegion arrays with a String identifier.
     * Each key represents an animation name, and the associated value is a 2D TextureRegion array
     * with rows representing different animation states and columns representing individual frames.
     */
    private ObjectMap<String, Animation> animations;
    /**
     * The current animation state of the enemy.
     */
    private String currentAnimation;
    /**
     * The ticks between frame switches
     */
    private int tickFrameSwitch = 5;
    /**
     * The maxFrame for the enemy
     */
    private int maxFrame;
    /**
     * The current frame of the video player.
     */
    private int currentFrame;


    /**
     * The sword killing enemy sound.  We only want to play once.
     */
    private Sound swordKillingSound;
    private long swordKillingSoundId = -1;
    private Sound swordHittingSound;
    private long swordHittingSoundId = -1;
    private JsonValue bullet;
    private float projectileEnemyRotation;
    private String projectileEnemyDirection;
    private String projectileSpriteSheetType;

    //#endregion

    //#region Getter and Setter
    /**
     * Gets the current frame of the video player.
     *
     * @return the current frame
     */
    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Sets the current frame of the video player.
     *
     * @param currentFrame the new current frame
     */
    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }
    /**
     * Gets the current number of ticks between frame switches
     *
     * @return the current frame
     */
    public int getTickFrameSwitch() {
        return tickFrameSwitch;
    }

    /**
     * Sets the current number of ticks between frame switches
     */
    public void setTickFrameSwitch(int frame) {
        this.tickFrameSwitch = frame;
    }

    /**
     * Gets the maximum frame count of the video player.
     *
     * @return the maximum frame count
     */
    public int getMaxFrame() {
        return maxFrame;
    }
    /**
     * Sets the maximum frame count of the video player.
     *
     * @param maxFrame the new maximum frame count
     */
    public void setMaxFrame(int maxFrame) {
        this.maxFrame = maxFrame;
    }
    /**
     * Gets the current animation of the character.
     *
     * @return the current animation
     */
    public String getCurrentAnimation() {
        return currentAnimation;
    }

    /**
     * Sets the current animation of the character.
     *
     * @param currentAnimation the new current animation
     */
    public void setCurrentAnimation(String currentAnimation) {
        this.currentAnimation = currentAnimation;
    }

    public int getDetectDistance() {
        return detectDistance;
    }

    public int getGuardianTime() {
        return guardianTime;
    }

    public ArrayList<Integer> getGuardianList() {
        return guardianList;
    }

    public String getType() {
        return type;
    }

    private String getSensorName() {
        return this.sensorName;
    }

    public float getSpiritRemain() {
        return spiritRemain;
    }

    public void LossSpirit(float rate) {
        this.spiritRemain -= rate;
    }

    /**
     * Retrieves the blood effect sprite sheet of the object.
     *
     * @return The current blood effect sprite sheet.
     */
    public TextureRegion getBloodEffect() {
        return bloodEffectSpriteSheet;
    }

    public Animation getBloodEffectAnimation() {
        return bloodEffectAnimation;
    }

    /**
     * Returns whether enemy is facing right or not
     *
     * @return isFacingRight.
     */
    public boolean getIsFacingRight() {
        return isFacingRight;
    }
    public void setFacingRight(boolean val){
        isFacingRight = val;
    }

    /**
     * Get basicGoombaRightSpriteSheet.
     *
     * @return The basicGoombaRightSpriteSheet TextureRegion.
     */
    public TextureRegion getMoveSpriteSheet() {
        return moveSpriteSheet;
    }

    /**
     * Get basicGoombaLeftSpriteSheet.
     *
     * @return The basicGoombaLeftSpriteSheet TextureRegion.
     */
    public TextureRegion getLeftMoveSpriteSheet() {
        return leftMoveSpriteSheet;
    }

    //#endregion

    /**
     * normalize the vector and apply it to velocity
     *
     * @param v un-normalized vector
     */
    public void setVelocity(Vector2 v) {
        double m = Math.sqrt(v.x * v.x + v.y * v.y);
        velocityX = speedMult * (float) (v.x / m);
        velocityY = speedMult * (float) (v.y / m);
        if (velocityX < 0) {
            isFacingRight = false;
        } else if (velocityX > 0) {
            isFacingRight = true;
        }
    }

    public void setMovement(EnemyAction move) {
        if (move == EnemyAction.MOVE_RIGHT) {
            velocityX = 1 * speedMult;
            velocityY = 0;
        } else if (move == EnemyAction.MOVE_LEFT) {
            velocityX = -1 * speedMult;
            velocityY = 0;
        } else if (move == EnemyAction.STAY) {
            velocityX = 0;
            velocityY = 0;
        }

        velocityY *= this.force;
        velocityX *= this.force;
        // Change facing if appropriate
        if (velocityX < 0) {
            isFacingRight = false;
        } else if (velocityX > 0) {
            isFacingRight = true;
        }
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public JsonValue getBullet() {
        return this.bullet;
    }

    public TextureRegion getBulletTexture() {
        return bulletTexture;
    }

    /**
     * Set the y-axis offset value.
     *
     * @param oyOffset The y-axis offset value as a float.
     */
    public void setOyOffset(float oyOffset) {
        this.oyOffset = oyOffset;
    }
    /**
     * Set the x-axis offset value.
     */
    public void setOxOffset(float oxOffset) {
        this.oxOffset = oxOffset;
    }

    public Enemy(JsonValue json, AssetDirectory assets, float x, float y) {
//        super(x,y,1f,1.4f);
        super(x, y,
                assets.getEntry("sharedConstants", JsonValue.class).get(json.getString("name")).getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get(json.getString("name")).getFloat("hitboxHeight"));
        this.type = json.getString("name");
        this.enemyData = assets.getEntry("sharedConstants", JsonValue.class).get(type);
        String TextureAsset = enemyData.getString("TextureAsset");

        //Position and Movement
        this.startX = x;
        this.startY = y;
        this.projectileEnemyDirection = "Left";
        this.projectileEnemyRotation = 0;
        this.speedMult = enemyData.getFloat("speedMult");

        //Size
        this.enemyImageWidth = enemyData.getFloat("ImageWidth");
        this.enemyImageHeight = enemyData.getFloat("ImageHeight");
        scaleX = enemyData.getFloat("drawScaleX");
        scaleY = enemyData.getFloat("drawScaleY");
        oxOffset = enemyData.getFloat("oxOffset");
        oyOffset = enemyData.getFloat("oyOffset");

        //Query the type of this enemy, then query the corresponding data in enemyConstants.json
        this.guardianList = new ArrayList<>();
        switch (this.type) {
            case "Goomba":
                break;
            case "Fast":
                break;
            case "Fly":
                break;
            case "FlyGuardian":
                this.guardianTime = enemyData.getInt("guardianTime");
//                JsonValue glist = json.get("guardianList");
                JsonValue fgjv = json.get("properties");
                String value = null;
                for (JsonValue property : fgjv) {
                    if ("StopRelateToSelf".equals(property.getString("name"))) {
                        value = property.getString("value");
                        break;
                    }
                }
                //loop over value and add numbers to guardianList
                String[] values = value.split(",");
                for (String s : values) {
                    this.guardianList.add(Integer.parseInt(s));
                }
                break;
            case "GoombaGuardian":
                this.guardianTime = enemyData.getInt("guardianTime");
//                glist = json.get("guardianList");
                ArrayList<Integer> list2 = new ArrayList<>();
                JsonValue properties = json.get("properties");
                for (JsonValue property : properties) {
                    list2.add(property.getInt("value"));
                }
                for (int i = 0; i < list2.size(); i++) {
                    this.guardianList.add(list2.get(i));
                }
                break;
            case "Projectile":
                this.bullet = enemyData.get("bullet");
                String bulletT = enemyData.getString("BulletTextureAsset");
                this.bulletTexture = new TextureRegion(assets.getEntry(bulletT, Texture.class));
                this.detectDistance = enemyData.getInt("detectDistance");
                // this enemy should be static and not affected by recoil
                //super.setBodyTypeToStatic();
                // set the direction that this projectile enemy is facing
                JsonValue projectileProperties = json.get("properties");
                for (JsonValue property : projectileProperties) {
                    if (property.getString("name").equals("Direction")) {
                        projectileEnemyDirection = property.getString("value");
                    }
                }
                for (JsonValue property : projectileProperties) {
                    if (property.getString("name").equals("SpriteSheetType")) {
                        projectileSpriteSheetType = property.getString("value");
                    }
                }
                switch (projectileEnemyDirection) {
                    case "Left":
                        this.projectileEnemyRotation = -(float) Math.PI/2;
                        this.setAngle((float) Math.PI/2);
                        break;
                    case "Right":
                        this.projectileEnemyRotation = (float) Math.PI/2;
                        this.setAngle((float) Math.PI/2);
                        break;
                    case "Up":
                        this.projectileEnemyRotation = (float) Math.PI;
                        //this.setOyOffset(-110f);
                        break;
                    case "Down":
                        this.projectileEnemyRotation = 0;
                        break;
                    default:
                        System.out.println("something wrong");
                }
                switch (projectileSpriteSheetType) {
                    case "Left":
                        TextureAsset = "enemy:projectileEnemyLeft";
                        break;
                    case "Right":
                        TextureAsset = "enemy:projectileEnemyRight";
                        break;
                    case "Up":
                        TextureAsset = "enemy:projectileEnemyUp";
                        break;
                    case "Down":
                        TextureAsset = "enemy:projectileEnemyUp";
                        break;
                    default:
                        System.out.println("something wrong");
                }
                break;
            default:
                //should never reach here
                throw new IllegalArgumentException("Enemy type does not exist");
        }
        this.setWidth(enemyData.getFloat("hitboxWidth"));
        this.setHeight(enemyData.getFloat("hitboxHeight"));
        this.setMaxFrame(enemyData.getInt("maxFrame"));
        this.setTickFrameSwitch(enemyData.getInt("tickFrameSwitch"));
        Filter f =this.getFilterData();
        f.groupIndex = -1; //cancel its collision with enemy
        this.setFilterData(f);


        //Texture
        this.enemyTexture = new TextureRegion(assets.getEntry(TextureAsset, Texture.class));
        this.texture = this.enemyTexture;
        this.moveSpriteSheet = new TextureRegion(assets.getEntry(enemyData.getString("MoveAsset"), Texture.class));
        animations = new ObjectMap<>();
        addAnimations(moveSpriteSheet, maxFrame, 1, "move");
        if (this.type.equals("Projectile")) addAnimations(texture, maxFrame, 1, "projectileIdle");
        currentAnimation = "idle";

        this.bloodEffectSpriteSheet = new TextureRegion(assets.getEntry("bloodEffect", Texture.class));
        TextureRegion[][] frames = bloodEffectSpriteSheet.split(bloodEffectSpriteSheet.getRegionWidth() / 17, bloodEffectSpriteSheet.getRegionHeight());
        bloodEffectAnimation = new Animation<>(0.5f, frames[0]);



        this.maxSpeed = enemyData.getFloat("maxSpeed");
        this.force = enemyData.getFloat("force");

        //Attacking
        this.attackPower = enemyData.getInt("attackPower");
        this.attackCooldown = enemyData.getInt("attackCooldown");
        this.attackOffset = enemyData.getFloat("attackOffset");

        //Sound Effects
        this.swordKillingSound = Gdx.audio.newSound(Gdx.files.internal("audio/temp-sword-killing.mp3"));
        this.swordHittingSound = Gdx.audio.newSound(Gdx.files.internal("audio/temp-non-killing-sound.mp3"));

        //Sensor. Wtf is this?
        //used for collision detection
        this.sensorName = "EnemyGroundSensor";

        //Other Information
        int initialHearts = enemyData.getInt("initialHearts");
        this.hearts = initialHearts;
        this.spiritRemain = enemyData.getFloat(("spiritLimitation"));
        this.isFacingRight = enemyData.getBoolean("startsFacingRight");

        if (this.type.equals("Fly") || this.type.equals("FlyGuardian")) {
            this.setGravityScale(0);
        } else {
            this.setGravityScale(1);
        }
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (currentAnimation == "idle"){
            if (this.type.equals("Projectile")) this.texture = (TextureRegion) animations.get("projectileIdle").getKeyFrame(currentFrame);
            else this.texture = enemyTexture;
        }
        else{
            this.texture = (TextureRegion) animations.get(currentAnimation).getKeyFrame(currentFrame);
        }

        float x = getX();
        float y = getY();

        float ox = this.texture.getRegionWidth() / 2 + oxOffset;
        float oy = this.texture.getRegionHeight() / 2 + oyOffset;

        float sx = scaleX * (isFacingRight ? 1 : -1) * enemyImageWidth / this.texture.getRegionWidth();
        float sy = scaleY * enemyImageHeight / this.texture.getRegionHeight();

        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, projectileEnemyRotation, sx, sy);
    }


    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * This method overrides the base method to keep your ship from spinning.
     *
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
        sensorDef.density = enemyData.getFloat("density", 0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = enemyData.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink", 0) * getWidth() / 2.0f,
                sensorjv.getFloat("height", 0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        if (this.type.equals("Projectile")) {
            body.setType(BodyDef.BodyType.KinematicBody);
        }

        return true;
    }

    /**
     * Back to the start x and y
     */
    public void backtoStart() {
        this.setX(startX);
        this.setY(startY);
    }

    private void addAnimations(TextureRegion spriteSheet, int columns, int rows, String name) {
        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth() / columns, spriteSheet.getRegionHeight() / rows);
        Animation animation = new Animation<TextureRegion>(1f, frames[0]); // Creates an animation with a frame duration of 0.1 seconds
        animation.setPlayMode(Animation.PlayMode.NORMAL); // Sets the animation to play normally
        animations.put(name, animation);
    }

    /**
     * Applies the velocity to the body of this dude
     * This method should be called after the velocity attributes are set.
     */
    public void applyVelocity() {
        if (!isActive()) {
            return;
        }
        forceCache = new Vector2(0, 0);

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= this.maxSpeed) {
            setVX(Math.signum(getVX()) * this.maxSpeed);
        } else if (this.type.equals("Fly") && Math.abs(getVY()) >= this.maxSpeed) {
            setVY(Math.signum(getVY()) * this.maxSpeed);
        }

        //if(this.type.equals("Fly")){this.movementV *= this.force; this.movementH *= this.force;}
        forceCache.set(getVelocityX(), getVelocityY());
        body.setLinearVelocity(forceCache);
    }


    /**
     * Called when this enemy is hit by a sword.
     * This method decrements the number of hearts for this enemy by 1. If the number of hearts
     * reaches 0, this method destroys the enemy
     */
    public void hitBySword(Player player) {
        hearts--;
        if (hearts > 0) {
            float direction = player.getX() - this.getX() > 0 ? -1 : 1;
            Vector2 knockback = new Vector2(direction * enemyData.getFloat("knockbackX"),
                    enemyData.getFloat("knockbackY"));
            forceCache.set(knockback);
            body.applyForce(forceCache, this.getPosition(), true);
            //body.setLinearVelocity(forceCache);
        } else {
            this.markRemoved(true);
            player.increaseSpiritByKill(); //player gain some spirit when the enemy killed
        }
        if(hearts == 0){
            swordKillingSoundId = playSound(swordKillingSound, swordKillingSoundId, 0.5F);
        }
        else{
            swordHittingSoundId = playSound(swordHittingSound, swordHittingSoundId, 0.05F);
        }

    }

    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop(soundId);
        }
        return sound.play(volume);
    }

    public void dispose() {
        this.swordHittingSound.dispose();
        this.swordKillingSound.dispose();
    }

}