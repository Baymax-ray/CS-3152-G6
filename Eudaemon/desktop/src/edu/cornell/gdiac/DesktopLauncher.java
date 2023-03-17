package edu.cornell.gdiac;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import edu.cornell.gdiac.game.GameRoot;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1025, 576);
		config.setForegroundFPS(60);
		config.setTitle("Eudaemon");
		new Lwjgl3Application(new GameRoot(), config);
	}
}
