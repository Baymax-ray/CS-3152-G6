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
			setScreen(levelScreen);
			backgroundDroneSound = Gdx.audio.newSound(Gdx.files.internal("audio/temp-background-drone.mp3"));
			backgroundDroneSound.loop();
		}

		if (screen instanceof LevelScreen) {
			if (exitCode == ExitCode.RESET || exitCode == ExitCode.LOSE) { // TODO: Separate LOSE and RESET effects
				screen.pause();
				this.state.resetCurrentLevel();
				setScreen(null);
				levelScreen.dispose();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());

				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				setScreen(levelScreen);
			}

			if (exitCode == ExitCode.WIN) {
				screen.pause();
				setScreen(null);
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
