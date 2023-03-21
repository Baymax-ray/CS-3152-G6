package edu.cornell.gdiac.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.GameState;
import edu.cornell.gdiac.util.ScreenListener;

public class GameRoot extends Game implements ScreenListener {

	private GameState state;
	private LevelScreen levelScreen;
	private LoadingScreen loadingScreen;
	private GameCanvas canvas;
	private AssetDirectory assets;

	@Override
	public void create() {
		this.canvas = new GameCanvas();
		this.loadingScreen = new LoadingScreen("assets.json", canvas);
		this.loadingScreen.setScreenListener(this);
		setScreen(loadingScreen);
	}

	private GameState loadState() {
		AssetDirectory assets = new AssetDirectory("assets.json");
		assets.loadAssets();
		assets.finishLoading(); // TODO: add loading screen
		return new GameState(assets);
	}

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loadingScreen) {
			assets = loadingScreen.getAssets();
			state = new GameState(assets);
			this.levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());
			levelScreen.setScreenListener(this);
			levelScreen.setCanvas(canvas);
			setScreen(levelScreen);
		}

		if (screen instanceof LevelScreen) {
			if (exitCode == ExitCode.RESET || exitCode == ExitCode.LOSE) {
				screen.pause();
				this.state.resetCurrentLevel();
				levelScreen = new LevelScreen(this.state.getCurrentLevel(), this.state.getActionBindings());
				levelScreen.setScreenListener(this);
				levelScreen.setCanvas(canvas);
				setScreen(levelScreen);
			}
		}
	}
}
