package l2s.gameserver.templates.item;

import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EtcItemTemplate extends ItemTemplate
{
	private static final Logger _log;
	private final String _handlerName;
	private IItemHandler _handler;

	public EtcItemTemplate(StatsSet set)
	{
		super(set);
		_handlerName = set.getString("handler", null);
		_type = (ItemType) set.getEnum("type", (Class) EtcItemType.class, (Enum) EtcItemType.OTHER);
		_type1 = 4;
		switch(getItemType())
		{
			case QUEST:
			{
				_type2 = 3;
				break;
			}
			case CURRENCY:
			{
				_type2 = 4;
				break;
			}
			default:
			{
				_type2 = 5;
				break;
			}
		}
		_handler = getItemType().getHandler();
		_exType = (ExItemType) set.getEnum("ex_type", (Class) ExItemType.class, (Enum) getItemType().getExType());
		if(_handlerName != null && !_handlerName.isEmpty())
		{
			_handler = ItemHandler.getInstance().getItemHandler(_handlerName);
			if(_handler == null)
				_log.warn("Cannot find item handler: " + _handlerName + " for item ID[" + getItemId() + "]!");
		}
	}

	@Override
	public EtcItemType getItemType()
	{
		return (EtcItemType) super.getItemType();
	}

	@Override
	public long getItemMask()
	{
		return getItemType().mask();
	}

	@Override
	public final boolean isShadowItem()
	{
		return false;
	}

	@Override
	public boolean isEnchantable()
	{
		return false;
	}

	@Override
	public boolean isCrystallizable()
	{
		return false;
	}

	@Override
	public IItemHandler getHandler()
	{
		return _handler;
	}

	static
	{
		_log = LoggerFactory.getLogger(EtcItemTemplate.class);
	}

	public enum EtcItemType implements ItemType
	{
		OTHER(ItemHandler.DEFAULT_HANDLER, ExItemType.OTHER_ITEMS),
		QUEST(null, ExItemType.NONE_0),
		CURRENCY(ItemHandler.DEFAULT_HANDLER, ExItemType.NONE_0),
		ARROW(ItemHandler.EQUIPABLE_HANDLER, ExItemType.OTHER_ITEMS),
		POTION(ItemHandler.SKILL_REDUCE_ITEM_HANDLER, ExItemType.POTION),
		SCROLL_ENCHANT_WEAPON(ItemHandler.ENCHANT_SCROLL_HANDLER, ExItemType.SCROLL_ENCHANT_WEAPON),
		SCROLL_ENCHANT_ARMOR(ItemHandler.ENCHANT_SCROLL_HANDLER, ExItemType.SCROLL_ENCHANT_ARMOR),
		SCROLL(ItemHandler.SKILL_REDUCE_ITEM_HANDLER, ExItemType.SCROLL_OTHER),
		RECIPE(ItemHandler.RECIPE_HANDLER, ExItemType.RECIPE),
		MATERIAL(ItemHandler.DEFAULT_HANDLER, ExItemType.CRAFTING_MAIN_INGRIDIENTS),
		PET_COLLAR(ItemHandler.PET_SUMMON_HANDLER, ExItemType.PET_SUPPLIES),
		DYE(ItemHandler.DEFAULT_HANDLER, ExItemType.DYES),
		SEED(ItemHandler.SEED_HANDLER, ExItemType.OTHER_ITEMS),
		ALT_SEED(ItemHandler.SEED_HANDLER, ExItemType.OTHER_ITEMS),
		CROP(ItemHandler.DEFAULT_HANDLER, ExItemType.OTHER_ITEMS),
		MATURECROP(ItemHandler.DEFAULT_HANDLER, ExItemType.OTHER_ITEMS),
		LURE(ItemHandler.EQUIPABLE_HANDLER, ExItemType.OTHER_ITEMS),
		WEAPON_ENCHANT_STONE(ItemHandler.DEFAULT_HANDLER, ExItemType.WEAPON_ENCHANT_STONE),
		ARMOR_ENCHANT_STONE(ItemHandler.DEFAULT_HANDLER, ExItemType.ARMOR_ENCHANT_STONE),
		RUNE_SELECT(ItemHandler.DEFAULT_HANDLER, ExItemType.OTHER_ITEMS),
		RUNE(ItemHandler.DEFAULT_HANDLER, ExItemType.OTHER_ITEMS),
		SOULSHOT(ItemHandler.SOULSHOT_HANDLER, ExItemType.SOULSHOT),
		SPIRITSHOT(ItemHandler.SPIRITSHOT_HANDLER, ExItemType.SPIRITSHOT),
		BLESSED_SPIRITSHOT(ItemHandler.BLESSED_SPIRITSHOT_HANDLER, ExItemType.SPIRITSHOT),
		BEAST_SOULSHOT(ItemHandler.BEAST_SOULSHOT_HANDLER, ExItemType.SOULSHOT),
		BEAST_SPIRITSHOT(ItemHandler.BEAST_SPIRITSHOT_HANDLER, ExItemType.SPIRITSHOT),
		BEAST_BLESSED_SPIRITSHOT(ItemHandler.BEAST_BLESSED_SPIRITSHOT_HANDLER, ExItemType.SPIRITSHOT),
		FISHSHOT(ItemHandler.FISHSHOT_HANDLER, ExItemType.SOULSHOT),
		PET_SUPPLIES(ItemHandler.SKILL_REDUCE_ITEM_HANDLER, ExItemType.PET_SUPPLIES),
		EXTRACTABLE(ItemHandler.CAPSULED_ITEM_HANDLER, ExItemType.OTHER_ITEMS),
		CRYSTAL(ItemHandler.DEFAULT_HANDLER, ExItemType.CRYSTAL),
		LIFE_STONE(ItemHandler.DEFAULT_HANDLER, ExItemType.LIFE_STONE),
		ACC_LIFE_STONE(ItemHandler.DEFAULT_HANDLER, ExItemType.LIFE_STONE),
		SPELLBOOK(ItemHandler.DEFAULT_HANDLER, ExItemType.SPELLBOOK),
		FORGOTTEN_SPELLBOOK(ItemHandler.SKILL_REDUCE_ITEM_HANDLER, ExItemType.SPELLBOOK),
		GEMSTONE(ItemHandler.DEFAULT_HANDLER, ExItemType.GEMSTONE),
		POUCH(ItemHandler.DEFAULT_HANDLER, ExItemType.POUCH),
		PIN(ItemHandler.DEFAULT_HANDLER, ExItemType.PIN),
		MAGIC_RUNE_CLIP(ItemHandler.DEFAULT_HANDLER, ExItemType.MAGIC_RUNE_CLIP),
		MAGIC_ORNAMENT(ItemHandler.DEFAULT_HANDLER, ExItemType.MAGIC_ORNAMENT),
		HERB(ItemHandler.DEFAULT_HANDLER, ExItemType.NONE_0),
		ARROW_QUIVER(ItemHandler.EQUIPABLE_HANDLER, ExItemType.OTHER_ITEMS),
		SOUL_CRYSTAL(ItemHandler.DEFAULT_HANDLER, ExItemType.SOUL_CRYSTAL),
		MERCENARY_TICKET(ItemHandler.MERCENARY_TICKET_HANDLER, ExItemType.OTHER_ITEMS);

		private final IItemHandler _handler;
		private final ExItemType _exType;

		EtcItemType(IItemHandler handler, ExItemType exType)
		{
			_handler = handler;
			_exType = exType;
		}

		@Override
		public long mask()
		{
			return 0L;
		}

		@Override
		public IItemHandler getHandler()
		{
			return _handler;
		}

		@Override
		public ExItemType getExType()
		{
			return _exType;
		}

		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}
}
