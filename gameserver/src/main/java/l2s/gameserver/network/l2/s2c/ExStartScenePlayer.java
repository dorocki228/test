package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExStartScenePlayer implements IClientOutgoingPacket
{
	private final int _sceneId;

	public ExStartScenePlayer(int sceneId)
	{
		_sceneId = sceneId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_START_SCENE_PLAYER.writeId(packetWriter);
		packetWriter.writeD(_sceneId);
		packetWriter.writeD(-1);	// TODO[UNDERGROUND]: UNK

		return true;
	}
}