package edu.cornell.gdiac.game;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.Level;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import java.util.ArrayList;

public class CollisionController implements ContactListener {

    Level level;
    protected ObjectSet<Fixture> sensorFixtures;
    public CollisionController(Level level) {
        this.level = level;
        sensorFixtures = new ObjectSet<Fixture>();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        try {
            if ((body1.getUserData().toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getSensorName())) ||
                    (body2.getUserData().toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getSensorName()))) {
                level.getPlayer().setGrounded(true);
                sensorFixtures.add(level.getPlayer() == body1.getUserData() ? fix2 : fix1);
            }
        } catch (Exception e) { }
        if(bd1 instanceof Enemy && bd2 instanceof Enemy ){
            // Get the two fixtures that you want to ignore each other
            Fixture fixture1 = fix1;
            Fixture fixture2 = fix2;

            // Get the filter data for each fixture
            Filter filter1 = fixture1.getFilterData();
            Filter filter2 = fixture2.getFilterData();

            /** Collision groups allow a certain group of objects to never collide (negative) or always collide (positive). Zero means no
             * collision group. Non-zero group filtering always wins against the mask bits. */
            filter1.groupIndex = -1;
            filter2.groupIndex = -1;
            // Set the updated filter data for each fixture
            fixture1.setFilterData(filter1);
            fixture2.setFilterData(filter2);

        }


    }

    @Override
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((body1.getUserData().toString().contains("Tile") && level.getPlayer() != body1.getUserData()) ||
                (body2.getUserData().toString().contains("Tile") && level.getPlayer() != body2.getUserData())) {
            sensorFixtures.remove(level.getPlayer() == body1.getUserData() ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                level.getPlayer().setGrounded(false);
            }

        }


    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
