package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class SurrenderPledgeWar implements IClientOutgoingPacket
{
	private String _pledgeName;
	private String _char;

	public SurrenderPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SURRENDER_PLEDGE_WAR.writeId(packetWriter);
		packetWriter.writeS(_pledgeName);
		packetWriter.writeS(_char);

		return true;
	}
}