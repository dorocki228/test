package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class MTLPacket extends L2GameServerPacket
{
	private final int _objectId;
	private int _client_z_shift;
	private final Location _current;
	private Location _destination;

	public MTLPacket(Creature cha)
	{
		this(cha, cha.getLoc(), cha.getDestination());
	}

	public MTLPacket(Creature cha, Location from, Location to)
	{
		_objectId = cha.getObjectId();
		_current = from;
		_destination = to;

		if(!cha.isFlying())
			_client_z_shift = Config.CLIENT_Z_SHIFT;

		if(cha.isInWater())
			_client_z_shift += Config.CLIENT_Z_SHIFT;

		if(_destination == null)
		{
			_destination = _current;

			String message = "CharMoveToLocation: desc is null, but moving. Character: {}; Loc: {}";
			ParameterizedMessage parameterizedMessage = new ParameterizedMessage(message, cha, _current);
			LogService.getInstance().log(LoggerType.DEBUG, parameterizedMessage);
		}
	}

	public MTLPacket(int objectId, Location from, Location to)
	{
		_objectId = objectId;
		_current = from;
		_destination = to;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z + _client_z_shift);
		writeD(_current.x);
		writeD(_current.y);
		writeD(_current.z + _client_z_shift);
	}
}
