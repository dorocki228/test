package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeLock implements IClientOutgoingPacket {
	public static final ExCostumeLock FAIL = new ExCostumeLock(0, 0, false);

	private final int result;
	private final int costumeId;
	private final boolean locked;

	public ExCostumeLock(int result, int costumeId, boolean locked) {
		this.result = result;
		this.costumeId = costumeId;
		this.locked = locked;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_COSTUME_LOCK.writeId(packetWriter);
		packetWriter.writeC(result);   //Result
		packetWriter.writeD(costumeId);   //CostumeID
		packetWriter.writeC(locked);   //LockState

		return true;
	}
}