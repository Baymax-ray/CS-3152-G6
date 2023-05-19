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
    private int windowedWidth;
    private int windowedHeight;
    private int levelDifficulty;
    public boolean settingsChanged;
    private int numLevelsAvailable;

    private transient Array<SettingsObserver> observers;


    public Settings() {
        this.observers = new Array<>();
        this.fullscreen = true;
    }

    public static Settings defaultSettings() {
        Settings s = new Settings();
        s.masterVolume = 1;
        s.musicVolume = 1;
        s.sfxVolume = 1;
        s.brightness = 1;
        s.fullscreen = true;
        s.windowedWidth = 800;
        s.windowedHeight = 450;
        s.levelDifficulty = NORMAL_DIFFICULTY;
        s.settingsChanged = false;
        s.numLevelsAvailable = 1;

        return s;
    }

    public void save() {
        FileHandle saveFile = Gdx.files.local("eudaemon-save-data.json");
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String saveData = json.toJson(this);
        saveFile.writeString(saveData, false);
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
