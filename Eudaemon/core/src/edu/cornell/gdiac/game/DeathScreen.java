package edu.cornell.gdiac.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class DeathScreen extends Actor {

    private BitmapFont font;
    private String message;
    private float timeRemaining;

    public DeathScreen(BitmapFont font, String message, float timeToShow){
        this.font = font;
        this.message = message;
        this.timeRemaining = timeToShow;
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        font.draw(batch, message, getX(), getY());
    }

    @Override
    public void act(float delta){
        super.act(delta);
        timeRemaining -= delta;
        if(timeRemaining <= 0){
            this.remove();
        }
    }

}
