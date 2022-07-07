package l2s.gameserver.model.items.enchant;

import java.util.List;

/**
 * @author Java-man
 * @since 08.02.2019
 */
public class EnchantBonuses {
    private final List<EnchantBonus> bonuses;

    public EnchantBonuses(List<EnchantBonus> bonuses) {
        this.bonuses = bonuses;
    }

    public List<EnchantBonus> getBonuses() {
        return bonuses;
    }
}
