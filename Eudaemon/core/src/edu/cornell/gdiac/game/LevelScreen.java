package edu.cornell.gdiac.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
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

    private CollisionController collisionController;

    public LevelScreen(Level level, ActionBindings actionBindings) {
        this.level = level;

        this.active = false;
        this.inputController = new InputController(actionBindings);
        this.playerAction = EnumSet.noneOf(Action.class);
        Enemy[] enemies=level.getEnemies();
        this.aiControllers = new Array<>(enemies.length);
        this.enemyActions = new Array<>(enemies.length);
        for (int i = 0; i < enemies.length; i++) {
            if (enemies[i].getType()=="Goomba"){aiControllers.add(new GoombaAI(i, level));}
            else if (enemies[i].getType()=="Fly") {aiControllers.add(new FlyAI(i, level));}
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
