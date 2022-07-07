package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.QuestState;

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
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return false;
		Player player = creature.getPlayer();
		QuestState qs = player.getQuestState(_questId);
		return qs != null && qs.getCond() == _cond;
	}
}
