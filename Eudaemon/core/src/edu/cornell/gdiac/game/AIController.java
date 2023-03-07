package edu.cornell.gdiac.game;

import edu.cornell.gdiac.game.models.EnemyAction;

import java.util.EnumSet;

public class AIController {

    public EnumSet<EnemyAction> getActions() {
        return EnumSet.noneOf(EnemyAction.class);
    }
}
