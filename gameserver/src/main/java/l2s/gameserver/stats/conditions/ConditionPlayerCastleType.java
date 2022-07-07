package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerCastleType extends Condition
{
	private final int _type;

	public ConditionPlayerCastleType(int type)
	{
		_type = type;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.isPlayer() && creature.getPlayer().getClan() != null && creature.getPlayer().getClan().getCastle() != 0 && (_type != 0 || creature.getPlayer().getCastle().getResidenceSide() == ResidenceSide.LIGHT) && (_type != 1 || creature.getPlayer().getCastle().getResidenceSide() == ResidenceSide.DARK);
	}
}
