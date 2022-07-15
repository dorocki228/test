package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * 		<packet id="FE;007E" name="ExPVPMatchRecord" extended="true">
 			<part name="state" type="d" /> <!--0 - start, 1 - update, 2 - finish-->
 			<part name="win-index" type="d" />
 			<part name="loose-index" type="d" />
 			<part name="blue_kills" type="d" />
 			<part name="red_kills" type="d" />
 			<part name="blue_size" type="d" id="0"/>
 			<for id="0">
 				<part name="name" type="S" />
 				<part name="kills" type="d" />
 				<part name="deaths" type="d" />
 			</for>
 			<part name="red_size" type="d" id="1"/>
 			<for id="1">
 				<part name="name" type="S" />
 				<part name="kills" type="d" />
 				<part name="deaths" type="d" />
 			</for>
 		</packet>
 */
public class ExPVPMatchRecord implements IClientOutgoingPacket
{
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

	public static final int START = 0;
	public static final int UPDATE = 1;
	public static final int FINISH = 2;

	private int _type;
	private TeamType _winnerTeam;
	private int _blueKills;
	private int _redKills;

	private List<Member> _blueList;
	private List<Member> _redList;

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
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PVPMATCH_RECORD.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeD(_winnerTeam.ordinal());
		packetWriter.writeD(_winnerTeam.revert().ordinal());
		packetWriter.writeD(_blueKills);
		packetWriter.writeD(_redKills);
		packetWriter.writeD(_blueList.size());
		for(Member member : _blueList)
		{
			packetWriter.writeS(member.name);
			packetWriter.writeD(member.kills);
			packetWriter.writeD(member.deaths);
		}
		packetWriter.writeD(_redList.size());
		for(Member member : _redList)
		{
			packetWriter.writeS(member.name);
			packetWriter.writeD(member.kills);
			packetWriter.writeD(member.deaths);
		}

		return true;
	}
}