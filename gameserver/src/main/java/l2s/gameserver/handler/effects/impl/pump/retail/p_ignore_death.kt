package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 */
class p_ignore_death(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.IGNORE_DEATH)