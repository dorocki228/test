package l2s.gameserver.listener.actor.party;

import l2s.gameserver.listener.PartyListener;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;

/**
 * @author mangol
 */
public interface OnLeavePlayerParty extends PartyListener {
	void leave(Party party, Player player, boolean kick);
}
