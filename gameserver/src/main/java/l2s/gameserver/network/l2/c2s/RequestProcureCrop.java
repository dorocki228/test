package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.ManorManagerInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.manor.CropProcure;

import java.util.Collections;
import java.util.List;

public class RequestProcureCrop extends L2GameClientPacket
{
	private int _manorId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private List<CropProcure> _procureList;

	public RequestProcureCrop()
	{
		_procureList = Collections.emptyList();
	}

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 16 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0; i < _count; ++i)
		{
			readD();
			_items[i] = readD();
			_itemQ[i] = readQ();
			if(_itemQ[i] < 1L)
			{
				_count = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.isPK() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		GameObject target = activeChar.getTarget();
		ManorManagerInstance manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		if(!activeChar.isGM() && (manor == null || !activeChar.checkInteractionDistance(manor)))
		{
			activeChar.sendActionFailed();
			return;
		}
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(castle == null)
			return;
		int slots = 0;
		long weight = 0L;
		try
		{
			for(int i = 0; i < _count; ++i)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				CropProcure crop = castle.getCrop(itemId, 0);
				if(crop == null)
					return;
				int rewradItemId = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
				long rewradItemCount = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));
				rewradItemCount = SafeMath.mulAndCheck(count, rewradItemCount);
				ItemTemplate template = ItemHolder.getInstance().getTemplate(rewradItemId);
				if(template == null)
					return;
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, template.getWeight()));
				if(!template.isStackable() || activeChar.getInventory().getItemByItemId(itemId) == null)
					++slots;
			}
		}
		catch(ArithmeticException ae)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		activeChar.getInventory().writeLock();
		try
		{
			if(!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			if(!activeChar.getInventory().validateCapacity(slots))
			{
				activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			_procureList = castle.getCropProcure(0);
			for(int i = 0; i < _count; ++i)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				int rewradItemId2 = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
				long rewradItemCount2 = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));
				rewradItemCount2 = SafeMath.mulAndCheck(count, rewradItemCount2);
				if(activeChar.getInventory().destroyItemByItemId(itemId, count))
				{
					ItemInstance item = activeChar.getInventory().addItem(rewradItemId2, rewradItemCount2);
					if(item != null)
						activeChar.sendPacket(SystemMessagePacket.obtainItems(rewradItemId2, rewradItemCount2, 0));
				}
			}
		}
		catch(ArithmeticException ae)
		{
			_count = 0;
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.sendChanges();
	}
}
