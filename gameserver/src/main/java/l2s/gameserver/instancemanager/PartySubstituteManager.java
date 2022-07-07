package l2s.gameserver.instancemanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.network.l2.s2c.ExRegistWaitingSubstituteOk;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class PartySubstituteManager extends SteppingRunnableQueueManager
{
	private static final PartySubstituteManager _instance;
	private final List<Player> waitingPlayers;
	private final List<Player> waitingMembers;

	public void addWaitingPlayer(Player player)
	{
		waitingPlayers.add(player);
	}

	public void removeWaitingPlayer(Player player)
	{
		waitingPlayers.remove(player);
	}

	public void addPartyMember(Player player)
	{
		waitingMembers.add(player);
	}

	public void removePartyMember(Player player)
	{
		waitingMembers.remove(player);
	}

	public static PartySubstituteManager getInstance()
	{
		return _instance;
	}

	private PartySubstituteManager()
	{
		super(10000L);
		waitingPlayers = new CopyOnWriteArrayList<>();
		waitingMembers = new CopyOnWriteArrayList<>();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000L, 10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> purge(), 60000L, 60000L);

		scheduleAtFixedRate(() -> {
			if(!waitingMembers.isEmpty() && !waitingPlayers.isEmpty())
				for(Player player : waitingMembers)
				{
					if(player == null || !player.isOnline() || player.getRequest() != null || !player.isPartySubstituteStarted() || player.getParty() == null)
						continue;
					for(Player wait : waitingPlayers)
					{
						if(wait == null || wait.getParty() != null || wait.getRequest() != null || wait.getClassId() != player.getClassId() || wait.getLevel() != player.getLevel())
							continue;
						new Request(Request.L2RequestType.PARTY_MEMBER_SUBSTITUTE, player, wait).setTimeout(10000);
						wait.sendPacket(new ExRegistWaitingSubstituteOk(null));
						player.stopSubstituteTask();
					}
				}
		}, 30000L, 30000L);
	}

	public Future<?> SubstituteSearchTask(Player player)
	{
		if(player == null)
			return null;
		waitingMembers.add(player);
		return schedule(() -> {
			waitingMembers.remove(player);
			if(player.getParty() != null)
			{}
			player.sendUserInfo();
			player.sendMessage("test");
		}, 300000L);
	}

	static
	{
		_instance = new PartySubstituteManager();
	}
}
