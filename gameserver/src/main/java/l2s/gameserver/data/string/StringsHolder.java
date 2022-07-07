package l2s.gameserver.data.string;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public final class StringsHolder extends AbstractHolder {
    private static final Logger LOGGER = LogManager.getLogger(StringsHolder.class);

    private static final StringsHolder INSTANCE = new StringsHolder();

    private final Map<Language, ResourceBundle> resourceBundles;

    public static StringsHolder getInstance() {
        return INSTANCE;
    }

    private StringsHolder() {
        resourceBundles = new HashMap<>(2);
    }

    public String getString(String address, Player player) {
        Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
        return getString(address, lang);
    }

    public String getString(Player player, String address) {
        Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
        return getString(address, lang);
    }

    public String getString(String address, Language lang) {
        ResourceBundle resourceBundle = resourceBundles.get(lang);
        if (resourceBundle == null) {
            resourceBundle = resourceBundles.get(Language.ENGLISH);

            LOGGER.error("Can't find localization files for lang {}. {}", () -> lang, Exception::new);
        }

        return resourceBundle.getString(address);
    }

    public void load() {
        Path file = Path.of(Config.DATAPACK_ROOT.toURI()).resolve("data/string/strings");

        try {
            URL[] urls = {file.toUri().toURL()};
            ClassLoader loader = new URLClassLoader(urls);

            try (Stream<Path> pathStream = Files.list(file)) {
                pathStream.forEach(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.indexOf(".") > 0)
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));

                    String[] split = fileName.split("_");
                    if (split.length == 1) {
                        Language language = Language.getLanguage(Locale.ENGLISH);
                        resourceBundles.put(language, ResourceBundle.getBundle("localization", Locale.ENGLISH, loader));
                    } else {
                        Locale locale = Locale.forLanguageTag(split[1]);
                        Language language = Language.getLanguage(locale);
                        resourceBundles.put(language, ResourceBundle.getBundle("localization", locale, loader));
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error("Can't load localization files.", e);
        }

        if (!resourceBundles.containsKey(Language.ENGLISH))
            throw new IllegalArgumentException("Can't find localization files.");

        log();
    }

    public void reload() {
        clear();
        load();
    }

    @Override
    public void log() {
        for (Map.Entry<Language, ResourceBundle> entry : resourceBundles.entrySet()) {
            if (!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
                continue;
            info("load strings: " + entry.getValue().keySet().size() + " for lang: " + entry.getKey());
        }
    }

    @Override
    public int size() {
        return resourceBundles.size();
    }

    @Override
    public void clear() {
        resourceBundles.clear();
    }
}
