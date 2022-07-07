package ai;

import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author KRonst
 */
public class ValakasMinion extends Mystic {

    public ValakasMinion(NpcInstance actor) {
        super(actor);
        actor.startImmobilized();
    }
}
