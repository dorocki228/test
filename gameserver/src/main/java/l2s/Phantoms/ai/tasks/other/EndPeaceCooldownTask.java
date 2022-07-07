package l2s.Phantoms.ai.tasks.other;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gve.zones.GveZoneManager;
import gve.zones.model.GveOutpost;
import gve.zones.model.GveZone;
import l2s.Phantoms.PhantomPlayers;
import l2s.Phantoms.PhantomVariables;
import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.enums.PhantomType;
import l2s.Phantoms.objects.GveZoneParam;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.Phantoms.objects.TrafficScheme.Point;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.service.LocationBalancerService;
import l2s.gameserver.utils.Location;

public class EndPeaceCooldownTask extends RunnableImpl
{
	public Player phantom;

	public EndPeaceCooldownTask(Player ph)
	{
		phantom = ph;
	}

	@Override
	public void runImpl()
	{
		if(phantom == null)
			return;
		if(phantom.getOlympiadGame() != null /*|| phantom.isInPvPEvent()*/ || phantom.getReflectionId() != 0)
			return;
		if(phantom.isInPeaceZoneOld())
		{
			//if(PhantomVariables.getInt("PhantomsInTheCity", 250) > 0 && PhantomPlayers.getInstance().getRoute().size() < PhantomVariables.getInt("PhantomsInTheCity", 250) && TrafficSchemeParser.getInstance().getAllTrafficScheme().size() > GameObjectsStorage.getAllPhantomTownCount())
				//toTs();
		//	else
				toFarm();
		}
	}

	private void toFarm()
	{
		if(phantom == null)
			return;

		List<GveZone> lst = GveZoneManager.getInstance().getActiveZones().stream().filter(z->
		{
			if(PhantomUtils.IsActive("Status_" + z.getName()))
			{
				if(z.getZone().getInsidePhantoms().size() > PhantomVariables.getInt(z.getName(), 0))
					return false;
				return true;
			}
			return false;
		}
				).filter(z-> z.getZone().getType()!=ZoneType.gve_pvp && z.canEnterZone(phantom) && !LocationBalancerService.getInstance().isLocationLimit(z)).filter(z-> {
					GveZoneParam param = PhantomUtils.getLocType(z);
					if(phantom.getLevel()> param.getMaxLvl())
						return false;
					if (phantom.getLevel()<param.getMinLvl())
						return false;
					return true;
				}).collect(Collectors.toList());

		if(lst==null || lst.size() ==0)
		{
			phantom.kick();
			return;
		}
		GveZone zone = Rnd.get(lst);
		
		// локация спавна
		List<Location> listloc = new ArrayList<Location>();
		//TODO test


			// возможный респ возле флагов
			GveOutpost tmp = zone.getRandomOutpost(phantom.getFraction());
			if (tmp.getStatus() != GveOutpost.DEAD)
				for (Location loc : tmp.getLocations())
					listloc.add(Location.findAroundPosition(loc, 350, phantom.getGeoIndex()));
		
			for(int i = 0; i < 10; i++) // 10 точек с зоны с проверкой на игроков
				listloc.add(zone.getZone().getTerritory().getRandomLoc(0, 3000));
		
		List<Location> listloc2 = listloc.stream().filter(l -> l != null && PhantomPlayers.getInstance().checkZone(l)).collect(Collectors.toList());

		if (listloc2.size() == 0)
		{
			phantom.kick();
			return;
		}
		Location loc = Rnd.get(listloc2);
		if(loc == null)
		{
			phantom.kick();
			return;
		}
		phantom.teleToLocation(new Location(loc.getX() + Rnd.get(200), loc.getY() + Rnd.get(200), loc.getZ()));
	}

	private void toTs()
	{
		if(phantom == null)
			return;

		PhantomRoute scheme = PhantomPlayers.getInstance().getRandomTrafficScheme(phantom, null);
		if(scheme == null) // нет схем - отправляем на фарм
		{
			toFarm();
			return;
		}
		phantom.phantom_params.getPhantomAI().abortAITask();//остановим аи
		phantom.phantom_params.setRndDelayAi(Rnd.get(-100, 100));

		Point loc = scheme.getPointsFirstTask().get(0);
		// проверим доступность точки "добежать без тп"
		phantom.phantom_params.setTrafficScheme(scheme, null, false);
		phantom.setPhantomType(PhantomType.PHANTOM_TOWNS_PEOPLE);

		List<List<Location>> moveList = GeoMove.findMovePath(phantom.getX(), phantom.getY(), phantom.getZ(), loc.getX(), loc.getY(), loc.getZ(), true, phantom.getGeoIndex());
		if(phantom.getDistance(loc.getLoc()) > 5000 || (moveList != null && moveList.size() > 0)) // добежать невозможно, используем тп (другой город тд тп)
		{
			phantom.setHeading(Rnd.get(1, 360));
			phantom.spawnMe(Location.coordsRandomize(loc.getLoc(), 30, 60));

		}
		else
		{
			phantom.moveToLocation(loc.getLoc(), 0, true);
		}

		phantom.phantom_params.getPhantomAI().startAITask(1000 + phantom.phantom_params.getRndDelayAi());
		phantom.phantom_params.getPhantomAI().startBuffTask(1800);

	}
}
