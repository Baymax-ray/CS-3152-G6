package edu.cornell.gdiac.game.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Timer;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.Player;

public class SwordWheelObstacle extends WheelObstacle {

    /**
     * The avatar of this object.
     */
    private final Player player;

    /**
     * This is the set of enemies that have been hit by this sword attack
     */
    private final ObjectSet<Enemy> hitEnemies;

    /**
     * Whether or not this object has hit a wall.
     */
    private boolean hasHitWall;

    /**
     * The angle the player is facing when attacking with the sword
     */
    private float angle;

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
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns whether or not this object has hit a wall.
     *
     * @return Whether or not this object has hit a wall.
     */
    public boolean hasHitWall() {return hasHitWall;}

    /**
     * Sets whether or not this object has hit a wall.
     *
     * @param hasHitWall Whether or not this object has hit a wall.
     */
    public void setHasHitWall(boolean hasHitWall) {this.hasHitWall = hasHitWall;}

    //</editor-fold>

    public SwordWheelObstacle(float x, float y, float radius, float angle, final Player player, float lifespan, float density, Vector2 scale, TextureRegion spriteSheet) {
        super(x,y,radius);
        this.player = player;
        this.angle = angle;
        this.hitEnemies = new ObjectSet<>();

        setName("sword");
        setDensity(density);
//        setDrawScale(scale);
        setBullet(true);
        setGravityScale(0);
        setBodyType(BodyDef.BodyType.KinematicBody);
        setSensor(true);

        //ANIMATION
        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth()/8, spriteSheet.getRegionHeight()/2);
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
        super.update(dt);
        if (player.getBody() != null){
            this.setLinearVelocity(player.getBodyVelocity());
        }

        //Animation
        currentTicks++;
        if (currentTicks % 4 == 0){
            currentFrame++;
            TextureRegion current = animation.getKeyFrame(currentFrame); // Gets the current frame of the animation
            setTexture(current);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        float sx = 2*getRadius()/this.texture.getRegionWidth(); // size in world coordinates / texture coordinates
        float sy = 2*getRadius()/this.texture.getRegionHeight();
        if (angle == 0){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), 0, sx, sy);
        }
        else if (angle == 45){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), 1, sx, sy);
        }
        else if (angle == 90){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), getAngle(), sx, sy);
        }
        else if (angle == 135){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), 2.35f, sx, sy);
        }
        else if (angle == 180){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), getAngle(), -sx, sy);
        }
        else if (angle == 225){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), 3.9f, sx, sy);
        }
        else if (angle == 270){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), getAngle(), sx, -sy);
        }
        else if (angle == 315){
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX(), getY(), 5.5f, sx, sy);
        }

    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
    }


    public void addHitEnemy(Enemy enemy) {
        this.hitEnemies.add(enemy);
    }

    public boolean hasHitEnemy(Enemy enemy) {
        return this.hitEnemies.contains(enemy);
    }
}
