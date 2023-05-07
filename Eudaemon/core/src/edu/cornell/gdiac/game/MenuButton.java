package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class MenuButton {

    public Rectangle hitbox;
    public Texture texture;
    public int exitCode;
    public Color tint;
    public int pressState;

    public MenuButton up, down, left, right;

    public MenuButton() {
        this.hitbox = new Rectangle();
        this.tint = Color.WHITE;
        this.pressState = 0;
    }

    public MenuButton(Texture texture, int exitCode) {
        this.hitbox = new Rectangle();
        this.texture = texture;
        this.exitCode = exitCode;
        this.tint = Color.WHITE;
        this.pressState = 0;

    }

    public MenuButton(Texture texture, int exitCode, Color tint, Rectangle hitbox) {
        this.hitbox = hitbox;
        this.texture = texture;
        this.exitCode = exitCode;
        this.tint = tint;
        this.pressState = 0;
    }

}
