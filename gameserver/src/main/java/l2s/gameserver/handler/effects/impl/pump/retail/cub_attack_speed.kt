package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Java-man
 */
class cub_attack_speed(template: EffectTemplate) :
        AbstractDoubleStatEffect(template, DoubleStat.PHYSICAL_ATTACK_SPEED)