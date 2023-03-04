package edu.cornell.gdiac.physics;

public class AIController {

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



}
