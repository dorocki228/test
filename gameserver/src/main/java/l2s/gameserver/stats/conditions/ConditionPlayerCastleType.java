package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerCastleType extends Condition
{
	private final ResidenceSide _type;

	public ConditionPlayerCastleType(ResidenceSide type)
	{
		_type = type;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;

		if(actor.getPlayer().getClan() == null)
			return false;

		Castle castle = actor.getPlayer().getCastle();
		if(castle == null)
			return false;

		return castle.getResidenceSide() == _type;
	}
}