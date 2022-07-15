package l2s.gameserver.data.htm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.flogger.FluentLogger;
import l2s.commons.string.CharsetEncodingDetector;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.ArabicConv;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Util;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.flogger.LazyArgs.lazy;

/**
 * Кэширование html диалогов.
 *
 * В кеше список вот так
 * admin/admhelp.htm
 * admin/admin.htm
 * admin/admserver.htm
 * admin/banmenu.htm
 * admin/charmanage.htm
 *
 * @author G1ta0
 * @reworked VISTALL
 * @reworked (again) Java-man
 */
public class HtmCache
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	public static final int DISABLED = 0; // кеширование отключено (только для тестирования)
	public static final int LAZY = 1; // диалоги кешируются по мере обращения
	public static final int ENABLED = 2; // все диалоги кешируются при загрузке сервера

	

	private static final String NOT_EXIST_OR_EMPTY_HTML = StringUtils.EMPTY;

	private static final String UTF8_BOM = "\ufeff";
	public static final Charset HTML_DEFAULT_ENCODING = StandardCharsets.UTF_8;

	private final Map<Language, LoadingCache<String, String>> cache;

	private HtmCache()
	{
		Map<Language, LoadingCache<String, String>> tempMap = new HashMap<>(Language.values().length);
		for (Language language : Language.VALUES) {
			Path rootPath = Config.DATAPACK_ROOT_PATH.resolve("data/html").resolve(language.getShortName());
			Path customPath = Config.CUSTOM_PATH.resolve("html").resolve(language.getShortName());
			if (Files.exists(rootPath)) {
				int duration = GameServer.DEVELOP ? 1 : 600;
				LoadingCache<String, String> loadingCache = Caffeine.newBuilder()
						.expireAfterAccess(duration, TimeUnit.MINUTES)
						.build(filePath -> putContent(rootPath, customPath, filePath));
				tempMap.put(language, loadingCache);
			}
		}

		cache = new HashMap<>(tempMap);

		LOGGER.atInfo().log( "HtmCache: lazy cache mode." );
	}

	private String putContent(Path rootPath, Path customPath, String file) {
		Path fullPath = rootPath.resolve(file);

		if (Files.notExists(fullPath)) {
			fullPath = customPath.resolve(file);
			if (Files.notExists(fullPath)) {
				return null;
			}
		}

		String content;
		try {
			Charset encoding = CharsetEncodingDetector.detectEncoding(fullPath, HTML_DEFAULT_ENCODING);
			content = Files.readString(fullPath, encoding);
			if (!encoding.equals(HTML_DEFAULT_ENCODING)) {
				content = new String(content.getBytes(HTML_DEFAULT_ENCODING));
			}
		} catch (IOException e) {
			LOGGER.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "HtmCache: File error: %s", fullPath );
			return null;
		}

		if (content.isBlank()) {
			LOGGER.atWarning().log( "dialog %s is blank.", fullPath );
			return null;
		}

		if (content.startsWith(UTF8_BOM)) {
			content = content.substring(1);
		}

		if (Config.HTM_SHAPE_ARABIC) {
			content = ArabicConv.shapeArabic(content);
		}

		content = HtmlUtils.bbParse(content);
		content = HtmlUtils.compress(content);

		return content;
	}

	public void clear() {
		cache.values().forEach(Cache::invalidateAll);
	}

	/**
	 * Получить html.
	 *
	 * @param fileName путь до html относительно data/html/LANG
	 * @param player
	 * @return существующий диалог, либо null и сообщение об ошибке в лог, если диалога не существует
	 */
	public String getHtml(String fileName, Player player) {
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getHtml(fileName, lang);
	}

	public String getHtml(String fileName, Language language) {
		Optional<String> cache = getCache(fileName, language);
		return cache.orElse(NOT_EXIST_OR_EMPTY_HTML);
	}

	/**
	 * Получить существующий html.
	 *
	 * @param fileName путь до html относительно data/html/LANG
	 * @param player
	 * @return null если диалога не существует
	 */
	@Deprecated
	public String getIfExists(String fileName, Player player)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		Optional<String> cache = getCache(fileName, lang);
		return cache.orElse(null);
	}

	@Deprecated
	public String getIfExists(String fileName, Language language)
	{
		Optional<String> cache = getCache(fileName, language);
		return cache.orElse(null);
	}

	/**
	 * Получить шаблоны из html.
	 *
	 * @param fileName путь до html относительно data/html/LANG
	 * @param player
	 * @return HtmTemplates
	 */
	public HtmTemplates getTemplates(String fileName, Player player)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		HtmTemplates templates = Util.parseTemplates(fileName, lang, getHtml(fileName, player));
		if(templates == null)
			return HtmTemplates.EMPTY_TEMPLATES;
		return templates;
	}

	private Optional<String> getCache(String file, Language lang) {
		if (file == null) {
			return Optional.empty();
		}

		return get(lang, file);
	}

	private Optional<String> get(Language lang, String file) {
		String element = cache.get(lang).get(file);
		if (element == null) {
			element = cache.get(Language.ENGLISH).get(file);
		}

		if (element == null) {
			LOGGER.atFine().log( "Dialog: data/html/%s/%s not found.", lazy(() -> lang.getShortName()), file );
			return Optional.empty();
		}

		return Optional.of(element);
	}

	public static HtmCache getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final HtmCache INSTANCE = new HtmCache();
	}
}