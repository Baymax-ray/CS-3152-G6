package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.Action;

public interface SettingsObserver {
    default void onDifficultyChange(int newDifficulty) {}
    default void onMasterVolumeChange(float newVolume) {}
    default void onMusicVolumeChange(float newVolume) {}
    default void onSfxVolumeChange(float newVolume) {}
    default void onBrightnessChange(float newVolume) {}
    default void onNumLevelsAvailable(float newNumLevels) {}
    default void onUseArrowKeys(boolean useArrowKeys) {}
    default void onDefault() {}
    default void onCustomBinding(Action action, String binding) {}
}