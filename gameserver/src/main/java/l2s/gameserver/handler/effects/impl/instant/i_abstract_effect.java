package l2s.gameserver.handler.effects.impl.instant;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 **/
public abstract class i_abstract_effect extends EffectHandler {
    public i_abstract_effect(EffectTemplate template) {
        super(template);
    }

    @Override
    protected final boolean checkPumpCondition(Abnormal abnormal, Creature caster, Creature target) {
        return true;
    }

    @Override
    protected final boolean checkActingCondition(Abnormal abnormal, Creature effector, Creature effected) {
        return true;
    }

    @Override
    public final void pumpStart(Abnormal abnormal, Creature caster, Creature target) {
        //
    }

    @Override
    public final void tick(Abnormal abnormal, Creature effector, Creature effected) {
    }

    @Override
    public final void pumpEnd(Abnormal abnormal, Creature caster, Creature target) {
        //
    }

    @Override
    public abstract void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic);
}