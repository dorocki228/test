package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.Config
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dispel By Slot Probability effect implementation.
 * @author Adry_85, Zoey76
 * @author Java-man
 */
class i_dispel_by_slot_probability(template: EffectTemplate) : i_abstract_effect(template) {

    private val abnormalType: AbnormalType
    private val chance = params.getInteger("i_dispel_by_slot_probability_param2")

    init {
        val name = params.getString("i_dispel_by_slot_probability_param1").toUpperCase()
        abnormalType = AbnormalType.valueOf(name)
        require(abnormalType != AbnormalType.NONE) {
            "Skill ${skill.id} abnormal_type should not be NONE."
        }
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        val abnormalList = targetCreature.abnormalList
        abnormalList
                .filter { canBeDispelled(caster, targetCreature, it) }
                .forEach {
                    removeAbnormal(caster, it)
                }
    }

    fun canBeDispelled(caster: Creature, target: Creature, abnormal: Abnormal): Boolean {
        /*if(!abnormal.isCancelable())
                 return false;*/

        val effectSkill = abnormal.skill ?: return false

        if (effectSkill.isToggle)
            return false

        if (effectSkill.isPassive)
            return false

        if (target.isSpecialAbnormal(effectSkill))
            return false

        if (effectSkill.isIrreplaceableBuff)
            return false

        if (!Rnd.chance(chance))
            return false

        if (!abnormal.abnormalTypeList.contains(abnormalType))
            return false

        return true
    }

    private fun removeAbnormal(caster: Creature, abnormal: Abnormal) {
        abnormal.exit()

        if (!Config.SHOW_TARGET_PLAYER_DEBUFF_EFFECTS && !abnormal.isHidden) {
            val packet =
                    SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED)
                            .addSkillName(abnormal.skill)
            caster.sendPacket(packet)
        }
    }

}