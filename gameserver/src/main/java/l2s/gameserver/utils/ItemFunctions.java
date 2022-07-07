package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.PetDataHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.attachment.PickableAttachment;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.taskmanager.DelayedItemsManager;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

public final class ItemFunctions
{
	public static ItemInstance createItem(int itemId)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setLocation(ItemInstance.ItemLocation.VOID);
		item.setCount(1L);
		return item;
	}

	public static List<ItemInstance> addItem(Playable playable, int itemId, long count)
	{
		return addItem(playable, itemId, count, 0, true);
	}

	public static List<ItemInstance> addItem(Playable playable, int itemId, long count, boolean notify)
	{
		return addItem(playable, itemId, count, 0, notify);
	}

	public static List<ItemInstance> addItem(Playable playable, int itemId, long count, int enchantLevel, boolean notify)
	{
		if(playable == null || count < 1L)
			return Collections.emptyList();

		Player player = playable.getPlayer();

        if (!player.isOnline()) {
            int nextId = IdFactory.getInstance().getNextId();
            DelayedItemsManager.addDelayed(nextId, itemId, count, enchantLevel, "ItemFunctions");
            return Collections.emptyList();
        }

		if(itemId > 0)
		{
			List<ItemInstance> items = new ArrayList<>();
			ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);

			if(t.isStackable())
			{
				items.add(player.getInventory().addItem(itemId, count));
				if(notify)
					player.sendPacket(SystemMessagePacket.obtainItems(itemId, count, 0));
			}
			else
				for(long i = 0L; i < count; ++i)
				{
					ItemInstance item = player.getInventory().addItem(itemId, 1L, enchantLevel);
					items.add(item);
					if(notify)
						player.sendPacket(SystemMessagePacket.obtainItems(item));
				}

			return items;
		}
		else if(itemId == -100)
			player.getPlayer().addPcBangPoints((int) count, false, notify);
		else if(itemId == -200)
		{
			if(player.getPlayer().getClan() != null)
			{
				player.getPlayer().getClan().incReputation((int) count, false, "itemFunction");
				if(notify)
				{}
			}
		}
		else if(itemId == -300)
			player.getPlayer().setFame((int) count + player.getPlayer().getFame(), "itemFunction", notify);

		return Collections.emptyList();
	}

	public static long getItemCount(Playable playable, int itemId)
	{
		if(playable == null)
			return 0L;
		Playable player = playable.getPlayer();
		return player.getInventory().getCountOf(itemId);
	}

	public static boolean haveItem(Playable playable, int itemId, long count)
	{
		return getItemCount(playable, itemId) >= count;
	}

	public static boolean deleteItem(Playable playable, int itemId, long count)
	{
		return deleteItem(playable, itemId, count, true);
	}

	public static boolean deleteItem(Playable playable, int itemId, long count, boolean notify)
	{
		if(playable == null || count < 1L)
			return false;
		Player player = playable.getPlayer();
		if(itemId > 0)
		{
			player.getInventory().writeLock();
			try
			{
				ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
				if(t == null)
					return false;
				if(t.isStackable())
				{
					if(!player.getInventory().destroyItemByItemId(itemId, count))
						return false;
				}
				else
				{
					if(player.getInventory().getCountOf(itemId) < count)
						return false;
					for(long i = 0L; i < count; ++i)
						if(!player.getInventory().destroyItemByItemId(itemId, 1L))
							return false;
				}
			}
			finally
			{
				player.getInventory().writeUnlock();
			}
			if(notify)
				player.sendPacket(SystemMessagePacket.removeItems(itemId, count));
		}
		else if(itemId == -100)
			player.addPcBangPoints((int) count, false, notify);
		else if(itemId == -200)
		{
			Clan clan = player.getClan();
			if(clan == null)
				return false;
			if(clan.getReputationScore() < count)
				return false;
			clan.incReputation((int) -count, false, "itemFunction");
			if(notify)
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLANS_REPUTATION).addNumber(count));
		}
		else if(itemId == -300)
		{
			if(player.getPlayer().getFame() < count)
				return false;
			player.getPlayer().setFame((int) (player.getPlayer().getFame() - count), "itemFunction", notify);
		}
		return true;
	}

	public static void deleteItemsEverywhere(Playable playable, int itemId)
	{
		if(playable == null)
			return;
		Player player = playable.getPlayer();
		if(itemId > 0)
		{
			player.getInventory().writeLock();
			try
			{
				for(ItemInstance item = player.getInventory().getItemByItemId(itemId); item != null; item = player.getInventory().getItemByItemId(itemId))
					player.getInventory().destroyItem(item);
			}
			finally
			{
				player.getInventory().writeUnlock();
			}
			player.getWarehouse().writeLock();
			try
			{
				for(ItemInstance item = player.getWarehouse().getItemByItemId(itemId); item != null; item = player.getWarehouse().getItemByItemId(itemId))
					player.getWarehouse().destroyItem(item);
			}
			finally
			{
				player.getWarehouse().writeUnlock();
			}
			player.getFreight().writeLock();
			try
			{
				for(ItemInstance item = player.getFreight().getItemByItemId(itemId); item != null; item = player.getFreight().getItemByItemId(itemId))
					player.getFreight().destroyItem(item);
			}
			finally
			{
				player.getFreight().writeUnlock();
			}
			player.getRefund().writeLock();
			try
			{
				for(ItemInstance item = player.getRefund().getItemByItemId(itemId); item != null; item = player.getRefund().getItemByItemId(itemId))
					player.getRefund().destroyItem(item);
			}
			finally
			{
				player.getRefund().writeUnlock();
			}
		}
	}

	public static boolean deleteItem(Playable playable, ItemInstance item, long count)
	{
		return deleteItem(playable, item, count, true);
	}

	public static boolean deleteItem(Playable playable, ItemInstance item, long count, boolean notify)
	{
		if(playable == null || count < 1L)
			return false;
		if(item.getCount() < count)
			return false;
		Player player = playable.getPlayer();
		player.getInventory().writeLock();
		try
		{
			if(!player.getInventory().destroyItem(item, count))
				return false;
		}
		finally
		{
			player.getInventory().writeUnlock();
		}
		if(notify)
			player.sendPacket(SystemMessagePacket.removeItems(item.getItemId(), count));
		return true;
	}

	public static final IBroadcastPacket checkIfCanEquip(PetInstance pet, ItemInstance item)
	{
		if(!item.isEquipable())
			return SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM;
		int petId = pet.getNpcId();
		if(item.getTemplate().isPendant() || PetDataHolder.isWolf(petId) && item.getTemplate().isForWolf() || PetDataHolder.isHatchling(petId) && item.getTemplate().isForHatchling() || PetDataHolder.isStrider(petId) && item.getTemplate().isForStrider() || PetDataHolder.isGreatWolf(petId) && item.getTemplate().isForGWolf() || PetDataHolder.isBabyPet(petId) && item.getTemplate().isForPetBaby() || PetDataHolder.isImprovedBabyPet(petId) && item.getTemplate().isForPetBaby())
			return null;
		return SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM;
	}

	public static final IBroadcastPacket checkIfCanEquip(Player player, ItemInstance item)
	{
		int itemId = item.getItemId();
		int targetSlot = item.getTemplate().getBodyPart();
		Clan clan = player.getClan();

		if(item.getItemType() == WeaponTemplate.WeaponType.DUALDAGGER && player.getSkillLevel(923) < 1)
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(itemId == 6841 && (clan == null || !player.isClanLeader() || clan.getCastle() == 0))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(targetSlot == ItemTemplate.SLOT_DECO)
		{
			int count = player.getTalismanCount();
			if(count <= 0)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_WEAR_S1_BECAUSE_YOU_ARE_NOT_WEARING_A_BRACELET).addItemName(itemId);
			for(int slot = Inventory.PAPERDOLL_DECO1; slot <= Inventory.PAPERDOLL_DECO6; slot++)
			{
				ItemInstance deco = player.getInventory().getPaperdollItem(slot);
				if(deco == null)
					continue;
				if(deco == item)
					return null;

				if(--count > 0 && deco.getItemId() != itemId)
					continue;

				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
			}
		}

		boolean isItemInTrade = player.getTradeList().stream()
				.map(ItemInfo::getObjectId)
				.anyMatch(objectId -> item.getObjectId() == objectId);
		if(isItemInTrade)
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		boolean canEquip = player.getGveZones().stream()
				.allMatch(gveZone -> gveZone.canEquipItem(player, item));
		if(!canEquip)
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		return null;
	}

	public static boolean checkIfCanPickup(Playable playable, ItemInstance item)
	{
		Player player = playable.getPlayer();
		return item.getDropTimeOwner() <= System.currentTimeMillis() || item.getDropPlayers().contains(player.getObjectId());
	}

	public static boolean canAddItem(Player player, ItemInstance item)
	{
		if(!player.getInventory().validateWeight(item))
		{
			player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return false;
		}
		if(!player.getInventory().validateCapacity(item))
		{
			player.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		if(!item.getTemplate().getHandler().pickupItem(player, item))
			return false;
		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		return attachment == null || attachment.canPickUp(player);
	}

	public static final boolean checkIfCanDiscard(Player player, ItemInstance item)
	{
		return player.getMountControlItemObjId() != item.getObjectId() && player.getPetControlItem() != item && player.getEnchantScroll() != item && !item.getTemplate().isQuest();
	}

	public static int getCrystallizeCrystalAdd(ItemInstance item)
	{
		int crystalsAdd = 0;
		if(item.isWeapon())
			switch(item.getGrade())
			{
				case D:
				{
					crystalsAdd = 90;
					break;
				}
				case C:
				{
					crystalsAdd = 45;
					break;
				}
				case B:
				{
					crystalsAdd = 67;
					break;
				}
				case A:
				{
					crystalsAdd = 145;
					break;
				}
				case S:
				case S80:
				case S84:
				{
					crystalsAdd = 250;
					break;
				}
				case R:
				case R95:
				case R99:
				{
					crystalsAdd = 500;
					break;
				}
			}
		else
			switch(item.getGrade())
			{
				case D:
				{
					crystalsAdd = 11;
					break;
				}
				case C:
				{
					crystalsAdd = 6;
					break;
				}
				case B:
				{
					crystalsAdd = 11;
					break;
				}
				case A:
				{
					crystalsAdd = 20;
					break;
				}
				case S:
				case S80:
				case S84:
				{
					crystalsAdd = 25;
					break;
				}
				case R:
				case R95:
				case R99:
				{
					crystalsAdd = 30;
					break;
				}
			}
		int result = 0;
		if(item.getEnchantLevel() > 3)
		{
			result = crystalsAdd * 3;
			if(item.isWeapon())
				crystalsAdd *= 2;
			else
				crystalsAdd *= 3;
			result += crystalsAdd * (item.getEnchantLevel() - 3);
		}
		else
			result = crystalsAdd * item.getEnchantLevel();
		return result;
	}

	public static boolean checkIsEquipped(Player player, int slot, int itemId, int enchant)
	{
		Inventory inv = player.getInventory();
		if(slot < 0)
		{
			for(int s : Inventory.PAPERDOLL_ORDER)
			{
				ItemInstance item = inv.getPaperdollItem(s);
				if(item != null)
					if(item.getItemId() == itemId && item.getFixedEnchantLevel(player) >= enchant)
						return true;
			}
			return false;
		}
		ItemInstance item2 = inv.getPaperdollItem(slot);
		if(item2 == null)
			return itemId == 0;
		return item2.getItemId() == itemId && item2.getFixedEnchantLevel(player) >= enchant;
	}

	public static boolean checkForceUseItem(Player player, ItemInstance item, boolean sendMsg)
	{
		if(player.isOutOfControl())
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}
		int itemId = item.getItemId();
		if(player.isFishing() && (itemId < 6535 || itemId > 6540 && itemId != 38154))
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return false;
		}
		if(player.isStunned() || player.isDecontrolled() || player.isSleeping() || player.isAfraid() || player.isAlikeDead())
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}
		if(item.getTemplate().isQuest())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_CANNOT_USE_QUEST_ITEMS);
			return false;
		}
		return true;
	}

	public static boolean checkUseItem(Player player, ItemInstance item, boolean sendMsg)
	{
		if(player.isInStoreMode())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
			return false;
		}
		if(player.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
		{
			if(sendMsg)
				player.sendReuseMessage(item);
			return false;
		}
		if(!item.isEquipped() && !item.getTemplate().testCondition(player, item, sendMsg))
			return false;
		if(player.getInventory().isLockedItem(item))
			return false;
		for(Event e : player.getEvents())
		{
			IBroadcastPacket result = e.canUseItem(player, item);
			if(result != null)
			{
				if(sendMsg)
					player.sendPacket(result);
				return false;
			}
		}
		if(item.getTemplate().isForPet())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM);
			return false;
		}
		return true;
	}

	public static boolean useItem(Player player, ItemInstance item, boolean ctrl, boolean sendMsg)
	{
		return checkForceUseItem(player, item, sendMsg) && (player.useItem(item, ctrl, true) || checkUseItem(player, item, sendMsg) && player.useItem(item, ctrl, false));
	}

	public static boolean needToLogProducts(List<MultiSellIngredient> products) {
		return products.stream()
			.anyMatch(ingredient -> Config.MULTISELL.loggableItems().contains(ingredient.getItemId()));
	}
}
