package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatAddEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Java-man
 */
class p_soulshot_power(template: EffectTemplate) :
        AbstractDoubleStatAddEffect(template, DoubleStat.SOULSHOT_POWER)