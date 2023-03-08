package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class Level {

    //#region FINAL FIELDS

    private final Player player;
    private final Enemy[] enemies;

    private final int[][] tilemap; // value represents the index of the tile in the tiles array
    private final Tile[] tiles;

    /**
     * In level coordinates (not pixels), the width and height of a single tile.
     */
    private final float tileSize;

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

    //#region GETTERS & SETTERS

    public float getGravity() {
        return gravity;
    }

    public Enemy[] getEnemies() {
        return enemies;
    }

    //#endregion

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return
     */
    public Tile tileAt(float x, float y) {
        return tiles[tilemap[toTileCoordinates(x)][toTileCoordinates(y)]];
    }

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return whether the point is in an air tile.
     */
    public boolean isAirAt(float x, float y) {
        return tilemap[toTileCoordinates(x)][toTileCoordinates(y)] == 0;
    }


    public int toTileCoordinates(float levelCoordinate) {
        return (int) Math.floor(levelCoordinate / this.tileSize);
    }


    public Level(JsonValue json, AssetDirectory assets) {
        this.player = new Player(json.get("player"), assets);
        throw new UnsupportedOperationException("Not implemented");

    }

}
