package l2s.gameserver.handler.effects.impl.pump

import l2s.gameserver.ai.CtrlIntention
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.World
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.instances.NpcInstance
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Java-man
 */
class p_block_target_me(template: EffectTemplate) : EffectHandler(template) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return !target.isRaid
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.flags.untargetableList.start(this, caster)

        target.abortAttack(true, true)
        target.abortCast(true, true)

        val characters = World.getAroundCharacters(target)
        for (character in characters) {
            if (character.target != target && character.ai.attackTarget != target && character.ai.castTarget != target)
                continue

            if (character.isNpc)
                (character as NpcInstance).aggroList.remove(target, true)

            if (character.target == target)
                character.target = null

            if (character.ai.attackTarget == target)
                character.abortAttack(true, true)

            if (character.ai.castTarget == target)
                character.abortCast(true, true)

            character.sendActionFailed()
            character.movement.stopMove()
            character.ai.intention = CtrlIntention.AI_INTENTION_ACTIVE
        }
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.flags.untargetableList.stop(this)
    }

}