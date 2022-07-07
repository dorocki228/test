package l2s.gameserver.model.items.enchant;

import l2s.gameserver.templates.item.ItemGrade;

import java.util.List;

/**
 * @author Java-man
 * @since 08.02.2019
 */
public class EnchantBonus {
    private final int enchant;
    private final EnchantBonusItemType itemType;
    private final ItemGrade grade;
    private final List<EnchantBonusStat> stats;

    public EnchantBonus(int enchant, EnchantBonusItemType itemType, ItemGrade grade, List<EnchantBonusStat> stats) {
        this.enchant = enchant;
        this.itemType = itemType;
        this.grade = grade;
        this.stats = stats;
    }

    public int getEnchant() {
        return enchant;
    }

    public EnchantBonusItemType getItemType() {
        return itemType;
    }

    public ItemGrade getGrade() {
        return grade;
    }

    public List<EnchantBonusStat> getStats() {
        return stats;
    }
}
