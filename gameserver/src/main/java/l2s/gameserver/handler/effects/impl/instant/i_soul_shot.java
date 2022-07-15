package l2s.gameserver.handler.effects.impl.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 */
public class i_soul_shot extends i_abstract_effect {
    private final double _power;

    public i_soul_shot(EffectTemplate template) {
        super(template);
        _power = getParams().getDouble("power", 100.);
    }

    @Override
    public void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic) {
        target.sendPacket(SystemMsg.YOUR_SOULSHOTS_ARE_ENABLED);
        target.setChargedSoulshotPower(_power);
    }
}