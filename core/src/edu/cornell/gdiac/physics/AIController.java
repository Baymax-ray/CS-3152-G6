package edu.cornell.gdiac.physics;

public class AIController {

    private int id;
    private Board board;

    private static enum FSMState {

    }
    private static final int ATTACK_DIST = 4;
    public AIController(int id, Board board){
        this.id = id;
        this.board = board;
    }



}
