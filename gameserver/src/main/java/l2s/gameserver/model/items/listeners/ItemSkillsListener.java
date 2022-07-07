package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.Ensoul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemSkillsListener implements OnEquipListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();

	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player) actor;
		ItemTemplate it = item.getTemplate();
		List<SkillEntry> itemSkills = new ArrayList<>(Arrays.asList(it.getAttachedSkills()));
		SkillEntry enchant4Skill = it.getEnchant4Skill();
		if(enchant4Skill != null)
			itemSkills.add(enchant4Skill);

		for(Ensoul ensoul : item.getNormalEnsouls())
			itemSkills.addAll(ensoul.getSkills());

		for(Ensoul ensoul : item.getSpecialEnsouls())
			itemSkills.addAll(ensoul.getSkills());

		player.removeTriggers(it);
		if(!itemSkills.isEmpty())
		{
			if(it.getItemType() == EtcItemTemplate.EtcItemType.RUNE_SELECT)
				for(SkillEntry itemSkillEntry : itemSkills)
				{
					int level = player.getSkillLevel(itemSkillEntry.getId());
					int newlevel = level - 1;
					if(newlevel > 0)
						player.addSkill(SkillHolder.getInstance().getSkillEntry(itemSkillEntry.getId(), newlevel), false);
					else
						player.removeSkillById(itemSkillEntry.getId());
				}
			else
				for(SkillEntry itemSkillEntry : itemSkills)
				{
					player.removeSkill(itemSkillEntry, false);
					player.enableOnUnequipSkill(itemSkillEntry.getTemplate());
				}
			player.sendSkillList();
			player.updateStats();
		}
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player) actor;
		ItemTemplate it = item.getTemplate();
		List<SkillEntry> itemSkills = new ArrayList<>(Arrays.asList(it.getAttachedSkills()));
		if(item.getFixedEnchantLevel(player) >= 4)
		{
			SkillEntry enchant4Skill = it.getEnchant4Skill();
			if(enchant4Skill != null)
				itemSkills.add(enchant4Skill);
		}

		for(Ensoul ensoul : item.getNormalEnsouls())
			itemSkills.addAll(ensoul.getSkills());

		for(Ensoul ensoul : item.getSpecialEnsouls())
			itemSkills.addAll(ensoul.getSkills());

		if(it.getType2() == 0 && player.getWeaponsExpertisePenalty() > 0)
			return;
		player.addTriggers(it);
		if(!itemSkills.isEmpty())
		{
			if(it.getItemType() == EtcItemTemplate.EtcItemType.RUNE_SELECT)
				for(SkillEntry itemSkillEntry : itemSkills)
				{
					int level = player.getSkillLevel(itemSkillEntry.getId());
					int newlevel;
					if((newlevel = level) > 0)
					{
						if(SkillHolder.getInstance().getSkill(itemSkillEntry.getId(), level + 1) != null)
							newlevel = level + 1;
					}
					else
						newlevel = 1;
					if(newlevel != level)
						player.addSkill(SkillHolder.getInstance().getSkillEntry(itemSkillEntry.getId(), newlevel), false);
				}
			else
				for(SkillEntry itemSkillEntry : itemSkills)
					if(player.getSkillLevel(itemSkillEntry.getId()) < itemSkillEntry.getLevel())
					{
						player.addSkill(itemSkillEntry, false);
						Skill itemSkill = itemSkillEntry.getTemplate();
						if(!itemSkill.isActive() || player.isSkillDisabled(itemSkill))
							continue;

						long reuseDelay = itemSkill.getReuseDelayOnEquip();
						if(reuseDelay > 0L)
							player.disableSkill(itemSkill, reuseDelay, true);
					}
			player.sendSkillList();
			player.updateStats();
		}
	}
}
