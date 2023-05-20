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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Settings;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;

import static java.lang.Math.round;

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

	/** are we coming from the main menu to the setting screen?
	 * This is for judging if we should go back to menu or level*/
	private boolean isFromMainMenu;


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

	private Texture unfilledBar;

	private MenuButton backButton;

	private MenuButton controlsButton;

	private MenuSlider volumeSlider;
	private MenuSlider musicSlider;
	private MenuSlider sfxSlider;
	private MenuSlider brightnessSlider;

	private MenuButton difficultyNormalButton;
	private MenuButton difficultyHardButton;
	private MenuButton difficultyVeteranButton;

	private MenuButton fullScreenOnButton;
	private MenuButton fullScreenOffButton;

	private MenuButton screenShakeOnButton;
	private MenuButton screenShakeOffButton;

	private Array<MenuButton> buttons;

	private Array<MenuSlider> sliders;

	private MenuButton hoveredButton;


	//Texture to visually show the adjustment of settings (e.g volume, screen size)
	private Texture filledBar;

	//Texture for the circular toggle to drag to adjust setting - should sense user input from touching this
	private Texture toggle;

	private int menuCooldown;
	private boolean menuUp;
	private boolean menuDown;
	private boolean menuLeft;
	private boolean menuRight;
	private static final int COOLDOWN = 20;


	public void setIsFromMainMenu(boolean isfrom){this.isFromMainMenu = isfrom;}

	/**
	 * Creates a SettingsScreen with the default size and position.
	 *
	 * @param assets  	The asset directory
	 * @param canvas 	The game canvas to draw to
	 */
	public SettingsScreen(AssetDirectory assets, GameCanvas canvas, final Settings settings, FontTextureLoader fontTextureLoader) {
		this.canvas  = canvas;
		this.isFromMainMenu = false;
		this.settings = settings;

		background = assets.getEntry( "settingsScreen:background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		unfilledBar = assets.getEntry("settingsScreen:unfilledBar", Texture.class);
		filledBar = assets.getEntry("settingsScreen:filledBar", Texture.class);
		toggle = assets.getEntry("settingsScreen:dragToggle", Texture.class);

		backButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:back", Texture.class))); // we are not adding the exitCode here, it is determined by `isFromMainMenu`
		backButton.texture.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		controlsButton = new MenuButton(new TextureRegion(fontTextureLoader.createFontTexture(assets.getEntry("font:menu", BitmapFont.class), "CONTROLS")), ExitCode.CONTROLS);

		volumeSlider = new MenuSlider(filledBar, unfilledBar, toggle) {
			@Override
			public float getValue() {
				return settings.getMasterVolume();
			}

			@Override
			protected void updateValue(float value) {
				settings.setMasterVolume(value);
			}
		};

		musicSlider = new MenuSlider(filledBar, unfilledBar, toggle) {
			@Override
			public float getValue() {
				return settings.getMusicVolume();
			}

			@Override
			protected void updateValue(float value) {
				settings.setMusicVolume(value);
			}
		};

		sfxSlider = new MenuSlider(filledBar, unfilledBar, toggle) {
			@Override
			public float getValue() {
				return settings.getSfxVolume();
			}

			@Override
			protected void updateValue(float value) {
				settings.setSfxVolume(value);
			}
		};

		brightnessSlider = new MenuSlider(filledBar, unfilledBar, toggle) {
			@Override
			public float getValue() {
				return settings.getBrightness();
			}

			@Override
			protected void updateValue(float value) {
				settings.setBrightness(value);
			}
		};

		fullScreenOnButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:settingsOn", Texture.class)));
		fullScreenOffButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:settingsOff", Texture.class)));
		screenShakeOnButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:settingsOn", Texture.class)));
		screenShakeOffButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:settingsOff", Texture.class)));
		difficultyNormalButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:normal", Texture.class)));
		difficultyHardButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:hard", Texture.class)));
		difficultyVeteranButton = new MenuButton(new TextureRegion(assets.getEntry("settingsScreen:vet", Texture.class)));

		buttons = new Array<>();
		buttons.add(backButton);
		buttons.add(controlsButton);
		buttons.add(fullScreenOnButton);
		buttons.add(fullScreenOffButton);
		buttons.add(screenShakeOnButton);
		buttons.add(screenShakeOffButton);
		buttons.add(difficultyNormalButton);
		buttons.add(difficultyHardButton);
		buttons.add(difficultyVeteranButton);

		sliders = new Array<>();
		sliders.add(volumeSlider);
		sliders.add(musicSlider);
		sliders.add(sfxSlider);
		sliders.add(brightnessSlider);

		hoveredButton = volumeSlider;

		volumeSlider.down = musicSlider;
		musicSlider.up = volumeSlider;
		musicSlider.down = sfxSlider;
		sfxSlider.up = musicSlider;
		sfxSlider.down = brightnessSlider;
		brightnessSlider.up = sfxSlider;
		brightnessSlider.down = fullScreenOnButton;

		fullScreenOnButton.up = brightnessSlider;
		fullScreenOnButton.right = fullScreenOffButton;
		fullScreenOnButton.down = screenShakeOnButton;
		fullScreenOffButton.up = brightnessSlider;
		fullScreenOffButton.left = fullScreenOnButton;
		fullScreenOffButton.down = screenShakeOffButton;

		screenShakeOnButton.up = fullScreenOnButton;
		screenShakeOnButton.right = fullScreenOffButton;
		screenShakeOnButton.down = difficultyHardButton;
		screenShakeOffButton.up = fullScreenOffButton;
		screenShakeOffButton.left = screenShakeOnButton;
		screenShakeOffButton.down = difficultyHardButton;

		difficultyNormalButton.up = screenShakeOnButton;
		difficultyNormalButton.right = difficultyHardButton;
		difficultyHardButton.up = screenShakeOffButton;
		difficultyHardButton.left = difficultyNormalButton;
		difficultyHardButton.right = difficultyVeteranButton;
		difficultyVeteranButton.up = screenShakeOffButton;
		difficultyVeteranButton.left = difficultyHardButton;

		volumeSlider.up = controlsButton;
		controlsButton.down = volumeSlider;
		backButton.down = volumeSlider;


		fullScreenOnButton.left = backButton;
		screenShakeOnButton.left = backButton;
		difficultyNormalButton.left = backButton;
		controlsButton.left = backButton;

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

		if (fullScreenOnButton.pressState == 2) {
//			settings.setFullscreen(true);
			fullScreenOnButton.pressState = 0;
		}
		if (fullScreenOffButton.pressState == 2) {
//			settings.setFullscreen(false);
			fullScreenOffButton.pressState = 0;
		}
		if (screenShakeOnButton.pressState == 2) {
			settings.setScreenShake(true);
			screenShakeOnButton.pressState = 0;
		}
		if (screenShakeOffButton.pressState == 2) {
			settings.setScreenShake(false);
			screenShakeOffButton.pressState = 0;
		}
		if (difficultyNormalButton.pressState == 2) {
			settings.setNormalDifficulty();
			difficultyNormalButton.pressState = 0;
		}
		if (difficultyHardButton.pressState == 2) {
			settings.setHardDifficulty();
			difficultyHardButton.pressState = 0;
		}
		if (difficultyVeteranButton.pressState == 2) {
			settings.setVeteranDifficulty();
			difficultyVeteranButton.pressState = 0;
		}

		if (menuCooldown == 0) {
			if (menuUp) {
				if (hoveredButton == null) {
					hoveredButton = difficultyNormalButton;
				} else if (hoveredButton.up != null) {
					hoveredButton = hoveredButton.up;
				}
				menuCooldown = COOLDOWN;
			} else if (menuDown) {
				if (hoveredButton == null) {
					hoveredButton = volumeSlider;
				} else if (hoveredButton.down != null){
					hoveredButton = hoveredButton.down;
				}
				menuCooldown = COOLDOWN;
			} else if (menuLeft) {
				if (hoveredButton == null) {
					hoveredButton = volumeSlider;
				} else if (hoveredButton instanceof MenuSlider) {
					MenuSlider slider = (MenuSlider) hoveredButton;
					float value = slider.getValue();
					value *= 10;
					value = Math.round(value);
					value -= 1;
					value /= 10;
					slider.setValue(value);
				} else if (hoveredButton.left != null) {
					if (hoveredButton.left == backButton && hoveredButton != backButton) {
						backButton.right = hoveredButton;
					}
					hoveredButton = hoveredButton.left;
				}
				menuCooldown = COOLDOWN;
			} else if (menuRight) {
				if (hoveredButton == null) {
					hoveredButton = volumeSlider;
				} else if (hoveredButton instanceof MenuSlider) {
					MenuSlider slider = (MenuSlider) hoveredButton;
					float value = slider.getValue();
					value *= 10;
					value = Math.round(value);
					value += 1;
					value /= 10;
					slider.setValue(value);
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

		fullScreenOnButton.tint = (settings.isFullscreen()) ? Color.ORANGE : Color.WHITE;
		fullScreenOffButton.tint = (!settings.isFullscreen()) ? Color.ORANGE : Color.WHITE;

		screenShakeOnButton.tint = (settings.isScreenShake()) ? Color.ORANGE : Color.WHITE;
		screenShakeOffButton.tint = (!settings.isScreenShake()) ? Color.ORANGE : Color.WHITE;

		difficultyNormalButton.tint = (settings.isNormalDifficulty() ? Color.ORANGE: Color.WHITE);
		difficultyHardButton.tint = (settings.isHardDifficulty() ? Color.ORANGE: Color.WHITE);
		difficultyVeteranButton.tint = (settings.isVeteranDifficulty() ? Color.ORANGE: Color.WHITE);
		backButton.tint = (backButton.pressState == 1 ? Color.GRAY: Color.WHITE);
		controlsButton.tint = (controlsButton.pressState == 1 ? Color.ORANGE: Color.WHITE);

		for (MenuButton button : buttons) {
			canvas.draw(button.texture, button.tint, button.hitbox.x, button.hitbox.y, button.hitbox.width, button.hitbox.height);
		}

		for (MenuSlider slider : sliders) {
			slider.draw(canvas);
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
				if(isFromMainMenu){listener.exitScreen(this, ExitCode.MAIN_MENU);}
				else{ listener.exitScreen(this, ExitCode.PAUSE);}
			}
			if (controlsButton.pressState == 2 && listener != null) {
				listener.exitScreen(this, controlsButton.exitCode);
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
		for (MenuSlider slider : sliders) {
			slider.pressState = 0;
		}
		hoveredButton = volumeSlider;
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

//		screenSizeButtonHitbox.setSize(BUTTON_SCALE *scale*screenSizeButton.getWidth(),BUTTON_SCALE * scale * screenSizeButton.getHeight());
//		screenSizeButtonHitbox.setCenter(canvas.getWidth()/2.0f, centerY + screenSizeButton.getHeight()*scale *0.05f);

		float backButtonScale = 8 * scale;
		backButton.hitbox.setSize(10 * backButtonScale, 9 * backButtonScale);
		backButton.hitbox.setPosition(5 * backButtonScale, canvas.getHeight() - 12 * backButtonScale);
		backButton.texture.setRegion(5, 4, 10, 9);

		controlsButton.hitbox.setSize(controlsButton.texture.getRegionWidth() * scale, controlsButton.texture.getRegionHeight() * scale);
		controlsButton.hitbox.setPosition(canvas.getWidth() * 0.8f, canvas.getHeight() - 96 * scale);

		fullScreenOnButton.hitbox.setSize(fullScreenOnButton.texture.getRegionWidth() * scale, fullScreenOnButton.texture.getRegionHeight() * scale);
		fullScreenOnButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.33f);

		fullScreenOffButton.hitbox.setSize(fullScreenOffButton.texture.getRegionWidth() * scale, fullScreenOffButton.texture.getRegionHeight() * scale);
		fullScreenOffButton.hitbox.setPosition(canvas.getWidth() / 1.7f, canvas.getHeight() * 0.33f);

		screenShakeOnButton.hitbox.setSize(screenShakeOnButton.texture.getRegionWidth() * scale, screenShakeOnButton.texture.getRegionHeight() * scale);
		screenShakeOnButton.hitbox.setPosition(canvas.getWidth() / 1.9f, canvas.getHeight() * 0.25f);

		screenShakeOffButton.hitbox.setSize(screenShakeOffButton.texture.getRegionWidth() * scale, screenShakeOffButton.texture.getRegionHeight() * scale);
		screenShakeOffButton.hitbox.setPosition(canvas.getWidth() / 1.7f, canvas.getHeight() * 0.25f);

		difficultyNormalButton.hitbox.setSize(difficultyNormalButton.texture.getRegionWidth() * scale, difficultyNormalButton.texture.getRegionHeight() * scale);
		difficultyNormalButton.hitbox.setPosition((canvas.getWidth() / 1.9f), canvas.getHeight() * 0.16f);

		difficultyHardButton.hitbox.setSize(difficultyHardButton.texture.getRegionWidth() * scale, difficultyHardButton.texture.getRegionHeight() * scale);
		difficultyHardButton.hitbox.setPosition((canvas.getWidth() / 1.5f), canvas.getHeight() * 0.16f);

		difficultyVeteranButton.hitbox.setSize(difficultyVeteranButton.texture.getRegionWidth() * scale, difficultyVeteranButton.texture.getRegionHeight() * scale);
		difficultyVeteranButton.hitbox.setPosition((canvas.getWidth() / 1.3f), canvas.getHeight() * 0.16f);

		//slider sizing

		volumeSlider.unfilledRect.setSize(canvas.getWidth() * 0.3f, unfilledBar.getHeight() / (float) unfilledBar.getWidth() * canvas.getWidth() * 0.3f);
		volumeSlider.unfilledRect.setPosition( canvas.getWidth() / 1.9f, canvas.getHeight() * 0.67f);

		volumeSlider.hitbox.setSize(volumeSlider.unfilledRect.height * 8.0f/6.0f);
		volumeSlider.hitbox.setCenter(volumeSlider.unfilledRect.x + volumeSlider.unfilledRect.width * volumeSlider.getValue(), volumeSlider.unfilledRect.y + volumeSlider.unfilledRect.height/2);

		musicSlider.unfilledRect.setSize(canvas.getWidth() * 0.3f, unfilledBar.getHeight() / (float) unfilledBar.getWidth() * canvas.getWidth() * 0.3f);
		musicSlider.unfilledRect.setPosition( canvas.getWidth() / 1.9f, canvas.getHeight() * 0.586f);

		musicSlider.hitbox.setSize(musicSlider.unfilledRect.height * 8.0f/6.0f);
		musicSlider.hitbox.setCenter(musicSlider.unfilledRect.x + musicSlider.unfilledRect.width * musicSlider.getValue(), musicSlider.unfilledRect.y + musicSlider.unfilledRect.height/2);

		sfxSlider.unfilledRect.setSize(canvas.getWidth() * 0.3f, unfilledBar.getHeight() / (float) unfilledBar.getWidth() * canvas.getWidth() * 0.3f);
		sfxSlider.unfilledRect.setPosition( canvas.getWidth() / 1.9f, canvas.getHeight() * 0.503f);

		sfxSlider.hitbox.setSize(sfxSlider.unfilledRect.height * 8.0f/6.0f);
		sfxSlider.hitbox.setCenter(sfxSlider.unfilledRect.x + sfxSlider.unfilledRect.width * sfxSlider.getValue(), sfxSlider.unfilledRect.y + sfxSlider.unfilledRect.height/2);

		brightnessSlider.unfilledRect.setSize(canvas.getWidth() * 0.3f, unfilledBar.getHeight() / (float) unfilledBar.getWidth() * canvas.getWidth() * 0.3f);
		brightnessSlider.unfilledRect.setPosition( canvas.getWidth() / 1.9f, canvas.getHeight() * 0.42f);

		brightnessSlider.hitbox.setSize(brightnessSlider.unfilledRect.height * 8.0f/6.0f);
		brightnessSlider.hitbox.setCenter(brightnessSlider.unfilledRect.x + brightnessSlider.unfilledRect.width * brightnessSlider.getValue(), brightnessSlider.unfilledRect.y + brightnessSlider.unfilledRect.height/2);

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

		if (backButton.pressState == 2) {
			return true;
		}

		// Flip to match graphics coordinates
		screenY = heightY-screenY;



//		if (backButton.hitbox.contains(screenX, screenY)) {
//			backButton.pressState = 1;
//			return true;
//		}
//		if(difficultyNormalButton.hitbox.contains(screenX, screenY)){
//			difficultyNormalButton.pressState = 1;
////			level.settingsChanged = true;
////			level.getPlayer().setHearts(5);
//			return true;
//		}
//		if(difficultyHardButton.hitbox.contains(screenX, screenY)){
//			difficultyHardButton.pressState = 1;
////			level.settingsChanged = true;
////			level.getPlayer().setHearts(4);
//			return true;
//		}
//		if(difficultyVeteranButton.hitbox.contains(screenX, screenY)){
//			difficultyVeteranButton.pressState = 1;
////			level.settingsChanged = true;
////			level.getPlayer().setHearts(3); //TODO fix
//			return true;
//		}
//		if (fullScreenOnButton.hitbox.contains(screenX, screenY)) {
//			fullScreenOnButton.pressState = 1;
//		}
//		if (fullScreenOffButton.hitbox.contains(screenX, screenY)) {
//			fullScreenOffButton.pressState = 1;
//		}
		for (MenuButton button : buttons) {
			if (button.hitbox.contains(screenX, screenY) && button.pressState == 0) {
				button.pressState = 1;
			}
		}
		for (MenuSlider slider : sliders) {
			if (slider.hitbox.contains(screenX, screenY) && slider.pressState == 0) {
				slider.pressState = 1;
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
			if (button.pressState == 1) {
				if (button.hitbox.contains(screenX, screenY)) {
					button.pressState = 2;
					return true;
				} else {
					button.pressState = 0;
				}
			}
		}

		for (MenuSlider slider : sliders) {
			if (slider.hitbox.contains(screenX, screenY) && slider.pressState == 1) {
				slider.pressState = 0;
				return true;
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
//		System.out.println("screenX is: " + screenX);
//		System.out.println("screenY is: " + screenY);
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
		for (MenuSlider slider : sliders) {
			if (slider.hitbox.contains(screenX, screenY)) {
				hoveredButton = slider;
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
		if (!active) return false;
		if (!(hoveredButton instanceof MenuSlider)) return false;
		if (hoveredButton.pressState != 1) return false;

		MenuSlider slider = (MenuSlider) hoveredButton;
		float value = (screenX - slider.unfilledRect.x) / slider.unfilledRect.width;
		slider.setValue(value);
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