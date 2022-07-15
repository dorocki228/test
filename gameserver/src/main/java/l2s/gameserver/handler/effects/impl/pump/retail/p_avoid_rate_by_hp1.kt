package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalHpEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_avoid_rate_by_hp1(template: EffectTemplate) :
        AbstractDoubleStatConditionalHpEffect(template, DoubleStat.EVASION_RATE, 30)