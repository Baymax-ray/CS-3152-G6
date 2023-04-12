package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.game.models.Action;
import edu.cornell.gdiac.game.models.ActionBindings;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.XBoxController;

import java.util.EnumSet;
import java.util.HashMap;

public class InputController {

    private final HashMap<String, Integer> inputMap;
    private final EnumSet<Action> prevAction;

    private boolean resetPressed;


    public InputController(ActionBindings bindings) {
        inputMap = bindings.getInputMap();
        prevAction = EnumSet.noneOf(Action.class);
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
                if (!prevAction.contains(Action.HOLD_JUMP)) playerAction.add(Action.JUMP);
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
        if (Gdx.input.isKeyPressed(inputMap.get("jump"))){
            if (!prevAction.contains(Action.HOLD_JUMP)) {
                playerAction.add(Action.JUMP);
            }
            playerAction.add(Action.HOLD_JUMP);
        }
        if (Gdx.input.isKeyPressed(inputMap.get("up"))){
            playerAction.add(Action.LOOK_UP);
        }
        if (Gdx.input.isKeyPressed(inputMap.get("down"))){
            playerAction.add(Action.LOOK_DOWN);
        }
        if (Gdx.input.isKeyPressed(inputMap.get("left"))){
            playerAction.add(Action.MOVE_LEFT);
        }
        if (Gdx.input.isKeyPressed(inputMap.get("right"))){
            playerAction.add(Action.MOVE_RIGHT);
        }
        if (Gdx.input.isButtonPressed(inputMap.get("transform"))){
            playerAction.add(Action.TRANSFORM);
        }
        if (Gdx.input.isButtonPressed(inputMap.get("attack"))){
            playerAction.add(Action.ATTACK);
        }
        if (Gdx.input.isButtonPressed(inputMap.get("dash"))){
            playerAction.add(Action.DASH);
        }
        if (Gdx.input.isKeyPressed(inputMap.get("reset"))) {
            playerAction.add(Action.RESET);
            resetPressed = true;
        }

        //Only apply the action on the rising edge.
        if (!prevAction.contains(Action.DEBUG) && Gdx.input.isKeyJustPressed(inputMap.get("debug"))) {
            playerAction.add(Action.DEBUG);
        }
    }

    public boolean didReset() {
        return resetPressed;
    }
}
