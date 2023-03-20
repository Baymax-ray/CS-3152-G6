package edu.cornell.gdiac.game;

import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;
import java.util.HashSet;
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
        /** The enemy has a target, but must get closer (not in the same tile)*/
        CHASE,
        /** The enemy has a target(in the same tile), but must get closer*/
        CHASE_close,
        /** The enemy has a target and is attacking it */
        ATTACK
    }
    public FlyAI(int ii, Level level) {
        super(ii,level);
        this.state= FSMState.SPAWN;
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
        int py=level.levelToTileCoordinatesY(level.getPlayer().getY());
        int px=level.levelToTileCoordinatesX(level.getPlayer().getX());
        int ey=level.levelToTileCoordinatesY(enemy.getY());
        int ex=level.levelToTileCoordinatesX(enemy.getX());
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
                // only need to fly to the same tile
                goal[0]=(float) level.levelToTileCoordinatesX(level.getPlayer().getX());
                goal[1]=(float) level.levelToTileCoordinatesX(level.getPlayer().getY());
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
            case WANDER:
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
                int gy=level.levelToTileCoordinatesY(goal[0]);
                int gx=level.levelToTileCoordinatesX(goal[0]);
                int cy=level.levelToTileCoordinatesY(ey);
                int cx=level.levelToTileCoordinatesX(ex);
                Queue<Coord> boundary= new Queue<>();
                HashSet<int[]>visited= new HashSet<>();
                if (level.isAirAt(level.tileToLevelCoordinatesX(cx-1),level.tileToLevelCoordinatesY(cy))){
                    Coord L=new Coord(cx-1,cy,EnemyAction.FLY_LEFT);
                    boundary.addLast(L);
                    visited.add(new int[] {cx-1, cy});
                }
                if (level.isAirAt(level.tileToLevelCoordinatesX(cx+1),level.tileToLevelCoordinatesY(cy))){
                    Coord R=new Coord(cx+1,cy,EnemyAction.FLY_RIGHT);
                    boundary.addLast(R);
                    visited.add(new int[] {cx+1, cy});
                }
                if (level.isAirAt(level.tileToLevelCoordinatesX(cx),level.tileToLevelCoordinatesY(cy-1))){
                    Coord D=new Coord(cx,cy-1,EnemyAction.FLY_DOWN);
                    boundary.addLast(D);
                    visited.add(new int[] {cx,cy-1});
                }
                if (level.isAirAt(level.tileToLevelCoordinatesX(cx),level.tileToLevelCoordinatesY(cy+1))){
                    Coord U=new Coord(cx,cy+1,EnemyAction.FLY_UP);
                    boundary.addLast(U);
                    visited.add(new int[] {cx,cy+1});
                }
                if (cx==gx&&cy==gy){
                    this.move= EnemyAction.STAY;
                    return;}
                else{
                    while(!boundary.isEmpty()){
                        Coord co=boundary.removeFirst();
                        int x=co.getX();
                        int y=co.getY();
                        if (x==gx&&y==gy){
                            this.move= co.getDirection();
                            return;
                        }
                        if (level.isAirAt(level.tileToLevelCoordinatesX(x-1),level.tileToLevelCoordinatesY(y))){
                            Coord L=new Coord(x-1,y,co.getDirection());
                            if(!visited.contains(new int[] {x - 1, y})) {
                                boundary.addLast(L);
                                visited.add(new int[]{x - 1, y});
                            }
                        }
                        if (level.isAirAt(level.tileToLevelCoordinatesX(x+1),level.tileToLevelCoordinatesY(y))){
                            Coord R=new Coord(x-1,y,co.getDirection());
                            if(!visited.contains(new int[] {x + 1, y})) {
                                boundary.addLast(R);
                                visited.add(new int[]{x + 1, y});
                            }
                        }
                        if (level.isAirAt(level.tileToLevelCoordinatesX(x),level.tileToLevelCoordinatesY(y-1))){
                            Coord D=new Coord(x,y-1,co.getDirection());
                            if(!visited.contains(new int[] {x, y-1})) {
                                boundary.addLast(D);
                                visited.add(new int[]{x, y-1});
                            }
                        }
                        if (level.isAirAt(level.tileToLevelCoordinatesX(x),level.tileToLevelCoordinatesY(y+1))){
                            Coord U=new Coord(x,y+1,co.getDirection());
                            if(!visited.contains(new int[] {x, y+1})) {
                                boundary.addLast(U);
                                visited.add(new int[]{x, y+1});
                            }
                        }
                    }
                }
                this.move=EnemyAction.STAY; //do not move if cannot find a path
                break;
        }
    }
    private class Coord {
        private int x;
        private int y;
        private EnemyAction d;
        public Coord(int x, int y,EnemyAction direction){
            this.x=x;
            this.y=y;
            this.d=direction;
        }

        public int getX(){return x;}
        public int getY(){return y;}
        public EnemyAction getDirection(){return d;}

    }
}