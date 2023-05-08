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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.GameState;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;

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

    public static final int LEVEL_BUTTON_START_X = 94;
    public static final int LEVEL_BUTTON_START_Y = 9;
    public static final int LEVEL_BUTTON_WIDTH = 9;
    public static final int LEVEL_BUTTON_WIDTH_2 = 7;
    public static final int LEVEL_BUTTON_HEIGHT = 5;
    public static final int LEVEL_BUTTON_OFFSET_X = 16;
    public static final int LEVEL_BUTTON_OFFSET_Y = 15;

    /** Background texture for start-up */
    private Texture background;

    /** texture with lights for level completion */
    private Texture lights;

    private Array<MenuButton> levelButtons;

    /** this array includes the menu buttons. */
    private Array<MenuButton> buttons;

    /** The level number textures */
    private ObjectMap<MenuButton, Texture> levelNumbers;

    private MenuButton backButton;

    private MenuButton hoveredButton;

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

    private int menuCooldown;
    private boolean menuUp;
    private boolean menuDown;
    private boolean menuLeft;
    private boolean menuRight;
    private static final int COOLDOWN = 20;


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

        buttons = new Array<>();

        backButton = new MenuButton(new TextureRegion(assets.getEntry("levelSelect:back", Texture.class)), ExitCode.MAIN_MENU);
        backButton.texture.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        buttons.add(backButton);

        levelButtons = new Array<>(10);
        levelNumbers = new ObjectMap<>(10);
        for (int i = 0; i < 10; i++) {
            MenuButton button = new MenuButton(new TextureRegion(lights), ExitCode.START);
            levelButtons.add(button);
            buttons.add(button);

            levelNumbers.put(button, assets.getEntry("levelSelect:" + (i + 1), Texture.class));
            levelNumbers.get(button).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        }

        hoveredButton = levelButtons.get(0);

        levelButtons.get(0).down = levelButtons.get(0);
        for (int i = 0; i < 4; i++) {
            levelButtons.get(i).left = backButton;
            levelButtons.get(i).up = levelButtons.get(i+1);
            levelButtons.get(i+1).down = levelButtons.get(i);
            levelButtons.get(i).right = levelButtons.get(i);
        }

        levelButtons.get(4).right = levelButtons.get(5);
        levelButtons.get(4).up = levelButtons.get(6);
        levelButtons.get(4).left = backButton;

        levelButtons.get(5).right = levelButtons.get(5);
        levelButtons.get(5).up = levelButtons.get(7);
        levelButtons.get(5).left = levelButtons.get(4);
        levelButtons.get(5).down = levelButtons.get(3);

        levelButtons.get(6).right = levelButtons.get(7);
        levelButtons.get(6).up = levelButtons.get(8);
        levelButtons.get(6).left = backButton;
        levelButtons.get(6).down = levelButtons.get(4);

        levelButtons.get(7).right = levelButtons.get(7);
        levelButtons.get(7).up = levelButtons.get(9);
        levelButtons.get(7).left = levelButtons.get(6);
        levelButtons.get(7).down = levelButtons.get(5);

        levelButtons.get(8).right = levelButtons.get(9);
        levelButtons.get(8).up = levelButtons.get(8);
        levelButtons.get(8).left = backButton;
        levelButtons.get(8).down = levelButtons.get(6);

        levelButtons.get(9).right = levelButtons.get(9);
        levelButtons.get(9).up = levelButtons.get(9);
        levelButtons.get(9).left = levelButtons.get(8);
        levelButtons.get(9).down = levelButtons.get(7);

        backButton.up = backButton;
        backButton.left = backButton;
        backButton.down = backButton;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        Gdx.input.setInputProcessor( this );

        // Let ANY connected controller start the game.
        for (Controller controller : Controllers.get().getControllers()) {
            controller.addListener( this );
        }

        active = false;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        buttons.clear();
        levelButtons.clear();
        levelNumbers.clear();
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

        for (MenuButton button : buttons) {
            button.tint = (button.pressState == 1) ? Color.ORANGE : Color.WHITE;
            canvas.draw(button.texture, button.tint, button.hitbox.x, button.hitbox.y, button.hitbox.width, button.hitbox.height);
        }

        if (hoveredButton != null && levelNumbers.containsKey(hoveredButton)) {
            canvas.draw(levelNumbers.get(hoveredButton), Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
        }

        canvas.end();

        if (hoveredButton != null) {
            canvas.beginShapes(true);
            canvas.drawRectangle(hoveredButton.hitbox, hoveredButton.tint, 5);
            canvas.endShapes();
        }
    }

    private void update() {
        if (menuCooldown == 0) {
            if (menuUp) {
                if (hoveredButton == null) {
                    hoveredButton = levelButtons.get(0);
                } else {
                    hoveredButton = hoveredButton.up;
                }
                menuCooldown = COOLDOWN;
            } else if (menuDown) {
                if (hoveredButton == null) {
                    hoveredButton = levelButtons.get(9);
                } else {
                    hoveredButton = hoveredButton.down;
                }
                menuCooldown = COOLDOWN;
            } else if (menuLeft) {
                if (hoveredButton == null) {
                    hoveredButton = levelButtons.get(9);
                } else {
                    if (hoveredButton.left == backButton && hoveredButton != backButton) {
                        backButton.right = hoveredButton;
                    }
                    hoveredButton = hoveredButton.left;
                }
                menuCooldown = COOLDOWN;
            } else if (menuRight) {
                if (hoveredButton == null) {
                    hoveredButton = levelButtons.get(0);
                } else {
                    hoveredButton = hoveredButton.right;
                }
                menuCooldown = COOLDOWN;
            }
        }

        if (menuCooldown > 0) menuCooldown--;
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
            update();
            draw();

            // We are ready, notify our listener
            if (listener != null) {
                for (MenuButton button : buttons) {
                    if (button.pressState == 2) {

                        listener.exitScreen(this, button.exitCode);
                    }
                }
            }
        }
    }


    /**
     * Resets the screen so it can be reused
     */
    public void reset() {
        for (MenuButton button : buttons) {
            button.pressState = 0;
        }
        selectedLevel = -1; //?
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

        backButton.hitbox.setSize(10 * scale, 9 * scale);
        backButton.hitbox.setPosition(5 * scale, canvas.getHeight() - 12 * scale);
        backButton.texture.setRegion(5, 4, 10, 9);

        for (int i = 0; i < levelButtons.size; i++) {
            MenuButton button = levelButtons.get(i);

            int levelButtonWidth = (i < 5 || i % 2 == 0) ? LEVEL_BUTTON_WIDTH : LEVEL_BUTTON_WIDTH_2;
            Vector2 pos = getLevelHitboxPosition(i);

            button.texture.setRegion((int) pos.x, STANDARD_HEIGHT - (int) pos.y - LEVEL_BUTTON_HEIGHT, levelButtonWidth, LEVEL_BUTTON_HEIGHT);

            System.out.println(button.texture.getRegionX() + " " + button.texture.getRegionY() + " " + button.texture.getRegionWidth() + " " + button.texture.getRegionHeight());

            button.hitbox.setPosition(pos.scl(scale));
            button.hitbox.setSize(levelButtonWidth * scale, LEVEL_BUTTON_HEIGHT * scale);

        }
    }

    private Vector2 getLevelHitboxPosition(int i) { // uses 1 based indexing
        float x, y;
        x = (i < 5 || i % 2 == 0) ? LEVEL_BUTTON_START_X : LEVEL_BUTTON_START_X + LEVEL_BUTTON_OFFSET_X;

        int row;
        if (i < 5) {
            row = i;
        } else if (i < 6){
            row = 4;
        } else if (i < 8) {
            row = 5;
        } else {
            row = 6;
        }

        y = LEVEL_BUTTON_START_Y + LEVEL_BUTTON_OFFSET_Y * row;

        return new Vector2(x, y);
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
        Gdx.input.setCursorCatched(true);
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
    public boolean touchDown(int screenX, int screenY, int pointer, int buttonCode) {
        if (!active) return false;
        screenY = heightY-screenY;

        for (MenuButton button : buttons) {
            if (button.hitbox.contains(screenX, screenY) && button.pressState == 0) {
                button.pressState = 1;
                return true;
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
    public boolean touchUp(int screenX, int screenY, int pointer, int buttonCode) {
        if (!active) return false;
        screenY = heightY - screenY;

        for (MenuButton button : buttons) {
            if (button.pressState == 1 && button.hitbox.contains(screenX, screenY)) {
                button.pressState = 2;
                return true;
            } else {
                button.pressState = 0;
            }
        }

        return false;
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
        if (!active) return false;

        Gdx.input.setCursorCatched(true);
        ControllerMapping mapping = controller.getMapping();
        if (mapping == null) return true;

        if (backButton.pressState == 0 && buttonCode == mapping.buttonStart ) {
            backButton.pressState = 1;
            return true;
        }

        if (hoveredButton.pressState == 0 && buttonCode == mapping.buttonA) {
            hoveredButton.pressState = 1;
            return true;
        }

        return false;
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
        if (!active) return false;

        Gdx.input.setCursorCatched(true);
        ControllerMapping mapping = controller.getMapping();
        if (mapping == null) return true;

        if (backButton.pressState == 1 && buttonCode == mapping.buttonStart ) {
            backButton.pressState = 2;
            return true;
        }

        if (hoveredButton.pressState == 1 && buttonCode == mapping.buttonA) {
            hoveredButton.pressState = 2;
            return true;
        }

        return false;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        if (!active) return false;
        if (!Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(true);
        }
        menuUp = keycode == Input.Keys.UP;
        menuDown = keycode == Input.Keys.DOWN;
        menuLeft = keycode == Input.Keys.LEFT;
        menuRight = keycode == Input.Keys.RIGHT;

        if (keycode == Input.Keys.ENTER && hoveredButton != null && hoveredButton.pressState == 0) {
            hoveredButton.pressState = 1;
            return true;
        }

        return false;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (!active) return false;
        if (keycode == Input.Keys.UP) {
            menuUp = false;
            menuCooldown = 0;
            return true;
        }
        if (keycode == Input.Keys.DOWN) {
            menuDown = false;
            menuCooldown = 0;
            return true;
        }
        if (keycode == Input.Keys.LEFT) {
            menuLeft = false;
            menuCooldown = 0;
            return true;
        }
        if (keycode == Input.Keys.RIGHT) {
            menuRight = false;
            menuCooldown = 0;
            return true;
        }

        if (keycode == Input.Keys.ENTER && hoveredButton != null && hoveredButton.pressState == 1) {
            hoveredButton.pressState = 2;
            return true;
        }
        return false;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        if (!active) return false;
        if (Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(false);
            if (hoveredButton != null) {
                float x = hoveredButton.hitbox.x + hoveredButton.hitbox.width / 2;
                float y = hoveredButton.hitbox.y + hoveredButton.hitbox.height / 2;
                Gdx.input.setCursorPosition((int) x, heightY - (int) y);
            }
            return true;
        }
        screenY = heightY - screenY;
        hoveredButton = null;
        for (MenuButton button : buttons) {
            if (button.hitbox.contains(screenX, screenY)) {
                hoveredButton = button;
                return true;
            }
        }
        return false;
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
        return false;
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
        return false;
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
        if (!active) {
            return false;
        }

        if (axisCode == 0) {
            if (Math.abs(value) < 0.25) {
                menuLeft = false;
                menuRight = false;
            } else {
                menuLeft = value < 0;
                menuRight = value > 0;
            }
        } else if (axisCode == 1) {
            if (Math.abs(value) < 0.25) {
                menuDown = false;
                menuUp = false;
            } else {
                menuUp = value < 0;
                menuDown = value > 0;
            }
        }

        if (!menuUp && !menuDown && !menuLeft && !menuRight) {
            menuCooldown = 0;
        }

        return true;
    }

}
