package l2s.gameserver.templates.item;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.xml.holder.EnchantBonusHolder;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.items.impl.EquipableItemHandler;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.enchant.EnchantBonusItemType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.FuncAdd;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.augmentation.AugmentationInfo;
import l2s.gameserver.templates.item.data.CapsuledItemData;
import l2s.gameserver.templates.item.support.VisualChange;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemTemplate extends StatTemplate
{
	public static final int ITEM_ID_PC_BANG_POINTS = -100;
	public static final int ITEM_ID_CLAN_REPUTATION_SCORE = -200;
	public static final int ITEM_ID_FAME = -300;
	public static final int ITEM_ID_ADENA = 57;
	public static final int ITEM_ID_FORMAL_WEAR = 6408;
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_OTHER = 2;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int PET_EQUIP_TYPE_WOLF = 1;
	public static final int PET_EQUIP_TYPE_HATCHLING = 2;
	public static final int PET_EQUIP_TYPE_STRIDER = 3;
	public static final int PET_EQUIP_TYPE_GREAT_WOLF = 4;
	public static final int PET_EQUIP_TYPE_PENDANT = 5;
	public static final int PET_EQUIP_TYPE_BABY = 6;

	public static final int SLOT_NONE = 0x00000;
	public static final int SLOT_UNDERWEAR = 0x00001;

	public static final int SLOT_R_EAR = 0x00002;
	public static final int SLOT_L_EAR = 0x00004;

	public static final int SLOT_NECK = 0x00008;

	public static final int SLOT_R_FINGER = 0x00010;
	public static final int SLOT_L_FINGER = 0x00020;

	public static final int SLOT_HEAD = 0x00040;
	public static final int SLOT_R_HAND = 0x00080;
	public static final int SLOT_L_HAND = 0x00100;
	public static final int SLOT_GLOVES = 0x00200;
	public static final int SLOT_CHEST = 0x00400;
	public static final int SLOT_LEGS = 0x00800;
	public static final int SLOT_FEET = 0x01000;
	public static final int SLOT_BACK = 0x02000;
	public static final int SLOT_LR_HAND = 0x04000;
	public static final int SLOT_FULL_ARMOR = 0x08000;
	public static final int SLOT_HAIR = 0x10000;
	public static final int SLOT_FORMAL_WEAR = 0x20000;
	public static final int SLOT_DHAIR = 0x40000;
	public static final int SLOT_HAIRALL = 0x80000;
	public static final int SLOT_R_BRACELET = 0x100000;
	public static final int SLOT_L_BRACELET = 0x200000;
	public static final int SLOT_DECO = 0x400000;
	public static final int SLOT_BELT = 0x10000000;
	public static final int SLOT_BROOCH = 0x20000000;
	public static final int SLOT_JEWEL = 0x40000000;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	public static final int SLOT_GWOLF = -104;
	public static final int SLOT_PENDANT = -105;

	public static final int CRYSTAL_NONE = 0;
	public static final int CRYSTAL_D = 1458;
	public static final int CRYSTAL_C = 1459;
	public static final int CRYSTAL_B = 1460;
	public static final int CRYSTAL_A = 1461;
	public static final int CRYSTAL_S = 1462;

	public static final int[] HERO_WEAPON_IDS = { 6611, 6612, 6613, 6614, 6616, 6617, 6618, 6619, 6620, 6621 };

	private final int _itemId;
	private final String _name;
	private final String _addname;
	private final String _icon;
	private final int _weight;
	private final int _referencePrice;
	private final int _durability;
	private final boolean _temporal;
	private final ItemGrade _grade;
	private int _flags;
	protected ItemType _type;
	protected int _type1;
	protected int _type2;
	protected ExItemType _exType;
	protected int _petType;
	protected SkillEntry[] _skills;
	private SkillEntry _enchant4Skill;
	private final List<Condition> _conditions;
	private final boolean _stackable;
	private final ItemReuseType _reuseType;
	private final int _reuseDelay;
	private final int _reuseGroup;
	private final List<CapsuledItemData> _capsuledItems;
	protected int _bodyPart;
	private final int _crystalCount;
	private int[] _baseAttributes;

	private IntObjectMap<int[]> _enchantOptions;
	private IntObjectMap<AugmentationInfo> _augmentationInfos = Containers.emptyIntObjectMap();

	private List<VisualChange> visualChanges = List.of();

	private final boolean _isPvP;
	private final ItemQuality _quality;
	private final int _baseEnchantLevel;
	private final long _priceLimit;
	private int _pAtk;
	private int _mAtk;
	private int _pDef;
	private int _mDef;

	protected ItemTemplate(StatsSet set)
	{
		_exType = ExItemType.OTHER_ITEMS;
		_enchant4Skill = null;
		_conditions = new ArrayList<>();
		_capsuledItems = new ArrayList<>();
		_baseAttributes = new int[6];
		_enchantOptions = Containers.emptyIntObjectMap();
		_pAtk = 0;
		_mAtk = 0;
		_pDef = 0;
		_mDef = 0;
		_itemId = set.getInteger("item_id");
		_name = set.getString("name");
		_addname = set.getString("add_name", "");
		_icon = set.getString("icon", "");
		_weight = set.getInteger("weight", 0);
		_referencePrice = set.getInteger("price", 0);
		_durability = set.getInteger("durability", -1);
		_temporal = set.getBool("temporal", false);
		_grade = set.getEnum("crystal_type", ItemGrade.class, ItemGrade.NONE);
		_stackable = set.getBool("stackable", false);
		_bodyPart = set.getInteger("bodypart", 0);
		_reuseType = set.getEnum("reuse_type", ItemReuseType.class, ItemReuseType.NORMAL);
		_reuseDelay = set.getInteger("reuse_delay", 0);
		_reuseGroup = set.getInteger("delay_share_group", -_itemId);
		_isPvP = set.getBool("is_pvp", false);
		_baseEnchantLevel = set.getInteger("enchanted", 0);
		_crystalCount = set.getInteger("crystal_count", 0);
		_priceLimit = set.getLong("price_limit", 0L);
		_quality = set.getEnum("item_quality", ItemQuality.class, ItemQuality.NORMAL);
		for(ItemFlags f : ItemFlags.VALUES)
		{
			boolean flag = set.getBool(f.name().toLowerCase(), f.getDefaultValue());
			if(flag)
				activeFlag(f);
		}
		_funcTemplates = FuncTemplate.EMPTY_ARRAY;
		_skills = SkillEntry.EMPTY_ARRAY;
	}

	protected void initEnchantFuncs()
	{
		if(isWeapon())
		{
			attachFunc(new FuncTemplate(null, "Enchant", Stats.POWER_ATTACK, 12, 0.0));
			attachFunc(new FuncTemplate(null, "Enchant", Stats.MAGIC_ATTACK, 12, 0.0));
			attachFunc(new FuncTemplate(null, "Enchant", Stats.SOULSHOT_POWER, 12, 0.0));
			attachFunc(new FuncTemplate(null, "Enchant", Stats.SPIRITSHOT_POWER, 12, 0.0));
		}
		else if(isArmor())
		{
			if(_exType == ExItemType.SHIELD)
				attachFunc(new FuncTemplate(null, "Enchant", Stats.SHIELD_DEFENCE, 12, 0.0));
			else
				attachFunc(new FuncTemplate(null, "Enchant", Stats.POWER_DEFENCE, 12, 0.0));
		}
		else if(isAccessory())
			attachFunc(new FuncTemplate(null, "Enchant", Stats.MAGIC_DEFENCE, 12, 0.0));

		EnchantBonusItemType itemType;
		if(isWeapon())
			itemType = EnchantBonusItemType.WEAPON;
		else if(isArmor())
			itemType = EnchantBonusItemType.ARMOR;
		else
			return;

		EnchantBonusHolder.getInstance().getBonusStats(itemType, getGrade()).forEach(stats -> {
			attachFunc(new FuncTemplate(null, "Enchant", stats, 128, 0.0));
		});
	}

	public final int getItemId()
	{
		return _itemId;
	}

	public final String getName()
	{
		return _name;
	}

	public final String getName(Player player)
	{
		String name = ItemNameHolder.getInstance().getItemName(player, getItemId());
		return name == null ? _name : name;
	}

	public final String getAdditionalName()
	{
		return _addname;
	}

	public final String getIcon()
	{
		return _icon;
	}

	public final int getWeight()
	{
		return _weight;
	}

	public final int getReferencePrice()
	{
		return _referencePrice;
	}

	public final int getDurability()
	{
		return _durability;
	}

	public final boolean isTemporal()
	{
		return _temporal;
	}

	public ItemType getItemType()
	{
		return _type;
	}

	public final int getType1()
	{
		return _type1;
	}

	public final int getType2()
	{
		return _type2;
	}

	public final ItemGrade getGrade()
	{
		return _grade;
	}

	public abstract long getItemMask();

	public int getBaseAttributeValue(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return _baseAttributes[element.getId()];
	}

	public final void setBaseAtributeElements(int[] val)
	{
		_baseAttributes = val;
	}

	public int getBaseEnchantLevel()
	{
		return _baseEnchantLevel;
	}

	public boolean isCrystallizable()
	{
		return !Config.DISABLE_CRYSTALIZATION_ITEMS && isDestroyable() && getGrade() != ItemGrade.NONE && getCrystalCount() > 0;
	}

	public int getCrystalCount()
	{
		return _crystalCount;
	}

	public final int getBodyPart()
	{
		return _bodyPart;
	}

	public boolean isStackable()
	{
		return _stackable;
	}

	public boolean isForHatchling()
	{
		return _petType == 2;
	}

	public boolean isForStrider()
	{
		return _petType == 3;
	}

	public boolean isForWolf()
	{
		return _petType == 1;
	}

	public boolean isForPetBaby()
	{
		return _petType == 6;
	}

	public boolean isForGWolf()
	{
		return _petType == 4;
	}

	public boolean isPendant()
	{
		return _petType == 5;
	}

	public boolean isForPet()
	{
		return getExType() == ExItemType.PET_EQUIPMENT;
	}

	public void attachSkill(SkillEntry skill)
	{
		_skills = (SkillEntry[]) ArrayUtils.add((Object[]) _skills, skill);
	}

	public SkillEntry[] getAttachedSkills()
	{
		return _skills;
	}

	public SkillEntry getFirstSkill()
	{
		if(_skills.length > 0)
			return _skills[0];
		return null;
	}

	public SkillEntry getEnchant4Skill()
	{
		return _enchant4Skill;
	}

	public boolean isSealedItem()
	{
		return _name.startsWith("Sealed");
	}

	@Override
	public String toString()
	{
		return _itemId + " " + _name;
	}

	public boolean isShadowItem()
	{
		return _durability > 0 && !isTemporal();
	}

	public final boolean isCommonItem()
	{
		return _quality == ItemQuality.COMMON;
	}

	public final boolean isAltSeed()
	{
		return _type == EtcItemTemplate.EtcItemType.ALT_SEED;
	}

	public final boolean isAdena()
	{
		return _itemId == 57;
	}

	public final boolean isEquipment()
	{
		return _type1 != 4;
	}

	public final boolean isKeyMatherial()
	{
		return _type == EtcItemTemplate.EtcItemType.MATERIAL;
	}

	public final boolean isRecipe()
	{
		return _type == EtcItemTemplate.EtcItemType.RECIPE;
	}

	public final boolean isRune()
	{
		return _type == EtcItemTemplate.EtcItemType.RUNE || _type == EtcItemTemplate.EtcItemType.RUNE_SELECT;
	}

	public final boolean isTerritoryAccessory()
	{
		return _itemId >= 13740 && _itemId <= 13748 || _itemId >= 14592 && _itemId <= 14600 || _itemId >= 14664 && _itemId <= 14672 || _itemId >= 14801 && _itemId <= 14809 || _itemId >= 15282 && _itemId <= 15299;
	}

	public final boolean isArrow()
	{
		return _type == EtcItemTemplate.EtcItemType.ARROW;
	}

	public final boolean isQuiver()
	{
		return _type == EtcItemTemplate.EtcItemType.ARROW_QUIVER;
	}

	public final boolean isBracelet()
	{
		return _bodyPart == 1048576 || _bodyPart == 2097152;
	}

	public final boolean isHerb()
	{
		return _type == EtcItemTemplate.EtcItemType.HERB;
	}

	public final boolean isLifeStone()
	{
		return _type == EtcItemTemplate.EtcItemType.LIFE_STONE;
	}

	public final boolean isAccessoryLifeStone()
	{
		return _type == EtcItemTemplate.EtcItemType.ACC_LIFE_STONE;
	}

	public boolean isCrystall()
	{
		return _itemId == 1458 || _itemId == 1459 || _itemId == 1460 || _itemId == 1461 || _itemId == 1462 || _itemId == 17371;
	}

	public boolean isWeapon()
	{
		return getType2() == 0;
	}

	public boolean isArmor()
	{
		return getType2() == 1;
	}

	public boolean isAccessory()
	{
		return getType2() == 2;
	}

	public boolean isOther()
	{
		return getType2() == 5;
	}

	public boolean isQuest()
	{
		return getType2() == 3;
	}

	public boolean isJewelry()
	{
		return getExType() == ExItemType.RING || getExType() == ExItemType.EARRING || getExType() == ExItemType.NECKLACE;
	}

    public boolean isEnchantScroll()
    {
        return getExType() == ExItemType.SCROLL_ENCHANT_ARMOR || getExType() == ExItemType.SCROLL_ENCHANT_WEAPON;
    }

	public boolean isHairAccessory()
	{
		return getExType() == ExItemType.HAIR_ACCESSORY;
	}

	public boolean canBeEnchanted()
	{
		return isEnchantable();
	}

	public boolean isEquipable()
	{
		return getBodyPart() > 0 && getHandler() instanceof EquipableItemHandler;
	}

	public void setEnchant4Skill(SkillEntry enchant4Skill)
	{
		_enchant4Skill = enchant4Skill;
	}

    public boolean testCondition(Playable player, ItemInstance instance)
    {
        return testCondition(player, instance, true);
    }

	public boolean testCondition(Playable playable, ItemInstance instance, boolean sendMsg)
	{
		if(_conditions.isEmpty())
			return true;

		for(Condition condition : _conditions)
			if(!condition.test(playable, null, null, instance, 0))
			{
				if(sendMsg && playable.isPlayer()) {
					if(condition.getSystemMsg() != null) {
						if(condition.getSystemMsg().size() > 0)
							playable.sendPacket(new SystemMessagePacket(condition.getSystemMsg()).addItemName(getItemId()));
						else
							playable.sendPacket(condition.getSystemMsg());
					}
					else if(condition.isCustomMessageLink()) {
						playable.sendMessage(new CustomMessage(condition.getCustomMessageLink()));
					}
				}
				return false;
			}
		return true;
	}

	public boolean isBlocked(Playable playable, ItemInstance instance)
	{
		if(_conditions.isEmpty())
			return false;

		for(Condition condition : _conditions)
			if(!condition.test(playable, null, null, instance, 0) && (condition.getSystemMsg() == null && !condition.isCustomMessageLink()))
				return true;
		return false;
	}

	public void addCondition(Condition condition)
	{
		_conditions.add(condition);
	}

	public boolean isEnchantable()
	{
		return hasFlag(ItemFlags.ENCHANTABLE);
	}

	public final boolean isTradeable()
	{
		return hasFlag(ItemFlags.TRADEABLE);
	}

	public final boolean isPrivatestoreable()
	{
		return hasFlag(ItemFlags.PRIVATESTOREABLE);
	}

	public final boolean isDestroyable()
	{
		return hasFlag(ItemFlags.DESTROYABLE);
	}

	public final boolean isDropable()
	{
		return hasFlag(ItemFlags.DROPABLE);
	}

	public final boolean isSellable()
	{
		return hasFlag(ItemFlags.SELLABLE);
	}

	public final boolean isStoreable()
	{
		return hasFlag(ItemFlags.STOREABLE);
	}

	public final boolean isFreightable()
	{
		return hasFlag(ItemFlags.FREIGHTABLE);
	}

	public boolean hasFlag(ItemFlags f)
	{
		return (_flags & f.mask()) == f.mask();
	}

	private void activeFlag(ItemFlags f)
	{
		_flags |= f.mask();
	}

	public IItemHandler getHandler()
	{
		return null;
	}

	public int getReuseDelay()
	{
		return _reuseDelay;
	}

	public int getReuseGroup()
	{
		return _reuseGroup;
	}

	public int getDisplayReuseGroup()
	{
		return _reuseGroup < 0 ? -1 : _reuseGroup;
	}

	public void addEnchantOptions(int level, int[] options)
	{
		if(_enchantOptions.isEmpty())
			_enchantOptions = new HashIntObjectMap<>();
		_enchantOptions.put(level, options);
	}

	public IntObjectMap<int[]> getEnchantOptions()
	{
		return _enchantOptions;
	}

	public void addAugmentationInfo(AugmentationInfo augmentationInfo)
	{
		if(_augmentationInfos.isEmpty())
			_augmentationInfos = new HashIntObjectMap<AugmentationInfo>();

		_augmentationInfos.put(augmentationInfo.getMineralId(), augmentationInfo);
	}

	public IntObjectMap<AugmentationInfo> getAugmentationInfos()
	{
		return _augmentationInfos;
	}

	public ItemReuseType getReuseType()
	{
		return _reuseType;
	}

	public boolean isMagicWeapon()
	{
		return false;
	}

	public final List<CapsuledItemData> getCapsuledItems()
	{
		return _capsuledItems;
	}

	public void addCapsuledItem(CapsuledItemData ci)
	{
		_capsuledItems.add(ci);
	}

	public ItemQuality getQuality()
	{
		return _quality;
	}

	public boolean isPvP()
	{
		return _isPvP;
	}

	public ExItemType getExType()
	{
		return _exType;
	}

	public long getPriceLimitForItem()
	{
		return _priceLimit;
	}

	public WeaponFightType getWeaponFightType()
	{
		return WeaponFightType.WARRIOR;
	}

	@Override
	public void attachFunc(FuncTemplate f)
	{
		if(isForPet())
		{
			super.attachFunc(f);
			return;
		}
		if(isWeapon())
		{
			if(f._stat == Stats.POWER_ATTACK && f._func == FuncAdd.class && f._order == 16)
			{
				_pAtk = (int) f._value;
				return;
			}
			if(f._stat == Stats.MAGIC_ATTACK && f._func == FuncAdd.class && f._order == 16)
			{
				_mAtk = (int) f._value;
				return;
			}
		}
		else if(isArmor())
			switch(_exType)
			{
				case HELMET:
				case UPPER_PIECE:
				case LOWER_PIECE:
				case FULL_BODY:
				case GLOVES:
				case FEET:
				case PENDANT:
				case CLOAK:
				{
					if(f._stat == Stats.POWER_DEFENCE && f._func == FuncAdd.class && f._order == 16)
					{
						_pDef = (int) f._value;
						return;
					}
					break;
				}
			}
		else if(isAccessory())
			switch(_exType)
			{
				case RING:
				case EARRING:
				case NECKLACE:
				{
					if(f._stat == Stats.MAGIC_DEFENCE && f._func == FuncAdd.class && f._order == 16)
					{
						_mDef = (int) f._value;
						return;
					}
					break;
				}
			}
		super.attachFunc(f);
	}

	public final int getPAtk()
	{
		return _pAtk;
	}

	public final int getMAtk()
	{
		return _mAtk;
	}

	public final int getPDef()
	{
		return _pDef;
	}

	public final int getMDef()
	{
		return _mDef;
	}

	public boolean isHeroCloak()
	{
		return _itemId == 30372;
	}

	public boolean isHeroWeapon()
	{
		return _itemId >= 6611 && _itemId <= 6621 || _itemId >= 9388 && _itemId <= 9390 || _itemId >= 30392 && _itemId <= 30405;
	}

	public final boolean isCloak()
	{
		return _bodyPart == 8192;
	}

	public final boolean isTalisman()
	{
		return _bodyPart == 4194304;
	}

	public boolean canBeEnsoul(int ensoulId)
	{
		return false;
	}

	public final boolean isEnsoulable()
	{
		return hasFlag(ItemFlags.ENSOULABLE);
	}

	public List<VisualChange> getVisualChanges() {
		return visualChanges;
	}

	public void setVisualChanges(List<VisualChange> visualChanges) {
		this.visualChanges = visualChanges;
	}

	public enum ItemClass
	{
		ALL,
		WEAPON,
		ARMOR,
		JEWELRY,
		ACCESSORY,
		/** Soul/Spiritshot, Potions, Scrolls */
		CONSUMABLE,
		/** Common craft matherials */
		MATHERIALS,
		/** Special (item specific) craft matherials */
		PIECES,
		/** Crafting recipies */
		RECIPIES,
		/** Skill learn books */
		SPELLBOOKS,
		/** Dyes, lifestones */
		MISC,
		/** All other */
		OTHER
	}
}
