package l2s.gameserver.network.floodprotector.config;

import com.google.common.flogger.FluentLogger;
import l2s.commons.configuration.ExProperties;
import l2s.gameserver.Config;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bonux
 * @author Java-man
 */
public final class FloodProtectorConfigs
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	public static final String FLOOD_PROTECTOR_FILE = "config/flood_protector.properties";

	public static List<FloodProtectorConfig> FLOOD_PROTECTORS;

	public static void load()
	{
		final ExProperties floodProtectors = Config.load(FLOOD_PROTECTOR_FILE);
		Set<String> floodProtectorTypes = floodProtectors.keySet().stream()
				.map(key -> (String) key)
				.map(key -> key.split("_")[0])
				.collect(Collectors.toUnmodifiableSet());

		FLOOD_PROTECTORS = floodProtectorTypes.stream()
				.map(type -> {
					if(StringUtils.isEmpty(type))
						return null;

					FloodProtectorConfig floodProtector = FloodProtectorConfig.load(type.toUpperCase(), floodProtectors);
					LOGGER.atInfo().log("Flood protector '%s' loaded.", type);

					return floodProtector;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toUnmodifiableList());

		boolean haveDefault = FLOOD_PROTECTORS.stream().anyMatch(config -> config.FLOOD_PROTECTOR_TYPE.equals("DEFAULT"));
		if (!haveDefault) {
			throw new IllegalArgumentException("Default flood protection not found.");
		}
	}
}
