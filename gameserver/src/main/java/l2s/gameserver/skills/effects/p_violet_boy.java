package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class p_violet_boy extends Abnormal
{
	public p_violet_boy(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isPlayer() && getTemplate().checkCondition(this);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = getEffected().getPlayer();
		if(player != null)
		{
			player.startVioletBoy();
			player.sendStatusUpdate(true, true, 26);
			player.broadcastRelation();
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = getEffected().getPlayer();
		if(player != null)
		{
			player.stopVioletBoy();
			player.startPvPFlag(null);
			player.setLastPvPAttack(System.currentTimeMillis() - Config.PVP_TIME + 20000L);
		}
	}
}
