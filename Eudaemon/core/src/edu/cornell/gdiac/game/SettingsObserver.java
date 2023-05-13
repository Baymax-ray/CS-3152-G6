package edu.cornell.gdiac.game;

public interface SettingsObserver {
    default void onDifficultyChange(int newDifficulty) {}
    default void onMasterVolumeChange(float newVolume) {}
    default void onMusicVolumeChange(float newVolume) {}
    default void onSfxVolumeChange(float newVolume) {}
    default void onBrightnessChange(float newVolume) {}
    default void onNumLevelsAvailable(float newNumLevels) {}
}