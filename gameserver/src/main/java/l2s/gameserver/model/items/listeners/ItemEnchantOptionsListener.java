package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.OptionDataTemplate;

public final class ItemEnchantOptionsListener implements OnEquipListener
{
	private static final ItemEnchantOptionsListener _instance;

	public static ItemEnchantOptionsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		Player player = actor.getPlayer();
		boolean updateStats = false;
		boolean sendSkillList = false;
		for(int i : item.getEnchantOptions())
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if (template == null) {
				continue;
			}

			final OptionDataTemplate oldOption = player.addOptionData(template);
			if(oldOption != null && !oldOption.equals(template))
			{
				updateStats = true;
				if(!template.getSkills().isEmpty())
					sendSkillList = true;
			}
		}
		if(updateStats)
		{
			if(sendSkillList)
				player.sendSkillList();
			player.sendChanges();
		}
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		Player player = actor.getPlayer();
		boolean updateStats = false;
		boolean sendSkillList = false;
		for(int i : item.getEnchantOptions())
		{
			OptionDataTemplate template = player.removeOptionData(i);
			if(template != null)
			{
				updateStats = true;
				if(!template.getSkills().isEmpty())
					sendSkillList = true;
			}
		}
		if(updateStats)
		{
			if(sendSkillList)
				player.sendSkillList();
			player.updateStats();
		}
	}

	static
	{
		_instance = new ItemEnchantOptionsListener();
	}
}
