package l2s.gameserver.skills.skillclasses;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.ItemFunctions;

public class Harvesting extends Skill
{
	public Harvesting(StatsSet set)
	{
		super(set);
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!activeChar.isPlayer())
			return;
		Player player = (Player) activeChar;
		if(!target.isMonster())
			return;
		MonsterInstance monster = (MonsterInstance) target;
		if(!monster.isSeeded())
		{
			activeChar.sendPacket(SystemMsg.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
			return;
		}
		if(!monster.isSeeded(player))
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
			return;
		}
		int diffPlayerTarget = Math.abs(activeChar.getLevel() - monster.getLevel());
		double SuccessRate = Config.MANOR_HARVESTING_BASIC_SUCCESS;
		if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
			SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
		if(SuccessRate < 1.0)
			SuccessRate = 1.0;
		if(player.isGM())
			player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Harvesting.Chance").addNumber((long) SuccessRate));
		if(!Rnd.chance(SuccessRate))
		{
			activeChar.sendPacket(SystemMsg.THE_HARVEST_HAS_FAILED);
			monster.clearHarvest();
			return;
		}
		RewardItem item = monster.takeHarvest();
		if(item == null)
			return;
		if(!player.getInventory().validateCapacity(item.itemId, item.count) || !player.getInventory().validateWeight(item.itemId, item.count))
		{
			ItemInstance harvest = ItemFunctions.createItem(item.itemId);
			harvest.setCount(item.count);
			harvest.dropToTheGround(player, monster);
			return;
		}
		player.getInventory().addItem(item.itemId, item.count);
		player.sendPacket(new SystemMessagePacket(SystemMsg.C1_HARVESTED_S3_S2S).addName(player).addNumber(item.count).addItemName(item.itemId));
		if(player.isInParty())
		{
			SystemMessagePacket smsg = new SystemMessagePacket(SystemMsg.C1_HARVESTED_S3_S2S).addString(player.getName()).addNumber(item.count).addItemName(item.itemId);
			player.getParty().broadcastToPartyMembers(player, smsg);
		}
	}
}
