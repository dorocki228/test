package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public final class WyvernManagerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	public WyvernManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		boolean condition = validateCondition(player);
		if("RideWyvern".equalsIgnoreCase(actualCommand))
		{
			if(condition && player.isClanLeader())
				if(player.getMountType() != MountType.STRIDER)
                    showChatWindow(player, "wyvern/not_ready.htm", false);
				else if(player.getInventory().getItemByItemId(1460) == null || player.getInventory().getItemByItemId(1460).getCount() < 25L)
                    showChatWindow(player, "wyvern/havenot_cry.htm", false);
				else if(player.getInventory().destroyItemByItemId(1460, 25L))
				{
					player.setMount(player.getMountControlItemObjId(), 12621, player.getMountLevel(), player.getMountCurrentFeed());
                    showChatWindow(player, "wyvern/after_ride.htm", false);
				}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		if(val == 0)
		{
			if(!validateCondition(player))
                showChatWindow(player, "wyvern/lord_only.htm", false);
			else
                showChatWindow(player, "wyvern/lord_here.htm", false, "%Char_name%", String.valueOf(player.getName()));
		}
		else
			super.showChatWindow(player, val, firstTalk, arg);
	}

	private boolean validateCondition(Player player)
	{
		Residence residence = getCastle();
		if(residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader())
			return true;
		residence = getClanHall();
		return residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader();
	}
}
