package ai.locations.crumatower;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.actor.npc.OnDecayListener;
import l2s.gameserver.listener.actor.npc.OnSpawnListener;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KanuToIIIKa
 */

public class FirstFloorCrutch implements OnDecayListener, OnSpawnListener
{
	private final List<String> groups = new ArrayList<>();
	private final List<String> groupsSpawned = new ArrayList<>();
	private final int[][] BONUS_SPAWN = { { 20216, 2 }, { 21035, 2 }, { 21036, 2 }, { 20218, 2 }, { 20213, 3 } };

	public FirstFloorCrutch()
	{
		groups.add("f1_r1");
		groups.add("f1_r2");
		groups.add("f1_r4");
		groups.add("f1_r5");
		groups.add("f1_r6");
		groups.add("f1_r7");
		groups.add("f1_r8");
		groups.add("f1_r9");
		groups.add("f1_r14");
		groups.add("f1_r15");
		groups.add("f1_r17");
		groups.add("f1_r18");
		groups.add("f1_r19");
		groups.add("f1_r20");
		groups.add("f1_r22");
		groups.add("f1_r23");

		for(String g : groups)
			SpawnManager.getInstance().spawn(g);
	}

	@Override
	public void onDecay(NpcInstance npc)
	{
		if(npc.getSpawn() != null)
		{
			String group = npc.getSpawn().getName();

			if(groups.contains(group) && !groupsSpawned.contains(group))
			{

				List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
				for(Spawner spawn : spawners)
				{
					int count = spawn.getMainNpcId() == npc.getNpcId() ? 1 : 0;

					if(spawn.getAllSpawned().size() != count)
						return;
				}

				for(int[] data : BONUS_SPAWN)
				{
					int id = data[0];
					int count = data[1];

					NpcTemplate template = NpcHolder.getInstance().getTemplate(id);
					for(int i = 0; i < count; i++)
					{

						SimpleSpawner spawn = new SimpleSpawner(template);
						spawn.setLoc(npc.getSpawn().getRandomSpawnRange().getRandomLoc(0));
						spawn.setAmount(1);
						spawn.setHeading(Rnd.get(65535));
						spawn.setRespawnDelay(60);
						spawn.setReflection(npc.getReflection());
						spawn.init();
						spawn.stopRespawn();
					}
				}
				groupsSpawned.add(group);

			}

		}
	}

	@Override
	public void onSpawn(NpcInstance npc)
	{
		if(npc.getSpawn() != null)
		{
			String group = npc.getSpawn().getName();

			if(groups.contains(group) && groupsSpawned.contains(group))
			{
				List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
				for(Spawner spawn : spawners)
					if(spawn.getScheduledCount() != 0)
						return;

				groupsSpawned.remove(group);
			}

		}
	}

}
