package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetClan extends Condition
{
	private final boolean _test;

	public ConditionTargetClan(String param)
	{
		_test = Boolean.valueOf(param);
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		Player player = creature.getPlayer();
		Player targetPlayer = target.getPlayer();
		if (player == null || targetPlayer == null)
			return false;

		int playerClanId = player.getClanId();
		Party playerParty = player.getParty();

		return playerClanId != 0 && playerClanId == targetPlayer.getClanId() == _test
				|| playerParty != null && playerParty == targetPlayer.getParty();
	}
}
