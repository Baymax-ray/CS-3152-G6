/*
 * RocketWorldController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.rocket;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Gameplay specific controller for the rocket lander game.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class RocketController extends WorldController implements ContactListener {
	/** Texture assets for the rocket */
	private TextureRegion rocketTexture;
	/** Texture filmstrip for the main afterburner */
	private FilmStrip mainTexture;
	/** Texture filmstrip for the main afterburner */
	private FilmStrip leftTexture;
	/** Texture filmstrip for the main afterburner */
	private FilmStrip rghtTexture;

	/** The sounds for collisions */
	private Sound bumpSound;
	/** The ids of the active collisions sounds */
	private final Queue<Long> bumpIds;
	private int maxBumps;
	/** Volume for bumping */
	private float bumpVol;
	/** Threshold for generating sound on collision */
	private float bumpThresh;

	/** The sound for the main afterburner */
	private Sound burnSound;
	/** The sound for the left afterburner */
	private Sound leftSound;
	/** The sound for the right afterburner */
	private Sound rghtSound;
	/** Volume for afterburners */
	private float burnVol;

	/** Texture assets for the crates */
	private TextureRegion[] crateTextures;

	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;
	/** Reference to the rocket/player avatar */
	private RocketModel rocket;

	/**
	 * Creates and initialize a new instance of the rocket lander game
	 *
	 * The game has default gravity and other settings
	 */
	public RocketController() {
		bumpIds = new Queue<Long>();
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
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
		constants  = directory.getEntry( "rocket:constants", JsonValue.class );
		crateTextures = new TextureRegion[constants.get("crates").getInt("textures",0)];
		for (int ii = 0; ii < crateTextures.length; ii++) {
			crateTextures[ii] = new TextureRegion( directory.getEntry( "rocket:crate0" + (ii + 1), Texture.class ) );
		}
		rocketTexture = new TextureRegion( directory.getEntry( "rocket:rocket", Texture.class ) );
		mainTexture  = directory.getEntry( "rocket:main.fire", FilmStrip.class );
		leftTexture  = directory.getEntry( "rocket:left.fire", FilmStrip.class );
		rghtTexture  = directory.getEntry( "rocket:right.fire", FilmStrip.class );

		bumpSound  = directory.getEntry( "rocket:bump", Sound.class );
		burnSound  = directory.getEntry( "rocket:mainburn", Sound.class );
		leftSound  = directory.getEntry( "rocket:leftburn", Sound.class );
		rghtSound = directory.getEntry( "rocket:rightburn", Sound.class );
		super.gatherAssets(directory);
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

		// Create ground pieces
		PolygonObstacle obj;
		JsonValue walljv = constants.get("walls");
		JsonValue defaults = constants.get("defaults");
		obj = new PolygonObstacle(walljv.get(0).asFloatArray(), 0, 0);
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(defaults.getFloat( "density", 0 ));
		obj.setFriction(defaults.getFloat( "friction", 0 ));
		obj.setRestitution(defaults.getFloat( "restitution", 0 ));
		obj.setDrawScale(scale);
		obj.setTexture(earthTile);
		obj.setName("wall1");
		addObject(obj);

		obj = new PolygonObstacle(walljv.get(1).asFloatArray(), 0, 0);
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(defaults.getFloat( "density", 0 ));
		obj.setFriction(defaults.getFloat( "friction", 0 ));
		obj.setRestitution(defaults.getFloat( "restitution", 0 ));
		obj.setDrawScale(scale);
		obj.setTexture(earthTile);
		obj.setName("wall2");
		addObject(obj);

		obj = new PolygonObstacle(walljv.get(2).asFloatArray(), 0, 0);
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(defaults.getFloat( "density", 0 ));
		obj.setFriction(defaults.getFloat( "friction", 0 ));
		obj.setRestitution(defaults.getFloat( "restitution", 0 ));
		obj.setDrawScale(scale);
		obj.setTexture(earthTile);
		obj.setName("wall3");
		addObject(obj);

		// Create the pile of boxes
		JsonValue boxjv = constants.get("boxes");
		JsonValue crates = constants.get("crates");
		for (int ii = 0; ii < boxjv.size; ii += 2) {
			int id = RandomController.rollInt(0,crateTextures.length-1);
			TextureRegion texture = crateTextures[id];
			dwidth  = texture.getRegionWidth()/scale.x;
			dheight = texture.getRegionHeight()/scale.y;
			BoxObstacle box = new BoxObstacle(boxjv.getFloat(ii), boxjv.getFloat(ii+1), dwidth, dheight);
			obj.setDensity(crates.getFloat( "density", 0 ));
			obj.setFriction(crates.getFloat( "friction", 0 ));
			obj.setRestitution(defaults.getFloat( "restitution", 0 ));
			box.setName("crate"+id);
			box.setDrawScale(scale);
			box.setTexture(texture);
			addObject(box);
		}

		// Create the rocket avatar
		dwidth  = rocketTexture.getRegionWidth()/scale.x;
		dheight = rocketTexture.getRegionHeight()/scale.y;
		JsonValue rockjv = constants.get("rocket");
		rocket = new RocketModel(rockjv, dwidth, dheight);
		rocket.setDrawScale(scale);
		rocket.setTexture(rocketTexture);
	    rocket.setBurnerStrip(RocketModel.Burner.MAIN,  mainTexture);
	    rocket.setBurnerStrip(RocketModel.Burner.LEFT,  leftTexture);
	    rocket.setBurnerStrip(RocketModel.Burner.RIGHT, rghtTexture);
	  
	    // Add the sound names
	    rocket.setBurnerSound(RocketModel.Burner.MAIN,  burnSound);
	    rocket.setBurnerSound(RocketModel.Burner.LEFT,  leftSound);
	    rocket.setBurnerSound(RocketModel.Burner.RIGHT, rghtSound);
		addObject(rocket);

		burnVol = defaults.getFloat("volume", 1);
		bumpVol = constants.get("collisions").getFloat( "volume", 1 );;
		maxBumps = constants.get("collisions").getInt( "maximum", 1 );;
		bumpThresh = constants.get("collisions").getFloat( "threshold", 0 );
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
		
		//#region INSERT CODE HERE
		// Read from the input and add the force to the rocket model
		// Then apply the force using the method you modified in RocketObject
		//Gets the rocket's thrust
		float thrust = rocket.getThrust();

		//Sets the horizontal force according to the thrust
		if (InputController.getInstance().getHorizontal() == 1f){
			rocket.setFX(thrust);
		}
		else if (InputController.getInstance().getHorizontal() == -1f){
			rocket.setFX(-thrust);
		}
		else{
			rocket.setFX(0f);
		}

		//Sets the vertical force according to the thrust
		if (InputController.getInstance().getVertical() == 1f){
			rocket.setFY(thrust);
		}
		else{
			rocket.setFY(0f);
		}

		//Applies the force
		rocket.applyForce();

		//#endregion
		
	    // Animate the three burners
	    updateBurner(RocketModel.Burner.MAIN, rocket.getFY() > 1);
	    updateBurner(RocketModel.Burner.LEFT, rocket.getFX() > 1);
	    updateBurner(RocketModel.Burner.RIGHT, rocket.getFX() < -1);
	}
	
	/**
	 * Updates that animation for a single burner
	 *
	 * This method is here instead of the the rocket model because of our philosophy
	 * that models should always be lightweight.  Animation includes sounds and other
	 * assets that we do not want to process in the model
	 *
	 * @param  burner   The rocket burner to animate
	 * @param  on       Whether to turn the animation on or off
	 */
	private void updateBurner(RocketModel.Burner burner, boolean on) {
	    Sound sound = rocket.getBurnerSound(burner);
	    long soundId = rocket.getBurnerId(burner);
	    if (on) {
	        rocket.animateBurner(burner, true);
	        if (rocket.getBurnerId( burner ) == -1) {
				soundId = sound.loop(burnVol);
				rocket.setBurnerId( burner, soundId );
			}
	    } else {
	        rocket.animateBurner(burner, false);
			rocket.setBurnerId( burner, -1 );
	        sound.stop(soundId);
	    }
	}
	
	/// CONTACT LISTENER METHODS
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
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();

		if( (body1.getUserData() == rocket   && body2.getUserData() == goalDoor) ||
			(body1.getUserData() == goalDoor && body2.getUserData() == rocket)) {
			setComplete(true);
		}
	}
	
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  We do not use it.
	 */ 
	public void endContact(Contact contact) {}
	
	private final Vector2 cache = new Vector2();
	
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	/**
	 * Handles any modifications necessary before collision resolution
	 *
	 * This method is called just before Box2D resolves a collision.  We use this method
	 * to implement sound on contact, using the algorithms outlined similar to those in
	 * Ian Parberry's "Introduction to Game Physics with Box2D".  
	 * 
	 * However, we cannot use the proper algorithms, because LibGDX does not implement 
	 * b2GetPointStates from Box2D.  The danger with our approximation is that we may
	 * get a collision over multiple frames (instead of detecting the first frame), and
	 * so play a sound repeatedly.  Fortunately, the cooldown hack in SoundController
	 * prevents this from happening.
	 *
	 * @param  contact  	The two bodies that collided
	 * @param  oldManifold  The collision manifold before contact
	 */
	public void preSolve(Contact contact, Manifold oldManifold) {
		float speed = 0;

		// Use Ian Parberry's method to compute a speed threshold
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();
		WorldManifold worldManifold = contact.getWorldManifold();
		Vector2 wp = worldManifold.getPoints()[0];
		cache.set(body1.getLinearVelocityFromWorldPoint(wp));
		cache.sub(body2.getLinearVelocityFromWorldPoint(wp));
		speed = cache.dot(worldManifold.getNormal());
		    
		// Play a sound if above threshold (otherwise too many sounds)
		if (speed > bumpThresh) {
			String s1 = ((Obstacle)body1.getUserData()).getName();
			String s2 = ((Obstacle)body2.getUserData()).getName();
			if (s1.equals("rocket") || s1.startsWith("crate")) {
				playBump();
			}
			if (s2.equals("rocket") || s2.startsWith("crate")) {
				playBump();
			}
		}
	}

	/**
	 * Plays a bump sound ensuring we never exceed MAX_BUMPS.
	 *
	 * The longest playing sound is evicted if necessary. To see why this
	 * is needed, make MAX_BUMPS large and watch it blow your speakers.
	 */
	public void playBump() {
		if (bumpIds.size >= maxBumps) {
			long oldest = bumpIds.removeFirst();
			bumpSound.stop(oldest);
		}
		long newest = bumpSound.play(bumpVol);
		bumpIds.addLast( newest );
	}

	/**
	 * Called when the Screen is paused.
	 *
	 * We need this method to stop all sounds when we pause.
	 * Pausing happens when we switch game modes.
	 */
	public void pause() {
		for(long id : bumpIds) {
			bumpSound.stop(id);
		}
		// THIS IS CRITICAL. THE AFTERBURNER IS ON A LOOP.
		// IF YOU DO NOT STOP IT THE AFTERBURNER WILL PLAY FOREVER!
		Sound sound;
		long sid;
		sound = rocket.getBurnerSound( RocketModel.Burner.MAIN );
		sid = rocket.getBurnerId( RocketModel.Burner.MAIN );
		if (sid != -1) {
			sound.stop(sid);
		}
		sound = rocket.getBurnerSound( RocketModel.Burner.LEFT );
		sid = rocket.getBurnerId( RocketModel.Burner.LEFT );
		if (sid != -1) {
			sound.stop(sid);
		}
		sound = rocket.getBurnerSound( RocketModel.Burner.RIGHT );
		sid = rocket.getBurnerId( RocketModel.Burner.RIGHT );
		if (sid != -1) {
			sound.stop(sid);
		}
	}
}