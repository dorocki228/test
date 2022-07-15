package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPartyPetWindowUpdate implements IClientOutgoingPacket
{
	private int owner_obj_id, npc_id, _type, curHp, maxHp, curMp, maxMp, level;
	private int obj_id = 0;
	private String _name;

	public ExPartyPetWindowUpdate(Servitor summon)
	{
		obj_id = summon.getObjectId();
		owner_obj_id = summon.getPlayer().getObjectId();
		npc_id = summon.getNpcId() + 1000000;
		_type = summon.getServitorType();
		_name = summon.getName();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		level = summon.getLevel();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PARTY_PET_WINDOW_UPDATE.writeId(packetWriter);
		packetWriter.writeD(obj_id);
		packetWriter.writeD(npc_id);
		packetWriter.writeD(_type);
		packetWriter.writeD(owner_obj_id);
		packetWriter.writeS(_name);
		packetWriter.writeD(curHp);
		packetWriter.writeD(maxHp);
		packetWriter.writeD(curMp);
		packetWriter.writeD(maxMp);
		packetWriter.writeD(level);

		return true;
	}
}