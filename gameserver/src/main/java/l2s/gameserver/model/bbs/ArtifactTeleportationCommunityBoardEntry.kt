package l2s.gameserver.model.bbs

import l2s.commons.util.Rnd
import l2s.gameserver.entity.ArtifactEntity
import l2s.gameserver.model.Player
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent
import l2s.gameserver.model.entity.olympiad.Olympiad
import l2s.gameserver.utils.Location

/**
 * @author Java-man
 * @since 11.06.2018
 */
class ArtifactTeleportationCommunityBoardEntry(private val artifact: ArtifactEntity) :
    DelayedCommunityBoardEntry(artifact.nameForCommunityBoard, "Teleport") {

    override fun getType(): CommunityBoardEntryType {
        return CommunityBoardEntryType.PORTAL
    }

    override fun isVisible(player: Player): Boolean {
        val artifactInstance = artifact.artifact ?: return false
        return !artifactInstance.fraction.canAttack(player.fraction)
    }

    override fun canUse(player: Player): Boolean {
        if (player.isInCombat) {
            player.sendMessage("You cannot teleport while being in fight.")
            return false
        }

        if (player.containsEvent(SingleMatchEvent::class.java)) {
            player.sendMessage("You cannot teleport while being in event.")
            return false
        }

        if (player.isInOlympiadMode || Olympiad.isRegisteredInComp(player)) {
            player.sendMessage("You cannot teleport while being in olympiad.")
            return false
        }

        return true
    }

    public override fun onAction(player: Player) {
        val artifactInstance = artifact.artifact ?: return
        val randomLoc = Rnd.get(artifact.template.teleportLocations) ?: return
        val loc = Location.findAroundPosition(randomLoc, 30, artifactInstance.geoIndex)
        player.teleToLocation(loc)
    }

    override fun getActionDelayInSeconds(): Long {
        return 3
    }

    override fun onActionSchedule(player: Player) {
        player.sendMessage("You must be teleported in $actionDelayInSeconds seconds.")
    }

    override fun onActionCancel(player: Player) {
        player.sendMessage("You cannot be teleported right now.")
    }

    fun getFaction() = artifact.fraction

    fun getObjectId() = artifact.artifact?.objectId ?: 0

}
