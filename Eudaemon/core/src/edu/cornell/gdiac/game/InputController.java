package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Action;

import java.util.EnumSet;

public class InputController {

    public EnumSet<Action> getActions() {
        return EnumSet.noneOf(Action.class);
    }
}
