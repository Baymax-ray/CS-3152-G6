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
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;
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
public class EscapeMenu implements Screen, InputProcessor, ControllerListener {
	// There are TWO asset managers.  One to load the loading screen.  The other to load the assets

	/** Background texture for start-up */
	private final Texture background;


	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static final int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static final int STANDARD_WIDTH  = 800;
	/** Standard window height (for scaling) */
	private static final int STANDARD_HEIGHT = 700;
	/** Ratio of the bar width to the screen */
	private static final float BAR_WIDTH_RATIO  = 0.66f;
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.25f;
	/** Height of the progress bar */
	private static final float BUTTON_SCALE  = 0.63f;

	/** Reference to GameCanvas created by the root */
	private GameCanvas canvas;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

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

	/** Current progress (0 to 1) of the asset manager */
	private float progress;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;

	/** Whether or not this player mode is still active */
	private boolean active;
	/** The hitbox of the start button */

	private MenuButton hoveredButton;

	private Array<MenuButton> buttons;
	private MenuButton resumeButton;
	private MenuButton settingsButton;
	private MenuButton restartButton;
	private MenuButton quitButton;

	private int menuCooldown;
	private boolean menuUp;
	private boolean menuDown;
	private static final int COOLDOWN = 10;




	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean restartIsReady() {
		return restartButton.pressState == 2;
	}
	public boolean quitIsReady() { return quitButton.pressState == 2; }

	public boolean resumeIsReady() { return resumeButton.pressState == 2; }
	public boolean settingsIsReady() { return settingsButton.pressState == 2; }

	/**
	 * Creates a LoadingScreen with the default budget, size and position.
	 *
	 * @param assets  	The asset directory
	 * @param canvas 	The game canvas to draw to
	 */
	public EscapeMenu(AssetDirectory assets, GameCanvas canvas) {
		this(assets, canvas, DEFAULT_BUDGET);
	}

