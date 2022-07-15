package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.BaseStatsBonusHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.BaseStatsBonus;

import java.util.NoSuchElementException;

public enum BaseStats
{
	STR(DoubleStat.STAT_STR)
	{
		@Override
		public final int getValue(Creature actor)
		{
			return actor == null ? 1 : actor.getSTR();
		}

		@Override
		public double getMinValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMinSTR();
			}
			return MIN_STAT_VALUE;
		}

		@Override
		public double getMaxValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMaxSTR();
			}
			return MAX_STAT_VALUE;
		}
	},
	INT(DoubleStat.STAT_INT)
	{
		@Override
		public final int getValue(Creature actor)
		{
			return actor == null ? 1 : actor.getINT();
		}

		@Override
		public double getMinValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMinINT();
			}
			return MIN_STAT_VALUE;
		}

		@Override
		public double getMaxValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMaxINT();
			}
			return MAX_STAT_VALUE;
		}
	},
	DEX(DoubleStat.STAT_DEX)
	{
		@Override
		public final int getValue(Creature actor)
		{
			return actor == null ? 1 : actor.getDEX();
		}

		@Override
		public double getMinValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMinDEX();
			}
			return MIN_STAT_VALUE;
		}

		@Override
		public double getMaxValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMaxDEX();
			}
			return MAX_STAT_VALUE;
		}
	},
	WIT(DoubleStat.STAT_WIT)
	{
		@Override
		public final int getValue(Creature actor)
		{
			return actor == null ? 1 : actor.getWIT();
		}

		@Override
		public double getMinValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMinWIT();
			}
			return MIN_STAT_VALUE;
		}

		@Override
		public double getMaxValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMaxWIT();
			}
			return MAX_STAT_VALUE;
		}
	},
	CON(DoubleStat.STAT_CON)
	{
		@Override
		public final int getValue(Creature actor)
		{
			return actor == null ? 1 : actor.getCON();
		}

		@Override
		public double getMinValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMinCON();
			}
			return MIN_STAT_VALUE;
		}

		@Override
		public double getMaxValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMaxCON();
			}
			return MAX_STAT_VALUE;
		}
	},
	MEN(DoubleStat.STAT_MEN)
	{
		@Override
		public final int getValue(Creature actor)
		{
			return actor == null ? 1 : actor.getMEN();
		}

		@Override
		public double getMinValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMinMEN();
			}
			return MIN_STAT_VALUE;
		}

		@Override
		public double getMaxValue(Creature actor) {
			if (actor.isPlayer()) {
				final Player player = actor.getPlayer();
				return player.getTemplate().getMaxMEN();
			}
			return MAX_STAT_VALUE;
		}
	};

	public static final BaseStats[] VALUES = values();

	public static final double MIN_STAT_VALUE = 1.0;
	public static final double MAX_STAT_VALUE = 100.0;

	private final DoubleStat stat;

	BaseStats(DoubleStat stat) {
		this.stat = stat;
	}

	public DoubleStat getStat() {
		return stat;
	}

	public abstract int getValue(Creature actor);
	
	public abstract double getMinValue(Creature actor);
	
	public abstract double getMaxValue(Creature actor);

	public double calcBonus(Creature actor)
	{
		if(actor == null)
			return 1.;

		int value = getValue(actor);

		if(actor.isPlayer())
		{
			Player player = actor.getPlayer();
			if(player.isTransformed() && player.getTransform().getBaseStatBonus(value, this) != 0)
				return player.getTransform().getBaseStatBonus(value, this);
		}

		Double bonus = BaseStatsBonusHolder.getInstance().getBaseStatsBonus(this, value);
		if(bonus == null)
			return 1.;	// TODO: [Bonux] Проверить на оффе.

		return bonus;
	}

	public static BaseStats valueOf(DoubleStat stat)
	{
		for (BaseStats baseStat : values())
		{
			if (baseStat.getStat() == stat)
			{
				return baseStat;
			}
		}
		throw new NoSuchElementException("Unknown base stat '" + stat + "' for enum BaseStats");
	}
}