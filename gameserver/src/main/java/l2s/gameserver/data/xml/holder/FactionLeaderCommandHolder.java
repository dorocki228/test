package l2s.gameserver.data.xml.holder;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.utils.Language;

public class FactionLeaderCommandHolder extends AbstractHolder {
    private static FactionLeaderCommandHolder INSTANCE = new FactionLeaderCommandHolder();
    private final Table<String, Language, String> table = HashBasedTable.create();

    private FactionLeaderCommandHolder() {
    }

    public static FactionLeaderCommandHolder getInstance() {
        return INSTANCE;
    }

    public void put(String code, Language language, String message) {
        table.put(code, language, message);
    }

    public String getMessage(String code, Language language) {
        String message = table.get(code, language);
        if(message == null)
            message = table.get(code, language == Language.RUSSIAN ? Language.ENGLISH : Language.RUSSIAN);
        return message;
    }

    @Override
    public int size() {
        return table.size();
    }

    @Override
    public void clear() {
        table.clear();
    }

    public Table<String, Language, String> getTable() {
        return table;
    }
}
