package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.skills.SkillEntry;

public class GMViewSkillInfoPacket implements IClientOutgoingPacket
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
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GM_VIEW_SKILL_INFO.writeId(packetWriter);
		packetWriter.writeS(_charName);
		packetWriter.writeD(_skills.size());
		for(SkillEntry skillEntry : _skills)
		{
			Skill temp = skillEntry.getTemplate();
			packetWriter.writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			packetWriter.writeD(temp.getDisplayLevel());
			packetWriter.writeD(temp.getDisplayId());
			packetWriter.writeD(temp.getReuseSkillId());
			packetWriter.writeC(_targetChar.isUnActiveSkill(temp.getId()) ? 0x01 : 0x00);
			packetWriter.writeC(0x00); // Enchantable
		}
		packetWriter.writeD(0x00);

		return true;
	}
}