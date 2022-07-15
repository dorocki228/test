package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 20.10.2019
 */
class p_cheapshot(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.CHEAPSHOT)