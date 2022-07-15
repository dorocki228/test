package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Block Buff Slot effect implementation.
 * @author Zoey76
 * @author Java-man
 */
class p_block_buff_slot(template: EffectTemplate) : EffectHandler(template) {

    private val blockAbnormalSlots: Set<AbnormalType>

    init {
        val abnormals = params.getString("p_block_buff_slot_param1")
        blockAbnormalSlots = if (abnormals != null && abnormals.isNotEmpty()) {
            abnormals.split(";")
                    .map { AbnormalType.valueOf(it.toUpperCase()) }
                    .toSet()
        } else {
            emptySet()
        }
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.abnormalList.addBlockedAbnormalTypes(blockAbnormalSlots)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.abnormalList.removeBlockedAbnormalTypes(blockAbnormalSlots)
    }

}