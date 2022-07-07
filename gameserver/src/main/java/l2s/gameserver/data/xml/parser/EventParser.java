package l2s.gameserver.data.xml.parser;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.actions.*;
import l2s.gameserver.model.entity.events.objects.*;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SysString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;
import org.dom4j.Element;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public final class EventParser extends AbstractParser<EventHolder>
{
	private static final EventParser _instance;

	public static EventParser getInstance()
	{
		return _instance;
	}

	protected EventParser()
	{
		super(EventHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/events/");
	}

	@Override
	public String getDTDFileName()
	{
		return "events.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("event");
		while(iterator.hasNext())
		{
			Element eventElement = iterator.next();
			int id = Integer.parseInt(eventElement.attributeValue("id"));
			String name = eventElement.attributeValue("name");
			String impl = eventElement.attributeValue("impl");
			Class<Event> eventClass = null;
			try
			{
				eventClass = (Class<Event>) Class.forName("l2s.gameserver.model.entity.events.impl." + impl + "Event");
			}
			catch(ClassNotFoundException e)
			{
				eventClass = (Class<Event>) Scripts.getInstance().getClasses().get("events." + impl + "Event");
			}
			if(eventClass == null)
                info("Not found impl class: " + impl + "; File: " + getCurrentFileName());
			else
			{
				Constructor<Event> constructor = eventClass.getConstructor(MultiValueSet.class);
				MultiValueSet<String> set = new MultiValueSet<>();
				set.set("id", id);
				set.set("name", name);
				Iterator<Element> parameterIterator = eventElement.elementIterator("parameter");
				while(parameterIterator.hasNext())
				{
					Element parameterElement = parameterIterator.next();
					set.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
				}
				Event event = constructor.newInstance(set);
				event.beforeInitialization();
				event.addOnStartActions(parseActions(eventElement.element("on_start"), Integer.MAX_VALUE));
				event.addOnStopActions(parseActions(eventElement.element("on_stop"), Integer.MAX_VALUE));
				event.addOnInitActions(parseActions(eventElement.element("on_init"), Integer.MAX_VALUE));
				Element onTime = eventElement.element("on_time");
				if(onTime != null)
				{
					Iterator<Element> onTimeIterator = onTime.elementIterator("on");
					while(onTimeIterator.hasNext())
					{
						Element on = onTimeIterator.next();
						int time = 0;
						if(Util.isNumber(on.attributeValue("time")))
							time = Integer.parseInt(on.attributeValue("time"));
						else
							time = set.getInteger(on.attributeValue("time"));
						List<EventAction> actions = parseActions(on, time);
						event.addOnTimeActions(time, actions);
					}
				}
				Iterator<Element> objectIterator = eventElement.elementIterator("objects");
				while(objectIterator.hasNext())
				{
					Element objectElement = objectIterator.next();
					String objectsName = objectElement.attributeValue("name");
					List<Object> objects = parseObjects(objectElement, id + ":" + name);
					event.addObjects(objectsName, objects);
				}
				getHolder().addEvent(event);
				event.afterInitialization();
			}
		}
	}

	private List<Object> parseObjects(Element element, String str)
	{
		if(element == null)
			return Collections.emptyList();
		List<Object> objects = new ArrayList<>(2);
		Iterator<Element> objectIterator = element.elementIterator();
		while(objectIterator.hasNext())
		{
			Element objectElement = objectIterator.next();
			String nodeName = objectElement.getName();
			if("boat_point".equalsIgnoreCase(nodeName))
				objects.add(BoatPoint.parse(objectElement));
			else if("point".equalsIgnoreCase(nodeName))
				objects.add(Location.parse(objectElement));
			else if("spawn".equalsIgnoreCase(nodeName))
				objects.add(new SpawnObject(objectElement.attributeValue("name")));
			else if("spawn_ex".equalsIgnoreCase(nodeName))
				objects.add(new SpawnExObject(objectElement.attributeValue("name")));
			else if("spawn_ex_fort".equalsIgnoreCase(nodeName))
				objects.add(new SpawnExFortObject(objectElement.attributeValue("name")));
			else if("door".equalsIgnoreCase(nodeName))
				objects.add(new DoorObject(Integer.parseInt(objectElement.attributeValue("id"))));
			else if("static_object".equalsIgnoreCase(nodeName))
				objects.add(new StaticObjectObject(Integer.parseInt(objectElement.attributeValue("id"))));
			else if("spawn_npc".equalsIgnoreCase(nodeName))
			{
				int id = Integer.parseInt(objectElement.attributeValue("id"));
				int x = Integer.parseInt(objectElement.attributeValue("x"));
				int y = Integer.parseInt(objectElement.attributeValue("y"));
				int z = Integer.parseInt(objectElement.attributeValue("z"));
				objects.add(new SpawnSimpleObject(id, new Location(x, y, z)));
			}
			else if("siege_toggle_npc".equalsIgnoreCase(nodeName))
			{
				int id = Integer.parseInt(objectElement.attributeValue("id"));
				int fakeId = Integer.parseInt(objectElement.attributeValue("fake_id"));
				int x2 = Integer.parseInt(objectElement.attributeValue("x"));
				int y2 = Integer.parseInt(objectElement.attributeValue("y"));
				int z2 = Integer.parseInt(objectElement.attributeValue("z"));
				int hp = Integer.parseInt(objectElement.attributeValue("hp"));
				Set<String> set = Collections.emptySet();
				Iterator<Element> oIterator = objectElement.elementIterator();
				while(oIterator.hasNext())
				{
					Element sub = oIterator.next();
					if(set.isEmpty())
						set = new HashSet<>();
					set.add(sub.attributeValue("name"));
				}
				objects.add(new SiegeToggleNpcObject(id, fakeId, new Location(x2, y2, z2), hp, set));
			}
			else if("reward".equalsIgnoreCase(nodeName))
			{
				int item_id = Integer.parseInt(objectElement.attributeValue("item_id"));
				long min = Long.parseLong(objectElement.attributeValue("min"));
				long max = objectElement.attributeValue("max") == null ? min : Long.parseLong(objectElement.attributeValue("max"));
				double chance = objectElement.attributeValue("chance") == null ? 100.0 : Double.parseDouble(objectElement.attributeValue("chance"));
				objects.add(new RewardObject(item_id, min, max, chance));
			}
			else if("item".equalsIgnoreCase(nodeName))
			{
				int item_id = Integer.parseInt(objectElement.attributeValue("id"));
				long item_count = objectElement.attributeValue("count") == null ? -1L : Long.parseLong(objectElement.attributeValue("count"));
				objects.add(new ItemObject(item_id, item_count));
			}
			else if("castle_zone".equalsIgnoreCase(nodeName))
			{
				long price = Long.parseLong(objectElement.attributeValue("price"));
				objects.add(new CastleDamageZoneObject(objectElement.attributeValue("name"), price));
			}
			else if("zone".equalsIgnoreCase(nodeName))
				objects.add(new ZoneObject(objectElement.attributeValue("name")));
			else if("ctb_team".equalsIgnoreCase(nodeName))
			{
				int mobId = Integer.parseInt(objectElement.attributeValue("mob_id"));
				int flagId = Integer.parseInt(objectElement.attributeValue("id"));
				Location loc = Location.parse(objectElement);
				objects.add(new CTBTeamObject(mobId, flagId, loc));
			}
			else if("rewardlist".equalsIgnoreCase(nodeName))
				objects.add(NpcParser.parseRewardList(this, objectElement, str));
			else
			{
				if(!"abnormal".equalsIgnoreCase(nodeName))
					continue;
				objects.add(AbnormalEffect.valueOf(objectElement.attributeValue("name")));
			}
		}
		return objects;
	}

	private List<EventAction> parseActions(Element element, int time)
	{
		if(element == null)
			return Collections.emptyList();
		IfElseAction lastIf = null;
		List<EventAction> actions = new ArrayList<>(0);
		Iterator<Element> iterator = element.elementIterator();
		while(iterator.hasNext())
		{
			Element actionElement = iterator.next();
			if("start".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				StartStopAction startStopAction = new StartStopAction(name, true);
				actions.add(startStopAction);
			}
			else if("stop".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				StartStopAction startStopAction = new StartStopAction(name, false);
				actions.add(startStopAction);
			}
			else if("spawn".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				int delay = actionElement.attributeValue("delay") == null ? 0 : Integer.parseInt(actionElement.attributeValue("delay"));
				SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, delay, true);
				actions.add(spawnDespawnAction);
			}
			else if("despawn".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				int delay = actionElement.attributeValue("delay") == null ? 0 : Integer.parseInt(actionElement.attributeValue("delay"));
				SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, delay, false);
				actions.add(spawnDespawnAction);
			}
			else if("respawn".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				RespawnAction respawnAction = new RespawnAction(name);
				actions.add(respawnAction);
			}
			else if("open".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				OpenCloseAction a = new OpenCloseAction(true, name);
				actions.add(a);
			}
			else if("close".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				OpenCloseAction a = new OpenCloseAction(false, name);
				actions.add(a);
			}
			else if("active".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				ActiveDeactiveAction a2 = new ActiveDeactiveAction(true, name);
				actions.add(a2);
			}
			else if("deactive".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				ActiveDeactiveAction a2 = new ActiveDeactiveAction(false, name);
				actions.add(a2);
			}
			else if("refresh".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				RefreshAction a3 = new RefreshAction(name);
				actions.add(a3);
			}
			else if("init".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				InitAction a4 = new InitAction(name);
				actions.add(a4);
			}
			else if("global_add_reward".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				actions.add(new GlobalRewardListAction(true, name));
			}
			else if("global_remove_reward".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				actions.add(new GlobalRewardListAction(false, name));
			}
			else if("global_remove_items".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				actions.add(new GlobalRemoveItemsAction(name));
			}
			else if("npc_say".equalsIgnoreCase(actionElement.getName()))
			{
				int npc = Integer.parseInt(actionElement.attributeValue("npc"));
				ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
				int range = Integer.parseInt(actionElement.attributeValue("range"));
				NpcString string = NpcString.valueOf(actionElement.attributeValue("text"));
				NpcSayAction action = new NpcSayAction(npc, range, chat, string);
				actions.add(action);
			}
			else if("play_sound".equalsIgnoreCase(actionElement.getName()))
			{
				int range2 = Integer.parseInt(actionElement.attributeValue("range"));
				String sound = actionElement.attributeValue("sound");
				PlaySoundPacket.Type type = PlaySoundPacket.Type.valueOf(actionElement.attributeValue("type"));
				PlaySoundAction action2 = new PlaySoundAction(range2, sound, type);
				actions.add(action2);
			}
			else if("give_item".equalsIgnoreCase(actionElement.getName()))
			{
				int itemId = Integer.parseInt(actionElement.attributeValue("id"));
				long count = Integer.parseInt(actionElement.attributeValue("count"));
				GiveItemAction action3 = new GiveItemAction(itemId, count);
				actions.add(action3);
			}
			else if("give_owner_crp".equalsIgnoreCase(actionElement.getName()))
			{
				int count = Integer.parseInt(actionElement.attributeValue("count"));
				GiveOwnerCrpAction action3 = new GiveOwnerCrpAction(count);
				actions.add(action3);
			}
			else if("announce".equalsIgnoreCase(actionElement.getName()))
			{
				SystemMsg msgId = actionElement.attributeValue("msg_id") == null ? null : SystemMsg.valueOf(Integer.parseInt(actionElement.attributeValue("msg_id")));
				String val = actionElement.attributeValue("val");
				if(val == null && time == Integer.MAX_VALUE)
                    info("Can't get announce time." + getCurrentFileName());
				else
				{
					int val2 = val == null ? time : Integer.parseInt(val);
					EventAction action4 = new AnnounceAction(val2, msgId);
					actions.add(action4);
				}
			}
			else if("if".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				IfElseAction action5 = new IfElseAction(name, false);
				action5.setIfList(parseActions(actionElement, time));
				actions.add(action5);
				lastIf = action5;
			}
			else if("ifnot".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("name");
				IfElseAction action5 = new IfElseAction(name, true);
				action5.setIfList(parseActions(actionElement, time));
				actions.add(action5);
				lastIf = action5;
			}
			else if("else".equalsIgnoreCase(actionElement.getName()))
			{
				if(lastIf == null)
                    info("Not find <if> for <else> tag");
				else
					lastIf.setElseList(parseActions(actionElement, time));
			}
			else if("say".equalsIgnoreCase(actionElement.getName()))
			{
				ChatType chat2 = ChatType.valueOf(actionElement.attributeValue("chat"));
				int range3 = Integer.parseInt(actionElement.attributeValue("range"));
				String how = actionElement.attributeValue("how");
				String text = actionElement.attributeValue("text");
				SysString sysString = SysString.valueOf2(how);
				SayAction sayAction = null;
				if(sysString != null)
					sayAction = new SayAction(range3, chat2, sysString, SystemMsg.valueOf(text));
				else
					sayAction = new SayAction(range3, chat2, how, NpcString.valueOf(text));
				actions.add(sayAction);
			}
			else if("teleport_players".equalsIgnoreCase(actionElement.getName()))
			{
				String name = actionElement.attributeValue("id");
				TeleportPlayersAction a5 = new TeleportPlayersAction(name);
				actions.add(a5);
			}
		}
		return actions.isEmpty() ? Collections.emptyList() : actions;
	}

	static
	{
		_instance = new EventParser();
	}
}
