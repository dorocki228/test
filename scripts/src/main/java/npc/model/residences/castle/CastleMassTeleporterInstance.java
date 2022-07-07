package npc.model.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CastleMassTeleporterInstance extends NpcInstance
{
	private int _delay;

	private class TeleportTask implements Runnable
	{
		@Override
		public void run()
		{
			Functions.npcShout(CastleMassTeleporterInstance.this, NpcString.THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE, "#" + getCastle().getNpcStringName().getId());

			Zone zone = ReflectionManager.MAIN.getZone("[" + getCastle().getName().toLowerCase() + "_mass_gatekeeper_zone]");
			for(Player p : zone.getInsidePlayers())
				p.teleToLocation(Location.findPointToStay(Rnd.get(_teleportLoc), 0, 200, p.getGeoIndex()));

			_teleportTask = null;
		}
	}

	private Future<?> _teleportTask;
	private final List<Location> _teleportLoc;

	public CastleMassTeleporterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		String[] data = template.getAIParams().getString("teleport_loc").split(" ");

		_teleportLoc = new ArrayList<>();

		for(int i = 0; i < data.length; i += 3)
			_teleportLoc.add(new Location(Integer.parseInt(data[i]), Integer.parseInt(data[i + 1]), Integer.parseInt(data[i + 2])));
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(_teleportTask == null) {
			_delay = Math.toIntExact(TimeUnit.SECONDS.toMillis(isAllTowersDead() ? 30 : 30));
			_teleportTask = ThreadPoolManager.getInstance().schedule(new TeleportTask(), _delay);
		}

		showChatWindow(player, getDelayHtml(_delay), false);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		if(_teleportTask != null)
			showChatWindow(player, getDelayHtml(_delay), firstTalk);
		else
		{
			if(isAllTowersDead())
				showChatWindow(player, "residence2/castle/gludio_mass_teleporter002.htm", firstTalk);
			else
				showChatWindow(player, "residence2/castle/gludio_mass_teleporter001.htm", firstTalk);
		}
	}

	private String getDelayHtml(int delay)
	{
		switch(delay)
		{
			case 120_000:
				return "residence2/castle/CastleTeleportDelayed2min.htm";
			case 30_000:
				return "residence2/castle/CastleTeleportDelayed30sec.htm";
		}
		return "";
	}

	private boolean isAllTowersDead()
	{
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent == null || !siegeEvent.isInProgress())
			return false;

		List<SiegeToggleNpcObject> towers = siegeEvent.getObjects(CastleSiegeEvent.CONTROL_TOWERS);
		return towers.stream().noneMatch(SiegeToggleNpcObject::isAlive);
	}
}
