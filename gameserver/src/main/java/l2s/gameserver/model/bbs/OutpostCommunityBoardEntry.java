package l2s.gameserver.model.bbs;

import gve.zones.model.GveOutpost;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;

/**
 * @author Java-man
 * @since 11.06.2018
 */
public class OutpostCommunityBoardEntry extends DelayedCommunityBoardEntry {
    private final GveOutpost outpost;

    //TODO мультиленг??
    public OutpostCommunityBoardEntry(GveOutpost outpost) {
        super(outpost.getName(Language.ENGLISH), "Teleport");
        this.outpost = outpost;
    }

    @Override
    protected CommunityBoardEntryType getType() {
        return CommunityBoardEntryType.OUTPOST;
    }

    @Override
    public boolean isVisible(Player player) {
        return outpost.getFraction() == player.getFraction() && outpost.getStatus() == GveOutpost.ATTACKED;
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
        var loc = Rnd.get(outpost.getLocations());
        if (loc != null) {
            player.teleToLocation(Location.coordsRandomize(loc, 50, 150));
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
