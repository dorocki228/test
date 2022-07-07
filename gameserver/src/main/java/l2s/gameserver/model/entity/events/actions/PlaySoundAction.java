package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;

import java.util.List;

public class PlaySoundAction implements EventAction
{
	private final int _range;
	private final String _sound;
	private final PlaySoundPacket.Type _type;

	public PlaySoundAction(int range, String s, PlaySoundPacket.Type type)
	{
		_range = range;
		_sound = s;
		_type = type;
	}

	@Override
	public void call(Event event)
	{
		GameObject object = event.getCenterObject();
		PlaySoundPacket packet = null;
		if(object != null)
			packet = new PlaySoundPacket(_type, _sound, 1, object.getObjectId(), object.getLoc());
		else
			packet = new PlaySoundPacket(_type, _sound, 0, 0, 0, 0, 0);
		List<Player> players = event.broadcastPlayers(_range);
		for(Player player : players)
			if(player != null)
				player.sendPacket(packet);
	}
}
