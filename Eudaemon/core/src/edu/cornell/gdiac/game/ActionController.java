package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;
import java.util.Timer;
import java.util.TimerTask;

import java.util.EnumSet;
import java.util.HashMap;

public class ActionController {

    private Level level;
    private Enemy[] enemies;
    private Player player;

    private float previousX;
    private float currentX;
    private boolean movedDuringLastFrame;

    /**
     * A HashMap storing 2D TextureRegion arrays with a String identifier.
     * Each key represents an animation name, and the associated value is a 2D TextureRegion array
     * with rows representing different animation states and columns representing individual frames.
     */
    private HashMap<String, Animation> animations;

    /**
     * The current animation TextureRegion.
     */
    private Animation currentAnimation;

    /**
     * The number of ticks that have occurred since this object was created
     */
    private int ticks;

    /**
     * The current frame of the animation.
     */
    private int currentFrame;

    /**
     * The number of ticks before the animation frame changes.
     */
    private int tickFrameSwitch = 4;

    /**
     * The max number of frames before currentFrame resets
     */
    private int maxFrame = 6;


    public ActionController(Level level) {
        enemies = level.getEnemies();
        player = level.getPlayer();
        this.level = level;
        animations = new HashMap<>();
        previousX = 0;
        currentX = 0;
        movedDuringLastFrame = false;

        //Creating a Dictionary of Textures
        addAnimations(player.getMomoRunSpriteSheet(), 6, 1, "momoRun");
        addAnimations(player.getMomoDashSpriteSheet(), 5, 1, "momoDash");
        addAnimations(player.getMomoJumpSpriteSheet(), 6, 1, "momoJump");
        addAnimations(player.getChiyoRunSpriteSheet(), 8, 1, "chiyoRun");
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
        ticks++;
    }

    //#region Player Actions
    /**
     * Resolves the set of player actions
     *
     * @param playerAction the set of player actions to resolve
     */
    private void resolvePlayerActions(EnumSet<Action> playerAction){
        currentX = player.getX();
        float deltaX = previousX - currentX;
        //#region Button Inputs
        boolean jumpPressed = playerAction.contains(Action.JUMP);
        boolean jumpHold = playerAction.contains(Action.HOLD_JUMP);
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

        int dashCooldownRemaining = player.getDashCooldownRemaining();
        if (dashCooldownRemaining > 0){
            player.setDashCooldownRemaining(dashCooldownRemaining - 1);
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
                x = Math.min(x+h_acc, max_speed);
            }
        }
        else if (leftPressed){
            if (x > -max_speed){
                //x = Math.max(x-h_acc, -max_speed);
                x = Math.max(x-h_acc, -max_speed);
            }
        }
        player.setVelocity(x,y);
        //#endregion

        //#region Form Switching
        if (transformPressed && player.getTransformCooldownRemaining() == 0 && player.getSpirit() > 1.0f){
            player.setTransformCooldownRemaining(player.getTransformCooldown());
            player.setForm();
        }
        //#endregion

        //#region Sword Attack
        if (attackPressed && player.getForm() == 1 && player.getAttackCooldownRemaining() == 0){
            player.setAttackCooldownRemaining(player.getAttackCooldown());
            player.setAttacking(true);
            System.out.println("attakc lifespan: "+ player.getAttackLifespan());
            player.setAttackLifespanRemaining(player.getAttackLifespan());
            createSword();
            if(player.getAngleFacing() == 270){
                player.setVelocity(player.getBodyVelocityX(), player.getJumpVelocity()-1F);
            }
        }
        //#endregion

