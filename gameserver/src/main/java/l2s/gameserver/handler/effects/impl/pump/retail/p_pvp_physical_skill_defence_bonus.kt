package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_pvp_physical_skill_defence_bonus(template: EffectTemplate) :
        AbstractDoubleStatEffect(template, DoubleStat.PVP_PHYSICAL_SKILL_DEFENCE)