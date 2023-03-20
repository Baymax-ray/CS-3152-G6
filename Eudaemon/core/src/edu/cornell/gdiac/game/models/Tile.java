package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import edu.cornell.gdiac.game.obstacle.PolygonObstacle;

public class Tile {
    private TextureRegion texture;

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion newTexture){texture = newTexture;}

    public Tile (AssetDirectory assets) {

        this.texture = new TextureRegion(assets.getEntry("shared:earth", Texture.class));
    }

}
