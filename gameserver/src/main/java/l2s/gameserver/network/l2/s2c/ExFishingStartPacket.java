package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Format (ch)ddddd
 */
public class ExFishingStartPacket implements IClientOutgoingPacket
{
	private int _charObjId;
	private Location _loc;
	private int _fishType;
	private boolean _isNightLure;

	public ExFishingStartPacket(Creature character, int fishType, Location loc, boolean isNightLure)
	{
		_charObjId = character.getObjectId();
		_fishType = fishType;
		_loc = loc;
		_isNightLure = isNightLure;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FISHING_START.writeId(packetWriter);
		packetWriter.writeD(_charObjId);
		packetWriter.writeD(_fishType); // fish type
		packetWriter.writeD(_loc.x); // x poisson
		packetWriter.writeD(_loc.y); // y poisson
		packetWriter.writeD(_loc.z); // z poisson
		packetWriter.writeC(_isNightLure ? 0x01 : 0x00); // 0 = day lure  1 = night lure
		packetWriter.writeC(0x01); // result Button

		return true;
	}
}