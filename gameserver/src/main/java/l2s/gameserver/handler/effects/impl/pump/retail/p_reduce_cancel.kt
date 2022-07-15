package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 18.10.2019
 */
class p_reduce_cancel(template: EffectTemplate) :
        AbstractDoubleStatEffect(template, DoubleStat.ATTACK_CANCEL)