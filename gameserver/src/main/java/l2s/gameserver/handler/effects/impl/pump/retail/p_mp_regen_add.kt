package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalItemTypeEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_mp_regen_add(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.MP_REGEN_ADD)