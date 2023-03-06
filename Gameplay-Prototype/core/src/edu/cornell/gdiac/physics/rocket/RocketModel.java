/*
 * RocketModel.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Note how this class combines physics and animation.  This is a good template
 * for models in your game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.rocket;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the rocket lander game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class RocketModel extends BoxObstacle {
    /**
     * Enumeration to identify the rocket afterburner
     */
    public enum Burner {
        /** The main afterburner */
        MAIN,
        /** The left side thruster */
        LEFT,
        /** The right side thruster */
        RIGHT
    };

	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;
	/** The upward force for the afterburners */
	private final float thrust;

	/** The texture filmstrip for the left animation node */
    private FilmStrip mainBurner;
    /** The associated sound for the main afterburner */
	private Sound mainSound;
    /** The actively playing sound for the main afterburner */
	private long mainId;
    /** The animation phase for the main afterburner */
	private boolean mainCycle = true;

    /** The texture filmstrip for the left animation node */
	private FilmStrip leftBurner;
    /** The associated sound for the left side burner */
	private Sound leftSound;
	/** The actively playing sound for the left side afterburner */
	private long leftId;
    /** The animation phase for the left side burner */
	private boolean leftCycle = true;

    /** The texture filmstrip for the left animation node */
	private FilmStrip rghtBurner;
    /** The associated sound for the right side burner */
	private Sound rghtSound;
	/** The actively playing sound for the right side afterburner */
	private long rghtId;
    /** The associated sound for the right side burner */
	private boolean rghtCycle  = true;

	/** Cache object for the force to apply to this rocket */
	private final Vector2 force = new Vector2();
	/** Cache object for transforming the force according the object angle */
	private final Affine2 affineCache = new Affine2();
	/** Cache object for left afterburner origin */
	private final Vector2 leftOrigin = new Vector2();
	/** Cache object for right afterburner origin */
	private final Vector2 rghtOrigin = new Vector2();
	
	/**
	 * Returns the force applied to this rocket.
	 * 
	 * This method returns a reference to the force vector, allowing it to be modified.
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the force applied to this rocket.
	 */
	public Vector2 getForce() {
		return force;
	}

	/**
	 * Returns the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the x-component of the force applied to this rocket.
	 */
	public float getFX() {
		return force.x;
	}

	/**
	 * Sets the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @param value the x-component of the force applied to this rocket.
	 */
	public void setFX(float value) {
		force.x = value;
	}

	/**
	 * Returns the y-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the y-component of the force applied to this rocket.
	 */
	public float getFY() {
		return force.y;
	}

	/**
	 * Sets the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @param value the x-component of the force applied to this rocket.
	 */
	public void setFY(float value) {
		force.y = value;
	}
	
	/**
	 * Returns the amount of thrust that this rocket has.
	 *
	 * Multiply this value times the horizontal and vertical values in the
	 * input controller to get the force.
	 *
	 * @return the amount of thrust that this rocket has.
	 */
	public float getThrust() {
		return thrust;
	}

	/**
	 * Creates a new rocket with the given physics data.
	 *
	 * The size is expressed in physics units NOT pixels. In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param data  	The physics constants for this rocket
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public RocketModel(JsonValue data, float width, float height) {
		super(data.get("pos").getFloat(0),data.get("pos").getFloat(1),width,height);
		setDensity(data.getFloat( "density", 0.0f ));
		setFriction(data.getFloat( "friction", 0.0f ));
		setRestitution(data.getFloat( "restitution", 0.0f ));
		thrust = data.getFloat( "thrust", 0.0f );
		this.data = data;
		setName("rocket");
		mainId = -1;
		leftId = -1;
		rghtId = -1;
	}
	
	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// Get the box body from our parent class
		if (!super.activatePhysics(world)) {
			return false;
		}
		
		//#region INSERT CODE HERE
		// Insert code here to prevent the body from rotating
//		body.setAngularDamping(1000f);
		body.setFixedRotation(true);
		//#endregion
		
		return true;
	}
	
	
	/**
	 * Applies the force to the body of this ship
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}
		
		// Orient the force with rotation.
		affineCache.setToRotationRad(getAngle());
		affineCache.applyTo(force);
		
		//#region INSERT CODE HERE
		// Apply force to the rocket BODY, not the rocket
		// Stops force if rocket is no being thrust to the right or left

		//Applying a slight air/ground frictional force to stop the force if the rocket is no longer being thrust to the right or left
		if (force.x == 0f && getVX() != 0.0f) {
			force.set(-0.2f*getVX(),force.y);
		}

		//Applying the force
		body.applyForce(force,getPosition(),true);
		//#endregion
	}

	// Animation methods (DO NOT CHANGE)
	/**
	 * Returns the animation node for the given afterburner
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @return the animation node for the given afterburner
	 */
	public FilmStrip getBurnerStrip(Burner burner) {
	    switch (burner) {
	        case MAIN:
	            return mainBurner;
	        case LEFT:
	            return leftBurner;
	        case RIGHT:
	            return rghtBurner;
	    }
	    assert false : "Invalid burner enumeration";
	    return null;
	}

	/**
	 * Sets the animation node for the given afterburner
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @param  strip 	the animation filmstrip for the given afterburner
	 */
	public void setBurnerStrip(Burner burner, FilmStrip strip) {
	    switch (burner) {
	        case MAIN:
	        	mainBurner = strip;
	            break;
	        case LEFT:
	        	leftBurner = strip;
	        	if (strip != null) {
	        		leftOrigin.set(strip.getRegionWidth()/2.0f,strip.getRegionHeight()/2.0f);
	        	}
	            break;
	        case RIGHT:
	        	rghtBurner = strip;
	        	if (strip != null) {
	        		rghtOrigin.set(strip.getRegionWidth()/2.0f,strip.getRegionHeight()/2.0f);
	        	}
	            break;
	        default:
	    	    assert false : "Invalid burner enumeration";
	    }   
	}

	/**
	 * Returns the sound to accompany the given afterburner
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @return the sound to accompany the given afterburner
	 */
	public Sound getBurnerSound(Burner burner) {
	    switch (burner) {
	        case MAIN:
	            return mainSound;
	        case LEFT:
	            return leftSound;
	        case RIGHT:
	            return rghtSound;
	    }
	    assert false : "Invalid burner enumeration";
	    return null;
	}

	/**
	 * Sets the sound to accompany the given afterburner
	 *
	 * @param  burner   enumeration to identify the afterburner
	 * @param  sound   	the sound to accompany the given afterburner
	 */
	public void setBurnerSound(Burner burner, Sound sound) {
	    switch (burner) {
	        case MAIN:
	            mainSound = sound;
	            break;
	        case LEFT:
	            leftSound = sound;
	            break;
	        case RIGHT:
	            rghtSound = sound;
	            break;
	        default:
	    	    assert false : "Invalid burner enumeration";
	    }
	}

	/**
	 * Returns the key of the active sound for the given afterburner
	 *
	 * This method returns -1 if no sound is active.
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @return the key of the active sound for the given afterburner
	 */
	public long getBurnerId(Burner burner) {
		switch (burner) {
			case MAIN:
				return mainId;
			case LEFT:
				return leftId;
			case RIGHT:
				return rghtId;
		}
		assert false : "Invalid burner enumeration";
		return -1;
	}

	/**
	 * Sets the key of the active sound for the given afterburner
	 *
	 * If no sound is active, this should be set to -1.
	 *
	 * @param  burner   enumeration to identify the afterburner
	 * @param  sid   	the id of the actively playing sound
	 */
	public void setBurnerId(Burner burner, long sid) {
		switch (burner) {
			case MAIN:
				mainId = sid;
				break;
			case LEFT:
				leftId = sid;
				break;
			case RIGHT:
				rghtId = sid;
				break;
			default:
				assert false : "Invalid burner enumeration";
		}
	}

	/**
	 * Animates the given burner.
	 *
	 * If the animation is not active, it will reset to the initial animation frame.
	 *
	 * @param  burner   The reference to the rocket burner
	 * @param  on       Whether the animation is active
	 */
	public void animateBurner(Burner burner, boolean on) {
	    FilmStrip node = null;
	    boolean  cycle = true;
	    
	    switch (burner) {
	        case MAIN:
	            node  = mainBurner;
	            cycle = mainCycle;
	            break;
	        case LEFT:
	            node  = leftBurner;
	            cycle = leftCycle;
	            break;
	        case RIGHT:
	            node  = rghtBurner;
	            cycle = rghtCycle;
	            break;
	        default:
	    	    assert false : "Invalid burner enumeration";
	    }
	    
	    if (on) {
	        // Turn on the flames and go back and forth
	        if (node.getFrame() == 0 || node.getFrame() == 1) {
	            cycle = true;
	        } else if (node.getFrame() == node.getSize()-1) {
	            cycle = false;
	        }
	        
	        // Increment
	        if (cycle) {
	            node.setFrame(node.getFrame()+1);
	        } else {
	            node.setFrame(node.getFrame()-1);
	        }
	    } else {
	        node.setFrame(0);
	    }
	    
	    switch (burner) {
        case MAIN:
            mainCycle = cycle;
            break;
        case LEFT:
            leftCycle = cycle;
            break;
        case RIGHT:
            rghtCycle = cycle;
            break;
        default:
    	    assert false : "Invalid burner enumeration";
	    }
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		super.draw(canvas);  // Ship
		// Flames
		if (mainBurner != null) {
			float offsety = mainBurner.getRegionHeight()-origin.y;
			canvas.draw(mainBurner,Color.WHITE,origin.x,offsety,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
		if (leftBurner != null) {
			canvas.draw(leftBurner,Color.WHITE,leftOrigin.x,leftOrigin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
		if (rghtBurner != null) {
			canvas.draw(rghtBurner,Color.WHITE,rghtOrigin.x,rghtOrigin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
	}
}