package l2s.gameserver.model.items;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.listeners.StatsListener;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public abstract class Inventory extends ItemContainer
{
	private static final Logger _log = LoggerFactory.getLogger(Inventory.class);

	public static final int PAPERDOLL_PENDANT = 0;
	public static final int PAPERDOLL_REAR = 1;
	public static final int PAPERDOLL_LEAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_RFINGER = 4;
	public static final int PAPERDOLL_LFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	public static final int PAPERDOLL_HAIR = 15;
	public static final int PAPERDOLL_DHAIR = 16;
	public static final int PAPERDOLL_RBRACELET = 17;
	public static final int PAPERDOLL_LBRACELET = 18;
	public static final int PAPERDOLL_DECO1 = 19;
	public static final int PAPERDOLL_DECO2 = 20;
	public static final int PAPERDOLL_DECO3 = 21;
	public static final int PAPERDOLL_DECO4 = 22;
	public static final int PAPERDOLL_DECO5 = 23;
	public static final int PAPERDOLL_DECO6 = 24;
	public static final int PAPERDOLL_BELT = 25;
	public static final int PAPERDOLL_BROOCH = 26;
	public static final int PAPERDOLL_JEWEL1 = 27;
	public static final int PAPERDOLL_JEWEL2 = 28;
	public static final int PAPERDOLL_JEWEL3 = 29;
	public static final int PAPERDOLL_JEWEL4 = 30;
	public static final int PAPERDOLL_JEWEL5 = 31;
	public static final int PAPERDOLL_JEWEL6 = 32;
	public static final int PAPERDOLL_MAX = 33;
	public static final int[] PAPERDOLL_ORDER = {
			0,
			1,
			2,
			3,
			4,
			5,
			6,
			7,
			8,
			9,
			10,
			11,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			20,
			21,
			22,
			23,
			24,
			25,
			26,
			27,
			28,
			29,
			30,
			31,
			32 };
	protected final int _ownerId;
	protected final ItemInstance[] _paperdoll;
	protected final InventoryListenerList _listeners;
	protected int _totalWeight;
	protected long _wearedMask;

	protected Inventory(int ownerId)
	{
		_paperdoll = new ItemInstance[33];
		_listeners = new InventoryListenerList();
		_ownerId = ownerId;
		addListener(StatsListener.getInstance());
	}

	public abstract Playable getActor();

	protected abstract ItemInstance.ItemLocation getBaseLocation();

	protected abstract ItemInstance.ItemLocation getEquipLocation();

	public int getOwnerId()
	{
		return _ownerId;
	}

	protected void onRestoreItem(ItemInstance item)
	{
		_totalWeight += (int) (item.getTemplate().getWeight() * item.getCount());
		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null)
			handler.onRestoreItem(getActor(), item);
	}

	@Override
	protected void onAddItem(ItemInstance item)
	{
		item.setOwnerId(getOwnerId());
		item.setLocation(getBaseLocation());
		item.setLocData(findSlot(item.getTemplate().isQuest()));
		if(item.getJdbcState().isSavable())
			item.save();
		else
		{
			item.setJdbcState(JdbcEntityState.UPDATED);
			item.update();
		}
		sendAddItem(item);
		refreshWeight();
		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null)
			handler.onAddItem(getActor(), item);
	}

	@Override
	protected void onModifyItem(ItemInstance item)
	{
		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();
		sendModifyItem(item);
		refreshWeight();
	}

	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		if(item.isEquipped())
			unEquipItem(item);

		sendRemoveItem(item);

		item.setLocData(-1);
		item.setVisualItemObjId(0);
		item.setVisualId(0);

		refreshWeight();

		IItemHandler handler = item.getTemplate().getHandler();
		if(handler != null)
			handler.onRemoveItem(getActor(), item);
	}

	@Override
	protected void onDestroyItem(ItemInstance item)
	{
		item.setCount(0L);
		item.delete();
	}

	protected void onEquip(int slot, ItemInstance item)
	{
		item.setLocation(getEquipLocation());
		item.setLocData(slot);
		item.setEquipped(true);
		item.setJdbcState(JdbcEntityState.UPDATED);
		_listeners.onEquip(slot, item);
		_wearedMask |= item.getTemplate().getItemMask();
		sendEquipInfo(slot);
		sendModifyItem(item);
	}

	protected void onReequip(int slot, ItemInstance newItem, ItemInstance oldItem)
	{
		oldItem.setLocation(getBaseLocation());
		oldItem.setLocData(findSlot(oldItem.getTemplate().isQuest()));
		oldItem.setEquipped(false);
		oldItem.setJdbcState(JdbcEntityState.UPDATED);
		oldItem.setChargedSoulshotPower(0.0);
		oldItem.setChargedSpiritshotPower(0.0);
		oldItem.setChargedFishshotPower(0.0);
		_listeners.onUnequip(slot, oldItem);
		_wearedMask &= ~oldItem.getTemplate().getItemMask();
		newItem.setLocation(getEquipLocation());
		newItem.setLocData(slot);
		newItem.setEquipped(true);
		newItem.setJdbcState(JdbcEntityState.UPDATED);
		_listeners.onEquip(slot, newItem);
		_wearedMask |= newItem.getTemplate().getItemMask();
		sendEquipInfo(slot);
		sendModifyItem(newItem, oldItem);
	}

	protected void onUnequip(int slot, ItemInstance item)
	{
		item.setLocation(getBaseLocation());
		item.setLocData(findSlot(item.getTemplate().isQuest()));
		item.setEquipped(false);
		item.setJdbcState(JdbcEntityState.UPDATED);
		item.setChargedSoulshotPower(0.0);
		item.setChargedSpiritshotPower(0.0);
		item.setChargedFishshotPower(0.0);
		_listeners.onUnequip(slot, item);
		_wearedMask &= ~item.getTemplate().getItemMask();
		sendEquipInfo(slot);
		sendModifyItem(item);
	}

	private int findSlot(boolean quest)
	{
        int slot = 0;
        Label_0004: while(slot < _items.size())
		{
			for(int i = 0; i < _items.size(); ++i)
			{
				ItemInstance item = _items.get(i);
				if(!item.isEquipped())
					if(quest || !item.getTemplate().isQuest())
						if(!quest || item.getTemplate().isQuest())
							if(item.getEquipSlot() == slot)
							{
								++slot;
								continue Label_0004;
							}
			}
			break;
		}
		return slot;
	}

	public ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public ItemInstance[] getPaperdollItems()
	{
		return _paperdoll;
	}

	public int getPaperdollItemId(int slot)
	{
		ItemInstance item = getPaperdollItem(slot);
		if(item != null)
			return item.getItemId();
		if(slot == 15)
		{
			item = _paperdoll[16];
			if(item != null)
				return item.getItemId();
		}
		return 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
			return item.getObjectId();
		if(slot == 15)
		{
			item = _paperdoll[16];
			if(item != null)
				return item.getObjectId();
		}
		return 0;
	}

	public void addListener(OnEquipListener listener)
	{
		_listeners.add(listener);
	}

	public void removeListener(OnEquipListener listener)
	{
		_listeners.remove(listener);
	}

	public ItemInstance setPaperdollItem(int slot, ItemInstance item)
	{
		writeLock();
		ItemInstance old;
		try
		{
			old = _paperdoll[slot];
			if(old != item)
				if(old != null && item != null)
					onReequip(slot, _paperdoll[slot] = item, old);
				else
				{
					if(old != null)
					{
						_paperdoll[slot] = null;
						onUnequip(slot, old);
					}
					if(item != null)
						onEquip(slot, _paperdoll[slot] = item);
				}
		}
		finally
		{
			writeUnlock();
		}
		return old;
	}

	public long getWearedMask()
	{
		return _wearedMask;
	}

	public void unEquipItem(ItemInstance item)
	{
		if(item.isEquipped())
            unEquipItemInBodySlot(item.getBodyPart(), item);
	}

	public void unEquipItemInBodySlot(int bodySlot)
	{
        unEquipItemInBodySlot(bodySlot, null);
	}

	private void unEquipItemInBodySlot(int bodySlot, ItemInstance item)
	{
		int pdollSlot = -1;
		switch(bodySlot)
		{
			case 8:
			{
				pdollSlot = 3;
				break;
			}
			case 4:
			{
				pdollSlot = 2;
				break;
			}
			case 2:
			{
				pdollSlot = 1;
				break;
			}
			case 6:
			{
				if(item == null)
					return;
				if(getPaperdollItem(2) == item)
					pdollSlot = 2;
				if(getPaperdollItem(1) == item)
				{
					pdollSlot = 1;
					break;
				}
				break;
			}
			case 32:
			{
				pdollSlot = 5;
				break;
			}
			case 16:
			{
				pdollSlot = 4;
				break;
			}
			case 48:
			{
				if(item == null)
					return;
				if(getPaperdollItem(5) == item)
					pdollSlot = 5;
				if(getPaperdollItem(4) == item)
				{
					pdollSlot = 4;
					break;
				}
				break;
			}
			case 65536:
			{
				pdollSlot = 15;
				break;
			}
			case 262144:
			{
				pdollSlot = 16;
				break;
			}
			case 524288:
			{
				setPaperdollItem(16, null);
				pdollSlot = 15;
				break;
			}
			case 64:
			{
				pdollSlot = 6;
				break;
			}
			case 128:
			{
				pdollSlot = 7;
				break;
			}
			case 256:
			{
				pdollSlot = 8;
				break;
			}
			case 512:
			{
				pdollSlot = 9;
				break;
			}
			case 2048:
			{
				pdollSlot = 11;
				break;
			}
			case 1024:
			case 32768:
			case 131072:
			{
				pdollSlot = 10;
				break;
			}
			case 4096:
			{
				pdollSlot = 12;
				break;
			}
			case 268435456:
			{
				pdollSlot = 25;
				break;
			}
			case 8192:
			{
				pdollSlot = 13;
				break;
			}
			case 16384:
			{
				setPaperdollItem(8, null);
				pdollSlot = 7;
				break;
			}
			case 1:
			{
				pdollSlot = 0;
				break;
			}
			case 2097152:
			{
				pdollSlot = 18;
				break;
			}
			case 1048576:
			{
				pdollSlot = 17;
				setPaperdollItem(19, null);
				setPaperdollItem(20, null);
				setPaperdollItem(21, null);
				setPaperdollItem(22, null);
				setPaperdollItem(23, null);
				setPaperdollItem(24, null);
				break;
			}
			case 4194304:
			{
				if(item == null)
					return;

				if(getPaperdollItem(19) == item)
				{
					pdollSlot = 19;
					break;
				}
				if(getPaperdollItem(20) == item)
				{
					pdollSlot = 20;
					break;
				}
				if(getPaperdollItem(21) == item)
				{
					pdollSlot = 21;
					break;
				}
				if(getPaperdollItem(22) == item)
				{
					pdollSlot = 22;
					break;
				}
				if(getPaperdollItem(23) == item)
				{
					pdollSlot = 23;
					break;
				}
				if(getPaperdollItem(24) != item)
					break;
				pdollSlot = 24;
				break;
			}
			case 536870912:
			{
				pdollSlot = 26;
                setPaperdollItem(27, null);
                setPaperdollItem(28, null);
                setPaperdollItem(29, null);
                setPaperdollItem(30, null);
                setPaperdollItem(31, null);
                setPaperdollItem(32, null);
				break;
			}
			case 1073741824:
			{
				if(item == null){ return; }
				if(getPaperdollItem(27) == item)
				{
					pdollSlot = 27;
					break;
				}
				if(getPaperdollItem(28) == item)
				{
					pdollSlot = 28;
					break;
				}
				if(getPaperdollItem(29) == item)
				{
					pdollSlot = 29;
					break;
				}
				if(getPaperdollItem(30) == item)
				{
					pdollSlot = 30;
					break;
				}
				if(getPaperdollItem(31) == item)
				{
					pdollSlot = 31;
					break;
				}
				if(getPaperdollItem(32) != item)
					break;
				pdollSlot = 32;
				break;
			}
			default:
			{
				_log.warn("Requested invalid body slot: " + bodySlot + ", Item: " + item + ", ownerId: '" + getOwnerId() + "'");
				return;
			}
		}
		if(pdollSlot >= 0)
			setPaperdollItem(pdollSlot, null);
	}

	public void equipItem(ItemInstance item)
	{
		int bodySlot = item.getBodyPart();
		double hp = getActor().getCurrentHp();
		double mp = getActor().getCurrentMp();
		double cp = getActor().getCurrentCp();
		switch(bodySlot)
		{
			case 16384:
			{
				setPaperdollItem(8, null);
				setPaperdollItem(7, item);
				break;
			}
			case 256:
			{
				ItemInstance rHandItem = getPaperdollItem(7);
				ItemTemplate rHandItemTemplate = rHandItem == null ? null : rHandItem.getTemplate();
				ItemTemplate newItem = item.getTemplate();
				if(newItem.getItemType() == EtcItemTemplate.EtcItemType.ARROW || newItem.getItemType() == EtcItemTemplate.EtcItemType.ARROW_QUIVER)
				{
					if(rHandItemTemplate == null)
						return;
					if(rHandItemTemplate.getItemType() != WeaponTemplate.WeaponType.BOW)
						return;
					if(rHandItemTemplate.getGrade().extOrdinal() != newItem.getGrade().extOrdinal())
						return;
				}
				else
				{
					if(newItem.getItemType() == EtcItemTemplate.EtcItemType.LURE)
					{
						if(rHandItemTemplate == null)
							return;

						if(rHandItemTemplate.getItemType() != WeaponTemplate.WeaponType.ROD)
							return;
					}
					else if(rHandItemTemplate != null && rHandItemTemplate.getBodyPart() == 16384)
						setPaperdollItem(7, null);
				}
				setPaperdollItem(8, item);
				break;
			}
			case 128:
			{
				setPaperdollItem(7, item);
				break;
			}
			case 2:
			case 4:
			case 6:
			{
				if(_paperdoll[2] == null)
				{
					setPaperdollItem(2, item);
					break;
				}
				if(_paperdoll[1] == null)
				{
					setPaperdollItem(1, item);
					break;
				}
				double lEarMDef = 0.0;
				FuncTemplate[] lEarFuncTemplates = _paperdoll[2].getTemplate().getAttachedFuncs();
				for(FuncTemplate func : lEarFuncTemplates)
					if(func._stat == Stats.MAGIC_DEFENCE)
					{
						lEarMDef = func._value;
						break;
					}
				double rEarMDef = 0.0;
				FuncTemplate[] rEarFuncTemplates = _paperdoll[1].getTemplate().getAttachedFuncs();
				for(FuncTemplate func2 : rEarFuncTemplates)
					if(func2._stat == Stats.MAGIC_DEFENCE)
					{
						rEarMDef = func2._value;
						break;
					}
				if(lEarMDef > rEarMDef)
					setPaperdollItem(1, item);
				else
					setPaperdollItem(2, item);
				break;
			}
			case 16:
			case 32:
			case 48:
			{
				if(_paperdoll[5] == null)
				{
					setPaperdollItem(5, item);
					break;
				}
				if(_paperdoll[4] == null)
				{
					setPaperdollItem(4, item);
					break;
				}
				double lFingerMDef = 0.0;
				FuncTemplate[] lFingerFuncTemplates = _paperdoll[5].getTemplate().getAttachedFuncs();
				for(FuncTemplate func : lFingerFuncTemplates)
					if(func._stat == Stats.MAGIC_DEFENCE)
					{
						lFingerMDef = func._value;
						break;
					}
				double rFingerMDef = 0.0;
				FuncTemplate[] rFingerFuncTemplates = _paperdoll[4].getTemplate().getAttachedFuncs();
				for(FuncTemplate func2 : rFingerFuncTemplates)
					if(func2._stat == Stats.MAGIC_DEFENCE)
					{
						rFingerMDef = func2._value;
						break;
					}
				if(lFingerMDef > rFingerMDef)
					setPaperdollItem(4, item);
				else
					setPaperdollItem(5, item);
				break;
			}
			case 8:
			{
				setPaperdollItem(3, item);
				break;
			}
			case 32768:
			{
				setPaperdollItem(11, null);
				setPaperdollItem(10, item);
				break;
			}
			case 1024:
			{
				setPaperdollItem(10, item);
				break;
			}
			case 2048:
			{
				ItemInstance chest = getPaperdollItem(10);
				if(chest != null && chest.getBodyPart() == 32768)
					setPaperdollItem(10, null);
				else if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(11, item);
				break;
			}
			case 4096:
			{
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(12, item);
				break;
			}
			case 268435456:
			{
				setPaperdollItem(25, item);
				break;
			}
			case 8192:
			{
				setPaperdollItem(13, item);
				break;
			}
			case 512:
			{
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(9, item);
				break;
			}
			case 64:
			{
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(6, item);
				break;
			}
			case 65536:
			{
				ItemInstance old = getPaperdollItem(16);
				if(old != null && old.getBodyPart() == 524288)
					setPaperdollItem(16, null);
				setPaperdollItem(15, item);
				break;
			}
			case ItemTemplate.SLOT_DHAIR:
			{
				ItemInstance slot2 = getPaperdollItem(16);
				if(slot2 != null && slot2.getBodyPart() == 524288)
					setPaperdollItem(15, null);
				setPaperdollItem(16, item);
				break;
			}
			case ItemTemplate.SLOT_HAIRALL:
			{
				setPaperdollItem(15, null);
				setPaperdollItem(16, item);
				break;
			}
			case 1:
			{
				setPaperdollItem(0, item);
				break;
			}
			case 1048576:
			{
				setPaperdollItem(17, item);
				break;
			}
			case 2097152:
			{
				setPaperdollItem(18, item);
				break;
			}
			case 131072:
			{
				setPaperdollItem(11, null);
				setPaperdollItem(6, null);
				setPaperdollItem(12, null);
				setPaperdollItem(9, null);
				setPaperdollItem(10, item);
				break;
			}
			case 4194304:
			{
				if(_paperdoll[19] == null)
				{
					setPaperdollItem(19, item);
					break;
				}
				if(_paperdoll[20] == null)
				{
					setPaperdollItem(20, item);
					break;
				}
				if(_paperdoll[21] == null)
				{
					setPaperdollItem(21, item);
					break;
				}
				if(_paperdoll[22] == null)
				{
					setPaperdollItem(22, item);
					break;
				}
				if(_paperdoll[23] == null)
				{
					setPaperdollItem(23, item);
					break;
				}
				if(_paperdoll[24] == null)
				{
					setPaperdollItem(24, item);
					break;
				}
				setPaperdollItem(19, item);
				break;
			}
			case 536870912:
			{
                setPaperdollItem(26, item);
				break;
			}
			case 1073741824:
			{
				if(_paperdoll[27] == null)
				{
                    setPaperdollItem(27, item);
					break;
				}
				if(_paperdoll[28] == null)
				{
                    setPaperdollItem(28, item);
					break;
				}
				if(_paperdoll[29] == null)
				{
                    setPaperdollItem(29, item);
					break;
				}
				if(_paperdoll[30] == null)
				{
                    setPaperdollItem(30, item);
					break;
				}
				if(_paperdoll[31] == null)
				{
                    setPaperdollItem(31, item);
					break;
				}
				if(_paperdoll[32] == null)
				{
                    setPaperdollItem(32, item);
					break;
				}
                setPaperdollItem(27, item);
				break;
			}
			default:
			{
				_log.warn("unknown body slot:" + bodySlot + " for item id: " + item.getItemId());
				return;
			}
		}
		getActor().setCurrentHp(hp, false);
		getActor().setCurrentMp(mp);
		getActor().setCurrentCp(cp);
		if(getActor().isPlayer())
			((Player) getActor()).autoShot();
	}

	public abstract void sendAddItem(ItemInstance p0);

	public abstract void sendModifyItem(ItemInstance... p0);

	public abstract void sendRemoveItem(ItemInstance p0);

	public void sendEquipInfo(int slot)
	{}

	protected void refreshWeight()
	{
        readLock();
        int weight = 0;
        try
		{
			for(int i = 0; i < _items.size(); ++i)
			{
				ItemInstance item = _items.get(i);
				weight += (int) (item.getTemplate().getWeight() * item.getCount());
			}
		}
		finally
		{
			readUnlock();
		}
		if(_totalWeight == weight)
			return;
		_totalWeight = weight;
		onRefreshWeight();
	}

	protected abstract void onRefreshWeight();

	public int getTotalWeight()
	{
		return _totalWeight;
	}

	public boolean validateCapacity(ItemInstance item)
	{
		long slots = 0L;
		if(!item.isStackable() || getItemByItemId(item.getItemId()) == null)
			++slots;
		return validateCapacity(slots);
	}

	public boolean validateCapacity(int itemId, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return validateCapacity(item, count);
	}

	public boolean validateCapacity(ItemTemplate item, long count)
	{
		long slots = 0L;
		if(!item.isStackable() || getItemByItemId(item.getItemId()) == null)
			slots = count;
		return validateCapacity(slots);
	}

	public boolean validateCapacity(long slots)
	{
		return slots == 0L || slots >= -2147483648L && slots <= 2147483647L && getSize() + (int) slots >= 0 && getSize() + slots <= getActor().getInventoryLimit();
	}

	public boolean validateWeight(ItemInstance item)
	{
		long weight = item.getTemplate().getWeight() * item.getCount();
		return validateWeight(weight);
	}

	public boolean validateWeight(int itemId, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return validateWeight(item, count);
	}

	public boolean validateWeight(ItemTemplate item, long count)
	{
		long weight = item.getWeight() * count;
		return validateWeight(weight);
	}

	public boolean validateWeight(long weight)
	{
		return weight == 0L || weight >= -2147483648L && weight <= 2147483647L && getTotalWeight() + (int) weight >= 0 && getTotalWeight() + weight <= getActor().getMaxLoad();
	}

	public abstract void restore();

	public abstract void store();

	public static int getPaperdollIndex(int slot)
	{
		switch(slot)
		{
			case 2:
			{
				return 1;
			}
			case 4:
			{
				return 2;
			}
			case 8:
			{
				return 3;
			}
			case 16:
			{
				return 4;
			}
			case 32:
			{
				return 5;
			}
			case 64:
			{
				return 6;
			}
			case 128:
			{
				return 7;
			}
			case 256:
			{
				return 8;
			}
			case 16384:
			{
				return 14;
			}
			case ItemTemplate.SLOT_GLOVES:
			{
				return 9;
			}
			case 1024:
			case 32768:
			case ItemTemplate.SLOT_FORMAL_WEAR:
			{
				return 10;
			}
			case 2048:
			{
				return 11;
			}
			case 4096:
			{
				return 12;
			}
			case 8192:
			{
				return 13;
			}
			case 0x10000:
			case 0x80000:
			{
				return 15;
			}
			case 0x40000:
			{
				return 16;
			}
			case 1:
			{
				return 0;
			}
			case 1048576:
			{
				return 17;
			}
			case 2097152:
			{
				return 18;
			}
			case 4194304:
			{
				return 19;
			}
			case 268435456:
			{
				return 25;
			}
			case 536870912:
			{
				return 26;
			}
			case 1073741824:
			{
				return 27;
			}
			default:
			{
				return -1;
			}
		}
	}

	@Override
	public int getSize()
	{
		return super.getSize() - getQuestSize();
	}

	public int getAllSize()
	{
		return super.getSize();
	}

	public int getQuestSize()
	{
		int size = 0;
		for(ItemInstance item : getItems())
			if(item.getTemplate().isQuest())
				++size;
		return size;
	}

	public class InventoryListenerList extends ListenerList<Playable>
	{
		public void onEquip(int slot, ItemInstance item)
		{
			for(Listener<Playable> listener : getListeners())
				((OnEquipListener) listener).onEquip(slot, item, getActor());
		}

		public void onUnequip(int slot, ItemInstance item)
		{
			for(Listener<Playable> listener : getListeners())
				((OnEquipListener) listener).onUnequip(slot, item, getActor());
		}
	}

	public static class ItemOrderComparator implements Comparator<ItemInstance>
	{
		private static final Comparator<ItemInstance> instance;

		public static final Comparator<ItemInstance> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(ItemInstance o1, ItemInstance o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o1.getLocData() - o2.getLocData();
		}

		static
		{
			instance = new ItemOrderComparator();
		}
	}
}
