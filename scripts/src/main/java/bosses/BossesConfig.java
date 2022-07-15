package bosses;

import l2s.commons.configuration.ExProperties;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.listener.script.OnLoadScriptListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class BossesConfig implements OnLoadScriptListener
{
	private static final Logger _log = LoggerFactory.getLogger(BossesConfig.class);

	private static final String PROPERTIES_FILE = "config/bosses.properties";

	// Antharas
	public static SchedulingPattern ANTHARAS_RESPAWN_TIME_PATTERN;
	public static int ANTHARAS_SPAWN_DELAY;
	public static int ANTHARAS_SLEEP_TIME;
	public static int ANTHARAS_MIN_MEMBERS_COUNT;
	public static int ANTHARAS_MAX_MEMBERS_COUNT;
	public static int ANTHARAS_MEMBER_MIN_LEVEL;
	public static int[][] ANTHARAS_ENTERANCE_NECESSARY_ITEMS;
	public static boolean ANTHARAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS;

	// Baium
	public static SchedulingPattern BAIUM_RESPAWN_TIME_PATTERN;
	public static int BAIUM_SLEEP_TIME;
	public static int[][] BAIUM_ENTERANCE_NECESSARY_ITEMS;
	public static boolean BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS;

	@Override
	public void onLoad()
	{
		ExProperties properties = Config.load(PROPERTIES_FILE);

		// Antharas
		ANTHARAS_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("ANTHARAS_RESPAWN_TIME_PATTERN", "~480:* * +11:* * *"));
		ANTHARAS_SPAWN_DELAY = properties.getProperty("ANTHARAS_SPAWN_DELAY", 10);
		ANTHARAS_SLEEP_TIME = properties.getProperty("ANTHARAS_SLEEP_TIME", 15);
		ANTHARAS_MIN_MEMBERS_COUNT = properties.getProperty("ANTHARAS_MIN_MEMBERS_COUNT", 90);
		ANTHARAS_MAX_MEMBERS_COUNT = properties.getProperty("ANTHARAS_MAX_MEMBERS_COUNT", 300);
		ANTHARAS_MEMBER_MIN_LEVEL = properties.getProperty("ANTHARAS_MEMBER_MIN_LEVEL", 76);
		ANTHARAS_ENTERANCE_NECESSARY_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("ANTHARAS_ENTERANCE_NECESSARY_ITEMS", "3865-1"), ";", "-");
		ANTHARAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS = properties.getProperty("ANTHARAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS", true);

		// Baium
		BAIUM_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("BAIUM_RESPAWN_TIME_PATTERN", "00 16 * * 7"));
		BAIUM_SLEEP_TIME = properties.getProperty("BAIUM_SLEEP_TIME", 30);
		BAIUM_ENTERANCE_NECESSARY_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("BAIUM_ENTERANCE_NECESSARY_ITEMS", "4295-1"), ";", "-");
		BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS = properties.getProperty("BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS", true);
	}
}