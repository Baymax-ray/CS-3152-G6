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
import com.badlogic.gdx.math.Rectangle;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Level;
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
public class SettingsScreen implements Screen, InputProcessor, ControllerListener {

	/** Background texture for start-up */
	private final Texture background;

	/** Play button to display when done */
	private Texture volumeButton;
	/** quit button to display when done */
	private Texture screenSizeButton;
	/** are we coming from the main menu to the setting screen?
	 * This is for judging if we should go back to menu or level*/
	private boolean isFromMainMenu=false;




	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static final int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static final int STANDARD_WIDTH  = 260;
	/** Standard window height (for scaling) */
	private static final int STANDARD_HEIGHT = 135;
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
	/** The current state of the play button */
	private int volumePressState;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;

	/** Whether this player mode is still active */
	private boolean active;
	/** The hitbox of the start button */
	private Rectangle volumeButtonHitbox;

	private Rectangle  screenSizeButtonHitbox;

	private int screenSizePressState;

	/** back button texture*/
	private Texture backButton;

	/** The hitbox for the back button */
	private Rectangle backHitbox;

	/** The current state of the back button */
	private int backPressState;
	private Texture unfilledBar;
	private Texture fullScreenOnButton;
	private Texture fullScreenOffButton;
	private Texture difficultyNormalButton;
	private Texture difficultyHardButton;
	private Texture difficultyVeteranButton;

	private Rectangle normalHitbox;
	private Rectangle hardHitbox;
	private Rectangle veteranHitbox;
	private int normalPressState;
	private int hardPressState;
	private int veteranPressState;


	//Texture to visually show the adjustment of settings (e.g volume, screen size)
	private Texture filledBar;

	//Texture for the circular toggle to drag to adjust setting - should sense user input from touching this
	private Texture toggle;

	private Level level;

