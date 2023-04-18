package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.game.obstacle.EffectObstacle;

public class EffectPool extends Pool<EffectObstacle> {

    private float x, y, width, height;

    @Override
    protected EffectObstacle newObject() {
        return new EffectObstacle();
    }

    public EffectObstacle obtainEffect(float x, float y, float width, float height, float sx, float sy, float angle, float pOffsetX, float pOffsetY, Boolean trackPlayer, String name, CapsuleObstacle avatar, float lifespan, float drawScaleX, float drawScaleY, Animation animation, int tickSpeed) {

        EffectObstacle effect = obtain();
        /* set up effect */
        effect.setPosition(x, y);
        effect.setDimension(width, height);
        effect.setTickSpeed(tickSpeed);
        effect.setAvatar(avatar, trackPlayer);
        effect.setsX(sx);
        effect.setsY(sy);
        effect.setpOffsetX(pOffsetX);
        effect.setpOffsetY(pOffsetY);
        effect.setLifespan(lifespan);
        effect.setName(name);
        effect.setDensity(0);
        effect.setDrawScale(drawScaleX, drawScaleY);
        effect.setBullet(true);
        effect.setGravityScale(0);
        effect.setBodyType(BodyDef.BodyType.KinematicBody);
        effect.setSensor(true);
        effect.setAngle(angle);
        effect.setTickSpeed(tickSpeed);

        effect.setAnimation(animation);
        effect.markRemoved(false);


        return effect;
    }
}
