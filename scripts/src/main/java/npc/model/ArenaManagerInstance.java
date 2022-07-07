package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

/**
 * @author Evil_dnk
 * reworked by Bonux
**/
public class ArenaManagerInstance extends NpcInstance
{
	public ArenaManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		if("recovery".equalsIgnoreCase(cmd))
		{
			if(!st.hasMoreTokens())
				return;

			if(player.isInZone(ZoneType.battle_zone))
				return;

			int neededmoney = 1000;
			long currentmoney = player.getAdena();
			if(neededmoney > currentmoney)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}

			player.reduceAdena(neededmoney, true);

			String cmd2 = st.nextToken();
			if("cp".equalsIgnoreCase(cmd2))
			{
				player.setCurrentCp(player.getMaxCp());
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addName(player));
			}
			else if("hp".equalsIgnoreCase(cmd2))
			{
				player.setCurrentHp(player.getMaxHp(), false);
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addName(player));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}
