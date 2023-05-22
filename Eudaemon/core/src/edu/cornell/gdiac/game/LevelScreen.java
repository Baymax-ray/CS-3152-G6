package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.EnumSet;

public class LevelScreen implements Screen, SettingsObserver {

    private boolean active;

    private final Level level;

    private ScreenListener listener;
    private GameCanvas canvas;

    private final InputController inputController;
    private final EnumSet<Action> playerAction;

    private final Array<AIController> aiControllers;
    private final Array<EnumSet<EnemyAction>> enemyActions;

    private final ActionController actionController;

    private final CollisionController collisionController;
    private AudioController audio;

    private Settings settings;


    public Level getLevel() {
        return level;
    }
    public AudioController getAudio(){return this.audio;}

    public LevelScreen(Level level, ActionBindings actionBindings, AssetDirectory assets, Settings settings) {
        this.settings = settings;
        this.settings.addObserver(this);

        this.level = level;
        level.setDifficulty(settings.getLevelDifficulty());
        level.setCameraShakeOn(settings.isScreenShake());

        this.active = false;
        this.inputController = new InputController(actionBindings);
        this.playerAction = EnumSet.noneOf(Action.class);

        //POPULATE ENEMIES
        Enemy[] enemies= level.getEnemies();
        this.aiControllers = new Array<>(enemies.length);
        this.enemyActions = new Array<>(enemies.length);
        for (int i = 0; i < enemies.length; i++) {
            switch (enemies[i].getType()) {
                case "Goomba":
                case "Fast":
                    aiControllers.add(new GoombaAI(i, level));
                    break;
                case "Fly":
                case "FlyRed":
                    aiControllers.add(new FlyAI(i, level));
                    break;
                case "FlyGuardian":
                    aiControllers.add(new FlyGuardianAI(i, level));
                    break;
                case "GoombaGuardian":
                    aiControllers.add(new GoombaGuardianAI(i, level));
                    break;
                case "Projectile":
                    aiControllers.add(new ProjectileAI(i, level));
                    break;
                case "MOMO":
                    aiControllers.add(new DumbAI(i, level));
                    break;
                default:
                    //should not get here
                    throw new IllegalArgumentException("Enemy type does not exist: "+enemies[i].getType());
            }
            // initializes an empty enumset with the right type
            enemyActions.add(EnumSet.noneOf(EnemyAction.class));
        }

        audio = new AudioController(assets, settings);
        // shouldn't play immediately, other screens might be shown

        actionController = new ActionController(level,aiControllers, audio);

        collisionController = new CollisionController(level, audio);
        level.getWorld().setContactListener(collisionController);
        level.activatePhysics();
    }


    public void update(float delta) {
        inputController.setPlayerAction(playerAction);
        for (int i = 0; i < aiControllers.size; i++) {
            aiControllers.get(i).setEnemyAction(enemyActions.get(i));
        }
        actionController.resolveActions(playerAction, enemyActions);

        audio.updateAudio(this.level.getPlayer().getForm());

        level.update(delta);
    }


    /** this function checks the player input and/or level state to determine whether to exit the screen */
    private void checkScreenTransitions() {
        if (this.level.getPlayer().getHearts() == 0 || !this.level.inBounds(level.getPlayer())) {
            listener.exitScreen(this, ExitCode.LOSE);
        } else if (playerAction.contains(Action.RESET)) {
            listener.exitScreen(this, ExitCode.RESET);
        } else if (level.isCompleted()) {
            listener.exitScreen(this, ExitCode.WIN);
        } else if (playerAction.contains(Action.PAUSE)) {
            //TODO: pause screen
            listener.exitScreen(this, ExitCode.PAUSE);
        }
    }

    public void draw(float delta) {
        canvas.clear();
        this.level.draw(canvas);

    }


    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
            checkScreenTransitions();
        }
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void resize(int width, int height) {
        // IGNORE FOR NOW
        // TODO: probably just update canvas and camera with new size
        canvas.getViewport().update(width, height);
    }

    @Override
    public void pause() {
        //empty in labs
    }

    @Override
    public void resume() {
        //empty in labs

    }

    @Override
    public void show() {
        active = true;

        audio.playAllSound();
        audio.muteChiyo();
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void hide() {
        active = false;
        audio.muteChiyo();
        audio.muteMomo();
    }

    @Override
    public void dispose() {
        // TODO: dispose of everything that needs it to prevent memory leaks
        collisionController.dispose();
        actionController.dispose();
        aiControllers.clear();
        enemyActions.clear();
        settings.removeObserver(this);
        canvas = null;
        listener = null;
        audio.dispose();
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDifficultyChange(int newDifficulty) {
        level.settingsChanged = true;
		level.setDifficulty(newDifficulty);
    }

}
