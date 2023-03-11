package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Json;
import edu.cornell.gdiac.assets.AssetDirectory;

public class GameState {
    private ActionBindings bindings;
    //other global settings go here
    private Tile[] tiles;
    private Level[] levels;

    public Level getLevel(int i) {
        return levels[i];
    }

    public ActionBindings getActionBindings() {
        return bindings;
    }

    public GameState(AssetDirectory assets) {

        JsonValue json = assets.getEntry("constants",  JsonValue.class);

        bindings = new ActionBindings(assets.getEntry("inputMappings", JsonValue.class));

        int numTiles = json.getInt("numTiles");
        tiles = new Tile[numTiles];
        for (int i = 0; i < numTiles; i++) {
            tiles[i] = new Tile(json.get("tiles").get(i), assets);
        }

        int numLevels = json.getInt("numLevels");
        this.levels = new Level[numLevels];
        for (int i = 0; i < numLevels; i++) {
            levels[i] = new Level(json.get("levels").get(i), tiles, assets);
        }

        //TODO: bindings etc.
    }
}
