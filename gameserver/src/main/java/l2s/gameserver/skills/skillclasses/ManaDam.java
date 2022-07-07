package l2s.gameserver.skills.skillclasses;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.StatsSet;

public class ManaDam extends Skill
{
	public ManaDam(StatsSet set)
	{
		super(set);
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(target.isDead())
			return;
		int magicLevel = getMagicLevel() == 0 ? activeChar.getLevel() : getMagicLevel();
		int landRate = Rnd.get(30, 100) * target.getLevel() / magicLevel;
		if(Rnd.chance(landRate))
		{
			double mAtk = activeChar.getMAtk(target, this);
			if(isSSPossible())
				mAtk *= (100.0 + activeChar.getChargedSpiritshotPower()) / 100.0;
			double mDef = Math.max(1.0, target.getMDef(activeChar, this));
			if(mDef < 1.0)
				mDef = 1.0;
			double damage = Math.sqrt(mAtk) * getPower() * (target.getMaxMp() / 97) / mDef;
			if(Formulas.calcMCrit(activeChar, target, this))
			{
				activeChar.sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
				damage *= 2.0;
				damage *= activeChar.getMagicCriticalDmg(target, this);
			}
			target.reduceCurrentMp(damage, activeChar);
		}
		else
		{
			ExMagicAttackInfo.packet(activeChar, target, MagicAttackType.RESISTED);

			SystemMessagePacket msg = new SystemMessagePacket(SystemMsg.C1_RESISTED_C2S_MAGIC).addName(target).addName(activeChar);
			activeChar.sendPacket(msg);
			target.sendPacket(msg);
			target.reduceCurrentHp(1.0, activeChar, this, true, true, false, true, false, false, true);
		}
	}
}
