package handler.admincommands;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.entity.residence.clanhall.InstantClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.HtmlUtils;

import java.util.Calendar;

/**
 * @author VISTALL
 * @date 15:10/06.03.2011
 */
public class AdminResidence extends ScriptAdminCommand
{
	private enum Commands
	{
		admin_residence_list,
		admin_residence,
		admin_set_owner,
		admin_set_siege_time,
		//
		admin_quick_siege_start,
		admin_quick_siege_stop,
		admin_add_participant,
		admin_remove_participant
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;

		Residence r;
		SiegeEvent<?, ?> event;
		Calendar calendar;
		HtmlMessage msg;
		GameObject target;
		SiegeClanObject siegeClan;
		switch(command)
		{
			case admin_residence_list:
				msg = new HtmlMessage(5);
				msg.setFile("admin/residence/residence_list.htm");
				StringBuilder replyMSG = new StringBuilder(200);
				for(Residence residence : ResidenceHolder.getInstance().getResidences())
					if(residence != null)
					{
						if(!residence.isInstant())
						{
							replyMSG.append("<tr><td>");
							replyMSG.append("<a action=\"bypass -h admin_residence ").append(residence.getId()).append("\">").append(HtmlUtils.htmlResidenceName(residence.getId())).append("</a>");
							replyMSG.append("</td><td>");

							Clan owner = residence.getOwner();
							if(owner == null)
								replyMSG.append("NPC");
							else
								replyMSG.append(owner.getName());

							replyMSG.append("</td></tr>");
						}
						else
						{
							replyMSG.append("<tr><td>");
							replyMSG.append("<a action=\"bypass -h admin_residence ").append(residence.getId()).append("\">").append(residence.getName()).append("</a>");
							replyMSG.append("</td><td>");

							InstantClanHall instant = (InstantClanHall) residence;
							replyMSG.append(instant.getOwners().size() + " owners");

							replyMSG.append("</td></tr>");
						}
					}
				msg.replace("%residence_list%", replyMSG.toString());
				activeChar.sendPacket(msg);
				break;
			case admin_residence:
				if(wordList.length != 2)
					return false;
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
					return false;

				msg = new HtmlMessage(5);

				event = r.getSiegeEvent();

				StringBuilder clans = new StringBuilder(100);

				if(r.isCastle())
				{
					msg.replace("%residence%", HtmlUtils.htmlResidenceName(r.getId()));
					msg.setFile("admin/residence/castle_siege_info.htm");
					msg.replace("%owner%", r.getOwner() == null ? "NPC" : r.getOwner().getName());
					event.getObjects().entrySet().stream()
							.flatMap(entry -> entry.getValue().stream())
							.filter(o -> o instanceof SiegeClanObject)
							.map(o -> (SiegeClanObject) o)
							.forEach(siegeClanObject ->
									clans.append("<tr>").append("<td align=center >")
											.append(siegeClanObject.getClan().getName())
											.append("</td>").append("<td align=center >")
											.append(siegeClanObject.getClan().getLeaderName())
											.append("</td>").append("<td align=center>")
											.append(siegeClanObject.getType()).append("</td>").append("</tr>"));
				}
				else
				{
					if(r.isInstant())
					{
						msg.setFile("admin/residence/instant_clanhall_siege_info.htm");
						msg.replace("%residence%", r.getName());
						msg.replace("%owner%", ((InstantClanHall) r).getOwners().size() + " owners");
					}
					else
					{
						msg.setFile("admin/residence/clanhall_siege_info.htm");
						msg.replace("%residence%", HtmlUtils.htmlResidenceName(r.getId()));
						msg.replace("%owner%", r.getOwner() == null ? "NPC" : r.getOwner().getName());
					}
					event.getObjects().entrySet().stream()
							.flatMap(entry -> entry.getValue().stream())
							.filter(o -> o instanceof AuctionSiegeClanObject)
							.map(o -> (SiegeClanObject) o)
							.forEach(siegeClanObject ->
									clans.append("<tr>").append("<td align=center >")
											.append(siegeClanObject.getClan().getName())
											.append("</td>").append("<td align=center >")
											.append(siegeClanObject.getClan().getLeaderName()).append("</td>")
											.append("<td align=center>").append(siegeClanObject.getParam()).append("</td>")
											.append("</tr>"));
				}

				msg.replace("%clans%", clans.toString());
				msg.replace("%id%", String.valueOf(r.getId()));
				msg.replace("%cycle%", String.valueOf(r.getCycle()));
				msg.replace("%paid_cycle%", String.valueOf(r.getPaidCycle()));
				msg.replace("%reward_count%", "0");
				msg.replace("%left_time%", String.valueOf(r.getCycleDelay()));
				msg.replace("%hour%", String.valueOf(r.getSiegeDate().get(Calendar.HOUR_OF_DAY)));
				msg.replace("%minute%", String.valueOf(r.getSiegeDate().get(Calendar.MINUTE)));
				msg.replace("%day%", String.valueOf(r.getSiegeDate().get(Calendar.DAY_OF_MONTH)));
				msg.replace("%month%", String.valueOf(r.getSiegeDate().get(Calendar.MONTH) + 1));
				msg.replace("%year%", String.valueOf(r.getSiegeDate().get(Calendar.YEAR)));

				activeChar.sendPacket(msg);
				break;
			case admin_set_owner:
				if(wordList.length != 3)
					return false;
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
					return false;
				Clan clan = null;
				String clanName = wordList[2];
				if(!"npc".equalsIgnoreCase(clanName))
				{
					clan = ClanTable.getInstance().getClanByName(clanName);
					if(clan == null)
					{
						activeChar.sendPacket(SystemMsg.INCORRECT_NAME);
						AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
						return false;
					}
				}

				event = r.getSiegeEvent();

				event.clearActions();

				r.getLastSiegeDate().setTimeInMillis(clan == null ? 0 : System.currentTimeMillis());
				r.getOwnDate().setTimeInMillis(clan == null ? 0 : System.currentTimeMillis());
				if(!r.isInstant())
				{
					r.changeOwner(clan);
				}
				else
				{
					((InstantClanHall) r).addOwner(clan, false);
					clan.setHasHideout(r.getId());
					clan.broadcastClanStatus(true, false, false);
				}

				r.setJdbcState(JdbcEntityState.UPDATED);
				r.update();

				if(clan == null && r.isCastle())
				{
					r.getSiegeEvent().spawnAction("castle_messenger_light_npc", false);
					r.getSiegeEvent().spawnAction("castle_messenger_dark_npc", false);
					r.getSiegeEvent().spawnAction("castle_peace_light_npcs", false);
					r.getSiegeEvent().spawnAction("castle_peace_dark_npcs", false);
					r.setResidenceSide(ResidenceSide.NEUTRAL, false);
					r.broadcastResidenceState();
				}

				event.reCalcNextTime(false);
				event.clearSpawnActions();
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			case admin_set_siege_time:
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
					return false;

				calendar = (Calendar) r.getSiegeDate().clone();
				for(int i = 2; i < wordList.length; i++)
				{
					int type;
					int val = Integer.parseInt(wordList[i]);
					switch(i)
					{
						case 2:
							type = Calendar.HOUR_OF_DAY;
							break;
						case 3:
							type = Calendar.MINUTE;
							break;
						case 4:
							type = Calendar.DAY_OF_MONTH;
							break;
						case 5:
							type = Calendar.MONTH;
							val -= 1;
							break;
						case 6:
							type = Calendar.YEAR;
							break;
						default:
							continue;
					}
					calendar.set(type, val);
				}
				event = r.getSiegeEvent();

				event.clearActions();
				r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
				event.registerActions();
				r.setJdbcState(JdbcEntityState.UPDATED);
				r.update();

				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			case admin_quick_siege_start:
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
					return false;

				calendar = Calendar.getInstance();
				if(wordList.length >= 3)
					calendar.set(Calendar.SECOND, -Integer.parseInt(wordList[2]));
				event = r.getSiegeEvent();

				event.clearActions();
				r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
				event.registerActions();
				r.setJdbcState(JdbcEntityState.UPDATED);
				r.update();

				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			case admin_quick_siege_stop:
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
					return false;

				event = r.getSiegeEvent();

				event.clearActions();
				ThreadPoolManager.getInstance().execute(() -> event.stopEvent(true));

				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			case admin_add_participant:
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[2]));
				if(r == null)
					return false;

