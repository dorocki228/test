package l2s.gameserver.handler.effects.impl.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 **/
public final class i_my_summon_kill extends i_abstract_effect {
    public i_my_summon_kill(EffectTemplate template) {
        super(template);
    }

    @Override
    public void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic) {
        for (Servitor servitor : target.getServitors()) {
            if (servitor.isSummon())
                servitor.unSummon(false);
        }
    }
}