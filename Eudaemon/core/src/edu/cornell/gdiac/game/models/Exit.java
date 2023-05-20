package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;
import org.w3c.dom.Text;

public class Exit extends BoxObstacle {

    private String nextLevel;

    private boolean isReached;

    private TextureRegion texture;

    private Animation<TextureRegion>  animation;
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
    /** The player in the level */
    private Player player;
    /** The flag to indicate if the player is in the zone for the animation to begin*/
    private boolean zoneFlag = false;
    /** The elapsed time since the animation started */
    private int elapsedTime = 0;

    public Exit(JsonValue json, AssetDirectory assets, float x, float y, float width, float height, Player player) {
        super(x, y, width, height);
        this.player = player;
        JsonValue props = json.get("properties");
        TextureRegion spriteSheet = new TextureRegion(assets.getEntry("platform:exit", Texture.class));
        TextureRegion[][] frames = spriteSheet.split(spriteSheet.getRegionWidth() / 3, spriteSheet.getRegionHeight());
        animation = new Animation<>(1f, frames[0]);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        texture = animation.getKeyFrame(0);
        scaleX = 1.3f;
        scaleY = 2.0f;
        oxOffset = 0.0f;
        oyOffset = 0f;
        xOffset = 0.5f;
        yOffset = 0.875f;
        setX(x + xOffset);
        setY(y + yOffset);

        boolean hasNext = false;

        for (JsonValue prop : props) {
            if (prop.getString("name").equals("NextLevel")) {
                this.nextLevel = prop.getString("value");
                hasNext = true;
            }
        }
        if(!hasNext)this.nextLevel = "win";

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

        elapsedTime++;
        float dist = 5f;
        if (player.getBody() != null && player.getX() > getX() - dist && player.getX() < getX() + dist && player.getY() > getY() - dist && player.getY() < getY() + dist) {
            if (!zoneFlag) {
                zoneFlag = true;
                elapsedTime = 0;
            }
            if (elapsedTime < 15){
                this.texture = animation.getKeyFrame(1);
            }
            else if (elapsedTime == 15){
                this.texture = animation.getKeyFrame(2);
            }
        }
        else {
            if (zoneFlag) {
                this.texture = animation.getKeyFrame(0);
                zoneFlag = false;
                elapsedTime = 0;
            }
            if (elapsedTime < 15){
                this.texture = animation.getKeyFrame(1);
            }
            else if (elapsedTime == 15) {
                this.texture = animation.getKeyFrame(0);
            }
        }

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
