package l2s.Phantoms.ai.tasks.other;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.utils.Location;

public class MoveToFreePointTask extends RunnableImpl
{
	public Player phantom;

	public MoveToFreePointTask(Player ph)
	{
		phantom = ph;
	}

	@Override
	public void runImpl()
	{
		if(phantom == null || phantom.getOlympiadGame() != null /*|| phantom.isInPvPEvent()*/ || phantom.getReflectionId() != 0)
			return;
		if(phantom.isInPeaceZoneOld())
		{
			for(int radius = 100; radius < 800; radius += 100)
			{
				Location new_lok = Location.findAroundPosition(phantom, 50, radius);
				if(!World.getAroundPlayers(new_lok, Rnd.get(40, 70), 100).isEmpty())
					continue;
				phantom.moveToLocation(new_lok, 0, true);
			}
		}
	}
}
