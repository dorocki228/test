package l2s.gameserver.handler.effects.impl.pump;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 **/
public final class p_block_party extends EffectHandler {
    public p_block_party(EffectTemplate template) {
        super(template);
    }

    @Override
    protected boolean checkPumpCondition(Abnormal abnormal, Creature caster, Creature target) {
        if (!target.isPlayer())
            return false;

        return true;
    }

    @Override
    public void pumpStart(Abnormal abnormal, Creature caster, Creature target) {
        target.getPlayer().getFlags().getPartyBlocked().start(this);
        target.getPlayer().leaveParty(false);
    }

    @Override
    public void pumpEnd(Abnormal abnormal, Creature caster, Creature target) {
        target.getPlayer().getFlags().getPartyBlocked().stop(this);
    }
}