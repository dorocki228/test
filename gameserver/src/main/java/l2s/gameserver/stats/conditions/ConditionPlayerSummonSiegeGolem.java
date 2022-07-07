package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerSummonSiegeGolem extends Condition
{
	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		Player player = creature.getPlayer();
		if(player == null)
			return false;
		Zone zone = player.getZone(Zone.ZoneType.SIEGE);
		if(zone == null)
			return false;
		SiegeEvent<?, ?> event = zone.getEvent(SiegeEvent.class);
		if(event == null)
			return false;

		var ownerFraction = event.getResidence().getFraction();

		if(event instanceof CastleSiegeEvent || event instanceof FortressSiegeEvent)
		{
			if(!event.isInProgress())
				return false;

			if(zone.getParams().getInteger("residence") != event.getId())
				return false;

			if(player.getFraction() == ownerFraction)
				return false;
		}
		
		return true;
	}
}
