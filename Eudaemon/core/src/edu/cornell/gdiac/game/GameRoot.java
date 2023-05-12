package edu.cornell.gdiac.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.GameState;
import edu.cornell.gdiac.util.ScreenListener;

public class GameRoot extends Game implements ScreenListener {

	private GameState state;
	private LevelScreen levelScreen;
	private LoadingScreen loadingScreen;
	private MainMenuScreen mainMenuScreen;
	private LevelSelectScreen levelSelectScreen;
	private DeathScreen deathScreen;
	private EscapeMenu escapeMenu;
	private SettingsScreen settingsScreen;
	private GameCanvas canvas;
	private AssetDirectory assets;
	private Sound backgroundMomoSound;
	private Sound backgroundChiyoSound;
	private AudioController audio;
	private int levelDifficulty;

	@Override
	public void create() {
		this.canvas = new GameCanvas();
		this.loadingScreen = new LoadingScreen("assets.json", canvas);
		this.loadingScreen.setScreenListener(this);
		setScreen(loadingScreen);
	}

	@Override
	public void dispose() {
		setScreen(null);
		if (levelScreen != null) levelScreen.dispose();
		if (loadingScreen != null) loadingScreen.dispose();
		if (deathScreen != null) deathScreen.dispose();
		if(escapeMenu!=null) escapeMenu.dispose();
		if(settingsScreen!=null) settingsScreen.dispose();
		if (assets != null) {
			assets.unloadAssets();
			assets.dispose();
			assets = null;
		}
		if (canvas != null)	canvas.dispose();
		if (state != null) state.dispose();
		if (backgroundMomoSound != null) backgroundMomoSound.dispose();
		if (audio!=null) audio.dispose();
		super.dispose();
	}


	@Override
	public void exitScreen(Screen screen, int exitCode) {
		System.out.println(screen);
		System.out.println(exitCode);

		if (exitCode == ExitCode.QUIT) {
			Gdx.app.exit();
			System.exit(0);
		}

		if (screen == loadingScreen) {
			assets = loadingScreen.getAssets();
			state = new GameState(assets);

			this.levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
			levelScreen.setScreenListener(this);
			levelScreen.setCanvas(canvas);
			levelScreen.getLevel().setDifficulty(this.levelDifficulty);

			this.deathScreen = new DeathScreen(assets, canvas);
			this.deathScreen.setScreenListener(this);

			this.levelSelectScreen = new LevelSelectScreen(assets, state, canvas, state.getSettings());
			this.levelSelectScreen.setScreenListener(this);

			this.mainMenuScreen = new MainMenuScreen(assets, canvas);
			mainMenuScreen.setScreenListener(this);

			this.escapeMenu = new EscapeMenu(assets, canvas);
			this.escapeMenu.setScreenListener(this);

			this.settingsScreen = new SettingsScreen(assets, canvas, levelScreen.getLevel());
			this.settingsScreen.setScreenListener(this);

			setScreen(mainMenuScreen);
		}

		if (screen == mainMenuScreen) {

			if (exitCode == ExitCode.START) {
				state.resetCurrentLevel();
				levelScreen.dispose();
				levelScreen = new LevelScreen(state.getCurrentLevel(), state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			} else if (exitCode == ExitCode.LEVEL_SELECT) {
				screen.pause();
				levelSelectScreen.reset();
				setScreen(levelSelectScreen);
			}else if(exitCode == ExitCode.SETTINGS){
				screen.pause();
				settingsScreen.reset();
				setScreen(settingsScreen);
				settingsScreen.setIsFromMainMenu(true);
			}
		}

		if (screen == deathScreen) {
			if (exitCode == ExitCode.RESET) {
				this.state.resetCurrentLevel();
				if (levelScreen != null) levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			}
			if (exitCode == ExitCode.MAIN_MENU) {
				screen.pause();
				mainMenuScreen.reset();
				setScreen(mainMenuScreen);
			}
		}

		if (screen == levelSelectScreen) {
			if (exitCode == ExitCode.MAIN_MENU) {
				screen.pause();
				mainMenuScreen.reset();
				setScreen(mainMenuScreen);
			}
			if (exitCode == ExitCode.START) {
				screen.pause();
				this.state.setCurrentLevel(levelSelectScreen.getSelectedLevel());

				this.state.resetCurrentLevel();

				levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			}
		}

		if (screen == levelScreen) {
			if (exitCode == ExitCode.MAIN_MENU) { // should probably refactor exit codes with a diagram
				levelScreen.pause();

				mainMenuScreen.reset();
				setScreen(mainMenuScreen);
			}
			if(exitCode == ExitCode.PAUSE){
				levelScreen.pause();
				escapeMenu.reset();
				setScreen(escapeMenu);
			}

			if (exitCode == ExitCode.RESET) {
				levelScreen.pause();

				this.state.resetCurrentLevel();

				levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				setScreen(levelScreen);
			}
			if (exitCode == ExitCode.LOSE) {
				levelScreen.pause();
				levelScreen.dispose();
				deathScreen.reset();
				setScreen(deathScreen);
			}

			if (exitCode == ExitCode.WIN) {
				screen.pause();
				levelScreen.dispose();
				state.unlockNextLevel();
				state.save();
				this.state.setCurrentLevel(state.getCurrentLevel().getExit().getNextLevel());
				this.state.resetCurrentLevel();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			}
		}
		if(screen == escapeMenu){
			if (exitCode == ExitCode.RESET) {
				this.state.resetCurrentLevel();
				if (levelScreen != null) levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			}
			if (exitCode == ExitCode.MAIN_MENU) {
				escapeMenu.pause();
				mainMenuScreen.reset();
				setScreen(mainMenuScreen);
			}
			if(exitCode == ExitCode.START && levelScreen.getLevel().isSettingsChanged()){
				escapeMenu.pause();
				this.state.resetCurrentLevel();
				if (levelScreen != null) levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings(), assets);
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			}
			if(exitCode == ExitCode.START && !levelScreen.getLevel().isSettingsChanged()){
				escapeMenu.pause();
				levelScreen.resume();
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				setScreen(levelScreen);
			}
			if(exitCode == ExitCode.SETTINGS){
				escapeMenu.pause();
				settingsScreen.reset();
				setScreen(settingsScreen);
				levelScreen.getLevel().setDifficulty(this.levelDifficulty);
				settingsScreen.setIsFromMainMenu(false);
			}
		}
		if(screen == settingsScreen){
			this.levelDifficulty = settingsScreen.getCurrentDifficulty();
			if(exitCode == ExitCode.PAUSE){
				System.out.println("Alright");
				screen.pause();
				escapeMenu.reset();
				setScreen(escapeMenu);
			}
			if (exitCode == ExitCode.MAIN_MENU) {
				screen.pause();
				mainMenuScreen.reset();
				setScreen(mainMenuScreen);
			}
		}
	}
}
