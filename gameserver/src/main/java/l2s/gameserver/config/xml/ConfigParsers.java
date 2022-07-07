package l2s.gameserver.config.xml;

import l2s.gameserver.config.xml.parser.GveRewardParser;
import l2s.gameserver.config.xml.parser.HostsConfigParser;
import l2s.gameserver.config.xml.parser.SpamFilterConfigParser;

public abstract class ConfigParsers
{
	public static void parseAll()
	{
		HostsConfigParser.getInstance().load();
		SpamFilterConfigParser.getInstance().load();
	}

	public static void reload()
	{
		HostsConfigParser.getInstance().reload();
		SpamFilterConfigParser.getInstance().reload();
		GveRewardParser.getInstance().reload();
	}
}
