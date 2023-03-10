package edu.cornell.gdiac.physics.obstacle;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.physics.DudeModel;
import com.badlogic.gdx.utils.Timer;
import edu.cornell.gdiac.util.FilmStrip;

public class SwordWheelObstacle extends WheelObstacle {

    /**
     * The avatar of this object.
     */
    private final DudeModel avatar;

    /**
     * Accumulates the ticks elapsed since the animation started.
     */
    int currentTicks;
    /**
     * Current frame of sword animation.
     */
    float currentFrame;
    /**
     * The animation object that represents a sequence of TextureRegions.
     */
    Animation<TextureRegion> animation;

    //<editor-fold desc="GETTERS AND SETTERS">
    /**
     * Gets the avatar of this object.
     *
     * @return The avatar of this object.
     */
    public DudeModel getAvatar() {
        return avatar;
    }

    //</editor-fold>

    public SwordWheelObstacle(float x, float y, float radius, final DudeModel avatar, float lifespan, float density, Vector2 scale, TextureRegion spriteSheet) {
        super(x,y,radius * 3.0f);
        this.avatar = avatar;
        avatar.setAttacking(true);
        setName("bullet");
        setDensity(density);
        setDrawScale(scale);
        setBullet(true);
        setGravityScale(0);
        setBodyType(BodyDef.BodyType.KinematicBody);
        setSensor(true);

        //ANIMATION
        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth()/6, spriteSheet.getRegionHeight());
        animation = new Animation<TextureRegion>(0.5f, frames[0]); // Creates an animation with a frame duration of 0.1 seconds
        animation.setPlayMode(Animation.PlayMode.NORMAL); // Sets the animation to play normally
        currentTicks = 0; // Accumulates the time elapsed since the animation started
        currentFrame = 0;
        TextureRegion current = animation.getKeyFrame(currentFrame); // Gets the current frame of the animation
        setTexture(current);

        // Schedule a task to destroy this object after lifespan seconds
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                avatar.setAttacking(false);
                // Destroy this object
                destroy();
            }
        }, lifespan);
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
        this.setLinearVelocity(avatar.getLinearVelocity());
        super.update(dt);

        //Animation
        currentTicks++;
        if (currentTicks % 4 == 0){
            currentFrame++;
            TextureRegion current = animation.getKeyFrame(currentFrame); // Gets the current frame of the animation
            setTexture(current);
        }
    }
}