	//0 for easy, 1 for hard, 2 for veteran
	public int currentDifficulty;



	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean restartIsReady() {
		return volumePressState == 2;
	}
	public boolean quitIsReady() {
		return screenSizePressState == 2;
	}
	public void setIsFromMainMenu(boolean isfrom){this.isFromMainMenu = isfrom;}

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
	 */
	public SettingsScreen(AssetDirectory assets, GameCanvas canvas, Level currentLevel) {
		this.canvas  = canvas;
		this.isFromMainMenu = false;
		this.level = currentLevel;

		// Compute the dimensions from the canvas
		resize(canvas.getWidth(),canvas.getHeight());

		// Load the next two images immediately.
		background = assets.getEntry( "settingsScreen:background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		volumeButton = assets.getEntry("deathScreen:restart",Texture.class);
		screenSizeButton = assets.getEntry("deathScreen:quit", Texture.class);
		backButton = assets.getEntry("settingsScreen:back", Texture.class);
		backButton.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		unfilledBar = assets.getEntry("settingsScreen:unfilledBar", Texture.class);
		fullScreenOnButton = assets.getEntry("settingsScreen:settingsOn", Texture.class);
		fullScreenOffButton = assets.getEntry("settingsScreen:settingsOff", Texture.class);
		difficultyNormalButton = assets.getEntry("settingsScreen:normal", Texture.class);
		difficultyHardButton = assets.getEntry("settingsScreen:hard", Texture.class);
		difficultyVeteranButton = assets.getEntry("settingsScreen:vet", Texture.class);
		filledBar = assets.getEntry("settingsScreen:filledBar", Texture.class);
		toggle = assets.getEntry("settingsScreen:dragToggle", Texture.class);

		backHitbox = new Rectangle();
		backPressState = 0;
		normalHitbox = new Rectangle();
		hardHitbox = new Rectangle();
		veteranHitbox = new Rectangle();
		normalPressState = 0;
		hardPressState = 0;
		veteranPressState = 0;


		Gdx.input.setInputProcessor( this );

		volumeButtonHitbox = new Rectangle();
		screenSizeButtonHitbox = new Rectangle();
		screenSizePressState = 0;
		currentDifficulty = 0;

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
		float sx = ((float)canvas.getWidth())/STANDARD_WIDTH;
		float sy = ((float)canvas.getHeight())/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);
		canvas.setOverlayCamera();
		canvas.begin();
		canvas.draw(background, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
		Color normalTint = (level.isNormalDifficulty() ? Color.ORANGE: Color.WHITE);
		Color hardTint = (level.isHardDifficulty() ? Color.ORANGE: Color.WHITE);
		Color veteranTint = (level.isVeteranDifficulty() ? Color.ORANGE: Color.WHITE);
		Color backTint = (backPressState == 1 ? Color.GRAY: Color.WHITE);
		canvas.draw(backButton, backTint, 0, 0, canvas.getWidth(), canvas.getHeight());
		//Master Volume Bar
		canvas.draw(unfilledBar,Color.WHITE, (canvas.getWidth() / 1.9f), canvas.getHeight()/1.49f, unfilledBar.getWidth()/scale, unfilledBar.getHeight()/scale*2.8f);
		//Music Volume Bar
		canvas.draw(unfilledBar,Color.WHITE, (canvas.getWidth() / 1.9f), canvas.getHeight()/1.71f, unfilledBar.getWidth()/scale, unfilledBar.getHeight()/scale*2.8f);
		//SFX Volume Bar
		canvas.draw(unfilledBar,Color.WHITE, (canvas.getWidth() / 1.9f), canvas.getHeight()/2f, unfilledBar.getWidth()/scale, unfilledBar.getHeight()/scale*2.8f);
		//Brightness Bar
		canvas.draw(unfilledBar,Color.WHITE, (canvas.getWidth() / 1.9f), canvas.getHeight()/2.4f, unfilledBar.getWidth()/scale, unfilledBar.getHeight()/scale*2.8f);
		//Fullscreen On Button
		canvas.draw(fullScreenOnButton, Color.WHITE, (canvas.getWidth() / 1.9f), canvas.getHeight()/3f , fullScreenOnButton.getWidth(), fullScreenOnButton.getHeight());
		//Fullscreen Off Button
		canvas.draw(fullScreenOffButton, Color.WHITE, (canvas.getWidth() / 1.68f), canvas.getHeight()/3f , fullScreenOffButton.getWidth(), fullScreenOffButton.getHeight());
		//Level Difficulty Normal Button
		canvas.draw(difficultyNormalButton, normalTint, (canvas.getWidth() / 1.9f), canvas.getHeight()/6f , difficultyNormalButton.getWidth()/2, difficultyNormalButton.getHeight());
		//Level Difficulty Hard Button
		canvas.draw(difficultyHardButton, hardTint, (canvas.getWidth() / 1.5f), canvas.getHeight()/6f , difficultyHardButton.getWidth()/2, difficultyHardButton.getHeight());
		//Level Difficulty Veteran Button
		canvas.draw(difficultyVeteranButton, veteranTint, (canvas.getWidth() / 1.3f), canvas.getHeight()/6f , difficultyVeteranButton.getWidth()/2, difficultyVeteranButton.getHeight());

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
			draw();

			// We are are ready, notify our listener
			if (backPressState == 2 && listener != null) {
				if(isFromMainMenu){listener.exitScreen(this, ExitCode.MAIN_MENU);}
				else{ listener.exitScreen(this, ExitCode.PAUSE);}
		}
	}}


