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
            System.out.println("jump");
        }
        if (Gdx.input.isKeyPressed(inputMap.get("up"))){
            System.out.println("up");
        }
        if (Gdx.input.isKeyPressed(inputMap.get("down"))){
            System.out.println("down");
        }
        if (Gdx.input.isKeyPressed(inputMap.get("left"))){
            System.out.println("left");
        }
        if (Gdx.input.isKeyPressed(inputMap.get("right"))){
            System.out.println("right");
        }
        if (Gdx.input.isButtonPressed(inputMap.get("transform"))){
            System.out.println("transform");
        }
        if (Gdx.input.isButtonPressed(inputMap.get("attack"))){
            System.out.println("attack");
        }
        if (Gdx.input.isButtonPressed(inputMap.get("dash"))){
            System.out.println("dash");
        }
    }
}
