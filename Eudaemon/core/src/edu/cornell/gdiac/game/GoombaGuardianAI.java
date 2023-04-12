package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.ArrayList;
import java.util.EnumSet;

public class GoombaGuardianAI extends AIController{
    private final Enemy enemy;
    private final Level level;
    private final float tileSize;
    private final float enemyHeight;
    private int indexAlongList;
    private int ticks=0;
    private FSMState state;
    private EnemyAction move;
    /**the amount of time to wait before back to wander state*/
    private final float[] goal;
    private int guardianTime;
    private ArrayList<Integer> guardianList;
    private enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /**The the enemy move between guard positions*/
        GUARD,
        /**The the enemy stay in the position*/
        STAY,
        /** The enemy has a target and is attacking it */
        ATTACK
    }
    public GoombaGuardianAI(int ii, Level level) {
        super(ii,level);
        this.state=FSMState.SPAWN;
        this.move=EnemyAction.STAY;
        this.goal=new float[2];
        this.level=super.level;
        this.enemy=super.enemy;
        this.guardianList=enemy.getGuardianList();
        this.guardianTime =enemy.getGuardianTime();
        this.indexAlongList=0;
        this.tileSize=this.level.gettileSize();
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

    private boolean canAttack(){
        //ToDo
        return false;
    }
    private boolean arrive(){
        float ex=enemy.getX();
        float gx=level.tileToLevelCoordinatesX(guardianList.get(indexAlongList))+tileSize/2;
        return (Math.abs(ex-gx)<0.1);
    }
    private void changeStateIfApplicable() {
        switch (state) {
            case SPAWN:
                if (ticks>60){
                    state=FSMState.GUARD;
                }
                break;
            case GUARD:
                if (canAttack()){
                    state=FSMState.ATTACK;
                }
                if (arrive()){
                    indexAlongList=(indexAlongList+1)%guardianList.size();
                    state=FSMState.STAY;
                    ticks=0;
                }
                break;
            case STAY:
                if (ticks>guardianTime){
                    state=FSMState.GUARD;
                }
                break;
            case ATTACK:
                //TODO: ?
                break;
        }

    }
    private void markGoal(){
        float ex=enemy.getX();
        switch (state) {
            case STAY:
            case SPAWN:
                goal[0]=ex;
                break;
            case GUARD:
                goal[0]=level.tileToLevelCoordinatesX(guardianList.get(indexAlongList))+tileSize/2;
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

        if(Math.abs(ex-goal[0])<0.1){this.move=EnemyAction.STAY;}
        else if (ex<goal[0]){
            this.move=EnemyAction.MOVE_RIGHT;
        }
        else if (ex>goal[0]){
            this.move=EnemyAction.MOVE_LEFT;
        }
    }
}