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
class p_attack_speed_by_hp2(template: EffectTemplate) :
        AbstractDoubleStatConditionalHpEffect(template, DoubleStat.PHYSICAL_ATTACK_SPEED, 60)