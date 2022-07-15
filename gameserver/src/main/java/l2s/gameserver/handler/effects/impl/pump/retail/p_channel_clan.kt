package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that allows you to create a command channel.
 * @author Nik
 * @author Java-man
 */
class p_channel_clan(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.CAN_CREATE_COMMAND_CHANNEL)