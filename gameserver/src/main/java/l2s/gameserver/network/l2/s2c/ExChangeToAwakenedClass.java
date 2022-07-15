package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExChangeToAwakenedClass implements IClientOutgoingPacket
{
	private int _classId;

	public ExChangeToAwakenedClass(Player player, NpcInstance npc, int classId)
	{
		_classId = classId;
		player.setLastNpc(npc);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_TO_AWAKENED_CLASS.writeId(packetWriter);
		packetWriter.writeD(_classId);

		return true;
	}
}
