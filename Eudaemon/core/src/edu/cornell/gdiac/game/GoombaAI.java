package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;
import java.util.Random;

public class GoombaAI extends AIController{
    public static final int maxWait = 10;
    private Enemy enemy;
    private Level level;
    private int ticks=0;
    private FSMState state;
    private EnemyAction move;
    /**the amount of time to wait before back to wander state*/
    private int WanderWait=0;
    private float[] goal;
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
    public GoombaAI(int ii, Level level) {
        super(ii,level);
        this.state=FSMState.SPAWN;
        this.move=EnemyAction.STAY;
        this.goal=new float[2];
        this.level=super.level;
        this.enemy=super.enemy;

    }
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){
        ticks=ticks+1;
        // Process the FSM
        changeStateIfApplicable();

        // Pathfinding
        markGoal();
        MoveAlongPathToGoal();

        enemyAction.add(move);
        if (enemyAction.size()>1){
        System.out.println(enemyAction.size());}

    }
    private boolean checkDetection(){
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
            this.WanderWait=0;
            return true;
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
                    state=FSMState.TOWANDER;
                }
                break;
            case TOWANDER:
                state=FSMState.WANDER;
            case WANDER:
                System.out.println("Wander");
                if (checkDetection()){
                    state=FSMState.CHASE;
                }
                break;
            case CHASE:
                if (canAttack()){
                    state=FSMState.ATTACK;
                }
                else if (!checkDetection()){
                    WanderWait++;
                    if (WanderWait>maxWait){
                        WanderWait=0;
                        state=FSMState.TOWANDER;
                    }
                }
                break;
            case ATTACK:
                //TODO
                break;
        }

    }
    private void markGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY();
        int tx=level.levelToTileCoordinatesX(ex);
        int ty=level.levelToTileCoordinatesY(ey);

        int a;
        switch (state) {
            case SPAWN:
                goal[0]=ex;
                goal[1]=ey;
            break;
            case TOWANDER:
                boolean c=true;
                goal[1]=ey;
                a=1;
                while(c){
                    if(level.isAirAt(tx+a,ty-1)){
                        goal[0]=level.tileToLevelCoordinatesX(tx+a);
                        c=false;
                    } else if (level.isAirAt(tx-a,ty-1)) {
                        goal[0]=level.tileToLevelCoordinatesX(tx-a);
                        c=false;
                    }
                    a++;
                }
                break;
            case WANDER:
                // turn if the ground of next cell is air or next cell is not air
                if (goal[0]>=ex && (level.isAirAt(tx+1,ty-1)||!level.isAirAt(tx+1,ty))){
                    boolean cl=true;
                    a=1;
                    while(cl){
                        if (level.isAirAt(tx-a,ty-1)&& !level.isAirAt(tx-a,ty)) {
                            goal[0]=level.tileToLevelCoordinatesX(tx-a)-1;
                            cl=false;
                        }
                        a++;
                    }
                }
                else if(goal[0]<ex && (level.isAirAt(tx-1,ty-1)||!level.isAirAt(tx-1,ty))){
                    boolean cr=true;
                    a=1;
                    while(cr){
                        if (level.isAirAt(tx+a,ty-1)&& !level.isAirAt(tx+a,ty)) {
                            goal[0]=level.tileToLevelCoordinatesX(tx+a)+1;
                            cr=false;
                        }
                        a++;
                    }
                }
                break;
            case CHASE:
                goal[0]=level.getPlayer().getX();
                break;
        }
        System.out.println(goal[0]);

    }
    private void MoveAlongPathToGoal(){
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

