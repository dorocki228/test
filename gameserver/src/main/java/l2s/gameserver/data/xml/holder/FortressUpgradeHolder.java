package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.entity.residence.fortress.FortressUpgrade;

import java.util.HashMap;
import java.util.Map;

public class FortressUpgradeHolder extends AbstractHolder {
    private static FortressUpgradeHolder instance = new FortressUpgradeHolder();
    private final Map<Integer, FortressUpgrade> map = new HashMap<>();

    public static FortressUpgradeHolder getInstance() {
        return instance;
    }

    public void add(int id, FortressUpgrade upgrade) {
        map.put(id, upgrade);
    }

    public FortressUpgrade get(int id) {
        return map.get(id);
    }

    public Map<Integer, FortressUpgrade> getMap() {
        return map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }
}