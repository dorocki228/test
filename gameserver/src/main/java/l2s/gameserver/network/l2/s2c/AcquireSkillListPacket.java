package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;

import java.util.Collection;

public class AcquireSkillListPacket extends L2GameServerPacket
{
	private final Collection<SkillLearn> _skills;

	public AcquireSkillListPacket(Player player)
	{
		_skills = SkillAcquireHolder.getInstance().getAcquirableSkillListByClass(player);
	}

	public AcquireSkillListPacket(Collection<SkillLearn> skills)
	{
		_skills = skills;
	}

	@Override
	protected void writeImpl()
	{
		writeH(_skills.size());
		for(SkillLearn sk : _skills)
		{
			Skill skill = SkillHolder.getInstance().getSkill(sk.getId(), sk.getLevel());
			if(skill == null)
				continue;

			writeD(sk.getId());
			writeH(sk.getLevel());
			writeQ(sk.getCost());
			writeC(sk.getMinLevel());
			writeC(0);
			if(sk.getItemId() > 0 && sk.getItemCount() > 0L)
			{
				writeC(1);
				writeD(sk.getItemId());
				writeQ(sk.getItemCount());
			}
			else
				writeC(0);
			writeC(0);
		}
	}
}
