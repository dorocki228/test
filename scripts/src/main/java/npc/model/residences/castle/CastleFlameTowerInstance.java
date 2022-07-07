package npc.model.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @date 8:58/17.03.2011
 */
public class CastleFlameTowerInstance extends SiegeToggleNpcInstance
{
	private static final Logger logger = LoggerFactory.getLogger(CastleFlameTowerInstance.class);

	private static final long serialVersionUID = 1L;

	private Set<String> _zoneList;

	public CastleFlameTowerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onDeathImpl(Creature killer)
	{
		List<CastleSiegeEvent> events = getEvents(CastleSiegeEvent.class);
		if(events.isEmpty())
			return;

		for(CastleSiegeEvent event : events)
		{
			if(!event.isInProgress())
				continue;

			for(String s : _zoneList)
			{
				List<CastleDamageZoneObject> objects = event.getObjects(s);
				for(CastleDamageZoneObject zone : objects) {
					if (zone == null) {
						logger.info("CastleFlameTowerInstance zone == null: npc: " + this + " loc = " + getLoc().toXYZString());
						continue;
					}
					if (zone.getZone() == null) {
						logger.info("CastleFlameTowerInstance zone.getZone() == null: " + zone.getName() + " npc: " + this + " loc = " + getLoc().toXYZString());
						continue;
					}
					zone.getZone().setActive(false);
				}
			}
		}
	}

	@Override
	public void setZoneList(Set<String> set)
	{
		_zoneList = set;
	}
}
