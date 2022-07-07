package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;

import java.util.Collection;

public class GMViewSkillInfoPacket extends L2GameServerPacket
{
	private final String _charName;
	private final Collection<SkillEntry> _skills;
	private final Player _targetChar;

	public GMViewSkillInfoPacket(Player cha)
	{
		_charName = cha.getName();
		_skills = cha.getAllSkills();
		_targetChar = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_charName);
        writeD(_skills.size());
		for(SkillEntry skillEntry : _skills)
		{
            writeD(skillEntry.getTemplate().isPassive() ? 1 : 0);
            writeD(skillEntry.getDisplayLevel());
            writeD(skillEntry.getId());
            writeD(skillEntry.getId());
            writeC(_targetChar.isUnActiveSkill(skillEntry.getId()) ? 1 : 0);
            writeC(SkillHolder.getInstance().getSkill(skillEntry.getId(), 1).getMaxLevel() > 100 ? 1 : 0);
		}
	}
}
