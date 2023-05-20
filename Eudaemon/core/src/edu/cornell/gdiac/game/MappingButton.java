package edu.cornell.gdiac.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Locale;

public class MappingButton extends MenuButton {

    public MappingButton(FontTextureLoader fontTextureLoader, BitmapFont font, String binding) {
        super(new TextureRegion(fontTextureLoader.createFontTexture(font, binding.toUpperCase(Locale.ROOT))));
    }

    // button needs to be resized
    public void updateTexture(FontTextureLoader fontTextureLoader, BitmapFont font, String newBinding) {
        if (texture != null) {
            fontTextureLoader.disposeTexture(texture.getTexture());
        }

        texture = new TextureRegion(fontTextureLoader.createFontTexture(font, newBinding.toUpperCase(Locale.ROOT)));
    }
}
