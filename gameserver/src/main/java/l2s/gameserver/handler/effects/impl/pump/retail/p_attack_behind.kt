package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Nik
 * @author Java-man
 *
 * @since 19.10.2019
 */
class p_attack_behind(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.ATTACK_BEHIND)