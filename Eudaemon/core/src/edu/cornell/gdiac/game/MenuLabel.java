package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class MenuLabel {

    public Rectangle position;
    public TextureRegion texture;

    public MenuLabel(TextureRegion texture) {
        this.position = new Rectangle();
        this.texture = texture;
    }

}
