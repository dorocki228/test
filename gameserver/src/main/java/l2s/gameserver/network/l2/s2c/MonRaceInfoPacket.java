package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public class MonRaceInfoPacket implements IClientOutgoingPacket
{
	private int _unknown1;
	private int _unknown2;
	private NpcInstance[] _monsters;
	private int[][] _speeds;

	public MonRaceInfoPacket(int unknown1, int unknown2, NpcInstance[] monsters, int[][] speeds)
	{
		/*
		 * -1 0 to initial the race
		 * 0 15322 to start race
		 * 13765 -1 in middle of race
		 * -1 0 to end the race
		 */
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MON_RACE_INFO.writeId(packetWriter);
		packetWriter.writeD(_unknown1);
		packetWriter.writeD(_unknown2);
		packetWriter.writeD(8);

		for(int i = 0; i < 8; i++)
		{
			//_log.info.println("MOnster "+(i+1)+" npcid "+_monsters[i].getNpcTemplate().getNpcId());
			packetWriter.writeD(_monsters[i].getObjectId()); //npcObjectID
			packetWriter.writeD(_monsters[i].getNpcId() + 1000000); //npcID
			packetWriter.writeD(14107); //origin X
			packetWriter.writeD(181875 + 58 * (7 - i)); //origin Y
			packetWriter.writeD(-3566); //origin Z
			packetWriter.writeD(12080); //end X
			packetWriter.writeD(181875 + 58 * (7 - i)); //end Y
			packetWriter.writeD(-3566); //end Z
			packetWriter.writeF(_monsters[i].getCurrentCollisionHeight()); //coll. height
			packetWriter.writeF(_monsters[i].getCurrentCollisionRadius()); //coll. radius
			packetWriter.writeD(120); // ?? unknown
			for(int j = 0; j < 20; j++)
				packetWriter.writeC(_unknown1 == 0 ? _speeds[i][j] : 0);
			packetWriter.writeD(0);
			packetWriter.writeD(0x00); // ? GraciaFinal
		}

		return true;
	}
}