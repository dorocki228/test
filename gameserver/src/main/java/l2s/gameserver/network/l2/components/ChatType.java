package l2s.gameserver.network.l2.components;

public enum ChatType
{
	ALL,
	SHOUT,
	TELL,
	PARTY,
	CLAN,
	GM,
	PETITION_PLAYER,
	PETITION_GM,
	FRACTION_SHOUT,
	ALLIANCE,
	ANNOUNCEMENT,
	SYSTEM_MESSAGE,
	FRIENDTELL,
	MSNCHAT,
	PARTY_ROOM,
	COMMANDCHANNEL_ALL,
	COMMANDCHANNEL_COMMANDER,
	HERO_VOICE,
	CRITICAL_ANNOUNCE,
	SCREEN_ANNOUNCE,
	BATTLEFIELD,
	MPCC_ROOM,
	NPC_ALL,
	NPC_SHOUT,
	BLUE_UNK,
	FRACTION_WORLD;

	public static final ChatType[] VALUES;

	static
	{
		VALUES = values();
	}

	public static ChatType getTypeIfPresent(String type){
		for (ChatType value : VALUES) {
			if(value.name().equals(type))
				return value;
		}
		return null;
	}
}
