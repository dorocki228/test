package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;

import java.util.ArrayList;
import java.util.List;

public class ExEventMatchTeamInfo extends L2GameServerPacket
{
	private final int leader_id;
	private final int loot;
	private final List<EventMatchTeamInfo> members;

	public ExEventMatchTeamInfo(List<Player> party, Player exclude)
	{
		members = new ArrayList<>();
		leader_id = party.get(0).getObjectId();
		loot = party.get(0).getParty().getLootDistribution();
		for(Player member : party)
			if(!member.equals(exclude))
				members.add(new EventMatchTeamInfo(member));
	}

	@Override
	protected void writeImpl()
	{}

	public static class EventMatchTeamInfo
	{
		public MathMember member;
		public List<MathMember> m_servitors;

		public EventMatchTeamInfo(Player player)
		{
			member = new MathMember();
			member.name = player.getName();
			member.objId = player.getObjectId();
			member.curCp = (int) player.getCurrentCp();
			member.maxCp = player.getMaxCp();
			member.curHp = (int) player.getCurrentHp();
			member.maxHp = player.getMaxHp();
			member.curMp = (int) player.getCurrentMp();
			member.maxMp = player.getMaxMp();
			member.level = player.getLevel();
			member.classId = player.getClassId().getId();
			member.raceId = player.getRace().ordinal();
			m_servitors = new ArrayList<>();
			for(Servitor s : player.getServitors())
			{
				MathMember m_servitor = new MathMember();
				m_servitor.name = s.getName();
				m_servitor.objId = s.getObjectId();
				m_servitor.npcId = s.getNpcId() + 1000000;
				m_servitor.curHp = (int) s.getCurrentHp();
				m_servitor.maxHp = s.getMaxHp();
				m_servitor.curMp = (int) s.getCurrentMp();
				m_servitor.maxMp = s.getMaxMp();
				m_servitor.level = s.getLevel();
				m_servitors.add(m_servitor);
			}
		}
	}

	public static class MathMember
	{
		public String name;
		public int objId;
		public int npcId;
		public int curCp;
		public int maxCp;
		public int curHp;
		public int maxHp;
		public int curMp;
		public int maxMp;
		public int level;
		public int classId;
		public int raceId;
	}
}
