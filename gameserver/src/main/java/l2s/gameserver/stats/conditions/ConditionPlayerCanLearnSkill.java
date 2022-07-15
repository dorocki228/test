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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
 */
public class ConditionPlayerCanLearnSkill extends Condition
{
	private static final AcquireType[] ACQUITE_TYPES_TO_CHECK = { AcquireType.NORMAL, AcquireType.FISHING, AcquireType.GENERAL, AcquireType.HERO };

	private final int _id;
	private final int _level;

	public ConditionPlayerCanLearnSkill(int id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		Skill skillToLearn = SkillHolder.getInstance().getSkill(_id, _level);
		if(skillToLearn == null)
			return false;

		if(!actor.isPlayer())
			return false;

		Player player = actor.getPlayer();

		int skillLvl = skillToLearn.getLevel();
		int haveSkillLvl = 0;

		SkillEntry knownSkillEntry = player.getKnownSkill(skillToLearn.getId());
		if(knownSkillEntry != null)
		{
			haveSkillLvl = knownSkillEntry.getTemplate().getLevel();
			if(haveSkillLvl >= skillLvl)
				return false;
		}

		if(skillLvl > (haveSkillLvl + 1))
			return false;

		for(AcquireType at : ACQUITE_TYPES_TO_CHECK)
		{
			if(!SkillAcquireHolder.getInstance().isSkillPossible(player, skillToLearn, at))
				continue;

			SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, skillToLearn.getId(), skillToLearn.getLevel(), at);
			if(skillLearn == null)
				continue;

			if(SkillAcquireHolder.getInstance().checkLearnCondition(player, player.getClan(), skillLearn, player.getLevel(), at))
				return true;
		}

		return false;
	}
}