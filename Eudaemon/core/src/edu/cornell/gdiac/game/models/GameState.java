package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Json;
import edu.cornell.gdiac.assets.AssetDirectory;

public class GameState {
    private final AssetDirectory assets;
    private final ActionBindings bindings;
    //other global settings go here
    private final Tile[] tiles;
    private final Level[] levels;
    private final int currentLevelId;

    public Level getCurrentLevel() {
        return levels[currentLevelId];
    }

    public void resetCurrentLevel() {
        JsonValue constants = assets.getEntry("constants", JsonValue.class);
        this.levels[currentLevelId] = new Level(constants.get("levels").get(currentLevelId), tiles, assets);
    }

    public Level getLevel(int i) {
        return levels[i];
    }

    public ActionBindings getActionBindings() {
        return bindings;
    }

    public void dispose() {
        //assets disposed by root
        //bindings don't need to be disposed
        // tiles don't need to be disposed
        for (Level level : levels) {
            level.dispose();
        }
    }

    public GameState(AssetDirectory assets) {
        this.currentLevelId = 0;

        this.assets = assets;

        JsonValue constants = assets.getEntry("constants",  JsonValue.class);

        bindings = new ActionBindings(assets.getEntry("inputMappings", JsonValue.class));

        String levelName = constants.get("levels").get(0).getString("level");
        JsonValue levelJson = assets.getEntry(levelName,  JsonValue.class);
//        int numTiles = levelJson.getInt("height") * levelJson.getInt("width") ; // bugging for height
        int numTiles = 20 * levelJson.getInt("width") ;
        tiles = new Tile[numTiles];
        for (int i = 0; i < numTiles; i++) {
            tiles[i] = new Tile(assets);
        }

        int numLevels = constants.getInt("numLevels");
        this.levels = new Level[numLevels];
        for (int i = 0; i < numLevels; i++) {
            levels[i] = new Level(constants.get("levels").get(i), tiles, assets);
        }

        //TODO: bindings etc.

    }
}
