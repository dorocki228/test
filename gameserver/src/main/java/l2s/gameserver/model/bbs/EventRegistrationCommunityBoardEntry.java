package l2s.gameserver.model.bbs;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;

import java.util.Set;

/**
 * @author Java-man
 * @since 11.06.2018
 */
public class EventRegistrationCommunityBoardEntry extends CommunityBoardEntry {
    private final Event event;

    public EventRegistrationCommunityBoardEntry(Event event) {
        super(event.getName(), "Reg/Unreg");
        this.event = event;
    }

    @Override
    protected CommunityBoardEntryType getType() {
        return CommunityBoardEntryType.EVENT_REGISTRATION;
    }

    @Override
    public boolean isVisible(Player player) {
        return !event.isRegistrationOver();
    }

    @Override
    protected boolean canUse(Player player) {
        if (player.containsEvent(SingleMatchEvent.class, Set.of(event.getId()))) {
            player.sendMessage("You cannot register while being in event.");
            return false;
        }

        if (player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player)) {
            player.sendMessage("You cannot register while being in olympiad.");
            return false;
        }

        return true;
    }

    @Override
    public void onAction(Player player) {
        boolean registered = event.isPlayerRegistered(player);

        if (registered) {
            String message = event.unregisterPlayer(player)
                    ? "Registration cancelled." : "Registration cancellation failed.";
            player.sendMessage(message);
        } else {
            String message = event.registerPlayer(player)
                    ? "You have been registered in the event." : "Registration failed.";
            player.sendMessage(message);
        }
    }
}
