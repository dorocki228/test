package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExCubeGameChangeTeam extends L2GameServerPacket
{
	private final int _objectId;
	private final boolean _fromRedTeam;

	public ExCubeGameChangeTeam(Player player, boolean fromRedTeam)
	{
		_objectId = player.getObjectId();
		_fromRedTeam = fromRedTeam;
	}

	@Override
	protected void writeImpl()
	{
        writeD(5);
        writeD(_objectId);
        writeD(_fromRedTeam ? 1 : 0);
        writeD(_fromRedTeam ? 0 : 1);
	}
}
