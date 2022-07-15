package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExResponseCrystalItemInfo implements IClientOutgoingPacket
{
	private final int _crystalId;
	private final long _crystalCount;

	public ExResponseCrystalItemInfo(ItemInstance item)
	{
		_crystalId = item.getGrade().getCrystalId();
		_crystalCount = item.getCrystalCountOnCrystallize();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESPONSE_CRYSTALITEM_INFO.writeId(packetWriter);
		if(_crystalId > 0 && _crystalCount > 0)
		{
			packetWriter.writeD(0x01);
			packetWriter.writeD(_crystalId);
			packetWriter.writeQ(_crystalCount);
			packetWriter.writeF(100.);
		}
		else
			packetWriter.writeD(0x00);

		return true;
	}
}