package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.c2s.RequestPostAttachment;
import l2s.gameserver.network.l2.c2s.RequestReceivedPost;
import l2s.gameserver.network.l2.c2s.RequestRejectPostAttachment;

/**
 * Просмотр полученного письма. Шлется в ответ на {@link RequestReceivedPost}.
 * При попытке забрать приложенные вещи клиент шлет {@link RequestPostAttachment}.
 * При возврате письма клиент шлет {@link RequestRejectPostAttachment}.
 * @see ExReplySentPost
 */
public class ExReplyReceivedPost extends AbstractItemPacket
{
	private final Player player;
	private final Mail mail;

	public ExReplyReceivedPost(Player player, Mail mail)
	{
		this.player = player;
		this.mail = mail;
	}

	// dddSSS dx[hddQdddhhhhhhhhhh] Qdd
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_RECEIVED_POST.writeId(packetWriter);
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

		packetWriter.writeD(mail.isPayOnDelivery() ? 0x01 : 0x00); // Платное письмо или нет
		packetWriter.writeD(mail.isReturned() ? 0x01 : 0x00);// unknown3

		packetWriter.writeS(mail.getSenderName()); // от кого
		packetWriter.writeS(mail.getTopic()); // топик
		packetWriter.writeS(mail.getBody()); // тело

		packetWriter.writeD(mail.getAttachments().size()); // количество приложенных вещей
		for(ItemInstance item : mail.getAttachments())
		{
			writeItem(packetWriter, player, item);
			packetWriter.writeD(item.getObjectId());
		}

		packetWriter.writeQ(mail.getPrice()); // для писем с оплатой - цена
		packetWriter.writeD(mail.isReturnable());
		packetWriter.writeD(mail.getReceiverId()); // Не известно. В сниффе оффа значение 24225 (не равняется MessageId)

		return true;
	}
}