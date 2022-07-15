package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.s2c.ExReplySentPost;
import l2s.gameserver.network.l2.s2c.ExShowSentPostList;

/**
 * Запрос информации об отправленном письме. Появляется при нажатии на письмо из списка {@link ExShowSentPostList}.
 * В ответ шлется {@link ExReplySentPost}.
 * @see RequestReceivedPost
 */
public class RequestSentPost implements IClientIncomingPacket
{
	private int postId;

	/**
	 * format: d
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		postId = packet.readD(); // id письма
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		Mail mail = MailDAO.getInstance().getSentMailByMailId(player.getObjectId(), postId);
		if(mail != null)
		{
			player.sendPacket(new ExReplySentPost(player, mail));
			return;
		}

		player.sendPacket(new ExShowSentPostList(player));
	}
}