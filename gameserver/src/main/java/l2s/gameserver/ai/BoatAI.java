package l2s.gameserver.ai;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.boat.Boat;

/**
 * Author: VISTALL
 * Date:  16:56/28.12.2010
 */
public class BoatAI extends CharacterAI<Boat>
{
	public BoatAI(Boat actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtArrived()
	{
		Boat actor = getActor();
		if(actor == null)
			return;

		actor.onEvtArrived();
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}
