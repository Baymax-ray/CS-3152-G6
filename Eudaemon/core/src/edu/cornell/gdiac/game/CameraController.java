package edu.cornell.gdiac.game;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.game.models.CameraShaker;
import edu.cornell.gdiac.game.models.Level;


public class CameraController {
    private final OrthographicCamera camera;
    private CameraShaker cameraShaker;
    private final float shakeRadius;
    private final float minimumShakeRadius;
    private final float radiusFallOffFactor;

    public CameraController(){
        // Set the projection matrix (for proper scaling)
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        shakeRadius = 0.5f;
        minimumShakeRadius = 0.05f;
        radiusFallOffFactor = 0.90f;
        cameraShaker = new CameraShaker(camera, shakeRadius, minimumShakeRadius, radiusFallOffFactor);
    }

    public void handleGameplayCamera(GameCanvas canvas, Level level) {
        if (level.getPlayer().isRemoved()) return;



        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            camera.zoom -= 0.02;
        }

        float camZone_x = level.getCamZoneX();
        float camZone_y = level.getCamZoneY();

        if (Math.abs(camera.position.x - level.getPlayer().getX()) > camZone_x) {
            if (camera.position.x > level.getPlayer().getX()) {
                setGameplayCamera(canvas, level.getPlayer().getX()+camZone_x, camera.position.y, level.getCameraWidth(), level.getCameraHeight());
            }

            else {
                setGameplayCamera(canvas, level.getPlayer().getX()-camZone_x, camera.position.y, level.getCameraWidth(), level.getCameraHeight());
            }
        }

        if (Math.abs(camera.position.y - level.getPlayer().getY()) > camZone_y) {
            if (camera.position.y > level.getPlayer().getY())
                setGameplayCamera(canvas, camera.position.x, level.getPlayer().getY()+camZone_y, level.getCameraWidth(), level.getCameraHeight());

            else setGameplayCamera(canvas, camera.position.x, level.getPlayer().getY()-camZone_y, level.getCameraWidth(), level.getCameraHeight());
        }

        if (camera.position.x < level.getCameraWidth()/2)
            setGameplayCamera(canvas, level.getCameraWidth()/2, camera.position.y, level.getCameraWidth(), level.getCameraHeight());

        cameraShaker.updateOrigPosition();
    }

    /**
     * Sets the projection matrix to draw objects in a level
     * @param x the x-coordinate of the center of the camera in level coordinates
     * @param y the y-coordinate of the center of the camera in level coordinates
     * @param width the width in level coordinates that the window should display horizontally
     * @param height the height in level coordinates that the window should display vertically
     */
    public void setGameplayCamera(GameCanvas canvas, float x, float y, float width, float height) {
        getCamera().setToOrtho(false, width, height);
        getCamera().position.set(x, y, 0); // set to some other position to follow player;
        getCamera().update();

        canvas.getSpriteBatch().setProjectionMatrix(getCamera().combined);
        cameraShaker.update(Gdx.graphics.getDeltaTime());

        System.out.println(x);
        System.out.println(y);
        System.out.println();



    }


    /**
     * Sets the projection matrix to draw overlay and menu elements
     */
    public void setOverlayCamera(GameCanvas canvas) {
        //getCamera().position.set(getWidth() / 2, getHeight() / 2, 0); // set to some other position to follow player;
        getCamera().setToOrtho(false, canvas.getWidth(), canvas.getHeight());
        getCamera().update();
        canvas.getSpriteBatch().setProjectionMatrix(getCamera().combined);
    }


    public OrthographicCamera getCamera() {
        return camera;
    }

    public CameraShaker getCameraShaker() { return cameraShaker; }
}
