package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExUpdatePledgePower implements IClientOutgoingPacket
{
	private int _privs;

	public ExUpdatePledgePower(int privs)
	{
		_privs = privs;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_UPDATE_PLEDGE_POWER.writeId(packetWriter);
		packetWriter.writeD(_privs); //Filler??????

		return true;
	}
}