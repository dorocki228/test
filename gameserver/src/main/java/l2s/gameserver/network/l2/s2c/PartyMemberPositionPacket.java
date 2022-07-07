package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

import java.util.HashMap;
import java.util.Map;

public class PartyMemberPositionPacket extends L2GameServerPacket
{
	private final Map<Integer, Location> positions;

	public PartyMemberPositionPacket()
	{
		positions = new HashMap<>();
	}

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
	protected final void writeImpl()
	{
        writeD(positions.size());
		for(Map.Entry<Integer, Location> e : positions.entrySet())
		{
            writeD(e.getKey());
            writeD(e.getValue().x);
            writeD(e.getValue().y);
            writeD(e.getValue().z);
		}
	}
}
