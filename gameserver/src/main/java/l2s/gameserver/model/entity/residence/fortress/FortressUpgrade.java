package l2s.gameserver.model.entity.residence.fortress;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FortressUpgrade {
    private static FortressUpgrade instance;
    private final Table<UpgradeType, Integer, UpgradeData> data;

    public FortressUpgrade() {
        data = HashBasedTable.create();
    }

    public static FortressUpgrade getInstance() {
        if (instance == null)
            instance = new FortressUpgrade();

        return instance;
    }

    public int size(final UpgradeType type) {
        return data.row(type).size();
    }

    public void put(final UpgradeType type, final int level, final UpgradeData data) {
        this.data.put(type, level, data);
    }

    public UpgradeData getData(final UpgradeType type, final int level) {
        return data.get(type, level);
    }
}