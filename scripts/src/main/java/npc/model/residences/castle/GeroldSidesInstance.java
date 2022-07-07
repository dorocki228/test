package npc.model.residences.castle;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public class GeroldSidesInstance extends NpcInstance
{
	private static final TIntObjectMap<Object[]> NPC = new TIntObjectHashMap<Object[]>(){
		{
			put(36664, new Object[] { ResidenceSide.LIGHT, 1 });
			put(36665, new Object[] { ResidenceSide.LIGHT, 2 });
			put(36666, new Object[] { ResidenceSide.LIGHT, 3 });
			put(36667, new Object[] { ResidenceSide.LIGHT, 4 });
			put(36668, new Object[] { ResidenceSide.LIGHT, 5 });
			put(36673, new Object[] { ResidenceSide.DARK, 1 });
			put(36674, new Object[] { ResidenceSide.DARK, 2 });
			put(36675, new Object[] { ResidenceSide.DARK, 3 });
			put(36676, new Object[] { ResidenceSide.DARK, 4 });
			put(36677, new Object[] { ResidenceSide.DARK, 5 });
		}
	};

	public GeroldSidesInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "castle/gerold/";
	}

	@Override
	public String getHtmlFilename(int val, Player player)
	{
		if(val == 0 && player.isClanLeader())
		{
			int id = player.getClan().getCastle();
			Object[] data = NPC.get(getNpcId());
			if(id == (int) data[1])
				return data[0] + ".htm";
		}
		return null;
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();
		if("change_side".equals(cmd))
		{
			Castle castle = player.getCastle();
			SiegeEvent<?, ?> event = castle.getSiegeEvent();

			if(castle.getResidenceSide() == ResidenceSide.DARK)
			{
				castle.setResidenceSide(ResidenceSide.LIGHT, false);

				event.spawnAction("castle_messenger_light_npc", false);
				event.spawnAction("castle_peace_light_npcs", true);
			}
			else if(castle.getResidenceSide() == ResidenceSide.LIGHT)
			{
				castle.setResidenceSide(ResidenceSide.DARK, false);

				event.spawnAction("castle_messenger_dark_npc", false);
				event.spawnAction("castle_peace_dark_npcs", true);
			}
			event.clearSpawnActions();
			castle.broadcastResidenceState();
		}
		else
			super.onBypassFeedback(player, command);
	}
}