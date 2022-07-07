package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.BaseStatsBonusHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.BaseStatsBonus;

public enum BaseStats
{
	STR
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getSTR();
		}

		@Override
		public final double calcChanceMod(Creature actor)
		{
			return Math.min(2.0 - Math.sqrt(calcBonus(actor)), 1.0);
		}
	},
	INT
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getINT();
		}
	},
	DEX
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getDEX();
		}
	},
	WIT
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getWIT();
		}
	},
	CON
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getCON();
		}
	},
	MEN
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getMEN();
		}
	},
	NONE;

	public static final BaseStats[] VALUES;
	public static final int MAX_STAT_VALUE = 100;

	public int getStat(Creature actor)
	{
		return 1;
	}

	public double calcBonus(Creature actor)
	{
		if(actor == null)
			return 1.0;
		int value = getStat(actor);
		BaseStatsBonus bonus = BaseStatsBonusHolder.getInstance().getBaseStatsBonus(value);
		if(bonus == null || actor.isServitor())
			return 1.0;
		double baseValue = bonus.get(this);
		if(actor.isPlayer())
		{
			Player player = actor.getPlayer();
			if(player.isTransformed() && player.getTransform().getBaseStatBonus(value, this) != 0.0)
				return player.getTransform().getBaseStatBonus(value, this);
		}

		return baseValue;
	}

	public double calcChanceMod(Creature actor)
	{
		return 2.0 - Math.sqrt(calcBonus(actor));
	}

	static
	{
		VALUES = values();
	}
}
