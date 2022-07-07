package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.BlockListPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBlock extends L2GameClientPacket
{
	private static final Logger _log;
	private static final int BLOCK = 0;
	private static final int UNBLOCK = 1;
	private static final int BLOCKLIST = 2;
	private static final int ALLBLOCK = 3;
	private static final int ALLUNBLOCK = 4;
	private Integer _type;
	private String targetName;

	public RequestBlock()
	{
		targetName = null;
	}

	@Override
	protected void readImpl()
	{
		_type = readD();
		if(_type == 0 || _type == 1)
			targetName = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		switch(_type)
		{
			case 0:
			{
				activeChar.getBlockList().add(targetName);
				break;
			}
			case 1:
			{
				activeChar.getBlockList().remove(targetName);
				break;
			}
			case 2:
			{
				activeChar.sendPacket(new BlockListPacket(activeChar));
				break;
			}
			case 3:
			{
				activeChar.setBlockAll(true);
				activeChar.sendPacket(SystemMsg.YOU_ARE_NOW_BLOCKING_EVERYTHING);
				activeChar.sendEtcStatusUpdate();
				break;
			}
			case 4:
			{
				activeChar.setBlockAll(false);
				activeChar.sendPacket(SystemMsg.YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING);
				activeChar.sendEtcStatusUpdate();
				break;
			}
			default:
			{
				_log.info("Unknown 0x0a block type: " + _type);
				break;
			}
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestBlock.class);
	}
}
