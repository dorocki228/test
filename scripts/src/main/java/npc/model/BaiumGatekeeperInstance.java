package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

public final class BaiumGatekeeperInstance extends NpcInstance
{
	// NPC's
	private static final int BAIUM_RAID_NPC_ID = 29020;
	private static final int BAIUM_STONED_NPC_ID = 29025;

	private static final int BloodedFabric = 4295;

	// Locations
	private static final Location TELEPORT_POSITION = new Location(113100, 14500, 10077);

	public BaiumGatekeeperInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onTeleportRequest(Player player)
	{
		NpcInstance baiumNpc = GameObjectsStorage.getByNpcId(BAIUM_STONED_NPC_ID);
		NpcInstance baiumBoss = GameObjectsStorage.getByNpcId(BAIUM_RAID_NPC_ID);
		if(baiumNpc != null || baiumBoss != null)
		{
			if(baiumBoss == null)
			{
				if(ItemFunctions.deleteItem(player, BloodedFabric, 1))
				{
					player.setVar("baiumPermission", "granted", -1);
					player.teleToLocation(TELEPORT_POSITION);
				}
				else
					showChatWindow(player, "default/dimension_vertex_4002.htm", false);
			}
			else
				showChatWindow(player, "default/dimension_vertex_4003.htm", false);
		}
		else
			showChatWindow(player, "default/dimension_vertex_4004.htm", false);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		if(val == 0)
			showChatWindow(player, "default/dimension_vertex_4001.htm", firstTalk);
		else
			super.showChatWindow(player, val, firstTalk, arg);
	}
}