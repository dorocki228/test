package l2s.gameserver.service;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.MultiSellIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MultisellLoggingService {
    private static final MultisellLoggingService INSTANCE = new MultisellLoggingService();
    private final Map<Integer, ItemLog> itemMap = new HashMap<>();

    private MultisellLoggingService() {
        ThreadPoolManager.getInstance().scheduleAtFixedDelay(this::store, 1, 1, TimeUnit.MINUTES);
    }

    public static MultisellLoggingService getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param productions - итемы которые покупает игрок
     * @param amount - кол-во покупак за 1 раз
     */
    public void add(List<MultiSellIngredient> productions, long amount) {
        Map<Integer, ItemLog> map = productions.stream()
                .map(p ->
                {
                    String name = ItemHolder.getInstance().getTemplate(p.getItemId()).getName();
                    name = name == null ? "null" : name;
                    return new ItemLog(p.getItemId(), name, p.getItemCount() * amount);
                })
                .collect(Collectors.toMap(ItemLog::getItemId, i -> i, (i1, i2) -> {
                    i1.add(i2.getCount());
                    return i1;
                }));
        map.forEach((k, v) ->
                itemMap.merge(k, v, (i1, i2) -> {
                    i1.add(i2.getCount());
                    return i1;
                }));
    }

    public void store() {
        List<ItemLog> logs = new ArrayList<>(itemMap.values());
        itemMap.clear();
        DatabaseFactory.getInstance().getJdbcTemplate().
                batchUpdate("INSERT INTO multisell_logs(obj_id, name, count) VALUES(?,?,?) ON DUPLICATE KEY UPDATE count = count + VALUES(count)", logs, 100,
                        (ps, argument) -> {
                            ps.setInt(1, argument.getItemId());
                            ps.setString(2, argument.getName());
                            ps.setLong(3, argument.getCount());
                        });
    }

    public class ItemLog {
        private final int itemId;
        private final String name;
        private long count;

        ItemLog(int itemId, String name, long count) {
            this.itemId = itemId;
            this.name = name;
            this.count = count;
        }

        public void add(long count) {
            this.count += count;
        }

        public int getItemId() {
            return itemId;
        }

        public String getName() {
            return name;
        }

        public long getCount() {
            return count;
        }
    }
}
