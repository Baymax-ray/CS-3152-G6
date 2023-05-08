package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Action;
import edu.cornell.gdiac.game.models.Level;

public class UIOverlay {
    /** Texture for hearts*/
    private final Texture heartRegion;

    /** Texture for filled spirit bar*/
    private final Texture filledSpiritBar;

    /** Texture for outline of bar/hearts*/
    private final Texture vetUI;
    private final Texture hardUI;
    private final Texture normalUI;

    private final Texture settingsButton;

    private final float numHearts;

    public UIOverlay(JsonValue json, AssetDirectory assets, Level level){
        this.heartRegion = assets.getEntry(json.getString("heart"), Texture.class);
        this.filledSpiritBar = assets.getEntry(json.getString("filled"), Texture.class);
        this.settingsButton = assets.getEntry(json.getString("settings"), Texture.class);
        this.numHearts = level.getLevelDifficulty();
        this.vetUI = assets.getEntry(json.getString("vetUI"), Texture.class);
        this.hardUI = assets.getEntry(json.getString("hardUI"), Texture.class);
        this.normalUI = assets.getEntry(json.getString("normalUI"), Texture.class);


    }

    public void draw(GameCanvas canvas, float playerSpirit, float playerHearts) {
//        float xPos = 74.5F;
        float xPos = 0.05173611111f*canvas.getWidth();
        if(playerHearts == 3){
            canvas.draw(vetUI, Color.WHITE, 0, canvas.getHeight()*0.927f, canvas.getWidth()*0.1534722222f, canvas.getHeight()*0.06f);
        }
        else if(playerHearts == 4){
            canvas.draw(hardUI, Color.WHITE, 0, canvas.getHeight()*0.927f, canvas.getWidth()*0.1534722222f, canvas.getHeight()*0.06f);
        }
        else{
            canvas.draw(normalUI, Color.WHITE, 0, canvas.getHeight()*0.927f, canvas.getWidth()*0.1534722222f, canvas.getHeight()*0.06f);
        }

        for(int i=0; i < playerHearts; i++){
            canvas.draw(heartRegion, Color.WHITE, xPos,canvas.getHeight()*0.962f, 0.016f*canvas.getWidth(), 0.024f*canvas.getHeight());
            xPos += 0.042f*canvas.getWidth();
        }
        float spiritPercentage = playerSpirit / 10F;
        int barWidth = (int)(filledSpiritBar.getWidth() * spiritPercentage);
        int barHeight = filledSpiritBar.getHeight();
        TextureRegion croppedBar = new TextureRegion(filledSpiritBar, 0, 0, barWidth, barHeight);
        canvas.draw(croppedBar, Color.WHITE, 0.05f* canvas.getWidth(), 0.928f*canvas.getHeight(), canvas.getWidth()*0.095f * spiritPercentage, 0.022f*canvas.getHeight());
    }
}
