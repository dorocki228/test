package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExRaidBossSpawnInfo implements IClientOutgoingPacket
{
	private final int[] _aliveBosses;

	public ExRaidBossSpawnInfo()
	{
		_aliveBosses = RaidBossSpawnManager.getInstance().getAliveRaidBosees();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RAID_BOSSPAWN_INFO.writeId(packetWriter);
		packetWriter.writeD(_aliveBosses.length);
		for(int bossId : _aliveBosses)
			packetWriter.writeD(bossId);

		return true;
	}
}