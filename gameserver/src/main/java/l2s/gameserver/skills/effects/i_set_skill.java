package l2s.gameserver.skills.effects;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_set_skill extends i_abstract_effect
{
	private final SkillEntry _skill;

	public i_set_skill(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		int[] temp = template.getParam().getIntegerArray("skill", "-");
		_skill = SkillHolder.getInstance().getSkillEntry(temp[0], temp.length >= 2 ? temp[1] : 1);
	}

	@Override
	public boolean checkCondition()
	{
		return _skill != null && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		Player player = getEffected().getPlayer();
		player.addSkill(_skill, true);
		player.updateStats();
		player.sendSkillList();
		player.updateSkillShortcuts(_skill.getId(), _skill.getLevel());
	}
}
