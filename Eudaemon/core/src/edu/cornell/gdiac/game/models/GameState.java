package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.HashMap;

public class GameState {
    private final AssetDirectory assets;
    private final ActionBindings bindings;
    //other global settings go here
    private final Tile[] tiles;
    private final HashMap<String, Level> levels;
    private String currentLevelName;

    public Level getCurrentLevel() {
        return levels.get(currentLevelName);
    }

    public void resetCurrentLevel() {
        JsonValue constants = assets.getEntry("constants", JsonValue.class);
        JsonValue currentLevel = null;
        for (JsonValue level : constants.get("levels")) {
            if (level.getString("level").equals(currentLevelName)) {
                currentLevel = level;
            }
        }
        this.levels.get(currentLevelName).dispose();
        this.levels.put(currentLevelName, new Level(currentLevel, tiles, assets));
    }

    public void setCurrentLevel(int i) {
        currentLevelName = assets.getEntry("constants", JsonValue.class).get("levels").get(i).getString("level");
    }

    public void setCurrentLevel(String name) {
        currentLevelName = name;
    }

    public Level getLevel(int i) {
        String levelName = assets.getEntry("constants", JsonValue.class).get("levels").get(i).getString("level");
        return levels.get(levelName);
    }

    public ActionBindings getActionBindings() {
        return bindings;
    }

    public void dispose() {
        //assets disposed by root
        //bindings don't need to be disposed
        // tiles don't need to be disposed
        for (Level level : levels.values()) {
            level.dispose();
        }
    }

    public GameState(AssetDirectory assets) {
        this.currentLevelName = assets.getEntry("constants", JsonValue.class).get("levels").get(0).getString("level");

        this.assets = assets;

        JsonValue constants = assets.getEntry("constants",  JsonValue.class);

        bindings = new ActionBindings(assets.getEntry("inputMappings", JsonValue.class));

        String levelName = constants.get("levels").get(0).getString("level");
        System.out.println(levelName);
        JsonValue levelJson = assets.getEntry(levelName,  JsonValue.class);
//        int numTiles = levelJson.getInt("height") * levelJson.getInt("width") ; // bugging for height
        //System.out.println(levelJson.getInt("tilewidth"));
        int numTiles = 20 * levelJson.getInt("width") ;
        tiles = new Tile[numTiles];
        for (int i = 0; i < numTiles; i++) {
            tiles[i] = new Tile(assets);
        }

        int numLevels = constants.getInt("numLevels");
        this.levels = new HashMap<>();
        for (int i = 0; i < numLevels; i++) {
            levels.put(constants.get(("levels")).get(i).getString("level"), new Level(constants.get("levels").get(i), tiles, assets));
        }

        //TODO: bindings etc.

    }
}
