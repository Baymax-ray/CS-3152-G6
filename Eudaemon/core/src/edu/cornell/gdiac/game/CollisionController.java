package edu.cornell.gdiac.game;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.Level;
import edu.cornell.gdiac.game.models.Player;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import java.util.ArrayList;

public class CollisionController implements ContactListener {

    private Level level;
    private Array<Fixture> sensorFixtures;
    public CollisionController(Level level) {
        this.level = level;
        sensorFixtures = new Array<Fixture>();
    }

    public void dispose() {
        this.level = null;
        this.sensorFixtures.clear();
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

        if(bd1 instanceof Enemy || bd1 instanceof Player){
            Fixture fixture1 = fix1;

            Filter filter1 = fixture1.getFilterData();

            filter1.groupIndex = -1;
            fixture1.setFilterData(filter1);
        }else if (bd2 instanceof Enemy || bd2 instanceof Player) {
            Fixture fixture1 = fix2;

            Filter filter1 = fixture1.getFilterData();

            filter1.groupIndex = -1;
            fixture1.setFilterData(filter1);
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

        try {
            if ((body1.getUserData().toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getSensorName())) ||
                    (body2.getUserData().toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getSensorName()))) {

                sensorFixtures.removeValue(level.getPlayer() == body1.getUserData() ? fix2 : fix1, true);
                if (sensorFixtures.size == 0) {
                    level.getPlayer().setGrounded(false);
                }
            }
        } catch (Exception e) {}

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();
//        if((bd1 instanceof Player && bd2 instanceof Enemy && ((Player) bd1).isDashing())||
//                (bd1 instanceof Enemy && bd2 instanceof Player&& ((Player) bd2).isDashing())){
//            // Get the two fixtures that you want to ignore each other
//            Fixture fixture1 = fix1;
//            Fixture fixture2 = fix2;
//
//            // Get the filter data for each fixture
//            Filter filter1 = fixture1.getFilterData();
//            Filter filter2 = fixture2.getFilterData();
//
//            filter1.groupIndex = -1;
//            filter2.groupIndex = -1;
//            // Set the updated filter data for each fixture
//            fixture1.setFilterData(filter1);
//            fixture2.setFilterData(filter2);
//        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        //Intended to solve the situation when player is stuck on wall by a flying enemy sticking to it
        //and cannot escape
        //BUT FAILED.
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();
//
//        if(bd1 instanceof Player && !((Player) bd1).isDashing()){
//            // Get the two fixtures that you want to ignore each other
//            Fixture fixture1 = fix1;
//
//            // Get the filter data for each fixture
//            Filter filter1 = fixture1.getFilterData();
//
//            filter1.groupIndex = 0;
//            // Set the updated filter data for each fixture
//            fixture1.setFilterData(filter1);
//        }else if(bd2 instanceof Player && !((Player) bd2).isDashing()){
//            Fixture fixture1 = fix2;
//
//            // Get the filter data for each fixture
//            Filter filter1 = fixture1.getFilterData();
//
//            filter1.groupIndex = 0;
//            // Set the updated filter data for each fixture
//            fixture1.setFilterData(filter1);
//        }
    }
}
