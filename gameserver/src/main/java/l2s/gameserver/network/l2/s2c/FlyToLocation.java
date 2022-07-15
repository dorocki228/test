package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

public class FlyToLocation implements IClientOutgoingPacket
{
	private final int _chaObjId;
	private final FlyType _type;
	private final ILocation _loc;
	private final ILocation _destLoc;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _animationSpeed;

	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		DUMMY,
		CHARGE,
		PUSH_HORIZONTAL,
		JUMP_EFFECTED,
		NONE,
		PUSH_DOWN_HORIZONTAL,
		WARP_BACK,
		WARP_FORWARD;
	}

	public FlyToLocation(Creature cha, ILocation destLoc, FlyType type)
	{
		this(cha, destLoc, type, 0, 0, 0);
	}

	public FlyToLocation(Creature cha, ILocation destLoc, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		_destLoc = destLoc;
		_type = type;
		_chaObjId = cha.getObjectId();
		_loc = cha;
		_flySpeed = flySpeed;
		_flyDelay = flyDelay;
		_animationSpeed = animationSpeed;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FLY_TO_LOCATION.writeId(packetWriter);
		packetWriter.writeD(_chaObjId);
		packetWriter.writeD(_destLoc.getX());
		packetWriter.writeD(_destLoc.getY());
		packetWriter.writeD(_destLoc.getZ());
		packetWriter.writeD(_loc.getX());
		packetWriter.writeD(_loc.getY());
		packetWriter.writeD(_loc.getZ());
		packetWriter.writeD(_type.ordinal());
		packetWriter.writeD(_flySpeed);
		packetWriter.writeD(_flyDelay);
		packetWriter.writeD(_animationSpeed);

		return true;
	}
}