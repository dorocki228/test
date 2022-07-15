package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPartyPetWindowAdd implements IClientOutgoingPacket
{
	private final int ownerId, npcId, type, curHp, maxHp, curMp, maxMp, level;
	private final int summonId;
	private final String name;

	public ExPartyPetWindowAdd(Servitor summon)
	{
		summonId = summon.getObjectId();
		ownerId = summon.getPlayer().getObjectId();
		npcId = summon.getNpcId() + 1000000;
		type = summon.getServitorType();
		name = summon.getName();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		level = summon.getLevel();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PARTY_PET_WINDOW_ADD.writeId(packetWriter);
		packetWriter.writeD(summonId);
		packetWriter.writeD(npcId);
		packetWriter.writeD(type);
		packetWriter.writeD(ownerId);
		packetWriter.writeS(name);
		packetWriter.writeD(curHp);
		packetWriter.writeD(maxHp);
		packetWriter.writeD(curMp);
		packetWriter.writeD(maxMp);
		packetWriter.writeD(level);

		return true;
	}
}