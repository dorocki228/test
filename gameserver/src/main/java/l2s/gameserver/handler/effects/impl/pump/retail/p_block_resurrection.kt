package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Block Resurrection effect implementation.
 * @author UnAfraid
 * @author Java-man
 */
class p_block_resurrection(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_RESURRECTION)