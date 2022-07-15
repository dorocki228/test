package ai.custom;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.LostItems;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;

/**
 * @author pchayka
 */
public class LabyrinthLostBeholder extends Fighter<NpcInstance>
{

	private static final int buff_id = 5700;
	private int buff_level = 1;

	public LabyrinthLostBeholder(NpcInstance actor)
	{
		super(actor);
	}

	protected void onEvtSpawn() {
		NpcInstance actor = getActor();
		Reflection r = actor.getReflection();
		if(!r.isDefault()) {
			buff_level = r.getInstancedZoneId() - 72;
			if(buff_level > 7)
				buff_level = 7;
			else if(buff_level < 1)
				buff_level = 1;
		}
		super.onEvtSpawn();    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	protected void onEvtDead(Creature killer, LostItems lostItems)
	{
		NpcInstance actor = getActor();
		Reflection r = actor.getReflection();
		if(!r.isDefault())
			if(checkMates(actor.getNpcId())) {
				NpcInstance captain;
				if((captain = findLostCaptain()) != null) {
					SkillHolder.getInstance().getSkill(buff_id, buff_level).getEffects(captain, captain);
				}
			}
		super.onEvtDead(killer, lostItems);
	}

	private boolean checkMates(int id)
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
			if(n.getNpcId() == id && !n.isDead())
				return false;
		return true;
	}

	private NpcInstance findLostCaptain()
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
			if(n instanceof ReflectionBossInstance)
				return n;
		return null;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}