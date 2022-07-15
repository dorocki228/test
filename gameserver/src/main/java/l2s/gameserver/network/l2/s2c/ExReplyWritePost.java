package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.c2s.RequestSendPost;

/**
 * Запрос на отправку нового письма. Шлется в ответ на {@link RequestSendPost}.
 */
public class ExReplyWritePost implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC_TRUE = new ExReplyWritePost(1);
	public static final IClientOutgoingPacket STATIC_FALSE = new ExReplyWritePost(0);

	private int _reply;

	/**
	 * @param i если 1 окно создания письма закрывается
	 */
	public ExReplyWritePost(int i)
	{
		_reply = i;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_WRITE_POST.writeId(packetWriter);
		packetWriter.writeD(_reply); // 1 - закрыть окно письма, иное - не закрывать

		return true;
	}
}