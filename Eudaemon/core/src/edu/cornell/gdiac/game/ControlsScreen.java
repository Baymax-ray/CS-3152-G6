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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Action;
import edu.cornell.gdiac.game.models.Settings;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.Locale;

public class ControlsScreen implements Screen, InputProcessor, ControllerListener {
    /** Background texture */
    private final Texture background;


    /** Standard window size (for scaling) */
    private static final int STANDARD_WIDTH  = 1920;
    /** Standard window height (for scaling) */
    private static final int STANDARD_HEIGHT = 1080;
    /** Ratio of the bar width to the screen */
    private static final float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen */
    private static final float BAR_HEIGHT_RATIO = 0.25f;
    /** Height of the progress bar */
    private static final float BUTTON_SCALE  = 0.05f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** the settings of the game */
    private Settings settings;

    /** The width of the progress bar */
    private int width;
    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;


    /** Whether this player mode is still active */
    private boolean active;

    private MenuButton backButton;
    private MenuButton wasdButton;
    private MenuButton arrowsButton;
    private MenuButton defaultButton;

    private MappingButton jumpButton;
    private MappingButton dashButton;
    private MappingButton attackButton;
    private MappingButton transformButton;
    private MappingButton resetButton;

    private Array<MenuButton> buttons;

    private Array<MappingButton> mappingButtons;

    private MenuLabel directionalLabel;
    private MenuLabel jumpLabel;
    private MenuLabel dashLabel;
    private MenuLabel attackLabel;
    private MenuLabel transformLabel;
    private MenuLabel resetLabel;

    private Array<MenuLabel> labels;

    private BitmapFont menuFont;
    private FontTextureLoader fontTextureLoader;

    private MenuButton hoveredButton;

    private int menuCooldown;
    private boolean menuUp;
    private boolean menuDown;
    private boolean menuLeft;
    private boolean menuRight;
    private static final int COOLDOWN = 20;

    private boolean mappingMode;

    /**
     * Creates a SettingsScreen with the default size and position.
     *
     * @param assets  	The asset directory
     * @param canvas 	The game canvas to draw to
     */
    public ControlsScreen(AssetDirectory assets, GameCanvas canvas, final Settings settings, FontTextureLoader fontTextureLoader) {
        this.canvas  = canvas;
        this.settings = settings;

        background = assets.getEntry( "mainMenu:background", Texture.class ); // todo: get correct background
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );

        backButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:back", Texture.class)), ExitCode.SETTINGS);
        backButton.texture.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        menuFont = assets.getEntry("font:menu", BitmapFont.class);
        this.fontTextureLoader = fontTextureLoader;

        wasdButton = new MenuButton(new TextureRegion(fontTextureLoader.createTexture(menuFont, "WASD")));
        arrowsButton = new MenuButton(new TextureRegion(fontTextureLoader.createTexture(menuFont, "ARROW KEYS")));
        defaultButton = new MenuButton(new TextureRegion(fontTextureLoader.createTexture(menuFont, "DEFAULT")));

        buttons = new Array<>();
        buttons.add(backButton);
        buttons.add(wasdButton);
        buttons.add(arrowsButton);
        buttons.add(defaultButton);

        hoveredButton = wasdButton;

        directionalLabel = new MenuLabel(new TextureRegion(fontTextureLoader.createTexture(menuFont, "DIRECTIONAL CONTROLS")));
        jumpLabel = new MenuLabel(new TextureRegion(fontTextureLoader.createTexture(menuFont, "JUMP")));
        dashLabel = new MenuLabel(new TextureRegion(fontTextureLoader.createTexture(menuFont, "DASH")));
        attackLabel = new MenuLabel(new TextureRegion(fontTextureLoader.createTexture(menuFont, "ATTACK")));
        transformLabel = new MenuLabel(new TextureRegion(fontTextureLoader.createTexture(menuFont, "TRANSFORM")));
        resetLabel = new MenuLabel(new TextureRegion(fontTextureLoader.createTexture(menuFont, "RESET")));

        labels = new Array<>();
        labels.add(directionalLabel);
        labels.add(jumpLabel, dashLabel, attackLabel, resetLabel);
        labels.add(transformLabel);

