package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public abstract class AIController {
    private Enemy enemy;
    private Level level;

    public AIController(int ii, Level level) {
        this.enemy = level.getEnemies()[ii];
        this.level = level;
    }

    public abstract void setEnemyAction(EnumSet<EnemyAction> enemyAction);
}
