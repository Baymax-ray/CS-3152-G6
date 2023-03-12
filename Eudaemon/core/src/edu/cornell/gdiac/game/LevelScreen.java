package edu.cornell.gdiac.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.game.models.*;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.EnumSet;

public class LevelScreen implements Screen {

    private boolean active;

    private Level level;

    private ScreenListener listener;
    private GameCanvas canvas;

    private InputController inputController;
    private EnumSet<Action> playerAction;

    private Array<AIController> aiControllers;
    private Array<EnumSet<EnemyAction>> enemyActions;

    private ActionController actionController;

    private World world;
    private CollisionController collisionController;

    public LevelScreen(Level level, ActionBindings actionBindings, GameCanvas canvas) {
        this.level = level;

        this.active = false;
        this.inputController = new InputController(actionBindings);
        this.playerAction = EnumSet.noneOf(Action.class);
        this.aiControllers = new Array<>(level.getEnemies().length);
        this.enemyActions = new Array<>(level.getEnemies().length);

        for (int i = 0; i < level.getEnemies().length; i++) {
            aiControllers.add(new AIController(i, level));
            // initializes an empty enumset with the right type
            enemyActions.add(EnumSet.noneOf(EnemyAction.class));
        }

        actionController = new ActionController(level);

        world = new World(new Vector2(0, level.getGravity()), true);
        collisionController = new CollisionController(level);
        world.setContactListener(collisionController);
        level.activatePhysics(world);
        this.setCanvas(canvas);
    }


    public void update(float delta) {
        inputController.setPlayerAction(playerAction);
        for (int i = 0; i < aiControllers.size; i++) {
            aiControllers.get(i).setEnemyAction(enemyActions.get(i));
        }
        actionController.resolveActions(playerAction, enemyActions);
        // TODO: pull magic numbers below to json
        world.step(delta, 6, 2);
        level.update(delta);
    }

    public void draw(float delta) {
        canvas.clear();
        canvas.begin();
        this.level.draw(canvas);
        canvas.end();
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
