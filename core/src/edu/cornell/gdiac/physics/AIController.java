package edu.cornell.gdiac.physics;

public class AIController {

    /** How close a target must be for us to attack it */
    private static final int ATTACK_DIST = 4;
    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;

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

    public AIController(EnemyModel enemy, Board board){
        this.enemy = enemy;
        this.board = board;
        this.platform = board.query(enemy);
        this.move = CONTROL_NO_ACTION;
        this.state = FSMState.SPAWN;
    }

    /**
     * Returns the action selected by this InputController
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
        int action = move;


        return action;
    }

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
        //TODO

    }



}
