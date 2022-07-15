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
class p_skill_critical_damage(template: EffectTemplate) :
        AbstractDoubleStatConditionalItemTypeEffect(
                template,
                DoubleStat.CRITICAL_DAMAGE_SKILL,
                DoubleStat.CRITICAL_DAMAGE_SKILL_ADD
        )