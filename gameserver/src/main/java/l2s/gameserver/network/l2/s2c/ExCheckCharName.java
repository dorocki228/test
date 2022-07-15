package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExCheckCharName implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket SUCCESS = new ExCheckCharName(-1); // Успешное создание чара.
	public static final IClientOutgoingPacket UNABLE_TO_CREATE_A_CHARACTER = new ExCheckCharName(0x00); // Не удалось создать персонажа.
	public static final IClientOutgoingPacket TOO_MANY_CHARACTERS = new ExCheckCharName(0x01); // Нельза создать персонажа. Удалите существующего и попробуйте еще раз.
	public static final IClientOutgoingPacket NAME_ALREADY_EXISTS = new ExCheckCharName(0x02); // Такое имя уже используется.
	public static final IClientOutgoingPacket ENTER_CHAR_NAME__MAX_16_CHARS = new ExCheckCharName(0x03); // Введите имя персонажа (максимум 16 символов).
	public static final IClientOutgoingPacket WRONG_NAME = new ExCheckCharName(0x04); // Не правильное имя, попробуйте еще раз.
	public static final IClientOutgoingPacket WRONG_SERVER = new ExCheckCharName(0x05); // Персонажи не могут быть созданы с этого сервера.
	public static final IClientOutgoingPacket DONT_CREATE_CHARS_ON_THIS_SERVER = new ExCheckCharName(0x06); // Нельзя создать персонажа на даном сервере. Действуют ограничения не позволяющие создавать песронажа.
	public static final IClientOutgoingPacket DONT_USE_ENG_CHARS = new ExCheckCharName(0x07); // Нельзя использовать англ символы в имени персонажа.

	public int _errorCode;

	public ExCheckCharName(int errorCode)
	{
		_errorCode = errorCode;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHECK_CHAR_NAME.writeId(packetWriter);
		packetWriter.writeD(_errorCode);

		return true;
	}
}
