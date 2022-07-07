package l2s.gameserver.component.fraction;

import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Castle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Заготовка под хранение бустов, пока тут высер
// FIXME: Убейте кодера
public class FractionBoostBalancer {
    private static FractionBoostBalancer instance;
    private Map<Fraction, List<Castle>> castles;

    private FractionBoostBalancer() {
        this.castles = new HashMap<>();
    }

    public static FractionBoostBalancer getInstance() {
        if (instance == null)
            instance = new FractionBoostBalancer();
        return instance;
    }

    public void updateList(final Fraction fraction, final Castle castle) {
        get(fraction).add(castle);
        get(fraction.revert()).remove(castle);
    }

    private List<Castle> get(final Fraction fraction) {
        return castles.computeIfAbsent(fraction, list -> new ArrayList<>());
    }

    public int getCount(final Fraction fraction) {
        return get(fraction).size();
    }
}