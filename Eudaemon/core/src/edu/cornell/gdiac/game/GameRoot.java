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
	private DeathScreen deathScreen;
	private GameCanvas canvas;
	private AssetDirectory assets;
	private Sound backgroundDroneSound;

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
		levelScreen.dispose();
		loadingScreen.dispose();
		deathScreen.dispose();
		assets.unloadAssets();
		assets.dispose();
		assets = null;
		canvas.dispose();
		state.dispose();
		super.dispose();
	}


	@Override
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loadingScreen) {
			assets = loadingScreen.getAssets();
			state = new GameState(assets);

			if (levelScreen != null) levelScreen.dispose();
			this.levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());
			levelScreen.setScreenListener(this);
			levelScreen.setCanvas(canvas);

			if (this.deathScreen != null) this.deathScreen.dispose();
			this.deathScreen = new DeathScreen("assets.json", canvas);
			this.deathScreen.setScreenListener(this);


			setScreen(levelScreen);
//			backgroundDroneSound = Gdx.audio.newSound(Gdx.files.internal("audio/temp-background-drone.mp3"));
//			backgroundDroneSound.loop();
		}

		if (screen == deathScreen) {
			if (exitCode == ExitCode.RESET) {
				this.state.resetCurrentLevel();
				if (levelScreen != null) levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				setScreen(levelScreen);
			}
		}

		if (screen == levelScreen) {
			if (exitCode == ExitCode.RESET) {
				levelScreen.pause();

				this.state.resetCurrentLevel();

				levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				setScreen(levelScreen);
			}
			if (exitCode == ExitCode.LOSE) {
				levelScreen.pause();

				setScreen(deathScreen);
			}

			if (exitCode == ExitCode.WIN) {
				screen.pause();
				levelScreen.dispose();

				this.state.setCurrentLevel(state.getCurrentLevel().getExit().getNextLevel());
				this.state.resetCurrentLevel();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());

				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				setScreen(levelScreen);
			}
		}
	}
}
