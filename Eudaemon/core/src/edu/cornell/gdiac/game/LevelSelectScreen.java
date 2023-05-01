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
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.GameState;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;

import java.util.Arrays;

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
public class LevelSelectScreen implements Screen, InputProcessor, ControllerListener {

    /** Background texture for start-up */
    private Texture background;

    /** texture with lights for level completion */
    private Texture lights;

    /** regions of the lights currently being drawn */
    private TextureRegion[] litUpLevels;

    /** back button texture*/
    private Texture backButton;

    /** The hitbox for the back button */
    private Rectangle backHitbox;

    /** The current state of the back button */
    private int backPressState;

    /** The hitboxes for the level select buttons */
    private Rectangle[] levelHitboxes;

    /** The current state of the level buttons */
    private int[] levelPressState;

    /** The level number textures */
    private Texture[] levelNumbers;

    /** Standard window size (for scaling) */
    private static final int STANDARD_WIDTH  = 240;
    /** Standard window height (for scaling) */
    private static final int STANDARD_HEIGHT = 135;


    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;


    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** the current level (1-based indexing)*/
    private int numAvailableLevels;

    /** the selected level (0-based indexing) */
    private int selectedLevel;


    /** Whether or not this player mode is still active */
    private boolean active;

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean restartIsReady() {
        return backPressState == 2;
    }

    /**
     * Creates a LoadingScreen with the default budget, size and position.
     *
     * @param assets  	The asset directory to get assets from
     * @param canvas 	The game canvas to draw to
     */
    public LevelSelectScreen(AssetDirectory assets, GameState state, GameCanvas canvas) {
        this.canvas  = canvas;


        background = assets.getEntry( "levelSelect:background", Texture.class );
        background.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        lights = assets.getEntry("levelSelect:lights", Texture.class);
        lights.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        numAvailableLevels = state.getLevels().size();
        setLitUpLevels(numAvailableLevels);

        backButton = assets.getEntry("levelSelect:back", Texture.class);
        backButton.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        backHitbox = new Rectangle();
        backPressState = 0;

        levelHitboxes = new Rectangle[10];
        levelPressState = new int[levelHitboxes.length];
        for (int i = 0; i < levelHitboxes.length; i++) {
            levelHitboxes[i] = new Rectangle();
            levelPressState[i] = 0;
        }
        levelNumbers = new Texture[10];
        for (int i = 0; i < levelNumbers.length; i++) {
            levelNumbers[i] = assets.getEntry("levelSelect:" + (i + 1), Texture.class);
            levelNumbers[i].setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        }

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

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
        Color tint = (backPressState == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(backButton, tint, 0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < litUpLevels.length; i++) {
            canvas.draw(litUpLevels[i], Color.WHITE, 0, canvas.getHeight() - litUpLevels[i].getRegionHeight() * scale, litUpLevels[i].getRegionWidth() * scale, litUpLevels[i].getRegionHeight() * scale);
        }

        canvas.draw(levelNumbers[numAvailableLevels - 1], Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());

//        for (int i = 0; i < levelHitboxes.length; i++) {
//            canvas.draw(background, tint, levelHitboxes[i].x, levelHitboxes[i].y, levelHitboxes[i].width, levelHitboxes[i].height); //for debugging hitboxes
//        }

        canvas.end();
    }

    private void setLitUpLevels(int i) {

        int splitX = 106; // this is the coordinate in the texture that splits the two sides of the building
        int[] splitY = new int[] {40, 55, 70, 85, 100, 115, lights.getHeight()}; // <-- may need to reverse

//        for (int j = 0; j < splitY.length; j++) {
//            splitY[j] = lights.getHeight() - splitY[j];
//        }

        if (i <= 0) {
            litUpLevels = new TextureRegion[0];
        } else if (i <= 2) {
            litUpLevels = new TextureRegion[1];
            int x = (i % 2 == 0) ? lights.getWidth() : splitX;
            litUpLevels[0] = new TextureRegion(lights, 0, 0, x, splitY[0]);
        } else if (i <= 4) {
            litUpLevels = new TextureRegion[2];
            litUpLevels[0] = new TextureRegion(lights, 0, 0, lights.getWidth(), splitY[0]);
            int x = (i % 2 == 0) ? lights.getWidth() : splitX;
            litUpLevels[1] = new TextureRegion(lights, 0, 0, x, splitY[1]);
        } else if (i <= 6) {
            litUpLevels = new TextureRegion[2];
            litUpLevels[0] = new TextureRegion(lights, 0, 0, lights.getWidth(), splitY[1]);
            int x = (i % 2 == 0) ? lights.getWidth() : splitX;
            litUpLevels[1] = new TextureRegion(lights, 0, 0, x, splitY[2]);
        } else {
            litUpLevels = new TextureRegion[1];
            int y = splitY[i - 4]; //7 -> 85 -> [3]
//            System.out.println(y);
//            System.out.println(lights.getHeight());
            litUpLevels[0] = new TextureRegion(lights, 0, 0, lights.getWidth(), y);
        }
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
            draw();

            // We are are ready, notify our listener
            if (backPressState == 2 && listener != null) {
                listener.exitScreen(this, ExitCode.MAIN_MENU);
            }
            for (int i = 0; i < levelPressState.length; i++) {
                if (levelPressState[i] == 2) {
                    selectedLevel = i;
                    listener.exitScreen(this, ExitCode.START);
                }
            }
        }
    }


    /**
     * Resets the screen so it can be reused
     */
    public void reset() {
        this.backPressState = 0;
        this.selectedLevel = -1;
        Arrays.fill(levelPressState, 0);
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

        centerY = height/2;
        centerX = width/2;
        heightY = height;

        float buttonSpacing = 0.25f;
        float buttonHeight = 0.1f;

        backHitbox.setSize(10 * scale);
        backHitbox.setPosition(5 * scale, canvas.getHeight() - 12 * scale);

        for (int i = 0; i < levelHitboxes.length; i++) {
            levelHitboxes[i].setSize(9 * scale, 5 * scale);
            levelHitboxes[i].setPosition(getLevelHitboxPosition(i + 1));
        }
    }

    private Vector2 getLevelHitboxPosition(int i) { // uses 1 based indexing
        float x, y;
        x = (i % 2 == 0 && i <= 6) ? 109 : 94;

        if (i <=2) {
            y = lights.getHeight() - 36;
        } else if (i <= 4) {
            y = lights.getHeight() - 51;
        } else if (i <= 6) {
            y = lights.getHeight() - 66;
        } else {
            y = lights.getHeight() - (66 + (i - 6) * 15);
        }

        System.out.print(x);
        System.out.print(" ");
        System.out.println(y);
        return new Vector2(x * scale, y * scale);
    }

    public int getSelectedLevel() {
        return selectedLevel;
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
        screenY = heightY-screenY;

        if (backHitbox.contains(screenX, screenY)) {
            backPressState = 1;
        }

        for (int i = 0, levelHitboxesLength = levelHitboxes.length; i < levelHitboxesLength; i++) {
            Rectangle levelHitbox = levelHitboxes[i];
            if (levelHitbox.contains(screenX, screenY)) {
                if (i < numAvailableLevels) {
                    System.out.println(i);
                    levelPressState[i] = 1;
                }
            }
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
        if (backPressState == 1) {
            backPressState = 2;
            return false;
        }
        for (int i = 0; i < levelPressState.length; i++) {
            if (levelPressState[i] == 1) {
                levelPressState[i] = 2;
                return false;
            }
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
        if (backPressState == 0) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                backPressState = 1;
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
        if (backPressState == 1) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                backPressState = 2;
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
