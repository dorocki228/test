package l2s.gameserver.utils;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

public class NpcUtils
{
	public static <T extends NpcInstance> T newInstance(int npcId)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
			throw new NullPointerException("Npc template id : " + npcId + " not found!");
		return (T) template.getNewInstance();
	}

	public static NpcInstance canPassPacket(Player player, L2GameClientPacket packet, Object... arg)
	{
		NpcInstance npcInstance = player.getLastNpc();
		return npcInstance != null && player.checkInteractionDistance(npcInstance) && npcInstance.canPassPacket(player, packet.getClass(), arg) ? npcInstance : null;
	}

	public static NpcInstance createNpc(int npcId)
	{
		return createNpc(npcId, null);
	}

	public static NpcInstance createNpc(int npcId, String title)
	{
		NpcInstance npc = newInstance(npcId);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
		if(title != null)
			npc.setTitle(title);
		return npc;
	}

	public static void spawnNpc(NpcInstance npc, int x, int y, int z)
	{
		spawnNpc(npc, new Location(x, y, z, -1), ReflectionManager.MAIN, 0L);
	}

	public static void spawnNpc(NpcInstance npc, int x, int y, int z, long despawnTime)
	{
		spawnNpc(npc, new Location(x, y, z, -1), ReflectionManager.MAIN, despawnTime);
	}

	public static void spawnNpc(NpcInstance npc, int x, int y, int z, int h, long despawnTime)
	{
		spawnNpc(npc, new Location(x, y, z, h), ReflectionManager.MAIN, despawnTime);
	}

	public static void spawnNpc(NpcInstance npc, Location loc)
	{
		spawnNpc(npc, loc, ReflectionManager.MAIN, 0L);
	}

	public static void spawnNpc(NpcInstance npc, Location loc, long despawnTime)
	{
		spawnNpc(npc, loc, ReflectionManager.MAIN, despawnTime);
	}

	public static void spawnNpc(NpcInstance npc, Location loc, Reflection reflection)
	{
		spawnNpc(npc, loc, reflection, 0L);
	}

	public static void spawnNpc(NpcInstance npc, Location loc, Reflection reflection, long despawnTime)
	{
		npc.setHeading(loc.h < 0 ? Rnd.get(65535) : loc.h);
		npc.setSpawnedLoc(loc);
		npc.setReflection(reflection);
		npc.spawnMe(npc.getSpawnedLoc());
		if(despawnTime > 0L)
			npc.startDeleteTask(despawnTime);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.MAIN, 0L, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.MAIN, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.MAIN, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc)
	{
		return spawnSingle(npcId, loc, ReflectionManager.MAIN, 0L, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, long despawnTime)
	{
		return spawnSingle(npcId, loc, ReflectionManager.MAIN, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection)
	{
		return spawnSingle(npcId, loc, reflection, 0L, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime)
	{
		return spawnSingle(npcId, loc, reflection, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime, String title)
	{
		NpcInstance npc = createNpc(npcId, title);
		if(npc == null)
			return null;
		spawnNpc(npc, loc, reflection, despawnTime);
		return npc;
	}
}
