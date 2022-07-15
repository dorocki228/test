package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.c2s.RequestCancelPostAttachment;
import l2s.gameserver.network.l2.c2s.RequestSentPost;

/**
 * Просмотр собственного отправленного письма. Шлется в ответ на {@link RequestSentPost}.
 * При нажатии на кнопку Cancel клиент шлет {@link RequestCancelPostAttachment}.
 * @see ExReplyReceivedPost
 */
public class ExReplySentPost extends AbstractItemPacket
{
	private final Player player;
	private final Mail mail;

	public ExReplySentPost(Player player, Mail mail)
	{
		this.player = player;
		this.mail = mail;
	}

	// ddSSS dx[hddQdddhhhhhhhhhh] Qd
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_SENT_POST.writeId(packetWriter);
		packetWriter.writeD(mail.getType().ordinal());
		if(mail.getType() == Mail.SenderType.SYSTEM)
		{
			packetWriter.writeD(mail.getSystemParams()[0]);
			packetWriter.writeD(mail.getSystemParams()[1]);
			packetWriter.writeD(mail.getSystemParams()[2]);
			packetWriter.writeD(mail.getSystemParams()[3]);
			packetWriter.writeD(mail.getSystemParams()[4]);
			packetWriter.writeD(mail.getSystemParams()[5]);
			packetWriter.writeD(mail.getSystemParams()[6]);
			packetWriter.writeD(mail.getSystemParams()[7]);
			packetWriter.writeD(mail.getSystemTopic());
			packetWriter.writeD(mail.getSystemBody());
		}
		else if(mail.getType() == Mail.SenderType.UNKNOWN)
		{
			packetWriter.writeD(3492);
			packetWriter.writeD(3493);
		}

		packetWriter.writeD(mail.getMessageId()); // id письма
		packetWriter.writeD(mail.isPayOnDelivery() ? 1 : 0); // 1 - письмо с запросом оплаты, 0 - просто письмо

		packetWriter.writeS(mail.getReceiverName()); // кому
		packetWriter.writeS(mail.getTopic()); // топик
		packetWriter.writeS(mail.getBody()); // тело

		packetWriter.writeD(mail.getAttachments().size()); // количество приложенных вещей
		for(ItemInstance item : mail.getAttachments())
		{
			writeItem(packetWriter, player, item);
			packetWriter.writeD(item.getObjectId());
		}

		packetWriter.writeQ(mail.getPrice()); // для писем с оплатой - цена
		packetWriter.writeD(mail.getReceiverId()); // Не известно. В сниффе оффа значение 24225 (не равняется MessageId)

		return true;
	}
}