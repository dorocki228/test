package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class StartAllianceWar implements IClientOutgoingPacket
{
	private String _allianceName;
	private String _char;

	public StartAllianceWar(String alliance, String charName)
	{
		_allianceName = alliance;
		_char = charName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.START_ALLIANCE_WAR.writeId(packetWriter);
		packetWriter.writeS(_char);
		packetWriter.writeS(_allianceName);

		return true;
	}
}