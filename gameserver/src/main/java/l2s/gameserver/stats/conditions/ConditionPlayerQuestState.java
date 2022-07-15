package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.QuestState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
**/
public class ConditionPlayerQuestState extends Condition
{
	private final int _questId;
	private final int _cond;

	public ConditionPlayerQuestState(int questId, int cond)
	{
		_questId = questId;
		_cond = cond;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;

		Player player = actor.getPlayer();
		QuestState qs = player.getQuestState(_questId);
		if(qs == null)
			return false;

		return qs.getCond() == _cond;
	}
}
