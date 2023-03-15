package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public class FlyAI extends AIController{
    private Enemy enemy;
    private Level level;
    private int ticks=0;
    private FSMState state;
    private enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /**The first frame the enemy starts to wander*/
        TOWANDER,
        /** The enemy is patrolling around without a target */
        WANDER,
        /** The enemy has a target, but must get closer */
        CHASE,
        /** The enemy has a target and is attacking it */
        ATTACK
    }
    public FlyAI(int ii, Level level) {
        super(ii,level);
    }
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){

    }
}