        //#region Dash
        if(player.getForm() == 0 && dashPressed && player.isGrounded() && player.getDashCooldownRemaining() == 0 && !player.isDashing() && !player.dashedInAir()
                || player.getForm() == 0 && dashPressed && player.getJumpVelocity() > 0 && player.getDashCooldownRemaining() == 0 && !player.isDashing() && !player.dashedInAir()){
            player.setDashCooldownRemaining(player.getDashCooldown());
            player.setDashing(true);
            player.setDashedInAir(true);
            player.setDashLifespanRemaining((player.getDashLifespan()));
            float currOffset = player.getAttackOffset();
            int angleFacing = player.getAngleFacing();
            float dashX = player.getX() + currOffset;
            float dashY = player.getY();
            float dash = player.getDash();
            if (angleFacing == 0) player.setVelocity(dash,0);
            if (angleFacing == 45){
                dashX = player.getX() + 0.71f * currOffset;
                dashY = player.getY() + 0.71f * currOffset;
                player.setVelocity(dash,10);
            }
            else if (angleFacing == 90){
                dashX = player.getX();
                dashY = player.getY() + currOffset;
                player.setVelocity(0,dash);
            }
            else if (angleFacing == 135){
                dashX = player.getX() - 0.71f * currOffset;
                dashY = player.getY() + 0.71f * currOffset;
                player.setVelocity(-0.71f*dash,10);
            }
            else if (angleFacing == 180){
                dashX = player.getX() - currOffset;
                dashY = player.getY();
                player.setVelocity(-dash,0);
            }
            else if (angleFacing == 225){
                dashX = player.getX() - 0.71f * currOffset;
                dashY = player.getY() - 0.71f * currOffset;
                player.setVelocity(-0.71f*dash,-0.71f*dash);
            }
            else if (angleFacing == 270){
                dashX = player.getX();
                dashY = player.getY()- currOffset;
                player.setVelocity(0,-dash);
            }
            else if (angleFacing == 315){
                dashX = player.getX() + 0.71f * currOffset;
                dashY = player.getY() - 0.71f * currOffset;
                player.setVelocity(0.71f*dash,-0.71f*dash);
            }
            Vector2 scale = new Vector2(10f,10f);
            SwordWheelObstacle dashAnimate = new SwordWheelObstacle(dashX, dashY, player.getDashEffectSpriteSheet().getRegionWidth()/100, player, 0.4f, 0.01f, scale, player.getDashEffectSpriteSheet());
            level.addQueuedObject(dashAnimate);
            //player.dash();
            //player.setVelocity(player.getBodyVelocityX(), 0);


            //Setting Gravity to 0 and scheduling to set it back
            player.setPlayerGravity(0.0f);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    player.setPlayerGravity(player.getPlayerGravity());
                }
            }, player.getDashTime());
        }

        System.out.println(player.getBody().getGravityScale());
        //#endregion

        //#region Jump
        //jump!
        //include all three situations
        //normal jump, coyote, and jump pressed in air

        if ((jumpPressed && player.isGrounded() && player.getJumpCooldownRemaining()==0) ||
                (jumpPressed && player.getCoyoteFramesRemaining() > 0 && player.getJumpCooldownRemaining()==0) ||
                (player.getJumpPressedInAir() && player.getJumpCooldownRemaining()==0 && player.isGrounded())) {
            player.setVelocity(player.getBodyVelocityX(), player.getJumpVelocity());
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
            player.setVelocity(player.getBodyVelocityX(), player.getJumpVelocity());
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
        //#endregion

        if (player.getiFramesRemaining() > 0) {
            player.setiFramesRemaining(Math.max(player.getiFramesRemaining()-1,0));
            if (player.getiFramesRemaining()==0) {
                player.setHit(false);

            }
        }

        if (player.getAttackLifespanRemaining() > 0) {
            System.out.println("attack lifespan decrease: "+player.getAttackLifespanRemaining());
            player.setAttackLifespanRemaining(Math.max(player.getAttackCooldownRemaining()-1,0));
            if (player.getAttackLifespanRemaining()==0) {
                player.setAttacking(false);
                System.out.println("yes, setting attacking to false");
            }
        }

        if(player.getDashLifespanRemaining() > 0){
            player.setDashLifespanRemaining((int)Math.max(player.getDashLifespanRemaining()-1, 0));
            if(player.getDashLifespanRemaining() == 0){
                player.setDashing(false);
                player.setVelocity(player.getBodyVelocityX()/3, player.getBodyVelocityY()/3);
            }

        }


        //System.out.println(player.getDashLifespanRemaining());

        if (player.isDashing()) {
            player.setDashCooldownRemaining(Math.max(player.getDashCooldownRemaining()-1,0));
            if (player.getDashCooldownRemaining()==0) player.setDashing(false);
        }

        if (player.isGrounded() && !player.isDashing()) player.setDashedInAir(false);

        if (debugPressed) {
            level.setDebug(!level.isDebug());
        }

        //#region Textures and Animation
        if (ticks % tickFrameSwitch == 0){
            currentFrame++;
            if (currentFrame > maxFrame){
                currentFrame = 0;
            }
        }
        if (player.getForm() == 0){
            player.setSxMult(1.0f);
            player.setSyMult(1.0f);
            if (player.isDashing()){
                TextureRegion current = (TextureRegion) (animations.get("momoDash")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 10;
                maxFrame = 4;
                player.setTexture(current);
                player.setOyOffset(-30);
            }
            else if (player.getIsJumping()){
                TextureRegion current = (TextureRegion) (animations.get("momoJump")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 10;
                maxFrame = 5;
                player.setTexture(current);
                player.setOyOffset(-30);
            }
            else if (player.getBodyVelocityX() != 0){
                TextureRegion current = (TextureRegion) (animations.get("momoRun")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 5;
                maxFrame = 5;
                player.setTexture(current);
                player.setOyOffset(-30);
            }
            else{
                player.setTexture(player.getMomoTexture());
                player.setOyOffset(-130);
            }
        }
        else{
            if (player.getBodyVelocityX() != 0){
                TextureRegion current = (TextureRegion) (animations.get("chiyoRun")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 4;
                maxFrame = 7;
                player.setTexture(current);
                player.setOyOffset(-25);
                player.setSxMult(1.5f);
                player.setSyMult(1.5f);
            }
            else{
                player.setTexture(player.getChiyoTexture());
                player.setOyOffset(-100);
                player.setSxMult(1.0f);
                player.setSyMult(1.0f);
            }
        }
        //#endregion
        if (deltaX == 0 && !player.isGrounded() && (leftPressed || rightPressed)
                && Math.abs(player.getBodyVelocityX()) == player.getHorizontalAcceleration() && movedDuringLastFrame) {
            player.setVelocity(0, player.getBodyVelocityY());
        }
        previousX = player.getX();
        movedDuringLastFrame = (leftPressed || rightPressed) && deltaX > 0;

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
                        //TODO: is this already handled by the HandleSpirit() in Level.java?

                    }
                    else {
                        //TODO
                        enemy.setMovement(action);
                        enemy.applyForce();
                    }
                }
                enemyAction.clear();

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
        Vector2 scale = new Vector2(32.0f,32.0f); //only gets used in sword attack
        SwordWheelObstacle sword = new SwordWheelObstacle(x, y, player.getSwordRadius()*2, player, player.getAttackLifespan(), 10.0f, scale, player.getSwordEffectSpriteSheet(angleFacing));
        level.addQueuedObject(sword);
    }

    //#endregion

    private void addAnimations (TextureRegion spriteSheet, int columns, int rows, String name){
        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth()/columns, spriteSheet.getRegionHeight()/rows);
        Animation animation = new Animation<TextureRegion>(0.5f, frames[0]); // Creates an animation with a frame duration of 0.1 seconds
        animation.setPlayMode(Animation.PlayMode.NORMAL); // Sets the animation to play normally
        animations.put(name, animation);
    }
}


