package edu.cornell.gdiac.game;

import com.badlogic.gdx.Screen;

public class DeathScreen implements Screen {

    private boolean active;

    private GameCanvas canvas;

    public DeathScreen(GameCanvas canvas){
        this.canvas = canvas;
    }

    public void draw(float delta) {
        canvas.clear();
    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void render(float delta) {
        if (active) {
            draw(delta);
        }
    }

    @Override
    public void resize(int width, int height) {
        canvas.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {

    }
}
