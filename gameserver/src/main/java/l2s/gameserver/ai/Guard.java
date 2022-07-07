package l2s.gameserver.ai;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.instances.NpcInstance;

public class Guard extends Fighter
{
	public Guard(NpcInstance actor)
	{
		super(actor);
	}

	@Override
    public boolean canAttackCharacter(Creature target)
    {
        NpcInstance actor = getActor();
        if(target.isPK() || actor.getFraction().canAttack(target.getFraction()))
            return target.isPlayable() || target.isGuard();
        else
        {
            return false;
        }
    }

	@Override
	public boolean checkTarget(Creature target, int range)
	{
		return super.checkTarget(target, range) && canAttackCharacter(target);
	}

	@Override
	public int getMaxAttackTimeout()
	{
		return 0;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	public boolean canSeeInSilentMove(Playable target)
	{
		// гварды могут видеть игроков в режиме Silent Move
		return true;
	}
}
