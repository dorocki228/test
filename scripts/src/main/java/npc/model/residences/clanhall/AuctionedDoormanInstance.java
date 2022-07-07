package npc.model.residences.clanhall;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.Privilege;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author VISTALL
 * @date 10:50/20.06.2011
 */
public class AuctionedDoormanInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private final int[] _doors;
	private final boolean _elite;

	public AuctionedDoormanInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
		_elite = template.getAIParams().getBool("elite", false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		ClanHall clanHall = getClanHall();
		if("openDoors".equalsIgnoreCase(command))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
					ReflectionUtils.getDoor(d).openMe();
				showChatWindow(player, "residence2/clanhall/agitafterdooropen.htm", false);
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm", false);
		}
		else if("closeDoors".equalsIgnoreCase(command))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
					ReflectionUtils.getDoor(d).closeMe(player, true);
				showChatWindow(player, "residence2/clanhall/agitafterdoorclose.htm", false);
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm", false);
		}
		else if("banish".equalsIgnoreCase(command))
		{
			if(player.hasPrivilege(Privilege.CH_DISMISS))
			{
				clanHall.banishForeigner(player.getClan().getClanId());
				showChatWindow(player, "residence2/clanhall/agitafterbanish.htm", false);
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm", false);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		if(clanHall != null)
		{
			Clan playerClan = player.getClan();
			if(playerClan != null && playerClan.getHasHideout() == clanHall.getId())
				showChatWindow(player, _elite ? "residence2/clanhall/WyvernAgitJanitorHi.htm" : "residence2/clanhall/AgitJanitorHi.htm", firstTalk, "%owner%", playerClan.getName());
			else if(clanHall.getOwner() != null)
				showChatWindow(player, "residence2/clanhall/noAgitInfoOwner.htm", firstTalk, "%owner_clan%", clanHall.getOwner(), "%owner_clan_leader%", clanHall.getOwner().getLeaderName());
			else
				showChatWindow(player, "residence2/clanhall/noAgitInfo.htm", firstTalk);
		}
		else
			showChatWindow(player, "residence2/clanhall/noAgitInfo.htm", firstTalk);
	}

}
