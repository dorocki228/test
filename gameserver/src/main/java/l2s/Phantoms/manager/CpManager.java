package  l2s.Phantoms.manager;

import java.util.ArrayList;
import java.util.List;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.Phantoms.objects.PhantomPartyObject;

public class CpManager
{
	private static List<PhantomPartyObject> parties = new ArrayList<PhantomPartyObject>();

	private static CpManager _instance;

	public static CpManager getInstance()
	{
		if(_instance == null)
			_instance = new CpManager();
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

	public PhantomDefaultPartyAI getPartyAIByID(int id)
	{
		for(PhantomDefaultPartyAI obj : parties)
		{
			if(obj.getPartyId() == id)
			{ return obj; }
		}
		return null;
	}

}
