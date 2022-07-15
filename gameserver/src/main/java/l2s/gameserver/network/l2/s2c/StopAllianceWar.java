package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class StopAllianceWar implements IClientOutgoingPacket
{
	private String _allianceName;
	private String _char;

	public StopAllianceWar(String alliance, String charName)
	{
		_allianceName = alliance;
		_char = charName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.STOP_ALLIANCE_WAR.writeId(packetWriter);
		packetWriter.writeS(_allianceName);
		packetWriter.writeS(_char);

		return true;
	}
}