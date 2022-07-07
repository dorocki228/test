package l2s.Phantoms.ai.tasks.other;

import java.util.List;
import java.util.stream.Collectors;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.enums.PhantomType;
import l2s.Phantoms.manager.PartyManager;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;

public class DespawnTask extends RunnableImpl
{
	@Override
	public void runImpl()
	{
		List<Player> phantoms = GameObjectsStorage.getPlayersStream().filter(d -> d != null && d.isPhantom() && d.getReflectionId() == 0 && d.getPhantomType() != PhantomType.PHANTOM_TRADER && d.getPhantomType() != PhantomType.PHANTOM_CRAFTER && d.getPhantomType() != PhantomType.PHANTOM_BOT_HUNTER && !d.isInOlympiadMode() && d.getOlympiadGame() == null && isCombat(d) /*&& !d.isInPvPEvent()*/).collect(Collectors.toList());
		for(Player phantom : phantoms)
		{
			if(phantom == null)
				continue;
			if(!phantom.isInPeaceZone() && World.getAroundRealPlayers(phantom.getLoc(), 1000, 200).size() > 0)
				continue;
			if(phantom.getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER)
			{
				if(phantom.phantom_params.getPhantomPartyAI() != null && System.currentTimeMillis() > phantom.phantom_params.getPhantomPartyAI().getDespawnTime())
				{
					PartyManager.getInstance().despawnParties(phantom.phantom_params.getPhantomPartyAI());
					continue;
				}
				if(phantom.phantom_params.getPhantomPartyAI() == null)
					phantom.kick();
			}
			else
			{
				long online_time = System.currentTimeMillis() - phantom.getOnlineBeginTime();
				if(online_time > PhantomUtils.getConfigLevelGroup(phantom.getLevel(), Config.PHANTOM_PLAYERS_DESPAWN_DELAY))
				{
					if(phantom.getPhantomType() == PhantomType.PHANTOM_PARTY && phantom.phantom_params.getPhantomPartyAI() != null)
					{
						for(Player member : phantom.phantom_params.getPhantomPartyAI().getAllMembers())
						{
							PhantomRoute scheme = member.phantom_params.getTrafficScheme();
							if(scheme != null)
								member.phantom_params.setTrafficScheme(null, scheme, true);// свобождаем маршрут
							member.kick();
						}
					}
					else
					{
						PhantomRoute scheme = phantom.phantom_params.getTrafficScheme();
						if(scheme != null)
							phantom.phantom_params.setTrafficScheme(null, scheme, true);// свобождаем маршрут
						phantom.kick();
					}
				}
			}
		}
	}

	private boolean isCombat(Player d)
	{
		if(d.isInCombat() && d.getTarget() != null && d.getTarget().isPlayer())
			return false;
		return true;
	}

}