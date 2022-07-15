package ai.custom;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author pchayka
 */
public class LabyrinthLostWarden extends Fighter<NpcInstance> {
	public LabyrinthLostWarden(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}