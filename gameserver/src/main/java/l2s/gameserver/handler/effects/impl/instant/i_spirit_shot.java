package l2s.gameserver.handler.effects.impl.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 */
public class i_spirit_shot extends i_abstract_effect {
    private final double _power;
    private final int _unk;
    private final double _healBonus;

    public i_spirit_shot(EffectTemplate template) {
        super(template);
        _power = getParams().getDouble("power", 100);
        _unk = getParams().getInteger("unk_spiritshot_parameter", 40);
        _healBonus = getParams().getDouble("heal_bonus", 1.0);
    }

    @Override
    public void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic) {
        target.sendPacket(SystemMsg.YOUR_SPIRITSHOT_HAS_BEEN_ENABLED);
        target.setChargedSpiritshotPower(_power, _unk, _healBonus);
    }
}