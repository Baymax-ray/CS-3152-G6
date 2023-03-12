package edu.cornell.gdiac.game.obstacle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
//import edu.cornell.gdiac.game.ChiyoMomo;
import com.badlogic.gdx.utils.Timer;
import edu.cornell.gdiac.game.models.Player;

public class SwordWheelObstacle extends WheelObstacle {

    /**
     * The avatar of this object.
     */
    private final Player player;

    //<editor-fold desc="GETTERS AND SETTERS">
    /**
     * Gets the avatar of this object.
     *
     * @return The avatar of this object.
     */
    public Player getPlayer() {
        return player;
    }

    //</editor-fold>

    public SwordWheelObstacle(float x, float y, float radius, final Player player, float lifespan, float density, Vector2 scale, TextureRegion texture) {
        super(x,y,radius * 3.0f);
        this.player = player;

        setName("bullet");
        setDensity(density);
        setDrawScale(scale);
        setTexture(texture);
        setBullet(true);
        setGravityScale(0);
        setBodyType(BodyDef.BodyType.KinematicBody);
        setSensor(true);

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

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        this.setLinearVelocity(player.getBodyVelocity());
        super.update(dt);
    }
}
