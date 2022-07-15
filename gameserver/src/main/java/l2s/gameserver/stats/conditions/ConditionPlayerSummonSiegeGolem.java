package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @date 18:37/22.05.2011
 */
public class ConditionPlayerSummonSiegeGolem extends Condition
{
	public ConditionPlayerSummonSiegeGolem()
	{
		//
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		Player player = actor.getPlayer();
		if(player == null)
			return false;

		Zone zone = player.getZone(Zone.ZoneType.RESIDENCE);
		if(zone != null)
			return false;

		zone = player.getZone(Zone.ZoneType.SIEGE);
		if(zone == null)
			return false;

		for(SiegeEvent<?, ?> event : player.getEvents(SiegeEvent.class))
		{
			if(event instanceof CastleSiegeEvent)
			{
				if(zone.getParams().getInteger("residence") == event.getId())
				{
					if(event.getSiegeClan(CastleSiegeEvent.ATTACKERS, player.getClan()) != null)
						return true;
				}
			}
			else if(event.getSiegeClan(CastleSiegeEvent.DEFENDERS, player.getClan()) != null)
				return true;
		}
		return false;
	}
}
