package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.c2s.RequestExDeleteReceivedPost;
import l2s.gameserver.network.l2.c2s.RequestExPostItemList;
import l2s.gameserver.network.l2.c2s.RequestExRequestReceivedPost;
import l2s.gameserver.network.l2.c2s.RequestExRequestReceivedPostList;

import java.util.Collections;
import java.util.List;

/**
 * Появляется при нажатии на кнопку "почта" или "received mail", входящие письма
 * <br> Ответ на {@link RequestExRequestReceivedPostList}.
 * <br> При нажатии на письмо в списке шлется {@link RequestExRequestReceivedPost} а в ответ {@link ExReplyReceivedPost}.
 * <br> При попытке удалить письмо шлется {@link RequestExDeleteReceivedPost}.
 * <br> При нажатии кнопки send mail шлется {@link RequestExPostItemList}.
 * @see ExShowSentPostList аналогичный список отправленной почты
 */
public class ExShowReceivedPostList extends L2GameServerPacket
{
	private final List<Mail> mails;

	public ExShowReceivedPostList(Player cha)
	{
		mails = MailDAO.getInstance().getReceivedMailByOwnerId(cha.getObjectId());
		Collections.sort(mails);
	}

	@Override
	protected void writeImpl()
	{
		writeD((int) (System.currentTimeMillis() / 1000L));
		writeD(mails.size());
		for(Mail mail : mails)
		{
			writeD(mail.getType().ordinal());
            if(mail.getType() == Mail.SenderType.SYSTEM)
                writeD(mail.getSystemTopic());
			writeD(mail.getMessageId());
			writeS(mail.getTopic());
			writeS(mail.getSenderName());
			writeD(mail.isPayOnDelivery() ? 1 : 0);
			writeD(mail.getExpireTime());
			writeD(mail.isUnread() ? 1 : 0);
			writeD(mail.isReturnable());
			writeD(mail.getAttachments().isEmpty() ? 0 : 1);
			writeD(mail.isReturned() ? 1 : 0);
			writeD(mail.getReceiverId());
		}
		writeD(100);
		writeD(1000);
	}
}
