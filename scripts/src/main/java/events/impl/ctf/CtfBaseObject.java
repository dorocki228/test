package events.impl.ctf;

import events.CaptureTeamFlagEvent;
import l2s.commons.geometry.Circle;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author VISTALL
 * @date 23:00/03.04.2012
 */
public class CtfBaseObject extends SpawnSimpleObject
{
	private class OnZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(!actor.isPlayer() || actor.getTeam() == TeamType.NONE || _teamType != actor.getTeam())
				return;

			Player player = actor.getPlayer();

			CaptureTeamFlagEvent event = actor.getEvent(CaptureTeamFlagEvent.class);

			FlagItemAttachment flagItemAttachment = player.getActiveWeaponFlagAttachment();
			if(!(flagItemAttachment instanceof CtfFlagObject))
				return;

            List<EventPlayerObject> objects = event.getObjects(player.getTeam());
            Optional<EventPlayerObject> optionalPlayerObject = objects.stream()
                    .filter(object -> Objects.equals(object.getPlayer(), player))
                    .findAny();
            optionalPlayerObject.ifPresent(playerObject -> playerObject.increasePoints("EVENT_POINTS", 1));

            event.checkForWinner();
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
			//
		}
	}

	private Zone _zone = null;
	private TeamType _teamType;

	public CtfBaseObject(int npcId, Location loc, TeamType teamType)
	{
		super(npcId, loc);
		_teamType = teamType;
	}

	@Override
	public void spawnObject(Event event)
	{
		super.spawnObject(event);

		Circle c = new Circle(getLoc(), 250);
		c.setZmax(World.MAP_MAX_Z);
		c.setZmin(World.MAP_MIN_Z);

		StatsSet set = new StatsSet();
		set.set("name", StringUtils.EMPTY);
		set.set("type", Zone.ZoneType.dummy);
		set.set("territory", new Territory().add(c));

		_zone = new Zone(new ZoneTemplate(set));
		_zone.setReflection(event.getReflection());
		_zone.addListener(new OnZoneEnterLeaveListenerImpl());
		_zone.setActive(true);
	}

	@Override
	public void despawnObject(Event event)
	{
		super.despawnObject(event);

		_zone.setActive(false);
		_zone = null;
	}
}
