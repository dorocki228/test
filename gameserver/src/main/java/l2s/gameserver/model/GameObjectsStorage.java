package l2s.gameserver.model;

import com.google.common.math.DoubleMath;

import l2s.Phantoms.enums.PhantomType;
import l2s.gameserver.Config;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameObjectsStorage
{
	private static final IntObjectMap<GameObject> _objects = new CHashIntObjectMap(60000 * Config.RATE_MOB_SPAWN + Config.MAXIMUM_ONLINE_USERS + 1000);
	private static final IntObjectMap<NpcInstance> _npcs = new CHashIntObjectMap(60000 * Config.RATE_MOB_SPAWN);
	private static final IntObjectMap<Player> _players = new CHashIntObjectMap(Config.MAXIMUM_ONLINE_USERS);

	public static GameObject findObject(int objId)
	{
		return _objects.get(objId);
	}

	public static Collection<GameObject> getObjects()
	{
		return _objects.values();
	}

	public static Player getPlayer(String name)
	{
        return _players.values().stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

	public static Player getPlayer(int objId)
	{
		return _players.get(objId);
	}

	public static Collection<Player> getPlayers()
	{
		return _players.values();
	}

	public static List<Player> getPlayers(Predicate<Player> predicate)
	{
		return _players.values().stream().filter(predicate).collect(Collectors.toList());
	}
	
	public static Stream<Player> getPlayersStream()
	{
		return _players.values().stream();
	}

	public static Stream<Player> getPlayersStream(Predicate<Player> playerPredicate)
	{
		return _players.values().parallelStream()
                .filter(playerPredicate);
	}

	public static Stream<Player> getFractionStream(Fraction fraction)
	{
		return _players.values().parallelStream()
				.filter(player -> player.getFraction() == fraction);
	}

	public static int getPlayersCount()
	{
		return getPlayersCount(_players.size());
	}

	public static int getPlayersCount(long count)
	{
		return DoubleMath.roundToInt(count * Config.ONLINE_MULTIPLIER, RoundingMode.UP);
	}

	public static NpcInstance getNpc(String npcName)
	{
		NpcInstance result = null;
		for(NpcInstance temp : getNpcs())
			if(temp.getName().equalsIgnoreCase(npcName))
			{
				if(!temp.isDead())
					return temp;
				result = temp;
			}
		return result;
	}

	public static NpcInstance getNpc(int objId)
	{
		return _npcs.get(objId);
	}

	public static Collection<NpcInstance> getNpcs()
	{
		return _npcs.values();
	}

	public static NpcInstance getByNpcId(int npcId)
	{
		NpcInstance result = null;
		for(NpcInstance temp : getNpcs())
			if(temp.getNpcId() == npcId)
			{
				if(!temp.isDead())
					return temp;
				result = temp;
			}
		return result;
	}

	public static List<NpcInstance> getAllByNpcId(int npcId, boolean justAlive)
	{
        return getNpcs().stream()
                .filter(temp -> temp.getNpcId() == npcId && (!justAlive || !temp.isDead()))
                .collect(Collectors.toList());
	}

	public static List<NpcInstance> getAllByNpcId(int[] npcIds, boolean justAlive)
	{
        return getNpcs().stream()
                .filter(temp -> (!justAlive || !temp.isDead()) && ArrayUtils.contains(npcIds, temp.getNpcId()))
                .collect(Collectors.toList());
	}

	public static <T extends GameObject> void put(T o)
	{
		IntObjectMap<T> map = getMapForObject(o);
		if(map != null)
			map.put(o.getObjectId(), o);
		_objects.put(o.getObjectId(), o);
	}

	public static <T extends GameObject> void remove(T o)
	{
		IntObjectMap<T> map = getMapForObject(o);
		if(map != null)
			map.remove(o.getObjectId());
		_objects.remove(o.getObjectId());
	}

	private static <T extends GameObject> IntObjectMap<T> getMapForObject(T o)
	{
		if(o.isNpc())
			return (IntObjectMap<T>) _npcs;
		if(o.isPlayer())
			return (IntObjectMap<T>) _players;
		return null;
	}

	public static int getAllPhantomTownCount()
	{
		return (int) _players.values().stream().filter(p->p !=null && p.getPhantomType() == PhantomType.PHANTOM_TOWNS_PEOPLE).count();
	}

	public static List <Player> getAllPhantoms()
	{
		return getPlayersStream().filter(d->d != null && d.isPhantom()).collect(Collectors.toList());
	}

	public static int getAllPlayersSize()
	{
		return _players.size();
	}

	private static long offline_refresh = 0;
	private static int offline_count = 0;
	
	public static int getAllOfflineCount()
	{
		if (!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			return 0;
		
		long now = System.currentTimeMillis();
		if (now > offline_refresh)
		{
			offline_refresh = now+10000;
			offline_count = 0;
			for(Player player : getPlayers())
				if (player.isInOfflineMode() && !player.isPhantom())
					offline_count++;
		}
		
		return offline_count;
	}
	
	private static long Phantom_refresh = 0;
	private static int Phantom_count = 0;

	public static int getAllPhantomCount()
	{
		long now = System.currentTimeMillis();
		if (now > Phantom_refresh)
		{
			Phantom_refresh = now+10000;
			Phantom_count = getAllPhantoms().size();
		}
		return Phantom_count;
	}
}
