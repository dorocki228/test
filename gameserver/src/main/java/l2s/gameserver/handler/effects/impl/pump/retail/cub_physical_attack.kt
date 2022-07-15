package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatEffect
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 */
class cub_physical_attack(template: EffectTemplate) :
        AbstractDoubleStatEffect(template, DoubleStat.PHYSICAL_ATTACK)