package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.EnchantItemHolder;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExPutEnchantTargetItemResult;
import l2s.gameserver.templates.item.support.EnchantScroll;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.isActionsDisabled() || player.isInStoreMode() || player.isInTrade())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			return;
		}
		PcInventory inventory = player.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		ItemInstance scroll = player.getEnchantScroll();
		if(itemToEnchant == null || scroll == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			return;
		}

		String messagePattern = "{} trying to put enchant on item {}";
		ParameterizedMessage message = new ParameterizedMessage(messagePattern, player, itemToEnchant);
		LogService.getInstance().log(LoggerType.ENCHANTS, message);

		int scrollId = scroll.getItemId();
		int itemId = itemToEnchant.getItemId();
		EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scrollId);
		if(!itemToEnchant.canBeEnchanted() || itemToEnchant.isStackable())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.setEnchantScroll(null);
			return;
		}
		if(itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}
		if(player.isInStoreMode())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return;
		}
		if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			return;
		}
		if(enchantScroll == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			return;
		}
		if(!enchantScroll.getItems().isEmpty())
		{
			if(!enchantScroll.getItems().contains(itemId))
			{
				player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				return;
			}
		}
		else
		{
			if(!enchantScroll.containsGrade(itemToEnchant.getGrade()))
			{
				player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				return;
			}
			int itemType = itemToEnchant.getTemplate().getType2();
			switch(enchantScroll.getType())
			{
				case ARMOR:
				{
					if(itemType == 0 || itemToEnchant.getTemplate().isHairAccessory())
					{
						player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						return;
					}
					break;
				}
				case WEAPON:
				{
					if(itemType == 1 || itemType == 2 || itemToEnchant.getTemplate().isHairAccessory())
					{
						player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						return;
					}
					break;
				}
				case HAIR_ACCESSORY:
				{
					if(!itemToEnchant.getTemplate().isHairAccessory())
					{
						player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						return;
					}
					break;
				}
			}
		}
		if(enchantScroll.getMaxEnchant() != -1 && itemToEnchant.getEnchantLevel() >= enchantScroll.getMaxEnchant())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}
		if(itemToEnchant.getOwnerId() != player.getObjectId())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			return;
		}
		player.sendPacket(ExPutEnchantTargetItemResult.SUCCESS);
	}
}
