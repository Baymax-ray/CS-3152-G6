package edu.cornell.gdiac.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.game.obstacle.EffectObstacle;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;
import edu.cornell.gdiac.game.obstacle.WheelObstacle;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;

public class ActionController {

    private Level level;
    private final Enemy[] enemies;
    private final Billboard[] billboards;
    private final Player player;

    private float previousX;
    private float currentX;
    private boolean movedDuringLastFrame;

    private boolean impact = false;

    private boolean smallImpact = false;

    /**
     * A HashMap storing 2D TextureRegion arrays with a String identifier.
     * Each key represents an animation name, and the associated value is a 2D TextureRegion array
     * with rows representing different animation states and columns representing individual frames.
     */
    private ObjectMap<String, Animation> animations;

    /**
     * The current animation
     */
    private String currentAnimation;

    /**
     * The number of ticks that have occurred since this object was created
     */
    private int ticks;

    /** The tick of the last time the player wall jumped*/
    private int lastWallJumpTick = 0;

    /** If the player was facing right during their last walljump*/
    private boolean facingRightDuringLastWallJump;

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
    Array<AIController> aiControllers;

    private ArrayList<Sound> soundDictionary;

    private EffectObstacle spiritDrainEffect;

    private AudioController audio;

    public ActionController(Level level,Array<AIController> aiControllers, AudioController audio) {
        enemies = level.getEnemies();
        billboards = level.getBillboards();
        player = level.getPlayer();
        this.level = level;
        animations = new ObjectMap<>();
        previousX = 0;
        currentX = 0;
        movedDuringLastFrame = false;
        this.aiControllers= aiControllers;
        this.audio = audio;
        this.currentAnimation = "momoIdle";

        this.soundDictionary = new ArrayList<>();
        //Creating a Dictionary of Textures
        addAnimations(player.getMomoRunSpriteSheet(), 8, 1, "momoRun");
        addAnimations(player.getMomoDashSpriteSheet(), 5, 1, "momoDash");
        addAnimations(player.getMomoJumpSpriteSheet(), 7, 1, "momoJump");
        addAnimations(player.getChiyoRunSpriteSheet(), 8, 1, "chiyoRun");
        addAnimations(player.getChiyoJumpSpriteSheet(), 8, 1, "chiyoJump");
        addAnimations(player.getChiyoAttackSpriteSheet(), 8, 2, "chiyoAttack");
        addAnimations(player.getDashEffectSpriteSheet(), 5, 1, "dashEffect");
        addAnimations(player.getImpactEffectSpriteSheet(), 8, 1, "impactEffect");
        addAnimations(player.getSpiritDrainSpriteSheet(), 13, 1, "spiritDrain");
    }

    /**
     * Resolves the set of actions
     *
     * @param playerAction the set of player actions to resolve
     * @param enemyActions the set ofenemy actions to resolve
     */
    public void resolveActions(EnumSet<Action> playerAction, Array<EnumSet<EnemyAction>> enemyActions) {
        ticks++;
        resolvePlayerActions(playerAction);
        resolveEnemyActions(enemyActions);
    }

    //#region Player and Enemy Actions

