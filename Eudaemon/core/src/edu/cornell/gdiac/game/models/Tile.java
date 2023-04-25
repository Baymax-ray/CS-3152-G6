package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.assets.AssetDirectory;

public class Tile {
    private TextureRegion texture;

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion newTexture){texture = newTexture;}

    public Tile (AssetDirectory assets) {

        this.texture = new TextureRegion(assets.getEntry("shared:earth", Texture.class));
//        this.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

}
