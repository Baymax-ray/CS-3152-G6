package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public class ProjectileAI extends AIController{
    private final float tileSize;
    private final int CoolDown;
    private EnemyAction move;
    private FSMState state;
    private Level.MyGridGraph graph;
    private int ticks=0;
    private Vector2 v;
    private static final int detectDistance=8;


    private enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /** The enemy is ready to attack */
        ALERT
    }
    public ProjectileAI(int ii, Level level) {
        super(ii,level);
        this.state= FSMState.SPAWN;
        this.move=EnemyAction.STAY;
        this.level=super.level;
        this.enemy=super.enemy;
        this.tileSize=this.level.gettileSize();
        Level.MyGridGraph graph=Level.getGridGraph();
        this.graph=graph;
        this.CoolDown= enemy.getAttackCooldown();
    }
    /** In this special case, it return the velocity of the bullet instead of the enemy*/
    @Override
    public Vector2 getVelocity() {
            return this.v;
    }

    @Override
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction) {
        if(this.enemy.isRemoved()) return;
        ticks=ticks+1;
        // Process the FSM
        changeStateIfApplicable();
        enemyAction.add(move);

    }

    private boolean canAttack(){
        if (ticks<CoolDown) return false;
        float py=level.getPlayer().getY();
        float px=level.getPlayer().getX();
        int tpy=level.levelToTileCoordinatesY(py);
        int tpx=level.levelToTileCoordinatesX(px);
        float ey=enemy.getY();
        float ex=enemy.getX();
        int ty=level.levelToTileCoordinatesY(ey);
        int tx=level.levelToTileCoordinatesX(ex);
        if(Math.abs(tpy - ty) + Math.abs(tpx - tx) > detectDistance){
         return false;
        }
        float slope= (py-ey)/(px-ex);
        //Check if all tiles in the line are empty
        //first check horizontal
        if(px>ex){
            for(float i=0;i<Math.abs(px-ex);i=i+tileSize){
                if(!level.isAirAt(ex+i,ey+i*slope)){
                    return false;
                }
            }
        }else{
            for(float i=0;i<Math.abs(px-ex);i=i+tileSize){
                if(!level.isAirAt(ex-i,ey-i*slope)){
                    return false;
                }
            }
        }
        //then check vertical
        if(py>ey){
            for(float i=0;i<Math.abs(py-ey);i=i+tileSize){
                if(!level.isAirAt(ex+i/slope,ey+i)){
                    return false;
                }
            }
        }else{
            for(float i=0;i<Math.abs(py-ey);i=i+tileSize){
                if(!level.isAirAt(ex-i/slope,ey-i)){
                    return false;
                }
            }
        }
        return true;
    }
    private void changeStateIfApplicable() {
        switch (state) {
            case SPAWN:
                this.move = EnemyAction.STAY;
                if (ticks > 60) {
                    state = FSMState.ALERT;
                }
                break;
            case ALERT:
                if (canAttack()) {
                    this.move = EnemyAction.ATTACK;
                    //System.out.println("attack");
                    float vx=level.getPlayer().getX()-enemy.getX();
                    float vy=level.getPlayer().getY()-enemy.getY();
                    this.v=new Vector2(vx,vy);
                    ticks=0;
                }else{
                    this.move = EnemyAction.STAY;
                }
        }
    }
}
