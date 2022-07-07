package l2s.gameserver.model.actor.listener;

import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.listener.PartyListener;
import l2s.gameserver.listener.actor.party.OnDissolveParty;
import l2s.gameserver.listener.actor.party.OnLeavePlayerParty;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;

/**
 * @author mangol
 */
public class PartyListenerList extends ListenerList<Party> {
	static final ListenerList<Party> global = new ListenerList<>();
	protected final Party party;

	public PartyListenerList(Party party) {
		this.party = party;
	}

	public Party getParty() {
		return party;
	}

	public static final boolean addGlobal(PartyListener listener) {
		return global.add(listener);
	}

	public static final boolean removeGlobal(PartyListener listener) {
		return global.remove(listener);
	}

	public void onDissolve() {
		if(!global.getListeners().isEmpty()) {
			for(Listener<Party> listener : global.getListeners()) {
				if(OnDissolveParty.class.isInstance(listener)) {
					((OnDissolveParty) listener).dissolve(getParty());
				}
			}
		}
		if(!getListeners().isEmpty()) {
			for(Listener<Party> listener : getListeners()) {
				if(OnDissolveParty.class.isInstance(listener)) {
					((OnDissolveParty) listener).dissolve(getParty());
				}
			}
		}
	}


	public void onLeaveMember(Player player, boolean kick) {
		if(!global.getListeners().isEmpty()) {
			for(Listener<Party> listener : global.getListeners()) {
				if(OnLeavePlayerParty.class.isInstance(listener)) {
					((OnLeavePlayerParty) listener).leave(getParty(), player, kick);
				}
			}
		}
		if(!getListeners().isEmpty()) {
			for(Listener<Party> listener : getListeners()) {
				if(OnLeavePlayerParty.class.isInstance(listener)) {
					((OnLeavePlayerParty) listener).leave(getParty(), player, kick);
				}
			}
		}
	}
}
