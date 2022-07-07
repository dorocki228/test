package l2s.gameserver.utils;

import l2s.gameserver.Config;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.security.HwidUtils;

import java.util.function.Predicate;

public class ChatUtils
{
	private static void say(Player activeChar, GameObject activeObject, Iterable<Player> players, int range, SayPacket2 cs, boolean isSpam)
	{
		for(Player player : players)
		{
			if(player.isBlockAll())
				continue;
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;
			if(!activeObject.isInRangeZ(obj, range) || player.getBlockList().contains(activeChar) || !activeChar.canTalkWith(player))
				continue;

			if(!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player))
			{
				cs.setCharName(activeChar.getVisibleName(player));
				player.sendPacket(cs);
			}
		}
	}

	public static void say(Player activeChar, SayPacket2 cs, boolean isSpam)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if(activeObject == null)
			activeObject = activeChar;
		say(activeChar, activeObject, World.getAroundObservers(activeObject), Config.CHAT_RANGE, cs, isSpam);
	}

	public static void shout(Player activeChar, SayPacket2 cs, boolean isSpam)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if(activeObject == null)
			activeObject = activeChar;
		int rx = MapUtils.regionX(activeObject);
		int ry = MapUtils.regionY(activeObject);
		for(Player player : GameObjectsStorage.getPlayers())
			if(player != activeChar && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player)))
			{
				if(player.isBlockAll())
					continue;
				if(player.canSeeAllShouts() && !player.getBlockList().contains(activeChar) && activeChar.canTalkWith(player))
					player.sendPacket(cs);
				else
				{
					GameObject obj = player.getObservePoint();
					if(obj == null)
						obj = player;
					if(activeObject.getReflection() != obj.getReflection())
						continue;
					int tx = MapUtils.regionX(obj) - rx;
					int ty = MapUtils.regionY(obj) - ry;
					if(tx * tx + ty * ty > Config.SHOUT_SQUARE_OFFSET && !activeObject.isInRangeZ(obj, Config.CHAT_RANGE) || player.getBlockList().contains(activeChar) || !activeChar.canTalkWith(player))
						continue;
					player.sendPacket(cs);
				}
			}
	}

	public static void fractionalGlobalShout(Player activeChar, SayPacket2 cs, boolean isSpam) {
		Predicate<Player> predicate = player ->
				activeChar.getFraction() == player.getFraction()
						&& player != activeChar
						&& (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player))
						&& !player.isBlockAll();
		GameObjectsStorage.getPlayersStream(predicate)
				.forEach(player -> {
					if(!player.getBlockList().contains(activeChar)
							&& activeChar.canTalkWith(player))
						player.sendPacket(cs);
				});
	}

	public static void fractionalShout(Player activeChar, SayPacket2 cs, boolean isSpam)
	{
		GameObject activeObject = activeChar.getObservePoint();
		if(activeObject == null)
			activeObject = activeChar;
		int rx = MapUtils.regionX(activeObject);
		int ry = MapUtils.regionY(activeObject);
		Predicate<Player> predicate = player ->
				activeChar.getFraction() == player.getFraction()
						&& player != activeChar
						&& (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player))
						&& !player.isBlockAll();
		GameObject finalActiveObject = activeObject;
		GameObjectsStorage.getPlayersStream(predicate)
				.forEach(player -> {
					if(player.canSeeAllShouts() && !player.getBlockList().contains(activeChar)
							&& activeChar.canTalkWith(player))
						player.sendPacket(cs);
					else
					{
						GameObject obj = player.getObservePoint();
						if(obj == null)
							obj = player;
						if(finalActiveObject.getReflection() != obj.getReflection())
							return;
						int tx = MapUtils.regionX(obj) - rx;
						int ty = MapUtils.regionY(obj) - ry;
						if(tx * tx + ty * ty > Config.SHOUT_SQUARE_OFFSET
								&& !finalActiveObject.isInRangeZ(obj, Config.CHAT_RANGE)
								|| player.getBlockList().contains(activeChar) || !activeChar.canTalkWith(player))
							return;
						player.sendPacket(cs);
					}
				});
	}

	public static void announce(Player activeChar, SayPacket2 cs, boolean isSpam)
	{
		for(Player player : GameObjectsStorage.getPlayers())
			if(player != activeChar && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player)))
			{
				if(player.isBlockAll())
					continue;
				GameObject obj = player.getObservePoint();
				if(obj == null)
					obj = player;
				if(player.getBlockList().contains(activeChar) || !activeChar.canTalkWith(player))
					continue;
				player.sendPacket(cs);
			}
	}

    public static void fractionalAnnounce(Player activeChar, SayPacket2 cs, boolean isSpam)
    {
        Predicate<Player> predicate = player ->
                activeChar.getFraction() == player.getFraction()
                        && player != activeChar
                        && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player))
                        && !player.isBlockAll()
                        && !player.getBlockList().contains(activeChar) && activeChar.canTalkWith(player);
        GameObjectsStorage.getPlayersStream(predicate)
                .forEach(player -> player.sendPacket(cs));
    }
    public static void globalAnnounce(Player activeChar, SayPacket2 cs, boolean isSpam)
    {
        Predicate<Player> predicate = player -> player != activeChar
                        && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, player))
                        && !player.isBlockAll()
                        && !player.getBlockList().contains(activeChar) && activeChar.canTalkWith(player);
        GameObjectsStorage.getPlayers().stream().filter(predicate)
                .forEach(player -> player.sendPacket(cs));
    }

	public static void chat(NpcInstance activeChar, ChatType type, NpcString npcString, String... params)
	{
		switch(type)
		{
			case ALL:
			case NPC_ALL:
			{
				say(activeChar, npcString, params);
				break;
			}
			case SHOUT:
			case NPC_SHOUT:
			{
				shout(activeChar, npcString, params);
				break;
			}
		}
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, int range, NSPacket cs)
	{
		for(Player player : players)
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;
			if(activeChar.isInRangeZ(obj, range))
				player.sendPacket(cs);
		}
	}

	public static void say(NpcInstance activeChar, NSPacket cs)
	{
		say(activeChar, World.getAroundObservers(activeChar), Config.CHAT_RANGE, cs);
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, NSPacket cs)
	{
		say(activeChar, players, Config.CHAT_RANGE, cs);
	}

	public static void say(NpcInstance activeChar, int range, NSPacket cs)
	{
		say(activeChar, World.getAroundObservers(activeChar), range, cs);
	}

	public static void say(NpcInstance activeChar, int range, NpcString npcString, String... params)
	{
		say(activeChar, range, new NSPacket(activeChar, ChatType.NPC_ALL, npcString, params));
	}

	public static void say(NpcInstance npc, NpcString npcString, String... params)
	{
		say(npc, Config.CHAT_RANGE, npcString, params);
	}

	public static void shout(NpcInstance activeChar, NSPacket cs)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);
		for(Player player : GameObjectsStorage.getPlayers())
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;
			if(activeChar.getReflection() != obj.getReflection())
				continue;
			int tx = MapUtils.regionX(obj) - rx;
			int ty = MapUtils.regionY(obj) - ry;
			if(tx * tx + ty * ty > Config.SHOUT_SQUARE_OFFSET && !activeChar.isInRangeZ(obj, Config.CHAT_RANGE))
				continue;
			player.sendPacket(cs);
		}
	}

	public static void shout(NpcInstance activeChar, NpcString npcString, String... params)
	{
		shout(activeChar, new NSPacket(activeChar, ChatType.NPC_SHOUT, npcString, params));
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, int range, CustomMessage cm)
	{
		for(Player player : players)
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;
			if(activeChar.isInRangeZ(obj, range))
				player.sendPacket(new NSPacket(activeChar, ChatType.NPC_SHOUT, cm.toString(player)));
		}
	}

	public static void say(NpcInstance activeChar, CustomMessage cm)
	{
		say(activeChar, World.getAroundObservers(activeChar), Config.CHAT_RANGE, cm);
	}

	public static void say(NpcInstance activeChar, Iterable<Player> players, CustomMessage cm)
	{
		say(activeChar, players, Config.CHAT_RANGE, cm);
	}

	public static void say(NpcInstance activeChar, int range, CustomMessage cm)
	{
		say(activeChar, World.getAroundObservers(activeChar), range, cm);
	}

	public static void shout(NpcInstance activeChar, CustomMessage cm)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);
		for(Player player : GameObjectsStorage.getPlayers())
		{
			GameObject obj = player.getObservePoint();
			if(obj == null)
				obj = player;
			if(activeChar.getReflection() != obj.getReflection())
				continue;
			int tx = MapUtils.regionX(obj) - rx;
			int ty = MapUtils.regionY(obj) - ry;
			if(tx * tx + ty * ty > Config.SHOUT_SQUARE_OFFSET && !activeChar.isInRangeZ(obj, Config.CHAT_RANGE))
				continue;
			player.sendPacket(new NSPacket(activeChar, ChatType.NPC_SHOUT, cm.toString(player)));
		}
	}

	public static void say(Player activeChar, Iterable<Player> players, SayPacket2 cs)
	{
		Creature activeObject = activeChar.getObservePoint();
		if(activeObject == null)
			activeObject = activeChar;

		say(activeChar, activeObject, players, Config.CHAT_RANGE, cs);
	}

	private static void say(Player activeChar, GameObject activeObject, Iterable<Player> players, int range, SayPacket2 cs)
	{
		for(Player player : players)
		{
			if(player.isBlockAll())
				continue;

			Creature obj = player.getObservePoint();
			if(obj == null)
				obj = player;

			if(!activeObject.isInRangeZ(obj, range) || player.getBlockList().contains(activeChar) || !activeChar.canTalkWith(player))
				continue;

			cs.setCharName(activeChar.getVisibleName(player));

			player.sendPacket(cs);
		}
	}
}
