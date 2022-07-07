package l2s.gameserver.skills.effects

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Servitor
import l2s.gameserver.model.Skill
import l2s.gameserver.model.entity.events.impl.SiegeEvent
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.Location

import java.util.concurrent.TimeUnit

class EffectThrowHorizontal(creature: Creature, target: Creature, skill: Skill,
                            reflected: Boolean, template: EffectTemplate)
    : EffectFlyAbstract(creature, target, skill, reflected, template) {

    private var flyLoc: Location? = null

    override fun checkCondition(): Boolean {
        if (effected.isThrowAndKnockImmune) {
            effected.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET)
            return false
        }

        val player = effected.player
        if (player != null) {
            val siegeEvent = player.getEvent(SiegeEvent::class.java)
            if (effected.isSummon && siegeEvent != null && siegeEvent.containsSiegeSummon(effected as Servitor)) {
                effector.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET)
                return false
            }
        }

        if (effected.isInPeaceZone) {
            effector.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE)
            return false
        }

        flyLoc = effected.getFlyLocation(_effector, skill)

        return flyLoc != null
    }

    public override fun onStart() {
        val loc = flyLoc ?: return
        effected.abortAttack(true, true)
        effected.abortCast(true, true)
        effected.stopMove()
        effected.block()
        val player = effected.player
        if (player != null) {
            player.isIgnoreValidatePosition = true
        }
        effected.broadcastPacket(FlyToLocationPacket(effected, loc, FlyToLocationPacket.FlyType.THROW_HORIZONTAL, flySpeed, flyDelay, flyAnimationSpeed))
        effected.loc = loc
        effected.validateLocation(1)
        if (player != null&&!player.isPhantom) {
            ThreadPoolManager.getInstance().schedule({ player.setIgnoreValidatePosition(false) }, Math.max(player.netConnection.ping, 1000).toLong(), TimeUnit.MILLISECONDS)
        }
        if(player != null&&player.isPhantom)
            player.setIgnoreValidatePosition(false)
        super.onStart()
    }

    public override fun onExit() {
        effected.unblock()
        super.onExit()
    }

}
