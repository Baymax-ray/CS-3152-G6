package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;
import java.util.Random;

public class GoombaAI extends AIController{
    private Enemy enemy;
    private Level level;
    private int ticks=0;
    private FSMState state;
    private EnemyAction move;
    private enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /** The enemy is patrolling around without a target */
        WANDER,
        /** The enemy has a target, but must get closer */
        CHASE,
        /** The enemy has a target and is attacking it */
        ATTACK
    }
    public GoombaAI(int ii, Level level) {
        super(ii,level);
        this.state=FSMState.SPAWN;
        this.move=EnemyAction.STAY;
    }
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){
        ticks=ticks+1;
    }
    public boolean checkDetection(){
        float py=level.getPlayer().getY();
        float px=level.getPlayer().getX();
        float ey=enemy.getY();
        float ex=enemy.getX();
        if (Math.abs(py-ey)>1){return false;}
        else{
            float start=Math.min(px,ex);
            float end=Math.max(px,ex);
            for (float i=start;i<=end;i++){
                if(!level.isAirAt(i,ey)){
                    return false;
                }
            }
            return true;
        }
    }
    private void changeStateIfApplicable() {
        Random rand = new Random();
        int randomInt;
        switch (state) {
            case SPAWN:
                if (ticks>60){
                    state=FSMState.WANDER;
                }
            case WANDER:
                if (checkDetection()){
                    state=FSMState.CHASE;
                }
            case CHASE:
                //TODO
                break;
            case ATTACK:
                //TODO
                break;
        }

    }
    public void markGoal(){}
    public EnemyAction getMoveAlongPathToGoal(){}

}
