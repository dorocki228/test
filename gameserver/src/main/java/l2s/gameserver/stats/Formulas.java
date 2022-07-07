package l2s.gameserver.stats;

import java.util.Arrays;
import java.util.Collections;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.HitCondBonusHolder;
import l2s.gameserver.data.xml.holder.KarmaIncreaseDataHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.HitCondBonusType;
import l2s.gameserver.model.base.SkillTrait;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.time.GameTimeService;
import l2s.gameserver.utils.PositionUtils;

public class Formulas
{
	private static final boolean[] DEBUG_DISABLED = {false, false, false};

	public static boolean[] isDebugEnabled(Creature caster, Creature target)
	{
		if (Config.ALT_DEBUG_ENABLED)
		{
			// Включена ли отладка на кастере
			final boolean debugCaster = caster.getPlayer() != null && caster.getPlayer().isDebug();
			// Включена ли отладка на таргете
			final boolean debugTarget = target.getPlayer() != null && target.getPlayer().isDebug();
			// Разрешена ли отладка в PvP
			if (Config.ALT_DEBUG_PVP_ENABLED && (debugCaster && debugTarget) && (!Config.ALT_DEBUG_PVP_DUEL_ONLY || (caster.getPlayer().isInDuel() && target.getPlayer().isInDuel())))
				return new boolean[]{true, true, true};
			// Включаем отладку в PvE если разрешено
			if (Config.ALT_DEBUG_PVE_ENABLED && ((debugCaster && target.isMonster()) || (debugTarget && caster.isMonster())))
				return new boolean[]{true, debugCaster, debugTarget};
		}

		return DEBUG_DISABLED;
	}

