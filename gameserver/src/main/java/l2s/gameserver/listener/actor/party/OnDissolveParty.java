package l2s.gameserver.listener.actor.party;

import l2s.gameserver.listener.PartyListener;
import l2s.gameserver.model.Party;

/**
 * @author mangol
 */
public interface OnDissolveParty extends PartyListener {
	void dissolve(Party party);
}
