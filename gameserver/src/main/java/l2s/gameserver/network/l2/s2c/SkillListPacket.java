package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.skills.SkillEntry;

import java.util.Collection;

/**
 * format   d (dddc)
			d  dddcc
 */
public class SkillListPacket implements IClientOutgoingPacket
{
	private final Collection<SkillEntry> _skills;
	private final Player _player;
	private final int _learnedSkillId;

	public SkillListPacket(Player player)
	{
		_skills = player.getAllSkills();
		_player = player;
		_learnedSkillId = 0;
	}

	public SkillListPacket(Player player, int learnedSkillId)
	{
		_skills = player.getAllSkills();
		_player = player;
		_learnedSkillId = learnedSkillId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SKILL_LIST.writeId(packetWriter);
		packetWriter.writeD(_skills.size());
		for(SkillEntry skillEntry : _skills)
		{
			Skill temp = skillEntry.getTemplate();
			packetWriter.writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			packetWriter.writeD(temp.getDisplayLevel());
			packetWriter.writeD(temp.getDisplayId());
			packetWriter.writeD(temp.getReuseSkillId());
			packetWriter.writeC(_player.isUnActiveSkill(temp.getId()) ? 0x01 : 0x00); // иконка скилла серая если не 0
			packetWriter.writeC(0x00); // TODO для заточки: если 1 скилл можно точить
		}
		packetWriter.writeD(_learnedSkillId);

		return true;
	}
}