	public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean useShot, boolean onCrit)
	{
		return calcPhysDam(attacker, target, skill, 1.0, skill == null ? 0.0 : skill.getPower(target), dual, blow, useShot, onCrit);
	}

	public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, double pAtkMod, double power, boolean dual, boolean blow, boolean useShot, boolean onCrit)
	{
		AttackInfo info = new AttackInfo();
		info.damage = attacker.getPAtk(target) * pAtkMod;
		info.defence = target.getPDef(attacker);
		info.blow = blow;
		info.crit_static = attacker.calcStat(Stats.P_CRITICAL_DAMAGE_STATIC, target, skill);
		info.crit = calcPCrit(attacker, target, skill, info.blow);
		info.shld = (skill == null || !skill.getShieldIgnore()) && calcShldUse(attacker, target);
		info.miss = false;
		boolean isPvP = attacker.isPlayable() && target.isPlayable();
		boolean isPvE = attacker.isPlayable() && target.isNpc();
		boolean isBow = false;

        if(attacker.isPlayer())
		{
			WeaponTemplate weaponTemplate = attacker.getPlayer().getActiveWeaponTemplate();
			if(weaponTemplate != null)
				switch(weaponTemplate.getItemType())
				{
					case BOW:
						isBow = true;
						break;
					case DUALFIST:
                        boolean isDualFist = true;
                        break;
				}
		}

		if(info.shld)
		{
			double shldDef = target.getShldDef();

			if(skill != null && skill.getShieldIgnorePercent() > 0.0)
				shldDef -= shldDef * skill.getShieldIgnorePercent() / 100.0;

			info.defence += shldDef;
		}

		if(skill != null && skill.getDefenceIgnorePercent() > 0.0)
			info.defence *= 1.0 - skill.getDefenceIgnorePercent() / 100.0;

		if(skill != null)
		{
			if(power == 0.0)
				return new AttackInfo();

			if(info.damage > 0.0 && skill.canBeEvaded() && Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0, attacker, skill)))
			{
				ExMagicAttackInfo.packet(attacker, target, MagicAttackType.EVADED);
				info.miss = true;
				info.damage = 0.0;
				return info;
			}

			info.damage *= attacker.getLevelBonus();

			if(info.blow && !skill.isBehind() && useShot)
				info.damage *= (100.0 + attacker.getChargedSoulshotPower()) / 100.0;

			double skillPowerMod = 1.0;

			if(skill.getNumCharges() > 0)
				skillPowerMod *= attacker.calcStat(Stats.CHARGED_P_SKILL_POWER, 1.0);

			if(isBow)
				info.damage += power * attacker.calcStat(Stats.P_SKILL_POWER, 1.0) * skillPowerMod;
			else
				info.damage += power * attacker.calcStat(Stats.P_SKILL_POWER, 1.0) * skillPowerMod;

			info.damage += attacker.calcStat(Stats.P_SKILL_POWER_STATIC);

			if(info.blow && skill.isBehind() && useShot)
				info.damage *= (100.0 + attacker.getChargedSoulshotPower() / 2.0) / 100.0;

			if(!skill.isChargeBoost())
				info.damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;

			if(info.blow)
			{

				double critDmg = info.damage;
				critDmg *= 0.01 * attacker.calcStat(Stats.P_CRITICAL_DAMAGE, target, null) * 0.3;
				critDmg += 6.0 * info.crit_static;
				critDmg -= info.damage;
				critDmg -= (critDmg - target.calcStat(Stats.P_CRIT_DAMAGE_RECEPTIVE, critDmg)) / 2.0;
				critDmg = Math.max(0.0, critDmg);
				info.damage += critDmg;
			}

			if(skill.isChargeBoost())
			{
				int force = attacker.getIncreasedForce();
				if(force > 3)
					force = 3;
				info.damage *= 1.0 + 0.1 * force;
			}
			else if(skill.isSoulBoost())
			{
				info.damage *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);
			}

			if(info.crit)
			{
				double critDmg = info.damage;
				critDmg *= 2.0;
				critDmg *= attacker.calcStat(Stats.P_CRITICAL_DAMAGE_SKILL, target, null) * 0.01;
				critDmg -= info.damage;
				info.damage += critDmg;
			}
		}
		else
		{
			info.damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;

			if(dual)
				info.damage /= 2.0;

			if(info.crit)
			{
				double critDmg = info.damage;
				critDmg *= 2.0 + (attacker.calcStat(Stats.P_CRITICAL_DAMAGE, target, null) * 0.01 - 1.0);
				//				critDmg += attacker.calcStat(Stats.P_CRITICAL_DAMAGE_DIFF, target, null);
				critDmg -= info.damage;
				critDmg = target.calcStat(Stats.P_CRIT_DAMAGE_RECEPTIVE, critDmg);
				critDmg = Math.max(0.0, critDmg);
				critDmg += info.crit_static;

				info.damage += critDmg;
			}

			if(isBow)
			{
				double bowBonus = 0.0;
				switch(PositionUtils.getDirectionTo(target, attacker))
				{
					case BEHIND:
						bowBonus = attacker.getPAtk(target) * 2 * 0.1;
						break;
					case SIDE:
						bowBonus = attacker.getPAtk(target) * 2 * 0.05;
						break;
				}

				info.damage += bowBonus;
			}
		}

		if(info.crit)
		{
			int chance = attacker.getSkillLevel(467);
			if(chance > 0)
			{
				if(chance >= 21)
					chance = 30;
				else if(chance >= 15)
					chance = 25;
				else if(chance >= 9)
					chance = 20;
				else if(chance >= 4)
					chance = 15;
				if(Rnd.chance(chance))
					attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
			}
		}

		if(!isBow)
			if(attacker.isDistortedSpace())
				info.damage *= 1.2;
			else
				switch(PositionUtils.getDirectionTo(target, attacker))
				{
					case BEHIND:
						info.damage *= 1.2;
						break;
					case SIDE:
						info.damage *= 1.1;
						break;
				}

		if(useShot && !info.blow)
			info.damage *= (100.0 + attacker.getChargedSoulshotPower()) / 100.0;

		if(!isBow)
			info.damage *= 77.0 / info.defence;
		else
			info.damage *= 70.0 / info.defence;

		info.damage = attacker.calcStat(Stats.INFLICTS_P_DAMAGE_POWER, info.damage, target, skill);
		info.damage = target.calcStat(Stats.RECEIVE_P_DAMAGE_POWER, info.damage, attacker, skill);

		if(info.shld)
		{
			if(Rnd.chance(Config.EXCELLENT_SHIELD_BLOCK_CHANCE))
			{
				info.damage = Config.EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE;
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				return info;
			}
			target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
		}

		if(isPvP)
		{
			if(skill == null)
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0);
				info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0);

				if(attacker.isSummon() && attacker.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= attacker.getPlayer().calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0);

				if(target.isSummon() && target.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage /= target.getPlayer().calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0);
			}
			else
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0);
				info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0);

				if(attacker.isSummon() && attacker.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= attacker.getPlayer().calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0);

				if(target.isSummon() && target.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= target.getPlayer().calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0);
			}
		}
		else if(isPvE)
			if(skill == null)
			{
				info.damage *= attacker.calcStat(Stats.PVE_PHYS_DMG_BONUS, 1.0);
				info.damage /= target.calcStat(Stats.PVE_PHYS_DEFENCE_BONUS, 1.0);

				if(attacker.isSummon() && attacker.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= attacker.getPlayer().calcStat(Stats.PVE_PHYS_DMG_BONUS, 1.0);

				if(target.isSummon() && target.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage /= target.getPlayer().calcStat(Stats.PVE_PHYS_DEFENCE_BONUS, 1.0);
			}
			else
			{
				info.damage *= attacker.calcStat(Stats.PVE_PHYS_SKILL_DMG_BONUS, 1.0);
				info.damage /= target.calcStat(Stats.PVE_PHYS_SKILL_DEFENCE_BONUS, 1.0);

				if(attacker.isSummon() && attacker.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= attacker.getPlayer().calcStat(Stats.PVE_PHYS_SKILL_DMG_BONUS, 1.0);

				if(target.isSummon() && target.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage /= target.getPlayer().calcStat(Stats.PVE_PHYS_SKILL_DEFENCE_BONUS, 1.0);
			}

		if(info.crit)
			info.damage *= getPCritDamageMode(attacker, skill == null);
		if(info.blow)
			info.damage *= Config.ALT_BLOW_DAMAGE_MOD;
		if(!info.crit && !info.blow)
			info.damage *= getPDamModifier(attacker);

		if(skill != null)
		{
			if(info.damage > 0.0 && skill.isDeathlink())
				info.damage *= Config.DEATH_LINK_MOD * ((skill.isPhysic() ? 2.0 : 1.0) - attacker.getCurrentHpRatio());

			if(onCrit)
			{
				if(!attacker.isCriticalBlowCastingSkill() || attacker.getCastingSkill() != skill)
					return null;
				info.crit = true;
				ExMagicAttackInfo.packet(attacker, target, MagicAttackType.CRITICAL);
			}

			if(info.damage > 0.0)
			{
				WeaponTemplate weaponItem = attacker.getActiveWeaponTemplate();
				if(skill.getIncreaseOnPole() > 0.0 && weaponItem != null && weaponItem.getItemType() == WeaponTemplate.WeaponType.POLE)
					info.damage *= skill.getIncreaseOnPole();

				if(skill.getDecreaseOnNoPole() > 0.0 && weaponItem != null && weaponItem.getItemType() != WeaponTemplate.WeaponType.POLE)
					info.damage *= skill.getDecreaseOnNoPole();

				if(calcStunBreak(info.crit, true, false))
				{
					target.getAbnormalList().stopEffects(EffectType.Stun);
//					target.getAbnormalList().stopEffects(EffectType.Fear);
				}

				if(calcCastBreak(target, info.crit))
					target.abortCast(false, true);
			}
		}

		if(attacker.isServitor() && target.isPlayable())
		{
			if(attacker.getPlayer().isInOlympiadMode() && target.getPlayer().isInOlympiadMode())
				info.damage *= Config.SERVITOR_OLYMPIAD_DAMAGE_MODIFIER;
			else
				info.damage *= Config.SERVITOR_PVP_DAMAGE_MODIFIER;
		}

		return info;
	}

	public static double calcLethalDamage(Creature attacker, Creature target, Skill skill)
	{
		//		if(skill == null)
		//			return 0.0;
		//		if(target.isLethalImmune())
		//			return 0.0;
		//
		//		double deathRcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
		//		double lethal1Chance = skill.getLethal1(attacker) * deathRcpt;
		//		double lethal2Chance = skill.getLethal2(attacker) * deathRcpt;

		//		double damage = 0.0;
		//		if(Rnd.chance(lethal2Chance))
		//		{
		//			if(target.isPlayer())
		//			{
		//				damage = target.getCurrentHp() + target.getCurrentCp() - 1.1;
		//				target.sendPacket(SystemMsg.LETHAL_STRIKE);
		//			}
		//			else
		//				damage = target.getCurrentHp() - 1.0;
		//
		//			attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
		//		}
		//		else if(Rnd.chance(lethal1Chance))
		//		{
		//			if(target.isPlayer())
		//			{
		//				damage = target.getCurrentCp();
		//				target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
		//			}
		//			else
		//				damage = target.getCurrentHp() / 2.0;
		//			attacker.sendPacket(SystemMsg.CP_SIPHON);
		//		}
		//		return damage;
		return 0.0;
	}

	private static double getMSimpleDamageMode(Creature attacker)
	{
		if(!attacker.isPlayer())
			return Config.ALT_M_SIMPLE_DAMAGE_MOD;
		return Config.ALT_M_SIMPLE_DAMAGE_MOD;
	}

	public static double getMCritDamageMode(Creature attacker)
	{
		if(!attacker.isPlayer())
			return Config.ALT_M_CRIT_DAMAGE_MOD;
		return Config.ALT_M_CRIT_DAMAGE_MOD;
	}

	private static int[] archer_classId = new int[] { 9,24, 37, 92, 102, 109 };
	
	private static double getPDamModifier(Creature attacker)
	{
		if(!attacker.isPlayer())
			return Config.ALT_P_DAMAGE_MOD;
		if(attacker.isPlayer() && Arrays.stream(archer_classId).anyMatch(x -> x == attacker.getPlayer().getActiveClassId()))
			return Config.ALT_P_DAMAGE_MOD_ARCHER;
		
		return Config.ALT_P_DAMAGE_MOD;
	}

	private static double getPCritDamageMode(Creature attacker, boolean notSkill)
	{
		if(!attacker.isPlayer())
			return Config.ALT_P_CRIT_DAMAGE_MOD;
		if(attacker.isPlayer() && Arrays.stream(archer_classId).anyMatch(x -> x == attacker.getPlayer().getActiveClassId()))
			return Config.ALT_P_CRIT_DAMAGE_MOD_ARCHER;
		return Config.ALT_P_CRIT_DAMAGE_MOD;
	}

	private static double getPCritChanceMode(Creature attacker)
	{
		if(!attacker.isPlayer())
			return Config.ALT_P_CRIT_CHANCE_MOD;
		return Config.ALT_P_CRIT_CHANCE_MOD;
	}

	private static double getMCritChanceMode(Creature attacker)
	{
		if(!attacker.isPlayer())
			return Config.ALT_M_CRIT_CHANCE_MOD;
		return Config.ALT_M_CRIT_CHANCE_MOD;
	}

	public static AttackInfo calcMagicDam(Creature attacker, Creature target, Skill skill, boolean useShot)
	{
		return calcMagicDam(attacker, target, skill, skill.getPower(target), useShot);
	}

	public static AttackInfo calcMagicDam(Creature attacker, Creature target, Skill skill, double power, boolean useShot)
	{
		boolean isPvP = attacker.isPlayable() && target.isPlayable();
		boolean isPvE = attacker.isPlayable() && target.isNpc();
		boolean shield = !skill.getShieldIgnore() && calcShldUse(attacker, target);
		double crit_static = attacker.calcStat(Stats.M_CRITICAL_DAMAGE_STATIC, target, skill);
		double mAtk = attacker.getMAtk(target, skill);

		if(useShot)
			mAtk *= (100.0 + attacker.getChargedSpiritshotPower()) / 100.0;

		double mdef = target.getMDef(null, skill);

		if(shield)
		{
			double shldDef = target.getShldDef();

			if(skill.getShieldIgnorePercent() > 0.0)
				shldDef -= shldDef * skill.getShieldIgnorePercent() / 100.0;

			mdef += shldDef;
		}

		if(skill.getDefenceIgnorePercent() > 0.0)
			mdef *= 1.0 - skill.getDefenceIgnorePercent() / 100.0;

		mdef = Math.max(mdef, 1.0);

		AttackInfo info = new AttackInfo();

		if(power == 0.0)
			return info;
		
		power = attacker.calcStat(Stats.M_SKILL_POWER, power);

		if(skill.isSoulBoost())
			power *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);

		info.damage = 91.0 * power * Math.sqrt(mAtk) / mdef;

		if(target.isTargetUnderDebuff())
			info.damage *= skill.getPercentDamageIfTargetDebuff();

		// с 2.0 кдл не фейлится по мобам, но фейлится по игровым объектам
		boolean canFail = !skill.isIgnoreFails() || skill.getId() == 1159 && !target.isPlayable();

		if(info.damage > 0.0 && canFail && !skill.hasEffects(EffectUseType.NORMAL) && calcMagicHitMiss(attacker, target, skill))
		{
			info.miss = true;
			info.damage = 0.0;

			ExMagicAttackInfo.packet(attacker, target, MagicAttackType.EVADED);

			attacker.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(attacker));
			target.sendPacket(new SystemMessagePacket(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
			return info;
		}

		info.damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;
		info.crit = calcMCrit(attacker, target, skill);

		if(info.crit)
		{
			if(Config.ENABLE_CRIT_DMG_REDUCTION_ON_MAGIC)
			{
				double critDmg = info.damage;
				critDmg = critDmg * 2.0 * attacker.getMagicCriticalDmg(target, skill) + attacker.calcStat(Stats.M_CRITICAL_DAMAGE_SKILL, target, null) * 0.01;
				critDmg += crit_static;
				critDmg *= getMCritDamageMode(attacker);
				critDmg -= info.damage;

				double tempDamage = target.calcStat(Stats.M_CRIT_DAMAGE_RECEPTIVE, critDmg, attacker, skill);
				critDmg = Math.min(tempDamage, critDmg);
				critDmg = Math.max(0.0, critDmg);
				info.damage += critDmg;
			}
			else
			{
				info.damage = info.damage * 2.0 * attacker.getMagicCriticalDmg(target, skill) + attacker.calcStat(Stats.M_CRITICAL_DAMAGE_SKILL, target, null) * 0.01;
				info.damage += crit_static;
				info.damage *= getMCritDamageMode(attacker);
			}
		}
		else
			info.damage *= getMSimpleDamageMode(attacker);

		info.damage = attacker.calcStat(Stats.INFLICTS_M_DAMAGE_POWER, info.damage, target, skill);
		info.damage = target.calcStat(Stats.RECEIVE_M_DAMAGE_POWER, info.damage, attacker, skill);

		if(shield)
		{
			if(Rnd.chance(Config.EXCELLENT_SHIELD_BLOCK_CHANCE))
			{
				info.damage = Config.EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE;

				ExMagicAttackInfo.packet(attacker, target, MagicAttackType.RESISTED);

				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				attacker.sendPacket(new SystemMessagePacket(SystemMsg.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker));
				return info;
			}

			target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			attacker.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED));
		}

		if(info.damage > 0.0 && skill.isDeathlink())
			info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

		if(info.damage > 0.0 && skill.isBasedOnTargetDebuff())
			info.damage *= 1.0 + 0.05 * target.getAbnormalList().getEffectsCount();

		if(skill.getSkillType() == Skill.SkillType.MANADAM)
			info.damage = Math.max(1.0, info.damage / 4.0);
		else if(info.damage > 0.0)
			if(isPvP)
			{
				info.damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0);
				info.damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0);

				if(attacker.isSummon() && attacker.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= attacker.getPlayer().calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0);

				if(target.isSummon() && target.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage /= target.getPlayer().calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0);
			}
			else if(isPvE)
			{
				info.damage *= attacker.calcStat(Stats.PVE_MAGIC_SKILL_DMG_BONUS, 1.0);
				info.damage /= target.calcStat(Stats.PVE_MAGIC_SKILL_DEFENCE_BONUS, 1.0);

				if(attacker.isSummon() && attacker.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage *= attacker.getPlayer().calcStat(Stats.PVE_MAGIC_SKILL_DMG_BONUS, 1.0);

				if(target.isSummon() && target.calcStat(Stats.SharingEquipment) == 1.0)
					info.damage /= target.getPlayer().calcStat(Stats.PVE_MAGIC_SKILL_DEFENCE_BONUS, 1.0);
			}

		if(calcCastBreak(target, info.crit))
			target.abortCast(false, true);

		if(calcStunBreak(info.crit, true, true) && info.damage > 0.0)
		{
			target.getAbnormalList().stopEffects(EffectType.Stun);
//			target.getAbnormalList().stopEffects(EffectType.Fear);
		}

		if(attacker.isServitor() && target.isPlayable())
		{
			if(attacker.getPlayer().isInOlympiadMode() && target.getPlayer().isInOlympiadMode())
				info.damage *= Config.SERVITOR_OLYMPIAD_DAMAGE_MODIFIER;
			else
				info.damage *= Config.SERVITOR_PVP_DAMAGE_MODIFIER;
		}

		return info;
	}

	public static boolean calcStunBreak(boolean crit, boolean isSkill, boolean isMagic)
	{
		if(!isSkill)
			return Rnd.chance(crit ? Config.CRIT_STUN_BREAK_CHANCE_ON_REGULAR_HIT : Config.NORMAL_STUN_BREAK_CHANCE_ON_REGULAR_HIT);

		if(isMagic)
			return Rnd.chance(crit ? Config.CRIT_STUN_BREAK_CHANCE_ON_MAGICAL_SKILL : Config.NORMAL_STUN_BREAK_CHANCE_ON_MAGICAL_SKILL);

		return Rnd.chance(crit ? Config.CRIT_STUN_BREAK_CHANCE_ON_PHYSICAL_SKILL : Config.NORMAL_STUN_BREAK_CHANCE_ON_PHYSICAL_SKILL);
	}

	public static boolean calcBlow(Creature activeChar, Creature target, Skill skill)
	{
		double vulnMod = target.calcStat(Stats.BLOW_RESIST, activeChar, skill);
		double profMod = activeChar.calcStat(Stats.BLOW_POWER, target, skill);

		if(vulnMod == Double.POSITIVE_INFINITY || profMod == Double.NEGATIVE_INFINITY)
			return false;

		if(vulnMod == Double.NEGATIVE_INFINITY || profMod == Double.POSITIVE_INFINITY)
			return true;

		WeaponTemplate weapon = activeChar.getActiveWeaponTemplate();
		double base_weapon_crit = weapon == null ? 4.0 : weapon.getCritical();
		double crit_height_bonus = 1.0;

		if(Config.ENABLE_CRIT_HEIGHT_BONUS)
			crit_height_bonus = 0.008 * (double) Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ())) + 1.1;

		double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
		double skill_mod = skill.isBehind() ? Config.BLOW_SKILL_CHANCE_MOD_ON_BEHIND : Config.BLOW_SKILL_CHANCE_MOD_ON_FRONT;
		double chance = base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;
