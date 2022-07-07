package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExShowReceivedPostList;

public class RequestExRejectPost extends L2GameClientPacket
{
	private int postId;

	@Override
	protected void readImpl()
	{
		postId = readD();
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
		if(activeChar.isInStoreMode() || activeChar.isPrivateBuffer())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_CANCEL_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_CANCEL_DURING_AN_EXCHANGE);
			return;
		}
		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_CANCEL_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			if(!mail.isReturnable())
			{
				activeChar.sendActionFailed();
				return;
			}
			int expireTime = 1296000 + (int) (System.currentTimeMillis() / 1000L);
			Mail reject = mail.reject();
			mail.delete();
			reject.setExpireTime(expireTime);
			reject.save();
			Player sender = GameObjectsStorage.getPlayer(reject.getReceiverId());
			if(sender != null)
				sender.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		}
		activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
	}
}
