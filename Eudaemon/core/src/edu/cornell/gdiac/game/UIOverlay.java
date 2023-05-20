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

    private final Level level;

    public UIOverlay(JsonValue json, AssetDirectory assets, Level level){
        this.heartRegion = assets.getEntry(json.getString("heart"), Texture.class);
        heartRegion.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        this.filledSpiritBar = assets.getEntry(json.getString("filled"), Texture.class);
        filledSpiritBar.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        this.settingsButton = assets.getEntry(json.getString("settings"), Texture.class);
        settingsButton.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        this.level = level;
        this.numHearts = level.getLevelDifficulty();
        this.vetUI = assets.getEntry(json.getString("vetUI"), Texture.class);
        vetUI.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        this.hardUI = assets.getEntry(json.getString("hardUI"), Texture.class);
        hardUI.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        this.normalUI = assets.getEntry(json.getString("normalUI"), Texture.class);
        normalUI.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );


    }

    public void draw(GameCanvas canvas, float playerSpirit, float playerHearts) {
        float heart_xPos = 0.0422f*canvas.getWidth();
        if(level.getLevelDifficulty() == 3){
            canvas.draw(vetUI, Color.WHITE, 8f, canvas.getHeight()*0.875f, canvas.getWidth()*0.15469f*1.5f, canvas.getHeight()*0.07578f*1.5f);
            for(int i=0; i < playerHearts; i++){
                canvas.draw(heartRegion, Color.WHITE, heart_xPos,canvas.getHeight()*0.948f, 0.016f*canvas.getWidth()*1.5f, 0.024f*canvas.getHeight()*1.5f);
                heart_xPos += 0.0422f*canvas.getWidth();
            }
        }
        else if(level.getLevelDifficulty() == 4){
            canvas.draw(hardUI, Color.WHITE, 8f, canvas.getHeight()*0.875f, canvas.getWidth()*0.15469f*1.5f, canvas.getHeight()*0.07578f*1.5f);
            for(int i=0; i < playerHearts; i++){
                canvas.draw(heartRegion, Color.WHITE, heart_xPos,canvas.getHeight()*0.948f, 0.016f*canvas.getWidth()*1.5f, 0.024f*canvas.getHeight()*1.5f);
                heart_xPos += 0.0422f*canvas.getWidth();
            }
        }
        else{
            canvas.draw(normalUI, Color.WHITE, 30f, canvas.getHeight()*0.845f, canvas.getWidth()*0.15469f*1.5f, canvas.getHeight()*0.07578f*1.5f);
            for(int i=0; i < playerHearts; i++){
                canvas.draw(heartRegion, Color.WHITE, heart_xPos,canvas.getHeight()*0.918f, 0.016f*canvas.getWidth()*1.5f, 0.024f*canvas.getHeight()*1.5f);
                heart_xPos += 0.0422f*canvas.getWidth();
            }
        }
        float spiritPercentage = playerSpirit / 10F;
        int barWidth = (int)(filledSpiritBar.getWidth() * spiritPercentage);
        int barHeight = filledSpiritBar.getHeight();
        TextureRegion croppedBar = new TextureRegion(filledSpiritBar, 0, 0, barWidth, barHeight);
        canvas.draw(croppedBar, Color.WHITE, 0.015f* canvas.getWidth(), 0.840f*canvas.getHeight(), canvas.getWidth()*0.156f * spiritPercentage*1.5f, 0.1f*canvas.getHeight()*1.5f);
    }
}
