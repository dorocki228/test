package l2s.gameserver.data.xml.parser;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Shape;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ShapeHolder;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.model.Territory;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.spawn.PeriodOfDay;
import l2s.gameserver.templates.spawn.SpawnNpcInfo;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class SpawnParser extends AbstractParser<SpawnHolder>
{
	private static final SpawnParser _instance = new SpawnParser();

	public static SpawnParser getInstance()
	{
		return _instance;
	}

	protected SpawnParser()
	{
		super(SpawnHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/spawn/");
	}

	@Override
	public String getDTDFileName()
	{
		return "spawn.dtd";
	}

	@Override
	public void readData(Element rootElement) throws Exception
	{
		Iterator<Element> spawnIterator = rootElement.elementIterator("spawn");
		while(spawnIterator.hasNext())
		{
			Element spawnElement = spawnIterator.next();

			Territory territory = parseTerritory(spawnElement);

			String group = spawnElement.attributeValue("group");
			String name = spawnElement.attributeValue("name") == null ? group == null ? "" : group : spawnElement.attributeValue("name");
			PeriodOfDay pod = spawnElement.attributeValue("period_of_day") == null ? PeriodOfDay.NONE : PeriodOfDay.valueOf(spawnElement.attributeValue("period_of_day").toUpperCase());
			if(group == null)
				group = pod.name();

			Iterator<Element> npcIterator = spawnElement.elementIterator("npc");
			while(npcIterator.hasNext())
			{
				Element npcElement = npcIterator.next();

				int npcId = Integer.parseInt(npcElement.attributeValue("id"));
				int respawn = npcElement.attributeValue("respawn") == null ? 60 : Integer.parseInt(npcElement.attributeValue("respawn"));
				int respawnRandom = npcElement.attributeValue("respawn_random") == null ? 0 : Integer.parseInt(npcElement.attributeValue("respawn_random"));
				int count = npcElement.attributeValue("count") == null ? 1 : Integer.parseInt(npcElement.attributeValue("count"));
				Location loc = npcElement.attributeValue("pos") != null ? Location.parseLoc(npcElement.attributeValue("pos")) : null;
				MultiValueSet<String> parameters = StatsSet.EMPTY;
				for(Element e : npcElement.elements())
				{
					if(parameters.isEmpty())
						parameters = new MultiValueSet<>();

					parameters.set(e.attributeValue("name"), e.attributeValue("value"));
				}

				SpawnTemplate template = new SpawnTemplate(name, group, pod, count, respawn, respawnRandom);
				SpawnNpcInfo sni = new SpawnNpcInfo(npcId, count, parameters);
				template.addNpc(sni);
				template.addSpawnRange(loc != null ? loc : territory);

				if(template.getNpcList().isEmpty())
                    warn("Npc id is zero! File: " + getCurrentFileName());
				else if(template.getSpawnRangeList().isEmpty())
                    warn("No points to spawn! File: " + getCurrentFileName());
				else
					getHolder().addSpawn(group, template);
			}

		}
	}

	private Territory parseTerritory(Element spawnElement)
	{
		Territory territory = null;
		if(spawnElement.attribute("territory") != null)
		{
			territory = new Territory();

			for(String name : spawnElement.attributeValue("territory").split(";"))
			{
				Shape shape = ShapeHolder.getInstance().getShape(name);
				if(shape != null)
					territory.add(shape);
			}
		}

		if(spawnElement.attribute("banned_territory") != null)
		{
			if(territory != null)
			{
				for(String name : spawnElement.attributeValue("banned_territory").split(";"))
				{
					Shape shape = ShapeHolder.getInstance().getShape(name);
					if(shape != null)
						territory.addBanned(shape);
				}
			}
		}

		return territory;
	}
}
