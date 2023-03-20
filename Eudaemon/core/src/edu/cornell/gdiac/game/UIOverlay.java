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

    /** Texture for filled spirit bar*/
    private Texture filledSpiritBar;

    /** Texture for outline of bar/hearts*/
    private Texture uiOutline;
    /** heartLevel of player*/
    private float heartLevel;
    /** spiritLevel of player*/
    private float spiritLevel;

    public UIOverlay(JsonValue json, AssetDirectory assets, float playerHearts, float playerSpirit){
        this.heartRegion = assets.getEntry(json.getString("heart"), Texture.class);
        this.filledSpiritBar = assets.getEntry(json.getString("filled"), Texture.class);
        this.heartLevel = playerHearts;
        this.spiritLevel = playerSpirit;
        this.uiOutline = assets.getEntry(json.getString("uioutline"), Texture.class);
    }

    public void draw(GameCanvas canvas) {
        float xPos = 69;
        canvas.draw(uiOutline, Color.WHITE, 0, canvas.getHeight() - 62, 221, 52);
        for(int i=0; i < heartLevel; i++){
            canvas.draw(heartRegion, Color.WHITE, xPos,canvas.getHeight() - 32, 23, 20);
            xPos += 31.7F;
        }
//        canvas.draw(filledSpiritBar, Color.WHITE, 71, canvas.getHeight() - 61, 137, 18.234F);
        float spiritPercentage = spiritLevel / 10F;
        int barWidth = (int)(filledSpiritBar.getWidth() * spiritPercentage);
        int barHeight = filledSpiritBar.getHeight();
        TextureRegion croppedBar = new TextureRegion(filledSpiritBar, 0, 0, barWidth, barHeight);
        canvas.draw(croppedBar, Color.WHITE, 71, canvas.getHeight() - 61, 137 * spiritPercentage, 18.234F);
    }
}
