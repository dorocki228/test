package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.BossInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

import java.util.ArrayList;
import java.util.List;

public class QueenAntInstance extends BossInstance
{
	private static final int Queen_Ant_Larva = 29002;

	private final List<NpcInstance> _spawns = new ArrayList<>();
	private NpcInstance Larva;

	public QueenAntInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	public NpcInstance getLarva()
	{
		if(Larva == null)
			Larva = NpcUtils.spawnSingle(Queen_Ant_Larva, new Location(-21600, 179482, -5846, Rnd.get(0, 0xFFFF)));
		return Larva;
	}

	@Override
	protected int getKilledInterval(NpcInstance minion)
	{
		return minion.getNpcId() == 29003 ? 10000 : 280000 + Rnd.get(40000);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		broadcastPacketToOthers(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_D", 1, 0, getLoc()));

		for(NpcInstance npc : _spawns)
			npc.deleteMe();

		Larva = null;
		super.onDeath(killer);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		getLarva();
		broadcastPacketToOthers(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS01_A", 1, 0, getLoc()));
	}
}