package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.game.models.Enemy;
import edu.cornell.gdiac.game.models.Level;
import edu.cornell.gdiac.game.models.Player;
import edu.cornell.gdiac.game.obstacle.EffectObstacle;
import edu.cornell.gdiac.game.models.Spike;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;
import edu.cornell.gdiac.game.obstacle.WheelObstacle;

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

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

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

        try {
            if ((bd1.toString().contains("Spike") && bd2 instanceof Player && !fix2.isSensor() ||
                    bd2.toString().contains("Spike") && bd1 instanceof Player && !fix1.isSensor())
                    && level.getPlayer().getiFramesRemaining() == 0) {
                level.getPlayer().setHit(true);
                level.getPlayer().hitByEnemy();
            }
        } catch (Exception e) { }

        if (bd1 instanceof WheelObstacle && !(bd1 instanceof SwordWheelObstacle) && !(bd2 instanceof Enemy)){
            System.out.println(bd2.toString());
            ((WheelObstacle) bd1).markRemoved(true);
            if(bd2 instanceof Player){
                Player player=level.getPlayer();
                if (!player.isHit() & !player.isDashing()) {
                    player.setHit(true);
                    player.hitByEnemy();
                }
            }
        }else if (bd2 instanceof WheelObstacle && !(bd2 instanceof SwordWheelObstacle)&& !(bd1 instanceof Enemy)){
            System.out.println(bd1.toString());
            ((WheelObstacle) bd2).markRemoved(true);
            if(bd1 instanceof Player ) {
                Player player = level.getPlayer();
                if (!player.isHit() & !player.isDashing()) {
                    player.setHit(true);
                    player.hitByEnemy();
                }
            }
        }
        if (bd1 instanceof SwordWheelObstacle && bd2 instanceof Enemy || bd2 instanceof SwordWheelObstacle && bd1 instanceof Enemy) {
            Enemy enemy = (Enemy) (bd1 instanceof SwordWheelObstacle ? bd2 : bd1);
            SwordWheelObstacle sword = (SwordWheelObstacle) (bd1 instanceof SwordWheelObstacle ? bd1 : bd2);

            if (!sword.hasHitEnemy(enemy)) {
                Player player = level.getPlayer();

                //down
                if (player.getAngleFacing() == 270) {
                    player.setVelocity(player.getBodyVelocityX(), player.getDownwardAttackPropelY());
                }

                //down left
                else if (player.getAngleFacing() == 225) {
                    player.setVelocity(player.getDownwardAttackPropelX(), player.getDownwardAttackPropelY());
                }

                //down right
                else if (player.getAngleFacing() == 315) {
                    player.setVelocity(-player.getDownwardAttackPropelX(), player.getDownwardAttackPropelY());
                }

                enemy.hitBySword(level.getPlayer());
                sword.addHitEnemy(enemy);

                //create hit by sword effect
                float effectAngle = (float) Math.toRadians(level.getPlayer().getAngleFacing());
                float pOffsetX = 0.0f;
                float pOffsetY = 0.0f;
                float sx = 0.02f;
                Vector2 scale = new Vector2(1f, 1f);
                EffectObstacle bloodEffect = new EffectObstacle(enemy.getX(), enemy.getY(), enemy.getBloodEffect().getRegionWidth(),
                        enemy.getBloodEffect().getRegionHeight(), sx, 0.02f, effectAngle,
                        pOffsetX, pOffsetY,17, 1,true,
                        "bloodEffect", enemy, 0.35f,
                        scale, enemy.getBloodEffect(),3);

                level.addQueuedObject(bloodEffect);

            }

        }

        if (bd1 instanceof Player && !fix1.isSensor() && bd2 instanceof Enemy || bd2 instanceof Player && !fix2.isSensor() && bd1 instanceof Enemy) {
            Enemy enemy = (Enemy) (bd1 instanceof Player ? bd2 : bd1);
            Player player = (Player) (bd1 instanceof Player ? bd1 : bd2);

            if (!player.isHit() & !player.isDashing()) {
                player.setHit(true);
                player.hitByEnemy();
            }
        }

        if (level.getPlayer().getSpiritSensorName().equals(fd1) && bd2 instanceof Enemy || level.getPlayer().getSpiritSensorName().equals(fd2) && bd1 instanceof Enemy) {
            Enemy enemy = (Enemy) (bd1 instanceof Enemy ? bd1 : bd2);

             level.getPlayer().getEnemiesInSpiritRange().add(enemy);
        }

        //#endregion

        //#region Cancel ALL collision between spike and enemy
        if(bd1 instanceof Enemy || bd1 instanceof Spike){
            Fixture fixture1 = fix1;
            Filter filter1 = fixture1.getFilterData();
            filter1.groupIndex = -1;
            fixture1.setFilterData(filter1);
        }else if (bd2 instanceof Enemy || bd2 instanceof Spike) {
            Fixture fixture1 = fix2;
            Filter filter1 = fixture1.getFilterData();
            filter1.groupIndex = -1;
            fixture1.setFilterData(filter1);
        }
        //#endregion

        //#region Cancel ALL collision between spike and enemy
        if((bd1 instanceof Enemy && bd2 instanceof Spike) || (bd2 instanceof Enemy && bd1 instanceof Spike)){
            Fixture fixture1 = fix1;
            Fixture fixture2 = fix2;
            Filter filter1 = fixture1.getFilterData();
            Filter filter2 = fixture2.getFilterData();
            filter1.groupIndex = -1;
            filter2.groupIndex = -1;
            fixture1.setFilterData(filter1);
            fixture2.setFilterData(filter2);
        }


//        else if((bd1 instanceof Player && bd2 instanceof Spike) || (bd2 instanceof Player && bd1 instanceof Spike)){
//            Fixture fixture1 = fix1;
//            Fixture fixture2 = fix2;
//            Filter filter1 = fixture1.getFilterData();
//            Filter filter2 = fixture2.getFilterData();
//            filter1.groupIndex = 1;
//            filter2.groupIndex = 1;
//            fixture1.setFilterData(filter1);
//            fixture2.setFilterData(filter2);
//        }
//        else if (bd2 instanceof Enemy || bd2 instanceof Spike) {
//            Fixture fixture1 = fix2;
//            Filter filter1 = fixture1.getFilterData();
//            filter1.groupIndex = -1;
//            fixture1.setFilterData(filter1);
//        }
        //#endregion

    }

    @Override
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

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

        if (level.getPlayer().getSpiritSensorName().equals(fd1) && bd2 instanceof Enemy || level.getPlayer().getSpiritSensorName().equals(fd2) && bd1 instanceof Enemy) {
            Enemy enemy = (Enemy) (bd1 instanceof Enemy ? bd1 : bd2);

            level.getPlayer().getEnemiesInSpiritRange().removeValue(enemy, true);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();


        if (bd1 instanceof Player && !fix1.isSensor() && bd2 instanceof Enemy || bd2 instanceof Player && !fix2.isSensor() && bd1 instanceof Enemy) {
            contact.setEnabled(false);
        }

        if (bd1 instanceof Enemy && bd2 instanceof Enemy) {
            contact.setEnabled(false);
        }

        if (bd1 instanceof Enemy && bd2 instanceof WheelObstacle || bd2 instanceof Enemy && bd1 instanceof WheelObstacle) {
            WheelObstacle wheel = (WheelObstacle) (bd1 instanceof WheelObstacle ? bd1 : bd2);
            if (!wheel.getName().equals("bullet"))
                contact.setEnabled(false);
        }

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
