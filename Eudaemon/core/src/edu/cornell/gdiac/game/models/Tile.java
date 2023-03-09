package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class Tile {
    private final TextureRegion texture;

    public TextureRegion getTexture() {
        return texture;
    }

    public Tile (JsonValue json, AssetDirectory assets) {
        String textureAsset = json.getString("textureAsset");
        this.texture = new TextureRegion(assets.getEntry(textureAsset, Texture.class));
    }

}
