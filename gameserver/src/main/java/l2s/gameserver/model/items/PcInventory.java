package l2s.gameserver.model.items;

import java.util.Collection;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.listeners.AccessoryListener;
import l2s.gameserver.model.items.listeners.ArmorSetListener;
import l2s.gameserver.model.items.listeners.BowListener;
import l2s.gameserver.model.items.listeners.GveRewardItemListener;
import l2s.gameserver.model.items.listeners.ItemAugmentationListener;
import l2s.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2s.gameserver.model.items.listeners.ItemSkillsListener;
import l2s.gameserver.model.items.listeners.RodListener;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.ExAdenaInvenCount;
import l2s.gameserver.network.l2.s2c.ExUserInfoEquipSlot;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.service.ItemService;
import l2s.gameserver.taskmanager.DelayedItemsManager;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcInventory extends Inventory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PcInventory.class);

	private final Player _owner;
	private LockType _lockType;
	private int[] _lockItems;
	public boolean isRefresh;

	public PcInventory(Player owner)
	{
		super(owner.getObjectId());
		_lockType = LockType.NONE;
		_lockItems = ArrayUtils.EMPTY_INT_ARRAY;
		isRefresh = false;
		_owner = owner;

		addListener(ItemSkillsListener.getInstance());
		addListener(ItemAugmentationListener.INSTANCE);
		addListener(ItemEnchantOptionsListener.getInstance());
		addListener(ArmorSetListener.getInstance());
		addListener(BowListener.getInstance());
		addListener(AccessoryListener.getInstance());
		addListener(RodListener.getInstance());
		addListener(GveRewardItemListener.getInstance());
		//addListener(CostumeVisualChangeListener.INSTANCE);
	}

	@Override
	public Player getActor()
	{
		return _owner;
	}

	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.INVENTORY;
	}

	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PAPERDOLL;
	}

	public ItemInstance addAdena(long amount)
	{
		return addItem(57, amount);
	}

	public boolean reduceAdena(long adena)
	{
		return destroyItemByItemId(57, adena);
	}

	/**
	 * Returns the visual id of the item in the paperdoll slot
	 * @param slot : int designating the slot
	 * @return int designating the ID of the item
	 */
	public int getPaperdollItemVisualId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		return item != null ? item.getVisualId() : 0;
	}

	public int[] getPaperdollItemAugmentationId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
			return item.getAugmentations();
		return ItemInstance.EMPTY_AUGMENTATIONS;
	}

	@Override
	protected void onRefreshWeight()
	{
		getActor().refreshOverloaded();
	}

	public void validateItems()
	{
		for(ItemInstance item : _paperdoll)
			if(item != null && (ItemFunctions.checkIfCanEquip(getActor(), item) != null || !item.getTemplate().testCondition(getActor(), item, false)))
			{
				unEquipItem(item);
				getActor().sendDisarmMessage(item);
			}
	}

	public void validateItemsSkills()
	{
		for(ItemInstance item : _paperdoll)
			if(item != null)
				if(item.getTemplate().getType2() == 0)
				{
					boolean needUnequipSkills = getActor().getWeaponsExpertisePenalty() > 0;
					if(item.getTemplate().getAttachedSkills().length > 0)
					{
						boolean has = getActor().getSkillLevel(item.getTemplate().getAttachedSkills()[0].getId()) > 0;
						if(needUnequipSkills && has)
							ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
						else if(!needUnequipSkills && !has)
							ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
					}
					else if(item.getTemplate().getEnchant4Skill() != null)
					{
						boolean has = getActor().getSkillLevel(item.getTemplate().getEnchant4Skill().getId()) > 0;
						if(needUnequipSkills && has)
							ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
						else if(!needUnequipSkills && !has)
							ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
					}
					else if(!item.getTemplate().getTriggerList().isEmpty())
						if(needUnequipSkills)
							ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
						else
							ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
				}
	}

	public void refreshEquip()
	{
		isRefresh = true;
		for(ItemInstance item : getItems())
			if(item.isEquipped())
			{
				int slot = item.getEquipSlot();
				_listeners.onUnequip(slot, item);
				_listeners.onEquip(slot, item);
			}
			else if(item.getTemplate().isRune())
			{
				_listeners.onUnequip(-1, item);
				_listeners.onEquip(-1, item);
			}
		isRefresh = false;
	}

	public void sort(int[][] order)
	{
		boolean needSort = false;
		for(int[] element : order)
		{
			ItemInstance item = getItemByObjectId(element[0]);
			if(item != null)
				if(item.getLocation() == ItemInstance.ItemLocation.INVENTORY)
					if(item.getLocData() != element[1])
					{
						item.setLocData(element[1]);
						item.setJdbcState(JdbcEntityState.UPDATED);
						needSort = true;
					}
		}
		if(needSort)
			_items.sort(ItemOrderComparator.getInstance());
	}

	public ItemInstance findArrowForBow(ItemTemplate bow)
	{
		ItemInstance res = null;
		for(ItemInstance temp : getItems())
			if((temp.getItemType() == EtcItemTemplate.EtcItemType.ARROW || temp.getItemType() == EtcItemTemplate.EtcItemType.ARROW_QUIVER) && bow.getGrade().extOrdinal() == temp.getGrade().extOrdinal())
			{
				if(temp.getLocation() == ItemInstance.ItemLocation.PAPERDOLL && temp.getEquipSlot() == 8)
					return temp;
				if(res == null || temp.getItemId() < res.getItemId())
					res = temp;
			}
		return res;
	}

	public void lockItems(LockType lock, int[] items)
	{
		if(_lockType != LockType.NONE)
			return;
		_lockType = lock;
		_lockItems = items;
		getActor().sendItemList(false);
	}

	public void unlock()
	{
		if(_lockType == LockType.NONE)
			return;
		_lockType = LockType.NONE;
		_lockItems = ArrayUtils.EMPTY_INT_ARRAY;
		getActor().sendItemList(false);
	}

	public boolean isLockedItem(ItemInstance item)
	{
		switch(_lockType)
		{
			case INCLUDE:
			{
				return ArrayUtils.contains(_lockItems, item.getItemId());
			}
			case EXCLUDE:
			{
				return !ArrayUtils.contains(_lockItems, item.getItemId());
			}
			default:
			{
				return false;
			}
		}
	}

	public LockType getLockType()
	{
		return _lockType;
	}

	public int[] getLockItems()
	{
		return _lockItems;
	}

	@Override
	protected void onRestoreItem(ItemInstance item)
	{
		super.onRestoreItem(item);
		if(item.getTemplate().isRune())
			_listeners.onEquip(-1, item);
		if(item.isTemporalItem() || item.isFlagLifeTime())
			item.startTimer(new LifeTimeTask(item));
		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);
	}

	@Override
	protected void onAddItem(ItemInstance item)
	{
		super.onAddItem(item);
		if(item.getTemplate().isRune())
			_listeners.onEquip(-1, item);
		if(item.getTemplate().isArrow() || item.getTemplate().isQuiver())
			getActor().checkAndEquipArrows();
		if(item.isTemporalItem() || item.isFlagLifeTime())
			item.startTimer(new LifeTimeTask(item));
		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);
	}

	@Override
	protected void onModifyItem(ItemInstance item)
	{
		super.onModifyItem(item);
		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);
	}

	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		super.onRemoveItem(item);
		Player owner = getActor();
		owner.removeItemFromShortCut(item.getObjectId());
		if(item.getTemplate().isRune())
			_listeners.onUnequip(-1, item);
		if(item.isTemporalItem() || item.isFlagLifeTime())
			item.stopTimer();
		if(owner.getMountControlItemObjId() == item.getObjectId())
			owner.setMount(null);
		if(owner.getPetControlItem() == item)
		{
			PetInstance pet = owner.getPet();
			if(pet != null)
				pet.unSummon(false);
		}
		for(QuestState state : _owner.getAllQuestsStates())
			state.getQuest().notifyUpdateItem(item, state);

		ItemService.getInstance().disableAllVisualChanges(item, getActor());
	}

	@Override
	protected void onEquip(int slot, ItemInstance item)
	{
		super.onEquip(slot, item);
		if(item.isShadowItem())
			item.startTimer(new ShadowLifeTimeTask(item));
	}

	@Override
	protected void onReequip(int slot, ItemInstance newItem, ItemInstance oldItem)
	{
		super.onReequip(slot, newItem, oldItem);
		if(oldItem.isShadowItem())
			oldItem.stopTimer();
		if(newItem.isShadowItem())
			newItem.startTimer(new ShadowLifeTimeTask(newItem));
	}

	@Override
	protected void onUnequip(int slot, ItemInstance item)
	{
		super.onUnequip(slot, item);
		if(item.isShadowItem())
			item.stopTimer();
	}

	@Override
	public void restore()
	{
		int ownerId = getOwnerId();
		writeLock();
		try
		{
			Collection<ItemInstance> items = ItemContainer._itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getBaseLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
			}
			_items.sort(ItemOrderComparator.getInstance());
			items = ItemContainer._itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getEquipLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
				if(item.getEquipSlot() >= 33)
				{
					item.setLocation(getBaseLocation());
					item.setLocData(0);
					item.setEquipped(false);
				}
				else
					setPaperdollItem(item.getEquipSlot(), item);
			}
		}
		finally
		{
			writeUnlock();
		}
		DelayedItemsManager.getInstance().loadDelayed(getActor(), false);
		refreshWeight();
	}

	@Override
	public void store()
	{
		writeLock();
		try
		{
			ItemContainer._itemsDAO.update(_items);
		}
		finally
		{
			writeUnlock();
		}
	}

	@Override
	public void sendAddItem(ItemInstance item)
	{
		Player actor = getActor();
		actor.sendPacket(new InventoryUpdatePacket().addNewItem(actor, item));
		if(item.getItemId() == 57)
			actor.sendPacket(new ExAdenaInvenCount(actor));
	}

	@Override
	public void sendModifyItem(ItemInstance... items)
	{
		Player actor = getActor();
		InventoryUpdatePacket iu = new InventoryUpdatePacket();
		for(ItemInstance item : items)
			iu.addModifiedItem(actor, item);
		actor.sendPacket(iu);
		for(ItemInstance item : items)
			if(item.getItemId() == 57)
				actor.sendPacket(new ExAdenaInvenCount(actor));
	}

	@Override
	public void sendRemoveItem(ItemInstance item)
	{
		Player actor = getActor();
		actor.sendPacket(new InventoryUpdatePacket().addRemovedItem(actor, item));
		if(item.getItemId() == 57)
			actor.sendPacket(new ExAdenaInvenCount(actor));
	}

	@Override
	public void sendEquipInfo(int slot)
	{
		getActor().broadcastUserInfo(true);
		getActor().sendPacket(new ExUserInfoEquipSlot(getActor(), slot));
	}

	public void startTimers()
	{}

	public void stopAllTimers()
	{
		for(ItemInstance item : getItems())
			if(item.isShadowItem() || item.isTemporalItem() || item.isFlagLifeTime())
				item.stopTimer();
	}

	public void destroyAllItems()
	{
		for(ItemInstance item : getItems().clone())
			removeItem(item);
	}

	protected class ShadowLifeTimeTask implements Runnable
	{
		private final ItemInstance item;

		ShadowLifeTimeTask(ItemInstance item)
		{
			this.item = item;
		}

		@Override
		public void run()
		{
			Player player = getActor();
			if(!item.isEquipped())
				return;
			int mana;
			synchronized (item)
			{
				item.setLifeTime(item.getLifeTime() - 1);
				mana = item.getShadowLifeTime();
				if(mana <= 0)
					destroyItem(item);
			}
			SystemMessage sm = null;
			if(mana == 10)
				sm = new SystemMessage(1979);
			else if(mana == 5)
				sm = new SystemMessage(1980);
			else if(mana == 1)
				sm = new SystemMessage(1981);
			else if(mana <= 0)
				sm = new SystemMessage(1982);
			else
				player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, item));
			if(sm != null)
			{
				sm.addItemName(item.getItemId());
				player.sendPacket(sm);
			}
		}
	}

	protected class LifeTimeTask implements Runnable
	{
		private final ItemInstance item;

		LifeTimeTask(ItemInstance item)
		{
			this.item = item;
		}

		@Override
		public void run()
		{
			Player player = getActor();
			int left;
			synchronized (item)
			{
				left = item.getTemporalLifeTime();
				if(left <= 0) {
					destroyItem(item);
					Party party = player.getParty();
					if (party != null) {
						party.recalculatePartyData();
					}
				}
			}
			if(left <= 0)
				player.sendPacket(new SystemMessage(2366).addItemName(item.getItemId()));
		}
	}
}
