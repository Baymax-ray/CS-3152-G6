package edu.cornell.gdiac.game.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.models.Player;

public class EffectObstacle extends BoxObstacle{
    /**
     * The avatar of this object.
     */
    private CapsuleObstacle avatar;
    /**
     * Accumulates the ticks elapsed since the animation started.
     */
    int currentTicks;
    /**
     * Current frame of sword animation.
     */
    private int tickSpeed;
    /**
     * Number of frames between changes in animation frames
     */
    private float currentFrame;
    /**
     * The animation object that represents a sequence of TextureRegions.
     */
    Animation<TextureRegion> animation;
    /**
     * X scale of the sprite
     */
    private float sX;
    /**
     * Y scale of the sprite
     */
    private float sY;
    /**
     * Whether or not the effect should track the player
     */
    private boolean trackPlayer;
    /**
     * X offset of the effect from the player
     */
    private float pOffsetX;
    /**
     * Y offset of the effect from the player
     */
    private float pOffsetY;
    /**
     * the lifespan of the effect
     */
    private float lifespan;

    //<editor-fold desc="GETTERS AND SETTERS">
    /**
     * Gets the avatar of this object.
     *
     * @return The avatar of this object.
     */
    public CapsuleObstacle getAvatar() {
        return avatar;
    }

    public void setsX(float sX) {
        this.sX = sX;
    }

    public void setsY(float sY) {
        this.sY = sY;
    }

    public void setpOffsetX(float pOffsetX) {
        this.pOffsetX = pOffsetX;
    }

    public void setpOffsetY(float pOffsetY) {
        this.pOffsetY = pOffsetY;
    }

    public void setLifespan(float lifespan) {
        this.lifespan = lifespan;
    }

    public void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
        currentFrame = 0;
        currentTicks = 0;
        setTexture(animation.getKeyFrame(currentFrame));
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = tickSpeed;
    }

    /** Set the obstacle for this effect to track */
    public void setAvatar(CapsuleObstacle avatar, boolean trackPlayer) {
        this.avatar = avatar;
        this.trackPlayer = trackPlayer;
    }

    //</editor-fold>


    public EffectObstacle() {
        super(0, 0, 0.01f,0.01f);
    }

//    public EffectObstacle(float x, float y, float width, float height, float sx, float sy, float angle, float pOffsetX, float pOffsetY, int framesX, int framesY, Boolean trackPlayer, String name, CapsuleObstacle avatar, float lifespan, Vector2 scale, TextureRegion spriteSheet, int tickSpeed) {
//        super(x,y,width,height);
//        this.avatar = avatar;
//        this.tickSpeed = tickSpeed;
//        this.trackPlayer = trackPlayer;
//        this.sX = sx;
//        this.sY = sy;
//        this.pOffsetX = pOffsetX;
//        this.pOffsetY = pOffsetY;
//        this.lifespan = lifespan;
//
//        setName(name);
//        setDensity(0);
//        this.setDrawScale(scale);
//        setBullet(true);
//        setGravityScale(0);
//        setBodyType(BodyDef.BodyType.KinematicBody);
//        setSensor(true);
//        setAngle(angle);
//
//        //ANIMATION
//        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth()/framesX, spriteSheet.getRegionHeight()/framesY);
//        animation = new Animation<TextureRegion>(0.5f, frames[0]); // Creates an animation with a frame duration of 0.1 seconds
//        animation.setPlayMode(Animation.PlayMode.NORMAL); // Sets the animation to play normally
//        currentTicks = 0; // Accumulates the time elapsed since the animation started
//        currentFrame = 0;
//        TextureRegion current = animation.getKeyFrame(currentFrame); // Gets the current frame of the animation
//        setTexture(current);
//    }

    @Override
    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) return false;

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Destroy this object
                destroy();
            }
        }, lifespan);

        return true;
    }

    private void destroy() {
        // Code to destroy this object
        this.markRemoved(true);
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        if (trackPlayer) {
            this.setLinearVelocity(avatar.getLinearVelocity());
        }
        super.update(dt);

        //Animation
        currentTicks++;
        if (currentTicks % tickSpeed == 0){
            currentFrame++;
//            System.out.println("current frame: " + currentFrame);
            TextureRegion current = animation.getKeyFrame(currentFrame); // Gets the current frame of the animation
            setTexture(current);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
//        float sx = 2*getRadius()/this.texture.getRegionWidth(); // size in world coordinates / texture coordinates
//        float sy = 2*getRadius()/this.texture.getRegionHeight();
        canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() + pOffsetX, getY() + pOffsetY, getAngle(), sX, sY);
    }

    @Override
    public void deactivatePhysics(World world) {
        releaseFixtures();
        super.deactivatePhysics(world);
    }
}
