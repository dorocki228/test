package l2s.gameserver.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.commons.lang.ArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.*;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.permission.ActionPermissionComponent;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionLevel;
import l2s.gameserver.permission.interfaces.IAttackPermission;
import l2s.gameserver.permission.interfaces.IIncomingAttackPermission;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.skillclasses.*;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.templates.SkillName;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

public class Skill extends StatTemplate implements Cloneable
{
	public static final String CHECK_CONDITION_SKILL_FOR_ZONE = "check_condition_skill_for_zone";
	protected static final Logger _log;
	public static final Skill[] EMPTY_ARRAY;
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_COMMON_CRAFTING = 1320;
	public static final int SKILL_POLEARM_MASTERY = 216;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
	public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_BLINDING_BLOW = 321;
	public static final int SKILL_STRIDER_ASSAULT = 325;
	public static final int SKILL_WYVERN_AEGIS = 327;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_HEROIC_MIRACLE = 395;
	public static final int SKILL_HEROIC_BERSERKER = 396;
	public static final int SKILL_SOUL_MASTERY = 467;
	public static final int SKILL_TRANSFORM_DISPEL = 619;
	public static final int SKILL_FINAL_FLYING_FORM = 840;
	public static final int SKILL_AURA_BIRD_FALCON = 841;
	public static final int SKILL_AURA_BIRD_OWL = 842;
	public static final int SKILL_DETECTION = 933;
	public static final int SKILL_DETECTION2 = 10785;
	public static final int SKILL_RECHARGE = 1013;
	public static final int SKILL_TRANSFER_PAIN = 1262;
	public static final int SKILL_SUMMON_CP_POTION = 1324;
	public static final int SKILL_HEROIC_VALOR = 1374;
	public static final int SKILL_HEROIC_GRANDEUR = 1375;
	public static final int SKILL_HEROIC_DREAD = 1376;
	public static final int SKILL_MYSTIC_IMMUNITY = 1411;
	public static final int SKILL_RAID_BLESSING = 2168;
	public static final int SKILL_HINDER_STRIDER = 4258;
	public static final int SKILL_WYVERN_BREATH = 4289;
	public static final int SKILL_RAID_CURSE = 4515;
	public static final int SKILL_RAID_CURSE_2 = 4215;
	public static final int SKILL_EVENT_TIMER = 5239;
	public static final int SKILL_BATTLEFIELD_DEATH_SYNDROME = 5660;
	public static final int SKILL_SERVITOR_SHARE = 1557;
	public static final int SKILL_CONFUSION = 1570;
	private final TIntObjectMap<List<EffectTemplate>> _effectTemplates;
	private final AddedSkill[] _addedSkills;
	private final long _itemConsume;
	private final int _itemConsumeId;
	private final boolean _itemConsumeFromMaster;
	private final int[] _relationSkillsId;
	private final int _referenceItemId;
	private final int _referenceItemMpConsume;
	private final boolean _isBehind;
	private final boolean _isCancelable;
	private final boolean _isCorpse;
	private final boolean _isItemHandler;
	private final boolean _isOffensive;
	private final boolean _isPvpSkill;
	private final boolean isDebuff;
	private final boolean _isNotUsedByAI;
	private final boolean _isPvm;
	private final boolean _isForceUse;
	private final boolean _isNewbie;
	private final boolean _isPreservedOnDeath;
	private final boolean _isSaveable;
	private final boolean _isSkillTimePermanent;
	private final boolean _isReuseDelayPermanent;
	private final boolean _isReflectable;
	private final boolean _isSuicideAttack;
	private final boolean _isIgnoreFails;
	private final boolean _isShieldignore;
	private final double _shieldIgnorePercent;
	public final boolean _isUndeadOnly;
	private final Ternary _isUseSS;
	private final boolean _isOverhit;
	private final boolean _isSoulBoost;
	private final boolean _isChargeBoost;
	private final boolean _isIgnoreResists;
	private final boolean _isIgnoreInvul;
	private final boolean _isTrigger;
	private final boolean _isNotAffectedByMute;
	private final boolean _basedOnTargetDebuff;
	private final boolean _deathlink;
	private final boolean _hideStartMessage;
	private final boolean _hideUseMessage;
	private final boolean _skillInterrupt;
	private final boolean _flyingTransformUsage;
	private final boolean _canUseTeleport;
	private final boolean _isProvoke;
	protected final StatsSet params;
	private final boolean _checkConditionSkillForZone;
	private final boolean _globalReuse;
	private boolean _isCubicSkill;
	private final boolean _isSelfDispellable;
	private final boolean _abortable;
	private final boolean _isRelation;
	private final double _decreaseOnNoPole;
	private final double _increaseOnPole;
	private final boolean _canUseWhileAbnormal;
	private final int _lethal2SkillDepencensyAddon;
	private final double _lethal2Addon;
	private final int _lethal1SkillDepencensyAddon;
	private final double _lethal1Addon;
	private final boolean _isCancel;
	private final SkillType _skillType;
	private final SkillOperateType _operateType;
	private final SkillTargetType _targetType;
	private final SkillMagicType _magicType;
	private final SkillTrait _traitType;
	private final BaseStats _saveVs;
	private final boolean _dispelOnDamage;
	private final NextAction _nextAction;
	private final Element[] _elements;
	private final FlyToLocationPacket.FlyType _flyType;
	private final boolean _flyDependsOnHeading;
	private final int _flyRadius;
	private final int _flyPositionDegree;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _flyAnimationSpeed;
	private Condition[] _preCondition;
	private final int _id;
	private final int _level;
	private final int _maxLevel;
	private final int _displayId;
	private int _displayLevel;
	private final int _activateRate;
	private final int _castRange;
	private final int _condCharges;
	private final int _coolTime;
	private final int _effectPoint;
	private final int _cprConsume;
	private final int _fameConsume;
	private final int _elementsPower;
	private final int _hitTime;
	private final int _hpConsume;
	private final int _levelModifier;
	private final int _magicLevel;
	private final int _matak;
	private final PledgeRank _minPledgeRank;
	private final boolean _clanLeaderOnly;
	private final int _npcId;
	private final int _numCharges;
	private final int _skillInterruptTime;
	private final int _affectRange;
	private final int _behindRadius;
	private final int _soulsConsume;
	private final int _tickInterval;
	private final int _criticalRate;
	private final double _criticalRateMod;

	private final int _reuseDelay;
	private final int olympiadReuseDelay;

	private final double _power;
	private final double _chargeEffectPower;
	private final double _chargeDefectPower;
	private final double _powerPvP;
	private final double _chargeEffectPowerPvP;
	private final double _chargeDefectPowerPvP;
	private final double _powerPvE;
	private final double _chargeEffectPowerPvE;
	private final double _chargeDefectPowerPvE;
	private final double _mpConsume1;
	private final double _mpConsume2;
	private final double _mpConsumeTick;
	private final double _lethal1;
	private final double _lethal2;
	private final double _absorbPart;
	private final double _defenceIgnorePercent;
	private final String _name;
	private final String _baseValues;
	private final String _icon;
	public boolean _isStandart;
	private final int _hashCode;
	private final int _reuseSkillId;
	private final int _reuseHash;
	private final boolean _switchable;
	private final boolean _isNotDispelOnSelfBuff;
	private final int _abnormalTime;
	private final int _abnormalLvl;
	private final AbnormalType _abnormalType;
	private final AbnormalEffect[] _abnormalEffects;
	private final boolean _abnormalHideTime;
	private final boolean _abnormalCancelOnAction;
	private final boolean _detectPcHide;
	private final int _rideState;
	private final boolean _isSelfOffensive;
	private final boolean _applyEffectsOnSummon;
	private final boolean _applyEffectsOnPet;
	private final boolean _applyMinRange;
	private final int _masteryLevel;
	private final boolean _altUse;
	private final boolean _isItemSkill;
	private final boolean _addSelfTarget;
	private final double _percentDamageIfTargetDebuff;
	private final boolean _noFlagNoForce;
	private final boolean _noEffectsIfFailSkill;
	private final boolean _renewal;
	private final int _buffSlotType;
	private SkillEntry _entry;
	private final int[] _affectLimit;
	private final boolean _removeEffectOnDeleteSkill;
	private final long _reuseDelayOnEquip;

