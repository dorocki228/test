package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExSubPledgeUpdated implements IClientOutgoingPacket
{
	private int type;
	private String _name, leader_name;

	public ExSubPledgeUpdated(SubUnit subPledge)
	{
		type = subPledge.getType();
		_name = subPledge.getName();
		leader_name = subPledge.getLeaderName();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SUBPLEDGE_UPDATED.writeId(packetWriter);
		packetWriter.writeD(0x01);
		packetWriter.writeD(type);
		packetWriter.writeS(_name);
		packetWriter.writeS(leader_name);

		return true;
	}
}