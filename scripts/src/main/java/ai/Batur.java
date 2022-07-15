package ai;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.instances.NpcInstance;
import bosses.BaiumManager;

/*
 * @author SanyaDC
 */
public class Batur extends Fighter<NpcInstance>
{
	private final AtomicBoolean taskLaunched = new AtomicBoolean(false);

	public Batur(NpcInstance actor)
	{
		super(actor);		
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		if (taskLaunched.compareAndSet(false, true)) {
			ThreadPoolManager.getInstance().schedule(() -> getActor().deleteMe(), 1, TimeUnit.MINUTES);
		}

		super.onEvtAttacked(attacker, skill, damage);
	}
}