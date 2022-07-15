package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * Format:   ddd
 * Пример пакета:
 * 40
 * c8 22 00 49
 * be 50 00 00
 * 86 25 0b 00
 * @author SYS
 */
public class MagicAndSkillList implements IClientOutgoingPacket
{
	private int _chaId;
	private int _unk1;
	private int _unk2;

	public MagicAndSkillList(Creature cha, int unk1, int unk2)
	{
		_chaId = cha.getObjectId();
		_unk1 = unk1;
		_unk2 = unk2;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MAGIC_AND_SKILL_LIST.writeId(packetWriter);
		packetWriter.writeD(_chaId);
		packetWriter.writeD(_unk1); // в снифе было 20670
		packetWriter.writeD(_unk2); // в снифе было 730502

		return true;
	}
}