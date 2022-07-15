package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_safe_fall_height(template: EffectTemplate) :
        AbstractDoubleStatEffect(template, DoubleStat.FALL)