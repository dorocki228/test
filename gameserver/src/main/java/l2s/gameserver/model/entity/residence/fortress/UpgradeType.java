package l2s.gameserver.model.entity.residence.fortress;

public enum UpgradeType {
    GATE("Ворота"),
    GUARD("Стражников"),
    CRYSTAL("Кристалы"),
    GUARDIAN("Зашитника");

    private String name;

    UpgradeType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}