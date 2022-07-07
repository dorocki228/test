package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.TimeStamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SkillCoolTimePacket extends L2GameServerPacket
{
	private List<Skill> _list;

	public SkillCoolTimePacket(Player player)
	{
		_list = Collections.emptyList();
		Collection<TimeStamp> list = player.getSkillReuses();
		_list = new ArrayList<>(list.size());
		for(TimeStamp stamp : list)
		{
			if(!stamp.hasNotPassed())
				continue;
			SkillEntry skillEntry = player.getKnownSkill(stamp.getId());
			if(skillEntry == null)
				continue;
			Skill sk = new Skill();
			sk.skillId = skillEntry.getId();
			sk.level = skillEntry.getLevel();
			sk.reuseBase = (int) Math.round(stamp.getReuseBasic() / 1000.0);
			sk.reuseCurrent = (int) Math.round(stamp.getReuseCurrent() / 1000.0);
			_list.add(sk);
		}
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_list.size());
		for(int i = 0; i < _list.size(); ++i)
		{
			Skill sk = _list.get(i);
            writeD(sk.skillId);
            writeD(sk.level);
            writeD(sk.reuseBase);
            writeD(sk.reuseCurrent);
		}
	}

	private static class Skill
	{
		public int skillId;
		public int level;
		public int reuseBase;
		public int reuseCurrent;
	}
}
