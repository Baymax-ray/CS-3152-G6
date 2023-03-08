/*
 * RopeBridge.java
 *
 * The class is a classic example of how to subclass ComplexPhysicsObject.
 * You have to implement the createJoints() method to stick in all of the
 * joints between objects.
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.game.obstacle;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.obstacle.*;

/**
 * A bridge with planks connected by revolute joints.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class RopeBridge extends ComplexObstacle {
	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	// Invisible anchor objects
	/** The left side of the bridge */
	private WheelObstacle start = null;
	/** The right side of the bridge */
	private WheelObstacle finish = null;

	// Dimension information
	/** The size of the entire bridge */
	protected Vector2 dimension;
	/** The size of a single plank */
	protected Vector2 planksize;
	/* The length of each link */
	protected float linksize = 1.0f;
	/** The spacing between each link */
	protected float spacing = 0.0f;

	/**
	 * Creates a new rope bridge with the given physics data
	 *
	 * This bridge is straight horizontal. The coordinates given are the 
	 * position of the leftmost anchor.
	 *
	 * @param data  	The physics constants for this rope bridge
	 * @param lwidth	The plank length
	 * @param lheight	The bridge thickness
	 */
	public RopeBridge(JsonValue data, float lwidth, float lheight) {
		super(data.get("pos").getFloat(0),data.get("pos").getFloat(1));
		setName("bridge");
		this.data = data;

		float x0 = data.get("pos").getFloat(0);
		float y0 = data.get("pos").getFloat(1);
		planksize = new Vector2(lwidth,lheight);
		linksize = planksize.x;
		
	    // Compute the bridge length
		dimension = new Vector2(data.getFloat("width",0),data.getFloat("height",0));
	    float length = dimension.len();
	    Vector2 norm = new Vector2(dimension);
	    norm.nor();
	    
	    // If too small, only make one plank.
	    int nLinks = (int)(length / linksize);
	    if (nLinks <= 1) {
	        nLinks = 1;
	        linksize = length;
	        spacing = 0;
	    } else {
	        spacing = length - nLinks * linksize;
	        spacing /= (nLinks-1);
	    }
	    	    
	    // Create the planks
	    planksize.x = linksize;
	    Vector2 pos = new Vector2();
	    for (int ii = 0; ii < nLinks; ii++) {
	        float t = ii*(linksize+spacing) + linksize/2.0f;
	        pos.set(norm);
	        pos.scl(t);
	        pos.add(x0,y0);
	        BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
	        plank.setName("plank"+ii);
	        plank.setDensity(data.getFloat("density",0));
	        bodies.add(plank);
	    }
	}

	/**
	 * Creates the joints for this object.
	 * 
	 * This method is executed as part of activePhysics. This is the primary method to 
	 * override for custom physics objects.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world) {
		assert bodies.size > 0;
		
		Vector2 anchor1 = new Vector2(); 
		Vector2 anchor2 = new Vector2(-linksize / 2, 0);
		
		// Create the leftmost anchor
		// Normally, we would do this in constructor, but we have
		// reasons to not add the anchor to the bodies list.
		Vector2 pos = bodies.get(0).getPosition();
		pos.x -= linksize / 2;
		start = new WheelObstacle(pos.x,pos.y,data.getFloat("pin_radius", 1));
		start.setName("pin0");
		start.setDensity(data.getFloat("density", 0));
		start.setBodyType(BodyDef.BodyType.StaticBody);
		start.activatePhysics(world);

		// Definition for a revolute joint
		RevoluteJointDef jointDef = new RevoluteJointDef();

		// Initial joint
		jointDef.bodyA = start.getBody();
		jointDef.bodyB = bodies.get(0).getBody();
		jointDef.localAnchorA.set(anchor1);
		jointDef.localAnchorB.set(anchor2);
		jointDef.collideConnected = false;
		Joint joint = world.createJoint(jointDef);
		joints.add(joint);

		// Link the planks together
		anchor1.x = linksize / 2;
		for (int ii = 0; ii < bodies.size-1; ii++) {
			//#region INSERT CODE HERE
			// Look at what we did above
			Body body1 = bodies.get(ii).getBody();
			Body body2 = bodies.get(ii+1).getBody();
			RevoluteJointDef jD = new RevoluteJointDef();
			jD.initialize(body1, body2, body1.getWorldCenter().add(0.5f, 0.0f));
			RevoluteJoint rJD = (RevoluteJoint)world.createJoint(jD);

			//#endregion
		}

		// Create the rightmost anchor
		Obstacle last = bodies.get(bodies.size-1);
		
		pos = last.getPosition();
		pos.x += linksize / 2;
		finish = new WheelObstacle(pos.x,pos.y,data.getFloat("pin_radius", 1));
		finish.setName("pin1");
		finish.setDensity(data.getFloat("density", 0));
		finish.setBodyType(BodyDef.BodyType.StaticBody);
		finish.activatePhysics(world);

		// Final joint
		anchor2.x = 0;
		jointDef.bodyA = last.getBody();
		jointDef.bodyB = finish.getBody();
		jointDef.localAnchorA.set(anchor1);
		jointDef.localAnchorB.set(anchor2);
		joint = world.createJoint(jointDef);
		joints.add(joint);
				
		return true;
	}
	
	/**
	 * Destroys the physics Body(s) of this object if applicable,
	 * removing them from the world.
	 * 
	 * @param world Box2D world that stores body
	 */
	public void deactivatePhysics(World world) {
		super.deactivatePhysics(world);
		if (start != null) {
			start.deactivatePhysics(world);
		}
		if (finish != null) {
			finish.deactivatePhysics(world);
		}
	}
	
	/**
	 * Sets the texture for the individual planks
	 *
	 * @param texture the texture for the individual planks
	 */
	public void setTexture(TextureRegion texture) {
		for(Obstacle body : bodies) {
			((SimpleObstacle)body).setTexture(texture);
		}
	}
	
	/**
	 * Returns the texture for the individual planks
	 *
	 * @return the texture for the individual planks
	 */
	public TextureRegion getTexture() {
		if (bodies.size == 0) {
			return null;
		}
		return ((SimpleObstacle)bodies.get(0)).getTexture();
	}
}