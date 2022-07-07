package  l2s.Phantoms.manager;

import java.util.ArrayList;
import java.util.List;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.Phantoms.objects.PhantomPartyObject;
import  l2s.gameserver.model.Player;

public class PartyManager
{
	// private static final Logger _log = LoggerFactory.getLogger(PartyManager.class);
	private static List<PhantomPartyObject> parties = new ArrayList<PhantomPartyObject>();

	private static PartyManager _instance;

	public static PartyManager getInstance()
	{
		if(_instance == null)
			_instance = new PartyManager();
		return _instance;
	}

	public List<PhantomPartyObject> getAllParties()
	{
		return parties;
	}

	public int getPartiesSize()
	{
		return parties.size();
	}

	public void addParties(PhantomPartyObject party)
	{
		parties.add(party);
	}

	public boolean alreadySpawned(int cp_id)
	{
		return getPartyAIByID(cp_id) == null ? false:true;
	}
	
	public void despawnParties(PhantomPartyObject party)
	{
		ArrayList<Player> members = party.getAllMembers();
		for (Player member : members)
			if (member != null)
				member.kick();
		parties.remove(party);
	}
	
	public PhantomDefaultPartyAI getPartyAIByID(int id)
	{
		for(PhantomDefaultPartyAI obj : parties)
		{
			if(obj.getPartyId() == id)
				return obj;
		}
		return null;
	}
}
