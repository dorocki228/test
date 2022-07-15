package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class SnoopPacket implements IClientOutgoingPacket
{
	private int _convoID;
	private String _name;
	private int _type;
	private int _fStringId;
	private String _speaker;
	private String _msg;
	private String[] _params;

	public SnoopPacket(int id, String name, int type, String speaker, String msg, int fStringId, String... params)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_fStringId = fStringId;
		_params = params;
	}

	public SnoopPacket(int id, String name, int type, String speaker, String msg)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SNOOP.writeId(packetWriter);
		packetWriter.writeD(_convoID);
		packetWriter.writeS(_name);
		packetWriter.writeD(0x00); // ??
		packetWriter.writeD(_type);
		packetWriter.writeS(_speaker);
		//packet.writeD(_fStringId);
		/*for(String param : _params)
			packet.writeS(param);*/
		packetWriter.writeS(_msg);

		return true;
	}
}