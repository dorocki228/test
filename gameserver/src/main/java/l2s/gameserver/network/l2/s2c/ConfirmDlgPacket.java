package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.components.SystemMsg;

public class ConfirmDlgPacket extends SysMsgContainer<ConfirmDlgPacket>
{
	private final int _time;
	private int _requestId;

	public ConfirmDlgPacket(SystemMsg msg, int time)
	{
		super(msg);
		_time = time;
	}

	@Override
	protected final void writeImpl()
	{
		writeElements();
        writeD(_time);
        writeD(_requestId);
	}

	@Override
	protected final void writeMessageIdAndElementsSize()
	{
		writeD(_message.getId());
		writeD(_arguments.size());
	}

	@Override
	protected final void writeType(Types type)
	{
		writeD(type.ordinal());
	}

	public void setRequestId(int requestId)
	{
		_requestId = requestId;
	}
}
