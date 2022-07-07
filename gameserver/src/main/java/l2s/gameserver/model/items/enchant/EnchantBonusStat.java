package l2s.gameserver.model.items.enchant;

import l2s.gameserver.stats.Stats;

/**
 * @author Java-man
 * @since 08.02.2019
 */
public class EnchantBonusStat {
    private final Stats stat;
    private final EnchantBonusStatFuncType func;
    private final double value;

    public EnchantBonusStat(Stats stat, EnchantBonusStatFuncType func, double value) {
        this.stat = stat;
        this.func = func;
        this.value = value;
    }

    public Stats getStat() {
        return stat;
    }

    public EnchantBonusStatFuncType getFunc() {
        return func;
    }

    public double getValue() {
        return value;
    }
}