//		double modDiff = profMod - vulnMod;

//		if(modDiff != 1.0)
//			chance *= 1.0 + (80.0 + vulnMod) / 200.0;

		chance *= 1.0 + (80.0 + vulnMod) / 200.0;

		if(!target.isInCombat())
			chance *= 1.1;

		if(activeChar.isDistortedSpace())
			chance *= 1.3;
		else
		{
			switch(PositionUtils.getDirectionTo(target, activeChar))
			{
				case BEHIND:
					chance *= 1.3;
					break;
				case SIDE:
					chance *= 1.1;
					break;
				case FRONT:
					if(!skill.isBehind())
						break;
					chance = 3.0;
			}
		}

		chance = Math.min(skill.isBehind() ? Config.MAX_BLOW_RATE_ON_BEHIND : Config.MAX_BLOW_RATE_ON_FRONT_AND_SIDE, chance);

		return Rnd.chance(chance);
	}

	public static boolean calcPCrit(Creature attacker, Creature target, Skill skill, boolean blow)
	{
		if(attacker.isPlayer() && attacker.getActiveWeaponTemplate() == null)
			return false;

		if(skill != null)
		{
			boolean dexDep = attacker.calcStat(Stats.P_SKILL_CRIT_RATE_DEX_DEPENDENCE) > 0.0;

			double skillRate = skill.getCriticalRate() * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);
			skillRate *= blow ? (attacker.getSTR() > attacker.getDEX() ? BaseStats.STR.calcBonus(attacker) : BaseStats.DEX.calcBonus(attacker)) : BaseStats.STR.calcBonus(attacker);

			if(dexDep && attacker.getDEX() > attacker.getTemplate().getBaseDEX())
			{
				int statModifier = 100 + attacker.getDEX() - attacker.getTemplate().getBaseDEX();

				if(blow)
					statModifier = (int) (statModifier * Config.BLOW_SKILL_DEX_CHANCE_MOD);
				else
					statModifier = (int) (statModifier * Config.NORMAL_SKILL_DEX_CHANCE_MOD);

				skillRate *= statModifier * 0.01;
			}

			if(blow)
				skillRate *= Config.ALT_BLOW_CRIT_RATE_MODIFIER;

			return Rnd.chance(skillRate * getPCritChanceMode(attacker));
		}

		double rate = attacker.getPCriticalHit(target) * 0.01 * target.calcStat(Stats.P_CRIT_CHANCE_RECEPTIVE, attacker, skill);

		if(attacker.isDistortedSpace())
			rate *= 1.4;
		else
			switch(PositionUtils.getDirectionTo(target, attacker))
			{
				case BEHIND:
					rate *= 1.4;
					break;
				case SIDE:
					rate *= 1.2;
					break;
			}

		return Rnd.chance(rate / 10.0 * getPCritChanceMode(attacker));
	}

	public static boolean calcMCrit(Creature attacker, Creature target, Skill skill)
	{
		//FIXME КОСТЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЫЛЬ НА КОСТЫЛЕ, РОЖДАЕТ КОСТЫЛЬ

		double rate = attacker.getMCriticalHit(target, skill) * skill.getCriticalRateMod() * 0.01 * target.calcStat(Stats.M_CRIT_CHANCE_RECEPTIVE, attacker, skill);

		int max = skill.isHandler() ? 15 : 50;
		int attach = skill.isHandler() ? 0 : -2;

		return Rnd.chance(Math.min(max, (rate / 6.0 * getMCritChanceMode(attacker) * BaseStats.WIT.calcBonus(attacker)) + attach));
	}

	public static boolean calcCastBreak(Creature target, boolean crit)
	{
		if(target == null || target.isInvul() || target.isRaid() || !target.isCastingNow())
			return false;

		Skill skill = target.getCastingSkill();
		return (skill == null || !skill.isPhysic()) && Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75.0 : 10.0, null, skill));
	}

	public static int calcPAtkSpd(double rate)
	{
		return (int) (500000.0 / rate);
	}

	public static int calcSkillCastSpd(Creature attacker, Skill skill, double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333.0 / Math.max(attacker.getMAtkSpd(), 1));

		if(skill.isPhysic())
			return (int) (skillTime * 333.0 / Math.max(attacker.getPAtkSpd(), 1));

		return (int) skillTime;
	}

	public static long calcSkillReuseDelay(Creature actor, Skill skill)
	{
		long reuseDelay;
		if(actor.isPlayer() && actor.getPlayer().isInOlympiadMode())
            reuseDelay = skill.getOlympiadReuseDelay();
        else
            reuseDelay = skill.getReuseDelay();

        if(actor.isMonster())
			reuseDelay = skill.getReuseForMonsters();

		if(skill.isHandler() || skill.isItemSkill())
			return reuseDelay;

		if(skill.isReuseDelayPermanent())
			return reuseDelay;

		if(skill.isMusic())
			return (long) actor.calcStat(Stats.MUSIC_REUSE_RATE, reuseDelay, null, skill);

		if(skill.isMagic())
			return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill);

		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill);
	}

	private static double getConditionBonus(Creature attacker, Creature target)
	{
		double mod = 100.0;
		if(attacker.getZ() - target.getZ() > 50)
			mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.HIGH);
		else if(attacker.getZ() - target.getZ() < -50)
			mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.LOW);

		if(GameTimeService.INSTANCE.isNowNight())
			mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.DARK);

		if(attacker.isDistortedSpace())
			mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.BACK);
		else
		{
			PositionUtils.TargetDirection direction = PositionUtils.getDirectionTo(attacker, target);
			switch(direction)
			{
				case BEHIND:
					mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.BACK);
					break;
				case SIDE:
					mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.SIDE);
					break;
				default:
					mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.AHEAD);
					break;
			}
		}
		return Math.max(mod / 100.0, 0.0);
	}

	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		double chanceToHit = 100.0 - 5.0 * Math.pow(1.1, target.getPEvasionRate(attacker) - attacker.getPAccuracy());
		chanceToHit *= getConditionBonus(attacker, target);
		chanceToHit = Math.max(chanceToHit, Config.PHYSICAL_MIN_CHANCE_TO_HIT);
		chanceToHit = Math.min(chanceToHit, Config.PHYSICAL_MAX_CHANCE_TO_HIT);
		return !Rnd.chance(chanceToHit);
	}

	public static boolean calcMagicHitMiss(Creature attacker, Creature target, Skill skill)
	{

        double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
		int levelDiff = target.getLevel() - attacker.getLevel();

        double chanceToHit = Math.max(1.0, levelDiff) * (1.0 + magic_rcpt / 100.0);
        chanceToHit += (target.getMEvasionRate(attacker) - attacker.getMAccuracy());

		chanceToHit = Math.max(chanceToHit, Config.MAGIC_MIN_CHANCE_TO_HIT_MISS);
		chanceToHit = Math.min(chanceToHit, Config.MAGIC_MAX_CHANCE_TO_HIT_MISS);
		if (attacker.isPlayer() && ((Player) attacker).isDebug())
			attacker.sendMessage("Fail chance " + chanceToHit);
		return Rnd.chance(chanceToHit);
	}

	public static boolean calcShldUse(Creature attacker, Creature target)
	{
		WeaponTemplate template = target.getSecondaryWeaponTemplate();

		if (template == null || template.getItemType() != WeaponTemplate.WeaponType.NONE)
			return false;

		int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		return (angle >= 360 || PositionUtils.isFacing(target, attacker, angle)) && Rnd.chance((int) target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}

	public static boolean calcSkillSuccess(Creature player, Creature target, Skill skill, double value,
										   EffectTemplate et, boolean useShot)
	{
		if(value == -1.0)
			return true;

		value = Math.max(Math.min(value, 100.0), 1.0);
		double base = value;

		if(!skill.isOffensive())
			return Rnd.chance(value);

		final boolean[] debug = isDebugEnabled(player, target);
		final boolean debugGlobal = debug[0];
		final boolean debugCaster = debug[1];
		final boolean debugTarget = debug[2];
		if(!skill.isRenewal() && target.getAbnormalList().containsEffects(skill))
			return false;

		double statMod = 0;
        if(skill.getSaveVs() != null)
		{
			statMod = skill.getSaveVs().calcChanceMod(target);
            statMod = Math.min(statMod, Config.MIN_SAVEVS_REDUCTION);
			statMod = Math.max(statMod, Config.MAX_SAVEVS_REDUCTION);
			value *= statMod;
		}

		value = Math.max(value, 1.0);
		double mAtkMod = 1.;
		double ssMod = 0;
        if(skill.isMagic() && Config.ENABLE_MATK_SKILL_LANDING_MOD)
		{
			int mdef = Math.max(1, target.getMDef(target, skill));
			double matk = player.getMAtk(target, skill);

			if(skill.isSSPossible() && useShot) {
				ssMod = (100.0 + player.getChargedSpiritshotPower()) / 100.0;
				matk *= ssMod;
			}

            mAtkMod = Config.SKILLS_CHANCE_MOD * Math.pow(matk, Config.SKILLS_CHANCE_POW) / mdef;
            value *= mAtkMod;
			value = Math.max(value, 1.0);
		}

        if(skill.isMagic() && Config.ENABLE_WIT_SKILL_LANDING_MOD)
		{
            double witMod = BaseStats.WIT.calcBonus(player) / 4.0;

            if(skill.isSSPossible() && useShot)
				witMod *= (100.0 + player.getChargedSpiritshotPower()) / 100.0;

			witMod = 1.0 + witMod * 0.01;
			value *= witMod;
			value = Math.max(value, 1.0);
		}

		double lvlDependMod = skill.getLevelModifier();
		if(lvlDependMod != 0.0)
		{
			int attackLevel;

			if(skill.getMagicLevel() > 0)
			{
				if(skill.getMagicLevel() >= 75 && skill.getMagicLevel() < player.getLevel() && skill.getLevel() == skill.getMaxLevel())
					attackLevel = player.getLevel();
				else
					attackLevel = skill.getMagicLevel();
			}
			else
				attackLevel = player.getLevel();

			int delta = attackLevel - target.getLevel();

			lvlDependMod = 1. + delta * (target.isPlayable() ? 0.112 : 0.032) * lvlDependMod;
			if(lvlDependMod < 0.0)
				lvlDependMod = 0.0;
			else if(lvlDependMod > 2.0)
				lvlDependMod = 2.0;
			value *= lvlDependMod;
		}

        SkillTrait trait = skill.getTraitType();
		double resMod = 0;
		double profMod = 0;
		double vulnMod = 0;
		if(!skill.isIgnoreResists() && trait != null)
		{
            vulnMod = trait.calcVuln(player, target, skill);
            profMod = trait.calcProf(player, target, skill);

            if(vulnMod == Double.POSITIVE_INFINITY || profMod == Double.NEGATIVE_INFINITY) {
				if (debugGlobal)
				{
					if (debugCaster)
						player.getPlayer().sendMessage("Full debuff immunity");
					if (debugTarget)
						target.getPlayer().sendMessage("Full debuff immunity");
				}
				return false;
			}

			if(vulnMod == Double.NEGATIVE_INFINITY || profMod == Double.POSITIVE_INFINITY) {
				if (debugGlobal)
				{
					if (debugCaster)
						player.getPlayer().sendMessage("Full debuff vulnerability");
					if (debugTarget)
						target.getPlayer().sendMessage("Full debuff vulnerability");
				}
				return true;
			}

			double modDiff = profMod - vulnMod;

			if(modDiff != 1.0)
			{
                resMod = 1.0 + (80.0 + modDiff) / 200.0;
                value *= resMod;
			}
		}
		double debuffMod = 0;
        if(!skill.isIgnoreResists())
		{
			double debuffVuln = target.calcStat(Stats.DEBUFF_RESIST, player, skill);

			if(debuffVuln == Double.POSITIVE_INFINITY) {
				if (debugGlobal)
				{
					if (debugCaster)
						player.getPlayer().sendMessage("Full debuff vulnerability");
					if (debugTarget)
						target.getPlayer().sendMessage("Full debuff vulnerability");
				}
				return false;
			}

			if(debuffVuln == Double.NEGATIVE_INFINITY) {
				if (debugGlobal)
				{
					if (debugCaster)
						player.getPlayer().sendMessage("Full immunity");
					if (debugTarget)
						target.getPlayer().sendMessage("Full immunity");
				}
				return true;
			}

            debuffMod = 1.0 - debuffVuln * 0.01;

            if(debuffMod != 1.0)
			{
				debuffMod = Math.max(debuffMod, 0.0);
				value *= debuffMod;
			}
		}
		value = Math.max(value, Math.min(base, Config.SKILLS_CHANCE_MIN));
		value = Math.max(Math.min(value, Config.SKILLS_CHANCE_CAP), 0.0);
		final boolean result = Rnd.chance((int) value);

		if (debugGlobal)
		{
			StringBuilder stat = new StringBuilder(100);
			stat.append(skill.getId());
			stat.append("/");
			stat.append(skill.getDisplayLevel());
			stat.append(" ");
			if (et == null)
				stat.append(skill.getName());
			else
				stat.append(et._effectType.name());
			stat.append(" AR:");
			stat.append((int) base);
			stat.append(" ");
			if (skill.getSaveVs() != null)
			{
				stat.append(skill.getSaveVs().name());
				stat.append(":");
				stat.append(String.format("%1.1f", statMod));
			}
			if (skill.isMagic())
			{
				stat.append(" ");
				stat.append(" mAtk:");
				stat.append(String.format("%1.1f", mAtkMod));
				stat.append(" SS:");
				stat.append(ssMod);
			}
			if (skill.getTraitType() != null)
			{
				stat.append(" ");
				stat.append(skill.getTraitType().name());
			}
			stat.append(" ");
			stat.append(String.format("%1.1f", resMod));
			stat.append("(");
			stat.append(String.format("%1.1f", profMod));
			stat.append("/");
			stat.append(String.format("%1.1f", vulnMod));
			if (debuffMod != 0)
			{
				stat.append("+");
				stat.append(String.format("%1.1f", debuffMod));
			}
			stat.append(") lvl:");
			stat.append(String.format("%1.1f", lvlDependMod));
/*			stat.append(" elem:");
			stat.append(String.format("%1.1f", elementMod));*/
			stat.append(" Chance:");
			stat.append(String.format("%1.1f", value));
			if (!result)
				stat.append(" failed");

			// отсылаем отладочные сообщения
			if (debugCaster)
				player.getPlayer().sendMessage(stat.toString());
			if (debugTarget)
				target.getPlayer().sendMessage(stat.toString());
		}
		return result;
	}

	public static boolean calcSkillSuccess(Creature player, Creature target, Skill skill, int activateRate)
	{
		return calcSkillSuccess(player, target, skill, activateRate, null, true);
	}

	public static double calcDamageResists(Skill skill, Creature attacker, Creature defender, double value)
	{
		if(attacker == defender)
			return value;

		if(attacker.isBoss())
			value *= Config.RATE_EPIC_ATTACK;
		else if(attacker.isRaid() || attacker instanceof ReflectionBossInstance)
			value *= Config.RATE_RAID_ATTACK;

		if(defender.isBoss())
			value /= Config.RATE_EPIC_DEFENSE;
		else if(defender.isRaid() || defender instanceof ReflectionBossInstance)
			value /= Config.RATE_RAID_DEFENSE;

		Player pAttacker = attacker.getPlayer();
		int diff = defender.getLevel() - (pAttacker != null ? pAttacker.getLevel() : attacker.getLevel());

		if(attacker.isPlayable() && defender.isMonster() && defender.getLevel() >= 78 && diff > 2)
			value *= 0.7 / Math.pow(diff - 2, 0.25);

		double elementModSum = 1.0;

		if(skill != null)
		{
            StringBuilder elementNameDebug = new StringBuilder("Element:");
			StringBuilder elementAttackDebug = new StringBuilder("Attack:");
			StringBuilder elementDefenceDebug = new StringBuilder("Defence:");
			StringBuilder elementModifierDebug = new StringBuilder("Modifier:");
			double bonusMax = 1.0;
			Element[] elements = skill.getElements();
			double power = skill.getElementsPower();
			for(Element element : elements)
				if(element != Element.NONE)
				{
					double attack = attacker.calcStat(element.getAttack(), power);
					double defence = defender.calcStat(element.getDefence(), 0.0);
					double elementMod = getElementMod(defence, attack);

					bonusMax = Math.max(bonusMax, elementMod);
				}

			elementModSum *= bonusMax;

			if (pAttacker != null && pAttacker.isGM() && pAttacker.isDebug())
			{
				pAttacker.sendMessage(elementNameDebug.toString());
				pAttacker.sendMessage(elementAttackDebug.toString());
				pAttacker.sendMessage(elementDefenceDebug.toString());
				pAttacker.sendMessage(elementModifierDebug.toString());
			}
		}
		else
		{
			Element element2 = getAttackElement(attacker, defender);

			if(element2 == Element.NONE)
				return value;

			double attack2 = attacker.calcStat(element2.getAttack(), 0.0);
			double defence2 = defender.calcStat(element2.getDefence(), 0.0);
			elementModSum = getElementMod(defence2, attack2);

			if (pAttacker != null && pAttacker.isGM() && pAttacker.isDebug())
			{
				pAttacker.sendMessage("Element: " + element2.name());
				pAttacker.sendMessage("Attack: " + attack2);
				pAttacker.sendMessage("Defence: " + defence2);
				pAttacker.sendMessage("Modifier: " + elementModSum);
			}
		}
		return value * elementModSum;
	}

	private static double getElementMod(double defense, double attack)
	{
		double diff = attack - defense;
		if(diff > 0.0)
			diff = 1.025 + Math.sqrt(Math.pow(Math.abs(diff), 3.0) / 2.0) * 1.0E-4;
		else if(diff < 0.0)
			diff = 0.975 - Math.sqrt(Math.pow(Math.abs(diff), 3.0) / 2.0) * 1.0E-4;
		else
			diff = 1.0;
		diff = Math.max(diff, 0.75);
		diff = Math.min(diff, 1.25);
		return diff;
	}

	public static Element getAttackElement(Creature attacker, Creature target)
	{
		double max = Double.MIN_VALUE;
		Element result = Element.NONE;

		for(Element e : Element.VALUES)
		{
			double val = attacker.calcStat(e.getAttack(), 0.0);
			if(val > 0.0)
			{
				if(target != null)
					val -= target.calcStat(e.getDefence(), 0.0);
				if(val > max)
				{
					result = e;
					max = val;
				}
			}
		}
		return result;
	}

	public static int calculateKarmaLost(Player player, long exp)
	{
		if(Config.RATE_KARMA_LOST_STATIC != -1)
			return Config.RATE_KARMA_LOST_STATIC;

		double karmaLooseMul = KarmaIncreaseDataHolder.getInstance().getData(player.getLevel());

		if(exp > 0L)
			exp = (long) (exp / (Config.KARMA_RATE_KARMA_LOST == -1 ? Config.RATE_XP_BY_LVL[player.getLevel()] : Config.KARMA_RATE_KARMA_LOST));
		return (int) (Math.abs(exp) / karmaLooseMul / 5.96);
	}

	public static class AttackInfo
	{
		public double damage;
		public double defence;
		public double crit_static;
		public boolean crit;
		public boolean shld;
		public boolean miss;
		public boolean blow;

		public AttackInfo()
		{
			damage = 0.0;
			defence = 0.0;
			crit_static = 0.0;
			crit = false;
			shld = false;
			miss = false;
			blow = false;
		}
	}
}
