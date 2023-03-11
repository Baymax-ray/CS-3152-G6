package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import edu.cornell.gdiac.game.models.Action;
import edu.cornell.gdiac.game.models.ActionBindings;

import java.util.EnumSet;
import java.util.HashMap;

public class InputController {

    private final HashMap<String, Integer> inputMap;

    public InputController(ActionBindings bindings) {
        inputMap = bindings.getInputMap();
    }

    /**
     * Updates the player's actions based on the input from the keyboard or Xbox controller.
     * Returns a new {@link EnumSet} of {@link Action} representing the updated player actions.
     *
     * @param playerAction the current {@link EnumSet} of {@link Action} representing the player's actions
     */
    public void setPlayerAction(EnumSet<Action> playerAction) {
        //TODO: Actually update action thx
        //Clearing action set of previous actions
        playerAction.clear();

        //Checking for jumps
        if (Gdx.input.isKeyPressed(inputMap.get("jump"))){
            playerAction.add(Action.JUMP);
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
    }
}
