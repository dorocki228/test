package l2s.gameserver.model.items.enchant;

public enum EnchantBonusStatFuncType {
    ADD,
    MUL;

    public String value() {
        return name();
    }

    public static EnchantBonusStatFuncType fromValue(String v) {
        return valueOf(v);
    }
}
