package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Action;
import edu.cornell.gdiac.game.models.ActionBindings;

import java.util.EnumSet;

public class InputController {

    ActionBindings bindings;

    public InputController(ActionBindings bindings) {
        this.bindings = bindings;
    }


    public void setPlayerAction(EnumSet<Action> playerAction) {
        //TODO: Actually update action thx
    }
}
