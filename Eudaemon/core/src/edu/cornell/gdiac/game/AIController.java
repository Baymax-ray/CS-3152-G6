package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public abstract class AIController {
    protected Enemy enemy;
    protected Level level;

    public AIController(int ii, Level level) {
        this.enemy = level.getEnemies()[ii];
        this.level = level;
    }

    /**
     * @return un-normalized velocity
     */
    public abstract Vector2 getVelocity();

    public abstract void setEnemyAction(EnumSet<EnemyAction> enemyAction);
}
