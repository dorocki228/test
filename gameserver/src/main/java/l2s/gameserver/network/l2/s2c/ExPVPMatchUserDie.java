package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExPVPMatchUserDie implements IClientOutgoingPacket
{
	private int _blueKills, _redKills;

	public ExPVPMatchUserDie(int blueKills, int redKills)
	{
		_blueKills = blueKills;
		_redKills = redKills;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PVPMATCH_USER_DIE.writeId(packetWriter);
		packetWriter.writeD(_blueKills);
		packetWriter.writeD(_redKills);

		return true;
	}
}