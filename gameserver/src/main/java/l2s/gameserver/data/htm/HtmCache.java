package l2s.gameserver.data.htm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.ArabicConv;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Кэширование html диалогов.
 * <p>
 * В кеше список вот так
 * admin/admhelp.htm
 * admin/admin.htm
 * admin/admserver.htm
 * admin/banmenu.htm
 * admin/charmanage.htm
 *
 * @author G1ta0
 * @author VISTALL
 * @author Java-man
 */
public class HtmCache {
    private static final Logger LOGGER = LogManager.getLogger(HtmCache.class);

    private static final String NOT_EXIST_OR_EMPTY_HTML = StringUtils.EMPTY;

    private static final String UTF8_BOM = "\ufeff";

    private final Map<Language, LoadingCache<String, String>> cache;

    private HtmCache() {
        Map<Language, LoadingCache<String, String>> tempMap = new HashMap<>(Language.values().length);
        for (Language language : Language.VALUES) {
            Path root = Config.DATAPACK_ROOT_PATH.resolve("data/html").resolve(language.getShortName());
            if (Files.exists(root)) {
                int duration = GameServer.DEVELOP ? 10 : 60*60;
                LoadingCache<String, String> loadingCache = Caffeine.newBuilder()
                        .expireAfterAccess(duration, TimeUnit.SECONDS)
                        .build(key -> putContent(root, key));
                tempMap.put(language, loadingCache);
            }
        }

        cache = new HashMap<>(tempMap);

        LOGGER.info("HtmCache: lazy cache mode.");
    }

    public static HtmCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Получить html.
     *
     * @param fileName путь до html относительно data/html
     * @param player
     * @return пустую строку, если диалога не существует
     */
    public String getHtml(String fileName, Player player) {
        Language lang = player == null ? Language.ENGLISH : player.getLanguage();
        Optional<String> cache = getCache(fileName, lang);
        return cache.orElse(NOT_EXIST_OR_EMPTY_HTML);
    }

    public String getHtml(String fileName, Language language) {
        Optional<String> cache = getCache(fileName, language);
        return cache.orElse(NOT_EXIST_OR_EMPTY_HTML);
    }

    /**
     * Получить html.
     *
     * @param fileName путь до html относительно data/html
     * @param player
     * @return пустую строку, если диалога не существует
     * <p>
     * TODO remove
     */
    @Deprecated
    public String getIfExists(String fileName, Player player) {
        Language lang = player == null ? Language.ENGLISH : player.getLanguage();
        Optional<String> cache = getCache(fileName, lang);
        return cache.orElse(null);
    }

    public HashMap<Integer, String> getTemplates(String fileName, Player player) {
        return Util.parseTemplate(getHtml(fileName, player));
    }

    public void clear() {
        cache.values().forEach(Cache::invalidateAll);
    }

    private String putContent(Path rootPath, String file) {
        Path fullPath = rootPath.resolve(file);

        if (Files.notExists(fullPath)) {
            return null;
        }

        String content;
        try {
            content = Files.readString(fullPath);
        } catch (IOException e) {
            LOGGER.error("HtmCache: File error: {}", fullPath, e);
            return null;
        }

        if (content.isBlank()) {
            LOGGER.warn("dialog {} is blank.", fullPath);
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
            LOGGER.debug("Dialog: data/html/{}/{} not found.", lang.getShortName(), file);
            return Optional.empty();
        }

        return Optional.of(element);
    }

    private static class LazyHolder {
        private static final HtmCache INSTANCE = new HtmCache();
    }
}
