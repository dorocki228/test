package l2s.gameserver.model.instances;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.NpcInfoState;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.ScheduledFuture;

/**
 * @author KanuToIIIKa
 */

public class WildHohCannonInstance extends SummonInstance
{

	public WildHohCannonInstance(int objectId, NpcTemplate template, Player owner, int lifetime, int consumeid, int consumecount, int consumedelay, int summonPoints, Skill skill, boolean saveable)
	{
		super(objectId, template, owner, lifetime, consumeid, consumecount, consumedelay, summonPoints, skill, saveable);

		for(SkillEntry s : getAllSkills())
		{
			if(!s.getTemplate().isActive())
				continue;

			addUnActiveSkill(s.getTemplate());
		}
	}

	private boolean isInRangeMode;

	private ScheduledFuture<?> changeModeThread = null;

	public void changeMod()
	{
		if(isInChangingMode())
			return;

		if(!isInRangeMode)
			setFollowMode(false);

		broadcastPacket(new ChangeWaitTypePacket(this, isInRangeMode ? 1 : 0));

		changeModeThread = ThreadPoolManager.getInstance().schedule(new ChangeMode(), 30000);
	}

	private boolean isInChangingMode()
	{
		return changeModeThread != null && !changeModeThread.isDone();
	}

	@Override
	public boolean isActionsDisabled(boolean withCast)
	{
		return isInChangingMode() || super.isActionsDisabled(withCast);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return isInChangingMode() || isInRangeMode || super.isMovementDisabled();
	}

	private class ChangeMode implements Runnable
	{
		@Override
		public void run()
		{
			isInRangeMode = !isInRangeMode;

			if(!isInRangeMode)
			{
				setFollowMode(true);
				broadcastPacket(new NpcInfoState(WildHohCannonInstance.this, 0));
			}
			else
				broadcastPacket(new NpcInfoState(WildHohCannonInstance.this, 2));

			for(SkillEntry s : getAllSkills())
			{
				if(!s.getTemplate().isActive())
					continue;

				if(!isInRangeMode)
					addUnActiveSkill(s.getTemplate());
				else
					removeUnActiveSkill(s.getTemplate());
			}
		}

	}
}
