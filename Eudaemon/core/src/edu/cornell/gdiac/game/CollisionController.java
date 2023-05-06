package edu.cornell.gdiac.game;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.game.obstacle.EffectObstacle;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;
import edu.cornell.gdiac.game.obstacle.WheelObstacle;

public class CollisionController implements ContactListener {

    private AudioController audio;

    private Level level;
    /**
     * The list of tile fixtures the player's ground sensor is in contact with
     */
    private Array<Fixture> groundSensorFixtures;
    /**
     * The list of tile fixtures the player's right wall sensor is in contact with
     */
    private Array<Fixture> rightWallSensorFixtures;
    /**
     * The list of tile fixtures the player's left wall sensor is in contact with
     */
    private Array<Fixture> leftWallSensorFixtures;

    public CollisionController(Level level, AudioController audio) {
        this.level = level;
        this.audio = audio;
        groundSensorFixtures = new Array<Fixture>();
        rightWallSensorFixtures = new Array<Fixture>();
        leftWallSensorFixtures = new Array<Fixture>();
    }

    public void dispose() {
        this.level = null;
        this.groundSensorFixtures.clear();
        this.rightWallSensorFixtures.clear();
        this.leftWallSensorFixtures.clear();
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

        // Sees if the player is grounded
        try {
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getGroundSensorName())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getGroundSensorName()))) {
                level.getPlayer().setGrounded(true);
                groundSensorFixtures.add(level.getPlayer() == bd1 ? fix2 : fix1);
            }
        } catch (Exception e) { }

        // See if the player's right side is touching the wall
        try {
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getWallSensorNameRight())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getWallSensorNameRight()))) {
                level.getPlayer().setTouchingWallRight(true);
                rightWallSensorFixtures.add(level.getPlayer() == bd1 ? fix2 : fix1);
            }
        } catch (Exception e) { }

        // See if the player's left side is touching the wall
        try {
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getWallSensorNameLeft())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getWallSensorNameLeft()))) {
                level.getPlayer().setTouchingWallLeft(true);
                leftWallSensorFixtures.add(level.getPlayer() == bd1 ? fix2 : fix1);
            }
        } catch (Exception e) { }


        //here is the code for bullet
        //bullet is wheel obstacle but is not sword wheel obstacle
        if (bd1 instanceof WheelObstacle && !(bd1 instanceof SwordWheelObstacle) && !(bd2 instanceof Enemy) && !fix2.isSensor()){
            ((WheelObstacle) bd1).markRemoved(true);
            if(bd2 instanceof Player){
                Player player=level.getPlayer();
                if (!player.isHit() & !player.isDashing()) {
                    player.setHit(true);
                    player.hitByEnemy(2, bd1);
                    level.shakeControllerMedium();
                    if (!level.getPlayer().isRemoved()) level.setShouldShakeCamera(true,1);
                }
            }
        }else if (bd2 instanceof WheelObstacle && !(bd2 instanceof SwordWheelObstacle)&& !(bd1 instanceof Enemy) && !fix1.isSensor() ){
            ((WheelObstacle) bd2).markRemoved(true);
            if(bd1 instanceof Player ) {
                Player player = level.getPlayer();
                if (!player.isHit() & !player.isDashing()) {
                    player.setHit(true);
                    player.hitByEnemy(2, bd2);
                    level.shakeControllerHeavy();
                    if (!level.getPlayer().isRemoved()) level.setShouldShakeCamera(true,1);
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
                if(enemy.getHearts() == 0){
                    audio.playEffect("sword-kill", 0.5f);
                    if (!level.getPlayer().isRemoved()) level.setShouldShakeCamera(true,1);
                }
                else{
                    audio.playEffect("sword-hit", 0.3f);
                }
                sword.addHitEnemy(enemy);
                level.shakeControllerHeavy();

                if(enemy.getHearts() > 0){
                    //create hit by sword effect
                    float effectAngle = (float) Math.toRadians(level.getPlayer().getAngleFacing());
                    float pOffsetX = 0.0f;
                    float pOffsetY = 0.0f;
                    float sx = 0.02f;
                    EffectObstacle bloodEffect = level.getEffectPool().obtainEffect(enemy.getX(), enemy.getY(), enemy.getBloodEffect().getRegionWidth(),
                            enemy.getBloodEffect().getRegionHeight(), sx, 0.02f, effectAngle,
                            pOffsetX, pOffsetY,true,
                            "bloodEffect", enemy, 0.35f,
                            1, 1, enemy.getBloodEffectAnimation(),3);

                    level.addQueuedObject(bloodEffect);
                }
                else{
                    float effectAngle = (float) Math.toRadians(level.getPlayer().getAngleFacing());
                    float pOffsetX = 0.0f;
                    float pOffsetY = 0.0f;
                    float sx = 0.02f;
                    EffectObstacle killedBloodEffect = level.getEffectPool().obtainEffect(enemy.getX(), enemy.getY(), enemy.getKilledBloodEffect().getRegionWidth(),
                            enemy.getKilledBloodEffect().getRegionHeight(), sx, 0.02f, effectAngle,
                            pOffsetX, pOffsetY,true,
                            "killedBloodEffect", enemy, 0.35f,
                            1, 1, enemy.getKilledBloodEffectAnimation(),3);
                    level.addQueuedObject(killedBloodEffect);
                }
            }

        }

        if ((bd1 instanceof SwordWheelObstacle && bd2.toString().contains("Tile")) || (bd2 instanceof SwordWheelObstacle && bd1.toString().contains("Tile"))) {
            SwordWheelObstacle sword = (SwordWheelObstacle) (bd1 instanceof SwordWheelObstacle ? bd1 : bd2);

            //prints if hit wall
            System.out.println("hit wall");

            if (!sword.hasHitWall()) {
                Player player = level.getPlayer();
                float x = player.getX();
                float y = player.getY();
                //down
                if (player.getAngleFacing() == 270) {

                }

                //down left
                else if (player.getAngleFacing() == 225) {

                }

                //down right
                else if (player.getAngleFacing() == 315) {

                }

                sword.setHasHitWall(true);
                level.shakeControllerMedium();

                //create hit by hit wall effect
                float pOffsetX = 0.0f;
                float pOffsetY = 0.0f;
                float sx = 1.0f;
                float sy = 1.0f;
                EffectObstacle hitWallEffect = level.getEffectPool().obtainEffect(x, y, player.getHitWallEffect().getKeyFrame(0).getRegionWidth(),
                        player.getHitWallEffect().getKeyFrame(0).getRegionHeight(), sx, sy, 0,
                        pOffsetX, pOffsetY,false,
                        "hitEffect", player, 0.35f,
                        1, 1, player.getHitWallEffect(),3);

                level.addQueuedObject(hitWallEffect);
            }

        }
            //I move them to presolve()
//        if (bd1 instanceof Player && !fix1.isSensor() && bd2 instanceof Enemy ||
//                bd2 instanceof Player && !fix2.isSensor() && bd1 instanceof Enemy) {
//            Enemy enemy = (Enemy) (bd1 instanceof Player ? bd2 : bd1);
//            Player player = (Player) (bd1 instanceof Player ? bd1 : bd2);
//
//            if (!player.isHit() & !player.isDashing()) {
//                player.setHit(true);
//                player.hitByEnemy(0, bd1 instanceof Player? bd2:bd1);
//            }
//        }

        if (level.getPlayer().getSpiritSensorName().equals(fd1) && bd2 instanceof Enemy || level.getPlayer().getSpiritSensorName().equals(fd2) && bd1 instanceof Enemy) {
            Enemy enemy = (Enemy) (bd1 instanceof Enemy ? bd1 : bd2);
            if (enemy.getSpiritRemain()>0){
             level.getPlayer().getEnemiesInSpiritRange().add(enemy);}
        }

        if (bd1 instanceof Player && !fix1.isSensor() && bd2 instanceof Exit ||
                bd2 instanceof Player && !fix2.isSensor() && bd1 instanceof Exit) {
            Exit exit = (Exit) (bd1 instanceof Player ? bd2 : bd1);
            Player player = (Player) (bd1 instanceof Player ? bd1 : bd2);

            exit.setReached(true);
            level.setCompleted(true);
        }

        if ((bd1 instanceof Player && bd2 instanceof Billboard || bd2 instanceof Player && bd1 instanceof Billboard)
                && (!level.getPlayer().getSpiritSensorName().equals(fd1) && !level.getPlayer().getSpiritSensorName().equals(fd2))) {
            Billboard billboard = (Billboard) (bd1 instanceof Billboard? bd1 : bd2);
            billboard.setDisplay(true);
        }

        if ((bd1 instanceof Player && bd2 instanceof TutorialArea || bd2 instanceof Player && bd1 instanceof TutorialArea)
                && (!level.getPlayer().getSpiritSensorName().equals(fd1) && !level.getPlayer().getSpiritSensorName().equals(fd2))) {
            TutorialArea tutorialArea = (TutorialArea) (bd1 instanceof TutorialArea? bd1 : bd2);
            tutorialArea.setDisplay(true);
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
        //#endregion

        //#region Player attack destroys projectile
        if (bd1 instanceof SwordWheelObstacle && bd2 instanceof WheelObstacle ||
                bd2 instanceof SwordWheelObstacle && bd1 instanceof WheelObstacle) {
            WheelObstacle bullet = (WheelObstacle) (((WheelObstacle) bd1).getName().equals("bullet") ? bd1 : bd2);
            bullet.markRemoved(true);
        }
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

        // Sees if the player's ground sensor is no longer in contact with any tiles
        try {
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getGroundSensorName())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getGroundSensorName()))) {

                groundSensorFixtures.removeValue(level.getPlayer() == bd1 ? fix2 : fix1, true);
                if (groundSensorFixtures.size == 0) {
                    level.getPlayer().setGrounded(false);
                }
            }
        } catch (Exception e) {}

        // Sees if the player's right wall sensor is no longer in contact with any tiles
        try {
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getWallSensorNameRight())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getWallSensorNameRight()))) {

                rightWallSensorFixtures.removeValue(level.getPlayer() == bd1 ? fix2 : fix1, true);
                if (rightWallSensorFixtures.size == 0) {
                    level.getPlayer().setTouchingWallRight(false);
                }
            }
        } catch (Exception e) {}

        // Sees if the player's left wall sensor is no longer in contact with any tiles
        try {
            if ((bd1.toString().contains("Tile") && fix2.getUserData().equals(
                    level.getPlayer().getWallSensorNameLeft())) ||
                    (bd2.toString().contains("Tile") && fix1.getUserData().equals(
                            level.getPlayer().getWallSensorNameLeft()))) {

                leftWallSensorFixtures.removeValue(level.getPlayer() == bd1 ? fix2 : fix1, true);
                if (leftWallSensorFixtures.size == 0) {
                    level.getPlayer().setTouchingWallLeft(false);
                }
            }
        } catch (Exception e) {}


        if (level.getPlayer().getSpiritSensorName().equals(fd1) && bd2 instanceof Enemy ||
                level.getPlayer().getSpiritSensorName().equals(fd2) && bd1 instanceof Enemy) {
            Enemy enemy = (Enemy) (bd1 instanceof Enemy ? bd1 : bd2);

            level.getPlayer().getEnemiesInSpiritRange().removeValue(enemy, true);
        }

        if (bd1 instanceof Player && bd2 instanceof Billboard
                || bd2 instanceof Player && bd1 instanceof Billboard
                && (!level.getPlayer().getSpiritSensorName().equals(fd1) && !level.getPlayer().getSpiritSensorName().equals(fd2))) {
            Billboard billboard = (Billboard) (bd1 instanceof Billboard? bd1 : bd2);
            billboard.setDisplay(false);
        }

        if ((bd1 instanceof Player && bd2 instanceof TutorialArea
                || bd2 instanceof Player && bd1 instanceof TutorialArea)
                && (!level.getPlayer().getSpiritSensorName().equals(fd1) && !level.getPlayer().getSpiritSensorName().equals(fd2))) {
            TutorialArea tutorialArea = (TutorialArea) (bd1 instanceof TutorialArea? bd1 : bd2);
            tutorialArea.setDisplay(false);
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

        //spike
        if ((bd1.toString().contains("Spike") && bd2 instanceof Player && !fix2.isSensor() ||
                    bd2.toString().contains("Spike") && bd1 instanceof Player && !fix1.isSensor())) {
            Player player = (Player) (bd1 instanceof Player ? bd1 : bd2);
            if (!player.isHit()){
            player.setHit(true);
            player.hitByEnemy(1, bd2 instanceof Player? bd1: bd2);
            level.shakeControllerHeavy();
            if (!level.getPlayer().isRemoved())
                level.setShouldShakeCamera(true,1);
            //Resets dash when damaged by spike
            player.setDashedInAir(false);}
        }

        //enemy
        if (bd1 instanceof Player && !fix1.isSensor() && bd2 instanceof Enemy ||
                bd2 instanceof Player && !fix2.isSensor() && bd1 instanceof Enemy) {
            Player player = (Player) (bd1 instanceof Player ? bd1 : bd2);

            if (!player.isHit() && !player.isDashing() && player.getiFramesRemaining() <= 0) {
                player.setHit(true);
                player.hitByEnemy(0, bd1 instanceof Player? bd2:bd1);
                level.shakeControllerHeavy();
                if (!level.getPlayer().isRemoved()) level.setShouldShakeCamera(true,1);
            }
        }
        //cancel collision
        if (bd1 instanceof Player && !fix1.isSensor() && bd2 instanceof Enemy ||
                bd2 instanceof Player && !fix2.isSensor() && bd1 instanceof Enemy) {
            contact.setEnabled(false);
        }

        if (bd1 instanceof Enemy && bd2 instanceof Enemy) {
            contact.setEnabled(false);
        }

        if (bd1 instanceof Enemy && bd2 instanceof WheelObstacle ||
                bd2 instanceof Enemy && bd1 instanceof WheelObstacle) {
            WheelObstacle wheel = (WheelObstacle) (bd1 instanceof WheelObstacle ? bd1 : bd2);
            if (!wheel.getName().equals("bullet"))
                contact.setEnabled(false);
        }

        if (bd1 instanceof Billboard || bd2 instanceof Billboard) {
            contact.setEnabled(false);
        }

        if (bd1 instanceof TutorialArea || bd2 instanceof TutorialArea) {
            contact.setEnabled(false);
        }



    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
