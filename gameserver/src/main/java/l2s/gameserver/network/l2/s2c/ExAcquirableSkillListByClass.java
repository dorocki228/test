package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.ArrayList;
import java.util.List;

/**
 * Reworked: VISTALL
 */
public class ExAcquirableSkillListByClass implements IClientOutgoingPacket
{
	private AcquireType _type;
	private final List<Skill> _skills;

	static class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int cost;
		public int requirements;
		public int subUnit;

		Skill(int id, int nextLevel, int maxLevel, int cost, int requirements, int subUnit)
		{
			this.id = id;
			this.nextLevel = nextLevel;
			this.maxLevel = maxLevel;
			this.cost = cost;
			this.requirements = requirements;
			this.subUnit = subUnit;
		}
	}

	public ExAcquirableSkillListByClass(AcquireType type, int size)
	{
		_skills = new ArrayList<Skill>(size);
		_type = type;
	}

	public void addSkill(int id, int nextLevel, int maxLevel, int Cost, int requirements, int subUnit)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, Cost, requirements, subUnit));
	}

	public void addSkill(int id, int nextLevel, int maxLevel, int Cost, int requirements)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, Cost, requirements, 0));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ACQUIRABLE_SKILL_LIST_BY_CLASS.writeId(packetWriter);
		packetWriter.writeH(_type.getId());
		packetWriter.writeH(_skills.size());

		for(Skill temp : _skills)
		{
			packetWriter.writeD(temp.id);
			packetWriter.writeH(temp.nextLevel);
			packetWriter.writeH(temp.maxLevel);
			packetWriter.writeC(temp.requirements);
			packetWriter.writeQ(temp.cost);
			packetWriter.writeC(0x01); // UNK
			if(_type == AcquireType.SUB_UNIT)
				packetWriter.writeH(temp.subUnit);
		}

		return true;
	}
}