package l2s.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.service.GveStageService;
import l2s.gameserver.stats.Stats;

public class RewardGroup implements Cloneable
{
	private double _chance;
	private boolean _isAdena;
	private boolean _notRate;
	private final List<RewardData> _items;

	public RewardGroup(double chance)
	{
		_isAdena = true;
		_notRate = false;
		_items = new ArrayList<>();
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

	public double getChance()
	{
		return _chance;
	}

	public void setChance(double chance)
	{
		_chance = Math.min(chance, RewardList.MAX_CHANCE);
	}

	public boolean isAdena()
	{
		return _isAdena;
	}

	public void setIsAdena(boolean isAdena)
	{
		_isAdena = isAdena;
	}

	public void addData(RewardData item)
	{
		if(!item.getItem().isAdena())
			_isAdena = false;
		_items.add(item);
	}

	public List<RewardData> getItems()
	{
		return _items;
	}

	@Override
	public RewardGroup clone()
	{
		RewardGroup ret = new RewardGroup(_chance);
		for(RewardData i : _items)
			ret.addData(i.clone());
		return ret;
	}

	public List<RewardItem> roll(RewardType type, Player player, double chance_modifier, NpcInstance npc)
	{
		switch(type)
		{
			case NOT_RATED_GROUPED:
			{
				return rollItems(chance_modifier, 1.0);
			}
			case NOT_RATED_NOT_GROUPED:
			case EVENT_GROUPED:
			{
				if(npc != null && npc.getReflection().isDefault() && !npc.isRaid() && (npc.getLeader() == null || !npc.getLeader().isRaid()))
					return rollItems(chance_modifier * player.getDropChanceMod() / Config.DROP_CHANCE_MODIFIER, player.getRateItems() / Config.RATE_DROP_ITEMS_BY_LVL[player.getLevel()]);
				return Collections.emptyList();
			}
			case SWEEP:
			{
				return rollItems(chance_modifier * player.getSpoilChanceMod(), player.getRateSpoil() * (npc != null ? npc.calcStat(Stats.SPOIL_RATE_MULTIPLIER, 1.0, player, null) : 1.0));
			}
			case RATED_GROUPED:
			{
				if(isAdena())
					return rollAdena(chance_modifier, player.getRateAdena() * (npc != null ? npc.calcStat(Stats.ADENA_RATE_MULTIPLIER, 1.0, player, null) : 1.0));
				return rollItems(chance_modifier * player.getDropChanceMod(), npc != null ? npc.getRewardRate(player) * npc.calcStat(Stats.DROP_RATE_MULTIPLIER, 1.0, player, null) : player.getRateItems());
			}
			case RATED_NOT_GROUPED:
			{
				return rollItems(chance_modifier * player.getDropChanceMod() / Config.DROP_CHANCE_MODIFIER, player.getRateItems() / Config.RATE_DROP_ITEMS_BY_LVL[player.getLevel()]);
			}
			default:
			{
				return Collections.emptyList();
			}
		}
	}

	private List<RewardItem> rollAdena(double chance_modifier, double rate_modifier)
	{
		if(notRate())
		{
			chance_modifier = Math.min(chance_modifier, 1.0);
			rate_modifier = 1.0;
		}

		if(chance_modifier <= 0.0 || rate_modifier <= 0.0 || getChance() <= Rnd.get(RewardList.MAX_CHANCE))
			return Collections.emptyList();

		List<RewardItem> rolledItems = new ArrayList<>();

		for(RewardData data : getItems())
		{
			RewardItem item = data.rollAdena(chance_modifier, rate_modifier);
			if(item != null)
				rolledItems.add(item);
		}

		if(rolledItems.isEmpty())
			return Collections.emptyList();

		List<RewardItem> result = new ArrayList<>();

		for(int i = 0; i < Config.MAX_DROP_ITEMS_FROM_ONE_GROUP; ++i)
		{
			RewardItem rolledItem = Rnd.get(rolledItems);
			if(rolledItems.remove(rolledItem))
				result.add(rolledItem);
			if(rolledItems.isEmpty())
				break;
		}

		return result;
	}

	private List<RewardItem> rollItems(double chance_modifier, double rate_modifier)
	{
		if(notRate())
		{
			chance_modifier = Math.min(chance_modifier, 1.0);
			rate_modifier = 1.0;
		}
		if(chance_modifier > 0.0 && rate_modifier > 0.0)
		{
			double chance = getChance() * chance_modifier;
			if(chance > RewardList.MAX_CHANCE)
			{
				chance_modifier = (chance - RewardList.MAX_CHANCE) / getChance() + 1.0;
				chance = RewardList.MAX_CHANCE;
			}
			else
				chance_modifier = 1.0;

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
					List<RewardItem> rolledItems = new ArrayList<>();
					for(RewardData data : getItems())
					{
						if (GveStageService.getInstance().isDropItemAllowed(data.getItemId())) {
							RewardItem item = data.rollItem(chance_modifier, rate_modifier);
							if(item != null)
								rolledItems.add(item);
						}
					}

					if(rolledItems.isEmpty())
						return Collections.emptyList();

					List<RewardItem> result = new ArrayList<>();
					for(int i = 0; i < Config.MAX_DROP_ITEMS_FROM_ONE_GROUP; ++i)
					{
						RewardItem rolledItem = Rnd.get(rolledItems);

						if(rolledItems.remove(rolledItem))
							result.add(rolledItem);

						if(rolledItems.isEmpty())
							break;
					}
					return result;
				}
			}
		}
		return Collections.emptyList();
	}
}
