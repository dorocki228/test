package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExAttributeEnchantResultPacket implements IClientOutgoingPacket
{
	private final boolean _isWeapon;
	private final AttributeType attributeType;
	private final int _oldValue;
	private final int _newValue;
	private final int _usedStones;
	private final int _failedStones;

	public ExAttributeEnchantResultPacket(boolean isWeapon, AttributeType attributeType, int oldValue, int newValue, int usedStones, int failedStones)
	{
		_isWeapon = isWeapon;
		this.attributeType = attributeType;
		_oldValue = oldValue;
		_newValue = newValue;
		_usedStones = usedStones;
		_failedStones = failedStones;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ATTRIBUTE_ENCHANT_RESULT.writeId(packetWriter);
		packetWriter.writeH(0x00); // TODO
		packetWriter.writeH(0x00); // TODO
		packetWriter.writeC(_isWeapon ? 0x01 : 0x00); // Armor - 0x00 / Weapon - 0x01
		packetWriter.writeH(attributeType.getId()); // AttributeType
		packetWriter.writeH(_oldValue);
		packetWriter.writeH(_newValue);
		packetWriter.writeH(_usedStones);
		packetWriter.writeH(_failedStones);

		return true;
	}
}