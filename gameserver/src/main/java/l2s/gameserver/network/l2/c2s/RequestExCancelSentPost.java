package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowSentPostList;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

import java.util.Set;

public class RequestExCancelSentPost extends L2GameClientPacket
{
	private int postId;

	protected void readImpl()
	{
		postId = readD();
	}

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

		Mail mail = MailDAO.getInstance().getSentMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			if(mail.getAttachments().isEmpty())
			{
				activeChar.sendActionFailed();
				return;
			}

			activeChar.getInventory().writeLock();
			try
			{
				int slots = 0;
				long weight = 0L;
				for(ItemInstance item : mail.getAttachments())
				{
					weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getTemplate().getWeight()));
					if(!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
						slots++;
				}

				if(!activeChar.getInventory().validateWeight(weight))
				{
					activeChar.sendPacket(SystemMsg.YOU_COULD_NOT_CANCEL_RECEIPT_BECAUSE_YOUR_INVENTORY_IS_FULL);
					return;
				}

				if(!activeChar.getInventory().validateCapacity(slots))
				{
					activeChar.sendPacket(SystemMsg.YOU_COULD_NOT_CANCEL_RECEIPT_BECAUSE_YOUR_INVENTORY_IS_FULL);
					return;
				}

				Set<ItemInstance> attachments = mail.getAttachments();
				ItemInstance[] items;
				synchronized (attachments)
				{
					items = mail.getAttachments().toArray(new ItemInstance[attachments.size()]);
					attachments.clear();
				}

				for(ItemInstance item : items)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_ACQUIRED_S2_S1).addItemName(item.getItemId()).addNumber(item.getCount()));

					ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.PostCancel, item);
					LogService.getInstance().log(LoggerType.ITEM, message);

					activeChar.getInventory().addItem(item);
				}

				mail.delete();

				activeChar.sendPacket(SystemMsg.MAIL_SUCCESSFULLY_CANCELLED);
			}
			catch(ArithmeticException localArithmeticException)
			{}
			finally
			{
				activeChar.getInventory().writeUnlock();
			}
		}
		activeChar.sendPacket(new ExShowSentPostList(activeChar));
	}
}
