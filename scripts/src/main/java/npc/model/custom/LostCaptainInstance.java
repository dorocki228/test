package npc.model.custom;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.NpcUtils;

import java.util.Map;

public class LostCaptainInstance extends ReflectionBossInstance
{
	private static final int TELE_DEVICE_ID = 314;

	private static final Map<Integer, Integer> REWARD = Map.of(73, 200020, 74, 200021, 75, 200022);

	public LostCaptainInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		final int reward = REWARD.getOrDefault(getReflection().getInstancedZoneId(), -1);
		if (reward != -1) {
			getReflection().getPlayers().forEach(player -> ItemFunctions.addItem(player, reward, 1));
		}

		Reflection r = getReflection();
		r.setReenterTime(System.currentTimeMillis(), true);

		super.onDeath(killer);

		InstantZone iz = r.getInstancedZone();
		if(iz != null)
		{
			String tele_device_loc = iz.getAddParams().getString("tele_device_loc", null);
			if(tele_device_loc != null)
			{
				NpcUtils.spawnSingle(TELE_DEVICE_ID, Location.parseLoc(tele_device_loc), r);
			}
		}
	}
}
