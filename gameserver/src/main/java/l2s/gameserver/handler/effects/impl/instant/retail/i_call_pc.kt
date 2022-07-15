package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.geometry.Location
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.Zone
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.entity.olympiad.Olympiad
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.ItemFunctions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Call Pc effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_call_pc(template: EffectTemplate) : i_abstract_effect(template) {

    private val _itemId = params.getInteger("i_call_pc_param1")
    private val _itemCount = params.getLong("i_call_pc_param2")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return
        val targetPlayer = target.player ?: return

        if (casterPlayer == targetPlayer) {
            return
        }

        if (checkSummonTargetStatus(targetPlayer, casterPlayer)) {
            if (_itemId != 0 && _itemCount != 0L) {
                if (!ItemFunctions.deleteItem(targetPlayer, _itemId, _itemCount)) {
                    val sm = SystemMessagePacket(SystemMsg.S1_IS_REQUIRED_FOR_SUMMONING)
                    sm.addItemName(_itemId)
                    targetPlayer.sendPacket(sm)
                    return
                }
            }

            val position = Location.findAroundPosition(casterPlayer, 100, 150)
            targetPlayer.summonCharacterRequest(casterPlayer, position, 0)
        }
    }

    companion object {
        fun checkSummonTargetStatus(target: Player, activeChar: Creature): Boolean {
            if (target == activeChar) {
                return false
            }

            if (target.isAlikeDead) {
                val sm = SystemMessagePacket(SystemMsg.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED)
                sm.addName(target)
                activeChar.sendPacket(sm)
                return false
            }

            if (target.isInStoreMode) {
                val sm = SystemMessagePacket(SystemMsg.C1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED)
                sm.addName(target)
                activeChar.sendPacket(sm)
                return false
            }

            if (target.isImmobilized || target.isInCombat) {
                val sm = SystemMessagePacket(SystemMsg.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED)
                sm.addName(target)
                activeChar.sendPacket(sm)
                return false
            }

            if (target.isInOlympiadMode) {
                activeChar.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD)
                return false
            }

            if (target.isFlying || target.isInFlyingTransform /*|| target.isCombatFlagEquipped()*/) {
                activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING)
                return false
            }

            if (target.isInObserverMode || Olympiad.isRegisteredInComp(target)) {
                val sm = SystemMessagePacket(SystemMsg.C1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING_OR_TELEPORTING2)
                sm.addName(target)
                activeChar.sendPacket(sm)
                return false
            }

            if (target.isInZone(Zone.ZoneType.no_summon) || target.isInJail) {
                val sm = SystemMessagePacket(SystemMsg.C1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING_OR_TELEPORTING)
                sm.addName(target)
                activeChar.sendPacket(sm)
                return false
            }

            val instance = activeChar.reflection
            if (!instance.isMain /*&& !instance.isPlayerSummonAllowed()*/) {
                activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION)
                return false
            }

            return true
        }
    }

}
