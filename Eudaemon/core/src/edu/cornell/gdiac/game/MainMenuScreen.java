
/*
 * LoadingScreen.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do
 * anything until loading is complete. You know those loading screens with the inane tips
 * that want to be helpful?  That is asynchronous loading.
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class MainMenuScreen implements Screen, InputProcessor, ControllerListener {

    /** Background texture for start-up */
    private final Texture background;
    /** Title texture for start-up */
    private final Texture title;
    /** Play button to display when done */
    private Texture playButton;
    /** settings button to display when done */
    private Texture settingsButton;
    /** level select button to display when done */
    private Texture levelSelectButton;

    /** Standard window size (for scaling) */
    private static final int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static final int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    /** Height of the progress bar */
    private static final float BUTTON_SCALE  = 0.20f;
    /** Height of the title */
    private static final float TITLE_SCALE = 1.3f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the play button */
    private int playButtonState;
    /** The hitbox of the start button */
    private Rectangle playButtonHitbox;
    /** The current state of the level select button */
    private int levelSelectButtonState;
    /** The hitbox of the level select button */
    private Rectangle levelSelectButtonHitbox;
    /** The current state of the settings button */
    private int settingsButtonState;
    /** The hitbox of the settings button */
    private Rectangle settingsButtonHitbox;


    /** Whether or not this player mode is still active */
    private boolean active;

    /** The state of the escape key */
    private int escapePressState;

    /**
     * Creates a LoadingScreen with the default budget, size and position.
     *
     * @param assets  	The asset directory for the game
     * @param canvas 	The game canvas to draw to
     */
    public MainMenuScreen(AssetDirectory assets, GameCanvas canvas) {
        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());


        // Load the next two images immediately.
        background = assets.getEntry( "mainMenu:background", Texture.class );
        title = assets.getEntry("mainMenu:title", Texture.class);
        background.setFilter( TextureFilter.Linear, TextureFilter.Linear );

        playButton = assets.getEntry("mainMenu:start",Texture.class);
        playButtonHitbox = new Rectangle();

        settingsButton = assets.getEntry("mainMenu:settings", Texture.class);
        settingsButtonHitbox = new Rectangle();

        levelSelectButton = assets.getEntry("mainMenu:levelSelect", Texture.class);
        levelSelectButtonHitbox = new Rectangle();

        resize(canvas.getWidth(), canvas.getHeight());

        playButtonState = 0;
        levelSelectButtonState = 0;
        settingsButtonState = 0;

        Gdx.input.setInputProcessor( this );

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener( this );
        }

        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        canvas = null;
        listener = null;
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {

    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.setOverlayCamera();
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.draw(title, Color.WHITE, title.getWidth()/2f, title.getHeight()/2f, canvas.getWidth()*0.5f, canvas.getHeight()*0.8f, 0, scale*TITLE_SCALE, scale*TITLE_SCALE);

        Color tint = (playButtonState == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, tint, playButtonHitbox.x, playButtonHitbox.y, playButtonHitbox.width, playButtonHitbox.height);

        tint = (settingsButtonState == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(settingsButton, tint, settingsButtonHitbox.x, settingsButtonHitbox.y, settingsButtonHitbox.width, settingsButtonHitbox.height);

        tint = (levelSelectButtonState == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(levelSelectButton, tint, levelSelectButtonHitbox.x, levelSelectButtonHitbox.y, levelSelectButtonHitbox.width, levelSelectButtonHitbox.height);

        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are ready, notify our listener
            if (listener != null) {
                if (playButtonState == 2) {
                    listener.exitScreen(this, ExitCode.START);
                } else if (levelSelectButtonState == 2) {
                    listener.exitScreen(this, ExitCode.LEVEL_SELECT);
                } else if (escapePressState == 2) {
                    listener.exitScreen(this, ExitCode.QUIT);
                }
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        int progressBarCenterY = (int)(0.25*height);

        heightY = height;

        if (playButton != null) {
            float buttonSpacing = 0.25f;

            playButtonHitbox.setSize(BUTTON_SCALE*scale*playButton.getWidth(), BUTTON_SCALE*scale*playButton.getHeight());
            playButtonHitbox.setCenter(canvas.getWidth()/2.0f, progressBarCenterY + playButton.getHeight()*buttonSpacing*scale);
            float buttonDelta = playButtonHitbox.height * 1.5f;

            settingsButtonHitbox.setSize(BUTTON_SCALE*scale*settingsButton.getWidth(), BUTTON_SCALE*scale*settingsButton.getHeight());
            settingsButtonHitbox.setCenter(canvas.getWidth()/2.0f, progressBarCenterY -settingsButton.getHeight()*buttonSpacing*scale);

            levelSelectButtonHitbox.setSize(BUTTON_SCALE*scale*levelSelectButton.getWidth(), BUTTON_SCALE*scale*levelSelectButton.getHeight());
            levelSelectButtonHitbox.setCenter(canvas.getWidth()/2.0f, progressBarCenterY);
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    public void reset() {
        this.playButtonState = 0;
        this.levelSelectButtonState = 0;
        this.settingsButtonState = 0;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playButton == null || playButtonState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // TODO: Fix scaling
        // Play button is a circle.

        if (playButtonHitbox.contains(screenX, screenY)) {
            playButtonState = 1;
        }

        if (settingsButtonHitbox.contains(screenX, screenY)) {
            settingsButtonState = 1;
        }

        if (levelSelectButtonHitbox.contains(screenX, screenY)) {
            levelSelectButtonState = 1;
        }

        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (playButtonState == 1) {
            playButtonState = 2;
            return false;
        }
        if (settingsButtonState == 1) {
            settingsButtonState = 2;
        }
        if (levelSelectButtonState == 1) {
            levelSelectButtonState = 2;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (playButtonState == 0) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                playButtonState = 1;
                return false;
            }
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (playButtonState == 1) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                playButtonState = 2;
                return false;
            }
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            escapePressState = 1;
            return false;
        }
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            escapePressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

}