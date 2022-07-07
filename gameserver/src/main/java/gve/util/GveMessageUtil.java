package gve.util;

import gve.zones.GveZoneManager;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.service.ArtifactService;
import l2s.gameserver.utils.Language;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GveMessageUtil
{
	private static final L2GameServerPacket PROTECT_MESSAGE_HEADER = AAScreenStringPacketPresets.PROTECT_MESSAGE_HEADER.addOrUpdate("Need Defense:");
	private static final L2GameServerPacket PROTECT_MESSAGE_HEADER_REMOVE = AAScreenStringPacketPresets.PROTECT_MESSAGE_HEADER.remove();

	public static void updateProtectMessage(Player p)
	{
		StringBuilder water_sb = new StringBuilder();
		StringBuilder fire_sb = new StringBuilder();

		getInfo(p.getLanguage(), water_sb, fire_sb);

		if(p.getFraction() == Fraction.FIRE && fire_sb.length() > 0)
		{
			p.sendPacket(PROTECT_MESSAGE_HEADER);
			L2GameServerPacket packet = AAScreenStringPacketPresets.PROTECT_MESSAGE.addOrUpdate(fire_sb.toString());
			p.sendPacket(packet);
		}
		else if(p.getFraction() == Fraction.WATER && water_sb.length() > 0)
		{
			p.sendPacket(PROTECT_MESSAGE_HEADER);
			L2GameServerPacket packet = AAScreenStringPacketPresets.PROTECT_MESSAGE.addOrUpdate(water_sb.toString());
			p.sendPacket(packet);
		}

	}

	//TODO добавить мультиленг
	public static void updateProtectMessage(Fraction f)
	{
		StringBuilder water_sb = new StringBuilder();
		StringBuilder fire_sb = new StringBuilder();

		getInfo(Language.ENGLISH, water_sb, fire_sb);

		L2GameServerPacket fireMsgHeader;
		L2GameServerPacket fireMsg;

		if(fire_sb.length() > 0)
		{
			fireMsgHeader = PROTECT_MESSAGE_HEADER;
			fireMsg = AAScreenStringPacketPresets.PROTECT_MESSAGE.addOrUpdate(fire_sb.toString());
		}
		else
		{
			fireMsgHeader = PROTECT_MESSAGE_HEADER_REMOVE;
			fireMsg = AAScreenStringPacketPresets.PROTECT_MESSAGE.remove();
		}

		L2GameServerPacket waterMsg;
		L2GameServerPacket waterMsgHeader;
		if(water_sb.length() > 0)
		{
			waterMsgHeader = PROTECT_MESSAGE_HEADER;
			waterMsg = AAScreenStringPacketPresets.PROTECT_MESSAGE.addOrUpdate(water_sb.toString());
		}
		else
		{
			waterMsgHeader = PROTECT_MESSAGE_HEADER_REMOVE;
			waterMsg = AAScreenStringPacketPresets.PROTECT_MESSAGE.remove();
		}

		for(Player player : GameObjectsStorage.getPlayers())
		{
			if(player.getFraction() == Fraction.FIRE && (f == Fraction.NONE || f == Fraction.FIRE))
			{
				player.sendPacket(fireMsgHeader);
				player.sendPacket(fireMsg);
			}
			else if(player.getFraction() == Fraction.WATER && (f == Fraction.NONE || f == Fraction.WATER))
			{
				player.sendPacket(waterMsgHeader);
				player.sendPacket(waterMsg);
			}
		}

	}

	private static void getInfo(Language lang, StringBuilder water_sb, StringBuilder fire_sb)
	{
		for(Fortress r : ResidenceHolder.getInstance().getResidenceList(Fortress.class))
		{
			if(r.getSiegeEvent().isInProgress())
			{
				if(r.getFraction() == Fraction.FIRE)
					fire_sb.append(r.getName()).append("\n");
				else if(r.getFraction() == Fraction.WATER)
					water_sb.append(r.getName()).append("\n");
			}
		}

		String str = GveZoneManager.getInstance().getAttackedOutposts(Fraction.FIRE).stream().map(g -> g.getName(lang)).collect(Collectors.joining("\n"));
		if(!str.isBlank())
		{
			fire_sb.append(str);
			fire_sb.append("\n");
		}

		str = GveZoneManager.getInstance().getAttackedOutposts(Fraction.WATER).stream().map(g -> g.getName(lang)).collect(Collectors.joining("\n"));
		if(!str.isBlank())
		{
			water_sb.append(str);
			water_sb.append("\n");
		}

		final Map<Fraction, Map<Language, String>> broadcastDefense = ArtifactService.getInstance().getBroadcastDefense();
		if(!broadcastDefense.isEmpty())
		{
			Arrays.stream(Fraction.VALUES).forEach(p -> Optional.ofNullable(broadcastDefense.get(p)).map(m -> m.get(Language.ENGLISH)).ifPresent(s -> {
				if(p == Fraction.FIRE)
				{
					fire_sb.append(s);
					fire_sb.append("\n");
				}
				else if(p == Fraction.WATER)
				{
					water_sb.append(s);
					water_sb.append("\n");
				}
			}));
		}
	}
}
