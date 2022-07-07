package l2s.gameserver.network.authcomm;

public enum ServerType {
    NORMAL,
    RELAX,
    TEST,
    NO_LABEL,
    RESTRICTED,
    EVENT,
    FREE,
    UNK_7,
    UNK_8,
    UNK_9,
    CLASSIC;

    private final int _mask;

    ServerType() {
        _mask = 1 << ordinal();
    }

    public int getMask() {
        return _mask;
    }
}
