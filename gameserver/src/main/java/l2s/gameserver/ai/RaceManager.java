package l2s.gameserver.ai;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaceManagerInstance;
import l2s.gameserver.network.l2.s2c.MonRaceInfoPacket;

import java.util.ArrayList;
import java.util.List;

public class RaceManager extends DefaultAI
{
	private boolean thinking;
	private List<Player> _knownPlayers;

	public RaceManager(NpcInstance actor)
	{
		super(actor);
		thinking = false;
		_knownPlayers = new ArrayList<>();
		AI_TASK_ATTACK_DELAY = 5000L;
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtThink()
	{
		RaceManagerInstance actor = getActor();
		if(actor == null)
			return;
		MonRaceInfoPacket packet = actor.getPacket();
		if(packet == null)
			return;
		synchronized (this)
		{
			if(thinking)
				return;
			thinking = true;
		}
		try
		{
			List<Player> newPlayers = new ArrayList<>();
			for(Player player : World.getAroundObservers(actor))
			{
				if(player == null)
					continue;
				newPlayers.add(player);
				if(!_knownPlayers.contains(player))
					player.sendPacket(packet);
				_knownPlayers.remove(player);
			}
			for(Player player : _knownPlayers)
				actor.removeKnownPlayer(player);
			_knownPlayers = newPlayers;
		}
		finally
		{
			thinking = false;
		}
	}

	@Override
	public RaceManagerInstance getActor()
	{
		return (RaceManagerInstance) super.getActor();
	}
}
