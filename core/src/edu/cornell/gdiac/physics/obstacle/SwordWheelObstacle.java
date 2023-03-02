package edu.cornell.gdiac.physics.obstacle;

import edu.cornell.gdiac.physics.DudeModel;
import com.badlogic.gdx.utils.Timer;

public class SwordWheelObstacle extends WheelObstacle {

    /**
     * The avatar of this object.
     */
    private DudeModel avatar;

    //<editor-fold desc="GETTERS AND SETTERS">
    /**
     * Gets the avatar of this object.
     *
     * @return The avatar of this object.
     */
    public DudeModel getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar of this object.
     *
     * @param avatar The new avatar to set.
     */
    public void setAvatar(DudeModel avatar) {
        this.avatar = avatar;
    }
    //</editor-fold>

    public SwordWheelObstacle(float x, float y, float radius, DudeModel avatar, int lifespan) {
        super(x,y,radius);
        this.avatar = avatar;

        // Schedule a task to destroy this object after lifespan seconds
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Destroy this object
                destroy();
            }
        }, lifespan);
    }

    private void destroy() {
        // Code to destroy this object
        this.markRemoved(true);
    }
}
