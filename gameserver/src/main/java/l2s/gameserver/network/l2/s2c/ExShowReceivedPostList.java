package l2s.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Появляется при нажатии на кнопку "почта" или "received mail", входящие письма
 * <br> Ответ на {@link RequestExRequestReceivedPostList}.
 * <br> При нажатии на письмо в списке шлется {@link RequestExRequestReceivedPost} а в ответ {@link ExReplyReceivedPost}.
 * <br> При попытке удалить письмо шлется {@link RequestExDeleteReceivedPost}.
 * <br> При нажатии кнопки send mail шлется {@link RequestExPostItemList}.
 * @see ExShowSentPostList аналогичный список отправленной почты
 */
public class ExShowReceivedPostList implements IClientOutgoingPacket
{
	private final Mail[] _mails;

	public ExShowReceivedPostList(Player cha)
	{
		List<Mail> mails = MailDAO.getInstance().getReceivedMailByOwnerId(cha.getObjectId());
		Collections.sort(mails);
		_mails = mails.toArray(new Mail[mails.size()]);
	}

	// d dx[dSSddddddd]
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_RECEIVED_POST_LIST.writeId(packetWriter);
		packetWriter.writeD((int) (System.currentTimeMillis() / 1000L));
		packetWriter.writeD(_mails.length); // количество писем
		for(Mail mail : _mails)
		{
			packetWriter.writeD(mail.getType().ordinal()); // тип письма

			if(mail.getType() == Mail.SenderType.SYSTEM)
				packetWriter.writeD(mail.getSystemTopic());

			packetWriter.writeD(mail.getMessageId()); // уникальный id письма
			packetWriter.writeS(mail.getTopic()); // топик
			packetWriter.writeS(mail.getSenderName()); // отправитель
			packetWriter.writeD(mail.isPayOnDelivery() ? 1 : 0); // если тут 1 то письмо требует оплаты
			packetWriter.writeD(mail.getExpireTime()); // время действительности письма
			packetWriter.writeD(mail.isUnread() ? 1 : 0); // письмо не прочитано - его нельзя удалить и оно выделяется ярким цветом
			packetWriter.writeD(mail.isReturnable()); // returnable
			packetWriter.writeD(mail.getAttachments().isEmpty() ? 0 : 1); // 1 - письмо с приложением, 0 - просто письмо
			packetWriter.writeD(mail.isReturned() ? 1 : 0);
			packetWriter.writeD(mail.getReceiverId());
		}
		packetWriter.writeD(100);
		packetWriter.writeD(1000);

		return true;
	}
}