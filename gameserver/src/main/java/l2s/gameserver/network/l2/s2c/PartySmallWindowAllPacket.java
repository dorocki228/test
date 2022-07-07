package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PartySmallWindowAllPacket extends L2GameServerPacket
{
	private final int leaderId;
	private final int loot;
	private final List<PartySmallWindowMemberInfo> members;

	public PartySmallWindowAllPacket(Party party, Player exclude)
	{
		Player partyLeader = party.getPartyLeader();
		leaderId = partyLeader == null ? 0 : partyLeader.getObjectId();
		loot = party.getLootDistribution();
		members = party.getPartyMembers().stream()
				.filter(member -> !member.equals(exclude))
				.map(PartySmallWindowMemberInfo::new)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	protected final void writeImpl()
	{
        writeD(leaderId);
        writeC(loot);
        writeC(members.size());
		for(PartySmallWindowMemberInfo mi : members)
		{
            writeD(mi.member.objId);
			writeS(mi.member.name);
            writeD(mi.member.curCp);
            writeD(mi.member.maxCp);
            writeD(mi.member.curHp);
            writeD(mi.member.maxHp);
            writeD(mi.member.curMp);
            writeD(mi.member.maxMp);
            writeD(0);
            writeC(mi.member.level);
            writeH(mi.member.classId);
            writeC(mi.member.sex);
            writeH(mi.member.raceId);
            writeD(mi.m_servitors.size());
			for(PartyMember servitor : mi.m_servitors)
			{
                writeD(servitor.objId);
                writeD(servitor.npcId);
                writeC(servitor.type);
				writeS(servitor.name);
                writeD(servitor.curHp);
                writeD(servitor.maxHp);
                writeD(servitor.curMp);
                writeD(servitor.maxMp);
                writeC(servitor.level);
			}
		}
	}

	public static class PartySmallWindowMemberInfo
	{
		public PartyMember member;
		public List<PartyMember> m_servitors;

		public PartySmallWindowMemberInfo(Player player)
		{
			member = new PartyMember();
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
			member.sex = player.getSex().ordinal();
			member.isPartySubstituteStarted = player.isPartySubstituteStarted() ? 1 : 0;
			m_servitors = new ArrayList<>();
			for(Servitor s : player.getServitors())
			{
				PartyMember m_servitor = new PartyMember();
				m_servitor.name = s.getName();
				m_servitor.objId = s.getObjectId();
				m_servitor.npcId = s.getNpcId() + 1000000;
				m_servitor.curHp = (int) s.getCurrentHp();
				m_servitor.maxHp = s.getMaxHp();
				m_servitor.curMp = (int) s.getCurrentMp();
				m_servitor.maxMp = s.getMaxMp();
				m_servitor.level = s.getLevel();
				m_servitor.type = s.getServitorType();
				m_servitors.add(m_servitor);
			}
		}
	}

	public static class PartyMember
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
		public int type;
		public int sex;
		public int isPartySubstituteStarted;
	}
}
