package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * This holds the key and mouse button bindings for each Action (read from JSON)
 */
public class ActionBindings {

    private final ObjectMap<Action, Integer> keyMap;
    private final ObjectMap<Action, Integer> mouseMap;
    private final ObjectMap<Action, Integer> controllerMap;

    /**
     * Gets the map of input actions to input keys/buttons.
     *
     * @return the input map
     */
    public ObjectMap<Action, Integer> getKeyMap() {
        return keyMap;
    }

    public ObjectMap<Action, Integer> getMouseMap() {
        return mouseMap;
    }

    public ObjectMap<Action, Integer> getControllerMap() {
        return controllerMap;
    }

    public ActionBindings(JsonValue json) {
        //Initialize map
        keyMap = new ObjectMap<>();
        mouseMap = new ObjectMap<>();
        controllerMap = new ObjectMap<>();

        for (Action action : Action.values()) {
            String name = action.name;
            addKeyForAction(action, json);
            addMouseButtonForAction(action, json);
//            controllerMap.put(action, getInputForKey(name, json)); // TODO: currently gets key name;
        }
    }

    // Helper method to get the Input.Key for a given key name
    private void addKeyForAction(Action action, JsonValue json) {
        try {
            String keyName = json.get("keyboard").getString(action.name);
            int keyCode = Input.Keys.valueOf(keyName);
            keyMap.put(action, keyCode);
        } catch (Exception ignore) {}
    }

    // Helper method to get the Input.Button for a given button name
    private void addMouseButtonForAction(Action action, JsonValue json) {
        try {
            String buttonName = json.get("mouse").getString(action.name);
            int buttonCode = Input.Buttons.class.getField(buttonName.toUpperCase()).getInt(null);
            mouseMap.put(action, buttonCode);
        } catch (Exception ignore) {}
    }

}
