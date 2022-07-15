package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PetStatusUpdatePacket implements IClientOutgoingPacket
{
	private int type, obj_id, level;
	private int maxFed, curFed, maxHp, curHp, maxMp, curMp;
	private long exp, exp_this_lvl, exp_next_lvl;
	private Location _loc;
	private String title;

	public PetStatusUpdatePacket(final Servitor summon)
	{
		type = summon.getServitorType();
		obj_id = summon.getObjectId();
		_loc = summon.getLoc();

		title = summon.getVisibleName(summon.getPlayer());

		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxFed();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PET_STATUS_UPDATE.writeId(packetWriter);
		packetWriter.writeD(type);
		packetWriter.writeD(obj_id);
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeS(title);
		packetWriter.writeD(curFed);
		packetWriter.writeD(maxFed);
		packetWriter.writeD(curHp);
		packetWriter.writeD(maxHp);
		packetWriter.writeD(curMp);
		packetWriter.writeD(maxMp);
		packetWriter.writeD(level);
		packetWriter.writeQ(exp);
		packetWriter.writeQ(exp_this_lvl);// 0% absolute value
		packetWriter.writeQ(exp_next_lvl);// 100% absolute value
		packetWriter.writeD(1); // ???

		return true;
	}
}