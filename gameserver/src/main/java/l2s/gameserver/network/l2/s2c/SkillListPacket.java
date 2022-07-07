package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.SkillEntry;

import java.util.ArrayList;
import java.util.List;

public class SkillListPacket extends L2GameServerPacket
{
	private final List<SkillEntry> _skills;
	private final Player _player;
	private final int _learnedSkillId;

	public SkillListPacket(Player player)
	{
		_skills = new ArrayList<>(player.getAllSkills());
		_player = player;
		_learnedSkillId = 0;
	}

	public SkillListPacket(Player player, int learnedSkillId)
	{
		_skills = new ArrayList<>(player.getAllSkills());
		_player = player;
		_learnedSkillId = learnedSkillId;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_skills.size());
		for(SkillEntry skillEntry : _skills)
		{
			Skill temp = skillEntry.getTemplate();
            writeD(!temp.isActive() && !temp.isToggle() ? 1 : 0);
            writeD(temp.getDisplayLevel());
            writeD(temp.getDisplayId());
            writeD(temp.getReuseSkillId());
            writeC(_player.isUnActiveSkill(temp.getId()) ? 1 : 0);
            writeC(0);
		}
        writeD(_learnedSkillId);
	}
}
