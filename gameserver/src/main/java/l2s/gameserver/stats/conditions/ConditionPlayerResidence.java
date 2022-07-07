package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;

public class ConditionPlayerResidence extends Condition
{
	private final int _id;
	private final ResidenceType _type;

	public ConditionPlayerResidence(int id, ResidenceType type)
	{
		_id = id;
		_type = type;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return false;

		Player player = creature.getPlayer();
		Clan clan = player.getClan();
		if(clan == null)
			return false;
		int residenceId = clan.getResidenceId(_type);
		return _id > 0 ? residenceId == _id : residenceId > 0;
	}
}
