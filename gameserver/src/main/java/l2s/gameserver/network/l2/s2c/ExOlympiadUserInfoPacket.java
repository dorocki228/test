package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExOlympiadUserInfoPacket implements IClientOutgoingPacket
{
	// cdSddddd
	private int _side, class_id, curHp, maxHp, curCp, maxCp;
	private int obj_id = 0;
	private String _name;

	public ExOlympiadUserInfoPacket(Player player, int side)
	{
		_side = side;
		obj_id = player.getObjectId();
		class_id = player.getClassId().getId();
		_name = player.getName();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curCp = (int) player.getCurrentCp();
		maxCp = player.getMaxCp();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_OLYMPIAD_USER_INFO.writeId(packetWriter);
		packetWriter.writeC(_side);
		packetWriter.writeD(obj_id);
		packetWriter.writeS(_name);
		packetWriter.writeD(class_id);
		packetWriter.writeD(curHp);
		packetWriter.writeD(maxHp);
		packetWriter.writeD(curCp);
		packetWriter.writeD(maxCp);

		return true;
	}
}