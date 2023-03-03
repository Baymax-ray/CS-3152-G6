/*
 * PlatformController.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformController extends WorldController implements ContactListener {

	//<editor-fold desc="TEXTURES">
	/** Texture asset for momo avatar */
	private TextureRegion momoTexture;
	/** Texture asset for momo avatar looking up*/
	private TextureRegion momoUpTexture;
	/** Texture asset for chiyo avatar */
	private TextureRegion chiyoTexture;
	/** Texture asset for chiyo avatar looking up*/
	private TextureRegion chiyoUpTexture;
	/** Texture asset for the spinning barrier */
	private TextureRegion barrierTexture;
	/** Texture asset for the bullet */
	private TextureRegion bulletTexture;
	/** Texture asset for the sword attack */
	private FilmStrip swordSpriteSheet;
	/** Texture asset for the bridge plank */
	private TextureRegion bridgeTexture;
	//</editor-fold>

	//<editor-fold desc="SOUNDS">
	/** The jump sound.  We only want to play once. */
	private Sound jumpSound;
	private long jumpId = -1;
	/** The weapon fire sound.  We only want to play once. */
	private Sound fireSound;
	private long fireId = -1;
	/** The weapon pop sound.  We only want to play once. */
	private Sound plopSound;
	private long plopId = -1;
	/** The default sound volume */
	private float volume;
	//</editor-fold>

	//OTHER VARIABLES
	/** Tick Counter for switching */
	private int switchTicks;
	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Reference to the character avatar */
	private DudeModel avatar;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public PlatformController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		momoTexture = new TextureRegion(directory.getEntry("platform:momo",Texture.class));
		momoUpTexture = new TextureRegion(directory.getEntry("platform:momoUp",Texture.class));
		chiyoTexture = new TextureRegion(directory.getEntry("platform:chiyo",Texture.class));
		chiyoUpTexture = new TextureRegion(directory.getEntry("platform:chiyo" +
				"Up",Texture.class));
		barrierTexture = new TextureRegion(directory.getEntry("platform:barrier",Texture.class));
		bulletTexture = new TextureRegion(directory.getEntry("platform:bullet",Texture.class));
		bridgeTexture = new TextureRegion(directory.getEntry("platform:rope",Texture.class));

		jumpSound = directory.getEntry( "platform:jump", Sound.class );
		fireSound = directory.getEntry( "platform:pew", Sound.class );
		plopSound = directory.getEntry( "platform:plop", Sound.class );

		constants = directory.getEntry( "platform:constants", JsonValue.class );
		super.gatherAssets(directory);

		//ANIMATIONS
		swordSpriteSheet = directory.getEntry( "chiyo:swordAttack", FilmStrip.class );

	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );

		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Add level goal
		float dwidth  = goalTile.getRegionWidth()/scale.x;
		float dheight = goalTile.getRegionHeight()/scale.y;

		JsonValue goal = constants.get("goal");
		JsonValue goalpos = goal.get("pos");
		goalDoor = new BoxObstacle(goalpos.getFloat(0),goalpos.getFloat(1),dwidth,dheight);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setDensity(goal.getFloat("density", 0));
		goalDoor.setFriction(goal.getFloat("friction", 0));
		goalDoor.setRestitution(goal.getFloat("restitution", 0));
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalTile);
		goalDoor.setName("goal");
		addObject(goalDoor);

		String wname = "wall";
		JsonValue walljv = constants.get("walls");
		JsonValue defaults = constants.get("defaults");
		for (int ii = 0; ii < walljv.size; ii++) {
			PolygonObstacle obj;
			obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(defaults.getFloat( "density", 0.0f ));
			obj.setFriction(defaults.getFloat( "friction", 0.0f ));
			obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(wname+ii);
			addObject(obj);
		}

		String pname = "platform";
		JsonValue platjv = constants.get("platforms");
		for (int ii = 0; ii < platjv.size; ii++) {
			PolygonObstacle obj;
			obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(defaults.getFloat( "density", 0.0f ));
			obj.setFriction(defaults.getFloat( "friction", 0.0f ));
			obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(pname+ii);
			addObject(obj);
		}

		// This world is heavier
		world.setGravity( new Vector2(0,defaults.getFloat("gravity",0)) );

		// Create dude
		dwidth  = momoTexture.getRegionWidth()/scale.x;
		dheight = momoTexture.getRegionHeight()/scale.y;
		avatar = new DudeModel(constants.get("dude"), dwidth, dheight);
		avatar.setDrawScale(scale);
		avatar.setTexture(momoTexture);
		addObject(avatar);

		// Create rope bridge
		dwidth  = bridgeTexture.getRegionWidth()/scale.x;
		dheight = bridgeTexture.getRegionHeight()/scale.y;
		RopeBridge bridge = new RopeBridge(constants.get("bridge"), dwidth, dheight);
		bridge.setTexture(bridgeTexture);
		bridge.setDrawScale(scale);
		addObject(bridge);

		// Create spinning platform
		dwidth  = barrierTexture.getRegionWidth()/scale.x;
		dheight = barrierTexture.getRegionHeight()/scale.y;
		Spinner spinPlatform = new Spinner(constants.get("spinner"),dwidth,dheight);
		spinPlatform.setDrawScale(scale);
		spinPlatform.setTexture(barrierTexture);
		addObject(spinPlatform);

		volume = constants.getFloat("volume", 1.0f);
	}

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt	Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}

		if (!isFailure() && avatar.getY() < -1) {
			setFailure(true);
			return false;
		}

		return true;
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Process actions in object model
		avatar.setMovement(InputController.getInstance().getHorizontal() *avatar.getForce());
		avatar.setJumping(InputController.getInstance().didPrimary());
		avatar.setShooting(InputController.getInstance().didSecondary());

		// Switch Tick Decrement
		if (switchTicks > 0){
			switchTicks--;
		}

		// Add a bullet if we fire
		if (avatar.getForm() == 1 && avatar.isShooting()) {
			createBullet();
		}

		// Switches form if switch button is pressed
		if (switchTicks == 0 && InputController.getInstance().getSwitch() && avatar.getSpirit() > 0.0f) {
			avatar.setForm();
			if (avatar.getForm() == 0){
				avatar.setTexture(momoTexture);
			}
			else {
				avatar.setTexture(chiyoTexture);
			}
			switchTicks = 60;
		}

		// Changes avatar if looking up
		if (InputController.getInstance().getUp()) {
			avatar.setLookUp(true);
			if (avatar.getForm() == 0){
				avatar.setTexture(momoUpTexture);
			}
			else {
				avatar.setTexture(chiyoUpTexture);
			}
		}
		else {
			if (avatar.isLookUp()){
				avatar.setLookUp(false);
				if (avatar.getForm() == 0){
					avatar.setTexture(momoTexture);
				}
				else {
					avatar.setTexture(chiyoTexture);
				}
			}
		}

		avatar.applyForce();
		if (avatar.isJumping()) {
			jumpId = playSound( jumpSound, jumpId, volume );
		}
	}

	/**
	 * Add a new bullet to the world and send it in the right direction.
	 */
	private void createBullet() {
		JsonValue bulletjv = constants.get("bullet");
		float offset = bulletjv.getFloat("offset",0);
		offset *= (avatar.isFacingRight() ? 1 : -1);
		float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
		SwordWheelObstacle bullet;
		float density = bulletjv.getFloat("density", 0);
		if (avatar.isLookUp()){
			bullet = new SwordWheelObstacle(avatar.getX(), avatar.getY()+ offset, radius, avatar, 1.0f, density, scale, bulletTexture);
		}
		else {
			bullet = new SwordWheelObstacle(avatar.getX()+ offset, avatar.getY(), radius, avatar, 1.0f, density, scale, bulletTexture);
		}

		addQueuedObject(bullet);

		fireId = playSound( fireSound, fireId );
	}

	/**
	 * Remove a new bullet from the world.
	 *
	 * @param  bullet   the bullet to remove
	 */
	public void removeBullet(Obstacle bullet) {
		bullet.markRemoved(true);
		plopId = playSound( plopSound, plopId );
	}


	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		try {
			Obstacle bd1 = (Obstacle)body1.getUserData();
			Obstacle bd2 = (Obstacle)body2.getUserData();

			// Test bullet collision with world
			if (bd1.getName().equals("bullet") && bd2 != avatar) {
				removeBullet(bd1);
			}

			if (bd2.getName().equals("bullet") && bd1 != avatar) {
				removeBullet(bd2);
			}

			// See if we have landed on the ground.
			if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
					(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
				avatar.setGrounded(true);
				sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
			}

			// Check for win condition
			if ((bd1 == avatar   && bd2 == goalDoor) ||
					(bd1 == goalDoor && bd2 == avatar)) {
				setComplete(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
				(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
			if (sensorFixtures.size == 0) {
				avatar.setGrounded(false);
			}
		}
	}

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}

	/**
	 * Called when the Screen is paused.
	 *
	 * We need this method to stop all sounds when we pause.
	 * Pausing happens when we switch game modes.
	 */
	public void pause() {
		jumpSound.stop(jumpId);
		plopSound.stop(plopId);
		fireSound.stop(fireId);
	}
}