package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.olympiad.OlympiadManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExOlympiadMatchList extends L2GameServerPacket
{
	private List<ArenaInfo> _arenaList = Collections.emptyList();

	public ExOlympiadMatchList(Player player)
	{
		OlympiadManager manager = Olympiad._manager;
		if(manager != null)
		{
			_arenaList = new ArrayList<>();
			for(int i = 0; i < Olympiad.STADIUMS.length; ++i)
			{
				OlympiadGame game = manager.getOlympiadInstance(i);
				if(game == null || game.getState() <= 0)
					continue;
				_arenaList.add(new ArenaInfo(i, game.getState(), game.getType().ordinal(),
						game.getMember1DisplayedName(player), game.getMember2DisplayedName(player)));
			}
		}
	}

	public ExOlympiadMatchList(List<ArenaInfo> arenaList)
	{
		_arenaList = arenaList;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x00);
		writeD(_arenaList.size());
		writeD(0);
		for(ArenaInfo arena : _arenaList)
		{
			writeD(arena._id);
			writeD(arena._matchType);
			writeD(arena._status);
			writeS(arena._name1);
			writeS(arena._name2);
		}
	}

	public static class ArenaInfo
	{
		private final int _status;
		private final String _name1;
		private final String _name2;
		private final int _id;
		private final int _matchType;

		public ArenaInfo(int id, int status, int match_type, String name1, String name2)
		{
			_id = id;
			_status = status;
			_matchType = match_type;
			_name1 = name1;
			_name2 = name2;
		}
	}
}
