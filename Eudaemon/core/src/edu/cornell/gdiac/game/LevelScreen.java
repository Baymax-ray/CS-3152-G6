package edu.cornell.gdiac.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.EnumSet;

public class LevelScreen implements Screen {

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


    public LevelScreen(Level level, ActionBindings actionBindings) {
        this.level = level;

        this.active = false;
        this.inputController = new InputController(actionBindings);
        this.playerAction = EnumSet.noneOf(Action.class);

        //POPULATE ENEMIES
        Enemy[] enemies= level.getEnemies();
        this.aiControllers = new Array<>(enemies.length);
        this.enemyActions = new Array<>(enemies.length);
        for (int i = 0; i < enemies.length; i++) {
            if (enemies[i].getType().equals("Goomba")){aiControllers.add(new GoombaAI(i, level));}
            else if (enemies[i].getType().equals("Fly")) {aiControllers.add(new FlyAI(i, level));}
            // initializes an empty enumset with the right type
            enemyActions.add(EnumSet.noneOf(EnemyAction.class));
        }

        actionController = new ActionController(level);

        collisionController = new CollisionController(level);
        level.getWorld().setContactListener(collisionController);
        level.activatePhysics();
    }


    public void update(float delta) {
        inputController.setPlayerAction(playerAction);
        for (int i = 0; i < aiControllers.size; i++) {
            aiControllers.get(i).setEnemyAction(enemyActions.get(i));
        }
        actionController.resolveActions(playerAction, enemyActions);

        level.update(delta);

        checkScreenTransitions();
    }


    /** this function checks the player input and/or level state to determine whether to exit the screen */
    private void checkScreenTransitions() {
        if (this.level.getPlayer().getHearts() == 0) {
            listener.exitScreen(this, ExitCode.LOSE);
        }

        if (playerAction.contains(Action.RESET)) {
            listener.exitScreen(this, ExitCode.RESET);
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
    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {
        // TODO: dispose of everything that needs it to prevent memory leaks!
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}
