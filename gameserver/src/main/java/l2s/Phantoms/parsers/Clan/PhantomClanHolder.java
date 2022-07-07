package  l2s.Phantoms.parsers.Clan;


import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.objects.Clan.ConstantParty;
import  l2s.Phantoms.objects.Clan.PhantomClan;
import  l2s.commons.data.xml.AbstractHolder;

public class PhantomClanHolder extends AbstractHolder
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomClanHolder.class);
	
	private Map <String,PhantomClan> clans = new HashMap <String,PhantomClan>();
	
	@Override
	public int size()
	{
		return clans.size();
	}
	
	@Override
	public void clear()
	{
		clans.clear();
	}
	
	public Map <String,PhantomClan> getAllClans()
	{
		return clans;
	}
	public PhantomClan getClanByName(String name)
	{
		return clans.get(name);
	}
	
	public static PhantomClanHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static PhantomClanHolder instance = new PhantomClanHolder();
	}

	public void addItems(PhantomClan phantomClan)
	{
		clans.put(phantomClan.getClanName(),phantomClan);
	}

	public Set<Long> getTimeList()
	{
		Set<Long> time_cp = new HashSet<Long>();
		for (PhantomClan clan : clans.values())
			for (ConstantParty cp : clan.getCpList())
			{
				for (String ptstr : cp.getPrimeTimeStr())
				{
					Calendar startTime = Calendar.getInstance();
					startTime.setLenient(true);
					
					String[] splitTimeOfDay = ptstr.split(":");
					
					startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
					startTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
					startTime.set(Calendar.SECOND, 0);
					startTime.set(Calendar.MILLISECOND, 0);
					
					if (startTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
					{
						startTime.add(Calendar.DAY_OF_MONTH, 1);
					}
					time_cp.add(startTime.getTimeInMillis());
				}
			}
		
		return time_cp;
	}


	
}