package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.CubicHolder
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.cubic.CubicTemplate
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Summon Cubic effect implementation.
 *
 * @author Zoey76
 * @author Java-man
 */
class i_summon_cubic(template: EffectTemplate) : i_abstract_effect(template) {

    private val _template: CubicTemplate

    init {
        val cubicId = getTemplate().params.getInteger("i_summon_cubic_param1")
        val cubicLevel = getTemplate().params.getInteger("i_summon_cubic_param2")
        val temp = CubicHolder.getInstance().getTemplate(cubicId, cubicLevel)
        _template = requireNotNull(temp) {
            "Cannot find cubic template for skill: ID[${skill.id}], LEVEL[${skill.level}]!"
        }
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetPlayer = target.player ?: return

        if (targetPlayer.isAlikeDead || targetPlayer.isInObserverMode || targetPlayer.isMounted) {
            return
        }

        // If cubic is already present, it's replaced.
        val cubic = targetPlayer.getCubic(_template.slot)
        if (cubic != null) {
            cubic.delete()
        } else {
            // If maximum amount is reached, random cubic is removed.
            // Players with no mastery can have only one cubic.
            val allowedCubicCount = targetPlayer.stat.getValue(DoubleStat.MAX_CUBIC, 1.0).toInt()
            // Extra cubics are removed, one by one, randomly.
            while (true) {
                val cubics = targetPlayer.cubics
                if (cubics.size <= allowedCubicCount) {
                    break
                }

                val randomCubic = Rnd.get(cubics.toTypedArray())
                randomCubic?.delete()
            }
        }

        // Adding a new cubic.
        val newCubic = Cubic(targetPlayer, _template, skill)
        newCubic.init()
    }

}