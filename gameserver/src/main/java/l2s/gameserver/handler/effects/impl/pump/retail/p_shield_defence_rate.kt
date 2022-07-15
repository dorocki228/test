package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatAddEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_shield_defence_rate(template: EffectTemplate) :
        AbstractDoubleStatAddEffect(template, DoubleStat.SHIELD_DEFENCE_RATE)