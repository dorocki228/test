package  l2s.Phantoms.ai.individual.Elf;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.FighterTask;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import  l2s.gameserver.tables.GmListTable;
import  l2s.gameserver.utils.Location;
import  l2s.gameserver.utils.PositionUtils;

public class WindRider extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		if (phantom.getAbnormalList().getEffectBySkillId(922) != null && phantom.getAbnormalList().getEffectBySkillId(922).getTimeLeft() > Rnd.get(3, 6))
			return;
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
			if (castControlSkill(target))
				return;
		// скиллы по ситуации
		if (castSituationSkill(target))
			return;
		if (castDebuffSkill(target))
			return;
		
		if (target != null && PositionUtils.isInFrontOf(target, phantom))
		{
			if (phantom.phantom_params.getGmLog())
				GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", phantom.getName() + "\n" + " PositionUtils.getDirectionTo"));
			if (target.isStunned() || target.isSleeping() || target.getTarget() != phantom || Rnd.chance(Rnd.get(3,7))|| phantom.getAbnormalList().containsEffects(922))
			{
				Location loc = getLocBehind(phantom, target);
				if (loc!=null)
				{
					phantom.abortAttack(true, false);
					phantom.moveToLocation(loc, 0, true);
					return;
				}
			}
		}
		castNukeSkill(target);
	}
	
	@Override
	public void startAITask(long delay)
	{
		startAITask(new FighterTask(getActor()), delay);
	}
	
	@Override
	public boolean isMelee()
	{
		return true;
	}
	
}