	public Skill(StatsSet set)
	{
		_effectTemplates = new TIntObjectHashMap<>(EffectUseType.VALUES.length);
		_preCondition = Condition.EMPTY_ARRAY;
		_isStandart = false;
		_entry = null;
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_displayId = set.getInteger("display_id", _id);
		_displayLevel = set.getInteger("display_level", _level);
		_maxLevel = set.getInteger("max_level");
		_name = set.getString("name");
		_operateType = set.getEnum("operate_type", SkillOperateType.class);
		_isNewbie = set.getBool("isNewbie", false);
		_isSelfDispellable = set.getBool("isSelfDispellable", true);
		_isPreservedOnDeath = set.getBool("isPreservedOnDeath", false);
		_cprConsume = set.getInteger("clanRepConsume", 0);
		_fameConsume = set.getInteger("fameConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_soulsConsume = set.getInteger("soulsConsume", 0);
		_isSoulBoost = set.getBool("soulBoost", false);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_isProvoke = set.getBool("provoke", false);
		_matak = set.getInteger("mAtk", 0);
		_isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());
		_magicLevel = set.getInteger("magicLevel", 0);
		_tickInterval = Math.max(-1, (int) (set.getDouble("tick_interval", -1.0) * 1000.0));
		_castRange = set.getInteger("castRange", 40);
		_baseValues = set.getString("baseValues", null);
		_abnormalTime = set.getInteger("abnormal_time", -1);
		_abnormalLvl = set.getInteger("abnormal_level", 0);
		_abnormalType = set.getEnum("abnormal_type", AbnormalType.class, AbnormalType.none);
		String[] abnormalEffects = set.getString("abnormal_effect", AbnormalEffect.NONE.toString()).split(";");
		_abnormalEffects = new AbnormalEffect[abnormalEffects.length];
		for(int i = 0; i < abnormalEffects.length; ++i)
			_abnormalEffects[i] = AbnormalEffect.valueOf(abnormalEffects[i].toUpperCase());
		_abnormalHideTime = set.getBool("abnormal_hide_time", false);
		_abnormalCancelOnAction = set.getBool("abnormal_cancel_on_action", false);
		String[] ride_state = set.getString("ride_state", MountType.NONE.toString()).split(";");
		int rideState = 0;
		for(int j = 0; j < ride_state.length; ++j)
			rideState |= 1 << MountType.valueOf(ride_state[j].toUpperCase()).ordinal();
		_rideState = rideState;
		_switchable = set.getBool("switchable", true);
		_isNotDispelOnSelfBuff = set.getBool("doNotDispelOnSelfBuff", false);
		_itemConsume = set.getLong("itemConsumeCount", 0L);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeFromMaster = set.getBool("consume_item_from_master", false);
		String s3 = set.getString("relationSkillsId", "");
		if(s3.isEmpty())
		{
			_isRelation = false;
			_relationSkillsId = new int[] { 0 };
		}
		else
		{
			_isRelation = true;
			String[] s4 = s3.split(";");
			_relationSkillsId = new int[s4.length];
			for(int k = 0; k < s4.length; ++k)
				_relationSkillsId[k] = Integer.parseInt(s4[k]);
		}
		_referenceItemId = set.getInteger("referenceItemId", 0);
		_referenceItemMpConsume = set.getInteger("referenceItemMpConsume", 0);
		_isItemHandler = set.getBool("isHandler", false);
		_isSaveable = set.getBool("isSaveable", _operateType.isActive());
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = set.getInteger("hitCancelTime", 0);
		_reuseDelay = set.getInteger("reuseDelay", 0);
        olympiadReuseDelay = set.getInteger("olympiadReuseDelay", getReuseDelay());
		_hitTime = set.getInteger("hitTime", 0);
		_affectRange = set.getInteger("affect_range", 80);
		_behindRadius = Math.min(360, Math.max(0, set.getInteger("behind_radius", 0)));
		_targetType = set.getEnum("target", SkillTargetType.class, SkillTargetType.TARGET_SELF);
		_magicType = set.getEnum("magicType", SkillMagicType.class, SkillMagicType.PHYSIC);
		int mpConsume = set.getInteger("mp_consume", 0);
		_mpConsume1 = set.getInteger("mp_consume1", _magicType == SkillMagicType.MAGIC ? mpConsume / 4 : 0);
		_mpConsume2 = set.getInteger("mp_consume2", _magicType == SkillMagicType.MAGIC ? mpConsume / 4 * 3 : mpConsume);
		_mpConsumeTick = set.getInteger("mp_consume_tick", 0);
		_traitType = set.getEnum("trait", SkillTrait.class, null);
		_saveVs = set.getEnum("saveVs", BaseStats.class, null);
		_dispelOnDamage = set.getBool("dispelOnDamage", false);
		_hideStartMessage = set.getBool("isHideStartMessage", isHidingMesseges());
		_hideUseMessage = set.getBool("isHideUseMessage", isHidingMesseges());
		_isUndeadOnly = set.getBool("undeadOnly", false);
		_isCorpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.0);
		_chargeEffectPower = set.getDouble("chargeEffectPower", _power);
		_chargeDefectPower = set.getDouble("chargeDefectPower", _power);
		_powerPvP = set.getDouble("powerPvP", 0.0);
		_chargeEffectPowerPvP = set.getDouble("chargeEffectPowerPvP", _powerPvP);
		_chargeDefectPowerPvP = set.getDouble("chargeDefectPowerPvP", _powerPvP);
		_powerPvE = set.getDouble("powerPvE", 0.0);
		_chargeEffectPowerPvE = set.getDouble("chargeEffectPowerPvE", _powerPvE);
		_chargeDefectPowerPvE = set.getDouble("chargeDefectPowerPvE", _powerPvE);
		_effectPoint = set.getInteger("effectPoint", 1);
		_skillType = set.getEnum("skillType", SkillType.class, SkillType.EFFECT);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isIgnoreInvul = set.getBool("isIgnoreInvul", false);
		_isTrigger = set.getBool("isTrigger", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_flyingTransformUsage = set.getBool("flyingTransformUsage", false);
		_canUseTeleport = set.getBool("canUseTeleport", true);
		_altUse = set.getBool("alt_use", false);
		String[] elements = set.getString("elements", "NONE").split(";");
		_elements = new Element[elements.length];
		for(int l = 0; l < _elements.length; ++l)
		{
			String element = elements[l];
			if(NumberUtils.isCreatable(element))
				_elements[l] = Element.getElementById(Integer.parseInt(element));
			else
				_elements[l] = Element.getElementByName(element.toUpperCase());
		}
		_elementsPower = set.getInteger("elementsPower", 0);
		_activateRate = set.getInteger("activateRate", -1);
		_levelModifier = set.getInteger("levelModifier", 1);
		_isCancelable = set.getBool("cancelable", true);
		_isReflectable = set.getBool("reflectable", true);
		_isIgnoreFails = set.getBool("ignorefails", false);
		_isShieldignore = set.getBool("shieldignore", false);
		_shieldIgnorePercent = set.getDouble("shield_ignore_percent", 0.0);
		_criticalRate = set.getInteger("criticalRate", 0);
		_criticalRateMod = set.getDouble("critical_rate_modifier", 1.0);
		_isOverhit = set.getBool("overHit", false);
		_minPledgeRank = set.getEnum("min_pledge_rank", PledgeRank.class, PledgeRank.VAGABOND);
		_clanLeaderOnly = set.getBool("clan_leader_only", false);
		_isOffensive = set.getBool("isOffensive", _skillType.isOffensive());
		_isPvpSkill = set.getBool("isPvpSkill", _skillType.isPvpSkill());
		isDebuff = set.getBool("isDebuff", _skillType.isDebuff());
		_isPvm = set.getBool("isPvm", _skillType.isPvM());
		_isForceUse = set.getBool("isForceUse", false);
		_isBehind = set.getBool("behind", false);
		_npcId = set.getInteger("npcId", 0);
		_flyType = FlyToLocationPacket.FlyType.valueOf(set.getString("fly_type", "NONE").toUpperCase());
		_flyDependsOnHeading = set.getBool("fly_depends_on_heading", false);
		_flySpeed = set.getInteger("fly_speed", 0);
		_flyDelay = set.getInteger("fly_delay", 0);
		_flyAnimationSpeed = set.getInteger("fly_animation_speed", 0);
		_flyRadius = set.getInteger("fly_radius", 200);
		_flyPositionDegree = set.getInteger("fly_position_degree", 0);
		_numCharges = set.getInteger("num_charges", 0);
		_condCharges = set.getInteger("cond_charges", 0);
		_skillInterrupt = set.getBool("skillInterrupt", false);
		_lethal1 = set.getDouble("lethal1", 0.0);
		_decreaseOnNoPole = set.getDouble("decreaseOnNoPole", 0.0);
		_increaseOnPole = set.getDouble("increaseOnPole", 0.0);
		_lethal2 = set.getDouble("lethal2", 0.0);
		_lethal2Addon = set.getDouble("lethal2DepensencyAddon", 0.0);
		_lethal2SkillDepencensyAddon = set.getInteger("lethal2SkillDepencensyAddon", 0);
		_lethal1Addon = set.getDouble("lethal1DepensencyAddon", 0.0);
		_isCancel = set.getBool("isCancel", false);
		_lethal1SkillDepencensyAddon = set.getInteger("lethal1SkillDepencensyAddon", 0);
		_absorbPart = set.getDouble("absorbPart", 0.0);
		_icon = set.getString("icon", "");
		_canUseWhileAbnormal = set.getBool("canUseWhileAbnormal", false);
		_abortable = set.getBool("is_abortable", true);
		_defenceIgnorePercent = set.getDouble("defence_ignore_percent", 0.0);
		AddedSkill[] addedSkills = AddedSkill.EMPTY_ARRAY;
		StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st.hasMoreTokens())
		{
			int id = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			if(level == -1)
				level = _level;
			addedSkills = (AddedSkill[]) ArrayUtils.add((Object[]) addedSkills, new AddedSkill(id, level));
		}
		_addedSkills = addedSkills;
		NextAction nextAction = NextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
		if(nextAction == NextAction.DEFAULT)
			switch(_skillType)
			{
				case SOWING:
				case DRAIN_SOUL:
				case LETHAL_SHOT:
				case PDAM:
				case CPDAM:
				case STUN:
				{
					_nextAction = NextAction.ATTACK;
					break;
				}
				default:
				{
					_nextAction = NextAction.NONE;
					break;
				}
			}
		else
			_nextAction = nextAction;
		_reuseSkillId = set.getInteger("reuse_skill_id", _id);
		_reuseHash = SkillUtils.generateSkillHashCode(_reuseSkillId, _level);
		_detectPcHide = set.getBool("detectPcHide", false);
		_hashCode = SkillUtils.generateSkillHashCode(_id, _level);
		_isSelfOffensive = set.getBool("is_self_offensive", _isOffensive);
		_applyEffectsOnSummon = set.getBool("apply_effects_on_summon", true);
		_applyEffectsOnPet = set.getBool("apply_effects_on_pet", true);
		_applyMinRange = set.getBool("applyMinRange", true);
		_masteryLevel = set.getInteger("masteryLevel", -1);
		_isItemSkill = set.getBool("is_item_skill", false);
		for(EffectUseType type : EffectUseType.VALUES)
			_effectTemplates.put(type.ordinal(), new ArrayList<>(0));
		_addSelfTarget = set.getBool("add_self_target", false);
		_percentDamageIfTargetDebuff = set.getDouble("percent_damage_if_target_debuff", 1.0);
		_noFlagNoForce = set.getBool("noFlag_noForce", false);
		_noEffectsIfFailSkill = set.getBool("no_effects_if_fail_skill", false);
		_renewal = set.getBool("renewal", true);
		_buffSlotType = set.getInteger("buff_slot_type", -2);

		_affectLimit = set.getIntegerArray("affectLimit", new int[2]);

		_removeEffectOnDeleteSkill = set.getBool("removeEffectOnDeleteSkill", false);

