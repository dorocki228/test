package l2s.gameserver.handler.items.impl;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.data.CapsuledItemData;
import l2s.gameserver.utils.ItemFunctions;

import java.util.List;

public class CapsuledItemHandler extends DefaultItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player player;
		if(playable.isPlayer())
			player = (Player) playable;
		else
		{
			if(!playable.isPet())
				return false;
			player = playable.getPlayer();
		}
		int itemId = item.getItemId();
		if(!DefaultItemHandler.canBeExtracted(player, item))
			return false;
		if(!DefaultItemHandler.reduceItem(player, item))
			return false;
		List<CapsuledItemData> capsuled_items = item.getTemplate().getCapsuledItems();
		for(CapsuledItemData ci : capsuled_items)
			if(Rnd.chance(ci.getChance()))
			{
				long minCount = ci.getMinCount();
				long maxCount = ci.getMaxCount();
				long count;
				if(minCount == maxCount)
					count = minCount;
				else
					count = Rnd.get(minCount, maxCount);
				ItemFunctions.addItem(player, ci.getId(), count, ci.getEnchantLevel(), true);
			}
		player.sendPacket(SystemMessagePacket.removeItems(itemId, 1L));
		return true;
	}
}
