package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class StartPledgeWar implements IClientOutgoingPacket
{
	private String _pledgeName;
	private String _char;

	public StartPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.START_PLEDGE_WAR.writeId(packetWriter);
		packetWriter.writeS(_char);
		packetWriter.writeS(_pledgeName);

		return true;
	}
}