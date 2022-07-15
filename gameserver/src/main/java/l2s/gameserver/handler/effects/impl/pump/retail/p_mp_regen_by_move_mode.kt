package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatByMoveType
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
class p_mp_regen_by_move_mode(template: EffectTemplate) :
        AbstractDoubleStatByMoveType(template, DoubleStat.MP_REGEN)