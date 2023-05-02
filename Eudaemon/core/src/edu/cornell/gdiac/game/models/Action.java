package edu.cornell.gdiac.game.models;

public enum Action {
    MOVE_LEFT("left", false),
    MOVE_RIGHT("right", false),
    BEGIN_JUMP("jump", true),
    HOLD_JUMP("jump", false),
    DASH("dash", false),
    HOLD_DASH("dash", false),
    ATTACK("attack", false),
    TRANSFORM("transform", false),
    LOOK_UP("up", false),
    LOOK_DOWN("down", false),
    DEBUG("debug", true),
    RESET("reset", false),
    PAUSE("pause", false);





    public final String name;
    public final boolean risingEdge;

    Action(String name, boolean risingEdge) { this.name = name; this.risingEdge = risingEdge; }
}
