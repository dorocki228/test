package l2s.gameserver.handler.effects.impl.consume

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.GameObjectTasks
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket
import l2s.gameserver.network.l2.s2c.RevivePacket
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min

/**
 * Fake Death effect implementation.
 * @author mkizub
 * @author Java-man
 */
class c_fake_death(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.FAKE_DEATH) {

    private val _power = params.getDouble("c_fake_death_param1")

    init {
        ticks = params.getInteger("c_fake_death_param2")
    }

    override fun consume(abnormal: Abnormal?, target: Creature): Boolean {
        if (target.isDead) {
            return false
        }

        val consume = _power * ticksMultiplier
        val mp = target.currentMp
        val maxMp = target.stat.getMaxRecoverableMp().toDouble()
        if (consume > 0 && mp > maxMp) {
            return false
        }

        if (consume < 0 && mp + consume <= 0) {
            target.sendPacket(SystemMsg.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP)
            return false
        }

        target.currentMp = min(mp + consume, maxMp)

        return true
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player ?: return

        // if this is a player instance, start the grace period for this character (grace from mobs only)!
        player.setNonAggroTime(System.currentTimeMillis() + 5000L);

        target.broadcastPacket(ChangeWaitTypePacket(target, ChangeWaitTypePacket.WT_STOP_FAKEDEATH))
        target.broadcastPacket(RevivePacket(target))
        // need ? broadcastCharInfo();

        val task = GameObjectTasks.EndBreakFakeDeathTask(player)
        ThreadPoolManager.getInstance().schedule(task, 2500);
    }

}