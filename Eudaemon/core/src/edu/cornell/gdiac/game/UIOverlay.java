package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Action;

public class UIOverlay {
    /** Texture for hearts*/
    private Texture heartRegion;

    /** Texture for empty spirit bar*/
    private Texture emptySpiritBar;
    /** Texture for filled spirit bar*/
    private Texture filledSpiritBar;

    //private GameCanvas canvas;

    private float heartLevel;
    private float spiritLevel;

    public UIOverlay(JsonValue json, AssetDirectory assets, float playerHearts, float playerSpirit){
        this.heartRegion = assets.getEntry(json.getString("heart"), Texture.class);
        this.emptySpiritBar = assets.getEntry(json.getString("emptyspirit"), Texture.class);
//        this.filledSpiritBar = assets.get(json.getString("filledspirit"), Texture.class);
        this.heartLevel = playerHearts;
        this.spiritLevel = playerSpirit;
    }

    public void draw(GameCanvas canvas) {
        float xPos = canvas.getCamera().position.x - 500;
        for(int i=0; i < heartLevel; i++){
            canvas.draw(heartRegion, Color.WHITE, xPos,canvas.getCamera().position.y + 240, 38.4F, 32 );
            xPos += 47;
        }
        canvas.draw(emptySpiritBar, Color.WHITE,canvas.getCamera().position.x - 515, canvas.getCamera().position.y + 210, 271, 25 );
//        float spiritPercentage = spiritLevel / 10F;
//        int barWidth = (int)(filledSpiritBar.getWidth() * spiritPercentage);
//        int barHeight = filledSpiritBar.getHeight();
//        TextureRegion croppedBar = new TextureRegion(filledSpiritBar, 0, 0, barWidth, barHeight);
//        canvas.draw(croppedBar, Color.WHITE, canvas.getCamera().position.x - 515, canvas.getCamera().position.y + 204, 271 * spiritPercentage, 32);
    }
}
