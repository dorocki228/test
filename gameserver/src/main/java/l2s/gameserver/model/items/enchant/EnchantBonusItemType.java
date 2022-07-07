package l2s.gameserver.model.items.enchant;

public enum EnchantBonusItemType {

    ALL,
    SET,
    ARMOR,
    WEAPON;

    public String value() {
        return name();
    }

    public static EnchantBonusItemType fromValue(String v) {
        return valueOf(v);
    }

}
