package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.models.EnemyAction;
import edu.cornell.gdiac.game.models.Level;

import java.util.EnumSet;

public class DumbAI extends AIController {

    public DumbAI(int ii, Level level) {
        super(ii, level);
    }

    @Override
    public Vector2 getVelocity() {
        return new Vector2(0,0);
    }

    @Override
    public void setEnemyAction(EnumSet<EnemyAction> enemyAction) {
        return;
    }
}
