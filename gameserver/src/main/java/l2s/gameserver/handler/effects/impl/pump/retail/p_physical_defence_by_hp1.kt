package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalHpEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_physical_defence_by_hp1(template: EffectTemplate) :
        AbstractDoubleStatConditionalHpEffect(template, DoubleStat.PHYSICAL_DEFENCE, 30)