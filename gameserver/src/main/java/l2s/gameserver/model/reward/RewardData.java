package l2s.gameserver.model.reward;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.templates.item.ItemTemplate;

public class RewardData implements Cloneable
{
	private final ItemTemplate _item;
	private boolean _notRate;
	private long _mindrop;
	private long _maxdrop;
	private double _chance;

	public RewardData(int itemId)
	{
		_notRate = false;
		_item = ItemHolder.getInstance().getTemplate(itemId);
	}

	public RewardData(int itemId, long min, long max, double chance)
	{
		this(itemId);
		_mindrop = min;
		_maxdrop = max;
		setChance(chance);
	}

	public boolean notRate()
	{
		return _notRate;
	}

	public void setNotRate(boolean notRate)
	{
		_notRate = notRate;
	}

	public int getItemId()
	{
		return _item.getItemId();
	}

	public ItemTemplate getItem()
	{
		return _item;
	}

	public long getMinDrop()
	{
		return _mindrop;
	}

	public long getMaxDrop()
	{
		return _maxdrop;
	}

	public double getChance()
	{
		return _chance;
	}

	public void setMinDrop(long mindrop)
	{
		_mindrop = mindrop;
	}

	public void setMaxDrop(long maxdrop)
	{
		_maxdrop = maxdrop;
	}

	public void setChance(double chance)
	{
		_chance = Math.min(chance, RewardList.MAX_CHANCE);
	}

	@Override
	public String toString()
	{
		return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
	}

	@Override
	public RewardData clone()
	{
		return new RewardData(getItemId(), getMinDrop(), getMaxDrop(), getChance());
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof RewardData)
		{
			RewardData drop = (RewardData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return 18 * getItemId() + 184140;
	}

	protected RewardItem rollAdena(double chance_modifier, double rate_modifier)
	{
		if(notRate())
		{
			chance_modifier = Math.min(chance_modifier, 1.0);
			rate_modifier = 1.0;
		}

		if(chance_modifier > 0.0 && rate_modifier > 0.0)
		{
			double chance = getChance() * chance_modifier;
			if(chance > Rnd.get(RewardList.MAX_CHANCE))
			{
				RewardItem t = new RewardItem(_item.getItemId());
				if(getMinDrop() >= getMaxDrop())
					t.count = (long) (rate_modifier * getMinDrop());
				else
					t.count = (long) (rate_modifier * Rnd.get(getMinDrop(), getMaxDrop()));
				return t;
			}
		}
		return null;
	}

	protected RewardItem rollItem(double chance_modifier, double rate_modifier)
	{
		if(notRate())
		{
			chance_modifier = Math.min(chance_modifier, 1.0);
			rate_modifier = 1.0;
		}

		if(chance_modifier > 0.0 && rate_modifier > 0.0)
		{
			double chance = Math.min(RewardList.MAX_CHANCE, getChance() * chance_modifier);

			if(chance > 0.0)
			{
				int rolledCount = 0;
				int mult = (int) Math.ceil(rate_modifier);
				if(chance >= RewardList.MAX_CHANCE)
				{
					rolledCount = (int) rate_modifier;
					if(mult > rate_modifier && chance * (rate_modifier - (mult - 1)) > Rnd.get(RewardList.MAX_CHANCE))
						++rolledCount;
				}
				else
					for(int n = 0; n < mult; ++n)
						if(chance * Math.min(rate_modifier - n, 1.0) > Rnd.get(RewardList.MAX_CHANCE))
							++rolledCount;

				if(rolledCount > 0)
				{
					RewardItem t = new RewardItem(_item.getItemId());
					if(getMinDrop() >= getMaxDrop())
						t.count = (long) (rate_modifier * getMinDrop());
					else
						t.count = (long) (rate_modifier * Rnd.get(getMinDrop(), getMaxDrop()));
					return t;
				}
			}
		}
		return null;
	}
}
