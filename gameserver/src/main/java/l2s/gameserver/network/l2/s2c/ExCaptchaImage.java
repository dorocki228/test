package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExCaptchaImage implements IClientOutgoingPacket
{
	public ExCaptchaImage()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CAPTCHA_IMAGE.writeId(packetWriter);
		packetWriter.writeQ(0x00);	// TransactionID
		packetWriter.writeC(0x00);	// TryCount
		packetWriter.writeD(0x00);	// RemainTime

		return true;
	}
}