package edu.cornell.gdiac.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.game.models.Level;
import edu.cornell.gdiac.game.obstacle.Obstacle;

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


        try {
            if ((body1.getUserData().toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getSensorName())) ||
                    (body2.getUserData().toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getSensorName()))) {
                level.getPlayer().setGrounded(true);
                sensorFixtures.add(level.getPlayer() == body1.getUserData() ? fix2 : fix1);
            }
        } catch (Exception e) {

        }




    }

    @Override
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();



        if ((body1.getUserData().toString().contains("Tile") && level.getPlayer() != body1.getUserData()) ||
                (body2.getUserData().toString().contains("Tile") && level.getPlayer() != body2.getUserData())) {
            System.out.println(sensorFixtures.size);
            sensorFixtures.remove(level.getPlayer() == body1.getUserData() ? fix2 : fix1);
            System.out.println(sensorFixtures.size);
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
