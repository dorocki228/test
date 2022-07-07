package l2s.gameserver.model.bbs;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;

/**
 * @author Java-man
 * @since 11.06.2018
 */
public class EventTeleportationCommunityBoardEntry extends DelayedCommunityBoardEntry {
    private final Event event;

    public EventTeleportationCommunityBoardEntry(Event event) {
        this(event, event.getName());
    }

    public EventTeleportationCommunityBoardEntry(Event event, String visibleName) {
        super(visibleName, "Teleport");
        this.event = event;
    }

    @Override
    protected CommunityBoardEntryType getType() {
        return CommunityBoardEntryType.EVENT_TELEPORTATION;
    }

    @Override
    public boolean isVisible(Player player) {
        return event.isInProgress();
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
        if (!event.isInProgress()) {
            return;
        }

        event.teleportPlayerToEvent(player);
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
