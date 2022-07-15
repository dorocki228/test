package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Effect that blocks all incoming debuffs.
 * @author Nik
 * @author Java-man
 */
class p_block_debuff(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_DEBUFF)