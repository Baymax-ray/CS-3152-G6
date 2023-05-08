package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.utils.JsonValue;

public class Settings {
    public static final int NORMAL_DIFFICULTY = 0;
    public static final int HARD_DIFFICULTY = 1;
    public static final int VETERAN_DIFFICULTY = 2;
    public float masterVolume;
    public float musicVolume;
    public float sfxVolume;
    public float brightness;
    public boolean fullscreen;
    public int levelDifficulty;
    public int numLevelsAvailable;

    public static Settings defaultSettings() {
        Settings s = new Settings();
        s.masterVolume = 1;
        s.musicVolume = 1;
        s.sfxVolume = 1;
        s.brightness = 1;
        s.fullscreen = true;
        s.levelDifficulty = NORMAL_DIFFICULTY;
        s.numLevelsAvailable = 1;

        return s;
    }

    public Settings() {}
}
