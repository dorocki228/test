package l2s.gameserver.model.instances;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.PetDataHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public final class PetBabyInstance extends PetInstance
{
	private static final long serialVersionUID = 1L;
	private static final Logger _log;
	private Future<?> _actionTask;
	private boolean _buffEnabled;
	private final TIntObjectMap<List<Skill>> _buffSkills;
	private static final int HealTrick = 4717;
	private static final int GreaterHealTrick = 4718;
	private static final int GreaterHeal = 5195;
	private static final int BattleHeal = 5590;
	private static final int Recharge = 5200;

	public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, long exp)
	{
		super(objectId, template, owner, control, exp);
		_buffEnabled = true;
		_buffSkills = new TIntObjectHashMap();
		parseSkills();
	}

	public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control)
	{
		super(objectId, template, owner, control);
		_buffEnabled = true;
		_buffSkills = new TIntObjectHashMap();
		parseSkills();
	}

	private void parseSkills()
	{
		for(int step = 0; step < 10; ++step)
		{
			List<Skill> skills = _buffSkills.get(step);
			for(int buff = 1; buff < 10; ++buff)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_buff0" + buff, null);
				if(data == null)
					break;
				if(skills == null)
				{
					skills = new ArrayList<>();
					_buffSkills.put(step, skills);
				}
				int[][] stringToIntArray2X;
				int[][] skillsData = stringToIntArray2X = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : stringToIntArray2X)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}
		}
	}

	public List<Skill> getBuffs()
	{
		for(int step = getBuffLevel(); step >= 0; --step)
		{
			List<Skill> skills = _buffSkills.get(step);
			if(skills != null)
				return skills;
		}
		return Collections.emptyList();
	}

	private Skill getHealSkill(int hpPercent)
	{
		if(PetDataHolder.isImprovedBabyPet(getNpcId()))
		{
			if(hpPercent < 90)
			{
				if(hpPercent < 33)
					return SkillHolder.getInstance().getSkill(5590, getHealLevel());
				if(getNpcId() != 16035)
					return SkillHolder.getInstance().getSkill(5195, getHealLevel());
			}
		}
		else if(PetDataHolder.isBabyPet(getNpcId()))
		{
			if(hpPercent < 90)
			{
				if(hpPercent < 33)
					return SkillHolder.getInstance().getSkill(4718, getHealLevel());
				return SkillHolder.getInstance().getSkill(4717, getHealLevel());
			}
		}
		else
			switch(getNpcId())
			{
				case 16045:
				case 16052:
				{
					if(hpPercent >= 70)
						break;
					if(hpPercent < 30)
						return SkillHolder.getInstance().getSkill(5590, getHealLevel());
					return SkillHolder.getInstance().getSkill(5195, getHealLevel());
				}
				case 16046:
				case 16051:
				{
					if(hpPercent < 30)
						return SkillHolder.getInstance().getSkill(5590, getHealLevel());
					break;
				}
			}
		return null;
	}

	private Skill getManaHealSkill(int mpPercent)
	{
		switch(getNpcId())
		{
			case 16035:
			{
				if(mpPercent < 66)
					return SkillHolder.getInstance().getSkill(5200, getRechargeLevel());
				break;
			}
			case 16046:
			case 16051:
			{
				if(mpPercent < 50)
					return SkillHolder.getInstance().getSkill(5200, getRechargeLevel());
				break;
			}
		}
		return null;
	}

	public Skill onActionTask()
	{
		try
		{
			Player owner = getPlayer();
			if(!owner.isDead() && !owner.isInvul() && !isCastingNow())
			{
				if(getAbnormalList().containsEffects(5753))
					return null;
				if(getAbnormalList().containsEffects(5771))
					return null;
                if(!Config.ALT_PET_HEAL_BATTLE_ONLY || owner.isInCombat())
				{
					double curHp = owner.getCurrentHpPercents();
                    Skill skill = null;
                    if(Rnd.chance((100.0 - curHp) / 3.0))
						skill = getHealSkill((int) curHp);
					if(skill == null)
					{
						double curMp = owner.getCurrentMpPercents();
						if(Rnd.chance((100.0 - curMp) / 2.0))
							skill = getManaHealSkill((int) curMp);
					}
					if(skill != null && skill.checkCondition(this, owner, false, !isFollowMode(), true))
					{
						setTarget(owner);
						getAI().Cast(skill, owner, false, !isFollowMode());
						return skill;
					}
				}
				if(owner.isInOfflineMode() || owner.getAbnormalList().containsEffects(5771))
					return null;
				Label_0221: for(Skill buff : getBuffs())
				{
					if(getCurrentMp() < buff.getMpConsume2())
						continue;
					for(Abnormal ef : owner.getAbnormalList().getEffects())
						if(checkEffect(ef, buff))
							continue Label_0221;
					if(buff.checkCondition(this, owner, false, !isFollowMode(), true))
					{
						setTarget(owner);
						getAI().Cast(buff, owner, false, !isFollowMode());
						return buff;
					}
					return null;
				}
			}
		}
		catch(Throwable e)
		{
			_log.warn("Pet [#" + getNpcId() + "] a buff task error has occurred: " + e);
			_log.error("", e);
		}
		return null;
	}

	private boolean checkEffect(Abnormal ef, Skill skill)
	{
		if(ef == null)
			return false;
		if(ef.checkBlockedAbnormalType(skill.getAbnormalType()))
			return true;
		EffectTemplate effectTemplate = skill.getEffectTemplates(EffectUseType.NORMAL).get(0);
		return AbnormalList.checkAbnormalType(ef.getTemplate(), effectTemplate) && ef.getAbnormalLvl() >= effectTemplate.getAbnormalLvl() && (ef.getTimeLeft() > 10 || ef.getNext() != null && checkEffect(ef.getNext(), skill));
	}

	public synchronized void stopBuffTask()
	{
		if(_actionTask != null)
		{
			_actionTask.cancel(false);
			_actionTask = null;
		}
	}

	public synchronized void startBuffTask()
	{
		if(_actionTask != null)
			stopBuffTask();
		if(_actionTask == null && !isDead())
			_actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), 5000L);
	}

	public boolean isBuffEnabled()
	{
		return _buffEnabled;
	}

	public void triggerBuff()
	{
		_buffEnabled = !_buffEnabled;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		stopBuffTask();
		super.onDeath(killer);
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		startBuffTask();
	}

	@Override
	public void unSummon(boolean logout)
	{
		stopBuffTask();
		super.unSummon(logout);
	}

	public int getHealLevel()
	{
		return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 12), 1), 12);
	}

	public int getRechargeLevel()
	{
		return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 8), 1), 8);
	}

	public int getBuffLevel()
	{
		if(PetDataHolder.isSpecialPet(getNpcId()))
		{
			if(getLevel() < 10)
				return 0;
			if(getLevel() < 20)
				return 1;
			if(getLevel() < 30)
				return 2;
			if(getLevel() < 40)
				return 3;
			if(getLevel() < 50)
				return 4;
			if(getLevel() < 60)
				return 5;
			if(getLevel() < 70)
				return 6;
			if(getLevel() >= 70)
				return 7;
		}
		else
		{
			if(getLevel() < 60)
				return 0;
			if(getLevel() < 65)
				return 1;
			if(getLevel() < 70)
				return 2;
			if(getLevel() < 75)
				return 3;
			if(getLevel() < 80)
				return 4;
			if(getLevel() >= 80)
				return 5;
		}
		return 0;
	}

	@Override
	public int getSoulshotConsumeCount()
	{
		return 1;
	}

	@Override
	public int getSpiritshotConsumeCount()
	{
		return 1;
	}

	static
	{
		_log = LoggerFactory.getLogger(PetBabyInstance.class);
	}

	class ActionTask implements Runnable
	{
		@Override
		public void run()
		{
			Skill skill = onActionTask();
			_actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), skill == null ? 1000L : (long) Formulas.calcSkillCastSpd(PetBabyInstance.this, skill, skill.getHitTime()));
		}
	}
}
