package l2s.gameserver.model.bbs;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.PortalInstance;
import l2s.gameserver.service.LocationBalancerService;
import l2s.gameserver.utils.Location;

/**
 * @author Java-man
 * @since 11.06.2018
 */
public class PortalCommunityBoardEntry extends DelayedCommunityBoardEntry {
    private final PortalInstance portal;

    public PortalCommunityBoardEntry(PortalInstance portal) {
        super(portal.getNameForCommunityBoard(), "Teleport");
        this.portal = portal;
    }

    @Override
    protected CommunityBoardEntryType getType() {
        return CommunityBoardEntryType.PORTAL;
    }

    @Override
    public boolean isVisible(Player player) {
        if (portal.isPersonalPortal()) {
            return portal.getPlayer() == player;
        } else {
            return portal.getFraction() == player.getFraction();
        }
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
    public void onAction(Player player) {
        Location loc = Location.findPointToStay(portal.getLoc(), 10, 50, portal.getGeoIndex());
        if(LocationBalancerService.getInstance().canTeleport(player, loc)) {
            player.teleToLocation(loc);
            portal.decreaseTeleportsLeft();
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
