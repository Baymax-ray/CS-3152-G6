package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;

public class Exit extends BoxObstacle {

    private String nextLevel;

    private boolean isReached;

    public Exit(JsonValue json, AssetDirectory assets, float x, float y, float width, float height) {
        super(x, y, width, height);
        JsonValue props = json.get("properties");

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
