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
        //#endregion

        //#region Direction

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
            if (!player.isFacingRight()){
                player.setFacingRight(true);
            }
        }
        else if (leftPressed){
            if (x > -max_speed){
                x = Math.max(x-h_acc, -max_speed);
            }
            if (player.isFacingRight()){
                player.setFacingRight(false);
            }
        }
        player.setVelocity(x,y);
        //#endregion

        //#region Sword Attack
        if (attackPressed && player.getAttackCooldownRemaining() == 0){
            player.setAttackCooldownRemaining(player.getAttackCooldown());
            createSword();
        }
        //#endregion
    }

    /**
     * Add a new sword attack to the world and send it in the right direction.
     */
    private void createSword() {
        float currOffset = player.getAttackOffset() * (player.isFacingRight() ? 1 : -1);
        float x = player.getX() + currOffset;
        float y = player.getY();
        SwordWheelObstacle sword = new SwordWheelObstacle(x, y, player.getSwordRadius(), player, player.getAttackLifespan(), 10.0f, player.getScale(), player.getSwordSpriteSheet());
        level.addQueuedObject(sword);
    }
    //#endregion
}


