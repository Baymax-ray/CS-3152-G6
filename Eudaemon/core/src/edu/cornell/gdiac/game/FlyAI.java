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
        /** The enemy has a target, but must get closer (not in the same cell)*/
        CHASE,
        /** The enemy has a target(in the same cell), but must get closer*/
        CHASE_close,
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
    private boolean checkDetection(){
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

    private boolean sameCell(){
        float py=level.getPlayer().getY();
        float px=level.getPlayer().getX();
        float ey=enemy.getY();
        float ex=enemy.getX();
        if (Math.abs(py-ey)<1&&Math.abs(px-ex)<1){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean canAttack(){
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
                else if(sameCell()){
                    state= FSMState.CHASE_close;
                }
                else if (!checkDetection()){
                    WanderWait++;
                    if (WanderWait>maxWait){
                        WanderWait=0;
                        state= FSMState.WANDER;
                    }
                }
                break;
            case CHASE_close:
                if(sameCell()){
                    state=FSMState.CHASE;
                }
            case ATTACK:
                //TODO
                break;
        }

    }
    private void markGoal(){
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
                // only need to fly to the same cell
                goal[0]=(float) Math.round(level.getPlayer().getX());
                goal[1]=(float) Math.round(level.getPlayer().getY());
                break;
            case CHASE_close:
                goal[0]=level.getPlayer().getX();
                goal[1]=level.getPlayer().getY();
                break;

        }}
    private void MoveAlongPathToGoal() {
        float ex = enemy.getX();
        float ey = enemy.getY();

        switch (state) {
            case CHASE_close:
                float dx=goal[0]-ex;
                float dy=goal[1]-ey;
                if (Math.abs(dx)>Math.abs(dy)){
                    if (dx>0){
                        this.move=EnemyAction.FLY_RIGHT;
                    }else{
                        this.move=EnemyAction.FLY_LEFT;
                    }
                }else{
                    if (dy>0){
                        this.move=EnemyAction.FLY_UP;
                    }
                    else{
                        this.move=EnemyAction.FLY_DOWN;
                    }
                }
                break;
            default:
            break;
        }
    }
}