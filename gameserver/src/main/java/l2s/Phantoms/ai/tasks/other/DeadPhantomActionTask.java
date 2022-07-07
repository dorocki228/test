package l2s.Phantoms.ai.tasks.other;

import gve.zones.GveZoneManager;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.GveRewardManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class DeadPhantomActionTask extends RunnableImpl
{
	public Player phantom;

	public DeadPhantomActionTask(Player ph)
	{
		phantom = ph;
	}

	@Override
	public void runImpl()
	{
		if(phantom == null)
			return;
		if(phantom.getReflectionId() != 0 /*|| phantom.isInPvPEvent() */ || phantom.getOlympiadGame() != null)
		{
			phantom.phantom_params.startDeadPhantomActionTask(Rnd.get(1, 3) * 60 * 1000);
			return;
		}
		
		Location respawnLoc;

		// респ к флагу
		if(phantom.canFixedRessurect())
		{
			//GmListTable.broadcastMessageToGMs(phantom + " to flag ");
			respawnLoc = GveZoneManager.getInstance().getClosestRespawnLoc(phantom);
      if(respawnLoc !=null)
        GveRewardManager.getInstance().manageRevivePenalty(phantom, false);
			else
			{
				phantom.teleToClosestTown();
				return;
			}

		}
		else//альтернатива город, прочие 
		{
			phantom.teleToClosestTown();
			return;
		}
		phantom.doRevive(100.);
		phantom.dispelDebuffs();
		phantom.setCurrentHpMp(phantom.getMaxHp(), phantom.getMaxHp());
		phantom.setCurrentCp(phantom.getMaxCp());
		phantom.teleToLocation(Location.findAroundPosition(respawnLoc, 250, phantom.getGeoIndex()));
		
		if(/*(phantom.isInPeaceZone() || phantom.isInTownPeaceZone()) && */!phantom.getAroundPlayers(50, 100).isEmpty()) // если стоим в толпе - запустим такс и отойдем всторонку 
		{
			ThreadPoolManager.getInstance().PhantomOtherSchedule(new MoveToFreePointTask(phantom), Rnd.get(1, 2) * 1000);
		}
		if (!phantom.phantom_params.getMoveToGkTask())
		{
			phantom.phantom_params.initMoveToGkTask(Rnd.get(4, 5) * 1000);
		}

	}
}
