/*
 * DesktopLauncher.java
 * 
 * LibGDX is a cross-platform development library. You write all of your code in 
 * the core project.  However, you still need some extra classes if you want to
 * deploy on a specific platform (e.g. PC, Android, Web).  That is the purpose
 * of this class.  It deploys your game on a PC/desktop computer.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.physics.desktop;

import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;
import edu.cornell.gdiac.physics.GDXRoot;

/**
 * The main class of the game.
 * 
 * This class sets the window size and launches the game.  Aside from modifying
 * the window size, you should almost never need to modify this class.
 */
public class DesktopLauncher {

	/**
	 * Classic main method that all Java programmers know.
	 * 
	 * This method simply exists to start a new Lwjgl3Application.  For desktop games,
	 * LibGDX is built on top of LWJGL3 (this is not the case for Android).
	 * 
	 * @param arg Command line arguments
	 */
	public static void main (String[] arg) {
		GDXAppSettings config = new GDXAppSettings();
		config.width  = 1024;
		config.height = 576;
		config.fullscreen = false;
		config.resizable = false;
		new GDXApp(new GDXRoot(), config);
	}
}
