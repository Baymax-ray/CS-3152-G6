package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
        resolveEnemyActions(enemyActions);

    }

    //#region Player Actions
    /**
     * Resolves the set of player actions
     *
     * @param playerAction the set of player actions to resolve
     */
    private void resolvePlayerActions(EnumSet<Action> playerAction){
        //#region Button Inputs
        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean jumpHold = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        boolean attackPressed = playerAction.contains(Action.ATTACK);
        boolean dashPressed = playerAction.contains(Action.DASH);
        boolean transformPressed = playerAction.contains(Action.TRANSFORM);
        boolean downPressed = playerAction.contains(Action.LOOK_DOWN);
        boolean upPressed = playerAction.contains(Action.LOOK_UP);
        boolean rightPressed = playerAction.contains(Action.MOVE_RIGHT);
        boolean leftPressed = playerAction.contains(Action.MOVE_LEFT);
        boolean debugPressed = playerAction.contains(Action.DEBUG);
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
                //x = Math.min(x+h_acc, max_speed);
                x = Math.min(x+h_acc, 1.5f);
            }
        }
        else if (leftPressed){
            if (x > -max_speed){
                //x = Math.max(x-h_acc, -max_speed);
                x = Math.max(x-h_acc, -1.5f);
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
            player.setAttacking(true);
            player.setAttackLifespanRemaining((int) (player.getAttackLifespan()/(1/60)));
            createSword();
        }
        //#endregion

        //#region Dash
        if(player.getForm() == 0 && dashPressed){
            player.dash();
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

        //jump!
        //include all three situations
        //normal jump, coyote, and jump pressed in air

        if ((jumpPressed && player.isGrounded() && player.getJumpCooldownRemaining()==0) ||
                (jumpPressed && player.getCoyoteFramesRemaining() > 0 && player.getJumpCooldownRemaining()==0) ||
                (player.getJumpPressedInAir() && player.getJumpCooldownRemaining()==0 && player.isGrounded())) {
            player.setVelocity(player.getBodyVelocityX(), 2.0f);
            player.setJumpCooldownRemaining(player.getJumpCooldown());
            player.setJumpTimeRemaining(player.getJumpTime());
            player.setIsJumping(true);
        }
        else if (player.isGrounded() && player.getBodyVelocityY() == 0) {
            player.setIsJumping(false);
        }
        else player.setJumpCooldownRemaining(Math.max(0, player.getJumpCooldownRemaining()-1));

        if (player.getIsJumping()) player.setJumpTimeRemaining(player.getJumpTimeRemaining() - 1);

        if (jumpHold && player.getIsJumping() && player.getJumpTimeRemaining() > 0) {
            player.setVelocity(player.getBodyVelocityX(), 2.0f);
        }

        if (!jumpHold) player.setIsJumping(false);

        //calculate coyote time
        if (player.isGrounded()) {
            player.setCoyoteFramesRemaining(player.getCoyoteFrames());
        }
        else {
            player.setCoyoteFramesRemaining(Math.max(0, player.getCoyoteFramesRemaining()-1));
        }

        //jump pressed in air
        if (jumpPressed && !player.isGrounded()) {
            player.setJumpToleranceRemaining(player.getJumpTolerance());
            player.setJumpPressedInAir(true);
        } else if (!player.isGrounded()) {
            player.setJumpToleranceRemaining(Math.max(0, player.getJumpToleranceRemaining()-1));
            if (player.getJumpToleranceRemaining()==0) player.setJumpPressedInAir(false);
        }

        if (player.getiFramesRemaining() > 0) {
            player.setiFramesRemaining(Math.max(player.getiFramesRemaining()-1,0));
            if (player.getiFramesRemaining()==0) player.setHit(false);
        }

        if (player.getAttackLifespanRemaining() > 0) {
            player.setAttackLifespanRemaining(Math.max(player.getAttackCooldownRemaining()-1,0));
            if (player.getAttackLifespanRemaining()==0) player.setAttacking(false);
        }

        if (debugPressed) {
            level.setDebug(!level.isDebug());
        }
    }
    /**
     * Resolves the set of enemy actions
     *
     * @param enemyActions an array of sets of enemy actions to resolve, each set representing
     *                     the actions of one enemy
     */
    public void resolveEnemyActions(Array<EnumSet<EnemyAction>> enemyActions) {
        // TODO: 2023/3/19
        for (int i = 0; i < enemies.length; i++) {
            EnumSet<EnemyAction> enemyAction = enemyActions.get(i);
            Enemy enemy = enemies[i];
            if (!enemy.isRemoved()) {
                for (EnemyAction action : enemyAction) {
                    if (action == EnemyAction.ATTACK) {
                        //TODO
                    }
                    if (action == EnemyAction.MOVE_LEFT) {
                        //TODO
                    }
                    if (action == EnemyAction.MOVE_RIGHT) {
                        //TODO
                    }
                }
                if (enemyAction.contains(EnemyAction.DASH)) {
                    System.out.println("yes!");
                }
                if (enemyAction.contains(EnemyAction.STAY)) {
                    System.out.println("yes!");
                }
            }

        }
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
}


