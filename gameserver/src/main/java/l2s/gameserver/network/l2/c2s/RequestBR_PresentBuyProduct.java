package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;
import l2s.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;

public class RequestBR_PresentBuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;
	private String _receiverName;
	private String _topic;
	private String _message;

	@Override
	protected void readImpl() throws Exception
	{
		_productId = readD();
		_count = readD();
		_receiverName = readS();
		_topic = readS();
		_message = readS();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_count > 99 || _count < 0)
			return;
		ProductItem product = ProductDataHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(ExBR_PresentBuyProductPacket.RESULT_WRONG_PRODUCT);
			return;
		}
		if(!product.isOnSale() || System.currentTimeMillis() < product.getStartTimeSale() || System.currentTimeMillis() > product.getEndTimeSale())
		{
			activeChar.sendPacket(ExBR_PresentBuyProductPacket.RESULT_SALE_PERIOD_ENDED);
			return;
		}
		int pointsRequired = product.getPoints(true) * _count;
		if(pointsRequired < 0)
		{
			activeChar.sendPacket(ExBR_PresentBuyProductPacket.RESULT_WRONG_PRODUCT);
			return;
		}
		long pointsCount = activeChar.getPremiumPoints();
		if(pointsRequired > pointsCount)
		{
			activeChar.sendPacket(ExBR_PresentBuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
			return;
		}
		Player receiver = GameObjectsStorage.getPlayer(_receiverName);
		int recieverId;
		if(receiver != null)
		{
			recieverId = receiver.getObjectId();
			_receiverName = receiver.getName();
			if(receiver.getBlockList().contains(activeChar))
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_C1).addString(_receiverName));
				return;
			}
		}
		else
		{
			recieverId = CharacterDAO.getInstance().getObjectIdByName(_receiverName);
			if(recieverId > 0 && mysql.simple_get_int("target_Id", "character_blocklist", "obj_Id=" + recieverId + " AND target_Id=" + activeChar.getObjectId()) > 0)
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_C1).addString(_receiverName));
				return;
			}
		}
		if(recieverId == 0)
		{
			activeChar.sendPacket(SystemMsg.WHEN_THE_RECIPIENT_DOESNT_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE);
			return;
		}
		if(!activeChar.reducePremiumPoints(pointsRequired))
		{
			activeChar.sendPacket(ExBR_PresentBuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
			return;
		}
		activeChar.getProductHistoryList().onPurchaseProduct(product);
		List<ItemInstance> attachments = new ArrayList<>();
		for(ProductItemComponent comp : product.getComponents())
		{
			ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(comp.getId());
			if(itemTemplate.isStackable())
			{
				ItemInstance item = ItemFunctions.createItem(itemTemplate.getItemId());
				item.setCount(comp.getCount() * _count);
				item.setOwnerId(activeChar.getObjectId());
				item.setLocation(ItemInstance.ItemLocation.MAIL);
				if(item.getJdbcState().isSavable())
					item.save();
				else
				{
					item.setJdbcState(JdbcEntityState.UPDATED);
					item.update();
				}
				attachments.add(item);
			}
			else
				for(long count = comp.getCount() * _count, i = 0L; i < count; ++i)
				{
					ItemInstance item = ItemFunctions.createItem(itemTemplate.getItemId());
					item.setCount(1L);
					item.setOwnerId(activeChar.getObjectId());
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					if(item.getJdbcState().isSavable())
						item.save();
					else
					{
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();
					}
					attachments.add(item);
				}
		}

		Mail mail = new Mail();
		mail.setSenderId(activeChar.getObjectId());
		mail.setSenderName(activeChar.getName());
		mail.setSenderHwid(activeChar.getHwidHolder().asString());
		mail.setReceiverId(recieverId);
		mail.setReceiverName(_receiverName);
		mail.setTopic(_topic);
		mail.setBody(_message);
		mail.setPrice(0L);
		mail.setUnread(true);
		mail.setType(Mail.SenderType.PRESENT);
		mail.setExpireTime(1296000 + (int) (System.currentTimeMillis() / 1000L));
		for(ItemInstance item2 : attachments)
			mail.addAttachment(item2);
		mail.save();
		activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
		activeChar.sendPacket(new ExBR_GamePointPacket(activeChar));
		activeChar.sendPacket(ExBR_PresentBuyProductPacket.RESULT_OK);
		activeChar.sendChanges();
		if(receiver != null)
		{
			receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			receiver.sendPacket(new ExUnReadMailCount(receiver));
			receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
		}
	}
}
