package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import edu.cornell.gdiac.game.CameraController;

public class CameraShaker {

    private Camera camera;
    private boolean isShaking = false;
    private int shakeTimes = 0;
    private float origShakeRadius;
    private float minimumShakeRadius;
    private float radiusFallOffFactor;
    private float shakeRadius;
    private float randomAngle;
    private float timer;
    private Vector3 offset;
    private Vector3 backgroundShakeOffset;
    private Vector3 currentPosition;
    public Vector3 origPosition;
    public Vector3 startPosition;

    public Vector3 getOffset() { return offset; }
    public Vector3 getStartPosition() { return startPosition; }
    public Vector3 getCurrentPosition() { return currentPosition; }


    /**
     * Constructor
     *
     * @param camera                the camera that we want to shake
     * @param shakeRadius           must be greater than 0
     * @param minimumShakeRadius    must be greater than 0 and less than shakeRadius
     * @param radiusFallOffFactor   must be greater than 0 and less than 1
     */
    public CameraShaker(Camera camera, float shakeRadius, float minimumShakeRadius, float radiusFallOffFactor){
        checkParameters(shakeRadius, minimumShakeRadius, radiusFallOffFactor);
        this.camera = camera;
        this.offset = new Vector3();
        this.backgroundShakeOffset = new Vector3();
        this.currentPosition = new Vector3();
        this.origPosition = camera.position.cpy();
        reset();
    }

    /**
     * Start the camera shaking.
     */
    public void startShaking(){
        startPosition = camera.position.cpy();
        reset();
        isShaking = true;
    }

    /**
     * Call this together with camera's update method.
     * Actually does the shaking.
     */
    public void update(float delta, CameraController cameraController, Level level){
        if (!isCameraShaking()) return;

        // only update camera shake 60 times a second max
        timer += delta;
        if (timer >= 1f/60f) {
            computeCameraOffset();
            computeCurrentPosition(cameraController, level);
            diminishShake();
            camera.position.set(currentPosition);
//            if (shakeTimes < 5) {
//                camera.position.set(currentPosition);
//                shakeTimes += 1;
//            }
//            else {
//                camera.position.set(startPosition);
//                shakeTimes = 0;
//            }
            camera.update();
            timer = 0;
        }
    }

    /**
     * Called by diminishShake() when minimum shake radius reached to stop shaking.
     */
    public void reset(){
        shakeRadius = origShakeRadius;
        isShaking = false;
        seedRandomAngle();
        currentPosition = origPosition.cpy();
        timer = 0;
    }

    /**
     * Check if camera is currently shaking.
     *
     * @return is the camera currently shaking.
     */
    public boolean isCameraShaking(){
        return isShaking;
    }

    /**
     *  Updates the position of the camera, since it moves with the player
     */
    public void updateOrigPosition() {
        this.origPosition = camera.position.cpy();
    }

    private void seedRandomAngle(){
        randomAngle = MathUtils.random(1, 360);
    }

    private void computeCameraOffset(){
        float sine = MathUtils.sinDeg(randomAngle);
        float cosine = MathUtils.cosDeg(randomAngle);
        offset.x = cosine * shakeRadius;
        offset.y = sine * shakeRadius;
    }

    private void computeCurrentPosition(CameraController cameraController, Level level) {
        if (cameraController.isCameraInBound(origPosition.x + offset.x, origPosition.y, level))
            currentPosition.x = origPosition.x + offset.x;
        if (cameraController.isCameraInBound(origPosition.x, origPosition.y + offset.y, level))
            currentPosition.y = origPosition.y + offset.y;
        backgroundShakeOffset.x = currentPosition.x - origPosition.x;
        backgroundShakeOffset.y = currentPosition.y - origPosition.y;
    }

    private void diminishShake(){
        if(shakeRadius < minimumShakeRadius){
            reset();
            return;
        }
        isShaking = true;
        shakeRadius *= radiusFallOffFactor;
        randomAngle = MathUtils.random(1, 360);
    }

    private void checkParameters(float shakeRadius, float minimumShakeRadius, float radiusFallOffFactor) {
        // validation checks on parameters
        if (radiusFallOffFactor >= 1f) radiusFallOffFactor = 0.9f;      // radius fall off factor must be less than 1
        if (radiusFallOffFactor <= 0) radiusFallOffFactor = 0.9f;       // radius fall off factor must be greater than 0
        if (shakeRadius <= 0) shakeRadius = 0.1f;                       // shake radius must be greater than 0
        if (minimumShakeRadius < 0) minimumShakeRadius = 0;             // minimum shake radius must be greater than 0
        if (minimumShakeRadius >= shakeRadius)                          // minimum shake radius must be less than shake radius, if not
            minimumShakeRadius = 0.15f * shakeRadius;                   // then set minimum shake radius to 15% of shake radius

        this.shakeRadius = shakeRadius;
        this.origShakeRadius = shakeRadius;
        this.minimumShakeRadius = minimumShakeRadius;
        this.radiusFallOffFactor = radiusFallOffFactor;
    }


}