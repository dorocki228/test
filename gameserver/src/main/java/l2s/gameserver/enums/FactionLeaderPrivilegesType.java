package l2s.gameserver.enums;

public enum FactionLeaderPrivilegesType {
    FULL(0x01);
    private final int mask;

    FactionLeaderPrivilegesType(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}
