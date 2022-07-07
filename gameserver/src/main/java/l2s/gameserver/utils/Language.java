package l2s.gameserver.utils;

import l2s.gameserver.Config;

import java.util.Locale;

//4ipolibo нахера??? @Deprecated
public enum Language {
    KOREAN(0, "ko", "k", Locale.KOREAN),
    ENGLISH(1, "en", "e", Locale.ENGLISH),
    CHINESE(4, "zh", "cn", Locale.CHINESE),
    THAI(5, "th", "th", new Locale("th")),
    RUSSIAN(8, "ru", "ru", new Locale("ru")),
    ENGLISH_EU(9, "eu", "eu", new Locale("eu")),
    PORTUGUESE(-1, "pt", "e", new Locale("pt")),
    SPANISH(-2, "es", "e", new Locale("es")),
    ARABIC(-3, "ar", "e", new Locale("ar")),
    GREEK(-4, "el", "e", new Locale("el")),
    GEORGIAN(-5, "ka", "e", new Locale("ka")),
    HUNGARIAN(-6, "hu", "e", new Locale("hu")),
    FINNISH(-7, "fi", "e", new Locale("fi")),
    UKRAINIAN(-8, "uk", "e", new Locale("uk")),
    VIETNAMESE(-9, "vi", "e", new Locale("vi"));

    public static final Language[] VALUES = values();

    public static final String LANG_VAR = "lang@";

    private final int _id;
    private final String _shortName;
    private final String _datName;
    private final Locale locale;

    Language(int id, String shortName, String datName, Locale locale) {
        _id = id;
        _shortName = shortName;
        _datName = datName;
        this.locale = locale;
    }

    public int getId() {
        return _id;
    }

    public String getShortName() {
        return _shortName;
    }

    public String getDatName() {
        return _datName;
    }

    public Locale getLocale() {
        return locale;
    }

    public static Language getLanguage(int langId) {
        for (Language lang : VALUES)
            if (lang.getId() == langId)
                return lang;
        return Config.DEFAULT_LANG;
    }

    public static Language getLanguage(String shortName) {
        if (shortName != null)
            for (Language lang : VALUES)
                if (lang.getShortName().equalsIgnoreCase(shortName))
                    return lang;
        return Config.DEFAULT_LANG;
    }

    public static Language getLanguage(Locale locale) {
        for (Language lang : VALUES)
            if (lang.getLocale().equals(locale))
                return lang;
        return Config.DEFAULT_LANG;
    }
}
