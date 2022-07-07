package l2s.gameserver.model.reward;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.ArrayList;
import java.util.List;

public class RewardList extends ArrayList<RewardGroup>
{
	public static final int MAX_CHANCE = 1000000;
	private final RewardType _type;
	private final boolean _autoLoot;

	public RewardList(RewardType rewardType, boolean a)
	{
		super(5);
		_type = rewardType;
		_autoLoot = a;
	}

	public List<RewardItem> roll(Player player, double chance_modifier, NpcInstance npc)
	{
		List<RewardItem> temp = new ArrayList<>();
		for(RewardGroup g : this)
			temp.addAll(g.roll(_type, player, chance_modifier, npc));
		return temp;
	}

	public boolean isAutoLoot()
	{
		return _autoLoot;
	}

	public RewardType getType()
	{
		return _type;
	}
}
