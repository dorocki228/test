package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.TeamType;

import java.util.List;

public class ExPVPMatchRecord extends L2GameServerPacket
{
	public static final int START = 0;
	public static final int UPDATE = 1;
	public static final int FINISH = 2;
	private final int _type;
	private final TeamType _winnerTeam;
	private final int _blueKills;
	private final int _redKills;
	private final List<Member> _blueList;
	private final List<Member> _redList;

	public ExPVPMatchRecord(int type, TeamType winnerTeam, int blueKills, int redKills, List<Member> blueTeam, List<Member> redTeam)
	{
		_type = type;
		_winnerTeam = winnerTeam;
		_blueKills = blueKills;
		_redKills = redKills;
		_blueList = blueTeam;
		_redList = redTeam;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_type);
        writeD(_winnerTeam.ordinal());
        writeD(_winnerTeam.revert().ordinal());
        writeD(_blueKills);
        writeD(_redKills);
        writeD(_blueList.size());
		for(Member member : _blueList)
		{
			writeS(member.name);
            writeD(member.kills);
            writeD(member.deaths);
		}
        writeD(_redList.size());
		for(Member member : _redList)
		{
			writeS(member.name);
            writeD(member.kills);
            writeD(member.deaths);
		}
	}

	public static class Member
	{
		public String name;
		public int kills;
		public int deaths;

		public Member(String name, int kills, int deaths)
		{
			this.name = name;
			this.kills = kills;
			this.deaths = deaths;
		}
	}
}
