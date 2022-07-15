package l2s.gameserver.handler.effects.impl.pump

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalItemTypeEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Java-man
 */
class p_run_speed(template: EffectTemplate) : AbstractDoubleStatConditionalItemTypeEffect(template, DoubleStat.RUN_SPEED)