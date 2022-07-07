package l2s.gameserver.model.items;

import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.dao.ItemsEnsoulDAO;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.PetDataHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.attachment.ItemAttachment;
import l2s.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2s.gameserver.network.l2.s2c.DropItemPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SpawnItemPacket;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.item.ExItemType;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemType;
import l2s.gameserver.templates.item.support.Ensoul;
import l2s.gameserver.templates.item.support.VisualChange;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public final class ItemInstance extends GameObject implements JdbcEntity
{
	public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];

	public static final int[] EMPTY_AUGMENTATIONS = new int[2];

    public static final Ensoul[] EMPTY_ENSOULS_ARRAY = new Ensoul[0];

	/** Запрет на выбрасывание вещи */
	public static final int FLAG_NO_DROP = 1 << 0;
	/** Запрет продажи и обмена */
	public static final int FLAG_NO_TRADE = 1 << 1;
	/** Запрет передачи на склад **/
	public static final int FLAG_NO_STORE = 1 << 2;
	/** Запрет кристаллизации **/
	public static final int FLAG_NO_CRYSTALLIZE = 1 << 3;
	/** Запрет заточки **/
	public static final int FLAG_NO_ENCHANT = 1 << 4;
	/** Запрет уничтожения **/
	public static final int FLAG_NO_DESTROY = 1 << 5;
	/** Запрет передачи между персонажами на аккаунте **/
	public static final int FLAG_NO_FREIGHT = 1 << 6;
	/** Имеет время жизни **/
	public static final int FLAG_LIFE_TIME = 1 << 10;

    private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();

	private int ownerId;
	private int itemId;
	private long count;
	private int enchantLevel;
	private ItemLocation loc;
	private int locData;
	private int customType1;
	private int customType2;
	private int lifeTime;
	private int customFlags;
	private ItemAttributes attrs;
	private int[] _enchantOptions;
	private int _augmentationMineralId;
	private int[] _augmentations = EMPTY_AUGMENTATIONS;
	private ItemTemplate template;
	private boolean isEquipped;
	private long _dropTime;
	private Set<Integer> _dropPlayers;
	private long _dropTimeOwner;
	private double _chargedSoulshotPower;
	private double _chargedSpiritshotPower;
	private double _chargedFishshotPower;
	private ItemAttachment _attachment;
	private JdbcEntityState _state;
	private ScheduledFuture<?> _timerTask;

	private Map<Integer, Ensoul> _normalEnsouls;
	private Map<Integer, Ensoul> _specialEnsouls;

	private int visualId;
	private int visualItemObjId;

	public ItemInstance(int objectId)
	{
		super(objectId);
		enchantLevel = -1;
		attrs = new ItemAttributes();
		_enchantOptions = EMPTY_ENCHANT_OPTIONS;
		_dropPlayers = Collections.emptySet();
		_chargedSoulshotPower = 0.0;
		_chargedSpiritshotPower = 0.0;
		_state = JdbcEntityState.CREATED;
		ItemsEnsoulDAO.getInstance().restore(this);
	}

	public ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		enchantLevel = -1;
		attrs = new ItemAttributes();
		_enchantOptions = EMPTY_ENCHANT_OPTIONS;
		_dropPlayers = Collections.emptySet();
		_chargedSoulshotPower = 0.0;
		_chargedSpiritshotPower = 0.0;
		_state = JdbcEntityState.CREATED;
		setItemId(itemId);
		setLifeTime(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + getTemplate().getDurability() * 60 : getTemplate().getDurability());
		setLocData(-1);
		setEnchantLevel(getTemplate().getBaseEnchantLevel());
	}

	public int getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}

	public int getItemId()
	{
		return itemId;
	}

	public void setItemId(int id)
	{
		itemId = id;
		template = ItemHolder.getInstance().getTemplate(id);
		setCustomFlags(getCustomFlags());
	}

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		if(count < 0L)
			count = 0L;

		if(!isStackable() && count > 1L)
		{
			this.count = 1L;
			return;
		}

		this.count = count;
	}

	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	public int getFixedEnchantLevel(Player owner)
	{
		return enchantLevel;
	}

	public void setEnchantLevel(int enchantLevel)
	{
		int old = this.enchantLevel;
		this.enchantLevel = Math.max(getTemplate().getBaseEnchantLevel(), enchantLevel);
		if(old != this.enchantLevel && !getTemplate().getEnchantOptions().isEmpty())
		{
			Player player = GameObjectsStorage.getPlayer(ownerId);
			if(isEquipped() && player != null)
				ItemEnchantOptionsListener.getInstance().onUnequip(getEquipSlot(), this, player);
			int[] enchantOptions = getTemplate().getEnchantOptions().get(this.enchantLevel);
			_enchantOptions = enchantOptions == null ? EMPTY_ENCHANT_OPTIONS : enchantOptions;
			if(isEquipped() && player != null)
				ItemEnchantOptionsListener.getInstance().onEquip(getEquipSlot(), this, player);
		}
	}

	public boolean isAugmented()
	{
		return _augmentationMineralId != 0;
	}

	public void setAugmentation(int mineralId, int[] augmentations)
	{
		_augmentationMineralId = mineralId;
		_augmentations = augmentations;
	}

	public int[] getAugmentations()
	{
		return _augmentations;
	}

	public void setLocName(String loc)
	{
		this.loc = ItemLocation.valueOf(loc);
	}

	public String getLocName()
	{
		return loc.name();
	}

	public void setLocation(ItemLocation loc)
	{
		this.loc = loc;
	}

	public ItemLocation getLocation()
	{
		return loc;
	}

	public void setLocData(int locData)
	{
		this.locData = locData;
	}

	public int getLocData()
	{
		return locData;
	}

	public int getCustomType1()
	{
		return customType1;
	}

	public void setCustomType1(int newtype)
	{
		customType1 = newtype;
	}

	public int getCustomType2()
	{
		return customType2;
	}

	public void setCustomType2(int newtype)
	{
		customType2 = newtype;
	}

	public int getLifeTime()
	{
		return lifeTime;
	}

	public void setLifeTime(int lifeTime)
	{
		this.lifeTime = Math.max(0, lifeTime);
	}

	public int getCustomFlags()
	{
		return customFlags;
	}

	public void setCustomFlags(int flags)
	{
		customFlags = flags;
	}

	public ItemAttributes getAttributes()
	{
		return attrs;
	}

	public void setAttributes(ItemAttributes attrs)
	{
		this.attrs = attrs;
	}

	public int getShadowLifeTime()
	{
		if(!isShadowItem())
			return -1;
		return getLifeTime();
	}

	public int getTemporalLifeTime()
	{
		if(isTemporalItem() || isFlagLifeTime())
			return Math.toIntExact(getLifeTime() - (System.currentTimeMillis() / 1000L));
		return -9999;
	}

	public void startTimer(Runnable r)
	{
		_timerTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0L, 60000L);
	}

	public void stopTimer()
	{
		if(_timerTask != null)
		{
			_timerTask.cancel(false);
			_timerTask = null;
		}
	}

	public boolean isEquipable()
	{
		return template.isEquipable();
	}

	public boolean isEquipped()
	{
		return isEquipped;
	}

	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}

	public int getBodyPart()
	{
		return template.getBodyPart();
	}

	public int getEquipSlot()
	{
		return getLocData();
	}

	public ItemTemplate getTemplate()
	{
		return template;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public long getLastDropTime()
	{
		return _dropTime;
	}

	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}

	public ItemType getItemType()
	{
		return template.getItemType();
	}

	public boolean isArmor()
	{
		return template.isArmor();
	}

	public boolean isAccessory()
	{
		return template.isAccessory();
	}

	public boolean isOther()
	{
		return template.isOther();
	}

	public boolean isWeapon()
	{
		return template.isWeapon();
	}

	public int getReferencePrice()
	{
		return template.getReferencePrice();
	}

	public boolean isStackable()
	{
		return template.isStackable();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, ItemInstance.class, this, true))
			return;
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}

	public Func[] getStatFuncs()
	{
		Func[] result = Func.EMPTY_FUNC_ARRAY;
		List<Func> funcs = new ArrayList<>();
		if(template.getAttachedFuncs().length > 0)
			for(FuncTemplate t : template.getAttachedFuncs())
			{
				Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}
		for(Element e : Element.VALUES)
		{
			if(isWeapon())
				funcs.add(new FuncAttack(e, 64, this));
			if(isArmor())
				funcs.add(new FuncDefence(e, 64, this));
		}
		if(!funcs.isEmpty())
			result = (Func[]) funcs.toArray((Object[]) new Func[funcs.size()]);

		return result;
	}

	/**
	 * Return true if item can be destroyed
	 */
	public boolean canBeDestroyed(Player player)
	{
		if(player.isGM())
			return true;

		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(PetDataHolder.getInstance().isControlItem(getItemId()) && player.isMounted())
			return false;

		if(player.getPetControlItem() == this)
			return false;

		if(player.getMountControlItemObjId() == getObjectId())
			return false;

		if(player.getEnchantScroll() == this)
			return false;

		return template.isDestroyable();
	}

	/**
	 * Return true if item can be dropped
	 */
	public boolean canBeDropped(Player player, boolean pk)
	{
		if(player.isGM())
			return true;

		if((customFlags & FLAG_NO_DROP) == FLAG_NO_DROP)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && (!pk || !Config.DROP_ITEMS_AUGMENTED))
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if (HidenItemsDAO.isHidden(this))
			return false;

		return template.isDropable();
	}

	public boolean canBeTraded(Player player)
	{
		if(player.isGM())
			return true;

		if((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented())
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if (HidenItemsDAO.isHidden(this))
			return false;

		return template.isTradeable();
	}

	public boolean canBePrivateStore(Player player)
	{
		return getItemId() != 57 && canBeTraded(player) && template.isPrivatestoreable();
	}

	/**
	 * Можно ли продать в магазин NPC
	 */
	public boolean canBeSold(Player player)
	{
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
			return false;

		if(getItemId() == ItemTemplate.ITEM_ID_ADENA)
			return false;

		if(template.getReferencePrice() == 0)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented())
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if (HidenItemsDAO.isHidden(this))
			return false;

		return template.isSellable();
	}

	/**
	 * Можно ли положить на склад (privatewh - личный склад, или клановый)
	 */
	public boolean canBeStored(Player player, boolean privatewh)
	{
		if((customFlags & FLAG_NO_STORE) == FLAG_NO_STORE)
			return false;

		if(!template.isStoreable())
			return false;

		if(!privatewh && isShadowItem())
			return false;

		if(!privatewh && isTemporalItem())
			return false;

		if(!privatewh && isAugmented())
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if (HidenItemsDAO.isHidden(this))
			return false;

		return privatewh || template.isTradeable();
	}

	/**
	 * Можно ли передать на другого персонажа на аккаунте
	 */
	public boolean canBeFreighted(Player player)
	{
		if((customFlags & FLAG_NO_FREIGHT) == FLAG_NO_FREIGHT)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented())
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(HidenItemsDAO.isHidden(this))
			return false;

		return template.isFreightable();
	}

	public boolean canBeCrystallized(Player player)
	{
		if((customFlags & FLAG_NO_CRYSTALLIZE) == FLAG_NO_CRYSTALLIZE)
			return false;

		if(isStackable())
			return false;

		if(getGrade() == ItemGrade.NONE || template.getCrystalCount() == 0)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		return template.isCrystallizable();
	}

	public boolean canBeEnchanted()
	{
		if((customFlags & FLAG_NO_ENCHANT) == FLAG_NO_ENCHANT)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;

		return template.canBeEnchanted();
	}

	public boolean canBeExchanged(Player player)
	{
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(HidenItemsDAO.isHidden(this))
			return false;

		return template.isDestroyable();
	}

	public boolean isShadowItem()
	{
		return template.isShadowItem();
	}

	public boolean isTemporalItem()
	{
		return template.isTemporal();
	}

	public boolean isCommonItem()
	{
		return template.isCommonItem();
	}

	public boolean isAltSeed()
	{
		return template.isAltSeed();
	}

	public boolean isHiddenItem()
	{
		return HidenItemsDAO.isHidden(this);
	}

	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc)
	{
		Creature dropper = fromNpc;
		if(dropper == null)
			dropper = lastAttacker;
		Location pos = Location.findAroundPosition(dropper, 100);
		if(lastAttacker != null)
		{
			_dropPlayers = new HashSet<>(1, 2.0f);
			for(Player member : lastAttacker.getPlayerGroup())
				_dropPlayers.add(member.getObjectId());
			_dropTimeOwner = System.currentTimeMillis() + Config.NONOWNER_ITEM_PICKUP_DELAY + (fromNpc != null && fromNpc.isRaid() ? 15000 : 0);
		}
		dropMe(dropper, pos);
	}

	public void dropToTheGround(Collection<Player> dropPlayers, NpcInstance fromNpc)
	{
		Location pos = Location.findAroundPosition(fromNpc, 100);
		if(!dropPlayers.isEmpty())
		{
			_dropPlayers = dropPlayers.stream().map(Creature::getObjectId).collect(Collectors.toSet());
			_dropTimeOwner = System.currentTimeMillis() + Config.NONOWNER_ITEM_PICKUP_DELAY + (fromNpc != null && fromNpc.isRaid() ? 15000 : 0);
		}
		dropMe(fromNpc, pos);
	}

	public void dropToTheGround(Creature dropper, Location dropPos)
	{
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
	}

	public void dropToTheGround(Playable dropper, Location dropPos)
	{
		setLocation(ItemLocation.VOID);
		if(getJdbcState().isPersisted())
		{
			setJdbcState(JdbcEntityState.UPDATED);
			update();
		}
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
	}

	public void dropMe(Creature dropper, Location loc)
	{
		if(dropper != null)
            setReflection(dropper.getReflection());
		spawnMe0(loc, dropper);
		if(dropper != null && dropper.isPlayable())
		{
			if(Config.AUTODESTROY_PLAYER_ITEM_AFTER > 0)
				ItemsAutoDestroy.getInstance().addPlayerItem(this);
		}
		else if(isHerb())
			ItemsAutoDestroy.getInstance().addHerb(this);
		else if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance().addItem(this);
	}

	public final void pickupMe()
	{
		decayMe();
        setReflection(ReflectionManager.MAIN);
	}

	private int getDefence(Element element)
	{
		return isArmor() ? getAttributeElementValue(element, true) : 0;
	}

	public int getDefenceFire()
	{
		return getDefence(Element.FIRE);
	}

	public int getDefenceWater()
	{
		return getDefence(Element.WATER);
	}

	public int getDefenceWind()
	{
		return getDefence(Element.WIND);
	}

	public int getDefenceEarth()
	{
		return getDefence(Element.EARTH);
	}

	public int getDefenceHoly()
	{
		return getDefence(Element.HOLY);
	}

	public int getDefenceUnholy()
	{
		return getDefence(Element.UNHOLY);
	}

	public int getAttributeElementValue(Element element, boolean withBase)
	{
		return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
	}

	public Element getAttributeElement()
	{
		return attrs.getElement();
	}

	public int getAttributeElementValue()
	{
		return attrs.getValue();
	}

	public Element getAttackElement()
	{
		Element element = isWeapon() ? getAttributeElement() : Element.NONE;
		if(element == Element.NONE)
			for(Element e : Element.VALUES)
				if(template.getBaseAttributeValue(e) > 0)
					return e;
		return element;
	}

	public int getAttackElementValue()
	{
		return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
	}

	public void setAttributeElement(Element element, int value)
	{
		attrs.setValue(element, value);
	}

	public boolean isHerb()
	{
		return getTemplate().isHerb();
	}

	public long getPriceLimitForItem()
	{
		return getTemplate().getPriceLimitForItem();
	}

	public ItemGrade getGrade()
	{
		return template.getGrade();
	}

	@Override
	public String getName()
	{
		return getTemplate().getName();
	}

	public String getName(Player player)
	{
		return getTemplate().getName(player);
	}

	@Override
	public void save()
	{
		_itemsDAO.save(this);
	}

	@Override
	public void update()
	{
		_itemsDAO.update(this);
	}

	@Override
	public void delete()
	{
		_itemsDAO.delete(this);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		L2GameServerPacket packet = null;
		if(dropper != null)
			packet = new DropItemPacket(dropper.getObjectId(), this);
		else
			packet = new SpawnItemPacket(this);
		return Collections.singletonList(packet);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getTemplate().getItemId());
		sb.append(" ");
		if(getEnchantLevel() > 0)
		{
			sb.append("+");
			sb.append(getEnchantLevel());
			sb.append(" ");
		}
		sb.append(getTemplate().getName());
		if(!getTemplate().getAdditionalName().isEmpty())
		{
			sb.append(" ");
			sb.append("\\").append(getTemplate().getAdditionalName()).append("\\");
		}
		sb.append(" ");
		sb.append("(");
		sb.append(getCount());
		sb.append(")");
		sb.append("[");
		sb.append(getObjectId());
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void setJdbcState(JdbcEntityState state)
	{
		_state = state;
	}

	@Override
	public JdbcEntityState getJdbcState()
	{
		return _state;
	}

	@Override
	public boolean isItem()
	{
		return true;
	}

	public ItemAttachment getAttachment()
	{
		return _attachment;
	}

	public void setAttachment(ItemAttachment attachment)
	{
		ItemAttachment old = _attachment;
		_attachment = attachment;
		if(_attachment != null)
			_attachment.setItem(this);
		if(old != null)
			old.setItem(null);
	}

	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}

	public int getAugmentationMineralId()
	{
		return _augmentationMineralId;
	}

	public Set<Integer> getDropPlayers()
	{
		return _dropPlayers;
	}

	public int getCrystalCountOnCrystallize()
	{
		int crystalsAdd = ItemFunctions.getCrystallizeCrystalAdd(this);
		return template.getCrystalCount() + crystalsAdd;
	}

	public int getCrystalCountOnEchant()
	{
		int defaultCrystalCount = template.getCrystalCount();
		if(defaultCrystalCount > 0)
		{
			int crystalsAdd = ItemFunctions.getCrystallizeCrystalAdd(this);
			return (int) Math.ceil(defaultCrystalCount / 2.0) + crystalsAdd;
		}
		return 0;
	}

	public ExItemType getExType()
	{
		return getTemplate().getExType();
	}

	public double getChargedSoulshotPower()
	{
		return _chargedSoulshotPower;
	}

	public void setChargedSoulshotPower(double val)
	{
		_chargedSoulshotPower = val;
	}

	public double getChargedSpiritshotPower()
	{
		return _chargedSpiritshotPower;
	}

	public void setChargedSpiritshotPower(double val)
	{
		_chargedSpiritshotPower = val;
	}

	public boolean isFlagLifeTime()
	{
		return (customFlags & FLAG_LIFE_TIME) == FLAG_LIFE_TIME;
	}

	public boolean isFlagNoCrystallize()
	{
		return (customFlags & FLAG_NO_CRYSTALLIZE) == FLAG_NO_CRYSTALLIZE;
	}

    public enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		PET_INVENTORY,
		PET_PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT,
		LEASE,
		MAIL
    }

	public class FuncAttack extends Func
	{
		private final Element element;

		public FuncAttack(Element element, int order, Object owner)
		{
			super(element.getAttack(), order, owner);
			this.element = element;
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value + getAttributeElementValue(element, true);
		}
	}

	public class FuncDefence extends Func
	{
		private final Element element;

		public FuncDefence(Element element, int order, Object owner)
		{
			super(element.getDefence(), order, owner);
			this.element = element;
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value + getAttributeElementValue(element, true);
		}
	}

	@Override
	public int getActingRange()
	{
		return 16;
	}

	public boolean isHeroItem()
	{
		return isHeroWeapon() || isHeroCloak();
	}

	private boolean isHeroCloak()
	{
		return template.isHeroCloak();
	}

	private boolean isHeroWeapon()
	{
		return template.isHeroWeapon();
	}

	public double getChargedFishshotPower()
	{
		return _chargedFishshotPower;
	}

	public void setChargedFishshotPower(double val)
	{
		_chargedFishshotPower = val;
	}

	public boolean canBeEnsoul(int ensoulId)
	{
		if(isHeroItem())
			return false;
		if(isShadowItem())
			return false;
		if(isTemporalItem())
			return false;
		if(isCommonItem())
			return false;
		return template.canBeEnsoul(ensoulId);
	}

	public boolean containsEnsoul(int type, int id)
	{
		if(type == 1)
		{
			if(_normalEnsouls == null)
				return false;
			return _normalEnsouls.containsKey(id);
		}
		if(type == 2)
		{
			if(_specialEnsouls == null)
				return false;
			return _specialEnsouls.containsKey(id);
		}
		return false;
	}

	public Ensoul getEnsoul(int type, int id)
	{
		if(type == 1 && _normalEnsouls != null)
			return _normalEnsouls.get(id);
        if(type == 2 && _specialEnsouls != null)
            return _specialEnsouls.get(id);
        return null;
	}

	public void addEnsoul(int type, int id, Ensoul ensoul, boolean store)
	{
		if(type == 1)
		{
			if(_normalEnsouls == null)
				_normalEnsouls = new TreeMap<>();

			_normalEnsouls.put(id, ensoul);
		}
		else if(type == 2)
		{
			if(_specialEnsouls == null)
				_specialEnsouls = new TreeMap<>();

			_specialEnsouls.put(id, ensoul);
		}
		else
		{
			return;
		}

		if(store)
		{
			ItemsEnsoulDAO.getInstance().insert(getObjectId(), type, id, ensoul.getId());
		}
	}

    public void removeEnsoul(int type, int id, boolean store)
    {
        if(type == 1)
        {
            if(_normalEnsouls != null && _normalEnsouls.remove(id) == null)
                return;
        }
        else if(type == 2)
        {
            if(_specialEnsouls != null && _specialEnsouls.remove(id) == null)
                return;
        }
        else
            return;

        if(store)
            ItemsEnsoulDAO.getInstance().delete(getObjectId(), type, id);
    }

    public Ensoul[] getNormalEnsouls()
	{
		if(_normalEnsouls == null)
			return EMPTY_ENSOULS_ARRAY;

		return _normalEnsouls.values().toArray(EMPTY_ENSOULS_ARRAY);
	}

	public Ensoul[] getSpecialEnsouls()
	{
		if(_specialEnsouls == null)
			return EMPTY_ENSOULS_ARRAY;

		return _specialEnsouls.values().toArray(EMPTY_ENSOULS_ARRAY);
	}

	public int getVisualId() {
		return visualId;
	}

	public void setVisualId(int visualId) {
		this.visualId = visualId;
	}

	public int getVisualItemObjId() {
		return visualItemObjId;
	}

	public void setVisualItemObjId(int visualItemId) {
		this.visualItemObjId = visualItemId;
	}
}
