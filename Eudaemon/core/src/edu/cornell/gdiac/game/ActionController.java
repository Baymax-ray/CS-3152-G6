package edu.cornell.gdiac.game;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;

import java.util.EnumSet;

public class ActionController {

    private Level level;
    private Enemy[] enemies;
    private Player player;

    public ActionController(Level level) {
        enemies = level.getEnemies();
        player = level.getPlayer();
        this.level = level;
    }

    /**
     * Resolves the set of actions
     *
     * @param playerAction the set of player actions to resolve
     * @param enemyActions the set ofenemy actions to resolve
     */
    public void resolveActions(EnumSet<Action> playerAction, Array<EnumSet<EnemyAction>> enemyActions) {
        resolvePlayerActions(playerAction);
    }

    //#region Player Actions
    /**
     * Resolves the set of player actions
     *
     * @param playerAction the set of player actions to resolve
     */
    private void resolvePlayerActions(EnumSet<Action> playerAction){
        //#region Button Inputs
        boolean jumpPressed = playerAction.contains(Action.JUMP);
        boolean attackPressed = playerAction.contains(Action.ATTACK);
        boolean dashPressed = playerAction.contains(Action.DASH);
        boolean transformPressed = playerAction.contains(Action.TRANSFORM);
        boolean downPressed = playerAction.contains(Action.LOOK_DOWN);
        boolean upPressed = playerAction.contains(Action.LOOK_UP);
        boolean rightPressed = playerAction.contains(Action.MOVE_RIGHT);
        boolean leftPressed = playerAction.contains(Action.MOVE_LEFT);
        //#endregion

        //#region Timers
        int attackCooldownRemaining = player.getAttackCooldownRemaining();
        if (attackCooldownRemaining > 0){
            player.setAttackCooldownRemaining(attackCooldownRemaining - 1);
        }
        int transformCooldownRemaining = player.getTransformCooldownRemaining();
        if (transformCooldownRemaining > 0){
            player.setTransformCooldownRemaining(transformCooldownRemaining - 1);
        }
        //#endregion

        //#region Direction
        if (player.isFacingRight()){
            player.setAngleFacing(0);
        }
        else{
            player.setAngleFacing(180);
        }
        if ((rightPressed && !leftPressed && !upPressed && !downPressed) || (rightPressed && !leftPressed && upPressed && downPressed)){
            player.setAngleFacing(0);
            player.setFacingRight(true);
        }
        else if ((rightPressed && !leftPressed && upPressed && !downPressed)){
            player.setAngleFacing(45);
            player.setFacingRight(true);
        }
        else if ((rightPressed && leftPressed && upPressed && !downPressed) || (!rightPressed && !leftPressed && upPressed && !downPressed)){
            player.setAngleFacing(90);
        }
        else if ((!rightPressed && leftPressed && upPressed && !downPressed)){
            player.setAngleFacing(135);
            player.setFacingRight(false);
        }
        else if ((!rightPressed && leftPressed && !upPressed && !downPressed) || (!rightPressed && leftPressed && upPressed && downPressed)){
            player.setAngleFacing(180);
            player.setFacingRight(false);
        }
        else if ((!rightPressed && leftPressed && !upPressed && downPressed)){
            player.setAngleFacing(225);
            player.setFacingRight(false);
        }
        else if ((!rightPressed && !leftPressed && !upPressed && downPressed) || (rightPressed && leftPressed && !upPressed && downPressed)){
            player.setAngleFacing(270);
        }
        else if ((rightPressed && !leftPressed && !upPressed && downPressed)){
            player.setAngleFacing(315);
            player.setFacingRight(true);
        }
        //#endregion

        //#region Right Left Movement
        float y = player.getBodyVelocityY();
        float x = player.getBodyVelocityX();;
        float max_speed = player.getMaxSpeed();
        float h_acc = player.getHorizontalAcceleration();
        if (rightPressed && leftPressed || (!rightPressed && !leftPressed)){
            if (x > 0.0f){
                x = Math.max(x-h_acc, 0);
                if (x < 0.0f){
                    x = 0.0f;
                }
            }
            else if (x < 0.0f){
                x = Math.min(x+h_acc, 0);
                if (x > 0.0f){
                    x = 0.0f;
                }
            }
        }
        else if (rightPressed){
            if (x < max_speed){
                x = Math.min(x+h_acc, max_speed);
            }
        }
        else if (leftPressed){
            if (x > -max_speed){
                x = Math.max(x-h_acc, -max_speed);
            }
        }
        player.setVelocity(x,y);
        //#endregion

        //#region Form Switching
        if (transformPressed && player.getTransformCooldownRemaining() == 0){
            player.setTransformCooldownRemaining(player.getTransformCooldown());
            player.setForm();
        }
        //#endregion

        //#region Sword Attack
        if (attackPressed && player.getForm() == 1 && player.getAttackCooldownRemaining() == 0){
            player.setAttackCooldownRemaining(player.getAttackCooldown());
            createSword();
        }
        //#endregion

        //#region Textures and Animation
        if (player.getForm() == 0){
            player.setTexture(player.getMomoTexture());
        }
        else{
            player.setTexture(player.getChiyoTexture());
        }
        //#endregion
    }

    /**
     * Add a new sword attack to the world and send it in the right direction.
     */
    private void createSword() {
//        float currOffset = player.getAttackOffset() * (player.isFacingRight() ? 1 : -1);
        float currOffset = player.getAttackOffset();
        int angleFacing = player.getAngleFacing();
        float x = player.getX() + currOffset;
        float y = player.getY();
        if (angleFacing == 45){
            x = player.getX() + 0.71f * currOffset;
            y = player.getY() + 0.71f * currOffset;
        }
        else if (angleFacing == 90){
            x = player.getX();
            y = player.getY() + currOffset;
        }
        else if (angleFacing == 135){
            x = player.getX() - 0.71f * currOffset;
            y = player.getY() + 0.71f * currOffset;
        }
        else if (angleFacing == 180){
            x = player.getX() - currOffset;
            y = player.getY();
        }
        else if (angleFacing == 225){
            x = player.getX() - 0.71f * currOffset;
            y = player.getY() - 0.71f * currOffset;
        }
        else if (angleFacing == 270){
            x = player.getX();
            y = player.getY()- currOffset;
        }
        else if (angleFacing == 315){
            x = player.getX() + 0.71f * currOffset;
            y = player.getY() - 0.71f * currOffset;
        }
        SwordWheelObstacle sword = new SwordWheelObstacle(x, y, player.getSwordRadius(), player, player.getAttackLifespan(), 10.0f, player.getScale(), player.getSwordSpriteSheet());
        level.addQueuedObject(sword);
    }
    //#endregion

    /**
     * Bridges the gap between ActionController's player and the display of hearts in UIOverlay
     */
    public float getPlayerHearts(){
        //System.out.println("get heart is called");
        if(player != null) {
            //System.out.println("avatar is initialized");
            return player.getHearts();
        }else return 10f;
    }
    /**
     * Bridges the gap between ActionController's player and the display of spirit in UIOverlay
     */
    public float getPlayerSpirit(){
        if(player != null) {
            return player.getSpirit();
        }else return 10f;
    }
}