	/**
	 * Creates a LoadingScreen with the default size and position.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param assets  	The asset directory
	 * @param canvas 	The game canvas to draw to
	 * @param millis The loading budget in milliseconds
	 */
	public EscapeMenu(AssetDirectory assets, GameCanvas canvas, int millis) {
		this.canvas  = canvas;
		budget = millis;

		buttons = new Array<>();

		resumeButton = new MenuButton(assets.getEntry("escapeMenu:resume" , Texture.class), ExitCode.START);
		settingsButton = new MenuButton(assets.getEntry("escapeMenu:settings", Texture.class), ExitCode.SETTINGS);
		restartButton = new MenuButton(assets.getEntry("escapeMenu:restart", Texture.class), ExitCode.RESET);
		quitButton = new MenuButton(assets.getEntry("escapeMenu:quit", Texture.class), ExitCode.MAIN_MENU);

		buttons.add(resumeButton);
		buttons.add(settingsButton);
		buttons.add(restartButton);
		buttons.add(quitButton);

		resumeButton.up = quitButton;
		resumeButton.down = settingsButton;

		settingsButton.up = resumeButton;
		settingsButton.down = restartButton;

		restartButton.up = settingsButton;
		restartButton.down = quitButton;

		quitButton.up = restartButton;
		quitButton.down = resumeButton;

		hoveredButton = resumeButton;

		// Load the next two images immediately.
		background = assets.getEntry( "escapeMenu:background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );

		Gdx.input.setInputProcessor( this );

		// Compute the dimensions from the canvas
		resize(canvas.getWidth(),canvas.getHeight());

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

		if (menuUp && menuCooldown == 0) {
			if (hoveredButton == null) {
				hoveredButton = quitButton;
			} else {
				hoveredButton = hoveredButton.up;
			}
			System.out.println("UP");
			menuCooldown = COOLDOWN;
		} else if (menuDown && menuCooldown == 0) {
			if (hoveredButton == null) {
				hoveredButton = resumeButton;
			} else {
				hoveredButton = hoveredButton.down;
			}
			System.out.println("DOWN");
			menuCooldown = COOLDOWN;
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
		canvas.setOverlayCamera();
		canvas.begin();
		canvas.draw(background, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());

		for (MenuButton button : buttons) {
			button.tint = (button.pressState == 1 ? Color.ORANGE : Color.WHITE);
			canvas.draw(button.texture, button.tint, button.hitbox.x, button.hitbox.y, button.hitbox.width, button.hitbox.height);
		}

		canvas.end();
		if (hoveredButton != null) {
			canvas.beginShapes(true);
			canvas.drawRectangle(hoveredButton.hitbox, hoveredButton.tint, 10);
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
			if (restartIsReady() && listener != null) {
				listener.exitScreen(this, ExitCode.RESET);
			}
			if(quitIsReady() && listener != null){
				listener.exitScreen(this, ExitCode.MAIN_MENU);
			}
			if(resumeIsReady() && listener != null){
				listener.exitScreen(this, ExitCode.START);
			}
			if(settingsIsReady() && listener != null){
				listener.exitScreen(this, ExitCode.SETTINGS);
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
		hoveredButton = resumeButton;
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


		if(quitButton.texture != null){
			quitButton.hitbox.setSize(BUTTON_SCALE *scale*quitButton.texture.getWidth(),BUTTON_SCALE * scale * quitButton.texture.getHeight());
			quitButton.hitbox.setCenter(canvas.getWidth()/2.0f, centerY + quitButton.texture.getHeight() * scale*0.55f);
		}

		if (restartButton.texture != null) {
			restartButton.hitbox.setSize(BUTTON_SCALE * scale * restartButton.texture.getWidth(), BUTTON_SCALE * scale * restartButton.texture.getHeight());
			restartButton.hitbox.setCenter(canvas.getWidth() / 2.0f, centerY + restartButton.texture.getHeight() * scale + height/12f);
		}

		if (resumeButton.texture != null) {
			resumeButton.hitbox.setSize(BUTTON_SCALE * scale * resumeButton.texture.getWidth(), BUTTON_SCALE * scale * resumeButton.texture.getHeight());
			resumeButton.hitbox.setCenter(canvas.getWidth() / 2.0f, centerY + resumeButton.texture.getHeight() * scale + height/3.4f);
		}
		if (settingsButton.texture != null) {
			settingsButton.hitbox.setSize(BUTTON_SCALE * scale * settingsButton.texture.getWidth(), BUTTON_SCALE * scale * settingsButton.texture.getHeight());
			settingsButton.hitbox.setCenter(canvas.getWidth() / 2.0f, centerY + settingsButton.texture.getHeight() * scale + height / 5.3f);
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
		for (MenuButton menuButton : buttons) {
			if (menuButton.texture == null || menuButton.pressState == 2) {
				return true;
			}
		}

		// Flip to match graphics coordinates
		screenY = heightY-screenY;

		for (MenuButton menuButton : buttons) {
			if (menuButton.hitbox.contains(screenX, screenY)) {
				menuButton.pressState = 1;
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
		for (MenuButton menuButton : buttons) {
			if (menuButton.pressState == 1) {
				menuButton.pressState = 2;
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
		ControllerMapping mapping = controller.getMapping();

		if (restartButton.pressState == 0) {
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				restartButton.pressState = 1;
				return false;
			}
		}

		if (hoveredButton.pressState == 0) {
			if (mapping != null && buttonCode == mapping.buttonA) {
				hoveredButton.pressState = 1;
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
		ControllerMapping mapping = controller.getMapping();

		if (restartButton.pressState == 1) {
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				restartButton.pressState = 2;
				return false;
			}
		}
		if (hoveredButton.pressState == 1) {
			if (mapping != null && buttonCode == mapping.buttonA) {
				hoveredButton.pressState = 2;
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
		menuUp = keycode == Input.Keys.UP;
		menuDown = keycode == Input.Keys.DOWN;

		if (keycode == Input.Keys.ENTER && hoveredButton.pressState == 0) {
			hoveredButton.pressState = 1;
			return false;
		}
		if (keycode == Input.Keys.ESCAPE && resumeButton.pressState == 0) {
			resumeButton.pressState = 1;
			return false;
		}
		return keycode != Input.Keys.UP && keycode != Input.Keys.DOWN;
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
		if (keycode == Input.Keys.UP) {
			menuUp = false;
			menuCooldown = 0;
			return false;
		}
		if (keycode == Input.Keys.DOWN) {
			menuDown = false;
			menuCooldown = 0;
			return false;
		}

		if (keycode == Input.Keys.ENTER && hoveredButton.pressState == 1) {
			hoveredButton.pressState = 2;
			return false;
		}
		if (keycode == Input.Keys.ESCAPE && resumeButton.pressState == 1) {
			resumeButton.pressState = 2;
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
		screenY = heightY - screenY;
		hoveredButton = null;
		for (MenuButton button : buttons) {
			if (button.hitbox.contains(screenX, screenY)) {
				hoveredButton = button;
			}
		}

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

		if (axisCode == 1) { // vertical movement

			if (Math.abs(value) < 0.25) {
				menuCooldown = 0;
				menuDown = false;
				menuUp = false;
			} else {
				menuUp = value < 0;
				menuDown = value > 0;
			}

			return false;
		}
		return true;
	}

}