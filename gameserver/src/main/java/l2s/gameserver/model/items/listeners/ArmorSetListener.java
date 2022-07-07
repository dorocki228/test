package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.ArmorSetsHolder;
import l2s.gameserver.data.xml.holder.EnchantBonusHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.ArmorSet;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.enchant.EnchantBonusStat;
import l2s.gameserver.model.items.enchant.EnchantBonusStatFuncType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncAdd;
import l2s.gameserver.stats.funcs.FuncMul;

import java.util.ArrayList;
import java.util.List;

public final class ArmorSetListener implements OnEquipListener
{
	private static final ArmorSetListener _instance;

	private static final String SET_BONUS_FUNC_OWNER = "SET_BONUS_FUNC_OWNER";

	public static ArmorSetListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		List<ArmorSet> armorSets = ArmorSetsHolder.getInstance().getArmorSets(item.getItemId());
		if(armorSets == null || armorSets.isEmpty())
			return;
		Player player = (Player) actor;
		boolean update = false;
		for(ArmorSet armorSet : armorSets)
			if(armorSet.containItem(slot, item.getItemId()))
			{
				List<SkillEntry> skills = armorSet.getSkills(armorSet.getEquipedSetPartsCount(player));
				for(SkillEntry skillEntry : skills)
				{
					player.addSkill(skillEntry, false);
					update = true;
				}
				if(!armorSet.containAll(player))
					continue;
				if(armorSet.containShield(player))
				{
					skills = armorSet.getShieldSkills();
					for(SkillEntry skillEntry : skills)
					{
						player.addSkill(skillEntry, false);
						update = true;
					}
				}
				int enchantLevel = armorSet.getEnchantLevel(player);
				if(enchantLevel >= 6)
				{
					skills = armorSet.getEnchant6skills();
					for(SkillEntry skillEntry2 : skills)
					{
						player.addSkill(skillEntry2, false);
						update = true;
					}
				}
				if(enchantLevel >= 7)
				{
					skills = armorSet.getEnchant7skills();
					for(SkillEntry skillEntry2 : skills)
					{
						player.addSkill(skillEntry2, false);
						update = true;
					}
				}
				if(enchantLevel >= 8)
				{
					skills = armorSet.getEnchant8skills();
					for(SkillEntry skillEntry2 : skills)
					{
						player.addSkill(skillEntry2, false);
						update = true;
					}
				}

				List<EnchantBonusStat> bonuses = EnchantBonusHolder.getInstance().getBonusesForSet(enchantLevel);
				if(!bonuses.isEmpty())
					update = true;

				bonuses.forEach(bonusStat -> {
					Func func;
					if(bonusStat.getFunc() == EnchantBonusStatFuncType.ADD)
						func = new FuncAdd(bonusStat.getStat(), 128, SET_BONUS_FUNC_OWNER, bonusStat.getValue());
					else
						func = new FuncMul(bonusStat.getStat(), 128, SET_BONUS_FUNC_OWNER, bonusStat.getValue());

					player.addStatFunc(func);
				});

				player.setArmorSetEnchant(enchantLevel);
			}
			else
			{
				if(!armorSet.containShield(item.getItemId()) || !armorSet.containAll(player))
					continue;
				List<SkillEntry> skills = armorSet.getShieldSkills();
				for(SkillEntry skillEntry : skills)
				{
					player.addSkill(skillEntry, false);
					update = true;
				}
			}
		if(update)
		{
			player.sendSkillList();
			player.updateStats();
		}
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		List<ArmorSet> armorSets = ArmorSetsHolder.getInstance().getArmorSets(item.getItemId());
		if(armorSets == null || armorSets.isEmpty())
			return;
		Player player = (Player) actor;
		boolean update = false;
		for(ArmorSet armorSet : armorSets)
		{
			boolean remove = false;
            List<SkillEntry> removeSkillId1 = new ArrayList<>();
			List<SkillEntry> removeSkillId2 = new ArrayList<>();
			List<SkillEntry> removeSkillId3 = new ArrayList<>();
			List<SkillEntry> removeSkillId4 = new ArrayList<>();
			List<SkillEntry> removeSkillId5 = new ArrayList<>();
			if(armorSet.containItem(slot, item.getItemId()))
			{
				remove = true;
                boolean setPartUneqip = true;
                removeSkillId1 = armorSet.getSkillsToRemove();
				removeSkillId2 = armorSet.getShieldSkills();
				removeSkillId3 = armorSet.getEnchant6skills();
				removeSkillId4 = armorSet.getEnchant7skills();
				removeSkillId5 = armorSet.getEnchant8skills();
			}
			else if(armorSet.containShield(item.getItemId()))
			{
				remove = true;
				removeSkillId2 = armorSet.getShieldSkills();
			}
			if(remove)
			{
				for(SkillEntry skillEntry : removeSkillId1)
				{
					player.removeSkill(skillEntry, false);
					update = true;
				}
				for(SkillEntry skillEntry : removeSkillId2)
				{
					player.removeSkill(skillEntry);
					update = true;
				}
				for(SkillEntry skillEntry : removeSkillId3)
				{
					player.removeSkill(skillEntry);
					update = true;
				}
				for(SkillEntry skillEntry : removeSkillId4)
				{
					player.removeSkill(skillEntry);
					update = true;
				}
				for(SkillEntry skillEntry : removeSkillId5)
				{
					player.removeSkill(skillEntry);
					update = true;
				}
				player.setArmorSetEnchant(0);
			}
			List<SkillEntry> skills = armorSet.getSkills(armorSet.getEquipedSetPartsCount(player));
			for(SkillEntry skillEntry2 : skills)
			{
				player.addSkill(skillEntry2, false);
				update = true;
			}
		}

		if(player.removeStatsOwner(SET_BONUS_FUNC_OWNER))
		{
			update = true;
		}

		if(update)
		{
			player.sendSkillList();
			player.updateStats();
		}
	}

	static
	{
		_instance = new ArmorSetListener();
	}
}
