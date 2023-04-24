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
    private final Texture heartRegion;

    /** Texture for filled spirit bar*/
    private final Texture filledSpiritBar;

    /** Texture for outline of bar/hearts*/
    private final Texture uiOutline;

    private final Texture settingsButton;

    public UIOverlay(JsonValue json, AssetDirectory assets){
        this.heartRegion = assets.getEntry(json.getString("heart"), Texture.class);
        this.filledSpiritBar = assets.getEntry(json.getString("filled"), Texture.class);
        this.uiOutline = assets.getEntry(json.getString("uioutline"), Texture.class);
        this.settingsButton = assets.getEntry(json.getString("settings"), Texture.class);
    }

    public void draw(GameCanvas canvas, float playerHearts, float playerSpirit) {
        float xPos = 74.5F;
        canvas.draw(uiOutline, Color.WHITE, 0, canvas.getHeight() - 62, 221, 52);
//        canvas.draw(settingsButton, Color.WHITE, canvas.getWidth()-72, canvas.getHeight()-62, 51, 52);
        for(int i=0; i < playerHearts; i++){
            canvas.draw(heartRegion, Color.WHITE, xPos,canvas.getHeight() - 32, 23, 20);
            xPos += 60.5F;
        }
        float spiritPercentage = playerSpirit / 10F;
        int barWidth = (int)(filledSpiritBar.getWidth() * spiritPercentage);
        int barHeight = filledSpiritBar.getHeight();
        TextureRegion croppedBar = new TextureRegion(filledSpiritBar, 0, 0, barWidth, barHeight);
        canvas.draw(croppedBar, Color.WHITE, 72, canvas.getHeight() - 61, 137 * spiritPercentage, 18.234F);
    }
}
