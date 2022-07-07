package l2s.gameserver.model.instances;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class BossInstance extends RaidBossInstance
{
	private boolean _teleportedToNest;

	public BossInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public boolean isBoss()
	{
		return true;
	}

	@Override
	public final boolean isMovementDisabled()
	{
		return getNpcId() == 29006 || super.isMovementDisabled();
	}

	public void setTeleported(boolean flag)
	{
		_teleportedToNest = flag;
	}

	public boolean isTeleported()
	{
		return _teleportedToNest;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public void rollRewards(RewardList list, Creature lastAttacker, Creature topDamager) {
		RewardType type = list.getType();
		if(type == RewardType.SWEEP && !isSpoiled())
			return;
		Creature activeChar = type == RewardType.SWEEP ? lastAttacker : lockDropTo(topDamager);
		Player activePlayer = activeChar.getPlayer();
		if(activePlayer == null)
			return;
		double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()));

		Set<Player> dropPlayers = getAggroList().getPlayableMap().entrySet().stream()
			.filter(entry -> entry.getValue().damage >= Config.EPIC_BOSSES_DAMAGE_FOR_REWARD_AMOUNT)
			.map(Map.Entry::getKey)
			.map(GameObject::getPlayer)
			.collect(Collectors.toSet());

		List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, this);
		for (RewardItem drop : rewardItems) {
			if (!Config.DROP_ONLY_THIS.isEmpty() && !Config.DROP_ONLY_THIS.contains(drop.itemId)
				&& (!Config.INCLUDE_RAID_DROP || !isRaid())) {
				return;
			}

			dropItemToTheGround(dropPlayers, drop.itemId, drop.count);
		}

		if (getChampion() > 0 && Config.SPECIAL_ITEM_ID > 0 && Math.abs(getLevel() - activePlayer.getLevel()) < 9 && Rnd
			.chance(Config.SPECIAL_ITEM_DROP_CHANCE)) {
			ItemFunctions.addItem(activePlayer, Config.SPECIAL_ITEM_ID, Config.SPECIAL_ITEM_COUNT);
		}
	}
}
