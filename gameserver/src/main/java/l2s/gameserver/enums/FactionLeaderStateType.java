package l2s.gameserver.enums;

import java.util.Arrays;

public enum FactionLeaderStateType {
    NONE,
    INNINGS_REQUEST,
    VOTE;

    public static FactionLeaderStateType getValueFromOrdinal(int id) {
        return Arrays.stream(FactionLeaderStateType.values()).filter(p -> p.ordinal() == id).findFirst().orElse(null);
    }
}
