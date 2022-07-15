package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalItemTypeEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Nik
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_skill_critical_rate(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.CRITICAL_RATE_SKILL)