package edu.cornell.gdiac.physics;

public class AIController {

    /** How close a target must be for us to attack it */
    private static final int ATTACK_DIST = 4;

    /**The unique id specifying which AI this controller is assigned to*/
    private int id;
    /**The game board to communicate map information, such as if there is a wall ahead*/
    private Board board;

    /**
     * The FSMState SHOULD control whether the AI is in wandering, chasing,
     * or attacking mode.
     * */
    private static enum FSMState {

    }

    public AIController(int id, Board board){
        this.id = id;
        this.board = board;
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
