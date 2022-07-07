package  l2s.Phantoms.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.parsers.HuntingZone.HuntingZone;
import  l2s.Phantoms.parsers.HuntingZone.HuntingZoneHolder;
import  l2s.Phantoms.parsers.HuntingZone.HuntingZoneParser;
import  l2s.Phantoms.parsers.HuntingZone.HuntingZonePolygon;
import  l2s.commons.geometry.Polygon;
import  l2s.commons.lang.reference.HardReference;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.data.htm.HtmCache;
import  l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import  l2s.gameserver.listener.actor.player.OnAnswerListener;
import  l2s.gameserver.model.GameObject;
import  l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExShowTrace;
import  l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import  l2s.gameserver.utils.Location;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.network.l2.s2c.ExShowTerritory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.model.base.Race;

public class AdminPhantomHzEditor implements IAdminCommandHandler
{
	protected static final Logger _log = LoggerFactory.getLogger(AdminPhantomHzEditor.class);
	private Map<Integer, HuntingZone> _zones = new ConcurrentHashMap<>();
	private Map<Integer, HuntingZonePolygon> _newPolygon = new ConcurrentHashMap<>();
	int ITEMS_PER_PAGE = 15;
	
	static enum Commands
	{
		admin_hunting_zone,
		admin_hunting_zone_new,
		admin_hunting_zone_delete,
		admin_hunting_zone_cancel,
		admin_hunting_zone_save,
		admin_hunting_zone_set_param,
		admin_edit_hz;
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if (activeChar.getPlayerAccess().Menu)
		{
			Player fantom = null;
			GameObject target = activeChar.getTarget();
			switch (command)
			{
			case admin_hunting_zone_new:
			{
				
				HuntingZone hunting_zone = null;
				if (_zones.containsKey(activeChar.getObjectId())) // если зона есть - открываем
				{
					hunting_zone = HuntingZoneHolder.getInstance().getHZbyId(_zones.get(activeChar.getObjectId()).getId());
					activeChar.sendAdminMessage("Внимание!!! Вы не завершили редактирование зоны " + hunting_zone.getId() + " " + hunting_zone.getName() + "!");
					activeChar.sendAdminMessage("Отмените или сохраните результат.");
				} else
				{
					hunting_zone = new HuntingZone();
					hunting_zone.setId(HuntingZoneHolder.getInstance().getAllMap().size() == 0 ? 1 : Collections.max(HuntingZoneHolder.getInstance().getAllMap().keySet()) + 1);
					activeChar.sendAdminMessage("Внимание!!! Перед сохранением зоны заполните все обязательные параметры!");
					_zones.put(activeChar.getObjectId(), hunting_zone); // если нет - добавляем новою
				}
				if (hunting_zone != null)
				{
					String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneCreate.htm", activeChar);
					ShowBoardPacket.separateAndSend(replaceVar(activeChar, hunting_zone, dialog), activeChar);
				} else
				{
					activeChar.sendAdminMessage("Ошибка. Зоны " + Integer.parseInt(wordList[1]) + " не обнаружено.");
				}
				
				break;
			}
			case admin_hunting_zone_set_param:
			{
				HuntingZone tmp_hz = _zones.get(activeChar.getObjectId());
				if (wordList[1].equalsIgnoreCase("showpolygon"))
				{
					HuntingZonePolygon poly = tmp_hz.getPolygon().get(Integer.parseInt(wordList[2]));
					Polygon polygon = HuntingZoneParser.getInstance().parsePolygon(poly.coords).createPolygon();
					
					final ExShowTerritory exst = new ExShowTerritory(polygon.getZmin(), polygon.getZmax());
					for (String cord : poly.coords)
					{
						String[] tmp = cord.split(" ");
						exst.addVertice(new Location(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2])));
					}
					activeChar.sendPacket(exst);
					
				}
				if (wordList[1].equalsIgnoreCase("showallpolygon"))
				{
					for (HuntingZonePolygon poly : tmp_hz.getPolygon())
					{
						Polygon polygon = HuntingZoneParser.getInstance().parsePolygon(poly.coords).createPolygon();
						
						final ExShowTerritory exst = new ExShowTerritory(polygon.getZmin(), polygon.getZmax());
						for (String cord : poly.coords)
						{
							String[] tmp = cord.split(" ");
							exst.addVertice(new Location(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2])));
						}
						activeChar.sendPacket(exst);
					}
				}
				if (wordList[1].equalsIgnoreCase("addcurpoint"))
				{
					tmp_hz.addLoc(activeChar.getLoc());
				}
				if (wordList[1].equalsIgnoreCase("addnewpolygon"))
				{
					if (_newPolygon.containsKey(activeChar.getObjectId()))
						activeChar.sendAdminMessage("Сохраните или отмените текущий полигон.");
					else
						_newPolygon.put(activeChar.getObjectId(), new HuntingZonePolygon());
				}
				if (wordList[1].equalsIgnoreCase("cancelnewpolygon"))
				{
					_newPolygon.remove(activeChar.getObjectId());
				}
				if (wordList[1].equalsIgnoreCase("savenewpolygon"))
				{
					if (_newPolygon.containsKey(activeChar.getObjectId()))
					{
						HuntingZonePolygon tmp3 = _newPolygon.get(activeChar.getObjectId());
						if (tmp3.coords.size() > 3)
						{
							tmp_hz.addPolygon(tmp3);
							_newPolygon.remove(activeChar.getObjectId());
						} else
							activeChar.sendAdminMessage("Минимальное количество точек - 4");
					}
				}
				if (wordList[1].equalsIgnoreCase("addpointnewpoly"))
				{
					if (_newPolygon.containsKey(activeChar.getObjectId()))
					{
						HuntingZonePolygon tmp_poly = _newPolygon.get(activeChar.getObjectId());
						tmp_poly.coords
							.add(activeChar.getLoc().getX() + " " + activeChar.getLoc().getY() + " " + (activeChar.getLoc().getZ() - 200) + " " + (activeChar.getLoc().getZ() + 200));
						
						ExShowTrace trace = new ExShowTrace();
						trace.addTrace(activeChar.getLoc().getX(), activeChar.getLoc().getY(), activeChar.getLoc().getZ()+ 15,7000000);
						activeChar.sendPacket(trace);
						
						_newPolygon.put(activeChar.getObjectId(), tmp_poly);
					}
				} else if (wordList[1].equalsIgnoreCase("deletepolygon"))
				{
					tmp_hz.removePoligon(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("deleteppolygon"))
				{
					if (_newPolygon.containsKey(activeChar.getObjectId()))
					{
						HuntingZonePolygon tmp4 = _newPolygon.get(activeChar.getObjectId());
						tmp4.removeCords(wordList[2] + " " + wordList[3] + " " + wordList[4] + " " + wordList[5]);
					}
				} else if (wordList[1].equalsIgnoreCase("deleteclassid"))
				{
					tmp_hz.removeClassId(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("showpoint"))
				{
					ExShowTrace trace = new ExShowTrace();
					trace.addTrace(Integer.parseInt(wordList[2]), Integer.parseInt(wordList[3]), Integer.parseInt(wordList[4]) + 15,7000000);
					activeChar.sendPacket(trace);
				} else if (wordList[1].equalsIgnoreCase("showallpoint"))
				{
					ExShowTrace trace = new ExShowTrace();
					
					for (Location point : tmp_hz.getLoc())
						trace.addTrace(point.getX(), point.getY(), point.getZ() + 15,7000000);
					activeChar.sendPacket(trace);
				} else if (wordList[1].equalsIgnoreCase("deletepoint"))
				{
					tmp_hz.removePoint(Integer.parseInt(wordList[2]), Integer.parseInt(wordList[3]), Integer.parseInt(wordList[4]));
				} else if (wordList[1].equalsIgnoreCase("deletepw"))
				{
					tmp_hz.getPenalty().remove(WeaponType.valueOf(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("addpweapon"))
				{
					tmp_hz.getPenalty().add(WeaponType.valueOf(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("addclassid"))
				{
					tmp_hz.getClassId().add(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("setmaxpsize"))
				{
					tmp_hz.setMaxPartySize(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("setlvlmax"))
				{
					tmp_hz.setLvlMax(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("setcheckradius"))
				{
					tmp_hz.setCheckRadius(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("setrace"))
				{
					if (wordList[2].equalsIgnoreCase("ALL"))
						tmp_hz.setRace(null);
					else
						tmp_hz.setRace(Race.valueOf(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("setlvlmin"))
				{
					tmp_hz.setLvlMin(Integer.parseInt(wordList[2]));
				} else if (wordList[1].equalsIgnoreCase("setname"))
				{
					tmp_hz.setName(fullString.replace("admin_hunting_zone_set_param setname ", "").trim());
				}
				_zones.put(activeChar.getObjectId(), tmp_hz);
				String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneCreate.htm", activeChar);
				ShowBoardPacket.separateAndSend(replaceVar(activeChar, tmp_hz, dialog), activeChar);
				break;
			}
			case admin_hunting_zone_cancel:
			{
				if (_zones.containsKey(activeChar.getObjectId()))
				{
					activeChar.sendAdminMessage("Редактирование " + _zones.get(activeChar.getObjectId()).getId() + " " + _zones.get(activeChar.getObjectId()).getName() + " отменено.");
					String html = getHuntingZoneList(activeChar, HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneList.htm", activeChar), 0, 0);
					ShowBoardPacket.separateAndSend(html, activeChar);
					_zones.remove(activeChar.getObjectId());
					_newPolygon.remove(activeChar.getObjectId());
				} else
					activeChar.sendAdminMessage("Зоны для редактирования не обнаружено.");
				break;
			}
			case admin_hunting_zone_save:
			{
				if (_zones.containsKey(activeChar.getObjectId()))
				{
					HuntingZoneHolder.getInstance().addItems(_zones.get(activeChar.getObjectId()));
					activeChar.sendAdminMessage("HuntingZone " + _zones.get(activeChar.getObjectId()).getId() + " " + _zones.get(activeChar.getObjectId()).getName() + " saved.");
					
					String html = getHuntingZoneList(activeChar, HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneList.htm", activeChar), 0, 0);
					ShowBoardPacket.separateAndSend(html, activeChar);
					HuntingZoneParser.getInstance().Save();
					_zones.remove(activeChar.getObjectId());
					_newPolygon.remove(activeChar.getObjectId());
				} else
				{
					activeChar.sendAdminMessage("Зоны для редактирования не обнаружено.");
				}
				break;
			}
			case admin_hunting_zone:
			{
				if (_zones.containsKey(activeChar.getObjectId()))
				{
					ConfirmDlgPacket dlg = new ConfirmDlgPacket(SystemMsg.S1, 60000)
						.addString("Продолжить работу над зоной : " + _zones.get(activeChar.getObjectId()).getId() + " " + _zones.get(activeChar.getObjectId()).getName() + "?");
					activeChar.ask(dlg, new AnswerListener(activeChar));
					return true;
				}
				String html = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneList.htm", activeChar);
				for (String fdfd : wordList)
					activeChar.sendAdminMessage(fdfd);
				activeChar.sendAdminMessage("fullString:" + fullString);
				int sort = wordList.length >= 2 ? Integer.parseInt(wordList[1]) : 0;
				int page = wordList.length >= 3 ? Integer.parseInt(wordList[2]) : 0;
				html = getHuntingZoneList(activeChar, html, sort, page);
				ShowBoardPacket.separateAndSend(html, activeChar);
				break;
			}
			case admin_hunting_zone_delete:
			{
				HuntingZoneHolder.getInstance().removeHZ(Integer.parseInt(wordList[1]));
				String html = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneList.htm", activeChar);
				html = getHuntingZoneList(activeChar, html, 0, 0);
				ShowBoardPacket.separateAndSend(html, activeChar);
				break;
			}
			case admin_edit_hz:
			{
				activeChar.sendAdminMessage("fullString:" + fullString);
				HuntingZone hunting_zone = HuntingZoneHolder.getInstance().getHZbyId(Integer.parseInt(wordList[1]));
				if (_zones.containsKey(activeChar.getObjectId())) // если зона есть - открываем
					hunting_zone = HuntingZoneHolder.getInstance().getHZbyId(_zones.get(activeChar.getObjectId()).getId());
				else if (hunting_zone != null)
					_zones.put(activeChar.getObjectId(), hunting_zone); // если нет - добавляем новою
				if (hunting_zone != null)
				{
					String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneCreate.htm", activeChar);
					ShowBoardPacket.separateAndSend(replaceVar(activeChar, hunting_zone, dialog), activeChar);
				} else
				{
					activeChar.sendAdminMessage("Ошибка. Зоны " + Integer.parseInt(wordList[1]) + " не обнаружено.");
				}
				break;
			}
			default:
				break;
			}
		}
		return false;
	}
	
	private class AnswerListener implements OnAnswerListener
	{
		private HardReference<Player> _ref;
		
		protected AnswerListener(Player player)
		{
			_ref = player.getRef();
		}
		
		@Override
		public void sayYes()
		{
			Player player = _ref.get();
			if (player == null || !player.isOnline())
				return;
			
			HuntingZone hunting_zone = _zones.get(player.getObjectId());
			if (hunting_zone != null)
			{
				String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneCreate.htm", player);
				ShowBoardPacket.separateAndSend(replaceVar(player, hunting_zone, dialog), player);
			} else
			{
				player.sendAdminMessage("Ошибка. Запрошенной зоны не обнаружено.");
				String html = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneList.htm", player);
				html = getHuntingZoneList(player, html, 0, 0);
				ShowBoardPacket.separateAndSend(html, player);
				_zones.remove(player.getObjectId());
				_newPolygon.remove(player.getObjectId());
			}
		}
		
		@Override
		public void sayNo()
		{
			Player player = _ref.get();
			if (player == null || !player.isOnline())
				return;
			_zones.remove(player.getObjectId());
			_newPolygon.remove(player.getObjectId());
			String html = HtmCache.getInstance().getHtml("admin/cb/phantom/HuntingZoneList.htm", player);
			html = getHuntingZoneList(player, html, 0, 0);
			ShowBoardPacket.separateAndSend(html, player);
		}
	}
	
	private String replaceVar(Player player, HuntingZone hunting_zone, String dialog)
	{
		dialog = dialog.replaceAll("%name%", hunting_zone.getName());
		dialog = dialog.replaceAll("%lvlmin%", "" + hunting_zone.getLvlMin());
		dialog = dialog.replaceAll("%race%", hunting_zone.getRace() == null ? "ALL" : "" + hunting_zone.getRace());
		dialog = dialog.replaceAll("%checkradius%", "" + hunting_zone.getCheckRadius());
		dialog = dialog.replaceAll("%lvlmax%", "" + hunting_zone.getLvlMax());
		dialog = dialog.replaceAll("%maxpartysize%", "" + hunting_zone.getMaxPartySize());
		// Penalty Weapon
		StringBuilder pw = new StringBuilder();
		if (!hunting_zone.getPenalty().isEmpty())
			for (WeaponType tmp : hunting_zone.getPenalty())
			{
				pw.append("<tr>");
				pw.append("<td fixwidth=5></td>");
				pw.append("<td fixwidth=20>#</td>");
				pw.append("<td fixwidth=120>" + tmp.name() + "</td>");
				pw.append("<td fixwidth=35><button action=\"bypass -h admin_hunting_zone_set_param deletepw " + tmp.name()
					+ "\" value=\"-\" width=20 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				pw.append("</tr>");
			}
		dialog = dialog.replace("%PenaltyWeapon%", pw.toString());
		StringBuilder points = new StringBuilder();
		if (hunting_zone.getLoc() != null && !hunting_zone.getLoc().isEmpty())
			for (Location tmp : hunting_zone.getLoc())
			{
				points.append("<tr>");
				points.append("<td fixwidth=5></td>");
				points.append("<td fixwidth=20>#</td>");
				points.append("<td fixwidth=50>" + tmp.getX() + "</td>");
				points.append("<td fixwidth=50>" + tmp.getY() + "</td>");
				points.append("<td fixwidth=50>" + tmp.getZ() + "</td>");
				points.append("<td fixwidth=50><button action=\"bypass -h admin_teleport " + tmp.getX() + " " + tmp.getY() + " " + tmp.getZ()
					+ "\" value=\"Tele\" width=45 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				points.append("<td fixwidth=50><button action=\"bypass -h admin_hunting_zone_set_param showpoint " + tmp.getX() + " " + tmp.getY() + " " + tmp.getZ()
					+ "\" value=\"Show\" width=45 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				points.append("<td fixwidth=50><button action=\"bypass -h admin_hunting_zone_set_param deletepoint " + tmp.getX() + " " + tmp.getY() + " " + tmp.getZ()
					+ "\" value=\"-\" width=20 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				points.append("</tr>");
			}
		dialog = dialog.replace("%pointsl%", points.toString());
		
		StringBuilder polygonStr = new StringBuilder();
		if (hunting_zone.getPolygon() != null && !hunting_zone.getPolygon().isEmpty())
			for (int i = 0; i < hunting_zone.getPolygon().size(); i++)
			{
				String poly_cords = Rnd.get(hunting_zone.getPolygon().get(i).coords);
				Location rnd_loc = Location.parseLoc(poly_cords).correctGeoZ();
				polygonStr.append("<tr>");
				polygonStr.append("<td fixwidth=5></td>");
				polygonStr.append("<td fixwidth=20>#</td>");
				polygonStr.append("<td fixwidth=60>" + i + "</td>");
				polygonStr.append("<td fixwidth=60>" + hunting_zone.getPolygon().get(i).coords.size() + "</td>");
				polygonStr.append("<td fixwidth=60><button action=\"bypass -h admin_hunting_zone_set_param showpolygon " + i
					+ "\" value=\"Show\" width=45 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				polygonStr.append("<td fixwidth=60><button action=\"bypass -h admin_hunting_zone_set_param deletepolygon " + i
					+ "\" value=\"-\" width=20 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				polygonStr.append("<td fixwidth=50><button action=\"bypass -h admin_teleport " + rnd_loc.getX() + " " + rnd_loc.getY() + " " + rnd_loc.getZ()
					+ "\" value=\"Tele\" width=45 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				polygonStr.append("</tr>");
			}
		dialog = dialog.replace("%polygonl%", polygonStr.toString());
		
		StringBuilder classidStr = new StringBuilder();
		if (hunting_zone.getClassId() != null && !hunting_zone.getClassId().isEmpty())
			for (int tmpclassid : hunting_zone.getClassId())
			{
				classidStr.append("<tr>");
				classidStr.append("<td fixwidth=5></td>");
				classidStr.append("<td fixwidth=20>#</td>");
				classidStr.append("<td fixwidth=60>" + tmpclassid + "</td>");
				classidStr.append("<td fixwidth=35><button action=\"bypass -h admin_hunting_zone_set_param deleteclassid " + tmpclassid
					+ "\" value=\"-\" width=20 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				classidStr.append("</tr>");
			}
		dialog = dialog.replace("%classidlist%", classidStr.toString());
		
		String polygon_html = "";
		if (_newPolygon.containsKey(player.getObjectId()))
		{
			HuntingZonePolygon tmp = _newPolygon.get(player.getObjectId());
			polygon_html = HtmCache.getInstance().getHtml("admin/cb/phantom/PhantomNewPolygon.htm", player);
			StringBuilder newpointStr = new StringBuilder();
			for (String cord : tmp.coords)
			{
				String[] tmp2 = cord.split(" ");
				newpointStr.append("<tr>");
				newpointStr.append("<td fixwidth=5></td>");
				newpointStr.append("<td fixwidth=20>#</td>");
				newpointStr.append("<td fixwidth=50>" + tmp2[0] + "</td>");
				newpointStr.append("<td fixwidth=50>" + tmp2[1] + "</td>");
				newpointStr.append("<td fixwidth=50>" + tmp2[2] + "</td>");
				newpointStr.append("<td fixwidth=50>" + tmp2[3] + "</td>");
				newpointStr.append("<td fixwidth=50><button action=\"bypass -h admin_teleport " + tmp2[0] + " " + tmp2[1] + " " + tmp2[2]
					+ "\" value=\"Tele\" width=45 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				newpointStr.append("<td fixwidth=50><button action=\"bypass -h admin_hunting_zone_set_param deleteppolygon " + tmp2[0] + " " + tmp2[1] + " " + tmp2[2] + " " + tmp2[3]
					+ "\" value=\"-\" width=20 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				newpointStr.append("</tr>");
			}
			polygon_html = polygon_html.replace("%point_new_poligon%", newpointStr.toString());
			
		}
		dialog = dialog.replace("%new_polygon%", polygon_html);
		
		return dialog;
	}
	
	private List<HuntingZone> getSearchedHZ(int sort)
	{
		HuntingZoneComparator bvc = new HuntingZoneComparator(sort);
		List<HuntingZone> result = HuntingZoneHolder.getInstance().getAll();
		result.sort(bvc);
		return result;
	}
	
	private String getHuntingZoneList(Player visitor, String html, int sort, int page)
	{
		StringBuilder builder = new StringBuilder();
		List<HuntingZone> allHuntingZone = getSearchedHZ(sort);
		int i = 0;
		for (HuntingZone entry : allHuntingZone)
		{
			if (i < page * ITEMS_PER_PAGE)
			{
				i++;
				continue;
			}
			builder.append("<table bgcolor=").append(getLineColor(i)).append(" width=680 height=26 border=0 cellpadding=0 cellspacing=0><tr>");
			builder.append("<td width=210 height=20>").append("<a action=\"bypass -h admin_edit_hz " + entry.getId() + "\">").append(entry.getName()).append("</a>").append("</td>");
			builder.append("<td width=65 height=20>").append(entry.getLvlMin()).append("</td>");
			builder.append("<td width=65 height=20>").append(entry.getLvlMax()).append("</td>");
			builder.append("<td width=80 height=20>").append(entry.getRace() == null ? "ALL" : entry.getRace().name()).append("</td>");
			builder.append("<td width=100 height=20>").append(entry.getCheckRadius()).append("</td>");
			builder.append("<td width=100 height=20><center>").append((entry.getPenalty() == null || entry.getPenalty().isEmpty()) ? "-" : "True").append("</center></td>");
			builder.append("<td width=60 height=20>").append(
				"<button action=\"bypass -h admin_hunting_zone_delete " + entry.getId() + "\" value=\"-\" width=40 height=19 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">")
				.append("</td>");
			builder.append("</tr></table>");
			i++;
			if (i >= (page * ITEMS_PER_PAGE + ITEMS_PER_PAGE - 1))
			{
				break;
			}
		}
		builder.append("<img src=\"L2UI_CT1.Gauge_DF_CP_Center\" width=680 height=2>");
		// Prev
		builder.append("<center><table width=680><tr>");
		if (page > 0)
			builder.append("<td width=300 align=right><button value=\"Назад\" action=\"bypass -h admin_hunting_zone %sort% " + (page - 1)
				+ "\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		else
			builder.append("<td width=300 align=right><br></td>");
		// Next
		if (allHuntingZone.size() > i)
			builder.append("<td width=300 align=left><button value=\"Вперед\" action=\"bypass -h admin_hunting_zone %sort% " + (page + 1)
				+ "\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		else
			builder.append("<td width=300 align=right><br></td>");
		builder.append("</tr></table></center>");
		html = html.replace("%hunting_zone%", builder.toString());
		html = html.replace("%page%", String.valueOf(page));
		html = html.replace("%sort%", String.valueOf(sort));
		for (i = 1; i <= 6; i++)
			if (Math.abs(sort) == i)
				html = html.replace("%sort" + i + "%", String.valueOf(-sort));
			else
				html = html.replace("%sort" + i + "%", String.valueOf(i));
		return html;
	}
	
	private String getLineColor(int i)
	{
		if (i % 2 == 0)
			return "890202";
		else
			return "211618";
	}
	
	class HuntingZoneComparator implements Comparator<HuntingZone>
	{
		private int _sort;
		
		public HuntingZoneComparator(int sort)
		{
			_sort = sort;
		}
		
		private int sortById(HuntingZone temp1, HuntingZone temp2, int sortId)
		{
			switch (sortId)
			{
			case 1:
				return temp1.getName().compareTo(temp2.getName());
			case -1:
				return temp2.getName().compareTo(temp1.getName());
			case 2:
				return Integer.compare(temp1.getLvlMin(), temp2.getLvlMin());
			case -2:
				return Integer.compare(temp2.getLvlMin(), temp1.getLvlMin());
			case 3:
				return Integer.compare(temp1.getLvlMax(), temp2.getLvlMax());
			case -3:
				return Integer.compare(temp2.getLvlMax(), temp1.getLvlMax());
			case 4:
				return temp1.getRace() == null ? -1 : temp2.getRace() == null ? -1 : temp1.getRace().name().compareTo(temp2.getRace().name());
			case -4:
				return temp2.getRace() == null ? -1 : temp1.getRace() == null ? -1 : temp2.getRace().name().compareTo(temp1.getRace().name());
			case 5:
				return Integer.compare(temp1.getCheckRadius(), temp2.getCheckRadius());
			case -5:
				return Integer.compare(temp2.getCheckRadius(), temp1.getCheckRadius());
			case 6:// TODO
			{
				return 0;
			}
			case -6:
			{
				return 0;
			}
			}
			return 0;
		}
		
		@Override
		public int compare(HuntingZone a, HuntingZone b)
		{
			int sortResult = sortById(a, b, _sort);
			if (sortResult == 0 && Math.abs(_sort) != 3)
				sortResult = sortById(a, b, 3);
			if (sortResult == 0 && Math.abs(_sort) != 1)
				sortResult = sortById(a, b, 1);
			if (sortResult == 0 && Math.abs(_sort) != 2)
				sortResult = sortById(a, b, 2);
			return sortResult;
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
}
