package l2s.gameserver.stats.funcs;

import l2s.gameserver.data.xml.holder.EnchantBonusHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.enchant.EnchantBonusStat;
import l2s.gameserver.model.items.enchant.EnchantBonusStatFuncType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemType;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner);
	}

	@Override
	public double calc(Creature creature, Creature target, Skill skill, double value)
	{
		ItemInstance item = (ItemInstance) owner;

		int enchant = item.getEnchantLevel();
		int overenchant = Math.max(0, enchant - (item.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? 4 : 3));

		switch(stat)
		{
			case SHIELD_DEFENCE:
			case MAGIC_DEFENCE:
			case POWER_DEFENCE:
			{
				return value + enchant + overenchant * 2;
			}
			case MAGIC_ATTACK:
			{
				switch(item.getTemplate().getGrade().getCrystalId())
				{
					case ItemTemplate.CRYSTAL_S:
					case ItemTemplate.CRYSTAL_A:
					case ItemTemplate.CRYSTAL_B:
					case ItemTemplate.CRYSTAL_C:
					case ItemTemplate.CRYSTAL_D:
					case ItemTemplate.CRYSTAL_NONE:
						value += 3 * (enchant + overenchant);
						break;
				}
				return value;
			}
			case POWER_ATTACK:
			{
				ItemType itemType = item.getItemType();
				boolean isBow = itemType == WeaponType.BOW;
				boolean isSword = (itemType == WeaponType.DUALFIST || itemType == WeaponType.DUAL || itemType == WeaponType.BIGSWORD || itemType == WeaponType.SWORD || itemType == WeaponType.BLUNT || itemType == WeaponType.BIGBLUNT) && item.getTemplate().getBodyPart() == ItemTemplate.SLOT_LR_HAND;

				switch(item.getTemplate().getGrade().getCrystalId())
				{
					case ItemTemplate.CRYSTAL_S:
					case ItemTemplate.CRYSTAL_A:
					case ItemTemplate.CRYSTAL_D:
					case ItemTemplate.CRYSTAL_NONE:
						if(isBow)
							value += 8 * (enchant + overenchant);
						else if(isSword)
							value += 5 * (enchant + overenchant);
						else
							value += 4 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_B:
					case ItemTemplate.CRYSTAL_C:
						if(isBow)
							value += 6 * (enchant + overenchant);
						else if(isSword)
							value += 4 * (enchant + overenchant);
						else
							value += 3 * (enchant + overenchant);
						break;
				}
			}
			case SOULSHOT_POWER:
			case SPIRITSHOT_POWER:
			{
				return value + Math.min(30, enchant) * 0.7;
			}
			default:
			{
				var bonuses = EnchantBonusHolder.getInstance().getBonuses(creature.getPlayer(), item, stat);
				for(EnchantBonusStat bonus : bonuses)
				{
					if(bonus.getFunc() == EnchantBonusStatFuncType.ADD)
						value += bonus.getValue();
					else if(bonus.getFunc() == EnchantBonusStatFuncType.MUL)
						value *= bonus.getValue();
				}
			}
		}

		return value;
	}
}