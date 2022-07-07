package handler.admincommands;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.ShapeHolder;
import l2s.gameserver.data.xml.parser.ShapeParser;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Territory;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExShowTrace;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author KanuToIIIKa
 */

public class AdminSpawnMaker extends ScriptAdminCommand
{
	private static final Logger LOGGER = LogManager.getLogger(AdminSpawnMaker.class);

	private final String SPAWN_DIR = "data/spawn/spawnmaker";

	@Override
	public void onInit()
	{
		super.onInit();
	}

	private enum Commands
	{
		admin_spawnmaker,
		admin_spawnmaker_create,
		admin_spawnmaker_select,
		admin_spawnmaker_delete,
		admin_spawnmaker_trace,
		admin_spawnmaker_move
	}

	private final TIntObjectHashMap<MakerStage> admin_stage = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<Map<MakerStage, String>> admin_choice = new TIntObjectHashMap<>();

	private enum MakerStage
	{
		file,
		territory,
		point,
		spawn
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] worldlist, String fullString, Player admin)
	{
		Commands command = (Commands) comm;

		HtmlMessage html = new HtmlMessage(5);
		html.setFile("admin/spawnmaker/index.htm");
		if(!admin_stage.containsKey(admin.getObjectId()))
		{
			admin_stage.put(admin.getObjectId(), MakerStage.file);

			Map<MakerStage, String> choice_map = new HashMap<>(3);
			choice_map.put(MakerStage.file, null);
			choice_map.put(MakerStage.territory, null);
			choice_map.put(MakerStage.spawn, null);

			admin_choice.put(admin.getObjectId(), choice_map);
		}

		MakerStage stage = admin_stage.get(admin.getObjectId());
		try
		{
			switch(command)
			{
				case admin_spawnmaker:
				{
					if(worldlist.length == 2)
					{
						switch(MakerStage.valueOf(worldlist[1]))
						{
							case file:
							{
								admin_stage.put(admin.getObjectId(), MakerStage.file);
								admin_choice.get(admin.getObjectId()).put(MakerStage.file, null);
								admin_choice.get(admin.getObjectId()).put(MakerStage.territory, null);
								admin_choice.get(admin.getObjectId()).put(MakerStage.spawn, null);
								break;
							}
							case territory:
							{
								admin_stage.put(admin.getObjectId(), MakerStage.territory);
								admin_choice.get(admin.getObjectId()).put(MakerStage.territory, null);
								admin_choice.get(admin.getObjectId()).put(MakerStage.spawn, null);
								break;
							}
							case spawn:
							{
								admin_stage.put(admin.getObjectId(), MakerStage.point);
								break;
							}

						}
					}
					break;
				}
				case admin_spawnmaker_create:
				{

					switch(stage)
					{
						case file:
						{
							if(worldlist.length != 2)
							{
								admin.sendMessage("Неверно указано имя файла.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							if(!createFile(worldlist[1]))
								admin.sendMessage("Такой файл уже существует.");
							else
								admin.sendMessage(worldlist[1] + ".xml создан.");

							break;
						}
						case territory:
						{
							if(worldlist.length != 2)
							{
								admin.sendMessage("Неверно указано имя территории.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							createTerritory(admin, worldlist[1]);
							break;
						}
						case point:
						{
							createPoint(admin);
							break;
						}
						case spawn:
						{
							if(worldlist.length != 5)
							{
								admin.sendMessage("Недостаточно аргументов для создания спауна.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							createSpawn(admin, worldlist);
							break;
						}
					}
					break;
				}
				case admin_spawnmaker_select:
				{
					switch(stage)
					{
						case file:
						{
							if(worldlist.length != 2)
							{
								admin.sendMessage("Неверно указано имя файла.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							admin_stage.put(admin.getObjectId(), MakerStage.territory);
							admin_choice.get(admin.getObjectId()).put(stage, worldlist[1]);
							break;
						}
						case territory:
						{
							if(worldlist.length != 2)
							{
								admin.sendMessage("Неверно указано имя территории.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							admin_stage.put(admin.getObjectId(), MakerStage.point);
							admin_choice.get(admin.getObjectId()).put(stage, worldlist[1]);
							break;
						}
						case point:
						{
							admin_stage.put(admin.getObjectId(), MakerStage.spawn);
							break;
						}
						case spawn:
						{
							break;
						}
					}
					break;
				}
				case admin_spawnmaker_delete:
				{
					switch(stage)
					{
						case file:
						{
							if(worldlist.length != 2)
							{
								admin.sendMessage("Неверно указано имя файла.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							if(!deleteFile(worldlist[1]))
								admin.sendMessage(worldlist[1] + " не существует.");
							else
								admin.sendMessage(worldlist[1] + " был удален.");
							break;
						}
						case territory:
						{
							if(worldlist.length != 2)
							{
								admin.sendMessage("Неверно указано имя файла.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}

							deleteTerritory(admin, worldlist[1]);
							break;
						}
						case point:
						{
							if(worldlist.length != 5)
							{
								admin.sendMessage("Неверно указана точка.");
								useAdminCommand(Commands.admin_spawnmaker, new String[] {}, Commands.admin_spawnmaker.name(), admin);
								return false;
							}
							deletePoint(admin, worldlist);
							break;
						}
						case spawn:
						{
							if(worldlist.length == 2)
								deleteSpawn(admin, worldlist[1]);
							break;
						}
					}
					break;
				}
				case admin_spawnmaker_trace:
				{
					tracePoint(admin, worldlist);
					break;
				}
				case admin_spawnmaker_move:
				{
					moveToPolygon(admin);
					break;
				}
			}

			switch(admin_stage.get(admin.getObjectId()))
			{
				case file:
					replaceFile(admin, html);
					break;
				case territory:
					replaceTerritory(admin, html);
					break;
				case point:
					replacePoint(admin, html);
					break;
				case spawn:
					replaceSpawn(admin, html);
					break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		admin.sendPacket(html);

		return true;
	}

	private void moveToPolygon(Player admin) throws DocumentException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);

		String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document doc = getDoc(filename);
		if(doc == null)
			return;

		Element root = doc.getRootElement();

		Iterator<Element> it = root.elementIterator("territory");

		PolygonBuilder polygonBuilder = new PolygonBuilder();
		while(it.hasNext())
		{
			Element e = it.next();
			if(e.attributeValue("name").equals(territoryname))
			{
				Iterator<Element> add = e.elementIterator("add");
				while(add.hasNext())
				{
					Element v = add.next();
					polygonBuilder.add(Integer.parseInt(v.attributeValue("x")), Integer.parseInt(v.attributeValue("y")));
					polygonBuilder.setZmax(Integer.parseInt(v.attributeValue("zmax")));
					polygonBuilder.setZmin(Integer.parseInt(v.attributeValue("zmin")));
				}
				break;
			}
		}

		if(polygonBuilder.validate())
		{
            Polygon p = polygonBuilder.createPolygon();

			Territory t = new Territory();
			t.add(p);
			admin.teleToLocation(t.getRandomLoc(admin.getGeoIndex()));
		}
	}

	private void deleteSpawn(Player admin, String id) throws DocumentException, IOException
	{
		deleteSpawn(admin, id, null);
	}

	private void deleteSpawn(Player admin, String id, String territoryname) throws DocumentException, IOException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);

		if(territoryname == null)
			territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document document = getDoc(filename);
		if(document == null)
			return;

		Element root = document.getRootElement();

		Iterator<Element> spawnIterator = root.elementIterator("spawn");
		while(spawnIterator.hasNext())
		{

			Element spawnElement = spawnIterator.next();
			if(Objects.equals(spawnElement.attributeValue("territory"), territoryname))
			{
				Iterator<Element> npcIterator = spawnElement.elementIterator();
				while(npcIterator.hasNext())
				{
					Element npcElement = npcIterator.next();
					if(npcElement.attributeValue("id").equals(id))
					{
						npcElement.detach();
						SpawnManager.getInstance().reloadAll();
						flushDoc(filename, document);

						AdminCommandHandler.getInstance().useAdminCommandHandler(admin, "admin_reload_spawn");
						return;
					}
				}
			}

		}
	}

	private void createSpawn(Player admin, String[] worldlist) throws DocumentException, IOException
	{
		if(!worldlist[1].matches("^\\d+$") || !worldlist[2].matches("^\\d+$") || !worldlist[3].matches("^\\d+$") || !worldlist[4].matches("^\\d+$"))
		{
			admin.sendMessage("Неверно заданые аргументы.");
			return;
		}

		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);
		String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document document = getDoc(filename);

		if(document == null)
			return;

		Element root = document.getRootElement();

		ShapeParser.getInstance().reload();

		if(ShapeHolder.getInstance().getShape(territoryname) == null)
		{
			admin.sendMessage("Такая территории не существует.");
			return;
		}

		if(NpcHolder.getInstance().getTemplate(Integer.parseInt(worldlist[1])) == null)
		{
			admin.sendMessage("Такой npc_id не существует.");
			return;
		}

		Iterator<Element> spawnIterator = root.elementIterator("spawn");

		while(spawnIterator.hasNext())
		{
			Element spawnElement = spawnIterator.next();
			if(Objects.equals(spawnElement.attributeValue("territory"), territoryname))
			{
				Element npc = spawnElement.addElement("npc");

				npc.addAttribute("id", worldlist[1]);
				npc.addAttribute("count", worldlist[2]);
				npc.addAttribute("respawn", worldlist[3]);
				npc.addAttribute("respawn_random", worldlist[4]);

				spawnElement.addComment(NpcHolder.getInstance().getTemplate(Integer.parseInt(worldlist[1])).getName());

				flushDoc(filename, document);
				AdminCommandHandler.getInstance().useAdminCommandHandler(admin, "admin_reload_spawn");
				return;
			}
		}

		Element spawn = root.addElement("spawn");
		spawn.addAttribute("name", territoryname);
		spawn.addAttribute("territory", territoryname);

		Element npc = spawn.addElement("npc");

		npc.addAttribute("id", worldlist[1]);
		npc.addAttribute("count", worldlist[2]);
		npc.addAttribute("respawn", worldlist[3]);
		npc.addAttribute("respawn_random", worldlist[4]);

		spawn.addComment(NpcHolder.getInstance().getTemplate(Integer.parseInt(worldlist[1])).getName());

		flushDoc(filename, document);

		AdminCommandHandler.getInstance().useAdminCommandHandler(admin, "admin_reload_spawn");
	}

	private void tracePoint(Player admin, String[] worldlist) throws DocumentException
	{
		if(worldlist.length == 1)
		{
			String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);
			String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

			Document document = getDoc(filename);

			if(document == null)
				return;

			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator("territory");

			while(it.hasNext())
			{
				Element territory = it.next();
				if(territory.attributeValue("name").equals(territoryname))
				{
					Iterator<Element> it2 = territory.elementIterator("add");

					ExShowTrace trace = new ExShowTrace();
					Location first = null;
					Location old = null;
					while(it2.hasNext())
					{
						Element point = it2.next();

						int x = Integer.parseInt(point.attributeValue("x"));
						int y = Integer.parseInt(point.attributeValue("y"));
						int zmin = Integer.parseInt(point.attributeValue("zmin"));
						int zmax = Integer.parseInt(point.attributeValue("zmax"));

						int z = zmin + (zmax - zmin) / 2;

						Location current = new Location(x, y, z).correctGeoZ();

						if(old != null)
							trace.addLine(old, current, 65, 15000);
						else
							first = new Location(x, y, z).correctGeoZ();

						old = current;
					}

					if(first != null)
						trace.addLine(old, first, 65, 15000);
					admin.sendPacket(trace);
					break;
				}
			}
		}
		else if(worldlist.length == 5)
		{
			int x = Integer.parseInt(worldlist[1]);
			int y = Integer.parseInt(worldlist[2]);
			int zmin = Integer.parseInt(worldlist[3]);
			int zmax = Integer.parseInt(worldlist[4]);

			int z = zmin + (zmax - zmin) / 2;

			z = GeoEngine.getHeight(x, y, z, 0);

			ExShowTrace trace = new ExShowTrace();

			trace.addLine(admin.getLoc(), new Location(x, y, z), 65, 15000);
			admin.sendPacket(trace, new RadarControlPacket(0, 2, x, y, z));
		}
	}

	private void deletePoint(Player admin, String[] worldlist) throws DocumentException, IOException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);
		String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document document = getDoc(filename);

		if(document == null)
			return;

		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator("territory");
		main: while(it.hasNext())
		{
			Element territory = it.next();
			if(territory.attributeValue("name").equals(territoryname))
			{
				Iterator<Element> it2 = territory.elementIterator("add");

				while(it2.hasNext())
				{
					Element point = it2.next();

					boolean x = point.attributeValue("x").equals(worldlist[1]);
					boolean y = point.attributeValue("y").equals(worldlist[2]);
					boolean zmin = point.attributeValue("zmin").equals(worldlist[3]);
					boolean zmax = point.attributeValue("zmax").equals(worldlist[4]);

					if(x && y && zmin && zmax)
					{
						territory.remove(point);
						break main;
					}
				}
				break;
			}
		}

		flushDoc(filename, document);
	}

	private void createPoint(Player admin) throws DocumentException, IOException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);
		String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document document = getDoc(filename);

		if(document == null)
			return;

		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator("territory");

		while(it.hasNext())
		{
			Element territory = it.next();
			if(territory.attributeValue("name").equals(territoryname))
			{
				Element point = territory.addElement("add");

				Location loc = admin.getLoc();
				point.addAttribute("x", String.valueOf(loc.x));
				point.addAttribute("y", String.valueOf(loc.y));
				point.addAttribute("zmin", String.valueOf(loc.z - 250));
				point.addAttribute("zmax", String.valueOf(loc.z + 250));
				break;
			}
		}

		flushDoc(filename, document);
	}

	private void replacePoint(Player admin, HtmlMessage html) throws DocumentException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);
		String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document document = getDoc(filename);

		if(document == null)
			return;

		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator("territory");
		StringBuilder sb = new StringBuilder();

		while(it.hasNext())
		{
			Element territory = it.next();
			if(territory.attributeValue("name").equals(territoryname))
			{
				Iterator<Element> it2 = territory.elementIterator("add");
				while(it2.hasNext())
				{
					Element point = it2.next();

					String x = point.attributeValue("x");
					String y = point.attributeValue("y");
					String zmin = point.attributeValue("zmin");
					String zmax = point.attributeValue("zmax");
					sb.append("<tr>");
					sb.append("<td width=55>").append(x).append("</td>");
					sb.append("<td width=55>").append(y).append("</td>");
					sb.append("<td width=55>").append(zmin).append("</td>");
					sb.append("<td width=55>").append(zmax).append("</td>");
					sb.append("<td width=52>").append(HtmlUtils.htmlButton("trace", "bypass -h admin_spawnmaker_trace " + x + " " + y + " " + zmin + " " + zmax, 46)).append("</td>");
					sb.append("<td width=52>").append(HtmlUtils.htmlButton("delete", "bypass -h admin_spawnmaker_delete " + x + " " + y + " " + zmin + " " + zmax, 46)).append("</td>");
					sb.append("</tr>");
				}
				break;
			}
		}

		String fileHtml = HtmCache.getInstance().getHtml("admin/spawnmaker/point_info.htm", admin);

		html.replace("%info%", fileHtml);
		html.replace("%file%", HtmlUtils.htmlButton(filename, "bypass -h admin_spawnmaker file", 210));
		html.replace("%territory%", HtmlUtils.htmlButton(territoryname, "bypass -h admin_spawnmaker territory", 210));
		html.replace("%points%", sb.toString());

	}

	private void createTerritory(Player activeChar, String name) throws DocumentException, IOException
	{
		String filename = admin_choice.get(activeChar.getObjectId()).get(MakerStage.file);

		Document document = getDoc(filename);
		if(document == null)
		{
			activeChar.sendMessage("Файл с территориями отсутствует.");
			return;
		}

		Element root = document.getRootElement();

		List<Element> territories = root.elements("territory");

		for(Element e : territories)
			if(e.attributeValue("name").equals(name))
			{
				activeChar.sendMessage("Такая территория уже существует.");
				return;
			}

		Element territory = root.addElement("territory");
		territory.addAttribute("name", name);

		flushDoc(filename, document);
	}

	private void deleteTerritory(Player admin, String name) throws DocumentException, IOException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);

		Document doc = getDoc(filename);
		if(doc == null)
			return;

		Element root = doc.getRootElement();

		Iterator<Element> it = root.elementIterator("territory");

		while(it.hasNext())
		{
			Element e = it.next();
			if(e.attributeValue("name").equals(name))
			{
				root.remove(e);
				flushDoc(filename, doc);
				break;
			}
		}

		it = root.elementIterator("spawn");

		while(it.hasNext())
		{
			Element e = it.next();
			String sname = e.attributeValue("name");
			Iterator<Element> ti = e.elementIterator("territory");
			while(ti.hasNext())
			{
				Element t = ti.next();
				if(t.attributeValue("name").equals(name))
					deleteSpawn(admin, sname, name);
			}
		}
	}

	private void replaceTerritory(Player activeChar, HtmlMessage html) throws DocumentException
	{

		String filename = admin_choice.get(activeChar.getObjectId()).get(MakerStage.file);

		Document document = getDoc(filename);

		if(document == null)
			return;

		Element root = document.getRootElement();

		List<Element> territories = root.elements("territory");

		StringBuilder sb = new StringBuilder();
		for(Element t : territories)
		{
			String name = t.attributeValue("name");

			sb.append("<tr>");
			sb.append("<td align=right width=290>").append(name).append("</td>");
			sb.append("<td align=center>").append("<button value=\"Select\" action=\"bypass -h admin_spawnmaker_select " + name + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">").append("</td>");
			sb.append("<td align=cemter>").append("<button value=\"Delete\" action=\"bypass -h admin_spawnmaker_delete " + name + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">").append("</td>");
			sb.append("</tr>");
		}

		String fileHtml = HtmCache.getInstance().getHtml("admin/spawnmaker/territory_info.htm", activeChar);

		html.replace("%info%", fileHtml);
		html.replace("%file%", HtmlUtils.htmlButton(filename, "bypass -h admin_spawnmaker file", 210));
		html.replace("%territorylist%", sb.toString());
	}

	private void replaceSpawn(Player admin, HtmlMessage html) throws DocumentException
	{
		String filename = admin_choice.get(admin.getObjectId()).get(MakerStage.file);
		String territoryname = admin_choice.get(admin.getObjectId()).get(MakerStage.territory);

		Document document = getDoc(filename);

		if(document == null)
			return;

		Element root = document.getRootElement();

		String fileHtml = HtmCache.getInstance().getHtml("admin/spawnmaker/spawn_info.htm", admin);
		StringBuilder sb = new StringBuilder();

		Iterator<Element> spawnIterator = root.elementIterator("spawn");

		while(spawnIterator.hasNext())
		{
			Element spawnElement = spawnIterator.next();

			if(Objects.equals(spawnElement.attributeValue("territory"), territoryname))
			{
				for(Element npc : spawnElement.elements())
				{
					String npc_id = npc.attributeValue("id");
					String name = NpcHolder.getInstance().getTemplate(Integer.parseInt(npc_id)).getName();
					String count = npc.attributeValue("count");
					String respawn = npc.attributeValue("respawn");
					String respawn_random = npc.attributeValue("respawn_random");

					sb.append("<tr>");
					sb.append("<td align=left width=58>").append(name).append("</td>");
					sb.append("<td align=center width=42>").append(npc_id).append("</td>");
					sb.append("<td align=center width=34>").append(count).append("</td>");
					sb.append("<td align=center width=48>").append(respawn).append("</td>");
					sb.append("<td align=center width=48>").append(respawn_random).append("</td>");
					sb.append("<td align=center width=50>").append(HtmlUtils.htmlButton("delete", "bypass -h admin_spawnmaker_delete " + npc_id, 52)).append("</td>");
					sb.append("</tr>");
				}
				break;
			}
		}

		html.replace("%info%", fileHtml);
		html.replace("%file%", HtmlUtils.htmlButton(filename, "bypass -h admin_spawnmaker file", 210));
		html.replace("%territory%", HtmlUtils.htmlButton(territoryname, "bypass -h admin_spawnmaker territory", 210));
		html.replace("%spawns%", sb.toString());
	}

	private void replaceFile(Player activeChar, HtmlMessage html)
	{
		var dir = Config.DATAPACK_ROOT_PATH.resolve(SPAWN_DIR);
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			LOGGER.error("Can't create directories.", e);
		}

		StringBuilder sb = new StringBuilder();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.xml")) {
			for (Path path : stream) {
				sb.append("<tr>");
				Path fileName = path.getFileName();
				sb.append("<td align=right width=290>").append(fileName).append("</td>");
				sb.append("<td align=center>")
						.append("<button value=\"Select\" action=\"bypass -h admin_spawnmaker_select "
								+ fileName + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">").append("</td>");
				sb.append("<td align=cemter>")
						.append("<button value=\"Delete\" action=\"bypass -h admin_spawnmaker_delete "
								+ fileName + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">").append("</td>");
				sb.append("</tr>");
			}
		} catch (IOException e) {
			LOGGER.error("Can't read files in directory.", e);
		}

		String fileHtml = HtmCache.getInstance().getHtml("admin/spawnmaker/file_info.htm", activeChar);

		html.replace("%info%", fileHtml);
		html.replace("%filelist%", sb.toString());
	}

	private void flushDoc(String filename, Document document) throws IOException
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(new File(Config.DATAPACK_ROOT, SPAWN_DIR + "/" + filename)), format);
		writer.write(document);
		writer.close();
	}

	private Document getDoc(String filename) throws DocumentException
	{
		if(filename == null)
			return null;
		File file = new File(Config.DATAPACK_ROOT, SPAWN_DIR + "/" + filename);
		if(!file.exists())
			return null;

		SAXReader reader = new SAXReader();

        return reader.read(file);
	}

	private boolean deleteFile(String filename)
	{
		File file = new File(Config.DATAPACK_ROOT, SPAWN_DIR + "/" + filename);
		if(!file.exists())
			return false;

		file.delete();

		return true;
	}

	private boolean createFile(String filename) throws DocumentException, IOException
	{
		File file = new File(Config.DATAPACK_ROOT, SPAWN_DIR + "/" + filename + ".xml");
		if(file.exists())
			return false;

		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("utf-8");
		document.addDocType("list", null, "../spawn.dtd");
		document.addElement("list");

		flushDoc(filename + ".xml", document);

		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

}
