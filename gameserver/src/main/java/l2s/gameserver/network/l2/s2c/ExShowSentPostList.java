package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;

import java.util.Collections;
import java.util.List;

public class ExShowSentPostList extends L2GameServerPacket
{
	private final List<Mail> mails;
	private final Player player;

	public ExShowSentPostList(Player cha)
	{
		Collections.sort(mails = MailDAO.getInstance().getSentMailByOwnerId(cha.getObjectId()));
		player = cha;
	}

	@Override
	protected void writeImpl()
	{
		writeD((int) (System.currentTimeMillis() / 1000L));
		writeD(mails.size());
		for(Mail mail : mails)
		{
			writeD(mail.getMessageId());
			writeS(mail.getTopic());
			writeS(mail.getReceiverName());
			writeD(mail.isPayOnDelivery() ? 1 : 0);
			writeD(mail.getExpireTime());
			writeD(mail.isUnread() ? 1 : 0);
			writeD(mail.isReturnable());
			writeD(mail.getAttachments().isEmpty() ? 0 : 1);
			writeD(0);
		}
	}
}
