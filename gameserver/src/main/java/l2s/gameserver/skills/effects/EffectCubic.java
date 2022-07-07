package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.xml.holder.CubicHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.CubicTemplate;
import l2s.gameserver.templates.CubicTemplate.SkillInfo;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EffectCubic extends Abnormal
{
	private static final Logger _log = LoggerFactory.getLogger(EffectCubic.class);
	private final CubicTemplate _template;

	public EffectCubic(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		int cubicId = getTemplate().getParam().getInteger("id", 0);
		int cubicLevel = getTemplate().getParam().getInteger("level", 0);
		if(cubicId > 0 && cubicLevel > 0)
		{
			_template = CubicHolder.getInstance().getTemplate(cubicId, cubicLevel);
			setTimeLeft(getDuration());
		}
		else
		{
			_template = null;
			_log.warn(getClass().getSimpleName() + ": Cannot find cubic template for skill: ID[" + getSkill().getId() + "], LEVEL[" + getSkill().getLevel() + "]!");
		}
	}

	@Override
	public boolean checkCondition()
	{
		if(!getEffected().isPlayer())
			return false;
		if(_template == null)
			return false;
		Player player = getEffected().getPlayer();
		if(player.getCubic(_template.getId()) != null)
			return true;
		int size = Math.max(1, (int) player.calcStat(Stats.CUBICS_LIMIT, 0));
		if(player.getCubics().size() >= size)
		{
			EffectCubic c = null;
			int effectTimeLeft = Integer.MAX_VALUE;

			for(EffectCubic cubic : player.getCubics())
			{
				if(cubic.getTimeLeft() < effectTimeLeft)
				{
					effectTimeLeft = cubic.getTimeLeft();
					c = cubic;
				}
			}

			if(c != null)
				c.exit();
		}

		return true;
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player == null)
			return;
		player.addCubic(this);
	}

	@Override
	protected void onExit()
	{
		super.onExit();
		Player player = _effected.getPlayer();
		if(player == null)
			return;

		for(Entry<Integer, List<SkillInfo>> e : _template.getSkills())
		{
			for(SkillInfo info : e.getValue())
				player.enableSkill(info.getSkill());
		}

		player.removeCubic(getId());
	}

	@Override
	protected boolean onActionTime()
	{
		if(!getEffected().isPlayer())
			return true;
		Player player = getEffected().getPlayer();
		if(player == null)
			return true;
		for(Map.Entry<Integer, List<CubicTemplate.SkillInfo>> entry : _template.getSkills())
			if(Rnd.chance(entry.getKey()))
			{
				for(CubicTemplate.SkillInfo skillInfo : entry.getValue())
				{
					if(player.isSkillDisabled(skillInfo.getSkill()))
						continue;
					switch(skillInfo.getActionType())
					{
						case ATTACK:
						{
							doAttack(player, skillInfo, _template.getDelay());
							continue;
						}
						case BUFF:
						{
							doBuff(player, skillInfo, _template.getDelay());
							continue;
						}
						case DEBUFF:
						{
							doDebuff(player, skillInfo, _template.getDelay());
							continue;
						}
						case HEAL:
						{
							doHeal(player, skillInfo, _template.getDelay());
							continue;
						}
						case MANA:
						{
							doMana(player, skillInfo, _template.getDelay());
							continue;
						}
						case CANCEL:
						{
							doCancel(player, skillInfo, _template.getDelay());
							continue;
						}
					}
				}
				break;
			}
		return true;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public int getDuration()
	{
		return _template.getDuration();
	}

	@Override
	public int getInterval()
	{
		return 1;
	}

	public int getId()
	{
		return _template.getId();
	}

	private static void doHeal(Player player, CubicTemplate.SkillInfo info, int delay)
	{
		Skill skill = info.getSkill();
		Creature target = null;
		if(player.getParty() == null)
		{
			if(!player.isCurrentHpFull() && !player.isDead())
				target = player;
		}
		else
		{
			double currentHp = 2.147483647E9;
			for(Player member : player.getParty().getPartyMembers())
			{
				if(member == null)
					continue;
				if(info.getSkill().getCastRange() != -1 && !player.isInRange(member, info.getSkill().getCastRange()) || member.isCurrentHpFull() || member.isDead() || member.getCurrentHp() >= currentHp)
					continue;
				currentHp = member.getCurrentHp();
				target = member;
			}
		}
		if(target == null)
			return;
		int chance = info.getChance((int) target.getCurrentHpPercents());
		if(!Rnd.chance(chance))
			return;
		Creature aimTarget = target;
		if(!skill.isNotBroadcastable())
			player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		player.disableSkill(skill, delay * 1000L);

		ThreadPoolManager.getInstance().schedule(() -> {
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(aimTarget);
			if (!skill.isNotBroadcastable()) {
				int[] objectIds = Util.objectToIntArray(targets);
				player.broadcastPacket(new MagicSkillLaunchedPacket(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
			}
			player.callSkill(skill, targets, false, false);
		}, skill.getHitTime());
	}

	private static void doMana(Player player, CubicTemplate.SkillInfo info, int delay)
	{
		Skill skill = info.getSkill();
		Creature target = null;
		if(player.getParty() == null)
		{
			if(!player.isCurrentMpFull() && !player.isDead())
				target = player;
		}
		else
		{
			double currentMp = 2.147483647E9;
			for(Player member : player.getParty().getPartyMembers())
			{
				if(member == null)
					continue;
				if(info.getSkill().getCastRange() != -1 && !player.isInRange(member, info.getSkill().getCastRange()) || member.isCurrentMpFull() || member.isDead() || member.getCurrentMp() >= currentMp)
					continue;
				currentMp = member.getCurrentMp();
				target = member;
			}
		}
		if(target == null)
			return;
		int chance = info.getChance((int) target.getCurrentMpPercents());
		if(!Rnd.chance(chance))
			return;
		Creature aimTarget = target;
		if(!skill.isNotBroadcastable())
			player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		player.disableSkill(skill, delay * 1000L);

		ThreadPoolManager.getInstance().schedule(() -> {
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(aimTarget);
			if (!skill.isNotBroadcastable()) {
				int[] objectIds = Util.objectToIntArray(targets);
				player.broadcastPacket(new MagicSkillLaunchedPacket(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
			}
			player.callSkill(skill, targets, false, false);
		}, skill.getHitTime());
	}

	private static void doAttack(Player player, CubicTemplate.SkillInfo info, int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;
		Skill skill = info.getSkill();

		Creature target = getTarget(player, info);
		if(target == null)
			return;

		Creature aimTarget = target;
		if(!skill.isNotBroadcastable())
			player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		player.disableSkill(skill, delay * 1000L);

		ThreadPoolManager.getInstance().schedule(() -> {
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(aimTarget);
			if (!skill.isNotBroadcastable()) {
				int[] objectIds = Util.objectToIntArray(targets);
				player.broadcastPacket(new MagicSkillLaunchedPacket(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
			}
			player.callSkill(skill, targets, false, false);
			if (aimTarget.isNpc())
				if (aimTarget.paralizeOnAttack(player)) {
					if (Config.PARALIZE_ON_RAID_DIFF)
						player.paralizeMe(aimTarget);
				} else {
					int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : (int) skill.getPower();
					aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, skill, damage);
				}
		}, skill.getHitTime());
	}

	private static void doBuff(Player player, CubicTemplate.SkillInfo info, int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;
		Skill skill = info.getSkill();
		if(!skill.isNotBroadcastable())
			player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		player.disableSkill(skill, delay * 1000L);

		ThreadPoolManager.getInstance().schedule(() -> {
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(player);
			if (!skill.isNotBroadcastable()) {
				int[] objectIds = Util.objectToIntArray(targets);
				player.broadcastPacket(new MagicSkillLaunchedPacket(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
			}
			player.callSkill(skill, targets, false, false);
		}, skill.getHitTime());
	}

	private static void doDebuff(Player player, CubicTemplate.SkillInfo info, int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;
		Skill skill = info.getSkill();

		Creature target = getTarget(player, info);
		if(target == null)
			return;

		Creature aimTarget = target;
		if(!skill.isNotBroadcastable())
			player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		player.disableSkill(skill, delay * 1000L);

		ThreadPoolManager.getInstance().schedule(() -> {
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(aimTarget);
			if (!skill.isNotBroadcastable()) {
				int[] objectIds = Util.objectToIntArray(targets);
				player.broadcastPacket(new MagicSkillLaunchedPacket(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
			}
			boolean succ;
			if (succ = Formulas.calcSkillSuccess(player, aimTarget, skill, info.getChance()))
				player.callSkill(skill, targets, false, false);
			if (aimTarget.isNpc())
				if (aimTarget.paralizeOnAttack(player)) {
					if (Config.PARALIZE_ON_RAID_DIFF)
						player.paralizeMe(aimTarget);
				} else {
					int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : (int) skill.getPower();
					aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, skill, damage);
				}
		}, skill.getHitTime());
	}

	private static void doCancel(Player player, CubicTemplate.SkillInfo info, int delay)
	{
		if(!Rnd.chance(info.getChance()))
			return;
		Skill skill = info.getSkill();
		if(!skill.isNotBroadcastable())
			player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		player.disableSkill(skill, delay * 1000L);

		ThreadPoolManager.getInstance().schedule(() -> {
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(player);
			if (!skill.isNotBroadcastable()) {
				int[] objectIds = Util.objectToIntArray(targets);
				player.broadcastPacket(new MagicSkillLaunchedPacket(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
			}
			player.callSkill(skill, targets, false, false);
		}, skill.getHitTime());
	}

	private static final Creature getTarget(Player owner, CubicTemplate.SkillInfo info)
	{
		if(!owner.isInCombat())
			return null;

		GameObject object = owner.getTarget();
		if(object == null || !object.isCreature())
			return null;

		Creature target = (Creature) object;
		if(target.isDead())
			return null;

		if((target.isDoor() && !info.isCanAttackDoor()))
			return null;

		if(info.getSkill().getCastRange() != -1 && !owner.isInRangeZ(target, info.getSkill().getCastRange()))
			return null;

		Player targetPlayer = target.getPlayer();
		if(targetPlayer != null && !targetPlayer.isInCombat())
			return null;

		if(!target.isAutoAttackable(owner))
			return null;

		if(target.isMonster())
		{
			MonsterInstance m = (MonsterInstance) target;
			if(m.getAggroList().getHate(owner) == 0)
				return null;
		}

		return target;
	}
}
