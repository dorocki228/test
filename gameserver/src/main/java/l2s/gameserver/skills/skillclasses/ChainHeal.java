package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChainHeal extends Skill
{
	private final int[] _healPercents;
	private final int _healRadius;
	private final int _maxTargets;

	public ChainHeal(StatsSet set)
	{
		super(set);
		_healRadius = set.getInteger("healRadius", 350);
		String[] params = set.getString("healPercents", "").split(";");
		_maxTargets = params.length;
		_healPercents = new int[params.length];
		for(int i = 0; i < params.length; ++i)
			_healPercents[i] = Integer.parseInt(params[i]);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(!checkMainTarget(activeChar, target))
		{
			activeChar.getPlayer().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		return true;
	}

	@Override
	public void onEndCast(Creature activeChar, List<Creature> targets)
	{
		super.onEndCast(activeChar, targets);
		int curTarget = 0;
		for(Creature target : targets)
			if(target != null)
			{
				if(target.isHealBlocked())
					continue;
				double hp = _healPercents[curTarget] * target.getMaxHp() / 100.0;
				double addToHp = Math.max(0.0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0 - target.getCurrentHp()));
				if(addToHp > 0.0)
					target.setCurrentHp(addToHp + target.getCurrentHp(), false);
				if(target.isPlayer())
					if(activeChar != target)
						target.sendPacket(new SystemMessage(1067).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
					else
						activeChar.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
				++curTarget;
			}
	}

	private boolean checkMainTarget(Creature activeChar, Creature target)
	{
		if(target == null)
			return false;
		if(activeChar == target)
			return true;
		if(target.isDoor() || target.isMonster() || activeChar.isAutoAttackable(target))
			return false;
		if(target.isPlayer())
		{
			Player activeCharTarget = target.getPlayer();
			Player activeCharPlayer = activeChar.getPlayer();
			if(activeCharTarget.isInDuel() && activeCharPlayer.getObjectId() != activeCharTarget.getObjectId() || activeCharPlayer != null && !isTargetFriendly(activeCharPlayer, activeCharTarget))
				return false;
		}
		return true;
	}

	private boolean isTargetFriendly(Player activeCharPlayer, Player activeCharTarget)
	{
		return true;
	}

	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
        List<Creature> targets = aimingTarget.getAroundCharacters(_healRadius, 128);
		List<HealTarget> healTargets = new ArrayList<>();
		healTargets.add(new HealTarget(-100.0, aimingTarget));
		Player activeCharPlayer = null;
		if(activeChar.isPlayer())
			activeCharPlayer = activeChar.getPlayer();
		if(targets != null && !targets.isEmpty() && (activeCharPlayer != null && !activeCharPlayer.isInDuel() || activeCharPlayer == null))
			for(Creature target : targets)
			{
				if(target == null)
					continue;
				if(target.getObjectId() == activeChar.getObjectId())
				{
					if(activeChar.getObjectId() != aimingTarget.getObjectId())
						continue;
				}
				else
				{
					if(target.isDoor() || target.isMonster() || target.isAutoAttackable(activeChar))
						continue;
					if(target.isAlikeDead())
						continue;
				}
				if(target.isSummon() || target.isPet())
				{
					Player owner = target.getPlayer();
					if(owner != null && activeCharPlayer != null && !isTargetFriendly(activeCharPlayer, owner) && owner.getObjectId() != activeCharPlayer.getObjectId())
						continue;
				}
				else if(target.isPlayer())
				{
					Player activeCharTarget = target.getPlayer();
					if(activeCharTarget.isInDuel() || activeCharPlayer != null && !isTargetFriendly(activeCharPlayer, activeCharTarget))
						continue;
				}
				double hpPercent = target.getCurrentHp() / target.getMaxHp();
				healTargets.add(new HealTarget(hpPercent, target));
			}
		HealTarget[] healTargetsArr = new HealTarget[healTargets.size()];
		healTargets.toArray(healTargetsArr);
		Arrays.sort(healTargetsArr, (o1, o2) -> {
			if(o1 == null || o2 == null)
				return 0;
			else
				return Double.compare(o1.getHpPercent(), o2.getHpPercent());
		});
		int targetsCount = 0;
        List<Creature> result = new ArrayList<>();
        for(HealTarget ht : healTargetsArr)
		{
			result.add(ht.getTarget());
			if(++targetsCount >= _maxTargets)
				break;
		}
		return result;
	}

	private static class HealTarget
	{
		private final double hpPercent;
		private final Creature target;

		public HealTarget(double hpPercent, Creature target)
		{
			this.hpPercent = hpPercent;
			this.target = target;
		}

		public double getHpPercent()
		{
			return hpPercent;
		}

		public Creature getTarget()
		{
			return target;
		}
	}
}
