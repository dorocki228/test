package l2s.gameserver.model.base;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Stats;

public enum SkillTrait
{
	NONE,
	BLEED
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.BLEED_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.BLEED_POWER, target, skill);
		}
	},
	BOSS,
	DEATH,
	DERANGEMENT
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.MENTAL_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return Math.min(100.0, creature.calcStat(Stats.MENTAL_POWER, target, skill));
		}
	},
	ETC,
	GUST,
	HOLD
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.ROOT_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.ROOT_POWER, target, skill);
		}
	},
	PARALYZE
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.PARALYZE_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.PARALYZE_POWER, target, skill);
		}
	},
	PHYSICAL_BLOCKADE,
	AIRJOKE
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.AIRJOKE_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.AIRJOKE_POWER, target, skill);
		}
	},
	MUTATE
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.MUTATE_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.MUTATE_POWER, target, skill);
		}
	},
	DISARM
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.DISARM_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.DISARM_POWER, target, skill);
		}
	},
	PULL
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.PULL_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.PULL_POWER, target, skill);
		}
	},
	POISON
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.POISON_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.POISON_POWER, target, skill);
		}
	},
	SHOCK
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.STUN_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return Math.min(40.0, creature.calcStat(Stats.STUN_POWER, target, skill));
		}
	},
	SLEEP
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.SLEEP_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.SLEEP_POWER, target, skill);
		}
	},
	VALAKAS,
	KNOCKBACK
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.KNOCKBACK_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.KNOCKBACK_POWER, target, skill);
		}
	},
	KNOCKDOWN
	{
		@Override
		public final double calcVuln(Creature creature, Creature target, Skill skill)
		{
			return target.calcStat(Stats.KNOCKDOWN_RESIST, creature, skill);
		}

		@Override
		public final double calcProf(Creature creature, Creature target, Skill skill)
		{
			return creature.calcStat(Stats.KNOCKDOWN_POWER, target, skill);
		}
	};

	public double calcVuln(Creature creature, Creature target, Skill skill)
	{
		return 0.0;
	}

	public double calcProf(Creature creature, Creature target, Skill skill)
	{
		return 0.0;
	}
}
