package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class MenuButton {

    public Rectangle hitbox;
    public TextureRegion texture;
    public int exitCode;
    public Color tint;
    public int pressState;

    public MenuButton up, down, left, right;

    public MenuButton(TextureRegion texture) {
        this(texture, -1);
    }

    public MenuButton(TextureRegion texture, int exitCode) {
        this.hitbox = new Rectangle();
        this.texture = texture;
        this.exitCode = exitCode;
        this.tint = Color.WHITE;
        this.pressState = 0;
    }
}
