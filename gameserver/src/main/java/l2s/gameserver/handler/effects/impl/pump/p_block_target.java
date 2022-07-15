package l2s.gameserver.handler.effects.impl.pump;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.List;

/**
 * @author Bonux
 **/
public class p_block_target extends EffectHandler {
    public p_block_target(EffectTemplate template) {
        super(template);
    }

    @Override
    protected boolean checkPumpCondition(Abnormal abnormal, Creature caster, Creature target) {
        if (target.isRaid())
            return false;
        return target.isTargetable(caster);
    }

    @Override
    public void pumpStart(Abnormal abnormal, Creature caster, Creature target) {
        target.setTargetable(false);

        target.abortAttack(true, true);
        target.abortCast(true, true);

        List<Creature> characters = World.getAroundCharacters(target);
        for (Creature character : characters) {
            if (character.getTarget() != target && character.getAI().getAttackTarget() != target && character.getAI().getCastTarget() != target)
                continue;

            if (character.isNpc())
                ((NpcInstance) character).getAggroList().remove(target, true);

            if (character.getTarget() == target)
                character.setTarget(null);

            if (character.getAI().getAttackTarget() == target)
                character.abortAttack(true, true);

            if (character.getAI().getCastTarget() == target)
                character.abortCast(true, true);

            character.sendActionFailed();
            character.getMovement().stopMove();
            character.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }

    }

    @Override
    public void pumpEnd(Abnormal abnormal, Creature caster, Creature target) {
        target.setTargetable(true);
    }
}