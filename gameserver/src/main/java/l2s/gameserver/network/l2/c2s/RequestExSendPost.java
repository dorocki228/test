package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.mysql;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExReplyWritePost;
import l2s.gameserver.network.l2.s2c.ExUnReadMailCount;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Util;
import l2s.gameserver.utils.spamfilter.SpamFilterManager;
import l2s.gameserver.utils.spamfilter.SpamFilterManager.SpamType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestExSendPost extends L2GameClientPacket
{
	private int _messageType;
	private String _recieverName;
	private String _topic;
	private String _body;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private long _price;

	@Override
	protected void readImpl()
	{
		_recieverName = readS(35);
		_messageType = readD();
		_topic = readS(127);
		_body = readS(32767);
		_count = readD();
		if(_count * 12 + 4 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0; i < _count; ++i)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			if(_itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				return;
			}
		}
		_price = readQ();
		if(_price < 0L)
		{
			_count = 0;
			_price = 0L;
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getPlayerAccess().UseMail)
		{
			activeChar.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_);
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isGM() && "ONLINE_ALL".equalsIgnoreCase(_recieverName))
		{
			Map<Integer, Long> map = new HashMap<>();
			if(_items != null && _items.length > 0)
				for(int i = 0; i < _items.length; ++i)
				{
					ItemInstance item = activeChar.getInventory().getItemByObjectId(_items[i]);
					map.put(item.getItemId(), _itemQ[i]);
				}
			for(Player p : GameObjectsStorage.getPlayers())
				if(p != null && p.isOnline())
					Functions.sendSystemMail(p, _topic, _body, map);
			activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
			activeChar.sendPacket(SystemMsg.MAIL_SUCCESSFULLY_SENT);
			return;
		}
		if(!Config.ALLOW_MAIL)
		{
			activeChar.sendMessage(new CustomMessage("mail.Disabled"));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FORWARD_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FORWARD_DURING_AN_EXCHANGE);
			return;
		}
		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FORWARD_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}
		if(activeChar.getName().equalsIgnoreCase(_recieverName))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(!activeChar.getAntiFlood().canMail())
		{
			activeChar.sendPacket(SystemMsg.THE_PREVIOUS_MAIL_WAS_FORWARDED_LESS_THAN_1_MINUTE_AGO_AND_THIS_CANNOT_BE_FORWARDED);
			return;
		}
		if(_price > 0L)
		{
			if(!activeChar.getPlayerAccess().UseMail)
			{
				activeChar.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_);
				activeChar.sendActionFailed();
				return;
			}
			String tradeBan = activeChar.getVar("tradeBan");
			if(tradeBan != null && ("-1".equals(tradeBan) || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			{
				if("-1".equals(tradeBan))
					activeChar.sendMessage(new CustomMessage("common.TradeBannedPermanently"));
				else
					activeChar.sendMessage(new CustomMessage("common.TradeBanned").addString(Util.formatTime((int) (Long.parseLong(tradeBan) / 1000L - System.currentTimeMillis() / 1000L))));
				return;
			}
		}
		if(activeChar.getBlockList().contains(_recieverName))
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_BLOCKED_C1).addString(_recieverName));
			return;
		}
		Player target = GameObjectsStorage.getPlayer(_recieverName);
		int recieverId;
		if(target != null)
		{
			recieverId = target.getObjectId();
			_recieverName = target.getName();
			if(target.getBlockList().contains(activeChar))
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_C1).addString(_recieverName));
				return;
			}
		}
		else
		{
			recieverId = CharacterDAO.getInstance().getObjectIdByName(_recieverName);
			if(recieverId > 0 && mysql.simple_get_int("target_Id", "character_blocklist", "obj_Id=" + recieverId + " AND target_Id=" + activeChar.getObjectId()) > 0)
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_C1).addString(_recieverName));
				return;
			}
		}
		if(recieverId == 0)
		{
			activeChar.sendPacket(SystemMsg.WHEN_THE_RECIPIENT_DOESNT_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE);
			return;
		}

		int expireTime = (_messageType == 1 ? 12 : 360) * 3600 + (int) (System.currentTimeMillis() / 1000L);
		if(_count > 8)
		{
			activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		long serviceCost = 1 + _count;
        activeChar.getInventory().writeLock();
        List<ItemInstance> attachments = new ArrayList<>();
        try
		{
			if(activeChar.getAdena() < serviceCost)
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DONT_HAVE_ENOUGH_ADENA);
				return;
			}
			if(_count > 0)
				for(int j = 0; j < _count; ++j)
				{
					ItemInstance item2 = activeChar.getInventory().getItemByObjectId(_items[j]);
					if(item2 == null || item2.getCount() < _itemQ[j] || item2.getItemId() == 57 && item2.getCount() < _itemQ[j] + serviceCost || !item2.canBeTraded(activeChar))
					{
						activeChar.sendPacket(SystemMsg.THE_ITEM_THAT_YOURE_TRYING_TO_SEND_CANNOT_BE_FORWARDED_BECAUSE_IT_ISNT_PROPER);
						return;
					}
				}
			if(!activeChar.reduceAdena(serviceCost, true))
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DONT_HAVE_ENOUGH_ADENA);
				return;
			}
			if(_count > 0)
				for(int j = 0; j < _count; ++j)
				{
					ItemInstance item2 = activeChar.getInventory().removeItemByObjectId(_items[j], _itemQ[j]);

					ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.PostSend, item2);
					LogService.getInstance().log(LoggerType.ITEM, message);

					item2.setOwnerId(activeChar.getObjectId());
					item2.setLocation(ItemInstance.ItemLocation.MAIL);
					if(item2.getJdbcState().isSavable())
						item2.save();
					else
					{
						item2.setJdbcState(JdbcEntityState.UPDATED);
						item2.update();
					}
					attachments.add(item2);
				}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		boolean isSpam = SpamFilterManager.getInstance().isSpam(activeChar, _topic, SpamType.MAIL_TOPIC) || SpamFilterManager.getInstance().isSpam(activeChar, _topic, SpamType.MAIL_BODY);

		Mail mail = new Mail();
		mail.setSenderId(activeChar.getObjectId());
		mail.setSenderName(activeChar.getName());
		mail.setSenderHwid(activeChar.getHwidHolder().asString());
		mail.setReceiverId(recieverId);
		mail.setReceiverName(_recieverName);
		mail.setTopic(_topic);
		mail.setBody(_body);
		mail.setPrice(_messageType > 0 ? _price : 0L);
		mail.setUnread(true);
		mail.setType(Mail.SenderType.NORMAL);
		mail.setExpireTime(expireTime);
		for(ItemInstance item3 : attachments)
			mail.addAttachment(item3);
		mail.save();
		activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
		activeChar.sendPacket(SystemMsg.MAIL_SUCCESSFULLY_SENT);
		if(target != null)
		{
			target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			target.sendPacket(new ExUnReadMailCount(target));
			target.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
		}
	}
}
