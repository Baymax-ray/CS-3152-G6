package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;

public class Exit extends BoxObstacle {

    private String nextLevel;

    private boolean isReached;

    private TextureRegion texture;
    /**
     * The texture origin offset value along the y-axis.
     */
    private float oyOffset;
    /**
     * The texture origin offset value along the y-axis.
     */
    private float oxOffset;
    /**
     * The offset value along the x-axis.
     */
    private float xOffset;
    /**
     * The offset value along the y-axis.
     */
    private float yOffset;
    /**
     * The texture scale along the x-axis.
     */
    private float scaleX;
    /**
     * The texture scale along the y-axis.
     */
    private float scaleY;

    public Exit(JsonValue json, AssetDirectory assets, float x, float y, float width, float height) {
        super(x, y, width, height);
        JsonValue props = json.get("properties");
        texture = new TextureRegion(assets.getEntry("platform:exit", Texture.class));
        scaleX = 1.3f;
        scaleY = 2.0f;
        oxOffset = 0f;
        oyOffset = 0f;
        xOffset = 0f;
        yOffset = -1.2f;
        setX(x + xOffset);
        setY(y + yOffset);

        for (JsonValue prop : props) {
            if (prop.getString("name").equals("NextLevel")) {
                this.nextLevel = prop.getString("value");
            }
        }

        this.isReached = false;
        this.bodyinfo.type = BodyDef.BodyType.StaticBody;
        this.fixture.isSensor = true;
    }

    public Exit() {
        super(15, 15, 2, 2);

        this.nextLevel = "win";
        this.isReached = false;

        bodyinfo.type = BodyDef.BodyType.StaticBody;
        fixture.isSensor = true;
    }

    public void draw(GameCanvas canvas) {

        float x = getX();
        float y = getY();

        float ox = oxOffset + this.texture.getRegionWidth()/2;
        float oy = oyOffset + this.texture.getRegionHeight()/2;

        float sx = scaleX * 1.0f / this.texture.getRegionWidth();
        float sy = scaleY * 1.0f / this.texture.getRegionHeight();
        canvas.draw(this.texture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }


    public boolean isReached() {
        return isReached;
    }

    public void setReached(boolean reached) {
        isReached = reached;
    }

    public String getNextLevel() {
        return nextLevel;
    }
}
