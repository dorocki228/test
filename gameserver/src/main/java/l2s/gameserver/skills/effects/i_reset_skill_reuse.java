package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_reset_skill_reuse extends i_abstract_effect
{
	private final int _skillId;

	public i_reset_skill_reuse(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_skillId = template.getParam().getInteger("id");
	}

	@Override
	public void instantUse()
	{
		SkillEntry skill = getEffected().getKnownSkill(_skillId);
		if(skill != null)
		{
			getEffected().enableSkill(skill.getTemplate());
			if(getEffected().isPlayer())
			{
				Player player = getEffected().getPlayer();
				player.sendPacket(new SkillCoolTimePacket(player));
			}
		}
	}
}