				target = activeChar.getTarget();
				if(target == null || !target.isPlayer())
					return false;

				clan = target.getPlayer().getClan();

				if(clan == null)
					return false;

				event = r.getSiegeEvent();
				if(r.isCastle())
				{
					siegeClan = event.getSiegeClan(wordList[1], clan);
					if(siegeClan == null)
					{
						siegeClan = new SiegeClanObject(wordList[1], clan, 0L);
						event.addObject(wordList[1], siegeClan);
						SiegeClanDAO.getInstance().insert(r, siegeClan);
					}
				}
				else if(wordList.length == 4)
				{
					int bid = Integer.parseInt(wordList[3]);
					siegeClan = event.getSiegeClan(wordList[1], clan);
					if(siegeClan != null)
					{
						event.removeObject(siegeClan.getType(), siegeClan);
						SiegeClanDAO.getInstance().delete(r, siegeClan);
					}
					siegeClan = new AuctionSiegeClanObject(wordList[1], clan, bid);
					event.addObject(wordList[1], siegeClan);
					SiegeClanDAO.getInstance().insert(r, siegeClan);
				}

				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			case admin_remove_participant:
				r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[2]));
				if(r == null)
					return false;

				target = activeChar.getTarget();
				if(target == null || !target.isPlayer())
					return false;

				clan = target.getPlayer().getClan();

				if(clan == null)
					return false;

				event = r.getSiegeEvent();

				siegeClan = event.getSiegeClan(wordList[1], clan);
				if(siegeClan != null)
				{
					event.removeObject(siegeClan.getType(), siegeClan);
					SiegeClanDAO.getInstance().delete(r, siegeClan);
				}
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
