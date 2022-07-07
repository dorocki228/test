package l2s.gameserver.component.fraction;

import l2s.gameserver.GameServer;
import l2s.gameserver.component.fraction.listener.OnShutdown;
import l2s.gameserver.dao.FractionTreasureDAO;
import l2s.gameserver.model.base.Fraction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class FractionTreasure {
    private Map<Fraction, AtomicLong> data;
    private static FractionTreasure instance;

    private FractionTreasure() {
        data = new HashMap<>();
        GameServer.getInstance().addListener(new OnShutdown());
    }

    public static FractionTreasure getInstance() {
        if (instance == null)
            instance = new FractionTreasure();

        return instance;
    }

    public void init(final Fraction fraction, final long treasure) {
        data.put(fraction, new AtomicLong(treasure));
    }

    public void update(final Fraction fraction, final long count) {
        data.get(fraction).addAndGet(count);
    }

    public long get(final Fraction fraction) {
        return data.get(fraction).get();
    }

    public void save() {
        data.forEach((fraction, treasure) -> FractionTreasureDAO.getInstance().update(fraction, treasure.get()));
    }
}
