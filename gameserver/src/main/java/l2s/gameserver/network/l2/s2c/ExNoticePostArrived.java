package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.c2s.RequestReceivedPostList;

/**
 * Уведомление о получении почты. При нажатии на него клиент отправляет {@link RequestReceivedPostList}.
 */
public class ExNoticePostArrived implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC_TRUE = new ExNoticePostArrived(1);
	public static final IClientOutgoingPacket STATIC_FALSE = new ExNoticePostArrived(0);

	private int _anim;

	public ExNoticePostArrived(int useAnim)
	{
		_anim = useAnim;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_NOTICE_POST_ARRIVED.writeId(packetWriter);
		packetWriter.writeD(_anim); // 0 - просто показать уведомление, 1 - с красивой анимацией

		return true;
	}
}