package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExStopScenePlayer implements IClientOutgoingPacket
{
	private final int _movieId;

	public ExStopScenePlayer(int movieId)
	{
		_movieId = movieId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_STOP_SCENE_PLAYER.writeId(packetWriter);
		packetWriter.writeD(_movieId);

		return true;
	}
}
