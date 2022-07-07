package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.enchant.EnchantBonus;
import l2s.gameserver.model.items.enchant.EnchantBonusItemType;
import l2s.gameserver.model.items.enchant.EnchantBonusStat;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.item.ItemGrade;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Java-man
 * @since 22.12.2018
 */
public final class EnchantBonusHolder extends AbstractHolder {
    private static final EnchantBonusHolder INSTANCE = new EnchantBonusHolder();

    private final List<EnchantBonus> bonuses = new ArrayList<>();

    public static EnchantBonusHolder getInstance() {
        return INSTANCE;
    }

    public void addBonuses(Collection<EnchantBonus> newBonuses) {
        bonuses.addAll(newBonuses);
    }

    public List<EnchantBonusStat> getBonuses(Player player, ItemInstance item, Stats stat) {
        if (item.getEnchantLevel() == 0) {
            return Collections.emptyList();
        }

        if (item.getFixedEnchantLevel(player) == 0) {
            return Collections.emptyList();
        }

        EnchantBonusItemType itemType;
        if (item.isWeapon()) {
            itemType = EnchantBonusItemType.WEAPON;
        } else if (item.isArmor()) {
            itemType = EnchantBonusItemType.ARMOR;
        } else {
            return Collections.emptyList();
        }

        return bonuses.stream()
                .filter(bonusType -> bonusType.getEnchant() == item.getEnchantLevel())
                .filter(bonusType -> bonusType.getItemType() == itemType
                        || bonusType.getItemType() == EnchantBonusItemType.ALL)
                .filter(bonusType -> bonusType.getGrade() == item.getGrade() || bonusType.getGrade() == ItemGrade.NONE)
                .flatMap(bonusType -> bonusType.getStats().stream())
                .filter(statType -> statType.getStat() == stat)
                .collect(Collectors.toList());
    }

    public List<EnchantBonusStat> getBonusesForSet(int enchantLevel) {
        if (enchantLevel == 0) {
            return Collections.emptyList();
        }

        return bonuses.stream()
                .filter(bonusType -> bonusType.getEnchant() == enchantLevel)
                .filter(bonusType -> bonusType.getItemType() == EnchantBonusItemType.SET)
                .flatMap(bonusType -> bonusType.getStats().stream())
                .collect(Collectors.toList());
    }

    public Set<Stats> getBonusStats(EnchantBonusItemType itemType, ItemGrade grade) {
        return bonuses.stream()
                .filter(bonusType -> bonusType.getItemType() == itemType
                        || bonusType.getItemType() == EnchantBonusItemType.ALL)
                .filter(bonusType -> bonusType.getGrade() == grade || bonusType.getGrade() == ItemGrade.NONE)
                .flatMap(bonusType -> bonusType.getStats().stream())
                .map(EnchantBonusStat::getStat)
                .collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return bonuses.size();
    }

    @Override
    public void clear() {
        bonuses.clear();
    }
}