	/**
	 * Resets the screen so it can be reused
	 */
	public void reset() {
		this.volumePressState = 0;
		this.screenSizePressState = 0;
		this.backPressState = 0;
		this.normalPressState = 0;
		this.hardPressState = 0;
		this.veteranPressState = 0;
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

		if(screenSizeButton != null){
			screenSizeButtonHitbox.setSize(BUTTON_SCALE *scale*screenSizeButton.getWidth(),BUTTON_SCALE * scale * screenSizeButton.getHeight());
			screenSizeButtonHitbox.setCenter(canvas.getWidth()/2.0f, centerY + screenSizeButton.getHeight()*scale *0.05f);
		}

		if (volumeButton != null) {
			float buttonSpacing = 0.35f;
			volumeButtonHitbox.setSize(BUTTON_SCALE * scale * volumeButton.getWidth(), BUTTON_SCALE * scale * volumeButton.getHeight());
			volumeButtonHitbox.setCenter(canvas.getWidth() / 2.0f, centerY + volumeButton.getHeight() * buttonSpacing * scale);
		}

		if(backButton != null){
			backHitbox.setSize(10 * scale);
			backHitbox.setPosition(5 * scale, canvas.getHeight() - 12 * scale);
		}

		if(difficultyNormalButton != null){
			normalHitbox.setSize(difficultyNormalButton.getWidth(), difficultyNormalButton.getHeight());
			normalHitbox.setPosition((canvas.getWidth() / 1.9f), canvas.getHeight()/6f);
		}
		if(difficultyHardButton!=null){
			hardHitbox.setSize(difficultyHardButton.getWidth(), difficultyHardButton.getHeight());
			hardHitbox.setPosition((canvas.getWidth() / 1.5f), canvas.getHeight()/6f);
		}
		if(difficultyVeteranButton!=null){
			veteranHitbox.setSize(difficultyVeteranButton.getWidth(), difficultyVeteranButton.getHeight());
			veteranHitbox.setPosition((canvas.getWidth() / 1.3f), canvas.getHeight()/6f);
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
		if (volumePressState == 2) {
			return true;
		}
		if(screenSizePressState == 2){
			return true;
		}

		// Flip to match graphics coordinates
		screenY = heightY-screenY;



		if (backHitbox.contains(screenX, screenY)) {
			backPressState = 1;
			return true;
		}
		if(normalHitbox.contains(screenX, screenY)){
			normalPressState = 1;
			level.setNormalDifficulty(true);
			this.currentDifficulty = 0;
			level.settingsChanged = true;
			level.getPlayer().setHearts(5);
			return true;
		}
		if(hardHitbox.contains(screenX, screenY)){
			hardPressState = 1;
			level.setHardDifficulty(true);
			this.currentDifficulty = 1;
			level.settingsChanged = true;
			level.getPlayer().setHearts(4);
			return true;
		}
		if(veteranHitbox.contains(screenX, screenY)){
			veteranPressState = 1;
			level.setVeteranDifficulty(true);
			this.currentDifficulty = 2;
			level.settingsChanged = true;
			level.getPlayer().setHearts(3);
			return true;
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
		if (volumePressState == 1) {
			volumePressState = 2;
			return true;
		}
		if(screenSizePressState == 1){
			screenSizePressState = 2;
			return true;
		}
		if (backPressState == 1) {
			backPressState = 2;
			return true;
		}
//		if(normalPressState == 1){
//			normalPressState = 2;
//			return true;
//		}
//		if(hardPressState == 1){
//			hardPressState = 2;
//			return true;
//		}
//		if(veteranPressState == 1){
//			veteranPressState = 2;
//			return true;
//		}
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
		if (volumePressState == 0) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				volumePressState = 1;
				return true;
			}
		}
		if (screenSizePressState == 0) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				screenSizePressState = 1;
				return true;
			}
		}
		if (backPressState == 0) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				backPressState = 1;
				return true;
			}
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
		if (volumePressState == 1) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				volumePressState = 2;
				return true;
			}
		}
		if (screenSizePressState == 1) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				screenSizePressState = 2;
				return true;
			}
		}
		if (backPressState == 1) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				backPressState = 2;
				return true;
			}
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
		Gdx.input.setCursorCatched(false);
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
		return false;
	}

}