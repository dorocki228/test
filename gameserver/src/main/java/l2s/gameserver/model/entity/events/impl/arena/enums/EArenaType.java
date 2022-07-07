package l2s.gameserver.model.entity.events.impl.arena.enums;

/**
 * @author mangol
 */
public enum EArenaType {
	ONE_VS_ONE(1, "1x1"),
	TWO_VS_TWO(2, "2x2"),
	THREE_VS_THREE(3, "3x3"),
	FIVE_VS_FIVE(5, "5x5"),
	SEVEN_VS_SEVEN(7, "7x7");

	private final String str;
	private final int playerSize;

	EArenaType(int playerSize, String str) {
		this.playerSize = playerSize;
		this.str = str;
	}

	public static EArenaType getTypeFromName(String type) {
		for(EArenaType value : values()) {
			if(value.getName().equalsIgnoreCase(type)) {
				return value;
			}
		}
		throw new NullPointerException(type);
	}

	public String getName() {
		return str;
	}

	public int getPlayerSize() {
		return playerSize;
	}
}
