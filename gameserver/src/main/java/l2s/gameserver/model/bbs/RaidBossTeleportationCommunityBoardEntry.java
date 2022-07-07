package l2s.gameserver.model.bbs;

import gve.zones.model.GveZoneStatus;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.service.PaidActionsStatsService;
import l2s.gameserver.service.PaidActionsStatsService.PaidActionType;
import l2s.gameserver.utils.Location;

/**
 * @author KRonst
 */
public class RaidBossTeleportationCommunityBoardEntry extends DelayedCommunityBoardEntry {

    private final NpcInstance raidBoss;

    public RaidBossTeleportationCommunityBoardEntry(NpcInstance raidBoss) {
        super(raidBoss.getName(), "RaidBossTeleport");
        this.raidBoss = raidBoss;
    }

    @Override
    protected CommunityBoardEntryType getType() {
        return CommunityBoardEntryType.RAID_BOSS;
    }

    @Override
    public boolean isVisible(Player player) {
        return !raidBoss.isDead() &&
            (
                raidBoss.getGveZones().stream().anyMatch(z -> z.getStatus() == GveZoneStatus.ACTIVATED)
                    || raidBoss.isInZone(ZoneType.gve_static_mid)
                    || raidBoss.isInZone(ZoneType.gve_static_high)
            );
    }

    @Override
    protected boolean canUse(Player player) {
        if (player.isInCombat()) {
            player.sendMessage("You cannot teleport while being in fight.");
            return false;
        }

        if (player.containsEvent(SingleMatchEvent.class)) {
            player.sendMessage("You cannot teleport while being in event.");
            return false;
        }

        if (player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player)) {
            player.sendMessage("You cannot teleport while being in olympiad.");
            return false;
        }

        return true;
    }

    @Override
    protected void onAction(Player player) {
        if (raidBoss.isDead()) {
            player.sendMessage("Raid Boss is already dead.");
            return;
        }
        final Location location = Location.findAroundPosition(raidBoss, 1100);
        if (player.reduceAdena(Config.RAID_BOSS_BBS_TELEPORT_PRICE, true)) {
            player.teleToLocation(location);
            PaidActionsStatsService.getInstance()
                .updateStats(PaidActionType.RAID_BOSS_TELEPORT, Config.RAID_BOSS_BBS_TELEPORT_PRICE);
        } else {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        }
    }

    @Override
    protected long getActionDelayInSeconds() {
        return 3;
    }

    @Override
    protected void onActionSchedule(Player player) {
        player.sendMessage("You must be teleported in " + getActionDelayInSeconds() + " seconds.");
    }

    @Override
    protected void onActionCancel(Player player) {
        player.sendMessage("You cannot be teleported right now.");
    }
}
