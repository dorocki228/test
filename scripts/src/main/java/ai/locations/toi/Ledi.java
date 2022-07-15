package ai.locations.toi;

import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.AbnormalVisualEffect;

public class Ledi extends Mystic<NpcInstance>
{
	public Ledi(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActor().startAbnormalEffect(AbnormalVisualEffect.ULTIMATE_DEFENCE);
		getActor().getFlags().getDamageBlocked().start(this);
	}

	@Override
	protected void onEvtPartyDied(NpcInstance minion, Creature killer)
	{
		super.onEvtPartyDied(minion, killer);

		NpcInstance actor = getActor();
		if(!actor.getMinionList().hasAliveMinions())
		{
			actor.stopAbnormalEffect(AbnormalVisualEffect.ULTIMATE_DEFENCE);
			actor.getFlags().getDamageBlocked().stop(this);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.getMinionList().hasAliveMinions() && !actor.getFlags().getDamageBlocked().get())
		{
			actor.startAbnormalEffect(AbnormalVisualEffect.ULTIMATE_DEFENCE);
			actor.getFlags().getDamageBlocked().start(this);
		}
		super.onEvtAttacked(attacker, skill, damage);
	}
}