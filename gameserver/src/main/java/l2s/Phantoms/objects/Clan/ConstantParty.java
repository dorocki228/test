package  l2s.Phantoms.objects.Clan;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import  l2s.Phantoms.enums.PartyType;

public class ConstantParty
{
	private List <MemberCP> members = new ArrayList <MemberCP>();
	private int _id;
	private String[] _prime_time;
	private int _despawn;
	private PartyType _party_type;

	public ConstantParty(int id, String[] prime_time, int despawn, PartyType party_type)
	{
		_id= id;
		_prime_time = prime_time;
		_despawn = despawn;
		_party_type = party_type;
	}

	public void addMember(MemberCP memberCP)
	{
		members.add(memberCP);
	}

	public List <MemberCP> getMembers()
	{
		return members;
	}

	public void setMembers(List <MemberCP> members)
	{
		this.members = members;
	}

	public int getCpId()
	{
		return _id;
	}

	public String[] getPrimeTimeStr()
	{
		return _prime_time;
	}
	
	// не разрешаем залетать на олимп за 20 минут до прайма
	public boolean checkTimeForOlympiad()
	{
		long current_time = System.currentTimeMillis(); 
		
		for (Long prime_time : getPrimeTime())
		{
			long time_diff = prime_time - current_time;
			if(time_diff>0 && time_diff < 1200000) // осталось меньше 20 минут до прайма
				return false;
		}
		return true;
	}
	//время "прайма" кп
	public List<Long> getPrimeTime()
	{
		List <Long> pt = new ArrayList<Long> ();
		for (String ptstr : getPrimeTimeStr())
		{
			Calendar startTime = Calendar.getInstance();
			startTime.setLenient(true);

			String[] splitTimeOfDay = ptstr.split(":");

			startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
			startTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
			startTime.set(Calendar.SECOND, 0);
			startTime.set(Calendar.MILLISECOND, 0);
			pt.add(startTime.getTimeInMillis());
		}
		return pt;
	}

	public int getDespawn()
	{
		return _despawn;
	}

	public PartyType getPartyType()
	{
		return _party_type;
	}

}