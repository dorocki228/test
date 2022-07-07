package ai.door;

import l2s.gameserver.ai.DoorAI;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DoorInstance;

import java.util.concurrent.TimeUnit;

public class CoreDoor extends DoorAI
{
	private final int[] ORFEN_DOORS = { 20210001, 20210002, 20210003 };
	private long _nextEventSpawn;

	public CoreDoor(DoorInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtOpen(Player player)
	{
		for(int id : ORFEN_DOORS)
		{
			DoorInstance door = ReflectionManager.MAIN.getDoor(id);
			if(door != null)
				door.openMe(player, true);
		}

		if(_nextEventSpawn < System.currentTimeMillis())
		{
			_nextEventSpawn = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(3);
			SpawnManager.getInstance().spawn("cruma_door_open");
		}
	}

	@Override
	public void onEvtClose(Player player)
	{
		if(!isActive())
			return;

		for(int id : ORFEN_DOORS)
		{
			DoorInstance door = ReflectionManager.MAIN.getDoor(id);
			if(door != null)
				door.closeMe(player, false);
		}
	}
}