		_reuseDelayOnEquip = set.getLong("reuseDelayOnEquip", 0);
		_checkConditionSkillForZone = set.getBool(CHECK_CONDITION_SKILL_FOR_ZONE, false);
		_globalReuse = set.getBool("globalReuse", false);
		this.params = set;
	}

	public boolean isGlobalReuse() {
		return _globalReuse;
	}

	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return checkCondition(activeChar, target, forceUse, dontMove, first, true, null);
	}

	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, TriggerType triggerType)
	{
		Player player = activeChar.getPlayer();
		if(activeChar.isDead())
			return false;
		if(!isHandler() && activeChar.isMuted(this))
			return false;
		if(activeChar.isUnActiveSkill(_id))
			return false;
		if(target != null && activeChar.getReflection() != target.getReflection())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}
		if(triggerType == null && (player != null && player.isInZone(Zone.ZoneType.JUMPING) || target != null && target.isInZone(Zone.ZoneType.JUMPING)))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_SKILLS_IN_THE_CORRESPONDING_REGION);
			return false;
		}
		if(first && activeChar.isSkillDisabled(this))
		{
			if(sendMsg)
				activeChar.sendReuseMessage(this);
			return false;
		}

		double mpConsume;

		if(isMusic())
		{
			mpConsume = _mpConsume2;
			double inc2 = mpConsume / 2.0;

			double add2 = 0.0;
			for(Abnormal e2 : activeChar.getAbnormalList().getEffects())
				if(e2.getSkill().getId() != getId() && e2.getSkill().isMusic() && e2.getTimeLeft() > 30)
					add2 += inc2;

			mpConsume += add2;
			mpConsume = activeChar.calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume, activeChar, this);
		}
		else if(isMagic())
			mpConsume = _mpConsume1 + activeChar.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, _mpConsume2, target, this);
		else
			mpConsume = _mpConsume1 + activeChar.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, _mpConsume2, target, this);

		if(first && activeChar.getCurrentMp() < mpConsume)
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			return false;
		}
		if(activeChar.getCurrentHp() < _hpConsume + 1)
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.NOT_ENOUGH_HP);
			return false;
		}
		if(getFameConsume() > 0 && (player == null || player.getFame() < _fameConsume))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOU_DONT_HAVE_ENOUGH_REPUTATION_TO_DO_THAT);
			return false;
		}
		if(getClanRepConsume() > 0 && (player == null || player.getClan() == null || player.getClan().getReputationScore() < _cprConsume))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return false;
		}
		if(_soulsConsume > activeChar.getConsumedSouls())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SOULS);
			return false;
		}
		if(_targetType == SkillTargetType.TARGET_GROUND)
		{
			if(!activeChar.isPlayer())
				return false;
			if(player.getGroundSkillLoc() == null)
				return false;
		}
		if(isNotTargetAoE() && isOffensive() && activeChar.isInPeaceZone())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
			return false;
		}
		if(player != null)
		{
			if(player.isInFlyingTransform() && isHandler() && !flyingTransformUsage())
			{
				if(sendMsg)
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}
			if(!checkRideState(player.getMountType()))
			{
				if(sendMsg)
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}
			if(player.isInObserverMode())
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE);
				return false;
			}
			if(!activeChar.hasClubCard() || !org.apache.commons.lang3.ArrayUtils.contains(Constants.CLUB_CARD_NO_CONSUME_SKILL_IDS, getId()))
			{
				if(!isHandler() && activeChar.isPlayable() && first && getItemConsumeId() > 0 && getItemConsume() > 0L && ItemFunctions.getItemCount(isItemConsumeFromMaster() ? player : (Playable) activeChar, getItemConsumeId()) < getItemConsume()) {
					if((isItemConsumeFromMaster() || activeChar == player) && sendMsg)
						player.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return false;
				}
			}
			if(player.isFishing() && !altUse() && !activeChar.isServitor())
			{
				if(activeChar == player && triggerType == null)
					player.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
				return false;
			}
		}
		switch(getFlyType())
		{
			case WARP_BACK:
			case WARP_FORWARD:
			case CHARGE:
			case DUMMY:
			{
				if(activeChar.calcStat(Stats.BlockFly) == 1.0)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				break;
			}
		}
		if(getFlyType() != FlyToLocationPacket.FlyType.NONE && getId() != 628 && getId() != 821 && (activeChar.isImmobilized() || activeChar.isMoveBlocked()))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(first && target != null && getFlyType() == FlyToLocationPacket.FlyType.CHARGE && isApplyMinRange() && activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius())) && getTargetType() != SkillTargetType.TARGET_SELF && !activeChar.isServitor())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
			return false;
		}
		SystemMsg msg = checkTarget(activeChar, target, target, forceUse, first, triggerType);
		if(msg != null && player != null)
		{
			if(sendMsg)
				player.sendPacket(msg);
			return false;
		}
		if(_preCondition.length == 0)
			return true;

		if(first)
			for(Condition c : _preCondition)
				if(!c.test(activeChar, target, this, null, 0))
				{
					if(sendMsg)
					{
						SystemMsg cond_msg = c.getSystemMsg();
						if(cond_msg != null) {
							if(cond_msg.size() > 0)
								activeChar.sendPacket(new SystemMessagePacket(cond_msg).addSkillName(this));
							else
								activeChar.sendPacket(cond_msg);
						}
						else if(c.isCustomMessageLink()){
							activeChar.sendMessage(new CustomMessage(c.getCustomMessageLink()));
						}
					}
					return false;
				}
		return true;
	}

	public SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		return checkTarget(activeChar, target, aimingTarget, forceUse, first, null);
	}

	public SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first, TriggerType triggerType)
	{
		for(Event e : activeChar.getEvents()) {
			if(!e.canUseSkill(activeChar, target, this)) {
				return SystemMsg.INVALID_TARGET;
			}
		}
		if(target == activeChar && isNotTargetAoE() || target != null && activeChar.isMyServitor(target.getObjectId()) && _targetType == SkillTargetType.TARGET_SERVITOR_AURA)
			return null;
		if(target == null)
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(target == activeChar)
		{
			if(_targetType != SkillTargetType.TARGET_SELF && isOffensive())
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			return null;
		}
		if(isPvpSkill() && target.isPeaceNpc())
            return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(activeChar.getReflection() != target.getReflection())
            return SystemMsg.CANNOT_SEE_TARGET;
		if(triggerType == null && !first && target == aimingTarget && getCastRange() > 0 && !activeChar.isInRange(target.getLoc(), getCastRange() + (getCastRange() < 200 ? 400 : 500)))
            return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		if(_skillType == SkillType.TAKECASTLE || _skillType == SkillType.TAKEFORTRESS)
			return null;
		/*Label_0251:
        {
            if(!first && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL))
            {
				if (_isBehind) {
					if (!PositionUtils.isFacing(activeChar, target, 120))
						break Label_0251;
				} else if (PositionUtils.isInFrontOf(activeChar, target, 60))
					break Label_0251;
				return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
			}
        }*/
		// Конусообразные скиллы
		if (!first && target != activeChar && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL) && (_isBehind ? PositionUtils.isFacing(activeChar, target, 120) : !PositionUtils.isFacing(activeChar, target, 60)))
			return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		
		if((target.isDead() != _isCorpse && _targetType != SkillTargetType.TARGET_AREA_AIM_CORPSE || _isUndeadOnly && !target.isUndead()) && (!isAoE() || target != aimingTarget))
            return SystemMsg.INVALID_TARGET;

		if(altUse() || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST)
            return null;

		Player player = activeChar.getPlayer();
		if(player != null)
        {
            Player pcTarget = target.getPlayer();
            if(pcTarget != null)
            {
                if(isPvM())
                    return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                if(player.isInZone(Zone.ZoneType.epic) != pcTarget.isInZone(Zone.ZoneType.epic))
                    return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                if(pcTarget.isInOlympiadMode() && (!player.isInOlympiadMode() || player.getOlympiadGame() != pcTarget.getOlympiadGame()))
                    return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

                blesseds:
                {
                    if((getId() == 1045 || getId() == 1048) && pcTarget.getPvpFlag() != 0)
                    {
                        if(pcTarget.isInSameParty(player))
                            break blesseds;
                        if(pcTarget.isInSameClan(player))
                            break blesseds;
                        if(pcTarget.isInSameChannel(player))
                            break blesseds;

                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    }
                }
				if(!isOffensive()) {
					ActionPermissionComponent actionPermissionComponent = target.getActionPermissionComponent();
					ActionPermissionContext anyFailureContext = actionPermissionComponent.anyFailureContext(EActionPermissionLevel.None, IIncomingAttackPermission.class, activeChar, target, this);
					if(anyFailureContext.isSuccess()) {
						return anyFailureContext.getMessage() == null ? SystemMsg.INVALID_TARGET : anyFailureContext.getMessage();
					}
					if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IIncomingAttackPermission.class, activeChar, target, this)) {
						return null;
					}
				}

				if(isOffensive())
                {
                    if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
                        return SystemMsg.INVALID_TARGET;
                    if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcTarget.getOlympiadSide() && !forceUse)
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    if(pcTarget.isInNonPvpTime())
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    if(isAoE() && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying())) {
                    	if(target != aimingTarget) {
							return SystemMsg.CANNOT_SEE_TARGET;
						}
                    	else if(!activeChar.isDoCast()) {
							return SystemMsg.CANNOT_SEE_TARGET;
						}
					}
                    if(activeChar.isInZoneBattle() != target.isInZoneBattle())
                        return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
                    if(activeChar.isInPeaceZone() || target.isInPeaceZone())
                        return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;

                    SystemMsg msg;
                    for(Event e : activeChar.getEvents())
                        if((msg = e.checkForAttack(target, activeChar, this, forceUse)) != null)
                            return msg;

                    for(Event e : activeChar.getEvents()) {
						if(e.canAttack(target, activeChar, this, forceUse, false))
							return null;
					}

					ActionPermissionComponent actionPermissionComponent = player.getActionPermissionComponent();
					ActionPermissionContext anyFailureContext = actionPermissionComponent.anyFailureContext(EActionPermissionLevel.None, IAttackPermission.class, activeChar, target, this);
					if(anyFailureContext.isSuccess()) {
						return anyFailureContext.getMessage() == null ? SystemMsg.INVALID_TARGET : anyFailureContext.getMessage();
					}
					if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IAttackPermission.class, activeChar, target, this)) {
						actionPermissionComponent = target.getActionPermissionComponent();
						anyFailureContext = actionPermissionComponent.anyFailureContext(EActionPermissionLevel.None, IIncomingAttackPermission.class, activeChar, target, this);
						if(anyFailureContext.isSuccess()) {
							return anyFailureContext.getMessage() == null ? SystemMsg.INVALID_TARGET : anyFailureContext.getMessage();
						}
						if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IIncomingAttackPermission.class, activeChar, target, this)) {
							return null;
						}
						return null;
					}

					if(isProvoke())
                    {
						if(!forceUse && player.getParty() != null && player.getParty() == pcTarget.getParty()) {
							return SystemMsg.INVALID_TARGET;
						}

						if(pcTarget.isInOlympiadMode() && player.isInOlympiadMode() && player.getOlympiadGame() == pcTarget.getOlympiadGame()) {
							if(pcTarget.getOlympiadSide() == player.getOlympiadSide()) {
								return SystemMsg.INVALID_TARGET;
							}
						}
						else {
							if(!player.getFraction().canAttack(pcTarget.getFraction())) {
								return SystemMsg.INVALID_TARGET;
							}
						}

                        return null;
                    }

					if(pcTarget.isInOlympiadMode() && player.isInOlympiadMode() && player.getOlympiadGame() == pcTarget.getOlympiadGame())
						return null;

					if((isPvpSkill() || !forceUse || isAoE()))
					{
						if(player == pcTarget)
							return SystemMsg.INVALID_TARGET;

						if(pcTarget.isInSameParty(player)) {
							return SystemMsg.INVALID_TARGET;
						}

						if(player.isInParty() && player.getParty().getCommandChannel() != null && pcTarget.isInParty() && pcTarget.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel())
							return SystemMsg.INVALID_TARGET;

						if(pcTarget.isInSameClan(player)) {
							return SystemMsg.INVALID_TARGET;
						}

						if(player.getClan() != null && player.getClan().getAlliance() != null && pcTarget.getClan() != null && pcTarget.getClan().getAlliance() != null && player.getClan().getAlliance() == pcTarget.getClan().getAlliance())
							return SystemMsg.INVALID_TARGET;
					}

					if(activeChar.isInSiegeZone() && target.isInSiegeZone())
					{
						if(player.isInSameClan(pcTarget))
							return SystemMsg.INVALID_TARGET;

						if(!player.getFraction().canAttack(pcTarget.getFraction()))
							return SystemMsg.INVALID_TARGET;

						return null;
					}
					if(pcTarget.getPvpFlag() != 0) {
						if(pcTarget.isInSameClan(player)) {
							return SystemMsg.INVALID_TARGET;
						}
						if(pcTarget.isInSameChannel(player)) {
							return SystemMsg.INVALID_TARGET;
						}
						if(pcTarget.isInSameParty(player)) {
							return SystemMsg.INVALID_TARGET;
						}
						if(pcTarget.isInSameAlly(player)){
							return SystemMsg.INVALID_TARGET;
						}
						return null;
					}
					if(player.atMutualWarWith(pcTarget))
						return null;
					if(isForceUse())
						return null;
					if(pcTarget.isPK())
						return null;
					if(activeChar.isInZoneBattle() && pcTarget.isInZoneBattle())
						return null;
					if(activeChar.getFraction().canAttack(target.getFraction()))
						return null;
					if(forceUse && !isPvpSkill() && (!isAoE() || aimingTarget == target)) {
						if((activeChar.getFraction().canAttack(target.getFraction()) || activeChar.isMyServitor(target.getObjectId()))) {
							return null;
						}
/*						if(player.getParty() != null && pcTarget.getParty() == player.getParty()) {
							switch(getSkillType()) {
								case MDAM:
								case PDAM:
								case CPDAM:
								case LETHAL_SHOT:
								case DRAIN:
									return null;
							}
						}*/
					}
					return SystemMsg.INVALID_TARGET;
				}
                else
                {
                    if(player.isInOlympiadMode() && !forceUse && player.getOlympiadSide() != pcTarget.getOlympiadSide())
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() != TeamType.NONE && player.getTeam() != pcTarget.getTeam())
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    if(player.getFraction().canAttack(pcTarget.getFraction()))
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    if(!activeChar.isInZoneBattle() && target.isInZoneBattle())
                        return SystemMsg.INVALID_TARGET;
                    if(forceUse || isForceUse())
                        return null;
                    if(player.getParty() != null && player.getParty() == pcTarget.getParty())
                        return null;
                    if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
                        return null;
                    if(player.atMutualWarWith(pcTarget))
                        return SystemMsg.INVALID_TARGET;
                    if(pcTarget.getPvpFlag() != 0)
                        return SystemMsg.INVALID_TARGET;
                    if(pcTarget.isPK())
                        return SystemMsg.INVALID_TARGET;
                    if(activeChar.getFraction().canAttack(target.getFraction()))
                        return SystemMsg.INVALID_TARGET;
					return null;
                }
            }
        }

		if((!activeChar.isDoCast() || activeChar.isDoCast()) && (triggerType == null || target != aimingTarget) && isAoE() && isOffensive() && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
            return SystemMsg.CANNOT_SEE_TARGET;

		if(!forceUse && !isForceUse() && !isNoFlagNoForce())
        {
        	boolean autoAttackable = target.isAutoAttackable(activeChar);
            if(!isOffensive() && autoAttackable)
                return SystemMsg.INVALID_TARGET;
            if(isOffensive() && !autoAttackable)
                return SystemMsg.INVALID_TARGET;
        }

		if(isOffensive() && !target.isAttackable(activeChar))
            return SystemMsg.INVALID_TARGET;

        if(!isOffensive())
            if(target.isGuard() || target.isNpc() && target.isAutoAttackable(activeChar))
                return SystemMsg.INVALID_TARGET;

		return null;
	}

	public final Creature getAimingTarget(Creature activeChar, GameObject obj)
	{
		Creature target = obj == null || !obj.isCreature() ? null : (Creature) obj;
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_CLAN_ONLY:
			case TARGET_SELF:
			case TARGET_FACTION: {
				return activeChar;
			}
			case TARGET_AURA:
			case TARGET_COMMCHANNEL:
			case TARGET_MULTIFACE_AURA:
			case TARGET_GROUND:
			{
				return activeChar;
			}
			case TARGET_HOLY:
			{
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			}
			case TARGET_FLAGPOLE:
			{
				if((obj instanceof StaticObjectInstance) && ((StaticObjectInstance) obj).getType() == 3)
					return activeChar;
				else if(target != null && activeChar.isPlayer() && target.isArtefact())
					return target;
				else if(target != null && activeChar.isPlayer() && target instanceof ArtifactInstance)
					return target;
				else if(target != null && activeChar.isPlayer() && target instanceof UpgradingArtifactInstance)
					return target;
				else
					return null;
			}
			case TARGET_UNLOCKABLE:
			{
				return target != null && target.isDoor() || target instanceof ChestInstance ? target : null;
			}
			case TARGET_CHEST:
			{
				return target instanceof ChestInstance ? target : null;
			}
			case TARGET_SERVITORS:
			case TARGET_SUMMONS:
			case TARGET_SELF_AND_SUMMONS:
			{
				return activeChar;
			}
			case TARGET_ONE_SERVITOR:
			case TARGET_SERVITOR_AURA:
			{
				return target != null && target.isServitor() && activeChar.isMyServitor(target.getObjectId()) && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_ONE_SERVITOR_NO_TARGET:
			{
				target = activeChar.getPlayer().getAnyServitor();
				return target != null && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_ONE_SUMMON:
			{
				return target != null && target.isSummon() && activeChar.isMyServitor(target.getObjectId()) && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_ONE_SUMMON_NO_TARGET:
			{
				target = activeChar.getPlayer().getAnySummon();
				return target != null && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_PET:
			{
				target = activeChar.isPlayer() ? activeChar.getPlayer().getPet() : null;
				return target != null && target.isPet() && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_OWNER:
			{
				if(activeChar.isServitor())
				{
					target = activeChar.getPlayer();
					return target != null && target.isDead() == _isCorpse ? target : null;
				}
				return null;
			}
			case TARGET_ENEMY_PET:
			{
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isPet())
					return null;
				return target;
			}
			case TARGET_ENEMY_SUMMON:
			{
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isSummon())
					return null;
				return target;
			}
			case TARGET_ENEMY_SERVITOR:
			{
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isServitor())
					return null;
				return target;
			}
			case TARGET_ONE:
			{
				return target != null && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_CLAN_ONE:
			{
				if(target == null)
					return null;
				Player cplayer = activeChar.getPlayer();
				Player cptarget = target.getPlayer();
				if(cptarget != null && cptarget == activeChar)
					return target;

				if(!(cplayer == null || !cplayer.isInOlympiadMode() || cptarget == null || cplayer.getOlympiadSide() != cptarget.getOlympiadSide() || cplayer.getOlympiadGame() != cptarget.getOlympiadGame() || target.isDead() != _isCorpse || target == activeChar && isOffensive() || _isUndeadOnly && !target.isUndead()))
					return target;

				if(cptarget != null && cplayer != null && cplayer.getClan() != null && cplayer.isInSameClan(cptarget) && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_PARTY_ONE:
			{
				if(target == null)
					return null;
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				if(ptarget != null && ptarget == activeChar)
					return target;

				if(!(player == null || !player.isInOlympiadMode() || ptarget == null || player.getOlympiadSide() != ptarget.getOlympiadSide() || player.getOlympiadGame() != ptarget.getOlympiadGame() || target.isDead() != _isCorpse || target == activeChar && isOffensive() || _isUndeadOnly && !target.isUndead()))
					return target;

				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_PARTY_ONE_WITHOUT_ME:
			{
				if(target == null)
					return null;
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				if(ptarget != null && ptarget == activeChar)
					return null;

				if(!(player == null || !player.isInOlympiadMode() || ptarget == null || player.getOlympiadSide() != ptarget.getOlympiadSide() || player.getOlympiadGame() != ptarget.getOlympiadGame() || target.isDead() != _isCorpse || target == activeChar && isOffensive() || _isUndeadOnly && !target.isUndead()))
					return target;

				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				return target != null && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				return target != null && target.isDead() ? target : null;
			}
			case TARGET_CORPSE:
			{
				if(target == null || !target.isDead())
					return null;
				if(target.isSummon() && !activeChar.isMyServitor(target.getObjectId()))
					return target;
				return target.isNpc() ? target : null;
			}
			case TARGET_CORPSE_PLAYER:
			{
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			}
			case TARGET_SIEGE:
			{
				return target != null && !target.isDead() && target.isDoor() ? target : null;
			}
			default:
			{
				activeChar.sendMessage("Target type of skill is not currently handled");
				return null;
			}
		}
	}

	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		if(oneTarget())
		{
			if(_addSelfTarget)
                return List.of(aimingTarget, activeChar);
			else
                return List.of(aimingTarget);
		}
		List<Creature> targets = new ArrayList<>(2);
		if(_addSelfTarget)
			targets.add(activeChar);
		switch(_targetType)
		{
			case TARGET_SELF_AND_SUMMONS:
			{
				targets.add(activeChar);
				if(!activeChar.isPlayer())
					break;
				for(Servitor servitor : activeChar.getPlayer().getSummons())
					if(activeChar.isInRange(servitor, getAffectRange()))
						targets.add(servitor);
				break;
			}
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			case TARGET_AREA_AIM_CORPSE:
			{
				if(aimingTarget.isDead() == _isCorpse && (!_isUndeadOnly || aimingTarget.isUndead()))
					targets.add(aimingTarget);
				addTargetsToList(targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_GROUND:
			{
				addTargetsToList(targets, activeChar, activeChar, forceUse);
				break;
			}
			case TARGET_COMMCHANNEL:
			{
				if(activeChar.getPlayer() == null)
					break;
				if(!activeChar.getPlayer().isInParty())
				{
					targets.add(activeChar);
					addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
					break;
				}
				if(activeChar.getPlayer().getParty().isInCommandChannel())
				{
					for(Player p : activeChar.getPlayer().getParty().getCommandChannel())
						if(!p.isDead() && (getAffectRange() == -1 || p.isInRange(activeChar, getAffectRange() == 0 ? 600L : (long) getAffectRange())))
							targets.add(p);
					addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
					break;
				}
				for(Player p : activeChar.getPlayer().getParty().getPartyMembers())
					if(!p.isDead() && p.isInRange(activeChar, getAffectRange() == -1 || getAffectRange() == 0 ? 600 : getAffectRange()))
						targets.add(p);
				addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
				break;
			}
			case TARGET_SERVITORS:
			{
				for(Servitor servitor : activeChar.getServitors())
					if(activeChar.isInRange(servitor, getAffectRange()))
						targets.add(servitor);
				break;
			}
			case TARGET_SUMMONS:
			{
				if(!activeChar.isPlayer())
					break;
				for(Servitor servitor : activeChar.getPlayer().getSummons())
					if(activeChar.isInRange(servitor, getAffectRange()))
						targets.add(servitor);
				break;
			}
			case TARGET_SERVITOR_AURA:
			{
				addTargetsToList(targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_CLAN_ONLY:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					if(_targetType != SkillTargetType.TARGET_PARTY_WITHOUT_ME)
						targets.add(activeChar);
					for(Creature c : World.getAroundCharacters(activeChar, getAffectRange(), 600))
						if(!c.isDead() && (c.isMonster() || c.isSiegeGuard()))
							targets.add(c);
					break;
				}
				Player player = activeChar.getPlayer();
				if(player == null)
					break;
				for(Player target : World.getAroundPlayers(activeChar, getAffectRange(), 600))
				{
					boolean check = false;
					switch(_targetType)
					{
						case TARGET_PARTY:
						case TARGET_PARTY_WITHOUT_ME:
						{
							check = player.getParty() != null && player.getParty() == target.getParty();
							break;
						}
						case TARGET_CLAN:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getParty() != null && target.getParty() == player.getParty();
							break;
						}
						case TARGET_CLAN_ONLY:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId();
							break;
						}
						case TARGET_ALLY:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getAllyId() != 0 && target.getAllyId() == player.getAllyId();
							break;
						}
					}
					if(!check)
						continue;
					if(checkTarget(player, target, aimingTarget, forceUse, false) != null)
						continue;
					if(player.isInOlympiadMode() && target.isInOlympiadMode() && player.getOlympiadSide() != target.getOlympiadSide())
						continue;

					addTargetAndPetToList(targets, activeChar, target);
				}
				addTargetAndPetToList(targets, activeChar, player);
				break;
			}
			case TARGET_FACTION:{
				World.getAroundCharacters(activeChar, getAffectRange(), 600).
						stream().
						filter(p-> p.getFraction() == activeChar.getFraction()).
						filter(p-> p.isNpc() || ((activeChar.isPlayable() && p.isPlayable()) && ((!activeChar.getPlayer().isInOlympiadMode() && !p.getPlayer().isInOlympiadMode()) || ((activeChar.getPlayer().isInOlympiadMode() && p.getPlayer().isInOlympiadMode()) && activeChar.getPlayer().getOlympiadSide() == p.getPlayer().getOlympiadSide())))).
						//filter(p-> checkTarget(activeChar, p, aimingTarget, forceUse, false) == null).
						forEach(p-> addTargetAndPetToList(targets, activeChar, p));
				addTargetAndPetToList(targets, activeChar, activeChar);
				break;
			}
		}
		return targets;
	}

	private void addTargetAndPetToList(List<Creature> targets, Creature actor, Creature target)
	{
		if((actor == target || getAffectRange() == -1 || actor.isInRange(target, getAffectRange())) && target.isDead() == _isCorpse)
			targets.add(target);
		for(Servitor servitor : target.getServitors())
			if((getAffectRange() == -1 || actor.isInRange(servitor, getAffectRange())) && servitor.isDead() == _isCorpse)
				targets.add(servitor);
	}

	private void addTargetsToList(List<Creature> targets, Creature aimingTarget, Creature activeChar, boolean forceUse)
	{
		List<Creature> arround = aimingTarget.getAroundCharacters(getAffectRange(), 300);
		Polygon terr = null;
		if(_targetType == SkillTargetType.TARGET_TUNNEL)
		{
			int radius = 100;
			int zmin1 = activeChar.getZ() - 200;
			int zmax1 = activeChar.getZ() + 200;
			int zmin2 = aimingTarget.getZ() - 200;
			int zmax2 = aimingTarget.getZ() + 200;
			double angle = PositionUtils.convertHeadingToDegree(activeChar.getHeading());
			double radian1 = Math.toRadians(angle - 90.0);
			double radian2 = Math.toRadians(angle + 90.0);
			terr = new PolygonBuilder()
                    .add(activeChar.getX() + (int) (Math.cos(radian1) * radius), activeChar.getY() + (int) (Math.sin(radian1) * radius))
                    .add(activeChar.getX() + (int) (Math.cos(radian2) * radius), activeChar.getY() + (int) (Math.sin(radian2) * radius))
                    .add(aimingTarget.getX() + (int) (Math.cos(radian2) * radius), aimingTarget.getY() + (int) (Math.sin(radian2) * radius))
                    .add(aimingTarget.getX() + (int) (Math.cos(radian1) * radius), aimingTarget.getY() + (int) (Math.sin(radian1) * radius))
                    .setZmin(Math.min(zmin1, zmin2))
                    .setZmax(Math.max(zmax1, zmax2))
                    .createPolygon();
		}
		else if(_targetType == SkillTargetType.TARGET_GROUND)
		{
			if(!activeChar.isPlayer())
				return;
			Location loc = activeChar.getPlayer().getGroundSkillLoc();
			if(loc == null)
				return;
			arround = World.getAroundCharacters(loc, aimingTarget.getObjectId(), aimingTarget.getReflectionId(), getAffectRange(), 300);
		}
		else if(_targetType == SkillTargetType.TARGET_AREA && getBehindRadius() > 0)
		{
			int zmin3 = activeChar.getZ() - 200;
			int zmax3 = activeChar.getZ() + 200;
			int zmin4 = aimingTarget.getZ() - 200;
			int zmax4 = aimingTarget.getZ() + 200;
			double radian3 = PositionUtils.convertHeadingToDegree(activeChar.getHeading()) + getBehindRadius() / 2;
			if(radian3 > 360.0)
				radian3 -= 360.0;
			radian3 = 3.141592653589793 * radian3 / 180.0;
			int x1 = aimingTarget.getX() + (int) (Math.cos(radian3) * getAffectRange());
			int y1 = aimingTarget.getY() + (int) (Math.sin(radian3) * getAffectRange());
			radian3 = PositionUtils.convertHeadingToDegree(activeChar.getHeading()) - getBehindRadius() / 2;
			if(radian3 > 360.0)
				radian3 -= 360.0;
			radian3 = 3.141592653589793 * radian3 / 180.0;
			int x2 = aimingTarget.getX() + (int) (Math.cos(radian3) * getAffectRange());
			int y2 = aimingTarget.getY() + (int) (Math.sin(radian3) * getAffectRange());
			terr = new PolygonBuilder()
                    .add(aimingTarget.getX(), aimingTarget.getY())
                    .add(x1, y1)
                    .add(x2, y2)
                    .setZmin(Math.min(zmin3, zmin4))
                    .setZmax(Math.max(zmax3, zmax4))
                    .createPolygon();
		}

		int affectLimit = getAffectLimit();
		AtomicInteger affected = new AtomicInteger(0);

		for(Creature target : arround)
		{
			if(terr != null && !terr.isInside(target.getX(), target.getY(), target.getZ()))
				continue;
			if(target == null || activeChar == target)
				continue;
			if(activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer())
				continue;
			if(getId() == 933 || getId() == 10785)
				target.checkAndRemoveInvisible();

            // DS: всегда сбрасываем флаг forceUse при проверке целей AoE.
            // Нужно для того чтобы не бить гуардов даже с контролом (кроме ПК)
            // и для того чтобы Sublime Self-Sacrifice не работал на мобах и флагнутых варах.
			if(checkTarget(activeChar, target, aimingTarget, false, false) != null)
				continue;

			targets.add(target);

			if(isOffensive() && (affectLimit > 0 && affected.incrementAndGet() > affectLimit) && !activeChar.isRaid())
				break;
		}
	}

	public boolean calcCriticalBlow(Creature caster, Creature target)
	{
		return false;
	}

	private final boolean calcEffectsSuccess(Creature effector, Creature effected, boolean showMsg)
	{
		int chance = getActivateRate();
		if(chance >= 0 && !Formulas.calcSkillSuccess(effector, effected, this, chance))
		{
			if(showMsg)
			{
				ExMagicAttackInfo.packet(effector, effected, MagicAttackType.RESISTED);
				effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(this));
			}
			return false;
		}
		if(effected.calcStat(Stats.MarkOfTrick) == 1.0 && Rnd.chance(20))
		{
			if(showMsg)
			{
				ExMagicAttackInfo.packet(effector, effected, MagicAttackType.RESISTED);
				effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(this));
			}
			return false;
		}
		return true;
	}

	public final boolean getEffects(Creature effector, Creature effected)
	{
		double timeMult = 1.0;
		if(isMusic())
			timeMult = Config.SONGDANCETIME_MODIFIER;
		else if(getId() >= 4342 && getId() <= 4360)
			timeMult = Config.CLANHALL_BUFFTIME_MODIFIER;
		else if(Config.BUFFTIME_MODIFIER_SKILLS.length > 0)
			for(int i : Config.BUFFTIME_MODIFIER_SKILLS)
				if(i == getId())
					timeMult = Config.BUFFTIME_MODIFIER;
		return getEffects(effector, effected, 0, timeMult);
	}

	public final boolean getEffects(Creature effector, Creature effected, int timeConst, double timeMult)
	{
		return getEffects(effector, effected, EffectUseType.NORMAL, timeConst, timeMult);
	}

	private final boolean getEffects(Creature effector, Creature effected, EffectUseType useType,
									 int timeConst, double timeMult)
	{
		if(isPassive() || effector == null)
			return false;
		if(!isToggle())
		{}
		if(!hasEffects(useType))
			return true;
		if(effected == null || effected.isDoor() || effected.isDead() && !isPreservedOnDeath())
			return false;
		if(effector != effected && useType == EffectUseType.NORMAL && effected.isEffectImmune(effector))
			return false;
		boolean reflected = false;
		if(useType == EffectUseType.NORMAL)
			reflected = effected.checkReflectDebuff(effector, this);
		List<Creature> targets = new ArrayList<>(1);
		if(useType == EffectUseType.SELF)
			targets.add(effector);
		else if(reflected)
			targets.add(effector);
		else
			targets.add(effected);
		if(useType == EffectUseType.NORMAL && (applyEffectsOnSummon() || applyEffectsOnPet()) && !isOffensive() && !isToggle() && !isCubicSkill())
		{
			Creature owner;
			if(reflected)
				owner = effector;
			else
				owner = effected;
			if(owner.isPlayer())
				for(Servitor servitor : owner.getPlayer().getServitors())
					if(applyEffectsOnSummon() && servitor.isSummon())
						targets.add(servitor);
					else
					{
						if(!applyEffectsOnPet() || !servitor.isPet())
							continue;
						targets.add(servitor);
					}
		}
		boolean successOnEffected = false;
		List<EffectTemplate> effectTemplates = getEffectTemplates(useType);
		for(Creature target : targets)
		{
			if(target.isDead() && !isPreservedOnDeath())
				continue;
			if(effector != target && useType == EffectUseType.NORMAL && target.isEffectImmune(effector))
				continue;
			boolean showMsg = false;
			boolean resistedDebuff = false;
			boolean resistedBuff = false;
			boolean success = false;
			for(EffectTemplate et : effectTemplates)
			{
				if(et.isInstant())
					continue;
				if(target.isRaid() && et.getEffectType().isRaidImmune())
					continue;
				if(et.getChance() >= 0 && !Rnd.chance(et.getChance()))
					continue;
				if(effector != target && ((target.isBuffImmune() || resistedBuff) && !isOffensive() || (target.isDebuffImmune() || resistedDebuff) && isOffensive()))
				{
					if(!resistedDebuff)
						for(Abnormal effect : target.getAbnormalList().getEffects())
							if(effect.checkDebuffImmunity())
								break;
					if(isOffensive())
						resistedDebuff = true;
					else
						resistedBuff = true;
				}
				else
				{
					//Env env = new Env(effector, target, this);
					//if(success)
					//	env.value = 2.147483647E9;
					double abnormalTimeModifier = Math.max(1.0, timeMult);
					if(!isToggle() && !isCubicSkill())
						abnormalTimeModifier *= target.calcStat(isOffensive() ? Stats.DEBUFF_TIME_MODIFIER : Stats.BUFF_TIME_MODIFIER, null, null);

					Abnormal e = et.getEffect(effector, target, this);
					if(e == null)
						continue;

					if(et.getChance() > 0)
						success = true;
					int duration = e.getDuration();
					if(timeConst > 0)
						duration = timeConst / 1000;
					else if(abnormalTimeModifier > 1.0)
						duration *= (int) abnormalTimeModifier;
					e.setDuration(duration);
					if(!e.schedule())
						continue;
					if(!showMsg && !e.isHidden())
						showMsg = true;
					if(target != effected)
						continue;
					successOnEffected = true;
				}
			}
			if(showMsg && !isHideStartMessage())
				target.sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(getDisplayId(), getDisplayLevel()));
			if(target != effected)
				continue;
			if(resistedDebuff || resistedBuff)
			{
				ExMagicAttackInfo.packet(effector, effected, MagicAttackType.RESISTED);
				effector.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(target).addSkillName(this));
			}
			if(!reflected)
				continue;
			target.sendPacket(new SystemMessage(1998).addName(effector));
			effector.sendPacket(new SystemMessage(1999).addName(target));
		}
		if(successOnEffected && isOffensive()) {
			if(!effected.isInCombat()) {
				effected.startAttackStanceTask();
			}
			if(!effector.isInCombat()){
				effector.startAttackStanceTask();
			}
		}
		return successOnEffected;
	}

	public final void attachEffect(EffectTemplate effect)
	{
		if(effect == null)
			return;
		_effectTemplates.get(effect.getUseType().ordinal()).add(effect);
	}

	public List<EffectTemplate> getEffectTemplates(EffectUseType useType)
	{
		return _effectTemplates.get(useType.ordinal());
	}

	public int getEffectsCount(EffectUseType useType)
	{
		return getEffectTemplates(useType).size();
	}

	public boolean hasEffects(EffectUseType useType)
	{
		return getEffectsCount(useType) > 0;
	}

	public boolean hasEffect(EffectUseType useType, EffectType type)
	{
		List<EffectTemplate> templates = getEffectTemplates(useType);
		for(EffectTemplate et : templates)
			if(et.getEffectType() == type)
				return true;
		return false;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj != null && getClass() == obj.getClass() && hashCode() == obj.hashCode();
	}

	public int getReuseSkillId()
	{
		return _reuseSkillId;
	}

	public int getReuseHash()
	{
		return _reuseHash;
	}

	@Override
	public int hashCode()
	{
		return _hashCode;
	}

	public final void attachCondition(Condition c)
	{
		_preCondition = (Condition[]) ArrayUtils.add((Object[]) _preCondition, c);
	}

	public final boolean altUse()
	{
		return (_altUse || _isItemHandler) && _hitTime <= 0;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills;
	}

	public final int getCastRange()
	{
		return _castRange;
	}

	public final int getAOECastRange()
	{
		return Math.max(getCastRange(), getAffectRange());
	}

	public int getCondCharges()
	{
		return _condCharges;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public boolean getCorpse()
	{
		return _isCorpse;
	}

	public final int getDisplayId()
	{
		return _displayId;
	}

	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	public int getEffectPoint()
	{
		return _effectPoint;
	}

	public Abnormal getSameByAbnormalType(Collection<Abnormal> list)
	{
		for(EffectTemplate et : getEffectTemplates(EffectUseType.NORMAL))
		{
			Abnormal ret;
			if(et != null && (ret = et.getSameByAbnormalType(list)) != null)
				return ret;
		}
		for(EffectTemplate et : getEffectTemplates(EffectUseType.SELF))
		{
			Abnormal ret;
			if(et != null && (ret = et.getSameByAbnormalType(list)) != null)
				return ret;
		}
		return null;
	}

	public Abnormal getSameByAbnormalType(AbnormalList list)
	{
		return getSameByAbnormalType(list.getEffects());
	}

	public Abnormal getSameByAbnormalType(Creature actor)
	{
		return getSameByAbnormalType(actor.getAbnormalList().getEffects());
	}

	public final Element[] getElements()
	{
		return _elements;
	}

	public final int getElementsPower()
	{
		return _elementsPower;
	}

	public SkillEntry getFirstAddedSkill()
	{
		if(_addedSkills.length == 0)
			return null;
		return _addedSkills[0].getSkill();
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}

	public int getFlyPositionDegree()
	{
		return _flyPositionDegree;
	}

	public FlyToLocationPacket.FlyType getFlyType()
	{
		return _flyType;
	}

	public boolean isFlyDependsOnHeading()
	{
		return _flyDependsOnHeading;
	}

	public int getFlySpeed()
	{
		return _flySpeed;
	}

	public int getFlyDelay()
	{
		return _flyDelay;
	}

	public int getFlyAnimationSpeed()
	{
		return _flyAnimationSpeed;
	}

	public final int getHitTime()
	{
		if(_hitTime < Config.MIN_HIT_TIME)
			return Config.MIN_HIT_TIME;
		return _hitTime;
	}

	public final int getHpConsume()
	{
		return _hpConsume;
	}

	public final int getId()
	{
		return _id;
	}

	public final long getItemConsume()
	{
		return _itemConsume;
	}

	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public final boolean isItemConsumeFromMaster()
	{
		return _itemConsumeFromMaster;
	}

	public final int getReferenceItemId()
	{
		return _referenceItemId;
	}

	public final int getReferenceItemMpConsume()
	{
		return _referenceItemMpConsume;
	}

	public final int getLevel()
	{
		return _level;
	}

	public final int getMaxLevel()
	{
		return _maxLevel;
	}

	public final int getLevelModifier()
	{
		return _levelModifier;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public int getMatak()
	{
		return _matak;
	}

	public PledgeRank getMinPledgeRank()
	{
		return _minPledgeRank;
	}

	public boolean clanLeaderOnly()
	{
		return _clanLeaderOnly;
	}

	public final double getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}

	public final double getMpConsume1()
	{
		return _mpConsume1;
	}

	public final double getMpConsume2()
	{
		return _mpConsume2;
	}

	public final double getMpConsumeTick()
	{
		return _mpConsumeTick;
	}

	public final String getName()
	{
		return _name;
	}

	public final String getName(Player player)
	{
		final SkillName skillName = SkillNameHolder.getInstance().getSkillName(player, this);
		return skillName == null ? _name : skillName.getName();
	}

	public final SkillName getSkillName(Player player)
	{
		return SkillNameHolder.getInstance().getSkillName(player, this);
	}

	public final NextAction getNextAction()
	{
		return _nextAction;
	}

	public final int getNpcId()
	{
		return _npcId;
	}

	public final int getNumCharges()
	{
		return _numCharges;
	}

	public final double getPower(Creature target)
	{
		if(target != null)
		{
			if(target.isPlayable())
				return getPowerPvP();
			if(target.isMonster())
				return getPowerPvE();
		}
		return getPower();
	}

	public final double getPower()
	{
		return _power;
	}

	public final double getPowerPvP()
	{
		return _powerPvP != 0.0 ? _powerPvP : _power;
	}

	public final double getPowerPvE()
	{
		return _powerPvE != 0.0 ? _powerPvE : _power;
	}

	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

    public int getOlympiadReuseDelay()
    {
        return olympiadReuseDelay;
    }

    public final boolean isIgnoreFails()
	{
		return _isIgnoreFails || _skillType == SkillType.DRAIN;
	}

	public final boolean getShieldIgnore()
	{
		return _isShieldignore;
	}

	public final double getShieldIgnorePercent()
	{
		return _shieldIgnorePercent;
	}

	public final boolean isReflectable()
	{
		return _isReflectable;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getAffectRange()
	{
		return _affectRange;
	}

	public final SkillType getSkillType()
	{
		return _skillType;
	}

	public final int getSoulsConsume()
	{
		return _soulsConsume;
	}

	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final SkillTrait getTraitType()
	{
		return _traitType;
	}

	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}

	public final boolean isDispelOnDamage()
	{
		return _dispelOnDamage;
	}

	public double getLethal1(Creature self)
	{
		return _lethal1 + getAddedLethal1(self);
	}

	public double getIncreaseOnPole()
	{
		return _increaseOnPole;
	}

	public double getDecreaseOnNoPole()
	{
		return _decreaseOnNoPole;
	}

	public boolean isCancelSkill()
	{
		return _isCancel;
	}

	public boolean isDetectPC()
	{
		return _detectPcHide;
	}

	public double getLethal2(Creature self)
	{
		return _lethal2 + getAddedLethal2(self);
	}

	private double getAddedLethal2(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return 0.0;
		if(_lethal2Addon == 0.0 || _lethal2SkillDepencensyAddon == 0)
			return 0.0;
		if(player.getAbnormalList().containsEffects(_lethal2SkillDepencensyAddon))
			return _lethal2Addon;
		return 0.0;
	}

	private double getAddedLethal1(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return 0.0;
		if(_lethal1Addon == 0.0 || _lethal1SkillDepencensyAddon == 0)
			return 0.0;
		if(player.getAbnormalList().containsEffects(_lethal1SkillDepencensyAddon))
			return _lethal1Addon;
		return 0.0;
	}

	public String getBaseValues()
	{
		return _baseValues;
	}

	public final boolean isCancelable()
	{
		return _isCancelable && _isSelfDispellable && !hasEffect(EffectUseType.NORMAL, EffectType.Transformation) && !isToggle();
	}

	public final boolean isSelfDispellable()
	{
		return _isSelfDispellable && !hasEffect(EffectUseType.NORMAL, EffectType.Transformation) && !isToggle() && !isOffensive() && !isMusic();
	}

	public final int getCriticalRate()
	{
		return _criticalRate;
	}

	public final double getCriticalRateMod()
	{
		return _criticalRateMod;
	}

	public final boolean isHandler()
	{
		return _isItemHandler;
	}

	public final boolean isMagic()
	{
		return _magicType == SkillMagicType.MAGIC || _magicType == SkillMagicType.SPECIAL || _magicType == SkillMagicType.AWAKED_BUFF;
	}

	public final boolean isPhysic()
	{
		return _magicType == SkillMagicType.UNK_MAG_TYPE_21 || _magicType == SkillMagicType.PHYSIC || _magicType == SkillMagicType.MUSIC || _magicType == SkillMagicType.ITEM;
	}

	public final boolean isSpecial()
	{
		return _magicType == SkillMagicType.SPECIAL;
	}

	public final boolean isMusic()
	{
		return _magicType == SkillMagicType.MUSIC;
	}

	public final SkillMagicType getMagicType()
	{
		return _magicType;
	}

	public final boolean isNewbie()
	{
		return _isNewbie;
	}

	public final boolean isPreservedOnDeath()
	{
		return _isPreservedOnDeath;
	}

	public final boolean isOverhit()
	{
		return _isOverhit;
	}

	public boolean isSaveable()
	{
		return (Config.ALT_SAVE_UNSAVEABLE || !isMusic() && !_name.startsWith("Herb of")) && _isSaveable;
	}

	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || isHandler() || _name.contains("Talisman");
	}

	public final boolean isReuseDelayPermanent()
	{
		return _isReuseDelayPermanent || isHandler();
	}

	public boolean isDeathlink()
	{
		return _deathlink;
	}

	public boolean isBasedOnTargetDebuff()
	{
		return _basedOnTargetDebuff;
	}

	public boolean isSoulBoost()
	{
		return _isSoulBoost;
	}

	public boolean isChargeBoost()
	{
		return _isChargeBoost;
	}

	public boolean isBehind()
	{
		return _isBehind;
	}

	public boolean isHideStartMessage()
	{
		return _hideStartMessage;
	}

	public boolean isHideUseMessage()
	{
		return _hideUseMessage;
	}

	public boolean isSSPossible()
	{
		if(isMagic() || isPhysic())
		{
			if(_isUseSS == Ternary.TRUE)
				return true;
			if(_isUseSS == Ternary.DEFAULT)
			{
				if(isHandler())
					return false;
				if(isMusic())
					return false;
				if(!isActive())
					return false;
				if(getTargetType() == SkillTargetType.TARGET_SELF && !isMagic())
					return false;
				if(!isPhysic())
					return true;
				if(getSkillType() == SkillType.CHARGE)
					return true;
				if(getSkillType() == SkillType.CHARGE_SOUL)
					return true;
				if(getSkillType() == SkillType.DRAIN)
					return true;
				if(getSkillType() == SkillType.LETHAL_SHOT)
					return true;
				if(getSkillType() == SkillType.PDAM)
					return true;
				if(getSkillType() == SkillType.DEBUFF)
					for(EffectUseType useType : EffectUseType.VALUES)
						if(!useType.isSelf())
						{
							if(hasEffect(useType, EffectType.i_p_attack))
								return true;
							if(hasEffect(useType, EffectType.i_m_attack))
								return true;
							if(hasEffect(useType, EffectType.i_hp_drain))
								return true;
						}
				return false;
			}
		}
		return false;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public boolean isActive()
	{
		return _operateType.isActive();
	}

	public boolean isPassive()
	{
		return _operateType.isPassive();
	}

	public boolean isToggle()
	{
		return _operateType.isToggle();
	}

	public boolean isAura()
	{
		return _operateType.isAura();
	}

	public boolean isHidingMesseges()
	{
		return _operateType.isHidingMesseges();
	}

	public boolean isNotBroadcastable()
	{
		return _operateType.isNotBroadcastable();
	}

	public boolean isContinuous()
	{
		return _operateType.isContinuous() || isSelfContinuous();
	}

	public boolean isSelfContinuous()
	{
		return _operateType.isSelfContinuous();
	}

	public boolean isChanneling()
	{
		return _operateType.isChanneling();
	}

	public boolean isSynergySkill()
	{
		return _operateType.isSynergy();
	}

	public void setDisplayLevel(int lvl)
	{
		_displayLevel = lvl;
	}

	public final boolean isItemSkill()
	{
		return _isItemSkill;
	}

	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}

	private final boolean checkCastTarget(Creature target)
	{
		for(Abnormal e : target.getAbnormalList().getEffects())
			if(e.isIgnoredSkill(this))
				return false;
		return true;
	}

	private final boolean applyEffectPoint(Creature activeChar, Creature target)
	{
		if(target.isNpc() && isOffensive() && !isAI() && getEffectPoint() > 0)
		{
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, 0);
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, this, Math.max(1, getEffectPoint()));
			return true;
		}
		return false;
	}

	public final void onStartCast(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		Creature castingTarget = activeChar.getCastingTarget();

		onStartCast(activeChar, castingTarget, targets);
		if(isPassive())
			return;
		if(!hasEffects(EffectUseType.START_INSTANT) && !hasEffects(EffectUseType.START))
			return;

		int castInterval = activeChar.getCastInterval();
		boolean startAttackStance = false;
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			if(!checkCastTarget(target))
				continue;
			if(applyEffectPoint(activeChar, target))
				startAttackStance = true;
			if(castInterval > 0 && hasEffects(EffectUseType.START))
			{
				if(calcEffectsSuccess(activeChar, target, true) && getEffects(activeChar, target, EffectUseType.START, castInterval * 1000, 1.0))
					continue;
				if(target == castingTarget)
					activeChar.abortCast(true, false);
			}
			for(EffectTemplate et : getEffectTemplates(EffectUseType.START_INSTANT))
				useInstantEffect(et, activeChar, target, false);
		}
		if(isSSPossible() && (!Config.SAVING_SPS || _skillType != SkillType.BUFF))
			activeChar.unChargeShots(isMagic());
		if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	protected void onStartCast(Creature activeChar, Creature castingTarget, List<Creature> targets) {

	}

	public final void onTickCast(Creature activeChar, List<Creature> targets)
	{
		if(isPassive())
			return;
		if(!hasEffects(EffectUseType.TICK_INSTANT) && !hasEffects(EffectUseType.TICK))
			return;
		Creature castingTarget = activeChar.getCastingTarget();
		int castInterval = activeChar.getCastInterval();
		int effectTime = Math.max(0, Math.min(castInterval, getTickInterval()));
		boolean startAttackStance = false;
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			if(!checkCastTarget(target))
				continue;
			if(applyEffectPoint(activeChar, target))
				startAttackStance = true;
			if(effectTime > 0 && hasEffects(EffectUseType.TICK) && calcEffectsSuccess(activeChar, target, false))
                getEffects(activeChar, target, EffectUseType.TICK, effectTime, 1.0);
			for(EffectTemplate et : getEffectTemplates(EffectUseType.TICK_INSTANT))
				useInstantEffect(et, activeChar, target, false);
		}
		if(isSSPossible() && (!Config.SAVING_SPS || _skillType != SkillType.BUFF))
			activeChar.unChargeShots(isMagic());
		if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public void onEndCast(Creature activeChar, List<Creature> targets)
	{
		if(isPassive())
			return;
		if(!isNotTargetAoE() || !isOffensive() || !targets.isEmpty())
		{
            getEffects(activeChar, activeChar, EffectUseType.SELF, 0, 1.0);
			for(EffectTemplate et : getEffectTemplates(EffectUseType.SELF_INSTANT))
				useInstantEffect(et, activeChar, activeChar, false);
		}
		Creature castingTarget = activeChar.getCastingTarget();
		boolean startAttackStance = false;
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			if(!checkCastTarget(target))
				continue;
			if(applyEffectPoint(activeChar, target))
				startAttackStance = true;
			boolean reflected = target.checkReflectSkill(activeChar, this);
			boolean success = calcEffectsSuccess(activeChar, target, true);
			useSkill(activeChar, target, reflected);
			if(!isNoEffectsIfFailSkill() || success)
				for(EffectTemplate et2 : getEffectTemplates(EffectUseType.NORMAL_INSTANT))
					useInstantEffect(et2, activeChar, target, reflected);
			if(!success || !hasEffects(EffectUseType.NORMAL))
				continue;
            getEffects(activeChar, target);
		}
		if(isSSPossible() && (!Config.SAVING_SPS || _skillType != SkillType.BUFF))
			activeChar.unChargeShots(isMagic());
		if(isSuicideAttack())
			activeChar.doDie(null);
		else if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public void onFinishCast(Creature activeChar, Creature castingTarget, List<Creature> targets)
	{
		if(isOffensive() && (getTargetType() == SkillTargetType.TARGET_CORPSE || getTargetType() == SkillTargetType.TARGET_AREA_AIM_CORPSE))
		{
			Creature target = targets.get(0);
			if(target != null && target.isDead())
			{
				if(target.isNpc())
					((NpcInstance) target).endDecayTask();
				else if(target.isSummon())
					((SummonInstance) target).endDecayTask();
			}
		}
	}

	public void onAbortCast(Creature caster, Creature castingTarget)
	{
	}

	public void onAbnormalTimeEnd(Creature activeChar, Creature target)
	{
		if(!checkCastTarget(target))
			return;
		for(EffectTemplate et : getEffectTemplates(EffectUseType.END))
			useInstantEffect(et, activeChar, target, false);
	}

	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{}

	private boolean useInstantEffect(EffectTemplate et, Creature activeChar, Creature target, boolean reflected)
	{
		if(!et.isInstant())
			return false;
		if(target.isRaid() && et.getEffectType().isRaidImmune())
			return false;
		if(et.getChance() >= 0 && !Rnd.chance(et.getChance()))
			return false;

		Abnormal e = et.getEffect(activeChar, target, this, reflected);
		if(e == null)
			return false;
		if(!e.checkCondition())
			return false;
		e.instantUse();
		return true;
	}

	public boolean isAoE()
	{
		switch(_targetType)
		{
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_GROUND:
			case TARGET_SERVITOR_AURA:
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			case TARGET_AREA_AIM_CORPSE:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isNotTargetAoE()
	{
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_CLAN_ONLY:
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_GROUND:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isOffensive()
	{
		return _isOffensive;
	}

	public final boolean isForceUse()
	{
		return _isForceUse;
	}

	public boolean isAI()
	{
		return _skillType.isAI();
	}

	public boolean isPvM()
	{
		return _isPvm;
	}

	public final boolean isPvpSkill()
	{
		return _isPvpSkill;
	}

	public final boolean isDebuff()
	{
		return isDebuff;
	}

	public boolean isTrigger()
	{
		return _isTrigger;
	}

	public boolean oneTarget()
	{
		switch(_targetType)
		{
			case TARGET_SELF:
			case TARGET_HOLY:
			case TARGET_FLAGPOLE:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_ONE_SERVITOR:
			case TARGET_ONE_SERVITOR_NO_TARGET:
			case TARGET_ONE_SUMMON:
			case TARGET_ONE_SUMMON_NO_TARGET:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_ENEMY_SUMMON:
			case TARGET_ENEMY_SERVITOR:
			case TARGET_ONE:
			case TARGET_CLAN_ONE:
			case TARGET_PARTY_ONE:
			case TARGET_PARTY_ONE_WITHOUT_ME:
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_SIEGE:
			case TARGET_ITEM:
			case TARGET_NONE:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isSkillInterrupt()
	{
		return _skillInterrupt;
	}

	public boolean isNotUsedByAI()
	{
		return _isNotUsedByAI;
	}

	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}

	public boolean isIgnoreInvul()
	{
		return _isIgnoreInvul;
	}

	public boolean isNotAffectedByMute()
	{
		return _isNotAffectedByMute;
	}

	public boolean flyingTransformUsage()
	{
		return _flyingTransformUsage;
	}

	public final boolean canUseTeleport()
	{
		return _canUseTeleport;
	}

	public int getTickInterval()
	{
		return _tickInterval;
	}

	public double getSimpleDamage(Creature attacker, Creature target)
	{
		if(isMagic())
		{
			double mAtk = attacker.getMAtk(target, this);
			double mdef = target.getMDef(null, this);
			double power = getPower();
			double shotPower = (100.0 + (isSSPossible() ? attacker.getChargedSpiritshotPower() : 0.0)) / 100.0;
			return 91.0 * power * Math.sqrt(shotPower * mAtk) / mdef;
		}
		double pAtk = attacker.getPAtk(target);
		double pdef = target.getPDef(attacker);
		double power = getPower();
		double shotPower = (100.0 + (isSSPossible() ? attacker.getChargedSoulshotPower() : 0.0)) / 100.0;
		return shotPower * (pAtk + power) * 70.0 / pdef;
	}

	public long getReuseForMonsters()
	{
		long min = 1000L;
		switch(_skillType)
		{
			case DEBUFF:
			case PARALYZE:
			case STEAL_BUFF:
			{
				min = 10000L;
				break;
			}
			case MUTE:
			case ROOT:
			case SLEEP:
			case STUN:
			{
				min = 5000L;
				break;
			}
		}
		return Math.max(Math.max(_hitTime + _coolTime, _reuseDelay), min);
	}

	public double getAbsorbPart()
	{
		return _absorbPart;
	}

	public boolean isProvoke()
	{
		return _isProvoke;
	}

	public String getIcon()
	{
		return _icon;
	}

	public int getClanRepConsume()
	{
		return _cprConsume;
	}

	public int getFameConsume()
	{
		return _fameConsume;
	}

	public void setCubicSkill(boolean value)
	{
		_isCubicSkill = value;
	}

	public boolean isCubicSkill()
	{
		return _isCubicSkill;
	}

	public int[] getRelationSkills()
	{
		return _relationSkillsId;
	}

	public boolean isRelationSkill()
	{
		return _isRelation;
	}

	public boolean isAbortable()
	{
		return _abortable;
	}

	public boolean isCanUseWhileAbnormal()
	{
		return _canUseWhileAbnormal;
	}

	public boolean isSwitchable()
	{
		return _switchable;
	}

	public boolean isDoNotDispelOnSelfBuff()
	{
		return _isNotDispelOnSelfBuff;
	}

	public int getAbnormalTime()
	{
		return _abnormalTime;
	}

	public int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public AbnormalType getAbnormalType()
	{
		return _abnormalType;
	}

	public AbnormalEffect[] getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	public boolean isAbnormalHideTime()
	{
		return _abnormalHideTime;
	}

	public boolean isAbnormalCancelOnAction()
	{
		return _abnormalCancelOnAction;
	}

	public boolean checkRideState(MountType mountType)
	{
		int v = 1 << mountType.ordinal();
		return (_rideState & v) == v;
	}

	public final boolean applyEffectsOnSummon()
	{
		return _applyEffectsOnSummon;
	}

	public final boolean applyEffectsOnPet()
	{
		return _applyEffectsOnPet;
	}

	public final boolean isApplyMinRange()
	{
		return _applyMinRange;
	}

	public final int getMasteryLevel()
	{
		return _masteryLevel;
	}

	public final boolean isSelfOffensive()
	{
		return _isSelfOffensive;
	}

	public boolean canBeEvaded()
	{
		switch(getSkillType())
		{
			case PDAM:
			case CHARGE:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public double getDefenceIgnorePercent()
	{
		return _defenceIgnorePercent;
	}

	public int getBehindRadius()
	{
		return _behindRadius;
	}

	public double getPercentDamageIfTargetDebuff()
	{
		return _percentDamageIfTargetDebuff;
	}

	public boolean isNoFlagNoForce()
	{
		return _noFlagNoForce;
	}

	public boolean isNoEffectsIfFailSkill()
	{
		return _noEffectsIfFailSkill;
	}

	public boolean isRenewal()
	{
		return _skillType != SkillType.MUTE && _skillType != SkillType.SLEEP && _skillType != SkillType.ROOT && _renewal;
	}

	public int getBuffSlotType()
	{
		return _buffSlotType;
	}

	public SkillEntry getEntry()
	{
		if(_entry == null)
			_entry = SkillHolder.getInstance().getSkillEntry(getId(), getLevel());
		return _entry;
	}

	static
	{
		_log = LoggerFactory.getLogger(Skill.class);
		EMPTY_ARRAY = new Skill[0];
	}

	public void addSkill(Player player){

	}

	public void removeSkill(Player player){

	}

	public void setup() {

	}

	public static class AddedSkill
	{
		public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];

		public int id;
		public int level;
		private SkillEntry _skillEntry;

		public AddedSkill(int id, int level)
		{
			this.id = id;
			this.level = level;
		}

		public SkillEntry getSkill()
		{
			if(_skillEntry == null)
				_skillEntry = SkillHolder.getInstance().getSkillEntry(id, level);
			if(_skillEntry == null)
				_log.warn("Cannot find added skill ID[" + id + "] LEVEL[" + level + "]!");
			return _skillEntry;
		}
	}

	public enum EnchantType
	{
		NORMAL,
		SAFE,
		UNTRAIN,
		CHANGE,
		IMMORTAL;

		public static final EnchantType[] VALUES;

		static
		{
			VALUES = values();
		}
	}

	public enum NextAction
	{
		ATTACK,
		CAST,
		DEFAULT,
		MOVE,
		NONE
	}

	public enum Ternary
	{
		TRUE,
		FALSE,
		DEFAULT
	}

	public enum SkillMagicType
	{
		PHYSIC,
		MAGIC,
		SPECIAL,
		MUSIC,
		ITEM,
		UNK_MAG_TYPE_21,
		AWAKED_BUFF
	}

	public enum SkillTargetType
	{
		TARGET_ALLY,
		TARGET_AREA,
		TARGET_AREA_AIM_CORPSE,
		TARGET_AURA,
		TARGET_SERVITOR_AURA,
		TARGET_CHEST,
		TARGET_CLAN,
		TARGET_CLAN_ONE,
		TARGET_CLAN_ONLY,
		TARGET_CORPSE,
		TARGET_CORPSE_PLAYER,
		TARGET_ENEMY_PET,
		TARGET_ENEMY_SUMMON,
		TARGET_ENEMY_SERVITOR,
		TARGET_FLAGPOLE,
		TARGET_COMMCHANNEL,
		TARGET_HOLY,
		TARGET_ITEM,
		TARGET_MULTIFACE,
		TARGET_MULTIFACE_AURA,
		TARGET_TUNNEL,
		TARGET_NONE,
		TARGET_ONE,
		TARGET_OWNER,
		TARGET_PARTY,
		TARGET_PARTY_WITHOUT_ME,
		TARGET_PARTY_ONE,
		TARGET_PARTY_ONE_WITHOUT_ME,
		TARGET_SERVITORS,
		TARGET_SUMMONS,
		TARGET_PET,
		TARGET_ONE_SERVITOR,
		TARGET_ONE_SERVITOR_NO_TARGET,
		TARGET_SELF_AND_SUMMONS,
		TARGET_ONE_SUMMON,
		TARGET_ONE_SUMMON_NO_TARGET,
		TARGET_SELF,
		TARGET_SIEGE,
		TARGET_UNLOCKABLE,
		TARGET_GROUND,
		TARGET_FACTION
	}

	public enum SkillType
	{
		AGGRESSION(Aggression.class),
		AIEFFECTS(Continuous.class),
		BALANCE(Balance.class),
		BUFF(Continuous.class),
		BUFF_CHARGER(BuffCharger.class),
		CALL(Call.class),
		CHAIN_HEAL(ChainHeal.class),
		CHARGE(Charge.class),
		CHARGE_SOUL(ChargeSoul.class),
		CLAN_GATE(ClanGate.class),
		CPDAM(CPDam.class),
		CPHOT(Continuous.class),
		CRAFT(Craft.class),
		DEBUFF_RENEWAL(DebuffRenewal.class),
		DECOY(Decoy.class),
		DEBUFF(Continuous.class),
		DELETE_HATE(Continuous.class),
		DESTROY_SUMMON(DestroySummon.class),
		DEFUSE_TRAP(DefuseTrap.class),
		DETECT_TRAP(DetectTrap.class),
		DISCORD(Continuous.class),
		DOT(Continuous.class),
		DRAIN(Drain.class),
		DRAIN_SOUL(DrainSoul.class),
		EFFECT(Skill.class),
		EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		EXTRACT_STONE(ExtractStone.class),
		HARDCODED(Skill.class),
		HARVESTING(Harvesting.class),
		HEAL(Continuous.class),
		HEAL_PERCENT(Continuous.class),
		HOT(Continuous.class),
		HIDE_HAIR_ACCESSORIES(HideHairAccessories.class),
        INSTANT_JUMP(InstantJump.class),
		LETHAL_SHOT(LethalShot.class),
		LUCK,
		MANADAM(ManaDam.class),
		MDAM(MDam.class),
		MDOT(Continuous.class),
		MPHOT(Continuous.class),
		MUTE(Disablers.class),
		ADD_PC_BANG(PcBangPointsAdd.class),
		NOTDONE,
		NOTUSED,
		PARALYZE(Disablers.class),
		PASSIVE,
		PDAM(PDam.class),
		PET_FEED(PetFeed.class),
		PET_SUMMON(PetSummon.class),
		POISON(Continuous.class),
		RECALL(Recall.class),
		RESURRECT(Resurrect.class),
		REPLACE(Replace.class),
		RIDE(Ride.class),
		ROOT(Disablers.class),
		SHIFT_AGGRESSION(ShiftAggression.class),
		SLEEP(Disablers.class),
		SOWING(Sowing.class),
		EXPHEAL(EXPHeal.class),
		SPHEAL(SPHeal.class),
		EXPGIVE(EXPGive.class),
		SACRIFICE(Sacrifice.class),
		STEAL_BUFF(StealBuff.class),
		STUN(Disablers.class),
		SUMMON(Summon.class),
		SUMMON_FLAG(SummonSiegeFlag.class),
		RESTORATION(Restoration.class),
		SWEEP(Sweep.class),
		TAKECASTLE(TakeCastle.class),
		TAKEFORTRESS(TakeFortress.class),
		TRAP_ACTIVATION(TrapActivation.class),
		UNLOCK(Unlock.class),
		WATCHER_GAZE(Continuous.class),
		SUMMON_PORTAL(SummonPortal.class),
		LN_AURA(LNAura.class),
		ARTIFACT(Artifact.class);

		private final Class<? extends Skill> clazz;

		SkillType()
		{
			clazz = Default.class;
		}

		SkillType(Class<? extends Skill> clazz)
		{
			this.clazz = clazz;
		}

		public Skill makeSkill(StatsSet set)
		{
			try
			{
				Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			}
			catch(Exception e)
			{
				_log.error("Skill ID[" + set.getInteger("skill_id") + "], LEVEL[" + set.getInteger("level") + "]", e);
				throw new RuntimeException(e);
			}
		}

		public final boolean isPvM()
		{
			switch(this)
			{
				case DISCORD:
				{
					return true;
				}
				default:
				{
					return false;
				}
			}
		}

		public boolean isAI()
		{
			switch(this)
			{
				case AGGRESSION:
				case AIEFFECTS:
				case SOWING:
				case DELETE_HATE:
				{
					return true;
				}
				default:
				{
					return false;
				}
			}
		}

		public final boolean isPvpSkill()
		{
			switch(this)
			{
				case AGGRESSION:
				case DELETE_HATE:
				case DEBUFF:
				case DOT:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case POISON:
				case ROOT:
				case SLEEP:
				case MANADAM:
				case DESTROY_SUMMON:
				case STEAL_BUFF:
				case DEBUFF_RENEWAL:
				{
					return true;
				}
				default:
				{
					return false;
				}
			}
		}

		public boolean isOffensive()
		{
			switch(this)
			{
				case DISCORD:
				case AGGRESSION:
				case AIEFFECTS:
				case SOWING:
				case DELETE_HATE:
				case DEBUFF:
				case DOT:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case POISON:
				case ROOT:
				case SLEEP:
				case MANADAM:
				case DESTROY_SUMMON:
				case STEAL_BUFF:
				case DEBUFF_RENEWAL:
				case DRAIN:
				case DRAIN_SOUL:
				case LETHAL_SHOT:
				case MDAM:
				case PDAM:
				case CPDAM:
				case STUN:
				case SWEEP:
				case HARVESTING:
				{
					return true;
				}
				default:
				{
					return false;
				}
			}
		}

		public boolean isDebuff()
		{
			switch(this)
			{
				case DEBUFF:
				case POISON:
				case ROOT:
				case SLEEP:
				case STUN:
				case PARALYZE:
				case MUTE:
				{
					return true;
				}
				default:
				{
					return false;
				}
			}
		}

		public boolean isHeal() {
			switch(this) {
				case HEAL:
				case HEAL_PERCENT:
				case CHAIN_HEAL: {
					return true;
				}
			}
			return false;
		}
	}

	public int getAffectLimit()
	{
		if((_affectLimit[0] > 0) || (_affectLimit[1] > 0))
			return (_affectLimit[0] + Rnd.get(_affectLimit[1]));

		return 0;
	}

	public boolean removeEffectOnDeleteSkill()
	{
		return _removeEffectOnDeleteSkill;
	}

	public long getReuseDelayOnEquip()
	{
		return _reuseDelayOnEquip;
	}

	public boolean isCheckConditionSkillForZone() {
		return _checkConditionSkillForZone;
	}
	
	public final boolean getWeaponDependancy(Creature activeChar)
	{
		//if (_weaponsAllowed == 0)
			return true;
		
		/*if (activeChar.getActiveWeaponInstance() != null && activeChar.getActiveWeaponInstance() != null)
			if ((activeChar.getActiveWeaponInstance().getItemType().mask() & _weaponsAllowed) != 0)
				return true;
			
		if (activeChar.getSecondaryWeaponInstance() != null && activeChar.getSecondaryWeaponInstance() != null)
			if ((activeChar.getSecondaryWeaponInstance().getItemType().mask() & _weaponsAllowed) != 0)
				return true;
			
		activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_displayId, _displayLevel));
		
		return false;*/
	}
}