    /**
     * Resolves the set of player actions
     *
     * @param playerAction the set of player actions to resolve
     */
    private void resolvePlayerActions(EnumSet<Action> playerAction) {
        currentX = player.getX();
        float deltaX = previousX - currentX;
        //#region Button Inputs
        boolean jumpPressed = playerAction.contains(Action.BEGIN_JUMP);
        boolean jumpHold = playerAction.contains(Action.HOLD_JUMP);
        boolean attackPressed = playerAction.contains(Action.ATTACK);
        boolean dashPressed = playerAction.contains(Action.DASH);
        boolean dashHold = playerAction.contains(Action.HOLD_DASH);
        boolean transformPressed = playerAction.contains(Action.TRANSFORM);
        boolean downPressed = playerAction.contains(Action.LOOK_DOWN);
        boolean upPressed = playerAction.contains(Action.LOOK_UP);
        boolean rightPressed = playerAction.contains(Action.MOVE_RIGHT);
        boolean leftPressed = playerAction.contains(Action.MOVE_LEFT);
        boolean debugPressed = playerAction.contains(Action.DEBUG);
        //#endregion

        //#region Timers
        int attackCooldownRemaining = player.getAttackCooldownRemaining();
        if (attackCooldownRemaining > 0) {
            player.setAttackCooldownRemaining(attackCooldownRemaining - 1);
        }
        int transformCooldownRemaining = player.getTransformCooldownRemaining();
        if (transformCooldownRemaining > 0) {
            player.setTransformCooldownRemaining(transformCooldownRemaining - 1);
        }

        //Sets the player's ticks in air falling
        if(!player.isGrounded() && player.getBodyVelocityY() < 0){
            player.setTicksInAir(player.getTicksInAir()+1);
        }
        else if (player.getTicksInAir() != 0){
            player.setTicksInAir(0);
        }

        int dashCooldownRemaining = player.getDashCooldownRemaining();
        if (dashCooldownRemaining > 0) {
            player.setDashCooldownRemaining(dashCooldownRemaining - 1);
        }

        if (player.getiFramesRemaining() > 0) {
            player.setiFramesRemaining(player.getiFramesRemaining() - 1);
            if (player.getiFramesRemaining() <= 0) {
                player.setHit(false);

            }
        }
        //#endregion

        //#region Direction
        if (player.isFacingRight()) {
            player.setAngleFacing(0);
        } else {
            player.setAngleFacing(180);
        }
        if ((rightPressed && !leftPressed && !upPressed && !downPressed) || (rightPressed && !leftPressed && upPressed && downPressed)) {
            player.setAngleFacing(0);
            player.setFacingRight(true);
        } else if (rightPressed && !leftPressed && upPressed) {
            player.setAngleFacing(45);
            player.setFacingRight(true);
        } else if ((rightPressed && leftPressed && upPressed && !downPressed) || (!rightPressed && !leftPressed && upPressed && !downPressed)) {
            player.setAngleFacing(90);
        } else if ((!rightPressed && leftPressed && upPressed && !downPressed)) {
            player.setAngleFacing(135);
            player.setFacingRight(false);
        } else if (!rightPressed && leftPressed && !upPressed && !downPressed || !rightPressed && leftPressed && upPressed) {
            player.setAngleFacing(180);
            player.setFacingRight(false);
        } else if (!rightPressed && leftPressed) {
            player.setAngleFacing(225);
            player.setFacingRight(false);
        } else if (!rightPressed && !upPressed && downPressed || rightPressed && leftPressed && !upPressed && downPressed) {
            player.setAngleFacing(270);
        } else if (rightPressed && !leftPressed) {
            player.setAngleFacing(315);
            player.setFacingRight(true);
        }
        //#endregion

        //#region Right Left Movement
        float y = player.getBodyVelocityY();
        float x = player.getBodyVelocityX();
        float max_speed;
        if (player.getForm() == 0) {
            max_speed = player.getMaxSpeed();
        } else {
            max_speed = player.getChiyoSpeedMult() * player.getMaxSpeed();
        }
        float h_acc = player.getHorizontalAcceleration();
        if ((rightPressed && leftPressed) || (!rightPressed && !leftPressed) && player.getDashLifespanRemaining() <= 0) {
            if (x > 0.0f) {
                x = Math.max(x - h_acc, 0);
                if (x < 0.0f) {
                    x = 0.0f;
                }
            } else if (x < 0.0f) {
                x = Math.min(x + h_acc, 0);
                if (x > 0.0f) {
                    x = 0.0f;
                }
            }
        } else if (rightPressed) {
            if (x < max_speed) {
                //x = Math.min(x+h_acc, max_speed);
                x = Math.min(x + h_acc, max_speed);
            }

        } else if (x > -max_speed && !player.isDashing()) {
                //x = Math.max(x-h_acc, -max_speed);
                x = Math.max(x - h_acc, -max_speed);
        }
        player.setVelocity(x, y);
        //#endregion

        //#region Form Switching
        if (transformPressed && player.getTransformCooldownRemaining() == 0 && player.getSpirit() > 1.0f) {
            player.setTransformCooldownRemaining(player.getTransformCooldown());
            if(player.getForm() == 0){
                player.setForm();
                player.setHeight(player.getHeight() * player.getChiyoHitBoxHeightMult());
//                playerChiyoTransformId = playSound( playerChiyoTransformSound, playerChiyoTransformId, 0.1F );
                audio.playEffect("chiyo-transform", 0.1f);
                player.updateGroundSensor();
            }
            else{
                player.setForm();
                player.setHeight(player.getHeight() / player.getChiyoHitBoxHeightMult());
//                playerMomoTransformId = playSound( playerMomoTransformSound, playerMomoTransformId, 0.1F );
                audio.playEffect("momo-transform", 0.1f);
                player.updateGroundSensor();
            }
        }
        //#endregion

        //#region Sword Attack
        if (attackPressed && player.getForm() == 1 && player.getAttackCooldownRemaining() == 0) {
            player.setAttackCooldownRemaining(player.getAttackCooldown());
            player.setAttacking(true);
            player.setAttackLifespanRemaining(player.getAttackLifespan());
            createSword();
//            swordSwipeSoundId = playSound( swordSwipeSound, swordSwipeSoundId, 0.05F );
            audio.playEffect("sword-swipe", 0.05f);
        }

        if (player.getAttackLifespanRemaining() > 0) {
            player.setAttackLifespanRemaining(Math.max(player.getAttackCooldownRemaining() - 1, 0));
            if (player.getAttackLifespanRemaining() == 0) {
                player.setAttacking(false);
            }
        }
        //#endregion

        //#region Dash
        if (player.getForm() == 0 && dashPressed && player.isGrounded() && player.getDashCooldownRemaining() == 0 && !player.isDashing() && !player.dashedInAir()
                || player.getForm() == 0 && dashPressed && player.getJumpVelocity() > 0 && player.getDashCooldownRemaining() == 0 && !player.isDashing() && !player.dashedInAir()) {
            player.setDashCooldownRemaining(player.getDashCooldown());
            player.setDashing(true);
            player.setDashedInAir(true);
            player.setDashLifespanRemaining((player.getDashLifespan()));
            int angleFacing = player.getAngleFacing();
            if (player.getIsJumping()){
                player.setIsJumping(false);
                player.setJumpTimeRemaining(0);
            }

            //Creates the dash effect
            //#region Dash Effect
            float diagonalDashMult = 0.71f;
            //direction effect angle
            float effectAngle = 0.0f;
            //offset of effect from player
            float pOffsetX = 0.0f;
            float pOffsetY = 0.0f;
            float pOffset = 1.0f;

            //Setting dash according to angles
            if (angleFacing == 0) {
                effectAngle = 1.57f;
                pOffsetX = -pOffset;
            }
            else if (angleFacing == 45) {
                effectAngle = 2.356f;
                pOffsetX = -diagonalDashMult * pOffset;
                pOffsetY = -diagonalDashMult * pOffset;
            } else if (angleFacing == 90) {
                effectAngle = 3.141f;
                pOffsetY = -pOffset;
                pOffsetX = 0.1f;
            } else if (angleFacing == 135) {
                effectAngle = 3.926f;
                pOffsetX = diagonalDashMult * pOffset;
                pOffsetY = -diagonalDashMult * pOffset;
            } else if (angleFacing == 180) {
                effectAngle = 4.712f;
                pOffsetX = 1.0f;
            } else if (angleFacing == 225) {
                effectAngle = 5.497f;
                pOffsetX = diagonalDashMult * pOffset;
                pOffsetY = diagonalDashMult * pOffset;
            } else if (angleFacing == 270) {
                effectAngle = 0.0f;
                pOffsetY = 1.0f;
            } else if (angleFacing == 315) {
                effectAngle = 0.785f;
                pOffsetX = -diagonalDashMult * pOffset;
                pOffsetY = diagonalDashMult * pOffset;
            }


            //Creating dash effect
            EffectObstacle dashAnimate = level.getEffectPool().obtainEffect(player.getX(), player.getY(), player.getDashEffectSpriteSheet().getRegionWidth(),
                    player.getDashEffectSpriteSheet().getRegionHeight(), 0.025f, 0.025f, effectAngle,
                    pOffsetX, pOffsetY,true,
                    "dashEffect", player, 0.35f,
                    1, 1, animations.get("dashEffect"),5);
            level.addQueuedObject(dashAnimate);
            //#endregion

            level.shakeControllerMedium();

            //Sound effect
//            dashSoundId = playSound(dashSound, dashSoundId, 0.05F);
            audio.playEffect("dash", 0.05f);

            //Setting Gravity to 0 and scheduling to set it back
            player.setPlayerGravity(0.0f);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(player.isRemoved()) return;
                    player.setPlayerGravity(player.getPlayerGravity());
                }
            }, player.getDashTime());
        }

        if (player.getDashLifespanRemaining() > 0) {
            player.setDashLifespanRemaining(Math.max(player.getDashLifespanRemaining() - 1, 0));

            if (player.getDashLifespanRemaining() == 0) {
                player.setDashing(false);
//                player.getBody().applyForce();
                System.out.println("xBody Velocity: " + player.getBodyVelocityX());
                System.out.println("yBody Velocity: " + player.getBodyVelocityY());
                System.out.println("isDashing: " + player.isDashing());
                player.setVelocity(player.getBodyVelocityX()/3, player.getBodyVelocityY()/3);
            }
        }

        if (player.isDashing()) {
            // Applies velocity to player
            int angleFacing = player.getAngleFacing();
            float dashX = 0;
            float dashY = 0.4f;
            float dash = player.getDash();
            float diagonalDashMult = 0.71f;
            //Setting dash according to angles
            if (angleFacing == 0) {
                dashX = dash;
                dashY = 0;
            }
            else if (angleFacing == 45) {
                dashX = diagonalDashMult * dash;
                dashY = diagonalDashMult * dash;
            } else if (angleFacing == 90) {
                dashX = 0;
                dashY = dash;
            } else if (angleFacing == 135) {
                dashX = -diagonalDashMult * dash;
                dashY = diagonalDashMult * dash;
            } else if (angleFacing == 180) {
                dashX = -dash;
                dashY = 0;
            } else if (angleFacing == 225) {
                dashX = - 0.71f * dash;
                dashY = - 0.71f * dash;
            } else if (angleFacing == 270) {
                dashX = 0;
                dashY = - dash;
            } else if (angleFacing == 315) {
                dashX = 0.71f * dash;
                dashY = - 0.71f * dash;
            }
            player.setVelocity(dashX, dashY);

            player.setDashCooldownRemaining(Math.max(player.getDashCooldownRemaining() - 1, 0));
            if (player.getDashCooldownRemaining() == 0){
                player.setDashing(false);
                Filter f =player.getFilterData();
                f.groupIndex = -1; //cancel its collision with bullet
                player.setFilterData(f);
            }
        } else {
            Filter f =player.getFilterData();
            f.groupIndex = 0;
            player.setFilterData(f);
        }

        if (!dashHold) player.setDashing(false);

        if (player.isGrounded() && !player.isDashing()) player.setDashedInAir(false);
        //#endregion

        //#region Jump
        //jump!
        //include all three situations
        //normal jump, coyote, and jump pressed in air
        boolean slidingJumpRequirements = player.isSliding() && ((ticks - player.getWallJumpCooldownTicks() > lastWallJumpTick) || player.isFacingRight() && !facingRightDuringLastWallJump || !player.isFacingRight() && facingRightDuringLastWallJump);
        if ((jumpPressed && (player.isGrounded() || slidingJumpRequirements) && player.getJumpCooldownRemaining() == 0) ||
                (jumpPressed && player.getCoyoteFramesRemaining() > 0 && player.getJumpCooldownRemaining() == 0) ||
                (player.getJumpPressedInAir() && player.getJumpCooldownRemaining() == 0 && (player.isGrounded() ||slidingJumpRequirements))) {
            jump();
            audio.playEffect("jump", 1.0f);
            if (slidingJumpRequirements) {
                lastWallJumpTick = ticks;
                facingRightDuringLastWallJump = player.isFacingRight();
            }
        } else if (player.isGrounded() && player.getBodyVelocityY() == 0) {
            player.setIsJumping(false);
        }

        if (player.getIsJumping()) player.setJumpTimeRemaining(player.getJumpTimeRemaining() - 1);
        else player.setJumpCooldownRemaining(Math.max(0, player.getJumpCooldownRemaining() - 1));

        if (jumpHold && player.getIsJumping() && player.getJumpTimeRemaining() > 0) {
            player.setVelocity(player.getBodyVelocityX(), player.getJumpVelocity());
        }

        if (!jumpHold) player.setIsJumping(false);

        //calculate coyote time
        if (player.isGrounded()) {
            player.setCoyoteFramesRemaining(player.getCoyoteFrames());
        } else {
            player.setCoyoteFramesRemaining(Math.max(0, player.getCoyoteFramesRemaining() - 1));
        }