        jumpButton = new MappingButton(fontTextureLoader, menuFont, Input.Keys.toString(settings.getActionBindings().getKeyMap().get(Action.BEGIN_JUMP)));
        dashButton = new MappingButton(fontTextureLoader, menuFont, Input.Keys.toString(settings.getActionBindings().getKeyMap().get(Action.DASH)));
        attackButton = new MappingButton(fontTextureLoader, menuFont, Input.Keys.toString(settings.getActionBindings().getKeyMap().get(Action.ATTACK)));
        transformButton = new MappingButton(fontTextureLoader, menuFont, Input.Keys.toString(settings.getActionBindings().getKeyMap().get(Action.TRANSFORM)));
        resetButton = new MappingButton(fontTextureLoader, menuFont, Input.Keys.toString(settings.getActionBindings().getKeyMap().get(Action.RESET)));

        mappingButtons = new Array<>();
        mappingButtons.add(jumpButton, dashButton, attackButton, resetButton);
        mappingButtons.add(transformButton);

        defaultButton.left = backButton;
        defaultButton.down = wasdButton;
        backButton.down = wasdButton;

        wasdButton.right = arrowsButton;
        wasdButton.left = backButton;
        wasdButton.up = defaultButton;
        wasdButton.down = jumpButton;

        arrowsButton.right = defaultButton;
        arrowsButton.left = wasdButton;
        arrowsButton.up = defaultButton;
        arrowsButton.down = jumpButton;

        jumpButton.right = defaultButton;
        jumpButton.left = backButton;
        jumpButton.up = wasdButton;
        jumpButton.down = dashButton;

        dashButton.right = defaultButton;
        dashButton.left = backButton;
        dashButton.up = jumpButton;
        dashButton.down = attackButton;

        attackButton.right = defaultButton;
        attackButton.left = backButton;
        attackButton.up = dashButton;
        attackButton.down = transformButton;

        transformButton.right = defaultButton;
        transformButton.left = backButton;
        transformButton.up = attackButton;
        transformButton.down = resetButton;

        resetButton.right = defaultButton;
        resetButton.left = backButton;
        resetButton.up = transformButton;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        Gdx.input.setInputProcessor( this );

        // Let ANY connected controller start the game.
        for (Controller controller : Controllers.get().getControllers()) {
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

    private void update() {

        if (wasdButton.pressState == 2) {
            settings.setUseArrowKeys(false);
            wasdButton.pressState = 0;
        }
        if (arrowsButton.pressState == 2) {
            settings.setUseArrowKeys(true);
            arrowsButton.pressState = 0;
        }
        if (defaultButton.pressState == 2) {
            settings.setDefault();
            jumpButton.updateTexture(fontTextureLoader, menuFont, settings.getDefaultJumpKey());
            dashButton.updateTexture(fontTextureLoader, menuFont, settings.getDefaultDashKey());
            attackButton.updateTexture(fontTextureLoader, menuFont, settings.getDefaultAttackKey());
            transformButton.updateTexture(fontTextureLoader, menuFont, settings.getDefaultTransformKey());
            resetButton.updateTexture(fontTextureLoader, menuFont, settings.getDefaultResetKey());
            resize(canvas.getWidth(), canvas.getHeight());
            defaultButton.pressState = 0;
        }
        for (MappingButton button : mappingButtons) {
            if (button.pressState == 2) {
                this.mappingMode = true;
                this.hoveredButton = button;
            }
        }

        if (menuCooldown == 0) {
            if (menuUp) {
                if (hoveredButton == null) {
                    hoveredButton = wasdButton;
                } else if (hoveredButton.up != null) {
                    hoveredButton = hoveredButton.up;
                }
                menuCooldown = COOLDOWN;
            } else if (menuDown) {
                if (hoveredButton == null) {
                    hoveredButton = wasdButton;
                } else if (hoveredButton.down != null){
                    hoveredButton = hoveredButton.down;
                }
                menuCooldown = COOLDOWN;
            } else if (menuLeft) {
                if (hoveredButton == null) {
                    hoveredButton = wasdButton;
                } else if (hoveredButton.left != null) {
                    if (hoveredButton.left == backButton && hoveredButton != backButton) {
                        backButton.right = hoveredButton;
                    }
                    hoveredButton = hoveredButton.left;
                }
                menuCooldown = COOLDOWN;
            } else if (menuRight) {
                if (hoveredButton == null) {
                    hoveredButton = wasdButton;
                } else if (hoveredButton.right != null) {
                    hoveredButton = hoveredButton.right;
                }
                menuCooldown = COOLDOWN;
            }
        }

        if (menuCooldown > 0) menuCooldown--;
    }


    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        float sx = ((float)canvas.getWidth())/STANDARD_WIDTH;
        float sy = ((float)canvas.getHeight())/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        canvas.setOverlayCamera();
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());

