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
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getSensorName())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getSensorName()))) {
                level.getPlayer().setGrounded(true);
                sensorFixtures.add(level.getPlayer() == bd1 ? fix2 : fix1);
            }
        } catch (Exception e) { }


        //#region Cancel ALL collision between player and enemy
        if(bd1 instanceof Enemy || bd1 instanceof Player){

            //System.out.println("YES1, hi");
            //System.out.println(((Enemy) bd1).getType().equals("FlyGuardian"));
            Fixture fixture1 = fix1;
            Filter filter1 = fixture1.getFilterData();
            filter1.groupIndex = -1;
            //one solution is to simply set the group index of flying enemies to 1, meaning that they always collide
            //with anything.
//            if(bd1 instanceof Enemy &&
//                    (((Enemy) bd1).getType().equals( "FlyGuardian") || ((Enemy) bd1).getType().equals( "Fly"))){
//            filter1.groupIndex = 1;
//            }
            fixture1.setFilterData(filter1);
        }else if (bd2 instanceof Enemy || bd2 instanceof Player) {
            //System.out.println("YES2, hi");
            //System.out.println(((Enemy) bd2).getType() == "FlyGuardian");
            Fixture fixture1 = fix2;
            Filter filter1 = fixture1.getFilterData();
            filter1.groupIndex = -1;
//            if(bd2 instanceof Enemy &&
//                    (((Enemy) bd2).getType().equals( "FlyGuardian") || ((Enemy) bd2).getType().equals( "Fly"))){
//                filter1.groupIndex = 1;
//            }
            fixture1.setFilterData(filter1);
        }
        //#endregion


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
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getSensorName())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getSensorName()))) {

                sensorFixtures.removeValue(level.getPlayer() == bd1 ? fix2 : fix1, true);
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

        //#region  Attempt to cancel fly enemies collision (failed, commented out)
        //the following if statement cannot be realized. idk why.
//        if(bd1 instanceof Enemy && bd2 instanceof Enemy){
//
//            System.out.println("flying1, hi!");
//            System.out.println(((Enemy) bd1).getType());
//            System.out.println(((Enemy) bd2).getType());
//            if(
//                    (((Enemy) bd1).getType().equals( "FlyGuardian") || ((Enemy) bd1).getType().equals( "Fly")) &&
//                            (((Enemy) bd2).getType().equals( "FlyGuardian") || ((Enemy) bd2).getType().equals( "Fly"))){
//                System.out.println("flying2, hi!");
//                Fixture fixture1 = fix1;
//
//                Filter filter1 = fixture1.getFilterData();
//                Fixture fixture2 = fix2;
//
//                Filter filter2 = fixture1.getFilterData();
//                filter1.groupIndex = 1;
//                filter2.groupIndex = 1;
//                fixture1.setFilterData(filter1);
//                fixture2.setFilterData(filter2);
//            }
//        }
    //#endregion

        //#region Attempt to cancel player-enemy collision WHEN DASHING
        //the following code is trying to remove the collision between player and enemy when player is dashing.
        //this is not used because now the player should not be colliding with enemies anyways.
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
        //#endregion
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

        //#region Attempt to cancel player-enemy collision WHEN DASHING
//      same as above: dash collision, not used.
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
        //#endregion
    }
}
