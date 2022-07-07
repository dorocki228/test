package l2s.gameserver.utils;

import l2s.gameserver.model.actor.instances.creature.Abnormal;

import java.util.Comparator;

public class EffectsComparator implements Comparator<Abnormal>
{
	private static final EffectsComparator instance;

	public static final EffectsComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Abnormal e1, Abnormal e2)
	{
		boolean toggle1 = e1.getSkill().isToggle();
		boolean toggle2 = e2.getSkill().isToggle();
		if(toggle1 && toggle2)
			return compareStartTime(e1, e2);
		if(toggle1 || toggle2)
		{
			if(toggle1)
				return 1;
			return -1;
		}
		else
		{
			boolean music1 = e1.getSkill().isMusic();
			boolean music2 = e2.getSkill().isMusic();
			if(music1 && music2)
				return compareStartTime(e1, e2);
			if(music1 || music2)
			{
				if(music1)
					return 1;
				return -1;
			}
			else
			{
				boolean offensive1 = e1.isOffensive();
				boolean offensive2 = e2.isOffensive();
				if(offensive1 && offensive2)
					return compareStartTime(e1, e2);
				if(offensive1 || offensive2)
				{
					if(!offensive1)
						return 1;
					return -1;
				}
				else
				{
					boolean trigger1 = e1.getSkill().isTrigger();
					boolean trigger2 = e2.getSkill().isTrigger();
					if(trigger1 && trigger2)
						return compareStartTime(e1, e2);
					if(!trigger1 && !trigger2)
						return compareStartTime(e1, e2);
					if(trigger1)
						return 1;
					return -1;
				}
			}
		}
	}

	private int compareStartTime(Abnormal o1, Abnormal o2)
	{
		if(o1.isHideTime() && !o2.isHideTime())
			return 1;
		if(!o1.isHideTime() && o2.isHideTime())
			return -1;
		if(o1.isHideTime() && o2.isHideTime())
		{
			if(o1.getDisplayId() > o2.getDisplayId())
				return 1;
			if(o1.getDisplayId() < o2.getDisplayId())
				return -1;
		}
		return Long.compare(o1.getStartTime(), o2.getStartTime());
	}

	static
	{
		instance = new EffectsComparator();
	}
}