        backButton.tint = (backButton.pressState == 1 ? Color.GRAY: Color.WHITE);
        wasdButton.tint = (!settings.getUseArrowKeys()) ? Color.ORANGE : Color.WHITE;
        arrowsButton.tint = (settings.getUseArrowKeys()) ? Color.ORANGE : Color.WHITE;
        defaultButton.tint = (defaultButton.pressState == 1) ? Color.ORANGE : Color.WHITE;

        for (MenuButton button : buttons) {
            canvas.draw(button.texture, button.tint, button.hitbox.x, button.hitbox.y, button.hitbox.width, button.hitbox.height);
        }

        for (MappingButton button : mappingButtons) {
            button.tint = (button.pressState == 0) ? Color.WHITE : Color.ORANGE;
            canvas.draw(button.texture, button.tint, button.hitbox.x, button.hitbox.y, button.hitbox.width, button.hitbox.height);
        }

        for (MenuLabel label : labels) {
            canvas.draw(label.texture, Color.WHITE, label.position.x, label.position.y, label.position.width, label.position.height);
        }

        canvas.end();

        if (hoveredButton != null) {
            canvas.beginShapes(true);
            float lineWidth = (hoveredButton == backButton) ? 5 : 10;
            canvas.drawRectangle(hoveredButton.hitbox, hoveredButton.tint, lineWidth);
            canvas.endShapes();
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
            update();
            draw();

            // We are are ready, notify our listener
            if (backButton.pressState == 2 && listener != null) {
                listener.exitScreen(this, backButton.exitCode);
            }
        }}


    /**
     * Resets the screen so it can be reused
     */
    public void reset() {
        System.out.println("WHATS GOIND ON HERE");
        for (MenuButton button : buttons) {
            button.pressState = 0;
        }
        for (MenuButton button : mappingButtons) {
            button.pressState = 0;
        }
        hoveredButton = wasdButton;
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

        this.width = (int)(BAR_WIDTH_RATIO*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;

        float backButtonScale = 8 * scale;
        backButton.hitbox.setSize(10 * backButtonScale, 9 * backButtonScale);
        backButton.hitbox.setPosition(5 * backButtonScale, canvas.getHeight() - 12 * backButtonScale);
        backButton.texture.setRegion(5, 4, 10, 9);

        defaultButton.hitbox.setSize(defaultButton.texture.getRegionWidth() * scale, defaultButton.texture.getRegionHeight() * scale);
        defaultButton.hitbox.setPosition(canvas.getWidth() * 0.8f, canvas.getHeight() - 96 * scale);

        directionalLabel.position.setSize(directionalLabel.texture.getRegionWidth() * scale, directionalLabel.texture.getRegionHeight() * scale);
        directionalLabel.position.setPosition(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.67f);

        wasdButton.hitbox.setSize(wasdButton.texture.getRegionWidth() * scale, wasdButton.texture.getRegionHeight() * scale);
        wasdButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.67f);

        arrowsButton.hitbox.setSize(arrowsButton.texture.getRegionWidth() * scale, arrowsButton.texture.getRegionHeight() * scale);
        arrowsButton.hitbox.setPosition(canvas.getWidth() / 1.6f, canvas.getHeight() * 0.67f);

        jumpLabel.position.setSize(jumpLabel.texture.getRegionWidth() * scale, jumpLabel.texture.getRegionHeight() * scale);
        jumpLabel.position.setPosition(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.586f);

        jumpButton.hitbox.setSize(jumpButton.texture.getRegionWidth() * scale, jumpButton.texture.getRegionHeight() * scale);
        jumpButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.586f);

        dashLabel.position.setSize(dashLabel.texture.getRegionWidth() * scale, dashLabel.texture.getRegionHeight() * scale);
        dashLabel.position.setPosition(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.503f);

        dashButton.hitbox.setSize(dashButton.texture.getRegionWidth() * scale, dashButton.texture.getRegionHeight() * scale);
        dashButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.503f);

        attackLabel.position.setSize(attackLabel.texture.getRegionWidth() * scale, attackLabel.texture.getRegionHeight() * scale);
        attackLabel.position.setPosition(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.42f);

        attackButton.hitbox.setSize(attackButton.texture.getRegionWidth() * scale, attackButton.texture.getRegionHeight() * scale);
        attackButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.42f);

        transformLabel.position.setSize(transformLabel.texture.getRegionWidth() * scale, transformLabel.texture.getRegionHeight() * scale);
        transformLabel.position.setPosition(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.337f);

        transformButton.hitbox.setSize(transformButton.texture.getRegionWidth() * scale, transformButton.texture.getRegionHeight() * scale);
        transformButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.337f);

        resetLabel.position.setSize(resetLabel.texture.getRegionWidth() * scale, resetLabel.texture.getRegionHeight() * scale);
        resetLabel.position.setPosition(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.254f);

        resetButton.hitbox.setSize(resetButton.texture.getRegionWidth() * scale, resetButton.texture.getRegionHeight() * scale);
        resetButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.254f);


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

        if (mappingMode) {
            mappingMode = false;
            hoveredButton.pressState = 0;
        }

        if (backButton.pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        for (MenuButton button : buttons) {
            if (button.hitbox.contains(screenX, screenY) && button.pressState == 0) {
                button.pressState = 1;
            }
        }
        for (MenuButton button : mappingButtons) {
            if (button.hitbox.contains(screenX, screenY) && button.pressState == 0) {
                button.pressState = 1;
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

        if (mappingMode) {
            mappingMode = false;
            hoveredButton.pressState = 0;
        }

        screenY = heightY - screenY;

        for (MenuButton button : buttons) {
            if (button.pressState == 1) {
                if (button.hitbox.contains(screenX, screenY)) {
                    button.pressState = 2;
                    return true;
                } else {
                    button.pressState = 0;
                }
            }
        }
        for (MenuButton button : mappingButtons) {
            if (button.pressState == 1) {
                if (button.hitbox.contains(screenX, screenY)) {
                    button.pressState = 2;
                    return true;
                } else {
                    button.pressState = 0;
                }
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

        if (mappingMode) {
            mappingMode = false;
            hoveredButton.pressState = 0;
        }

        Gdx.input.setCursorCatched(true);

        ControllerMapping mapping = controller.getMapping();
        if (mapping == null) return true;

        if (backButton.pressState == 0 && buttonCode == mapping.buttonStart ) {
            backButton.pressState = 1;
            return true;
        }

        if (hoveredButton != null && hoveredButton.pressState == 0 && buttonCode == mapping.buttonA) {
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

        if (mappingMode) {
            mappingMode = false;
            hoveredButton.pressState = 0;
        }

        Gdx.input.setCursorCatched(true);
        ControllerMapping mapping = controller.getMapping();
        if (mapping == null) return true;

        if (backButton.pressState == 1 && buttonCode == mapping.buttonStart ) {
            backButton.pressState = 2;
            return true;
        }

        if (hoveredButton != null && hoveredButton.pressState == 1 && buttonCode == mapping.buttonA) {
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

        if (mappingMode) {
            String keyName = Input.Keys.toString(keycode);
            if (keyName.length() == 1
                    || keyName.equals("Space")
                    || keyName.equals("Left")
                    || keyName.equals("Right")
                    || keyName.equals("Up")
                    || keyName.equals("Down")) { // valid key
                if (hoveredButton == jumpButton)
                    settings.setCustomJumpKey(keyName);
                else if (hoveredButton == dashButton)
                    settings.setCustomDashKey(keyName);
                else if (hoveredButton == attackButton)
                    settings.setCustomAttackKey(keyName);
                else if (hoveredButton == resetButton)
                    settings.setCustomResetKey(keyName);
                else if  (hoveredButton == transformButton)
                    settings.setCustomTransformKey(keyName);
                else {
                    System.out.println("Invalid State, weird");
                    mappingMode = false;
                    return true;
                }

                ((MappingButton) hoveredButton).updateTexture(fontTextureLoader, menuFont, keyName);
                resize(canvas.getWidth(), canvas.getHeight());
            } else {
//                error = true;
            }
            hoveredButton.pressState = 0;
            mappingMode = false;
            return true;
        }

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

        if (mappingMode) return true;

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
        for (MenuButton button : mappingButtons) {
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

        if (mappingMode) return true;

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
