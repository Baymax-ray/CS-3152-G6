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
    private boolean needGoal = true;
    //TODO 3-21
    int upRightDownLeft[][] = {{0,0}, {0,0}, {0,0}, {0,0}};

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
        if(this.enemy.isRemoved()) return;
        ticks=ticks+1;
        // Process the FSM
        changeStateIfApplicable();

        // Pathfinding
        markGoal();
        MoveAlongPathToGoal();
        if(move==EnemyAction.FLY_UP){
            System.out.println("up");
        }
        else if (move==EnemyAction.FLY_DOWN){
            System.out.println("down");
        }
        else if (move==EnemyAction.STAY){
            System.out.println("stay");
        }
        else if (move==EnemyAction.FLY_RIGHT){
            System.out.println("right");
        }
        else if (move==EnemyAction.FLY_LEFT){
            System.out.println("left");
        }

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
        if (Math.abs(tpy-ty)+Math.abs(tpx-tx)<=detectDistance){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean sameCell(){
        float py=level.getPlayer().getY();
        float px=level.getPlayer().getX();
        int tpy=level.levelToTileCoordinatesY(py);
        int tpx=level.levelToTileCoordinatesX(px);
        float ey=enemy.getY();
        float ex=enemy.getX();
        int ty=level.levelToTileCoordinatesY(ey);
        int tx=level.levelToTileCoordinatesX(ex);
        if (tpy==ty && tpx==tx){
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
                        System.out.println("return to wander");
                    }
                }
                break;
            case CHASE_close:
                if(!sameCell()){
                    state=FSMState.CHASE;
                }
            case ATTACK:
                //TODO
                break;
        }
        if (state==FSMState.WANDER){
            System.out.println("wander");
        } else if (state==FSMState.CHASE) {
            System.out.println("chase");

        }

    }
    private void markGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY();
        System.out.println("position is "+ex+":"+ey);

        int tx=level.levelToTileCoordinatesX(ex);
        int ty=level.levelToTileCoordinatesY(ey);//but NO!
        int ty2 = level.levelToTileCoordinatesX(ey);
        System.out.println("position (after transform) is "+tx+":"+ty + "and ty2 is: " + ty2);

        Random rand = new Random();
        int randomInt;
        switch (state) {
            case SPAWN:
                goal[0]=ex;
                goal[1]=ey;
                break;

            case WANDER:
                int nx=tx;
                int ny=ty2;
                if(Math.abs(ex - goal[0]) + Math.abs(ey - goal[1])<=0.5){needGoal = true;}
                if(!needGoal)break;

                System.out.println("finding an air. nx ny are: "+nx + " " + ny);
                randomInt = Math.abs(rand.nextInt());

                upRightDownLeft= new int[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}};
                if(level.isAirAt(tx, ty+1)){upRightDownLeft[0]= new int[]{0, 2};}
                else if (level.isAirAt(tx + 1, ty)) {upRightDownLeft[1] = new int[]{2, 0};}
                else if (level.isAirAt(tx, ty-1)){upRightDownLeft[2] = new int[]{0, -2};}
                else if (level.isAirAt(tx-1, ty)){upRightDownLeft[3] = new int[]{-2, 0};}
                int randStart = randomInt % 4;
//                for (int i = 0; i < upRightDownLeft.length; i++){
//
//                    int current = (randStart + i) % upRightDownLeft.length;
//                    if(upRightDownLeft[current][0]!=0 || upRightDownLeft[current][1] != 0){
//                        System.out.println("i found it");
//                        nx += upRightDownLeft[current][0];
//                        ny += upRightDownLeft[current][1];
//                        System.out.println("Now reset, and nx ny are: "+nx + " " + ny);
//                        break;
//                    }
//
//                }
                    if (randomInt % 4 == 0 && level.isAirAt(tx, ty - 1)) {
                        System.out.println("1111");
                        ny = ny - 1;
                    } else if (randomInt % 4 == 1 && level.isAirAt(tx, ty + 1)) {
                        System.out.println("2222");
                        ny = ny + 1;
                    } else if (randomInt % 4 == 2 && level.isAirAt(tx - 1, ty)) {
                        nx = nx - 1;
                        System.out.println("3333");
                    } else if (randomInt % 4 == 3 && level.isAirAt(tx + 1, ty)) {
                        nx = nx + 1;System.out.println("4444");
                    }

                goal[0]=nx;
                goal[1]=ny;
                System.out.println("goal is "+goal[0]+":"+goal[1]);
                needGoal = false;
                break;
            case CHASE:
                // only need to fly to the same tile
                goal[0]=level.tileToLevelCoordinatesX( level.levelToTileCoordinatesX(level.getPlayer().getX()));
                goal[1]=level.tileToLevelCoordinatesY( level.levelToTileCoordinatesY(level.getPlayer().getY()));
                break;
            case CHASE_close:
                goal[0]=level.getPlayer().getX();
                goal[1]=level.getPlayer().getY();
                break;

        }


    }
    private void MoveAlongPathToGoal() {
        float ex = enemy.getX();
        float ey = enemy.getY();
        System.out.println("moving along path, current position is "+ex+":"+ey);
        System.out.println("moving along path, goal is "+goal[0]+":"+goal[1]);
        switch (state) {
            case WANDER:
            case CHASE_close:
                float dx=goal[0]-ex;
                float dy=goal[1]-ey;
                if (Math.abs(dx)<=0.1 && Math.abs(dy)<=0.1){
                    this.move=EnemyAction.STAY;
                }
                else if (Math.abs(dx)>Math.abs(dy)){
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
                int gy=level.levelToTileCoordinatesY(goal[1]);
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
                if (level.isAirAt(level.tileToLevelCoordinatesX(cx),level.tileToLevelCoordinatesY(cy+1))){
                    Coord D=new Coord(cx,cy+1,EnemyAction.FLY_DOWN);
                    boundary.addLast(D);
                    visited.add(new int[] {cx,cy+1});
                }
                if (level.isAirAt(level.tileToLevelCoordinatesX(cx),level.tileToLevelCoordinatesY(cy-1))){
                    Coord U=new Coord(cx,cy-1,EnemyAction.FLY_UP);
                    boundary.addLast(U);
                    visited.add(new int[] {cx,cy-1});
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
                            Coord R=new Coord(x+1,y,co.getDirection());
                            if(!visited.contains(new int[] {x + 1, y})) {
                                boundary.addLast(R);
                                visited.add(new int[]{x + 1, y});
                            }
                        }
                        if (level.isAirAt(level.tileToLevelCoordinatesX(x),level.tileToLevelCoordinatesY(y+1))){
                            Coord D=new Coord(x,y+1,co.getDirection());
                            if(!visited.contains(new int[] {x, y+1})) {
                                boundary.addLast(D);
                                visited.add(new int[]{x, y+1});
                            }
                        }
                        if (level.isAirAt(level.tileToLevelCoordinatesX(x),level.tileToLevelCoordinatesY(y-1))){
                            Coord U=new Coord(x,y-1,co.getDirection());
                            if(!visited.contains(new int[] {x, y-1})) {
                                boundary.addLast(U);
                                visited.add(new int[]{x, y-1});
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