package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;
import java.util.Random;

public class FlyAI extends AIController{
    private static final int maxWait = 10;
    private static final int detectDistance=4;

    private Enemy enemy;
    private Level level;
    private int ticks=0;
    private FSMState state;
    private EnemyAction move;
    private int WanderWait=0;
    private float[] goal;

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
    public FlyAI(int ii, Level level) {
        super(ii,level);
        this.state= FSMState.SPAWN;
        this.move=EnemyAction.STAY;
    }
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){
        ticks=ticks+1;
        // Process the FSM
        changeStateIfApplicable();

        // Pathfinding
        markGoal();
        MoveAlongPathToGoal();

        enemyAction.add(move);

    }
    public boolean checkDetection(){
        float py=level.getPlayer().getY();
        float px=level.getPlayer().getX();
        float ey=enemy.getY();
        float ex=enemy.getX();
        if (Math.abs(py-ey)+Math.abs(px-ex)<=detectDistance){
            return true;
        }
        else{
            return false;
        }
    }
    public boolean canAttack(){
        //ToDo
        return false;
    }
    private void changeStateIfApplicable() {
        switch (state) {
            case SPAWN:
                if (ticks>60){
                    state= FSMState.WANDER;
                }
                break;
            case WANDER:
                if (checkDetection()){
                    state= FSMState.CHASE;
                }
                break;
            case CHASE:
                if (canAttack()){
                    state= FSMState.ATTACK;
                }
                else if (!checkDetection()){
                    WanderWait++;
                    if (WanderWait>maxWait){
                        WanderWait=0;
                        state= FSMState.WANDER;
                    }
                }
                break;
            case ATTACK:
                //TODO
                break;
        }

    }
    public void markGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY();
        Random rand = new Random();
        int randomInt;
        switch (state) {
            case SPAWN:
                goal[0]=ex;
                goal[1]=ey;
                break;

            case WANDER:
                float nx=ex;
                float ny=ex;
                randomInt = rand.nextInt();
                if(randomInt%4==0&& level.isAirAt(ex,ey-1)){ny=ey-1;}
                else if (randomInt%4==1&&level.isAirAt(ex,ey+1)){ny=ey+1;}
                else if (randomInt%4==2&&level.isAirAt(ex-1,ey)){nx=ex-1;}
                else if (randomInt%4==3&&level.isAirAt(ex+1,ey)){nx=ex=1;}
                goal[0]=nx;
                goal[1]=ny;
                break;
            case CHASE:
                goal[0]=level.getPlayer().getX();
                goal[1]=level.getPlayer().getY();
                break;
        }}
    public void MoveAlongPathToGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY();

        if(ex<=goal[0]+0.1||ex>=goal[0]-0.1){this.move=EnemyAction.STAY;}
        else if (ex<goal[0]){
            this.move=EnemyAction.MOVE_RIGHT;
            if(level.isAirAt(ex+1,ey-1)){this.move=EnemyAction.STAY;}
        }
        else if (ex>goal[0]){
            this.move=EnemyAction.MOVE_LEFT;
            if(level.isAirAt(ex-1,ey-1)){this.move=EnemyAction.STAY;}
        }
    }
}