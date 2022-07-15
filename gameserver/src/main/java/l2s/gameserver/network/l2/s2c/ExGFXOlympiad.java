package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.olympiad.OlympiadManager;
import l2s.gameserver.model.entity.olympiad.OlympiadMember;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 0:50/09.04.2011
 */
public abstract class ExGFXOlympiad implements IClientOutgoingPacket
{
	public static class MatchList extends ExGFXOlympiad
	{
		private List<ArenaInfo> _arenaList = Collections.emptyList();

		public MatchList()
		{
			super(0);
			OlympiadManager manager = Olympiad._manager;
			if(manager != null)
			{
				_arenaList = new ArrayList<ArenaInfo>();
				for(int i = 0; i < Olympiad.STADIUMS.length; i++)
				{
					OlympiadGame game = manager.getOlympiadInstance(i);
					if(game != null && game.getState() > 0)
						_arenaList.add(new ArenaInfo(i, game.getState(), game.getType().ordinal(), game.getMemberName1(), game.getMemberName2()));
				}
			}
		}

		public MatchList(List<ArenaInfo> arenaList)
		{
			super(0);
			_arenaList = arenaList;
		}

		@Override
		public boolean write(l2s.commons.network.PacketWriter packetWriter)
		{
			super.write(packetWriter);
			packetWriter.writeD(_arenaList.size());
			packetWriter.writeD(0x00); //unknown
			for(ArenaInfo arena : _arenaList)
			{
				packetWriter.writeD(arena._id);
				packetWriter.writeD(arena._matchType);
				packetWriter.writeD(arena._status);
				packetWriter.writeS(arena._name1);
				packetWriter.writeS(arena._name2);
			}

			return true;
		}

		public static class ArenaInfo
		{
			public int _status;
			private int _id, _matchType;
			public String _name1, _name2;

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

	public static class MatchResult extends ExGFXOlympiad
	{
		private boolean _tie;
		private String _name;
		private List<PlayerInfo> _teamOne = new ArrayList<PlayerInfo>(3);
		private List<PlayerInfo> _teamTwo = new ArrayList<PlayerInfo>(3);

		public MatchResult(boolean tie, String winnerName)
		{
			super(1);
			_tie = tie;
			_name = winnerName;
		}

		public void addPlayer(TeamType team, OlympiadMember member, int gameResultPoints, int dealOutDamage)
		{
			int points = Config.OLYMPIAD_OLDSTYLE_STAT ? 0 : member.getStat().getPoints();

			addPlayer(team, member.getName(), member.getClanName(), member.getClassId(), points, gameResultPoints, dealOutDamage);
		}

		public void addPlayer(TeamType team, String name, String clanName, int classId, int points, int resultPoints, int damage)
		{
			switch (team)
			{
				case RED:
					_teamOne.add(new PlayerInfo(name, clanName, classId, points, resultPoints, damage));
					break;
				case BLUE:
					_teamTwo.add(new PlayerInfo(name, clanName, classId, points, resultPoints, damage));
					break;
			}
		}

		@Override
		public boolean write(l2s.commons.network.PacketWriter packetWriter)
		{
			super.write(packetWriter);
			packetWriter.writeD(_tie);
			packetWriter.writeS(_name);
			packetWriter.writeD(1);
			packetWriter.writeD(_teamOne.size());
			for(PlayerInfo playerInfo : _teamOne)
			{
				packetWriter.writeS(playerInfo._name);
				packetWriter.writeS(playerInfo._clanName);
				packetWriter.writeD(0x00); // pledge id
				packetWriter.writeD(playerInfo._classId);
				packetWriter.writeD(playerInfo._damage);
				packetWriter.writeD(playerInfo._currentPoints);
				packetWriter.writeD(playerInfo._gamePoints);
				packetWriter.writeD(0x00);//unk
			}
			packetWriter.writeD(2);
			packetWriter.writeD(_teamTwo.size());
			for(PlayerInfo playerInfo : _teamTwo)
			{
				packetWriter.writeS(playerInfo._name);
				packetWriter.writeS(playerInfo._clanName);
				packetWriter.writeD(0x00); // pledge id
				packetWriter.writeD(playerInfo._classId);
				packetWriter.writeD(playerInfo._damage);
				packetWriter.writeD(playerInfo._currentPoints);
				packetWriter.writeD(playerInfo._gamePoints);
				packetWriter.writeD(0x00);//unk
			}

			return true;
		}

		private static class PlayerInfo
		{
			private String _name, _clanName;
			private int _classId, _currentPoints, _gamePoints, _damage;

			public PlayerInfo(String name, String clanName, int classId, int currentPoints, int gamePoints, int damage)
			{
				_name = name;
				_clanName = clanName;
				_classId = classId;
				_currentPoints = currentPoints;
				_gamePoints = gamePoints;
				_damage = damage;
			}
		}
	}

	private int _type;

	public ExGFXOlympiad(int type)
	{
		_type = type;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_GFX_OLYMPIAD.writeId(packetWriter);
		packetWriter.writeD(_type);
		return true;
	}
}
