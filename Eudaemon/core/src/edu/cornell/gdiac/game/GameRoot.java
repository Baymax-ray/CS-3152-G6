package edu.cornell.gdiac.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.GameState;
import edu.cornell.gdiac.util.ScreenListener;

public class GameRoot extends Game implements ScreenListener {

	private GameState state;
	private LevelScreen levelScreen;
	private GameCanvas canvas;

	@Override
	public void create() {
		this.canvas = new GameCanvas();
		this.state = loadState();
		this.levelScreen = new LevelScreen(this.state.getLevel(0), this.state.getActionBindings());
		levelScreen.setScreenListener(this);
		levelScreen.setCanvas(canvas);
		setScreen(levelScreen);
	}

	private GameState loadState() {
		AssetDirectory assets = new AssetDirectory("assets.json");
		assets.loadAssets();
		assets.finishLoading(); // TODO: add loading screen
		return new GameState(assets);
	}

	@Override
	public void exitScreen(Screen screen, int exitCode) {

	}
}
