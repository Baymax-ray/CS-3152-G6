package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.Collection;
import java.util.HashMap;

public class GameState {
    private final AssetDirectory assets;
    private final ActionBindings bindings;
    private Settings settings;
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
        this.levels.put(currentLevelName, new Level(currentLevel.getString("level"), tiles, assets));
    }

    public void setCurrentLevel(int i) {
        JsonValue levels = assets.getEntry("constants", JsonValue.class).get("levels");
        if (i < 0) i = 0;
        if (i >= levels.size()) i = levels.size() - 1;
        currentLevelName = assets.getEntry("constants", JsonValue.class).get("levels").get(i).getString("level");
    }

    public void setCurrentLevel(String name) {
        currentLevelName = name;
    }

    public Level getLevel(int i) {
        String levelName = assets.getEntry("constants", JsonValue.class).get("levels").get(i).getString("level");
        return levels.get(levelName);
    }

    public Collection<Level> getLevels() {
        return levels.values();
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

    public Settings getSettings() {
        return settings;
    }

    public void unlockNextLevel() {
        if (Character.getNumericValue(currentLevelName.charAt(currentLevelName.length() - 1)) == settings.numLevelsAvailable - 1 && settings.numLevelsAvailable < levels.size() - 1)
            settings.numLevelsAvailable++;
    }

    public GameState(AssetDirectory assets) {
        this.currentLevelName = assets.getEntry("constants", JsonValue.class).get("levels").get(0).getString("level");

        this.assets = assets;

        JsonValue constants = assets.getEntry("constants",  JsonValue.class);

        bindings = new ActionBindings(assets.getEntry("inputMappings", JsonValue.class));

        try {
            FileHandle saveFile = Gdx.files.local("eudaemon-save-data.json");
            Json json = new Json();
            System.out.println(saveFile.readString());
            settings = json.fromJson(Settings.class, saveFile.readString());
            System.out.println("here");
        } catch (Exception e) {
            settings = Settings.defaultSettings();
            save();
        }

        String levelName = constants.get("levels").get(0).getString("level");
//        System.out.println(levelName);
        JsonValue levelJson = assets.getEntry(levelName,  JsonValue.class);
//        int numTiles = levelJson.getInt("height") * levelJson.getInt("width") ; // bugging for height
        //System.out.println(levelJson.getInt("tilewidth"));
        int numTiles = 20 * levelJson.getInt("width") ;
        tiles = new Tile[numTiles];
        for (int i = 0; i < numTiles; i++) {
            tiles[i] = new Tile(assets);
        }

        int numLevels = constants.get("levels").size();
        this.levels = new HashMap<>();
        for (int i = 0; i < numLevels; i++) {
            levels.put(constants.get(("levels")).get(i).getString("level"), new Level(constants.get("levels").get(i).getString("level"), tiles, assets));
        }

        //TODO: bindings etc.

    }

    public void save() {
        FileHandle saveFile = Gdx.files.local("eudaemon-save-data.json");
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String saveData = json.toJson(settings);
        saveFile.writeString(saveData, false);
    }
}
