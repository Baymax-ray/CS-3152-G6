package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import edu.cornell.gdiac.game.SettingsObserver;

public class Settings {

    public static final int NORMAL_DIFFICULTY = 0;
    public static final int HARD_DIFFICULTY = 1;
    public static final int VETERAN_DIFFICULTY = 2;
    private float masterVolume;
    private float musicVolume;
    private float sfxVolume;
    private float brightness;
    private transient boolean fullscreen;
    private transient boolean screenShake;
    private int windowedWidth;
    private int windowedHeight;
    private int levelDifficulty;
    public boolean settingsChanged;
    private int numLevelsAvailable;
    private boolean useArrowKeys;
    private String customJumpKey;
    private String customDashKey;
    private String customAttackKey;
    private String customTransformKey;
    private String customResetKey;

    private transient Array<SettingsObserver> observers;
    private transient ActionBindings actionBindings;

    public Settings() {
        this.observers = new Array<>();
    }

    public static Settings defaultSettings() {
        Settings s = new Settings();
        s.masterVolume = 1;
        s.musicVolume = 1;
        s.sfxVolume = 1;
        s.brightness = 1;
        s.fullscreen = true;
        s.screenShake = true;
        s.windowedWidth = 800;
        s.windowedHeight = 450;
        s.levelDifficulty = NORMAL_DIFFICULTY;
        s.settingsChanged = false;
        s.numLevelsAvailable = 1;
        s.useArrowKeys = false;
        s.customJumpKey = null;
        s.customDashKey = null;
        s.customAttackKey = null;
        s.customTransformKey = null;
        s.customResetKey = null;

        return s;
    }

    public void save() {
        FileHandle saveFile = Gdx.files.local("eudaemon-save-data.json");
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String saveData = json.toJson(this);
        saveFile.writeString(saveData, false);
    }

    public void setActionBindings(ActionBindings actionBindings) {
        this.actionBindings = actionBindings;
        setUseArrowKeys(useArrowKeys);
        setCustomJumpKey(customJumpKey);
        setCustomDashKey(customDashKey);
        setCustomAttackKey(customAttackKey);
        setCustomTransformKey(customTransformKey);
        setCustomResetKey(customResetKey);
    }

    public ActionBindings getActionBindings() {
        return actionBindings;
    }

    public boolean isNormalDifficulty() {
        return this.levelDifficulty == NORMAL_DIFFICULTY;
    }

    public boolean isHardDifficulty() {
        return this.levelDifficulty == HARD_DIFFICULTY;
    }

    public boolean isVeteranDifficulty() {
        return this.levelDifficulty == VETERAN_DIFFICULTY;
    }

    public void setNormalDifficulty() {
        if (!isNormalDifficulty()) {
            this.levelDifficulty = NORMAL_DIFFICULTY;
            this.settingsChanged = true;
            save();
            observers.forEach(o->o.onDifficultyChange(NORMAL_DIFFICULTY));
        }
    }

    public void setHardDifficulty() {
        if (!isHardDifficulty()) {
            this.levelDifficulty = HARD_DIFFICULTY;
            this.settingsChanged = true;
            save();
            observers.forEach(o->o.onDifficultyChange(HARD_DIFFICULTY));
        }
    }

