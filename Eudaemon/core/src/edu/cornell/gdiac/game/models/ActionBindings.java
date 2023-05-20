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

    private String defaultJump;
    private String defaultDash;
    private String defaultAttack;
    private String defaultTransform;
    private String defaultReset;

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

    public ActionBindings(JsonValue json) {
        //Initialize map
        keyMap = new ObjectMap<>();
        mouseMap = new ObjectMap<>();
        controllerMap = new ObjectMap<>();

        for (Action action : Action.values()) {
            String name = action.name;
            addKeyForAction(action, json);
            addMouseButtonForAction(action, json);
        }

        defaultJump = json.get("keyboard").getString(Action.BEGIN_JUMP.name);
        defaultDash = json.get("keyboard").getString(Action.DASH.name);
        defaultAttack = json.get("keyboard").getString(Action.ATTACK.name);
        defaultTransform = json.get("keyboard").getString(Action.TRANSFORM.name);
        defaultReset = json.get("keyboard").getString(Action.RESET.name);
    }

    public String getDefaultJump() {
        return defaultJump;
    }

    public String getDefaultDash() {
        return defaultDash;
    }

    public String getDefaultAttack() {
        return defaultAttack;
    }

    public String getDefaultTransform() {
        return defaultTransform;
    }

    public String getDefaultReset() {
        return defaultReset;
    }

    /** returns true if the key value has changed */
    public boolean addCustomKeyForAction(Action action, String keyName) {
        try {
            int keyCode = Input.Keys.valueOf(keyName);
            if (keyCode == keyMap.get(action))
                return false;
            keyMap.put(action, keyCode);
            return true;
        } catch (Exception ignore) {
            return false;
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
