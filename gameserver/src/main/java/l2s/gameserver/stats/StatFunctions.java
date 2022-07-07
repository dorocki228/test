package l2s.gameserver.stats;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Mount;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.conditions.ConditionPlayerState;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public class StatFunctions
{
	public static void addPredefinedFuncs(Creature cha)
	{
		if(cha.isDoor())
			return;
		if(cha.isPlayer())
		{
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncHennaSTR.func);
			cha.addStatFunc(FuncHennaDEX.func);
			cha.addStatFunc(FuncHennaINT.func);
			cha.addStatFunc(FuncHennaMEN.func);
			cha.addStatFunc(FuncHennaCON.func);
			cha.addStatFunc(FuncHennaWIT.func);
			cha.addStatFunc(FuncInventory.func);
			cha.addStatFunc(FuncWarehouse.func);
			cha.addStatFunc(FuncTradeLimit.func);
			cha.addStatFunc(FuncSDefPlayers.func);
			cha.addStatFunc(FuncMaxHpLimit.func);
			cha.addStatFunc(FuncMaxMpLimit.func);
			cha.addStatFunc(FuncMaxCpLimit.func);
			cha.addStatFunc(FuncRunSpdLimit.func);
			cha.addStatFunc(FuncRunSpdLimit.func);
			cha.addStatFunc(FuncPDefLimit.func);
			cha.addStatFunc(FuncMDefLimit.func);
			cha.addStatFunc(FuncPAtkLimit.func);
			cha.addStatFunc(FuncMAtkLimit.func);
			cha.addStatFunc(FuncMaxLoadMul.func);
			cha.addStatFunc(FuncBreathMul.func);
			cha.addStatFunc(FuncCpRegenMul.func);
			cha.addStatFunc(FuncPAtkSpeedPenalty.func);
			cha.addStatFunc(FuncMaxCpMul.func);
			cha.addStatFunc(FuncRunSpeedMul.func);
		}
		cha.addStatFunc(FuncAttackRange.func);
		cha.addStatFunc(FuncMaxHpMul.func);
		cha.addStatFunc(FuncMaxMpMul.func);
		cha.addStatFunc(FuncHpRegenMul.func);
		cha.addStatFunc(FuncMpRegenMul.func);
		if(cha.isPet())
		{
			cha.addStatFunc(FuncMpRegenPenalty.func);
			cha.addStatFunc(FuncHpRegenPenalty.func);
		}
		if(cha.isPlayer() || cha.isPet())
			cha.addStatFunc(FuncMoveSpeedPenalty.func);
		cha.addStatFunc(FuncPAtkMul.func);
		cha.addStatFunc(FuncMAtkMul.func);
		cha.addStatFunc(FuncPDefMul.func);
		cha.addStatFunc(FuncMDefMul.func);
		if(cha.isSummon())
		{
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.FIRE));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.WATER));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.EARTH));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.WIND));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.HOLY));
			cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.UNHOLY));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.FIRE));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.WATER));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.EARTH));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.WIND));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.HOLY));
			cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.UNHOLY));
		}
		cha.addStatFunc(FuncPCritDamMul.func);
		cha.addStatFunc(FuncMCritDamMul.func);
		cha.addStatFunc(FuncPAccuracyAdd.func);
		cha.addStatFunc(FuncMAccuracyAdd.func);
		cha.addStatFunc(FuncPEvasionAdd.func);
		cha.addStatFunc(FuncMEvasionAdd.func);
		cha.addStatFunc(FuncPAtkSpeedMul.func);
		cha.addStatFunc(FuncMAtkSpeedMul.func);
		cha.addStatFunc(FuncSDefInit.func);
		cha.addStatFunc(FuncSDefAll.func);
		cha.addStatFunc(FuncPAtkSpdLimit.func);
		cha.addStatFunc(FuncMAtkSpdLimit.func);
		cha.addStatFunc(FuncCAtkLimit.func);
		cha.addStatFunc(FuncPEvasionLimit.func);
		cha.addStatFunc(FuncMEvasionLimit.func);
		cha.addStatFunc(FuncPAccuracyLimit.func);
		cha.addStatFunc(FuncMAccuracyLimit.func);
		cha.addStatFunc(FuncPCritLimit.func);
		cha.addStatFunc(FuncMCritLimit.func);
		cha.addStatFunc(FuncMCriticalRateMul.func);
		cha.addStatFunc(FuncPCriticalRateMul.func);
		cha.addStatFunc(FuncPDamageResists.func);
		cha.addStatFunc(FuncMDamageResists.func);
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.FIRE));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WATER));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.EARTH));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WIND));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.HOLY));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.UNHOLY));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.FIRE));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WATER));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.EARTH));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WIND));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.HOLY));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.UNHOLY));
	}

	private static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] func;

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenResting(stat);
			return func[pos];
		}

		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, true));
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.isPlayer() && creature.getLevel() <= 40 && ((Player) creature).getClassLevel().ordinal() < ClassLevel.SECOND.ordinal() && stat == Stats.REGENERATE_HP_RATE)
				return value * 6.0;
			else
				return value * 1.5;
		}

		static
		{
			func = new FuncMultRegenResting[Stats.NUM_STATS];
		}
	}

	private static class FuncMultRegenStanding extends Func
	{
		static final FuncMultRegenStanding[] func;

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenStanding(stat);
			return func[pos];
		}

		private FuncMultRegenStanding(Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, true));
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * 1.1;
		}

		static
		{
			func = new FuncMultRegenStanding[Stats.NUM_STATS];
		}
	}

	private static class FuncMultRegenRunning extends Func
	{
		static final FuncMultRegenRunning[] func;

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenRunning(stat);
			return func[pos];
		}

		private FuncMultRegenRunning(Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, true));
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * 0.7;
		}

		static
		{
			func = new FuncMultRegenRunning[Stats.NUM_STATS];
		}
	}

	private static class FuncPAtkMul extends Func
	{
		static final FuncPAtkMul func;

		private FuncPAtkMul()
		{
			super(Stats.POWER_ATTACK, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.STR.calcBonus(creature) * creature.getLevelBonus();
		}

		static
		{
			func = new FuncPAtkMul();
		}
	}

	private static class FuncMAtkMul extends Func
	{
		static final FuncMAtkMul func;

		private FuncMAtkMul()
		{
			super(Stats.MAGIC_ATTACK, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			double ib = BaseStats.INT.calcBonus(creature);
			double lvlb = creature.getLevelBonus();
			return value * lvlb * lvlb * ib * ib;
		}

		static
		{
			func = new FuncMAtkMul();
		}
	}

	private static class FuncPDefMul extends Func
	{
		static final FuncPDefMul func;

		private FuncPDefMul()
		{
			super(Stats.POWER_DEFENCE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * creature.getLevelBonus();
		}

		static
		{
			func = new FuncPDefMul();
		}
	}

	private static class FuncMDefMul extends Func
	{
		static final FuncMDefMul func;

		private FuncMDefMul()
		{
			super(Stats.MAGIC_DEFENCE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.MEN.calcBonus(creature) * creature.getLevelBonus();
		}

		static
		{
			func = new FuncMDefMul();
		}
	}

	private static class FuncPCritDamMul extends Func
	{
		static final FuncPCritDamMul func;

		private FuncPCritDamMul()
		{
			super(Stats.P_CRITICAL_DAMAGE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.STR.calcBonus(creature);
		}

		static
		{
			func = new FuncPCritDamMul();
		}
	}

	private static class FuncMCritDamMul extends Func
	{
		static final FuncMCritDamMul func;

		private FuncMCritDamMul()
		{
			super(Stats.M_CRITICAL_DAMAGE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value;
		}

		static
		{
			func = new FuncMCritDamMul();
		}
	}

	private static class FuncAttackRange extends Func
	{
		static final FuncAttackRange func;

		private FuncAttackRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.isPlayer() && creature.isTransformed())
			{
				WeaponType attackType = creature.getTransform().getBaseAttackType();
				if(attackType != WeaponType.NONE)
				{
					return value + WeaponTemplate.getDefaultAttackRange(attackType);
				}
			}
			WeaponTemplate weapon = creature.getActiveWeaponTemplate();

			if(weapon != null)
				return value + weapon.getAttackRange();
			else
				return value + creature.getBaseStats().getAtkRange();
		}

		static
		{
			func = new FuncAttackRange();
		}
	}

	private static class FuncPAccuracyAdd extends Func
	{
		static final FuncPAccuracyAdd func;

		private FuncPAccuracyAdd()
		{
			super(Stats.P_ACCURACY_COMBAT, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			value += Math.sqrt(creature.getDEX()) * 5.0 + creature.getLevel();
			if(creature.isServitor())
				value += creature.getLevel() < 60 ? 4.0 : 5.0;
			int level = creature.getLevel();
			if(level > 69)
				value += level - 69.0;
			if(level > 77)
				++value;
			if(level > 80)
				value += 2.0;
			if(level > 87)
				++value;
			if(level > 92)
				++value;
			if(level > 97)
				++value;
			return value;
		}

		static
		{
			func = new FuncPAccuracyAdd();
		}
	}

	private static class FuncMAccuracyAdd extends Func
	{
		static final FuncMAccuracyAdd func;

		private FuncMAccuracyAdd()
		{
			super(Stats.M_ACCURACY_COMBAT, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value + Math.sqrt(creature.getWIT()) * 3.0 + creature.getLevel() * 2;
		}

		static
		{
			func = new FuncMAccuracyAdd();
		}
	}

	private static class FuncPEvasionAdd extends Func
	{
		static final FuncPEvasionAdd func;

		private FuncPEvasionAdd()
		{
			super(Stats.P_EVASION_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			value += Math.sqrt(creature.getDEX()) * 5.0 + creature.getLevel();
			int level = creature.getLevel();
			if(level > 69)
				value += level - 69.0;
			if(level > 77)
				++value;
			if(level > 80)
				value += 2.0;
			if(level > 87)
				++value;
			if(level > 92)
				++value;
			if(level > 97)
				++value;
			return value;
		}

		static
		{
			func = new FuncPEvasionAdd();
		}
	}

	private static class FuncMEvasionAdd extends Func
	{
		static final FuncMEvasionAdd func;

		private FuncMEvasionAdd()
		{
			super(Stats.M_EVASION_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value + Math.sqrt(creature.getWIT()) * 3.0 + creature.getLevel() * 2;
		}

		static
		{
			func = new FuncMEvasionAdd();
		}
	}

	private static class FuncPCriticalRateMul extends Func
	{
		static final FuncPCriticalRateMul func;

		private FuncPCriticalRateMul()
		{
			super(Stats.BASE_P_CRITICAL_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			value *= BaseStats.DEX.calcBonus(creature);
			return value * 0.01 * creature.calcStat(Stats.P_CRITICAL_RATE, target, skill);
		}

		static
		{
			func = new FuncPCriticalRateMul();
		}
	}

	private static class FuncMCriticalRateMul extends Func
	{
		static final FuncMCriticalRateMul func;

		private FuncMCriticalRateMul()
		{
			super(Stats.BASE_M_CRITICAL_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			value *= BaseStats.WIT.calcBonus(creature);
			return value * 0.01 * creature.calcStat(Stats.M_CRITICAL_RATE, target, skill);
		}

		static
		{
			func = new FuncMCriticalRateMul();
		}
	}

	private static class FuncPAtkSpeedMul extends Func
	{
		static final FuncPAtkSpeedMul func;

		private FuncPAtkSpeedMul()
		{
			super(Stats.POWER_ATTACK_SPEED, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.DEX.calcBonus(creature);
		}

		static
		{
			func = new FuncPAtkSpeedMul();
		}
	}

	private static class FuncMAtkSpeedMul extends Func
	{
		static final FuncMAtkSpeedMul func;

		private FuncMAtkSpeedMul()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.WIT.calcBonus(creature);
		}

		static
		{
			func = new FuncMAtkSpeedMul();
		}
	}

	private static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR func;

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player pc = (Player) creature;
			if(pc != null)
				return Math.max(1.0, value + pc.getHennaList().getSTR());
			return value;
		}

		static
		{
			func = new FuncHennaSTR();
		}
	}

	private static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX func;

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player pc = (Player) creature;
			if(pc != null)
				return Math.max(1.0, value + pc.getHennaList().getDEX());
			return value;
		}

		static
		{
			func = new FuncHennaDEX();
		}
	}

	private static class FuncHennaINT extends Func
	{
		static final FuncHennaINT func;

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player pc = (Player) creature;
			if(pc != null)
				return Math.max(1.0, value + pc.getHennaList().getINT());
			return value;
		}

		static
		{
			func = new FuncHennaINT();
		}
	}

	private static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN func;

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player pc = (Player) creature;
			if(pc != null)
				return Math.max(1.0, value + pc.getHennaList().getMEN());
			return value;
		}

		static
		{
			func = new FuncHennaMEN();
		}
	}

	private static class FuncHennaCON extends Func
	{
		static final FuncHennaCON func;

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player pc = (Player) creature;
			if(pc != null)
				return Math.max(1.0, value + pc.getHennaList().getCON());
			return value;
		}

		static
		{
			func = new FuncHennaCON();
		}
	}

	private static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT func;

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player pc = (Player) creature;
			if(pc != null)
				return Math.max(1.0, value + pc.getHennaList().getWIT());
			return value;
		}

		static
		{
			func = new FuncHennaWIT();
		}
	}

	private static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul func;

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.CON.calcBonus(creature);
		}

		static
		{
			func = new FuncMaxHpMul();
		}
	}

	private static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul func;

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.CON.calcBonus(creature);
		}

		static
		{
			func = new FuncMaxCpMul();
		}
	}

	private static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul func;

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.MEN.calcBonus(creature);
		}

		static
		{
			func = new FuncMaxMpMul();
		}
	}

	private static class FuncPDamageResists extends Func
	{
		static final FuncPDamageResists func;

		private FuncPDamageResists()
		{
			super(Stats.INFLICTS_P_DAMAGE_POWER, 48, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(target.isRaid() && creature.getLevel() - target.getLevel() > Config.RAID_MAX_LEVEL_DIFF)
			{
				return 1.0;
			}
			value *= 0.01 * target.calcStat(creature.getBaseStats().getAttackType().getDefence(), creature, skill);
			return Formulas.calcDamageResists(skill, creature, target, value);
		}

		static
		{
			func = new FuncPDamageResists();
		}
	}

	private static class FuncMDamageResists extends Func
	{
		static final FuncMDamageResists func;

		private FuncMDamageResists()
		{
			super(Stats.INFLICTS_M_DAMAGE_POWER, 48, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(target.isRaid() && Math.abs(creature.getLevel() - target.getLevel()) > Config.RAID_MAX_LEVEL_DIFF)
			{
				return 1.0;
			}
			return Formulas.calcDamageResists(skill, creature, target, value);
		}

		static
		{
			func = new FuncMDamageResists();
		}
	}

	private static class FuncInventory extends Func
	{
		static final FuncInventory func;

		private FuncInventory()
		{
			super(Stats.INVENTORY_LIMIT, 1, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player player = (Player) creature;
			if(player.isGM())
				value = Config.INVENTORY_MAXIMUM_GM;
			else if(player.getRace() == Race.DWARF)
				value = Config.INVENTORY_MAXIMUM_DWARF;
			else
				value = Config.INVENTORY_MAXIMUM_NO_DWARF;
			value += player.getExpandInventory();
			return Math.min(value, Config.SERVICES_EXPAND_INVENTORY_MAX);
		}

		static
		{
			func = new FuncInventory();
		}
	}

	private static class FuncWarehouse extends Func
	{
		static final FuncWarehouse func;

		private FuncWarehouse()
		{
			super(Stats.STORAGE_LIMIT, 1, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player player = (Player) creature;
			if(player.getRace() == Race.DWARF)
				value = Config.WAREHOUSE_SLOTS_DWARF;
			else
				value = Config.WAREHOUSE_SLOTS_NO_DWARF;
			return value + player.getExpandWarehouse();
		}

		static
		{
			func = new FuncWarehouse();
		}
	}

	private static class FuncTradeLimit extends Func
	{
		static final FuncTradeLimit func;

		private FuncTradeLimit()
		{
			super(Stats.TRADE_LIMIT, 1, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			Player _cha = (Player) creature;
			if(_cha.getRace() == Race.DWARF)
				value = Config.MAX_PVTSTORE_SLOTS_DWARF;
			else
				value = Config.MAX_PVTSTORE_SLOTS_OTHER;
			return value;
		}

		static
		{
			func = new FuncTradeLimit();
		}
	}

	private static class FuncSDefInit extends Func
	{
		static final Func func;

		private FuncSDefInit()
		{
			super(Stats.SHIELD_RATE, 1, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return creature.getBaseStats().getShldRate();
		}

		static
		{
			func = new FuncSDefInit();
		}
	}

	private static class FuncSDefAll extends Func
	{
		static final FuncSDefAll func;

		private FuncSDefAll()
		{
			super(Stats.SHIELD_RATE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(value == 0.0)
				return 0;

			if(target != null)
				switch(target.getBaseStats().getAttackType())
				{
					case BOW:
					{
						value += 30.0;
						break;
					}
					case DAGGER:
					case DUALDAGGER:
					{
						value += 12.0;
						break;
					}
				}
			return value;
		}

		static
		{
			func = new FuncSDefAll();
		}
	}

	private static class FuncSDefPlayers extends Func
	{
		static final FuncSDefPlayers func;

		private FuncSDefPlayers()
		{
			super(Stats.SHIELD_RATE, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(value == 0.0)
				return 0;

			ItemInstance shld = ((Player) creature).getInventory().getPaperdollItem(8);
			if(shld == null || shld.getItemType() != WeaponTemplate.WeaponType.NONE)
				return 0;
			return value * BaseStats.CON.calcBonus(creature);
		}

		static
		{
			func = new FuncSDefPlayers();
		}
	}

	private static class FuncRunSpeedMul extends Func
	{
		static final FuncRunSpeedMul func;

		private FuncRunSpeedMul()
		{
			super(Stats.RUN_SPEED, 32, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.calcStat(Stats.SPEED_ON_DEX_DEPENDENCE) > 0.0)
				return value * BaseStats.DEX.calcBonus(creature);
			return value;
		}

		static
		{
			func = new FuncRunSpeedMul();
		}
	}

	private static class FuncMaxHpLimit extends Func
	{
		static final Func func;

		private FuncMaxHpLimit()
		{
			super(Stats.MAX_HP, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(Config.HP_LIMIT > 0)
				return Math.min(Config.HP_LIMIT, value);
			return value;
		}

		static
		{
			func = new FuncMaxHpLimit();
		}
	}

	private static class FuncMaxMpLimit extends Func
	{
		static final Func func;

		private FuncMaxMpLimit()
		{
			super(Stats.MAX_MP, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(Config.MP_LIMIT > 0)
				return Math.min(Config.MP_LIMIT, value);
			return value;
		}

		static
		{
			func = new FuncMaxMpLimit();
		}
	}

	private static class FuncMaxCpLimit extends Func
	{
		static final Func func;

		private FuncMaxCpLimit()
		{
			super(Stats.MAX_CP, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(Config.CP_LIMIT > 0)
				return Math.min(Config.CP_LIMIT, value);
			return value;
		}

		static
		{
			func = new FuncMaxCpLimit();
		}
	}

	private static class FuncRunSpdLimit extends Func
	{
		static final Func func;

		private FuncRunSpdLimit()
		{
			super(Stats.RUN_SPEED, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_MOVE, value);
		}

		static
		{
			func = new FuncRunSpdLimit();
		}
	}

	private static class FuncPDefLimit extends Func
	{
		static final Func func;

		private FuncPDefLimit()
		{
			super(Stats.POWER_DEFENCE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_PDEF, value);
		}

		static
		{
			func = new FuncPDefLimit();
		}
	}

	private static class FuncMDefLimit extends Func
	{
		static final Func func;

		private FuncMDefLimit()
		{
			super(Stats.MAGIC_DEFENCE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_MDEF, value);
		}

		static
		{
			func = new FuncMDefLimit();
		}
	}

	private static class FuncPAtkLimit extends Func
	{
		static final Func func;

		private FuncPAtkLimit()
		{
			super(Stats.POWER_ATTACK, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_PATK, value);
		}

		static
		{
			func = new FuncPAtkLimit();
		}
	}

	private static class FuncMAtkLimit extends Func
	{
		static final Func func;

		private FuncMAtkLimit()
		{
			super(Stats.MAGIC_ATTACK, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_MATK, value);
		}

		static
		{
			func = new FuncMAtkLimit();
		}
	}

	private static class FuncPAtkSpdLimit extends Func
	{
		static final Func func;

		private FuncPAtkSpdLimit()
		{
			super(Stats.POWER_ATTACK_SPEED, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_PATK_SPD, value);
		}

		static
		{
			func = new FuncPAtkSpdLimit();
		}
	}

	private static class FuncMAtkSpdLimit extends Func
	{
		static final Func func;

		private FuncMAtkSpdLimit()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_MATK_SPD, value);
		}

		static
		{
			func = new FuncMAtkSpdLimit();
		}
	}

	private static class FuncCAtkLimit extends Func
	{
		static final Func func;

		private FuncCAtkLimit()
		{
			super(Stats.P_CRITICAL_DAMAGE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_CRIT_DAM / 2.0, value);
		}

		static
		{
			func = new FuncCAtkLimit();
		}
	}

	private static class FuncPEvasionLimit extends Func
	{
		static final Func func;

		private FuncPEvasionLimit()
		{
			super(Stats.P_EVASION_RATE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_EVASION, value);
		}

		static
		{
			func = new FuncPEvasionLimit();
		}
	}

	private static class FuncMEvasionLimit extends Func
	{
		static final Func func;

		private FuncMEvasionLimit()
		{
			super(Stats.M_EVASION_RATE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_EVASION, value);
		}

		static
		{
			func = new FuncMEvasionLimit();
		}
	}

	private static class FuncPAccuracyLimit extends Func
	{
		static final Func func;

		private FuncPAccuracyLimit()
		{
			super(Stats.P_ACCURACY_COMBAT, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_ACCURACY, value);
		}

		static
		{
			func = new FuncPAccuracyLimit();
		}
	}

	private static class FuncMAccuracyLimit extends Func
	{
		static final Func func;

		private FuncMAccuracyLimit()
		{
			super(Stats.M_ACCURACY_COMBAT, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_ACCURACY, value);
		}

		static
		{
			func = new FuncMAccuracyLimit();
		}
	}

	private static class FuncPCritLimit extends Func
	{
		static final Func func;

		private FuncPCritLimit()
		{
			super(Stats.BASE_P_CRITICAL_RATE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(creature.calcStat(Stats.P_CRIT_RATE_LIMIT, Config.LIM_CRIT), value);
		}

		static
		{
			func = new FuncPCritLimit();
		}
	}

	private static class FuncMCritLimit extends Func
	{
		static final Func func;

		private FuncMCritLimit()
		{
			super(Stats.BASE_M_CRITICAL_RATE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return Math.min(Config.LIM_MCRIT, value);
		}

		static
		{
			func = new FuncMCritLimit();
		}
	}

	private static class FuncAttributeAttackInit extends Func
	{
		static final Func[] func;
		private final Element element;

		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private FuncAttributeAttackInit(Element element)
		{
			super(element.getAttack(), 1, null);
			this.element = element;
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value + creature.getBaseStats().getAttributeAttack()[element.getId()];
		}

		static
		{
			func = new FuncAttributeAttackInit[Element.VALUES.length];
			for(int i = 0; i < Element.VALUES.length; ++i)
				func[i] = new FuncAttributeAttackInit(Element.VALUES[i]);
		}
	}

	private static class FuncAttributeDefenceInit extends Func
	{
		static final Func[] func;
		private final Element element;

		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private FuncAttributeDefenceInit(Element element)
		{
			super(element.getDefence(), 1, null);
			this.element = element;
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value + creature.getBaseStats().getAttributeDefence()[element.getId()];
		}

		static
		{
			func = new FuncAttributeDefenceInit[Element.VALUES.length];
			for(int i = 0; i < Element.VALUES.length; ++i)
				func[i] = new FuncAttributeDefenceInit(Element.VALUES[i]);
		}
	}

	private static class FuncAttributeAttackSet extends Func
	{
		static final Func[] func;

		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private FuncAttributeAttackSet(Stats stat)
		{
			super(stat, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.getPlayer().getClassId().getType2() == ClassType2.SUMMONER)
				return creature.getPlayer().calcStat(stat, 0.0);
			return value;
		}

		static
		{
			func = new FuncAttributeAttackSet[Element.VALUES.length];
			for(int i = 0; i < Element.VALUES.length; ++i)
				func[i] = new FuncAttributeAttackSet(Element.VALUES[i].getAttack());
		}
	}

	private static class FuncAttributeDefenceSet extends Func
	{
		static final Func[] func;

		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}

		private FuncAttributeDefenceSet(Stats stat)
		{
			super(stat, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.getPlayer().getClassId().getType2() == ClassType2.SUMMONER)
				return creature.getPlayer().calcStat(stat, 0.0);
			return value;
		}

		static
		{
			func = new FuncAttributeDefenceSet[Element.VALUES.length];
			for(int i = 0; i < Element.VALUES.length; ++i)
				func[i] = new FuncAttributeDefenceSet(Element.VALUES[i].getDefence());
		}
	}

	private static class FuncMaxLoadMul extends Func
	{
		static final FuncMaxLoadMul func;

		private FuncMaxLoadMul()
		{
			super(Stats.MAX_LOAD, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.CON.calcBonus(creature) * Config.MAXLOAD_MODIFIER;
		}

		static
		{
			func = new FuncMaxLoadMul();
		}
	}

	private static class FuncBreathMul extends Func
	{
		static final FuncBreathMul func;

		private FuncBreathMul()
		{
			super(Stats.BREATH, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.CON.calcBonus(creature);
		}

		static
		{
			func = new FuncBreathMul();
		}
	}

	private static class FuncHpRegenMul extends Func
	{
		static final FuncHpRegenMul func;

		private FuncHpRegenMul()
		{
			super(Stats.REGENERATE_HP_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			value *= BaseStats.CON.calcBonus(creature);
			if(creature.isSummon())
				value *= 2.0;
			return value;
		}

		static
		{
			func = new FuncHpRegenMul();
		}
	}

	private static class FuncMpRegenMul extends Func
	{
		static final FuncMpRegenMul func;

		private FuncMpRegenMul()
		{
			super(Stats.REGENERATE_MP_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.MEN.calcBonus(creature);
		}

		static
		{
			func = new FuncMpRegenMul();
		}
	}

	private static class FuncCpRegenMul extends Func
	{
		static final FuncCpRegenMul func;

		private FuncCpRegenMul()
		{
			super(Stats.REGENERATE_CP_RATE, 16, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			return value * BaseStats.CON.calcBonus(creature);
		}

		static
		{
			func = new FuncCpRegenMul();
		}
	}

	private static class FuncHpRegenPenalty extends Func
	{
		static final FuncHpRegenPenalty func;

		private FuncHpRegenPenalty()
		{
			super(Stats.REGENERATE_HP_RATE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.isPet())
			{
				PetInstance pet = (PetInstance) creature;
				if(pet.getWeightPenalty() == 1 || pet.getWeightPenalty() == 2)
					value *= 0.5;
				else if(pet.getWeightPenalty() == 3)
					value = 0.0;
			}
			return value;
		}

		static
		{
			func = new FuncHpRegenPenalty();
		}
	}

	private static class FuncMpRegenPenalty extends Func
	{
		static final FuncMpRegenPenalty func;

		private FuncMpRegenPenalty()
		{
			super(Stats.REGENERATE_MP_RATE, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.isPet())
			{
				PetInstance pet = (PetInstance) creature;
				if(pet.getWeightPenalty() == 1 || pet.getWeightPenalty() == 2)
					value *= 0.5;
				else if(pet.getWeightPenalty() == 3)
					value = 0.0;
			}
			return value;
		}

		static
		{
			func = new FuncMpRegenPenalty();
		}
	}

	private static class FuncMoveSpeedPenalty extends Func
	{
		static final FuncMoveSpeedPenalty func;

		private FuncMoveSpeedPenalty()
		{
			super(Stats.RUN_SPEED, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.isPlayer())
			{
				Player player = creature.getPlayer();
				if(player.isMounted())
				{
					Mount mount = player.getMount();
					if(mount.isHungry() || mount.getLevel() - player.getLevel() >= 10)
						value *= 0.5;
				}
			}
			else if(creature.isPet())
			{
				PetInstance pet = (PetInstance) creature;
				if(pet.isHungry() || pet.getWeightPenalty() >= 2)
					value *= 0.5;
			}
			return value;
		}

		static
		{
			func = new FuncMoveSpeedPenalty();
		}
	}

	private static class FuncPAtkSpeedPenalty extends Func
	{
		static final FuncPAtkSpeedPenalty func;

		private FuncPAtkSpeedPenalty()
		{
			super(Stats.POWER_ATTACK_SPEED, 256, null);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			if(creature.isPlayer())
			{
				Player player = creature.getPlayer();
				if(player.isMounted() && player.getMount().isHungry())
					value *= 0.5;
			}
			return value;
		}

		static
		{
			func = new FuncPAtkSpeedPenalty();
		}
	}
}
