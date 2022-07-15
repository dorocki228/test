package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Author: VISTALL
 */
public class ExSubPledgetSkillAdd implements IClientOutgoingPacket
{
	private int _type, _id, _level;

	public ExSubPledgetSkillAdd(int type, int id, int level)
	{
		_type = type;
		_id = id;
		_level = level;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SUBPLEDGE_SKILL_ADD.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeD(_id);
		packetWriter.writeD(_level);

		return true;
	}
}