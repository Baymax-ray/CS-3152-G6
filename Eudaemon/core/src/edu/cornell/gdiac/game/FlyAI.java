package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public class FlyAI extends AIController{
    private Enemy enemy;
    private Level level;
    public FlyAI(int ii, Level level) {
        super(ii,level);
    }
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){

    }
}