package l2s.gameserver.model.actor.basestats;

import l2s.gameserver.model.instances.PetInstance;

/**
 * @author Bonux
**/
public class PetBaseStats extends PlayableBaseStats
{
	public PetBaseStats(PetInstance owner)
	{
		super(owner);
	}

	@Override
	public PetInstance getOwner()
	{
		return (PetInstance) _owner;
	}
}