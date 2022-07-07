package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

import java.util.Collections;
import java.util.Map;

public class PackageToListPacket extends L2GameServerPacket
{
	private Map<Integer, String> _characters;

	public PackageToListPacket(Player player)
	{
		_characters = Collections.emptyMap();
		_characters = player.getAccountChars();
	}

	@Override
	protected void writeImpl()
	{
        writeD(_characters.size());
		for(Map.Entry<Integer, String> entry : _characters.entrySet())
		{
            writeD(entry.getKey());
			writeS(entry.getValue());
		}
	}
}
