package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 10.10.2019
 */
class p_block_getdamage(template: EffectTemplate) : EffectHandler(template) {

    private val blockHp: Boolean
    private val blockMp: Boolean

    init {
        val type = params.getString("p_block_getdamage_param1")
        blockHp = type.equals("block_hp", ignoreCase = true)
        blockMp = type.equals("block_mp", ignoreCase = true)
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (blockHp) {
            target.stat.set(BooleanStat.HP_BLOCKED)
        } else if (blockMp) {
            target.stat.set(BooleanStat.MP_BLOCKED)
        }
    }

}