    public void setVeteranDifficulty() {
        if (!isVeteranDifficulty()) {
            this.levelDifficulty = VETERAN_DIFFICULTY;
            this.settingsChanged = true;
            save();
            observers.forEach(o->o.onDifficultyChange(VETERAN_DIFFICULTY));
        }
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    /**
     * sets the game to fullscreen or windowed mode. does not call any observer functions,
     * resizing should occur in Screen's resize() method
     * @param fullscreen whether to set the game to fullscreen.
     */
    public void setFullscreen(boolean fullscreen) {
        if (isFullscreen() != fullscreen) {
            this.fullscreen = fullscreen;
            save();
            if (fullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
            }
        }
    }

    public boolean isScreenShake() { return this.screenShake; }

    public void setScreenShake (boolean screenShake) {
        if (isScreenShake() != screenShake) {
            this.screenShake = screenShake;
            save();
        }
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        if (getMasterVolume() != masterVolume) {
            this.masterVolume = masterVolume;
            save();
            observers.forEach(o->o.onMasterVolumeChange(masterVolume));
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        if (getMusicVolume() != musicVolume) {
            this.musicVolume = musicVolume;
            save();
            observers.forEach(o->o.onMusicVolumeChange(musicVolume));
        }
    }

    public boolean getSettingsChanged(){return settingsChanged;}
    public void setSettingsChanged(boolean bool){settingsChanged = bool;}

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        if (getSfxVolume() != sfxVolume) {
            this.sfxVolume = sfxVolume;
            save();
            observers.forEach(o->o.onSfxVolumeChange(sfxVolume));
        }
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        if (getBrightness() != brightness) {
            this.brightness = brightness;
            save();
            observers.forEach(o->o.onBrightnessChange(brightness));
        }
    }

    public int getLevelDifficulty() {
        return levelDifficulty;
    }

    public int getNumLevelsAvailable() {
        return numLevelsAvailable;
    }

    public void setNumLevelsAvailable(int numLevelsAvailable) {
        if (getNumLevelsAvailable() != numLevelsAvailable) {
            this.numLevelsAvailable = numLevelsAvailable;
            save();
            observers.forEach(o -> o.onNumLevelsAvailable(numLevelsAvailable));
        }
    }

    public void incrementNumLevelsAvailable() {
        numLevelsAvailable++;
        save();
        observers.forEach(o -> o.onNumLevelsAvailable(numLevelsAvailable));
    }

    public boolean getUseArrowKeys() {
        return useArrowKeys;
    }

    public void setUseArrowKeys(boolean useArrowKeys) {
        this.useArrowKeys = useArrowKeys;
        if (useArrowKeys) {
            if (setArrowKeyBindings()) {
                observers.forEach(o -> o.onUseArrowKeys(true));
            }
        } else {
            if (setWasdBindings()) {
                observers.forEach(o -> o.onUseArrowKeys(false));
            }
        }
        save();
    }

    private boolean setArrowKeyBindings() {
         return actionBindings.addCustomKeyForAction(Action.LOOK_UP, "Up")
                | actionBindings.addCustomKeyForAction(Action.LOOK_DOWN, "Down")
                | actionBindings.addCustomKeyForAction(Action.MOVE_LEFT, "Left")
                | actionBindings.addCustomKeyForAction(Action.MOVE_RIGHT, "Right");
    }

    private boolean setWasdBindings() {
        return actionBindings.addCustomKeyForAction(Action.LOOK_UP, "W")
                | actionBindings.addCustomKeyForAction(Action.LOOK_DOWN, "S")
                | actionBindings.addCustomKeyForAction(Action.MOVE_LEFT, "A")
                | actionBindings.addCustomKeyForAction(Action.MOVE_RIGHT, "D");
    }

    public void setDefault() {
        setUseArrowKeys(false);

        setCustomJumpKey(getDefaultJumpKey());
        setCustomDashKey(getDefaultDashKey());
        setCustomAttackKey(getDefaultAttackKey());
        setCustomTransformKey(getDefaultTransformKey());
        setCustomResetKey(getDefaultResetKey());

        customJumpKey = null;
        customDashKey = null;
        customAttackKey = null;
        customTransformKey = null;
        customResetKey = null;
        save();
    }

    public String getDefaultJumpKey() {
        return actionBindings.getDefaultJump();
    }

    public String getCustomJumpKey() {
        return customJumpKey;
    }

    public void setCustomJumpKey(String customJumpKey) {
        this.customJumpKey = customJumpKey;
        if (this.actionBindings.addCustomKeyForAction(Action.BEGIN_JUMP, customJumpKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.BEGIN_JUMP, customJumpKey));
        }
        if (this.actionBindings.addCustomKeyForAction(Action.HOLD_JUMP, customJumpKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.HOLD_JUMP, customJumpKey));
        }
        save();
    }

    public String getDefaultDashKey() {
        return actionBindings.getDefaultDash();
    }

    public String getCustomDashKey() {
        return customDashKey;
    }

    public void setCustomDashKey(String customDashKey) {
        this.customDashKey = customDashKey;
        if (this.actionBindings.addCustomKeyForAction(Action.DASH, customDashKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.DASH, customDashKey));
        }
        if (this.actionBindings.addCustomKeyForAction(Action.HOLD_DASH, customDashKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.HOLD_DASH, customDashKey));
        }
        save();
    }

    public String getDefaultAttackKey() {
        return actionBindings.getDefaultAttack();
    }

    public String getCustomAttackKey() {
        return customAttackKey;
    }

    public void setCustomAttackKey(String customAttackKey) {
        this.customAttackKey = customAttackKey;
        if (this.actionBindings.addCustomKeyForAction(Action.ATTACK, customAttackKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.ATTACK, customAttackKey));
        }
        save();
    }

    public String getDefaultTransformKey() {
        return actionBindings.getDefaultTransform();
    }

    public String getCustomTransformKey() {
        return customTransformKey;
    }

    public void setCustomTransformKey(String customTransformKey) {
        this.customAttackKey = customAttackKey;
        if (this.actionBindings.addCustomKeyForAction(Action.TRANSFORM, customTransformKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.TRANSFORM, customTransformKey));
        }
        save();
    }

    public String getDefaultResetKey() {
        return actionBindings.getDefaultReset();
    }

    public String getCustomResetKey() {
        return customResetKey;
    }

    public void setCustomResetKey(String customResetKey) {
        this.customResetKey = customResetKey;
        if (this.actionBindings.addCustomKeyForAction(Action.RESET, customResetKey)) {
            observers.forEach(o -> o.onCustomBinding(Action.RESET, customResetKey));
        }
        save();
    }


    public void addObserver(SettingsObserver o) {
        observers.add(o);
    }

    public void removeObserver(SettingsObserver o) {
        observers.removeValue(o, true);
    }

    public void dispose() {
        observers.clear();
    }
}
