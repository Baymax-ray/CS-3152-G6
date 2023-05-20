package edu.cornell.gdiac.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;


/** FontTextureLoader is a class that provides static methods to generated textures from text */
public class FontTextureLoader {

    private Array<FrameBuffer> frameBuffers;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;

    public FontTextureLoader() {
        this.frameBuffers = new Array<>();
        this.camera = new OrthographicCamera();
        this.spriteBatch = new SpriteBatch();
    }

    public void dispose() {
        for (FrameBuffer frameBuffer : frameBuffers) {
            frameBuffer.dispose();
        }
        frameBuffers.clear();
    }

    /** create and return a texture from a string */
    public Texture createTexture(BitmapFont font, String text) {
        GlyphLayout layout = new GlyphLayout(font,text);

        int width = (int) layout.width;
        int height = (int) layout.height;

        FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        camera.setToOrtho(true, width, height);
        spriteBatch.getProjectionMatrix().set(camera.combined);

        frameBuffer.begin();
        spriteBatch.begin();
        font.draw(spriteBatch, layout, 0, height);
        spriteBatch.end();
        frameBuffer.end();

        frameBuffers.add(frameBuffer);

        Texture tex = frameBuffer.getColorBufferTexture();
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        return frameBuffer.getColorBufferTexture();
    }

    public void disposeTexture(Texture texture) {
        Array.ArrayIterator<FrameBuffer> iter = frameBuffers.iterator();
        while (iter.hasNext()) {
            if (iter.next().getColorBufferTexture() == texture) {
                iter.remove();
            }
        }
    }
}