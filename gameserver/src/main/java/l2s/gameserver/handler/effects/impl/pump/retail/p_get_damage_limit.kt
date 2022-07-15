package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatAddEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 16.10.2019
 */
class p_get_damage_limit(template: EffectTemplate) :
        AbstractDoubleStatAddEffect(template, DoubleStat.DAMAGE_LIMIT)