package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExChangeClientEffectInfo implements IClientOutgoingPacket
{
	private int _unk1;
	private int _unk2;
	private int _state;

	public ExChangeClientEffectInfo(int state)
	{
		_unk1 = 0;
		_unk2 = 0;
		_state = state;
	}

	public ExChangeClientEffectInfo(int unk1, int unk2, int state)
	{
		_unk1 = unk1;
		_unk2 = unk2;
		_state = state;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CLIENT_EFFECT_INFO.writeId(packetWriter);
		packetWriter.writeD(_unk1);
		packetWriter.writeD(_unk2);
		packetWriter.writeD(_state);

		return true;
	}
}