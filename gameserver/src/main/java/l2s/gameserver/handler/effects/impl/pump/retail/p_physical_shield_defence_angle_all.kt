package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 26.10.2019
 */
class p_physical_shield_defence_angle_all(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.PHYSICAL_SHIELD_DEFENCE_ANGLE_ALL)