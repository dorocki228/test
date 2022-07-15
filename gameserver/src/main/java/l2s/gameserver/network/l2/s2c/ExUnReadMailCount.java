package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExUnReadMailCount implements IClientOutgoingPacket
{
	private final int _count;

	public ExUnReadMailCount(Player player)
	{
		int count = 0;
		List<Mail> mails = MailDAO.getInstance().getReceivedMailByOwnerId(player.getObjectId());
		for(Mail mail : mails)
		{
			if(mail.isUnread())
				count++;
		}
		_count = count;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_UNREADMAIL_COUNT.writeId(packetWriter);
		packetWriter.writeD(_count);

		return true;
	}
}