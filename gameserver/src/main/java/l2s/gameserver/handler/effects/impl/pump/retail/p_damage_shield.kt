package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatAddEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 14.10.2019
 */
class p_damage_shield(template: EffectTemplate) :
        AbstractDoubleStatAddEffect(template, DoubleStat.REFLECT_DAMAGE_PERCENT)