package l2s.gameserver.model.reward;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.templates.item.ItemTemplate;

public class RewardItem
{
	public final int itemId;
	public long count;

	public RewardItem(int itemId)
	{
		this.itemId = itemId;
		count = 1L;
	}

	public boolean isHerb()
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return item != null && item.isHerb();
	}

	public boolean isAdena()
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return item != null && item.isAdena();
	}
}
