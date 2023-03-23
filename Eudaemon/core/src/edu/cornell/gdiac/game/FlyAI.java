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
    private static final int detectDistance=5;
    private final float enemyWidth;
    private final float enemyHeight;
    private final float tileSize;
    private Enemy enemy;
    private Level level;
    private int ticks=0;
    private FSMState state;
    private EnemyAction move;
    private int WanderWait=0;
    /**
     * in the level coordinate
     */
    private float[] goal;
    /** do we need to go to the next step in chasing?*/
    private boolean needGoal = true;
    private float[]tempgoal;


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
        this.enemyHeight= this.enemy.getHeight();
        this.enemyWidth= this.enemy.getWidth();
        this.tileSize=this.level.gettileSize();

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
//            System.out.println("up");
        }
        else if (move==EnemyAction.FLY_DOWN){
//            System.out.println("down");
        }
        else if (move==EnemyAction.STAY){
//            System.out.println("stay");
        }
        else if (move==EnemyAction.FLY_RIGHT){
//            System.out.println("right");
        }
        else if (move==EnemyAction.FLY_LEFT){
//            System.out.println("left");
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
                    needGoal=true;
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
//                        System.out.println("return to wander");
                    }
                }
                break;
            case CHASE_close:
                if(!sameCell()){
                    state=FSMState.CHASE;
                    needGoal = true;

                }
            case ATTACK:
                //TODO
                break;
        }
        if (state==FSMState.CHASE) {
//            System.out.println("chase");
        }

    }
    private void markGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY();
//       System.out.println("position is "+ex+":"+ey);

        int tx=level.levelToTileCoordinatesX(ex);
        int ty=level.levelToTileCoordinatesY(ey);//but NO!
//        System.out.println("position (in tile) is "+tx+":"+ty );

        Random rand = new Random();
        int randomInt;
        switch (state) {
            case SPAWN:
                goal[0]=ex;
                goal[1]=ey;
                break;
            case WANDER:
                int nx=tx;
                int ny=ty;
                randomInt = rand.nextInt();
                if (ticks%30==1) {
//                    System.out.println("we are changing goal right now");
                    if (randomInt % 4 == 0 && level.isAirAt(tx, ty - 1)) {
//                        System.out.println("1111");
                        ny = ny - 1;
                    } else if (randomInt % 4 == 1 && level.isAirAt(tx, ty + 1)) {
//                        System.out.println("2222");
                        ny = ny + 1;
                    } else if (randomInt % 4 == 2 && level.isAirAt(tx - 1, ty)) {
                        nx = nx - 1;
//                        System.out.println("3333");
                    } else if (randomInt % 4 == 3 && level.isAirAt(tx + 1, ty)) {
                        nx = nx + 1;
//                        System.out.println("4444");
                    } else {break;}

                    // the reason why we call this a second time is we must change the Y coordinate back
                    // to normal cardinality

                    goal[0]=level.tileToLevelCoordinatesX(nx);
                    goal[1]=level.tileToLevelCoordinatesY(ny);
//                    System.out.println("goal (in tile) is "+nx+":"+ny );
                }
                break;
            case CHASE:
                // only need to fly to the same tile
                int a=level.levelToTileCoordinatesX(level.getPlayer().getX());
                int b=level.levelToTileCoordinatesY(level.getPlayer().getY());
                if (a!=level.levelToTileCoordinatesX(goal[0])||b!=level.levelToTileCoordinatesY(goal[1])){
                    needGoal=true; //player is moving, need to re-compute the goal
//                    System.out.println("recompute because player moves");
                }
                goal[0]=level.tileToLevelCoordinatesX(a);
                goal[1]=level.tileToLevelCoordinatesY(b);
                break;
            case CHASE_close:
                goal[0]=level.getPlayer().getX();
                goal[1]=level.getPlayer().getY();
                break;
        }
//        System.out.println("goal is "+goal[0]+":"+goal[1]);
    }

    private float[] ChaseGoalHelper(){
        float ex = enemy.getX();
        float ey = enemy.getY();
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
            float[] result=new float[2];
            result[0]=level.tileToLevelCoordinatesX(cx)+this.tileSize/2;
            result[1]=level.tileToLevelCoordinatesY(cy)+this.tileSize/2;
            return result;}
        else{
            while(!boundary.isEmpty()){
                Coord co=boundary.removeFirst();
                int x=co.getX();
                int y=co.getY();
                if (x==gx&&y==gy){
                    EnemyAction nexttile= co.getDirection();
                    float[] result=new float[2];
                    if (nexttile==EnemyAction.FLY_DOWN){
                        result[0]=level.tileToLevelCoordinatesX(cx)+this.tileSize/2;
                        result[1]=level.tileToLevelCoordinatesY(cy+1)+this.tileSize/2;
                    }else if (nexttile==EnemyAction.FLY_UP){
                        result[0]=level.tileToLevelCoordinatesX(cx)+this.tileSize/2;
                        result[1]=level.tileToLevelCoordinatesY(cy-1)+this.tileSize/2;
                    }else if (nexttile==EnemyAction.FLY_RIGHT){
                        result[0]=level.tileToLevelCoordinatesX(cx+1)+this.tileSize/2;
                        result[1]=level.tileToLevelCoordinatesY(cy)+this.tileSize/2;
                    }else if (nexttile==EnemyAction.FLY_LEFT){
                    result[0]=level.tileToLevelCoordinatesX(cx-1)+this.tileSize/2;
                    result[1]=level.tileToLevelCoordinatesY(cy)+this.tileSize/2;
                    }
                    return result;
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
        float[] result=new float[2];
        result[0]=level.tileToLevelCoordinatesX(cx)+this.tileSize/2;
        result[1]=level.tileToLevelCoordinatesY(cy)+this.tileSize/2;
        return result;//do not move if cannot find a path
    }
    private void MoveAlongPathToGoal() {
        float ex = enemy.getX();
        float ey = enemy.getY();
//        System.out.println("moving along path, current position is "+ex+":"+ey);
//        System.out.println("moving along path, goal is "+goal[0]+":"+goal[1]);
        switch (state) {
            case WANDER:
            case CHASE_close:
                float dx=goal[0]+this.tileSize/2-ex;
                float dy=goal[1]+this.tileSize/2-ey;
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
                if (needGoal){
                    this.tempgoal=ChaseGoalHelper();
                    needGoal=false;
                }else {
//                    System.out.println("moving along path, tempgoal is "+tempgoal[0]+":"+tempgoal[1]);

                    float dtx = tempgoal[0] - ex;
                    float dty = tempgoal[1] - ey;
                    if (Math.abs(dtx) + Math.abs(dty) <= 0.2) {
                        needGoal = true;
//                        System.out.println("recompute because we reach there");
                    }
                    if (Math.abs(dtx) > Math.abs(dty)) {
                        if (dtx > 0) {
                            this.move = EnemyAction.FLY_RIGHT;
                        } else {
                            this.move = EnemyAction.FLY_LEFT;
                        }
                    } else {
                        if (dty > 0) {
                            this.move = EnemyAction.FLY_UP;
                        } else {
                            this.move = EnemyAction.FLY_DOWN;
                        }
                    }
                }
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