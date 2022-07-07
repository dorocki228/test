package  l2s.Phantoms.listener;

import java.util.List;
import  l2s.gameserver.listener.actor.npc.OnSpawnListener;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.instances.NpcInstance;

public class SpawnNewMonster implements OnSpawnListener
{
		@Override
		public void onSpawn(NpcInstance actor)
		{
			if (actor.isMonster())
			{
				List <Creature> list = actor.getAroundCharacters(300, 300);
				
				if (list==null || list.isEmpty())
					return;
				//List <Creature> Filteredtargets = list.stream().filter(d->d != null && !d.isDead() && d.isPhantom() && d.getPhantomType() != PhantomType.PHANTOM_SERVANTS).collect(Collectors.toList());
			}
		}
}