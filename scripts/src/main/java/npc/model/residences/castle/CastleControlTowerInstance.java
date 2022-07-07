package npc.model.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.HashSet;
import java.util.Set;

public class CastleControlTowerInstance extends SiegeToggleNpcInstance
{
	private final Set<Spawner> _spawnList = new HashSet<>();

	public CastleControlTowerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onDeathImpl(Creature killer)
	{
		for(Spawner spawn : _spawnList)
			spawn.stopRespawn();
		_spawnList.clear();
	}

	@Override
	public void register(Spawner spawn)
	{
		_spawnList.add(spawn);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		CastleSiegeEvent siege = getEvent(CastleSiegeEvent.class);
		if(siege != null && siege.isInProgress())
		{
			setFraction(siege.getOwnerFraction());
			broadcastCharInfo();
			siege.broadcastCrystalStatus();
		}
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();

		CastleSiegeEvent siege = getEvent(CastleSiegeEvent.class);
		if(siege != null && siege.isInProgress())
			siege.broadcastCrystalStatus();
	}
}