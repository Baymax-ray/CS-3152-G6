package edu.cornell.gdiac.game;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;
import java.util.Random;

public class FlyAI extends AIController{
    private static final int maxWait = 10;
    private static final int detectDistance=9;
    private final float tileSize;
    private final Enemy enemy;
    private final Level level;
    private int anchorX;
    private int anchorY;
    private int ticks=0;
    private Vector2 v;
    private FSMState state;
    private EnemyAction move;
    private int WanderWait=0;
    /**
     * in the level coordinate
     */
    private final float[] goal;
    /** do we need to go to the next step in chasing?*/
    private boolean needNewPath = true;
    private final Heuristic<Level.MyNode> heuristic;
    private final Level.MyGridGraph graph;
    private final GraphPath<Level.MyNode> path;
    private int indexAlongPath=0;

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
        this.tileSize=this.level.gettileSize();
        this.heuristic= new EuclideanDistance();
        this.graph= level.getGridGraph();
        this.path = new DefaultGraphPath<>();
        float ey=enemy.getY();
        float ex=enemy.getX();
        this.anchorX = level.levelToTileCoordinatesX(ex);
        this.anchorY = level.levelToTileCoordinatesY(ey);
    }

    @Override
    public Vector2 getVelocity() {
        return this.v;
    }

    public void setEnemyAction(EnumSet<EnemyAction> enemyAction){
        if(this.enemy.isRemoved()){
            return;
        }
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
        return Math.abs(tpy - ty) + Math.abs(tpx - tx) <= detectDistance;
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
        return tpy == ty && tpx == tx;
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
                    needNewPath =true;
                }
                break;
            case CHASE:
                if (canAttack()){
                    state= FSMState.ATTACK;
                }
                else if(sameCell()){
                    state= FSMState.CHASE_close;
                    this.WanderWait=0;
                }
                else if (!checkDetection()){
                    WanderWait++;
                    if (WanderWait>maxWait){
                        this.WanderWait=0;
                        state= FSMState.WANDER;
                        needNewPath=true;
                        float ey=enemy.getY();
                        float ex=enemy.getX();
                        this.anchorX = level.levelToTileCoordinatesX(ex);
                        this.anchorY =level.levelToTileCoordinatesY(ey);
                    }
                }else{
                    this.WanderWait=0;
                }
                break;
            case CHASE_close:
                if(!sameCell()){
                    state=FSMState.CHASE;
                    needNewPath = true;

                }
            case ATTACK:
                //TODO: animations?
                break;
        }
    }
    private void markGoal(){
        float ex=enemy.getX();
        float ey=enemy.getY();
//       System.out.println("position is "+ex+":"+ey);

        Random random = new Random();
        int x=3;
        switch (state) {
            case SPAWN:
                goal[0]=ex;
                goal[1]=ey;
                break;
            case WANDER:
                int nx=anchorX;
                int ny=anchorY;
                if (ticks%300==1) {
                    needNewPath=true;
                    int randomIntX = random.nextInt(2 * x + 1) - x;
                    nx=nx+randomIntX;
                    int randomIntY = random.nextInt(2 * x + 1) - x;
                    ny=ny+randomIntY;
                    // the reason why we call this a second time is we must change the Y coordinate back
                    // to normal cardinality
                    goal[0]=level.tileToLevelCoordinatesX(nx);
                    goal[1]=level.tileToLevelCoordinatesY(ny);
                }
                break;
            case CHASE:
                // only need to fly to the same tile
                int a=level.levelToTileCoordinatesX(level.getPlayer().getX());
                int b=level.levelToTileCoordinatesY(level.getPlayer().getY());
                if (a!=level.levelToTileCoordinatesX(goal[0])||b!=level.levelToTileCoordinatesY(goal[1])){
                    needNewPath =true; //player is moving, need to re-compute the goal
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

    private void MoveAlongPathToGoal() {
        float ex = enemy.getX();
        float ey = enemy.getY();
        //boolean foundPath = pathFinder.searchNodePath(startNode, endNode, heuristic, path);
        switch (state) {
            case SPAWN:
                this.move=EnemyAction.STAY;
                break;
            case CHASE_close:
                float dx=goal[0]-ex;
                float dy=goal[1]-ey;
                this.move=EnemyAction.FLY;
                this.v=new Vector2(dx,dy);
                break;
            case WANDER:
            case CHASE:
                IndexedAStarPathFinder<Level.MyNode> pathFinder = new IndexedAStarPathFinder<>(graph);
                if (needNewPath){
                    Level.MyNode startNode = graph.getNode(level.levelToTileCoordinatesX(ex), level.levelToTileCoordinatesY(ey));
                    Level.MyNode endNode = graph.getNode(level.levelToTileCoordinatesX(goal[0]), level.levelToTileCoordinatesY(goal[1]));
                    path.clear();
                    indexAlongPath=0;
//                    System.out.println("recalculate the path to: "+endNode.getX()+": "+endNode.getY());
                    boolean foundPath = pathFinder.searchNodePath(startNode, endNode, heuristic, path);
                    if(!foundPath){//did not find the path
//                        System.out.println("Did not find path, so stay");
                        this.move=EnemyAction.STAY;
                        break;
                    }else{
                    needNewPath =false;}
                }

                if (path.getCount()>indexAlongPath) {
                    this.move=EnemyAction.FLY;
//                    System.out.println("moving along path, index is "+this.indexAlongPath);
                    Level.MyNode goalnode=path.get(indexAlongPath);
                    float gx= level.tileToLevelCoordinatesX(goalnode.getX())+this.tileSize/2;
                    float gy= level.tileToLevelCoordinatesY(goalnode.getY())+this.tileSize/2;
                    this.v=new Vector2(gx-ex,gy-ey);
//                    System.out.println("the current goal is: "+gx+": "+gy );
//                    System.out.println("I am here "+ex+":"+ey);
                    if ((Math.abs(gx-ex)<0.2) &&(Math.abs(gy-ey)<0.2)){
                        //reach this goal, move to the next goal
//                        System.out.println("moving to next index");
                        indexAlongPath=indexAlongPath+1;
                        if (indexAlongPath>=path.getCount() && this.state.equals(FSMState.CHASE)){
                            needNewPath=true;
                        }
                    }
                }else{
                    this.move=EnemyAction.STAY;
                }
                break;
        }
    }

    private class EuclideanDistance implements Heuristic<Level.MyNode> {
        @Override
        public float estimate(Level.MyNode node, Level.MyNode endNode) {
            float dx = Math.abs(node.getX() - endNode.getX());
            float dy = Math.abs(node.getY() - endNode.getY());
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
    }
}