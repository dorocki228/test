package l2s.gameserver.model.actor.basestats;

import l2s.gameserver.model.Playable;

/**
 * @author Bonux
**/
public class PlayableBaseStats extends CreatureBaseStats
{
	public PlayableBaseStats(Playable owner)
	{
		super(owner);
	}

	@Override
	public Playable getOwner()
	{
		return (Playable) _owner;
	}

}