package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.Ensoul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemSkillsListener extends AbstractSkillListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();

	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}

	public int onEquip(int slot, ItemInstance item, Playable actor, boolean refresh)
	{
		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		ItemTemplate template = item.getTemplate();

		if(!refresh)
			player.removeTriggers(template);

		int flags = 0;

		List<SkillEntry> addedSkills = new ArrayList<SkillEntry>();

		// Для оружия при несоотвествии грейда скилы не выдаем
		if(template.getType2() != ItemTemplate.TYPE2_WEAPON || player.getExpertiseWeaponPenalty() == 0)
		{
			if(!refresh)
				player.addTriggers(template);

			if(template.getItemType() == EtcItemType.RUNE_SELECT)
			{
				for(SkillEntry itemSkillEntry : template.getAttachedSkills())
				{
					int skillsCount = 1;
					for(ItemInstance ii : player.getInventory().getItems())
					{
						if(ii == item)
							continue;

						ItemTemplate it = ii.getTemplate();
						if(it.getItemType() == EtcItemType.RUNE_SELECT)
						{
							for(SkillEntry se : it.getAttachedSkills())
							{
								if(se == itemSkillEntry)
								{
									skillsCount++;
									break;
								}
							}
						}
					}

					int skillLevel = Math.min(itemSkillEntry.getTemplate().getMaxLevel(), skillsCount);
					SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, itemSkillEntry.getId(), skillLevel);
					if(skillEntry != null)
						addedSkills.add(skillEntry);
				}
			}
			else
			{
				addedSkills.addAll(Arrays.asList(template.getAttachedSkills()));

				for(int e = item.getFixedEnchantLevel(player); e >= 0; e--)
				{
					List<SkillEntry> enchantSkills = template.getEnchantSkills(e);
					if(enchantSkills != null) {
						addedSkills.addAll(enchantSkills);
						break;
					}
				}

				addedSkills.addAll(item.getAppearanceStoneSkills());

				for(Ensoul ensoul : item.getNormalEnsouls())
					addedSkills.addAll(ensoul.getSkills());

				for(Ensoul ensoul : item.getSpecialEnsouls())
					addedSkills.addAll(ensoul.getSkills());
			}
		}

		flags |= refreshSkills(actor, item, addedSkills);
		return flags;
	}

	@Override
	protected boolean canAddSkill(Playable actor, ItemInstance item, SkillEntry skillEntry)
	{
		if(item.getTemplate().getItemType() == EtcItemType.RUNE_SELECT)
			return true;

		if (skillEntry.getLevel() < actor.getSkillLevel(skillEntry.getId())) {
			for (ItemInstance tempItem : actor.getInventory().getItems()) {
				if (tempItem != item) {
					int tempSkillLevel = tempItem.getEquippedSkillLevel(skillEntry.getId());
					if (tempSkillLevel > skillEntry.getLevel())
						return false;
				}
			}
			return true;
		}
		return skillEntry.getLevel() > actor.getSkillLevel(skillEntry.getId());
	}

	@Override
	protected int onAddSkill(Playable actor, ItemInstance item, SkillEntry skillEntry)
	{
		Skill itemSkill = skillEntry.getTemplate();
		if(itemSkill.isActive())
		{
			if(!actor.isSkillDisabled(itemSkill))
			{
				long reuseDelay = actor.getStat().getReuseTime(itemSkill);
				reuseDelay = Math.min(reuseDelay, 8000);

				if(reuseDelay > 0)
					actor.disableSkill(itemSkill, reuseDelay);
			}
		}
		return 0;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		return onEquip(slot, item, actor, false);
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!actor.isPlayer())
			return 0;

		actor.removeTriggers(item.getTemplate());
		return super.onUnequip(slot, item, actor);
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return onEquip(item.getEquipSlot(), item, actor, true);
	}
}