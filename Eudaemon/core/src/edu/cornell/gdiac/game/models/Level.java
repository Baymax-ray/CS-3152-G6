package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.utils.JsonValue;

public class Level {

    //#region FINAL FIELDS

    private final Player player;
    private final Enemy[] enemies;

    private final float levelWidth;
    private final float levelHeight;

    /**
     * In level coordinates, the width of the region the camera displays
     */
    private final float cameraWidth;

    /**
     * In level coordinates, the height of the region the camera displays
     */
    private final float cameraHeight;

    private final float gravity;

    //#endregion


    //#region NONFINAL FIELDS

    /**
     * Whether this level has been completed
     */
    private boolean isCompleted;

    /**
     * The x position of the camera in level coordinates
     */
    private float cameraX;

    /**
     * The y position of the camera in level coordinates
     */
    private float cameraY;

    //#endregion



    public Level(JsonValue json) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
