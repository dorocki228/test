package l2s.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PartyMemberPositionPacket implements IClientOutgoingPacket
{
	private final Map<Integer, Location> positions = new HashMap<Integer, Location>();

	public PartyMemberPositionPacket add(Player actor)
	{
		positions.put(actor.getObjectId(), actor.getLoc());
		return this;
	}

	public int size()
	{
		return positions.size();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PARTY_MEMBER_POSITION.writeId(packetWriter);
		packetWriter.writeD(positions.size());
		for(Map.Entry<Integer, Location> e : positions.entrySet())
		{
			packetWriter.writeD(e.getKey());
			packetWriter.writeD(e.getValue().x);
			packetWriter.writeD(e.getValue().y);
			packetWriter.writeD(e.getValue().z);
		}

		return true;
	}
}