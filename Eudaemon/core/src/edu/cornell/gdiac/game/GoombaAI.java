package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public class GoombaAI extends AIController{
    public static final int maxWait = 100;
    private final Enemy enemy;
    private final Level level;
    private final float enemyHeight;
    private int ticks=0;
    private FSMState state;
    private EnemyAction move;
    /**the amount of time to wait before back to wander state*/
    private int WanderWait=0;
    private final float[] goal;
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
        this.enemyHeight=enemy.getHeight();

    }
    //goomba AI do not actually need this method
    @Override
    public Vector2 getVelocity() {
        return null;
    }

    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){
        if(this.enemy.isRemoved()) return;
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
        int tpy=level.levelToTileCoordinatesY(py);
        int tpx=level.levelToTileCoordinatesX(px);
        float ey=enemy.getY();
        float ex=enemy.getX();
        int ty=level.levelToTileCoordinatesY(ey);
        int tx=level.levelToTileCoordinatesX(ex);
        if (ty!=tpy){return false;}
        else{
            int start=Math.min(tpx,tx);
            int end=Math.max(tpx,tx);
            for (int i=start;i<=end;i++){
                //that shall be reachable
                if(!level.isAirAt(i,ey)){
                    return false;
                }
                //also there shall be all land under feet
//                if(level.isAirAt(i, ey-1)){
//                    return false;
//                }
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
                break;
            case WANDER:
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
                        //System.out.println("now to wander");
                        WanderWait=0;
                        state=FSMState.TOWANDER;
                    }
                }
                break;
            case ATTACK:
                break;
        }

    }
    private void markGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY()-enemyHeight/2+0.1f;//check the feet of the enemy
        int tx=level.levelToTileCoordinatesX(ex);
        int ty=level.levelToTileCoordinatesY(ey);
        switch (state) {
            case SPAWN:
                goal[0]=ex;
            break;
            case TOWANDER:
                if(!level.isAirAt(tx+1,ty+1)){
                    goal[0]=level.tileToLevelCoordinatesX(tx+2);
                } else if (!level.isAirAt(tx-1,ty+1)) {
                    goal[0]=level.tileToLevelCoordinatesX(tx-2);
                }
                break;
            case WANDER:
                // turn if the ground of next cell is air or next cell is not air
                if (goal[0]>=ex){ //going right
                    if(level.isAirAt(tx+1,ty+1)||!level.isAirAt(tx+1,ty)) { //need to turn
                        goal[0]=level.tileToLevelCoordinatesX(tx-2);
                    }
                    else{
                        goal[0]=level.tileToLevelCoordinatesX(tx+2);
                    }
                }
                else if(goal[0]<ex) { //going left
                    if (level.isAirAt(tx-1,ty+1)||!level.isAirAt(tx-1,ty)) { //need to turn
                        goal[0]=level.tileToLevelCoordinatesX(tx+2);
                    }
                    else{
                        goal[0]=level.tileToLevelCoordinatesX(tx-2);
                    }
                }
                else {
                    goal[0]=ex; //should not reach here
                }
                break;
            case CHASE:
                goal[0]=level.getPlayer().getX();
                break;
        }
//        System.out.println("goal is "+goal[0]);

    }
    private void MoveAlongPathToGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY()-enemyHeight/2+0.1f;//check the feet of the enemy
//        System.out.println("moving along path, current position is "+ex+":"+ey);

        int tx=level.levelToTileCoordinatesX(ex);
        int ty=level.levelToTileCoordinatesY(ey);
//        System.out.println("in tiles urrent position is "+tx+":"+ty);

        if(Math.abs(ex-goal[0])<0.1){this.move=EnemyAction.STAY;}
        else if (ex<goal[0]){
            this.move=EnemyAction.MOVE_RIGHT;
            if(level.isAirAt(tx+1,ty+1)){
//                System.out.println("stop!!");
                this.move=EnemyAction.STAY;
            }
        }
        else if (ex>goal[0]){
            this.move=EnemyAction.MOVE_LEFT;
            if(level.isAirAt(tx-1,ty+1)){
//                System.out.println("stop!!");
                this.move=EnemyAction.STAY;
            }
        }
        }
    }