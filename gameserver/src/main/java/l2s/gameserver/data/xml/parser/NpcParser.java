package l2s.gameserver.data.xml.parser;

import com.google.common.collect.HashMultimap;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.reward.RewardData;
import l2s.gameserver.model.reward.RewardGroup;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.TeleportLocation;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.data.ResourcesData;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.templates.npc.*;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class NpcParser extends AbstractParser<NpcHolder>
{
	private static final NpcParser _instance;

	public static NpcParser getInstance()
	{
		return _instance;
	}

	private NpcParser()
	{
		super(NpcHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/npc/");
	}

    @Override
	public String getDTDFileName()
	{
		return "npc.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> npcIterator = rootElement.elementIterator();
		while(npcIterator.hasNext())
		{
			Element npcElement = npcIterator.next();
			int npcId = Integer.parseInt(npcElement.attributeValue("id"));
			int templateId = npcElement.attributeValue("template_id") == null ? 0 : Integer.parseInt(npcElement.attributeValue("template_id"));
			String name = npcElement.attributeValue("name");
			String title = npcElement.attributeValue("title");
			StatsSet set = new StatsSet();
			set.set("npcId", npcId);
			set.set("displayId", templateId);
			set.set("name", name);
			set.set("title", title);
			set.set("baseCpReg", 0);
			set.set("baseCpMax", 0);
			Iterator<Element> firstIterator = npcElement.elementIterator();
			while(firstIterator.hasNext())
			{
				Element firstElement = firstIterator.next();
				if("set".equalsIgnoreCase(firstElement.getName()))
					set.set(firstElement.attributeValue("name"), firstElement.attributeValue("value"));
				else if("equip".equalsIgnoreCase(firstElement.getName()))
				{
					Iterator<Element> eIterator = firstElement.elementIterator();
					while(eIterator.hasNext())
					{
						Element eElement = eIterator.next();
						set.set(eElement.getName(), eElement.attributeValue("item_id"));
					}
				}
				else if("ai_params".equalsIgnoreCase(firstElement.getName()))
				{
					StatsSet ai = new StatsSet();
					Iterator<Element> eIterator2 = firstElement.elementIterator();
					while(eIterator2.hasNext())
					{
						Element eElement2 = eIterator2.next();
						ai.set(eElement2.attributeValue("name"), eElement2.attributeValue("value"));
					}
					set.set("aiParams", ai);
				}
				else
				{
					if(!"attributes".equalsIgnoreCase(firstElement.getName()))
						continue;
					int[] attributeAttack = new int[6];
					int[] attributeDefence = new int[6];
					Iterator<Element> eIterator3 = firstElement.elementIterator();
					while(eIterator3.hasNext())
					{
						Element eElement3 = eIterator3.next();
						if("defence".equalsIgnoreCase(eElement3.getName()))
						{
							l2s.gameserver.model.base.Element element = l2s.gameserver.model.base.Element.getElementByName(eElement3.attributeValue("attribute"));
							attributeDefence[element.getId()] = Integer.parseInt(eElement3.attributeValue("value"));
						}
						else
						{
							if(!"attack".equalsIgnoreCase(eElement3.getName()))
								continue;
							l2s.gameserver.model.base.Element element = l2s.gameserver.model.base.Element.getElementByName(eElement3.attributeValue("attribute"));
							attributeAttack[element.getId()] = Integer.parseInt(eElement3.attributeValue("value"));
						}
					}
					set.set("baseAttributeAttack", attributeAttack);
					set.set("baseAttributeDefence", attributeDefence);
				}
			}
			NpcTemplate template = new NpcTemplate(set);
			Iterator<Element> secondIterator = npcElement.elementIterator();
			while(secondIterator.hasNext())
			{
				Element secondElement = secondIterator.next();
				String nodeName = secondElement.getName();
				if("faction".equalsIgnoreCase(nodeName))
				{
					String factionNames = secondElement.attributeValue("names");
					int factionRange = Integer.parseInt(secondElement.attributeValue("range"));
					Faction faction = new Faction(factionNames, factionRange);
					Iterator<Element> nextIterator = secondElement.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement = nextIterator.next();
						int ignoreId = Integer.parseInt(nextElement.attributeValue("npc_id"));
						faction.addIgnoreNpcId(ignoreId);
					}
					template.setFaction(faction);
				}
				else if("rewardlist".equalsIgnoreCase(nodeName))
					template.addRewardList(parseRewardList(this, secondElement, String.valueOf(npcId)));
				else if("client_skills".equalsIgnoreCase(nodeName) || "skills".equalsIgnoreCase(nodeName))
				{
					Iterator<Element> nextIterator2 = secondElement.elementIterator();
					while(nextIterator2.hasNext())
					{
						Element nextElement2 = nextIterator2.next();
						int id = Integer.parseInt(nextElement2.attributeValue("id"));
						int level = Integer.parseInt(nextElement2.attributeValue("level"));
						if(id == 4416)
							template.setRace(level);
						Skill skill = SkillHolder.getInstance().getSkill(id, level);
						if(skill == null)
							continue;
						String use_type = nextElement2.attributeValue("use_type");
						if(use_type != null)
							template.setAIParam(use_type, id + "-" + level);
						template.addSkill(skill);
					}
				}
				else if("minions".equalsIgnoreCase(nodeName))
				{
					Iterator<Element> nextIterator2 = secondElement.elementIterator();
					while(nextIterator2.hasNext())
					{
						Element nextElement2 = nextIterator2.next();
						int id = Integer.parseInt(nextElement2.attributeValue("npc_id"));
						int count = Integer.parseInt(nextElement2.attributeValue("count"));
						template.addMinion(new MinionData(id, count));
					}
				}
				else if("teleportlist".equalsIgnoreCase(nodeName))
				{
					Iterator<Element> sublistIterator = secondElement.elementIterator();
					while(sublistIterator.hasNext())
					{
						Element subListElement = sublistIterator.next();
						int id = Integer.parseInt(subListElement.attributeValue("id"));
						boolean prime_hours = subListElement.attributeValue("prime_hours") == null || Boolean.parseBoolean(subListElement.attributeValue("prime_hours"));
						List<TeleportLocation> list = new ArrayList<>();
						Iterator<Element> targetIterator = subListElement.elementIterator();
						while(targetIterator.hasNext())
						{
							Element targetElement = targetIterator.next();
							int itemId = Integer.parseInt(targetElement.attributeValue("item_id", "57"));
							long price = Integer.parseInt(targetElement.attributeValue("price"));
							int npcStringId = Integer.parseInt(targetElement.attributeValue("name"));
							int[] castleIds = StringArrayUtils.stringToIntArray(targetElement.attributeValue("castle_id", "0"), ";");
							int questZoneId = Integer.parseInt(targetElement.attributeValue("quest_zone_id", "-1"));
							TeleportLocation loc = new TeleportLocation(itemId, price, npcStringId, castleIds, prime_hours, questZoneId);
							loc.set(Location.parseLoc(targetElement.attributeValue("loc")));
							list.add(loc);
						}
						template.addTeleportList(id, list);
					}
				}
				else if("walker_route".equalsIgnoreCase(nodeName))
				{
					int id2 = Integer.parseInt(secondElement.attributeValue("id"));
					WalkerRouteType type = secondElement.attributeValue("type") == null ? WalkerRouteType.LENGTH : WalkerRouteType.valueOf(secondElement.attributeValue("type").toUpperCase());
					WalkerRoute walkerRoute = new WalkerRoute(id2, type);
					Iterator<Element> nextIterator = secondElement.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement = nextIterator.next();
						Location loc2 = Location.parse(nextElement).correctGeoZ();
						int[] phrasesIds = StringArrayUtils.stringToIntArray(nextElement.attributeValue("phrase_id") == null ? "" : nextElement.attributeValue("phrase_id"), ";");
						NpcString[] phrases = Arrays.stream(phrasesIds)
								.mapToObj(NpcString::valueOf).toArray(NpcString[]::new);
						int socialActionId = nextElement.attributeValue("social_action_id") == null ? -1 : Integer.parseInt(nextElement.attributeValue("social_action_id"));
						int delay = nextElement.attributeValue("delay") == null ? 0 : Integer.parseInt(nextElement.attributeValue("delay"));
						boolean running = nextElement.attributeValue("running") != null && Boolean.parseBoolean(nextElement.attributeValue("running"));
						boolean teleport = nextElement.attributeValue("teleport") != null && Boolean.parseBoolean(nextElement.attributeValue("teleport"));
						walkerRoute.addPoint(new WalkerRoutePoint(loc2, phrases, socialActionId, delay, running, teleport));
					}
					template.addWalkerRoute(walkerRoute);
				}
				else if("random_actions".equalsIgnoreCase(nodeName))
				{
					boolean random_order = secondElement.attributeValue("random_order") != null && Boolean.parseBoolean(secondElement.attributeValue("random_order"));
					RandomActions randomActions = new RandomActions(random_order);
					Iterator<Element> nextIterator3 = secondElement.elementIterator();
					while(nextIterator3.hasNext())
					{
						Element nextElement3 = nextIterator3.next();
						int id3 = Integer.parseInt(nextElement3.attributeValue("id"));
						NpcString phrase = nextElement3.attributeValue("phrase_id") == null ? null : NpcString.valueOf(Integer.parseInt(nextElement3.attributeValue("phrase_id")));
						int socialActionId2 = nextElement3.attributeValue("social_action_id") == null ? -1 : Integer.parseInt(nextElement3.attributeValue("social_action_id"));
						int delay2 = nextElement3.attributeValue("delay") == null ? 0 : Integer.parseInt(nextElement3.attributeValue("delay"));
						randomActions.addAction(new RandomActions.Action(id3, phrase, socialActionId2, delay2));
					}
					template.setRandomActions(randomActions);
				}
				else if("resources".equalsIgnoreCase(nodeName))
				{
					var hitChanceToDie = Integer.valueOf(secondElement.attributeValue("hit_chance_to_die"));
					var maxHitsToDie = Integer.valueOf(secondElement.attributeValue("max_hits_to_die"));
					var resources = HashMultimap.<Integer, RewardItemData>create();

					var weaponElements = secondElement.elements();
					weaponElements.forEach(weaponElement -> {
						Arrays.stream(weaponElement.attributeValue("id").split(","))
								.map(Integer::valueOf)
								.forEach(weaponId -> {
									var resourcesList = weaponElement.elements().stream()
											.map(RewardItemData::new).collect(Collectors.toList());
									resources.putAll(weaponId, resourcesList);
								});
					});

					var resourcesItemData = new ResourcesData(hitChanceToDie, maxHitsToDie, resources);
					template.setResources(resourcesItemData);
				}
			}
			secondIterator = npcElement.elementIterator("database_rewardlist");
			while(secondIterator.hasNext())
			{
				Element secondElement = secondIterator.next();
				RewardList list2 = new RewardList(RewardType.RATED_GROUPED, false);
				if(!template.isInstanceOf(RaidBossInstance.class))
				{
					RewardGroup equipAndPiecesGroup = null;
					RewardGroup etcGroup = null;
					Iterator<Element> nextIterator3 = secondElement.elementIterator("reward");
					while(nextIterator3.hasNext())
					{
						Element nextElement3 = nextIterator3.next();
						RewardData data = parseReward(nextElement3);
						ItemTemplate itemTemplate = data.getItem();
						if(itemTemplate.isAdena())
						{
							RewardGroup adenaGroup = new RewardGroup(data.getChance());
							data.setChance(1000000.0);
							adenaGroup.addData(data);
							list2.add(adenaGroup);
						}
						else if(itemTemplate.isArmor() || itemTemplate.isWeapon() || itemTemplate.isAccessory() || itemTemplate.isKeyMatherial())
						{
							if(equipAndPiecesGroup == null)
								equipAndPiecesGroup = new RewardGroup(1000000.0);
							equipAndPiecesGroup.addData(data);
						}
						else
						{
							if(etcGroup == null)
								etcGroup = new RewardGroup(1000000.0);
							etcGroup.addData(data);
						}
					}
					if(equipAndPiecesGroup != null)
					{
						equipAndPiecesGroup.setChance(1000000.0);
						for(RewardData data2 : equipAndPiecesGroup.getItems())
							data2.setChance(data2.getChance());
						list2.add(equipAndPiecesGroup);
					}
					if(etcGroup != null)
					{
						etcGroup.setChance(1000000.0);
						for(RewardData data2 : etcGroup.getItems())
							data2.setChance(data2.getChance());
						list2.add(etcGroup);
					}
				}
				else
				{
					Iterator<Element> nextIterator2 = secondElement.elementIterator("reward");
					while(nextIterator2.hasNext())
					{
						Element nextElement2 = nextIterator2.next();
						RewardGroup group = new RewardGroup(1000000.0);
						group.addData(parseReward(nextElement2));
						list2.add(group);
					}
				}
				template.addRewardList(list2);
			}
			getHolder().addTemplate(template);
		}
	}

	public static RewardList parseRewardList(AbstractParser<?> parser, Element element, String debugString)
	{
		RewardType type = RewardType.valueOf(element.attributeValue("type"));
		boolean autoLoot = element.attributeValue("auto_loot") != null && Boolean.parseBoolean(element.attributeValue("auto_loot"));
		RewardList list = new RewardList(type, autoLoot);
		Iterator<Element> nextIterator = element.elementIterator();
		while(nextIterator.hasNext())
		{
			Element nextElement = nextIterator.next();
			String nextName = nextElement.getName();
			boolean notGroupType = type == RewardType.SWEEP
					|| type == RewardType.NOT_RATED_NOT_GROUPED || type == RewardType.RATED_NOT_GROUPED;
			if("group".equalsIgnoreCase(nextName))
			{
				double enterChance = nextElement.attributeValue("chance") == null ? 1000000.0 : Double.parseDouble(nextElement.attributeValue("chance")) * 10000.0;
				RewardGroup group = notGroupType ? null : new RewardGroup(enterChance);
				Iterator<Element> rewardIterator = nextElement.elementIterator();
				while(rewardIterator.hasNext())
				{
					Element rewardElement = rewardIterator.next();
					RewardData data = parseReward(rewardElement);
					if(notGroupType)
						parser.warn("Can't load rewardlist from group: " + debugString + "; type: " + type);
					else
						group.addData(data);
				}
				if(group == null)
					continue;
				list.add(group);
			}
			else
			{
				if(!"reward".equalsIgnoreCase(nextName))
					continue;
				if(!notGroupType)
					parser.warn("Reward can't be without group(and not grouped): " + debugString + "; type: " + type);
				else
				{
					RewardData data2 = parseReward(nextElement);
					RewardGroup g = new RewardGroup(1000000.0);
					g.addData(data2);
					list.add(g);
				}
			}
		}
		return list;
	}

	private static RewardData parseReward(Element rewardElement)
	{
		int itemId = Integer.parseInt(rewardElement.attributeValue("item_id"));
		int min = Integer.parseInt(rewardElement.attributeValue("min"));
		int max = Integer.parseInt(rewardElement.attributeValue("max"));
		int chance = (int) (Double.parseDouble(rewardElement.attributeValue("chance")) * 10000.0);
		RewardData data = new RewardData(itemId);
		data.setChance(chance);
		data.setMinDrop(min);
		data.setMaxDrop(max);
		data.setNotRate(data.getItem().isArrow() || Config.NO_RATE_EQUIPMENT && data.getItem().isEquipment() || Config.NO_RATE_KEY_MATERIAL && data.getItem().isKeyMatherial() || Config.NO_RATE_RECIPES && data.getItem().isRecipe() || ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId));
		return data;
	}

	static
	{
		_instance = new NpcParser();
	}
}
