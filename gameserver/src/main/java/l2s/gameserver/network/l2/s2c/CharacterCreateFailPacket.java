package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class CharacterCreateFailPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket REASON_CREATION_FAILED = new CharacterCreateFailPacket(0x00); // "Your character creation has failed."
	public static final IClientOutgoingPacket REASON_TOO_MANY_CHARACTERS = new CharacterCreateFailPacket(0x01); // "You cannot create another character. Please delete the existing character and try again." Removes all settings that were selected (race, class, etc).
	public static final IClientOutgoingPacket REASON_NAME_ALREADY_EXISTS = new CharacterCreateFailPacket(0x02); // "This name already exists."
	public static final IClientOutgoingPacket REASON_16_ENG_CHARS = new CharacterCreateFailPacket(0x03); // "Your title cannot exceed 16 characters in length. Please try again."
	public static final IClientOutgoingPacket REASON_INCORRECT_NAME = new CharacterCreateFailPacket(0x04); // "Incorrect name. Please try again."
	public static final IClientOutgoingPacket REASON_CREATE_NOT_ALLOWED = new CharacterCreateFailPacket(0x05); // "Characters cannot be created from this server."
	public static final IClientOutgoingPacket REASON_CHOOSE_ANOTHER_SVR = new CharacterCreateFailPacket(0x06); // "Unable to create character. You are unable to create a new character on the selected server. A restriction is in place which restricts users from creating characters on different servers where no previous character exists. Please choose another server."

	private int _error;

	private CharacterCreateFailPacket(int errorCode)
	{
		_error = errorCode;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHARACTER_CREATE_FAIL.writeId(packetWriter);
		packetWriter.writeD(_error);
		return true;
	}
}