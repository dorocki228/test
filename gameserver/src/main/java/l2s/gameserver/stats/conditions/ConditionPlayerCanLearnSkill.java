package l2s.gameserver.stats.conditions;

import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;

public class ConditionPlayerCanLearnSkill extends Condition
{
	private static final AcquireType[] ACQUITE_TYPES_TO_CHECK = { AcquireType.NORMAL, AcquireType.GENERAL };

	private final int _id;
	private final int _level;

	public ConditionPlayerCanLearnSkill(int id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(_id, _level);
		if(skillEntry == null)
			return false;
		if(!creature.isPlayer())
			return false;
		Player player = creature.getPlayer();
		int skillLvl = skillEntry.getLevel();
		int haveSkillLvl = 0;
		SkillEntry knownSkillEntry = player.getKnownSkill(skillEntry.getId());
		if(knownSkillEntry != null)
		{
			haveSkillLvl = knownSkillEntry.getTemplate().getLevel();
			if(haveSkillLvl >= skillLvl)
				return false;
		}
		if(skillLvl > haveSkillLvl + 1)
			return false;
		for(AcquireType at : ACQUITE_TYPES_TO_CHECK)
			if(SkillAcquireHolder.getInstance().isSkillPossible(player, skillEntry, at))
			{
				SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, skillEntry.getId(), skillEntry.getLevel(), at);
				if(skillLearn != null)
					if(SkillAcquireHolder.getInstance().checkLearnCondition(player, skillLearn, player.getLevel()))
						return true;
			}
		return false;
	}
}
