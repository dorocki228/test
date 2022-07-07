package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.ServerPacketOpcodes;

import java.util.List;

public abstract class ExBlockUpSetList extends L2GameServerPacket
{
	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.ExBlockUpSetList;
	}

	public static class TeamList extends ExBlockUpSetList
	{
		private final List<Player> _bluePlayers;
		private final List<Player> _redPlayers;
		private final int _roomNumber;

		public TeamList(List<Player> redPlayers, List<Player> bluePlayers, int roomNumber)
		{
			_redPlayers = redPlayers;
			_bluePlayers = bluePlayers;
			_roomNumber = roomNumber - 1;
		}

		@Override
		protected void writeImpl()
		{
            writeD(0);
            writeD(_roomNumber);
            writeD(-1);
            writeD(_bluePlayers.size());
			for(Player player : _bluePlayers)
			{
                writeD(player.getObjectId());
				writeS(player.getName());
			}
            writeD(_redPlayers.size());
			for(Player player : _redPlayers)
			{
                writeD(player.getObjectId());
				writeS(player.getName());
			}
		}
	}

	public static class AddPlayer extends ExBlockUpSetList
	{
		private final int _objectId;
		private final String _name;
		private final boolean _isRedTeam;

		public AddPlayer(Player player, boolean isRedTeam)
		{
			_objectId = player.getObjectId();
			_name = player.getName();
			_isRedTeam = isRedTeam;
		}

		@Override
		protected void writeImpl()
		{
            writeD(1);
            writeD(-1);
            writeD(_isRedTeam ? 1 : 0);
            writeD(_objectId);
			writeS(_name);
		}
	}

	public static class RemovePlayer extends ExBlockUpSetList
	{
		private final int _objectId;
		private final boolean _isRedTeam;

		public RemovePlayer(Player player, boolean isRedTeam)
		{
			_objectId = player.getObjectId();
			_isRedTeam = isRedTeam;
		}

		@Override
		protected void writeImpl()
		{
            writeD(2);
            writeD(-1);
            writeD(_isRedTeam ? 1 : 0);
            writeD(_objectId);
		}
	}

	public static class CloseUI extends ExBlockUpSetList
	{
		@Override
		protected void writeImpl()
		{
            writeD(-1);
		}
	}
}
