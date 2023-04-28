package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import edu.cornell.gdiac.game.models.Action;
import edu.cornell.gdiac.game.models.ActionBindings;
import edu.cornell.gdiac.util.Controllers;

import java.util.EnumSet;
//import com.studiohartman.jamepad.ControllerManager;

public class InputController {

    private final ActionBindings bindings;
    private final EnumSet<Action> prevAction;

    public InputController(ActionBindings bindings) {
        this.bindings = bindings;
        prevAction = EnumSet.noneOf(Action.class);
//        ControllerManager controllers = new ControllerManager();
//        controllers.initSDLGamepad();
   }

    /**
     * Updates the player's actions based on the input from the keyboard or Xbox controller.
     * Returns a new {@link EnumSet} of {@link Action} representing the updated player actions.
     *
     * @param playerAction the current {@link EnumSet} of {@link Action} representing the player's actions
     */
    public void setPlayerAction(EnumSet<Action> playerAction) {

        //set previous action to the passed in actions
        prevAction.clear();
        prevAction.addAll(playerAction);

        //Clearing action set of previous actions
        playerAction.clear();

        Array<Controller> controllers = Controllers.get().getControllers();

        if (controllers.size > 0) {
            Controller controller = controllers.first();

            ControllerMapping mapping = controller.getMapping();

            if (controller.getButton(mapping.buttonX)) {
                playerAction.add(Action.DASH);
                playerAction.add(Action.ATTACK);
            }
            if (controller.getButton(mapping.buttonA)) {
                if (!prevAction.contains(Action.HOLD_JUMP)) playerAction.add(Action.BEGIN_JUMP);
                playerAction.add(Action.HOLD_JUMP);
            }

            if (controller.getButton(mapping.buttonY)) {
                playerAction.add(Action.TRANSFORM);
            }

            if (controller.getAxis(0) < -0.5) {
                playerAction.add(Action.MOVE_LEFT);
            }
            if (controller.getAxis(0) > 0.5) {
                playerAction.add(Action.MOVE_RIGHT);
            }
            if (controller.getAxis(1) < -0.5) {
                playerAction.add(Action.LOOK_UP);
            }
            if (controller.getAxis(1) > 0.5) {
                playerAction.add(Action.LOOK_DOWN);
            }


        }

        /*
        * jump b
        * transform y
        * dash x
        * attack x
        * */

        //Checking for jumps

        for (Action action : Action.values()) {
            if (action.risingEdge && checkRisingEdgeAction(action) || !action.risingEdge && checkHeldAction(action)) {
                playerAction.add(action);
            }
        }
    }

    public boolean checkRisingEdgeAction(Action action) {
        return bindings.getKeyMap().containsKey(action) && Gdx.input.isKeyJustPressed(bindings.getKeyMap().get(action)) ||
                bindings.getMouseMap().containsKey(action) && Gdx.input.isButtonJustPressed(bindings.getMouseMap().get(action));
    }

    public boolean checkHeldAction(Action action) {
        return bindings.getKeyMap().containsKey(action) && Gdx.input.isKeyPressed(bindings.getKeyMap().get(action)) ||
                bindings.getMouseMap().containsKey(action) && Gdx.input.isButtonPressed(bindings.getMouseMap().get(action));
    }

}