//        //jump pressed in air
        if (jumpPressed && !(player.isGrounded() || player.isSliding())) {
            player.setJumpToleranceRemaining(player.getJumpTolerance());
            player.setJumpPressedInAir(true);
        } else if (!(player.isGrounded() || player.isSliding())) {
            player.setJumpToleranceRemaining(Math.max(0, player.getJumpToleranceRemaining() - 1));
            if (player.getJumpToleranceRemaining() == 0) player.setJumpPressedInAir(false);
        }
        //#endregion

        //#region Wall Slide
        if (player.isTouchingWallRight() && !player.isGrounded() && rightPressed && !jumpHold){ //player sliding on right wall
            player.setSliding(true);
        } else if (player.isTouchingWallLeft() && !player.isGrounded() && leftPressed && !jumpHold) { //player is sliding on left wall
            player.setSliding(true);
        }
        else{
            player.setSliding(false);
        }

        if (player.isSliding()){
            player.setVelocity(player.getBodyVelocityX(), player.getWallSlideVelocity());
        }
        //#endregion

        if (debugPressed) {
            level.setDebug(!level.isDebug());
        }

        // What does this do
        if (deltaX == 0 && !player.isGrounded() && (leftPressed || rightPressed)
                && Math.abs(player.getBodyVelocityX()) == player.getHorizontalAcceleration() && movedDuringLastFrame) {
            player.setVelocity(0, player.getBodyVelocityY());
        }
        previousX = player.getX();
        movedDuringLastFrame = (leftPressed || rightPressed) && deltaX > 0;

        //#region Spirit Loss
        if (player.getForm() == 1) { // if player is chiyo
            player.decreaseSpirit();
            if (player.getSpirit() <= 0) {
                player.setForm(); // switch back to momo
                player.setHeight(player.getHeight() / player.getChiyoHitBoxHeightMult());
                player.updateGroundSensor();
            }
        } else if (player.getEnemiesInSpiritRange().size > 0) {
            player.getEnemiesInSpiritRange().get(0).LossSpirit(level.getPlayer().getSpiritIncreaseRate());
            player.increaseSpirit();
            spiritDrainEffect = level.getEffectPool().obtainEffect(player.getX(), player.getY(),
                    player.getSpiritDrainSpriteSheet().getRegionWidth(), player.getSpiritDrainSpriteSheet().getRegionHeight(),
                    0.01f, 0.01f, 0, 0, 0, true, "spiritDrain", player, 1f,
                    1, 1, player.getSpiritDrainAnimation(), 5);
            level.addQueuedObject(spiritDrainEffect);
        }
        //#endregion

        //#region Textures and Animation
        if (tickFrameSwitch != 0 && ticks % tickFrameSwitch == 0) {
            currentFrame++;
            if (currentFrame >= maxFrame) {
                currentFrame = 0;
            }
        }

        //Animation if Player is Momo
        if (player.getForm() == 0) {
//            chiyoRunSound.stop();
            audio.stopEffect("wall-slide");
            audio.stopEffect("chiyo-run");
            // Momo Dashing
            if (player.isDashing()) {
                TextureRegion current;
                currentAnimation = "momoDash";
                if (player.getAngleFacing() == 45 || player.getAngleFacing() == 135){
                    current = player.getMomoDiagonalDashTexture();
                    player.setSxMult(1.2f);
                    player.setSyMult(1.2f);
                    player.setOxOffset(0);
                    player.setOyOffset(-10);
                }
                else if (player.getAngleFacing() == 90){
                    current = player.getMomoUpDashTexture();
                    player.setSxMult(1.2f);
                    player.setSyMult(1.2f);
                    player.setOxOffset(-0);
                    player.setOyOffset(-10);
                }
                else if (player.getAngleFacing() == 270){
                    current = player.getMomoDownDashTexture();
                    player.setSxMult(1.2f);
                    player.setSyMult(1.2f);
                    player.setOxOffset(0);
                    player.setOyOffset(-20);
                }
                else{
                    current = (TextureRegion) (animations.get("momoDash")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                    tickFrameSwitch = 7;
                    maxFrame = 5;
                    player.setSxMult(1.2f);
                    player.setSyMult(1.2f);
                    player.setOxOffset(0);
                    player.setOyOffset(-47);
                }
                player.setTexture(current);
            }
            else if (player.isSliding()){
                player.setTexture(player.getMomoSlideTexture());
                audio.loopEffect("wall-slide", 1f);
                player.setOxOffset(-4);
                player.setOyOffset(-15);
                player.setSxMult(-1.2f);
                player.setSyMult(1.2f);
                currentAnimation = "momoSlide";
            }
            // Momo jumping/falling
            else if (!player.isGrounded()) {
                if (!currentAnimation.equals("momoJump")) {
                    currentFrame = 0;
                }
                currentAnimation = "momoJump";
                TextureRegion current = (TextureRegion) (animations.get("momoJump")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 7;
                maxFrame = 7;
                if (currentFrame == 0 && player.getBodyVelocityY() < 0){
                    currentFrame = 3;
                }
                // Disables animation while rising in the air
                if (currentFrame == 2 && player.getBodyVelocityY() > 0){
                    tickFrameSwitch = 0;
                }
                if (currentFrame == 4 && player.getBodyVelocityY() < 0){
                    tickFrameSwitch = 0;
                }
                player.setTexture(current);
                player.setOxOffset(0);
                player.setOyOffset(-47);
                player.setSxMult(1.2f);
                player.setSyMult(1.2f);
            }
//            else if (player.isGrounded() && currentAnimation == "momoJump"){
//                currentAnimation = "momoLand";
//                TextureRegion current = (TextureRegion) (animations.get("momoJump")).getKeyFrame(5);
//                tickFrameSwitch = 7;
//                maxFrame = 7;
//                player.setTexture(current);
//                player.setOyOffset(-47);
//                player.setSxMult(1.2f);
//                player.setSyMult(1.2f);
//            }
            else if (player.getBodyVelocityX() != 0) {
                if (!currentAnimation.equals("momoRun")) {
                    currentFrame = 0;
                }
                currentAnimation = "momoRun";
                if(player.isGrounded() && !player.getIsJumping()){
//                    if(!soundDictionary.contains(momoRunSound)){
//                        soundDictionary.add(momoRunSound);
//                        momoRunSound.loop();
//                    }
                    audio.loopEffect("momo-run", 1.0f);
                }
                else if(!player.isGrounded() || player.getIsJumping()){
//                    momoRunSound.stop();
//                    soundDictionary.remove(momoRunSound);
                    audio.stopEffect("momo-run");
                }

                TextureRegion current = (TextureRegion) (animations.get("momoRun")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                maxFrame = 8;
                tickFrameSwitch = 5;
                player.setTexture(current);
                player.setOxOffset(0);
                player.setOyOffset(-47);
                player.setSxMult(1.2f);
                player.setSyMult(1.2f);
            } else {
                currentAnimation = "momoIdle";
//                momoRunSound.stop();
//                soundDictionary.remove(momoRunSound);
                audio.stopEffect("momo-run");
                player.setTexture(player.getMomoTexture());
                player.setOxOffset(0);
                player.setOyOffset(-220);
                player.setSxMult(1.2f);
                player.setSyMult(1.2f);
            }
        }
        // Animations if Player is Chiyo
        else {
//            momoRunSound.stop();
            audio.stopEffect("momo-run");
            if(player.isAttacking()) {
//                chiyoRunSound.stop();
//                soundDictionary.remove(chiyoRunSound);
                audio.stopEffect("chiyo-run");
                if (!currentAnimation.equals("chiyoAttack")) {
                    currentFrame = 0;
                }
                currentAnimation = "chiyoAttack";
                TextureRegion current = (TextureRegion) (animations.get("chiyoAttack")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 4;
                maxFrame = 12;
                player.setTexture(current);
                player.setOxOffset(20);
                player.setOyOffset(-25);
                player.setSxMult(2.3f);
                player.setSyMult(2.0f);
            }
            // if chiyo is sliding
            else if (player.isSliding()){
                player.setTexture(player.getChiyoSlideTexture());
                audio.loopEffect("wall-slide", 1f);
                player.setOxOffset(2);
                player.setOyOffset(-29);
                player.setSxMult(-2.1f);
                player.setSyMult(1.84f);
                currentAnimation = "chiyoSlide";
            }
            else if (!player.isGrounded()) {
//                chiyoRunSound.stop();
//                soundDictionary.remove(chiyoRunSound);
                audio.stopEffect("chiyo-run");
                if (!currentAnimation.equals("chiyoJump")) {
                    currentFrame = 0;
                }
                currentAnimation = "chiyoJump";
                TextureRegion current = (TextureRegion) (animations.get("chiyoJump")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 8;
                maxFrame = 8;
                if (currentFrame == 0 && player.getBodyVelocityY() < 0){
                    currentFrame = 3;
                }
                // Disables animation while rising in the air
                if (currentFrame == 2 && player.getBodyVelocityY() > 0){
                    tickFrameSwitch = 0;
                }
                if (currentFrame == 4 && player.getBodyVelocityY() < 0){
                    tickFrameSwitch = 0;
                }
                player.setTexture(current);
                player.setOxOffset(20);
                player.setOyOffset(-25);
                player.setSxMult(2.0f);
                player.setSyMult(2.0f);
            } else if(player.getBodyVelocityX() != 0){
                currentAnimation = "chiyoRun";
                if(player.isGrounded() && !player.getIsJumping()){
//                    if(!soundDictionary.contains(chiyoRunSound)) {
//                        soundDictionary.add(chiyoRunSound);
//                        chiyoRunSound.loop();
//                    }
                    audio.loopEffect("chiyo-run", 1.0f);
                }
                TextureRegion current = (TextureRegion) (animations.get("chiyoRun")).getKeyFrame(currentFrame); // Gets the current frame of the animation
                tickFrameSwitch = 4;
                maxFrame = 7;
                player.setTexture(current);
                player.setOxOffset(20);
                player.setOyOffset(-25);
                player.setSxMult(2.0f);
                player.setSyMult(2.0f);

            } else {
                currentAnimation = "chiyoIdle";
//                chiyoRunSound.stop();
//                soundDictionary.remove(chiyoRunSound);
                audio.stopEffect("chiyo-run");
                audio.stopEffect("wall-slide");
                player.setTexture(player.getChiyoTexture());
                player.setOxOffset(0);
                player.setOyOffset(-29);
                player.setSxMult(2.3f);
                player.setSyMult(2.0f);
            }
        }
        //Creating impact animation for large jumps
        if (!player.isGrounded() && player.getTicksInAir() > player.getTimeForImpact()) {
            impact = true;
        }
        //Small impact sound effect for smaller jumps
        if (!player.isGrounded() && player.getTicksInAir() <= player.getTimeForImpact()) {
            smallImpact = true;
        }
        if(smallImpact && player.isGrounded()){
            smallImpact = false;
//            smallImpactSoundId = playSound(smallImpactSound, smallImpactSoundId, 0.8F);
            audio.playEffect("small-impact", 0.8f);
        }
        //next time the player is grounded, create the impact animation
        if (impact && player.isGrounded()) {
            impact = false;
            float effectAngle = 0.0f;
            //offset of effect from player
            float pOffsetX = 0.0f;
            float pOffsetY = -0.33f;
            EffectObstacle impactAnimate = level.getEffectPool().obtainEffect(player.getX(), player.getY(), player.getImpactEffectSpriteSheet().getRegionWidth(),
                    player.getImpactEffectSpriteSheet().getRegionHeight(), 0.02f, 0.02f, effectAngle,
                    pOffsetX, pOffsetY, true,
                    "impactEffect", player, 0.35f,
                    1, 1, animations.get("impactEffect"), 5);
            level.addQueuedObject(impactAnimate);
//            impactId = playSound(impactSound, impactId, 0.3F);
            audio.playEffect("impact", 0.3f);
            level.shakeControllerSmall();

        }

        //#endregion



    }
//    public void drawSpiritEffect(){
//        spiritDrainEffect = level.getEffectPool().obtainEffect(player.getX(), player.getY(),
//                player.getSpiritDrainSpriteSheet().getRegionWidth(), player.getSpiritDrainSpriteSheet().getRegionHeight(),
//                0.01f, 0.01f, 0, 0, 0, true, "spiritDrain", player, 1f,
//                1, 1, player.getSpiritDrainAnimation(), 5);
//        level.addQueuedObject(spiritDrainEffect);
//        level.removeQueuedObject(spiritDrainEffect);
//    }

    /**
     * Causes the player to jump.
     */
    private void jump() {
        //Sound Effect
        //jumpId = playSound( jumpSound, jumpId, 0.5F );

        if (player.isSliding()){
            player.setVelocity((player.isFacingRight() ? -1 : 1) * player.getWallJumpXVelocity(), player.getJumpVelocity());
        }
        else{
            player.setVelocity(player.getBodyVelocityX(), player.getJumpVelocity());
        }
        player.setJumpCooldownRemaining(player.getJumpCooldown());
        player.setJumpTimeRemaining(player.getJumpTime());
        player.setJumpTimeRemaining(
                player.getForm()==0 ? player.getJumpTime() : (int) (player.getJumpTime()
                        * player.getChiyoJumpTimeMult()));
        player.setIsJumping(true);
    }


    /**
     * Resolves the set of enemy actions
     *
     * @param enemyActions an array of sets of enemy actions to resolve, each set representing
     *                     the actions of one enemy
     */
    public void resolveEnemyActions(Array<EnumSet<EnemyAction>> enemyActions) {
        for (int i = 0; i < enemies.length; i++) {
            EnumSet<EnemyAction> enemyAction = enemyActions.get(i);
            Enemy enemy = enemies[i];
            if (!enemy.isRemoved()) {

                for (EnemyAction action : enemyAction) {

                    if (action == EnemyAction.ATTACK) {
                        createBullet(aiControllers.get(i).getVelocity(),enemy);
                        enemy.startToMove("move",60);
                    } else if(action==EnemyAction.FLY){
                        enemy.setVelocity(aiControllers.get(i).getVelocity());
                        enemy.applyVelocity();
                    }else{
                        enemy.setMovement(action);
                        enemy.applyVelocity();
                    }
                }
                if (enemy.getVelocityX() > 0){
                    enemy.setFacingRight(true);
                }
                else if(enemy.getVelocityX() < 0){
                    enemy.setFacingRight(false);
                }

                //#region Enemy Animation
                if (ticks % enemy.getTickFrameSwitch() == 0) {
                    enemy.setCurrentFrame(enemy.getCurrentFrame() + 1);
                    if (enemy.getCurrentFrame() >= enemy.getMaxFrame()) {
                        enemy.setCurrentFrame(0);
                    }
                }
                //Purple Skull Guy
                switch (enemy.getType()) {
                    case "Goomba":
                    //Green Dragon
                    case "FlyGuardian":
                        //Green Mosquito
                    case "Fly":
                        enemy.setCurrentAnimation("move");
                        break;
                    //Green Beetle
                    case "GoombaGuardian":
                        if (enemy.getVelocityX() != 0) {
                            enemy.setCurrentAnimation("move");
                        } else {
                            enemy.setCurrentAnimation("idle");
                        }
                        break;

                    //Tank guy
                    case "Fast":
                        if (enemy.getVelocityX() != 0) {
                            enemy.setCurrentAnimation("move");
                            enemy.setOyOffset(-29);
                        } else {
                            enemy.setCurrentAnimation("idle");
                            enemy.setOyOffset(-10);
                        }
                        break;
                    // Projectile
                    case "Projectile":
                        enemy.durationloss();
                        break;
                }
                //#endregion
                enemyAction.clear();
            }
        }
    }
    /**
     * Add a new bullet to the world
     */
    private void createBullet(Vector2 v, Enemy enemy){
        JsonValue bulletjv = enemy.getBullet();
        float offset_c = bulletjv.getFloat("offset",0);
        //normalize the velocity
        v.nor();
        TextureRegion bulletTexture= enemy.getBulletTexture();
        Vector2 offset = new Vector2(v.x*offset_c, v.y*offset_c);
        Vector2 scale = new Vector2(8.0f, 8.0f);
        float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(enemy.getX()+offset.x, enemy.getY()+offset.y, 0.5f * radius);

        bullet.setName("bullet");
        bullet.setDensity(bulletjv.getFloat("density", 0));
        bullet.setTexture(bulletTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);
        Filter f =bullet.getFilterData();
        f.groupIndex = -1; //cancel its collision with enemy
        bullet.setFilterData(f);

        // Compute position and velocity
        float speed = bulletjv.getFloat( "speed", 0 );
        Vector2 s= new Vector2(v.x*speed, v.y*speed);
        bullet.setVX(s.x);
        bullet.setVY(s.y);
        level.addQueuedObject(bullet);
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
        //When on ground, downward slashes should just be left or right slash
        if(player.getBodyVelocityY() == 0 && (angleFacing == 225 || angleFacing == 270 || angleFacing == 315) && !player.getIsJumping()){
            if(angleFacing == 225){
                x = player.getX() - currOffset;
                y = player.getY();
                angleFacing = 180;
            }
            else if(angleFacing == 270){
                if(player.isFacingRight()){
                    angleFacing = 0;
                }
                else{
                    x = player.getX() - currOffset;
                    y = player.getY();
                    angleFacing = 180;
                }
            }
            else {
                angleFacing = 0;
            }
        }
        if (angleFacing == 45) {
            x = player.getX() + 0.71f * currOffset;
            y = player.getY() + 0.71f * currOffset;
        } else if (angleFacing == 90) {
            x = player.getX();
            y = player.getY() + 1.3f * currOffset;
        } else if (angleFacing == 135) {
            x = player.getX() - 0.71f * currOffset;
            y = player.getY() + 0.71f * currOffset;
        } else if (angleFacing == 180) {
            x = player.getX() - currOffset;
            y = player.getY();
        } else if (angleFacing == 225) {
            x = player.getX() - 0.71f * currOffset;
            y = player.getY() - 0.71f * currOffset;
        } else if (angleFacing == 270) {
            x = player.getX();
            y = player.getY() - currOffset;
        } else if (angleFacing == 315) {
            x = player.getX() + 0.71f * currOffset;
            y = player.getY() - 0.71f * currOffset;
        }

        Vector2 scale = new Vector2(64.0f, 64.0f);
        SwordWheelObstacle sword = new SwordWheelObstacle(x, y, player.getSwordRadius(), player, player.getAttackLifespan(), 10.0f, scale, player.getSwordEffectSpriteSheet(angleFacing));
        level.addQueuedObject(sword);
        level.shakeControllerSmall();
    }

    //#endregion

    private void addAnimations(TextureRegion spriteSheet, int columns, int rows, String name) {
        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth() / columns, spriteSheet.getRegionHeight() / rows);
        Animation<TextureRegion> animation = new Animation<TextureRegion>(1f, frames[0]); // Creates an animation with a frame duration of 0.1 seconds
        animation.setPlayMode(Animation.PlayMode.NORMAL); // Sets the animation to play normally
        animations.put(name, animation);
    }

    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     * @param volume	The sound volume
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
    }


    public void dispose() {

    }
}


