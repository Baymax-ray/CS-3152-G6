package edu.cornell.gdiac.physics;

import java.util.Random;

public class AIController {

    /** How close a target must be for us to attack it */
    private static final int ATTACK_DIST = 4;
    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Attack */
    public static final int CONTROL_FIRE 	   = 0x10;
    /**The enemy that this AIController is controlling*/
    private EnemyModel enemy;
    /**The game board to communicate map information, such as if there is a wall ahead*/
    private Board board;
    /**The platform that this enemy is standing on*/
    private float[][] platform;
    /** The enemy's next action (may include firing). */
    private int move; // A ControlCode
    /** The enemy's current state in the FSM */
    private FSMState state;
    /** The number of ticks since we started this controller */
    private long ticks;
    /** index of this enemy*/
    private int id;
    /** the goal to move*/
    private int[] goal;

    /**
     * The FSMState SHOULD control whether the AI is in wandering, chasing,
     * or attacking mode.
     * */
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

    public AIController(EnemyModel enemy, Board board,int id){
        this.enemy = enemy;
        this.board = board;
        this.platform = board.StandOn(enemy.getX(), enemy.getY()-0.3f);
        this.move = CONTROL_NO_ACTION;
        this.state = FSMState.SPAWN;
        this.ticks=0;
        this.id=id;
    }

    /**
     * Returns the action selected by this AIController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        ticks++;
        if ((id + ticks) % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable();

            // Pathfinding
            markGoal();
            move = getMoveAlongPathToGoal();
        }

        int action = move;

        // If we're attacking someone and we can shoot him now, then do so.
        if (state == FSMState.ATTACK && canAttack()) {
            action |= CONTROL_FIRE;
        }
        return action;
    }

    /**
     * the extreme value of a coordinate of a list of points
     * @param points
     * @param xy 0 for x and 1 for y
     * @return [min,max]
     */
    public float[] extremeValuePoints(float[][] points, int xy){
        float smallest = Float.MAX_VALUE;
        float largest = Float.MIN_VALUE;

        for (int i = 0; i < points.length; i++) {
            float x = points[i][xy];
            if (x < smallest) {
                smallest = x;
            }
            if (x>largest){
                largest=x;
            }
        }
        float [] result=new float[2];
        result[0] = smallest;
        result[1] = largest;
        return result;
    }
    public void markGoal(){
        switch (state) {
            case SPAWN:
                goal[0]= (int) enemy.getX();
                goal[1]= (int) enemy.getY();
                break;
            case WANDER:
                float[] est= extremeValuePoints(platform,0);
                float dist1 = Math.abs(enemy.getX() - est[0]);
                float dist2 = Math.abs(enemy.getX() - est[1]);
                if(dist1 <= dist2 && dist1 >= 1){
                    // go left
                    goal[0]=(int)enemy.getX()-1;
                    goal[1]=(int)enemy.getY();
                }
                else if(dist2 >= 1){
                    //go right
                    goal[0]=(int)enemy.getX()+1;
                    goal[1]=(int)enemy.getY();
                }
                else {
                    //stay
                    goal[0]=(int)enemy.getX();
                    goal[1]=(int)enemy.getY();
                }
                break;
            case CHASE:
                break;
            case ATTACK:
                break;
        }
    }
    public int getMoveAlongPathToGoal(){
        int sx=(int)enemy.getX();
        int sy=(int)enemy.getX();
        int dx=sx-goal[0];
        if (dx==0){return CONTROL_NO_ACTION;}
        else if (dx<0){return CONTROL_MOVE_LEFT;}
        else if (dx>0){return CONTROL_MOVE_RIGHT;}
        return CONTROL_NO_ACTION;
    }
    public boolean canAttack(){
        //ToDo
        return false;
    }
    /**
     * If this enemy can detect the character
     * @return true if can see the character
     */
    public boolean checkDetection(){return false;}
    /**
     * Change the state of the ship.
     *
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {
        //Working on!
        Random rand = new Random();
        int randomInt;
        switch (state) {
            case SPAWN:
                if (ticks>60){state=FSMState.WANDER;}
            case WANDER:
                if (checkDetection()){
                    state=FSMState.CHASE;
                }
            case CHASE:
                break;
            case ATTACK:
                break;
        }

    }



}