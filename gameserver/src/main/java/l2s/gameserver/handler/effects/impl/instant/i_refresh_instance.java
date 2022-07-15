package l2s.gameserver.handler.effects.impl.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 */
public final class i_refresh_instance extends i_abstract_effect {
    public i_refresh_instance(EffectTemplate template) {
        super(template);
    }

	/*@Override
	protected boolean checkPumpCondition(Creature effector, Creature effected)
	{
		return effected.isPlayer();
	}*/

    @Override
    public void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic) {
        Player player = target.getPlayer();
        if (player != null) {
            int instanceId = (int) getValue();
            if (instanceId == -1)
                player.removeAllInstanceReuses();
            else
                player.removeInstanceReuse(instanceId);
        }
    }
}