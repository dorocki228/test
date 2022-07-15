package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.StorageType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 16.10.2019
 */
class p_enlarge_storage(template: EffectTemplate) : EffectHandler(template) {

    private val type = params.getEnum(
            "p_enlarge_storage_param1",
            StorageType::class.java,
            true
    )
    private val amount = params.getInteger("p_enlarge_storage_param2")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        val stat = when (type) {
            StorageType.TRADE_BUY -> DoubleStat.TRADE_BUY
            StorageType.TRADE_SELL -> DoubleStat.TRADE_SELL
            StorageType.RECIPE_DWARVEN -> DoubleStat.RECIPE_DWARVEN
            StorageType.RECIPE_COMMON -> DoubleStat.RECIPE_COMMON
            StorageType.STORAGE_PRIVATE -> DoubleStat.STORAGE_PRIVATE
            else -> DoubleStat.INVENTORY_NORMAL
        }

        if (skillEntry != null) {
            target.stat.mergeAdd(stat, amount.toDouble(), skillEntry)
        } else {
            target.stat.mergeAdd(stat, amount.toDouble(), skill)
        }
    }

}