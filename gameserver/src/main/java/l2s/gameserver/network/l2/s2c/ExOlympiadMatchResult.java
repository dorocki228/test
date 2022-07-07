package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.olympiad.OlympiadMember;

import java.util.ArrayList;
import java.util.List;

public class ExOlympiadMatchResult extends L2GameServerPacket
{
	private final boolean _tie;
	private final String _name;
	private final List<PlayerInfo> _teamOne = new ArrayList<>(3);
	private final List<PlayerInfo> _teamTwo = new ArrayList<>(3);

	public ExOlympiadMatchResult(boolean tie, String winnerName)
	{
		_tie = tie;
		_name = winnerName;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x01);
		writeD(_tie);
		writeS(_name);
		writeD(1);
		writeD(_teamOne.size());
		for(PlayerInfo playerInfo : _teamOne)
		{
			writeS(playerInfo._name);
			writeS(playerInfo._clanName);
			writeD(0);
			writeD(playerInfo._classId);
			writeD(playerInfo._damage);
			writeD(playerInfo._currentPoints);
			writeD(playerInfo._gamePoints);
			writeD(0);
		}
		writeD(2);
		writeD(_teamTwo.size());
		for(PlayerInfo playerInfo : _teamTwo)
		{
			writeS(playerInfo._name);
			writeS(playerInfo._clanName);
			writeD(0);
			writeD(playerInfo._classId);
			writeD(playerInfo._damage);
			writeD(playerInfo._currentPoints);
			writeD(playerInfo._gamePoints);
			writeD(0);
		}
	}

	public void addPlayer(TeamType team, OlympiadMember member, int gameResultPoints, int dealOutDamage)
	{
		int points = Config.OLYMPIAD_OLDSTYLE_STAT ? 0 : member.getStat().getPoints();
		addPlayer(team, member.getName(), member.getClanName(), member.getClassId(), points, gameResultPoints, dealOutDamage);
	}

	public void addPlayer(TeamType team, String name, String clanName, int classId, int points, int resultPoints, int damage)
	{
		switch(team)
		{
			case RED:
			{
				_teamOne.add(new PlayerInfo(name, clanName, classId, points, resultPoints, damage));
				break;
			}
			case BLUE:
			{
				_teamTwo.add(new PlayerInfo(name, clanName, classId, points, resultPoints, damage));
			}
		}
	}

	private static class PlayerInfo
	{
		private final String _name;
		private final String _clanName;
		private final int _classId;
		private final int _currentPoints;
		private final int _gamePoints;
		private final int _damage;

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
