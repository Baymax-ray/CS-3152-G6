package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.HashMap;
import java.io.FileReader;

/**
 * This holds the key and mouse button bindings for each Action (read from JSON)
 */
public class ActionBindings {

    private final HashMap<String, Integer> inputMap;

    /**
     * Gets the map of input actions to input keys/buttons.
     *
     * @return the input map
     */
    public HashMap<String, Integer> getInputMap() {
        return inputMap;
    }

    public ActionBindings(JsonValue json) {
        //Initialize map
        inputMap = new HashMap<>();

        try {
            inputMap.put("attack", getInputForButton(json.getString("attack")));
            inputMap.put("dash", getInputForButton(json.getString("dash")));
            inputMap.put("transform", getInputForButton(json.getString("transform")));
            inputMap.put("jump", getInputForKey(json.getString("jump")));
            inputMap.put("up", getInputForKey(json.getString("up")));
            inputMap.put("down", getInputForKey(json.getString("down")));
            inputMap.put("left", getInputForKey(json.getString("left")));
            inputMap.put("right", getInputForKey(json.getString("right")));
            inputMap.put("debug", getInputForKey(json.getString("debug")));
            inputMap.put("reset", getInputForKey(json.getString("reset")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to get the Input.Key for a given key name
    private int getInputForKey(String keyName) {
        try {
//            return Input.Keys.class.getField(keyName).getInt(Input.Keys.class.getField(keyName));
            return Input.Keys.valueOf(keyName);
        } catch (Exception e) {
            e.printStackTrace();
            return Input.Keys.UNKNOWN;
        }
    }

    // Helper method to get the Input.Button for a given button name
    private int getInputForButton(String buttonName) {
        try {
            return Input.Buttons.class.getField(buttonName.toUpperCase()).getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Input.Buttons.LEFT;
        }
    }

}
