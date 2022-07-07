package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.model.Territory;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.io.File;
import java.util.*;

public class InstantZoneParser extends AbstractParser<InstantZoneHolder>
{
	private static final InstantZoneParser _instance;

	public static InstantZoneParser getInstance()
	{
		return _instance;
	}

	public InstantZoneParser()
	{
		super(InstantZoneHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/instances/");
	}

	@Override
	public String getDTDFileName()
	{
		return "instances.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			SchedulingPattern resetReuse = new SchedulingPattern("30 6 * * *");
			int timelimit = -1;
            IntObjectMap<InstantZone.DoorInfo> doors = Containers.emptyIntObjectMap();
            int instanceId = Integer.parseInt(element.attributeValue("id"));
			String name = element.attributeValue("name");
			String n = element.attributeValue("timelimit");
			if(n != null)
				timelimit = Integer.parseInt(n);
			n = element.attributeValue("collapseIfEmpty");
            int collapseIfEmpty = Integer.parseInt(n);
            n = element.attributeValue("maxChannels");
            int maxChannels = Integer.parseInt(n);
            n = element.attributeValue("dispelBuffs");
            boolean dispelBuffs = n != null && Boolean.parseBoolean(n);
            int minLevel = 0;
			int maxLevel = 0;
			int minParty = 1;
			int maxParty = 9;
			int mapx = -1;
			int mapy = -1;
			List<Location> teleportLocs = Collections.emptyList();
			Location ret = null;
			Iterator<Element> subIterator = element.elementIterator();
            Map<String, InstantZone.SpawnInfo2> spawns2 = Collections.emptyMap();
            Map<String, InstantZone.ZoneInfo> zones = Collections.emptyMap();
            List<InstantZone.SpawnInfo> spawns = new ArrayList<>();
            StatsSet params = new StatsSet();
            boolean setReuseUponEntry = true;
            boolean removedItemNecessity = false;
            int requiredQuestId = 0;
            int givedItemCount = 0;
            int giveItemId = 0;
            int removedItemCount = 0;
            int removedItemId = 0;
            InstantZone.SpawnInfo spawnDat = null;
            int spawnType = 0;
            int sharedReuseGroup = 0;
            boolean onPartyDismiss = true;
            int timer = 60;
            List<ChancedItemData> rewards = new ArrayList<>();
            while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				if("level".equalsIgnoreCase(subElement.getName()))
				{
					minLevel = subElement.attributeValue("min") == null ? 1 : Integer.parseInt(subElement.attributeValue("min"));
					maxLevel = subElement.attributeValue("max") == null ? Integer.MAX_VALUE : Integer.parseInt(subElement.attributeValue("max"));
				}
				else if("collapse".equalsIgnoreCase(subElement.getName()))
				{
					onPartyDismiss = Boolean.parseBoolean(subElement.attributeValue("on-party-dismiss"));
					timer = Integer.parseInt(subElement.attributeValue("timer"));
				}
				else if("party".equalsIgnoreCase(subElement.getName()))
				{
					minParty = Integer.parseInt(subElement.attributeValue("min"));
					maxParty = Integer.parseInt(subElement.attributeValue("max"));
				}
				else if("return".equalsIgnoreCase(subElement.getName()))
					ret = Location.parseLoc(subElement.attributeValue("loc"));
				else if("teleport".equalsIgnoreCase(subElement.getName()))
				{
					if(teleportLocs.isEmpty())
						teleportLocs = new ArrayList<>(1);
					teleportLocs.add(Location.parseLoc(subElement.attributeValue("loc")));
				}
				else if("remove".equalsIgnoreCase(subElement.getName()))
				{
					removedItemId = Integer.parseInt(subElement.attributeValue("itemId"));
					removedItemCount = Integer.parseInt(subElement.attributeValue("count"));
					removedItemNecessity = Boolean.parseBoolean(subElement.attributeValue("necessary"));
				}
				else if("geodata".equalsIgnoreCase(subElement.getName()))
				{
					String[] rxy = subElement.attributeValue("map").split("_");
					mapx = Integer.parseInt(rxy[0]);
					mapy = Integer.parseInt(rxy[1]);
				}
				else if("give".equalsIgnoreCase(subElement.getName()))
				{
					giveItemId = Integer.parseInt(subElement.attributeValue("itemId"));
					givedItemCount = Integer.parseInt(subElement.attributeValue("count"));
				}
				else if("quest".equalsIgnoreCase(subElement.getName()))
					requiredQuestId = Integer.parseInt(subElement.attributeValue("id"));
				else if("reuse".equalsIgnoreCase(subElement.getName()))
				{
					resetReuse = new SchedulingPattern(subElement.attributeValue("resetReuse"));
					sharedReuseGroup = Integer.parseInt(subElement.attributeValue("sharedReuseGroup"));
					setReuseUponEntry = Boolean.parseBoolean(subElement.attributeValue("setUponEntry"));
				}
				else if("doors".equalsIgnoreCase(subElement.getName()))
					for(Element e : subElement.elements())
					{
						if(doors.isEmpty())
							doors = new HashIntObjectMap();
						boolean opened = e.attributeValue("opened") != null && Boolean.parseBoolean(e.attributeValue("opened"));
						boolean invul = e.attributeValue("invul") == null || Boolean.parseBoolean(e.attributeValue("invul"));
						DoorTemplate template = DoorHolder.getInstance().getTemplate(Integer.parseInt(e.attributeValue("id")));
						doors.put(template.getId(), new InstantZone.DoorInfo(template, opened, invul));
					}
				else if("zones".equalsIgnoreCase(subElement.getName()))
					for(Element e : subElement.elements())
					{
						if(zones.isEmpty())
							zones = new HashMap<>();
						boolean active = e.attributeValue("active") != null && Boolean.parseBoolean(e.attributeValue("active"));
						ZoneTemplate template2 = ZoneHolder.getInstance().getTemplate(e.attributeValue("name"));
						if(template2 == null)
                            error("Zone: " + e.attributeValue("name") + " not found; file: " + getCurrentFileName());
						else
							zones.put(template2.getName(), new InstantZone.ZoneInfo(template2, active));
					}
				else if("add_parameters".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
						if("param".equalsIgnoreCase(e.getName()))
							params.set(e.attributeValue("name"), e.attributeValue("value"));
				}
				else if("rewards".equalsIgnoreCase(subElement.getName()))
				{
					for (Element e : subElement.elements()) {
						int itemId = Integer.parseInt(e.attributeValue("itemId"));
						int count = Integer.parseInt(e.attributeValue("count"));
						double chance = Double.parseDouble(e.attributeValue("chance"));
						ChancedItemData itemData = new ChancedItemData(itemId, count, chance);
						rewards.add(itemData);
					}
				}
				else
				{
					if(!"spawns".equalsIgnoreCase(subElement.getName()))
						continue;
					for(Element e : subElement.elements())
						if("group".equalsIgnoreCase(e.getName()))
						{
							String group = e.attributeValue("name");
							boolean spawned = e.attributeValue("spawned") != null && Boolean.parseBoolean(e.attributeValue("spawned"));
							List<SpawnTemplate> templates = SpawnHolder.getInstance().getSpawn(group);
							if(templates == null)
                                info("not find spawn group: " + group + " in file: " + getCurrentFileName());
							else
							{
								if(spawns2.isEmpty())
									spawns2 = new Hashtable<>();
								spawns2.put(group, new InstantZone.SpawnInfo2(templates, spawned));
							}
						}
						else
						{
							if(!"spawn".equalsIgnoreCase(e.getName()))
								continue;
							String[] mobs = e.attributeValue("mobId").split(" ");
							String respawnNode = e.attributeValue("respawn");
							int respawn = respawnNode != null ? Integer.parseInt(respawnNode) : 0;
							String respawnRndNode = e.attributeValue("respawnRnd");
							int respawnRnd = respawnRndNode != null ? Integer.parseInt(respawnRndNode) : 0;
							String countNode = e.attributeValue("count");
							int count = countNode != null ? Integer.parseInt(countNode) : 1;
                            spawnType = 0;
							String spawnTypeNode = e.attributeValue("type");
							if(spawnTypeNode == null || "point".equalsIgnoreCase(spawnTypeNode))
								spawnType = 0;
							else if("rnd".equalsIgnoreCase(spawnTypeNode))
								spawnType = 1;
							else if("loc".equalsIgnoreCase(spawnTypeNode))
								spawnType = 2;
							else
                                error("Spawn type  '" + spawnTypeNode + "' is unknown!");
                            List<Location> coords = new ArrayList<>();
                            for(Element e2 : e.elements())
								if("coords".equalsIgnoreCase(e2.getName()))
									coords.add(Location.parseLoc(e2.attributeValue("loc")));
							Territory territory = null;
							if(spawnType == 2)
							{
								PolygonBuilder poly = new PolygonBuilder();
								for(Location loc : coords)
									poly.add(loc.x, loc.y).setZmin(loc.z).setZmax(loc.z);
								if(!poly.validate())
                                    error("invalid spawn territory for instance id : " + instanceId + " - " + poly + "!");
								territory = new Territory().add(poly.createPolygon());
							}
							for(String mob : mobs)
							{
								int mobId = Integer.parseInt(mob);
								spawnDat = new InstantZone.SpawnInfo(spawnType, mobId, count, respawn, respawnRnd, coords, territory);
								spawns.add(spawnDat);
							}
						}
				}
			}
			InstantZone instancedZone = new InstantZone(instanceId, name, resetReuse, sharedReuseGroup, timelimit, dispelBuffs, minLevel, maxLevel, minParty, maxParty, timer, onPartyDismiss, teleportLocs, ret, doors, zones, spawns2, spawns, collapseIfEmpty, maxChannels, removedItemId, removedItemCount, removedItemNecessity, giveItemId, givedItemCount, requiredQuestId, setReuseUponEntry, mapx, mapy, params);
			instancedZone.addRewards(rewards);
            getHolder().addInstantZone(instancedZone);
		}
	}

	static
	{
		_instance = new InstantZoneParser();
	}
}
