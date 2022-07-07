package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.StatsSet;

import java.util.List;

public class Charge extends Skill
{
	public static final int MAX_CHARGE = 10;
	private int _charges;
	private final boolean _fullCharge;

	public Charge(StatsSet set)
	{
		super(set);
		_charges = set.getInteger("charges", getLevel());
		_fullCharge = set.getBool("fullCharge", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(!activeChar.isPlayer())
			return false;
		Player player = (Player) activeChar;
		if(_charges == 10 && player.getSkillLevel(10301) != -1)
			_charges = 15;
		if(getPower() <= 0.0 && getId() != 2165 && player.getIncreasedForce() >= _charges)
		{
			activeChar.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return false;
		}
		if(getId() == 2165)
			player.sendPacket(new MagicSkillUse(player, player, 2165, 1, 0, 0L));
		return true;
	}

	@Override
	public void onEndCast(Creature activeChar, List<Creature> targets)
	{
		super.onEndCast(activeChar, targets);
		if(activeChar.isPlayer())
			chargePlayer((Player) activeChar, getId());
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!activeChar.isPlayer())
			return;
		if(target.isDead())
			return;
		if(target == activeChar)
			return;
		if(getPower() <= 0.0)
			return;
		Creature realTarget = reflected ? activeChar : target;
		Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, isSSPossible(), false);
		if(info == null)
			return;
		realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true, true, info.crit || info.blow, false, false, false);
		if(!info.miss || info.damage >= 1.0)
		{
			double lethalDmg = Formulas.calcLethalDamage(activeChar, realTarget, this);
			if(lethalDmg > 0.0)
				realTarget.reduceCurrentHp(lethalDmg, activeChar, this, true, true, false, false, false, false, false);
			else if(!reflected)
				realTarget.doCounterAttack(this, activeChar, false);
		}
	}

	public void chargePlayer(Player player, Integer skillId)
	{
		if(player.getIncreasedForce() >= _charges)
		{
			player.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return;
		}
		if(_fullCharge)
			player.setIncreasedForce(_charges);
		else
			player.setIncreasedForce(player.getIncreasedForce() + 1);
	}
}
