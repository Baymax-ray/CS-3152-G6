package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public abstract class MenuSlider extends MenuButton {

    public TextureRegion filled, unfilled;
    public Rectangle unfilledRect;

    public MenuSlider(Texture filled, Texture unfilled, Texture toggle) {
        super(new TextureRegion(toggle));
        this.unfilled = new TextureRegion(unfilled);
        this.filled = new TextureRegion(filled, 0, 0, getValue(), 1);
        this.unfilledRect = new Rectangle();
    }

    public abstract float getValue();
    protected abstract void updateValue(float value);

    public void setValue(float value) {
        if (value < 0) value = 0;
        if (value > 1) value = 1;
        updateValue(value);
        filled.setU2(value);
        hitbox.setCenter(unfilledRect.x + unfilledRect.width * value, unfilledRect.y + unfilledRect.height/2);
    }

    public void draw(GameCanvas canvas) {
        float filledOffset = unfilledRect.height / 6.0f;

        canvas.draw(filled, Color.WHITE, unfilledRect.x + filledOffset, unfilledRect.y, unfilledRect.width * getValue() - filledOffset, unfilledRect.height);
        canvas.draw(unfilled, Color.WHITE, unfilledRect.x, unfilledRect.y, unfilledRect.width, unfilledRect.height);
        canvas.draw(texture, tint, hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }
}
