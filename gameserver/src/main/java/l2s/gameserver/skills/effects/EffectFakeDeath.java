package l2s.gameserver.skills.effects;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectFakeDeath extends Abnormal
{
	public EffectFakeDeath(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isInvisible(null))
			return false;
		if(_effected.isPlayer())
		{
			Player player = _effected.getPlayer();
			if(player.getActiveWeaponFlagAttachment() != null)
				return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = (Player) getEffected();
		player.setFakeDeath(true);
		player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		player.broadcastPacket(new ChangeWaitTypePacket(player, 2));
		player.broadcastCharInfo();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = (Player) getEffected();
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);
		player.broadcastPacket(new ChangeWaitTypePacket(player, 3));
		if(getSkill().getId() == 10528)
			player.setTargetable(true);
		player.broadcastPacket(new RevivePacket(player));
		player.broadcastCharInfo();
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndBreakFakeDeathTask(player), 2500L);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;
		double manaDam = calc();
		if(manaDam > getEffected().getCurrentMp() && getSkill().isToggle())
		{
			getEffected().sendPacket(SystemMsg.NOT_ENOUGH_MP);
			getEffected().sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}
		getEffected().reduceCurrentMp(manaDam, null);
		return true;
	}
}
