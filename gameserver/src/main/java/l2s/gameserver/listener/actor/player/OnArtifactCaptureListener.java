package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.ArtifactInstance;

public interface OnArtifactCaptureListener extends CharListener
{
	void onArtifactCapture(Player player, ArtifactInstance artifact);
}
