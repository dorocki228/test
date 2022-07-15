package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExCastleState implements IClientOutgoingPacket
{
	private final int _id;
	private final ResidenceSide _side;

	public ExCastleState(Castle castle)
	{
		_id = castle.getId();
		_side = castle.getResidenceSide();
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CASTLE_STATE.writeId(packetWriter);
		packetWriter.writeD(_id);
		packetWriter.writeD(_side.ordinal());

		return true;
	}
}