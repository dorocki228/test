package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.s2c.ExShowSentPostList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

public class RequestExDeleteSentPost extends L2GameClientPacket
{
	private int _count;
	private int[] _list;

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 4 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_list = new int[_count];
		for(int i = 0; i < _count; ++i)
			_list[i] = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;
		Collection<Mail> mails = MailDAO.getInstance().getSentMailByOwnerId(activeChar.getObjectId());
		if(!mails.isEmpty())
			for(Mail mail : mails)
				if(ArrayUtils.contains(_list, mail.getMessageId()) && mail.getAttachments().isEmpty())
					MailDAO.getInstance().deleteSentMailByMailId(activeChar.getObjectId(), mail.getMessageId());
		activeChar.sendPacket(new ExShowSentPostList(activeChar));
	}
}
