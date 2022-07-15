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
class p_magic_speed(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.MAGICAL_ATTACK_SPEED)