package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.manor.CropProcure;
import l2s.gameserver.utils.NpcUtils;
import org.apache.commons.lang3.ArrayUtils;

public class RequestProcureCropList extends L2GameClientPacket
{
	private int _count;
	private int[] _items;
	private int[] _crop;
	private int[] _manor;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 20 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_crop = new int[_count];
		_manor = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0; i < _count; ++i)
		{
			_items[i] = readD();
			_crop[i] = readD();
			_manor[i] = readD();
			_itemQ[i] = readQ();
			if(_crop[i] < 1 || _manor[i] < 1 || _itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i)
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
		NpcInstance manor = NpcUtils.canPassPacket(activeChar, this);
		if(manor == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		int currentManorId = manor == null ? 0 : manor.getCastle().getId();
		long totalFee = 0L;
		int slots = 0;
		long weight = 0L;
		try
		{
			for(int i = 0; i < _count; ++i)
			{
				int objId = _items[i];
				int cropId = _crop[i];
				int manorId = _manor[i];
				long count = _itemQ[i];
				ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
				if(item == null || item.getCount() < count || item.getItemId() != cropId)
					return;
				Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
				if(castle == null)
					return;
				CropProcure crop = castle.getCrop(cropId, 0);
				if(crop == null || crop.getId() == 0 || crop.getPrice() == 0L)
					return;
				if(count > crop.getAmount())
					return;
				long price = SafeMath.mulAndCheck(count, crop.getPrice());
				long fee = 0L;
				if(currentManorId != 0 && manorId != currentManorId)
					fee = price * 5L / 100L;
				totalFee = SafeMath.addAndCheck(totalFee, fee);
				int rewardItemId = Manor.getInstance().getRewardItem(cropId, crop.getReward());
				ItemTemplate template = ItemHolder.getInstance().getTemplate(rewardItemId);
				if(template == null)
					return;
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, template.getWeight()));
				if(!template.isStackable() || activeChar.getInventory().getItemByItemId(cropId) == null)
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
			if(activeChar.getInventory().getAdena() < totalFee)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(int i = 0; i < _count; ++i)
			{
				int objId = _items[i];
				int cropId = _crop[i];
				int manorId = _manor[i];
				long count = _itemQ[i];
				ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
				if(item != null && item.getCount() >= count)
					if(item.getItemId() == cropId)
					{
						Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
						if(castle != null)
						{
							CropProcure crop = castle.getCrop(cropId, 0);
							if(crop != null && crop.getId() != 0)
								if(crop.getPrice() != 0L)
									if(count <= crop.getAmount())
									{
										int rewardItemId2 = Manor.getInstance().getRewardItem(cropId, crop.getReward());
										long sellPrice = count * crop.getPrice();
										long rewardPrice = ItemHolder.getInstance().getTemplate(rewardItemId2).getReferencePrice();
										if(rewardPrice != 0L)
										{
											double reward = sellPrice / rewardPrice;
											long rewardItemCount = (long) reward + (Rnd.nextDouble() <= reward % 1.0 ? 1 : 0);
											if(rewardItemCount < 1L)
											{
												SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.FAILED_IN_TRADING_S2_OF_S1_CROPS);
												sm.addItemName(cropId);
												sm.addNumber(count);
												activeChar.sendPacket(sm);
											}
											else
											{
												long fee2 = 0L;
												if(currentManorId != 0 && manorId != currentManorId)
													fee2 = sellPrice * 5L / 100L;
												if(activeChar.getInventory().destroyItemByObjectId(objId, count))
													if(!activeChar.reduceAdena(fee2, false))
													{
														SystemMessagePacket sm2 = new SystemMessagePacket(SystemMsg.FAILED_IN_TRADING_S2_OF_S1_CROPS);
														sm2.addItemName(cropId);
														sm2.addNumber(count);
														activeChar.sendPacket(sm2, SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
													}
													else
													{
														crop.setAmount(crop.getAmount() - count);
														castle.updateCrop(crop.getId(), crop.getAmount(), 0);
														castle.addToTreasuryNoTax(fee2, false, false);
														if(activeChar.getInventory().addItem(rewardItemId2, rewardItemCount) != null)
														{
															activeChar.sendPacket(new SystemMessagePacket(SystemMsg.TRADED_S2_OF_S1_CROPS).addItemName(cropId).addNumber(count), SystemMessagePacket.removeItems(cropId, count), SystemMessagePacket.obtainItems(rewardItemId2, rewardItemCount, 0));
															if(fee2 > 0L)
																activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES).addNumber(fee2));
														}
													}
											}
										}
									}
						}
					}
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.sendChanges();
	}
}
