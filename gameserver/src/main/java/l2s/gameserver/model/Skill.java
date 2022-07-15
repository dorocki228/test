package l2s.gameserver.model;

import com.google.common.flogger.FluentLogger;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.lang.ArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.handler.skillconditions.SkillCondition;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.ChestInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.skill.SkillConditionScope;
import l2s.gameserver.model.skill.SkillTarget;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FlyToLocation.FlyType;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.targets.AffectObject;
import l2s.gameserver.skills.targets.AffectScope;
import l2s.gameserver.skills.targets.TargetType;
import l2s.gameserver.stats.BooleanStat;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Skill extends StatTemplate implements SkillInfo, Cloneable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static class AddedSkill
	{
		public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];

		private final SkillEntryType entryType;
		public final int id;
		public final int level;
		private SkillEntry _skillEntry;

		public AddedSkill(SkillEntryType entryType, int id, int level)
		{
			this.entryType = entryType;
			this.id = id;
			this.level = level;
		}

		public SkillEntryType getEntryType() {
			return entryType;
		}

		public SkillEntry getSkill()
		{
			if(_skillEntry == null)
				_skillEntry = SkillEntry.makeSkillEntry(entryType, id, level);
			if(_skillEntry == null)
				_log.atWarning().log( "Cannot find added skill ID[%s] LEVEL[%s]!", id, level );
			return _skillEntry;
		}
	}

	public static enum EnchantType
	{
		/*0*/NORMAL,
		/*1*/SAFE,
		/*2*/UNTRAIN,
		/*3*/CHANGE,
		/*4*/IMMORTAL;

		public static final EnchantType[] VALUES = values();
	}

	public static enum NextAction
	{
		ATTACK,
		CAST,
		DEFAULT,
		MOVE,
		NONE
	}

	public static enum Ternary
	{
		TRUE,
		FALSE,
		DEFAULT
	}

	public enum SkillMagicType
	{
		PHYSIC, // Offlike: 0
		MAGIC, // Offlike: 1
		SPECIAL, // Offlike: 2
		MUSIC, // Offlike: 3
		TRIGGER, // Offlike: 4
		UNK_MAG_TYPE_5,
		UNK_MAG_TYPE_6,
		UNK_MAG_TYPE_7,
		UNK_MAG_TYPE_8,
		UNK_MAG_TYPE_9,
		UNK_MAG_TYPE_10,
		UNK_MAG_TYPE_11,
		UNK_MAG_TYPE_12,
		UNK_MAG_TYPE_13,
		UNK_MAG_TYPE_14,
		UNK_MAG_TYPE_15,
		UNK_MAG_TYPE_16,
		UNK_MAG_TYPE_17,
		UNK_MAG_TYPE_18,
		UNK_MAG_TYPE_19,
		UNK_MAG_TYPE_20,
		UNK_MAG_TYPE_21, // Offlike: 21
		AWAKED_BUFF, // Offlike: 22
		UNK_MAG_TYPE_23,
		UNK_MAG_TYPE_24,
		UNK_MAG_TYPE_25,
		UNK_MAG_TYPE_26,
		UNK_MAG_TYPE_27,
		UNK_MAG_TYPE_28,
		UNK_MAG_TYPE_29,
		UNK_MAG_TYPE_30,
		UNK_MAG_TYPE_31,
		UNK_MAG_TYPE_32,
		UNK_MAG_TYPE_33,
		UNK_MAG_TYPE_34,
		UNK_MAG_TYPE_35,
		UNK_MAG_TYPE_36,
		UNK_MAG_TYPE_37,
		UNK_MAG_TYPE_38,
		UNK_MAG_TYPE_39,
		UNK_MAG_TYPE_40,
		UNK_MAG_TYPE_41,
		UNK_MAG_TYPE_42,
		UNK_MAG_TYPE_43,
		UNK_MAG_TYPE_44,
		UNK_MAG_TYPE_45,
		UNK_MAG_TYPE_46,
		UNK_MAG_TYPE_47,
		UNK_MAG_TYPE_48,
		UNK_MAG_TYPE_49,
		UNK_MAG_TYPE_50,
		UNK_MAG_TYPE_51,
		UNK_MAG_TYPE_52;
	}

	@Deprecated
	public static enum SkillTargetType
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
		TARGET_NONE,
		TARGET_ONE,
		TARGET_OWNER,
		TARGET_PARTY,
		TARGET_PARTY_WITHOUT_ME,
		TARGET_PARTY_ONE,
		TARGET_PARTY_ONE_WITHOUT_ME,
		TARGET_SERVITORS,
		TARGET_SUMMON,
		TARGET_PET,
		TARGET_ONE_SERVITOR,
		TARGET_ONE_SERVITOR_NO_TARGET,
		TARGET_SELF_AND_SUMMON,
		TARGET_SELF,
		TARGET_SIEGE,
		TARGET_UNLOCKABLE,
		TARGET_GROUND,
		// PTS target types
		TARGET_FAN,
		TARGET_FAN_PB,
		TARGET_SQUARE,
		TARGET_SQUARE_PB,
		TARGET_RANGE,
		TARGET_RING_RANGE
	}

	public static final Skill[] EMPTY_ARRAY = new Skill[0];

	//public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_COMMON_CRAFTING = 1320;
	public static final int SKILL_POLEARM_MASTERY = 216;
	public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
	public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_BLINDING_BLOW = 321;
	public static final int SKILL_STRIDER_ASSAULT = 325;
	public static final int SKILL_WYVERN_AEGIS = 327;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_HEROIC_MIRACLE = 395;
	public static final int SKILL_HEROIC_BERSERKER = 396;
	public static final int SKILL_TRANSFORM_DISPEL = 619;
	public static final int SKILL_FINAL_FLYING_FORM = 840;
	public static final int SKILL_AURA_BIRD_FALCON = 841;
	public static final int SKILL_AURA_BIRD_OWL = 842;
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

	private final TIntObjectMap<List<EffectTemplate>> _effectTemplates = new TIntObjectHashMap<List<EffectTemplate>>(EffectUseType.VALUES.length);

	private final AddedSkill[] _addedSkills;

	private final long _itemConsume;
	private final int _itemConsumeId;
	private final int[] _relationSkillsId;
	private final int _referenceItemId; // для талисманов
	private final int _referenceItemMpConsume; // количество потребляемой мп талисмана

	private final boolean _isBehind;
	private final boolean undeletable;
	private final boolean _isCorpse;
	private final boolean _isItemHandler;
	private final int debuff;
	private final boolean _isNotUsedByAI;
	private final boolean _isForceUse;
	private final boolean _isPreservedOnDeath;
	private final boolean _isSaveable;
	private final boolean _isSkillTimePermanent;
	private final boolean _isReuseDelayPermanent;
	private final boolean _isReflectable;
	private final boolean _isSuicideAttack;
	private final boolean _isShieldignore;
	private final double _shieldIgnorePercent;
	private final boolean _isUndeadOnly;
	private final Ternary _isUseSS;
	private final boolean _isOverhit;
	private final boolean _isChargeBoost;
	private final boolean _isIgnoreResists;
	private final boolean _isIgnoreInvul;
	private final boolean _isNotAffectedByMute;
	private final boolean _basedOnTargetDebuff;
	private final boolean _deathlink;
	private final boolean _hideStartMessage;
	private final boolean _hideUseMessage;
	private final boolean _skillInterrupt;
	private final boolean _flyingTransformUsage;
	private final boolean _canUseTeleport;
	private boolean _isCubicSkill;
	private final boolean _isSelfDispellable;
	private final boolean _isRelation;
	private final double _decreaseOnNoPole;
	private final double _increaseOnPole;
	private final boolean _canUseWhileAbnormal;
	private final int _lethal2SkillDepencensyAddon;
	private final double _lethal2Addon;
	private final int _lethal1SkillDepencensyAddon;
	private final double _lethal1Addon;

	private final SkillOperateType _operateType;

	// TODO remove _targetType
	@Deprecated
	private final SkillTargetType _targetType = null;
	private final TargetType targetTypeNew;
	private final AffectScope affectScope;
	private final AffectObject affectObject;

	private final SkillMagicType _magicType;
	private final TraitType _traitType;
	private final boolean _dispelOnDamage;
	private final NextActionType _nextAction;
	private final AttributeType attributeType;
	private final int attributePower;

	private final FlyType _flyType;
	private final boolean _flyDependsOnHeading;
	private final int _flyRadius;
	private final int _flyPositionDegree;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _flyAnimationSpeed;

	private final int _activateRate;
	private final double _minChance;
	private final double _maxChance;
	private final int _castRange;
	private final int _condCharges;
	private final int _coolTime;
	private final int _effectPoint;
	private final int _energyConsume;
	private final int _cprConsume;
	private final int _fameConsume;

	private final int _hitTime;
	private final int _levelBonusRate;
	private final int _magicLevel;
	private final PledgeRank _minPledgeRank;
	private final boolean _clanLeaderOnly;
	private final int chargeConsume;
	private final int _hitCancelTime;
	private final AddedSkill _attachedSkill;
	private final int _channelingStart;
	private final int _affectRange;
	// TODO use
	private final int[] affectScopeHeight;
	private final int[] _fanRange;
	private final int[] _affectLimit;
	private final int _effectiveRange;
	private final int _behindRadius;
	private final int _tickInterval;
	private final double _criticalRate;

	private final int _reuseDelay;

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
	private final double _defenceIgnorePercent;

	private final String _name;
	private final String _icon;

	public boolean _isStandart = false;

	private final int _hashCode;

	private final int _reuseSkillId;
	private final int _reuseHash;

	private final int _toggleGroupId;
	private final boolean _isNecessaryToggle;
	private final boolean _isNotDispelOnSelfBuff;

	private final int _abnormalTime;
	private final int _abnormalLvl;
	private final AbnormalTypeList abnormalTypeList;
	private final AbnormalVisualEffect[] abnormalVisualEffects;
	private final boolean _abnormalHideTime;
	private final boolean _irreplaceableBuff;
	private final boolean _abnormalInstant;
	private boolean _detectPcHide;

	private final int _rideState;

	private final boolean _isSelfDebuff;
	private final boolean _applyEffectsOnSummon;
	private final boolean _applyEffectsOnPet;

	// @Rivelia.
	private final boolean _applyMinRange;
	private final int _masteryLevel;

	private final boolean _altUse;

	private final boolean _isItemSkill;
	// .

	private final double _percentDamageIfTargetDebuff;
	private final boolean _noFlagNoForce;
	private final boolean _renewal;
	private final int _buffSlotType;
	
	private final BasicProperty _basicProperty;

	private final boolean _isDouble;

	private final double _onAttackCancelChance;
	private final double _onCritCancelChance;


	private final int _id;
	private final int _level;
	private final int _displayId;
	private int _displayLevel;

	private final Map<SkillConditionScope, List<SkillCondition>> conditions = new EnumMap<>(SkillConditionScope.class);

	private final int _hpConsume;

	private int olympiadUse;

	private int buffProtectLevel;

	// TODO use it
	private int isHate;
	private int npcNotice;

	// Для заглушки отображения кондишона требующий эффект у цели.
	private boolean showPlayerAbnormal = false;
	private boolean showNpcAbnormal = false;

	private boolean autoUsable;

	private final Set<AbnormalType> _abnormalResists;

	/**
	 * Внимание!!! У наследников вручную надо поменять тип на public
	 * @param set парамерты скилла
	 */
	public Skill(StatsSet set)
	{
		//_set = set;
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");

		_displayId = set.getInteger("display_id", _id);
		_displayLevel = set.getInteger("display_level", _level);

		_name = set.getString("name");

		_operateType = set.getEnum("operate_type", SkillOperateType.class);

		_magicLevel = set.getInteger("magic_level", 0);

		_castRange = set.getInteger("cast_range", -1);

		_abnormalTime = set.getInteger("abnormal_time", -1);
		_abnormalLvl = set.getInteger("abnormal_lv", 0);

		abnormalTypeList = AbnormalTypeList.Companion.parse(set.getString("abnormal_type", AbnormalType.NONE.toString()));

		_tickInterval = Math.max(-1, (int) (set.getDouble("tick_interval", -1) * 1000));

		_hpConsume = set.getInteger("hp_consume", 0);

		olympiadUse = set.getInteger("olympiad_use", 1);

		buffProtectLevel = set.getInteger("buff_protect_level", 0);
		isHate = set.getInteger("is_hate", 0);
		npcNotice = set.getInteger("npc_notice", 0);

		autoUsable = set.getBool("autousable", false);

		final String abnormalResist = set.getString("resist_cast", null);
		if (abnormalResist != null)
		{
			String[] abnormalResistStrings = abnormalResist.split(";");
			if (abnormalResistStrings.length > 0)
			{
				_abnormalResists = new HashSet<>(abnormalResistStrings.length);
				for (String s : abnormalResistStrings)
				{
					try
					{
						_abnormalResists.add(AbnormalType.valueOf(s.toUpperCase()));
					}
					catch (Exception e)
					{
						_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("Skill ID[%d] Expected AbnormalType for abnormalResists but found %s", _id, s);
					}
				}
			}
			else
			{
				_abnormalResists = Collections.emptySet();
			}
		}
		else
		{
			_abnormalResists = Collections.emptySet();
		}

		// old

		_isSelfDispellable = set.getBool("isSelfDispellable", true);
		_isPreservedOnDeath = set.getBool("isPreservedOnDeath", false);
		_energyConsume = set.getInteger("energyConsume", 0);
		_cprConsume = set.getInteger("clanRepConsume", 0);
		_fameConsume = set.getInteger("fameConsume", 0);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());

		String[] abnormalEffects = set.getString("abnormal_visual_effect", AbnormalVisualEffect.NONE.toString()).split(";");
		abnormalVisualEffects = new AbnormalVisualEffect[abnormalEffects.length];
		for(int i = 0; i < abnormalEffects.length; i++)
			abnormalVisualEffects[i] = AbnormalVisualEffect.find(abnormalEffects[i]);

		_abnormalHideTime = set.getBool("abnormal_hide_time", false);
		_irreplaceableBuff = set.getInteger("irreplaceable_buff", 0) == 1;
		_abnormalInstant = set.getInteger("abnormal_instant", 0) == 1;

		String[] ride_state = set.getString("ride_state", MountType.NONE.toString()).split(";");
		int rideState = 0;
		for (String value : ride_state) {
			final String name = value.replace("ride_", "").toUpperCase();
			rideState |= (1 << MountType.valueOf(name).ordinal());
		}
		_rideState = rideState;

		_toggleGroupId = set.getInteger("toggle_group_id", 0);
		_isNecessaryToggle = set.getBool("is_necessarytg", false);
		_isNotDispelOnSelfBuff = set.getBool("doNotDispelOnSelfBuff", false);

		String[] itemConsume = set.getString("item_consume", "0:0").split(":");
		_itemConsumeId = Integer.parseInt(itemConsume[0]);
		_itemConsume = Long.parseLong(itemConsume[1]);

		String s3 = set.getString("relationSkillsId", "");
		if(s3.length() == 0)
		{
			_isRelation = false;
			_relationSkillsId = new int[] { 0 };
		}
		else
		{
			_isRelation = true;
			String[] s = s3.split(";");
			_relationSkillsId = new int[s.length];
			for(int i = 0; i < s.length; i++)
				_relationSkillsId[i] = Integer.parseInt(s[i]);
		}

		_referenceItemId = set.getInteger("referenceItemId", 0);
		_referenceItemMpConsume = set.getInteger("referenceItemMpConsume", 0);

		_isItemHandler = set.getBool("isHandler", false);
		_isSaveable = set.getBool("isSaveable", _operateType.isActive());
		_coolTime = (int) (set.getDouble("skill_cool_time", 0) * 1000);
		_hitCancelTime = (int) (set.getDouble("skill_hit_cancel_time", 0) * 1000);

		int[] attachedSkill = set.getIntegerArray("attached_skill", new int[2], "-"); // TODO: Учитывать subLevel
		if(attachedSkill.length > 0 && attachedSkill[0] > 0)
			_attachedSkill = new AddedSkill(SkillEntryType.NONE, attachedSkill[0], attachedSkill.length > 1 ? attachedSkill[1] : 1);
		else
			_attachedSkill = null;

		_channelingStart = (int) (set.getDouble("channeling_start", 0) * 1000);
		_reuseDelay = (int) (set.getDouble("reuse_delay", 0) * 1000);
		_hitTime = (int) (set.getDouble("skill_hit_time", 0) * 1000);
		_fanRange = set.getIntegerArray("fan_range", new int[4]);	// unk;startDegree;fanAffectRange;fanAffectAngle
		_effectiveRange = set.getInteger("effective_range", -1);

		_behindRadius = Math.min(360, Math.max(0, set.getInteger("behind_radius", 0)));

		targetTypeNew = set.getEnum("target_type", TargetType.class, TargetType.SELF, true);
		affectScope = set.getEnum("affect_scope", AffectScope.class, AffectScope.SINGLE, true);
		_affectRange = set.getInteger("affect_range", 80);
		affectScopeHeight = set.getIntegerArray("affect_scope_height", new int[] { 0, 0 });
		affectObject = set.getEnum("affect_object", AffectObject.class, AffectObject.ALL, true);
		// minAffected;additionalRandom;unk
		int[] affectLimits = set.getIntegerArray("affect_limit", new int[3]);
		if (affectLimits.length == 2) {
			affectLimits = Arrays.copyOf(affectLimits, 3);
		}
		_affectLimit = affectLimits;

		_magicType = SkillMagicType.values()[set.getInteger("is_magic", 0)];

		int mpConsume = set.getInteger("mp_consume", 0);
		_mpConsume1 = set.getInteger("mp_consume1", _magicType == SkillMagicType.MAGIC ? (mpConsume / 4) : 0);
		_mpConsume2 = set.getInteger("mp_consume2", _magicType == SkillMagicType.MAGIC ? (mpConsume / 4) * 3 : mpConsume);
		_mpConsumeTick = set.getInteger("mp_consume_tick", 0);

		String traitName = set.getString("trait", "NONE").toUpperCase();
		if(traitName.startsWith("TRAIT_"))
			traitName = traitName.substring(6).trim();
		_traitType = TraitType.valueOf(traitName);

		_dispelOnDamage = set.getBool("dispelOnDamage", false);
		_hideStartMessage = set.getBool("isHideStartMessage", false);
		_hideUseMessage = set.getBool("isHideUseMessage", false);
		_isUndeadOnly = set.getBool("undeadOnly", false);
		_isCorpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.);
		_chargeEffectPower = set.getDouble("chargeEffectPower", _power);
		_chargeDefectPower = set.getDouble("chargeDefectPower", _power);
		_powerPvP = set.getDouble("powerPvP", 0.);
		_chargeEffectPowerPvP = set.getDouble("chargeEffectPowerPvP", _powerPvP);
		_chargeDefectPowerPvP = set.getDouble("chargeDefectPowerPvP", _powerPvP);
		_powerPvE = set.getDouble("powerPvE", 0.);
		_chargeEffectPowerPvE = set.getDouble("chargeEffectPowerPvE", _powerPvE);
		_chargeDefectPowerPvE = set.getDouble("chargeDefectPowerPvE", _powerPvE);
		_effectPoint = set.getInteger("effect_point", 0);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getInteger("reuse_delay_lock", 0) == 1;
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isIgnoreInvul = set.getBool("isIgnoreInvul", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_flyingTransformUsage = set.getBool("flyingTransformUsage", false);
		_canUseTeleport = set.getBool("canUseTeleport", true);
		_altUse = set.getBool("alt_use", false);

		String[] attribute = set.getString("attribute", "attr_none;0").split(";");
		attributeType = AttributeType.find(attribute[0]);
		attributePower = Integer.parseInt(attribute[1]);

		_activateRate = set.getInteger("activate_rate", -1);
		_minChance = set.getDouble("min_chance", Config.MIN_ABNORMAL_SUCCESS_RATE);
		_maxChance = set.getDouble("max_chance", Config.MAX_ABNORMAL_SUCCESS_RATE);
		_levelBonusRate = set.getInteger("lv_bonus_rate", 0);
		undeletable = set.getBool("undeletable", false);
		_isReflectable = set.getBool("reflectable", true);
		_isShieldignore = set.getBool("shieldignore", false);
		_shieldIgnorePercent = set.getDouble("shield_ignore_percent", 0.);
		_criticalRate = set.getDouble("magic_critical_rate", 0.0);
		_isOverhit = set.getBool("overHit", false);
		_minPledgeRank = set.getEnum("min_pledge_rank", PledgeRank.class, PledgeRank.VAGABOND);
		_clanLeaderOnly = set.getBool("clan_leader_only", false);
		debuff = set.getInteger("debuff");
		_isForceUse = set.getBool("isForceUse", false);
		_isBehind = set.getBool("behind", false);

		_flyType = FlyType.valueOf(set.getString("fly_type", "NONE").toUpperCase());
		_flyDependsOnHeading = set.getBool("fly_depends_on_heading", false);
		_flySpeed = set.getInteger("fly_speed", 0);
		_flyDelay = set.getInteger("fly_delay", 0);
		_flyAnimationSpeed = set.getInteger("fly_animation_speed", 0);
		_flyRadius = set.getInteger("fly_radius", 200);
		_flyPositionDegree = set.getInteger("fly_position_degree", 0);

		int[] consumeEtcs = set.getIntegerArray("consume_etc", new int[] {0, 0, 0});
		chargeConsume = consumeEtcs[0];
		_condCharges = set.getInteger("cond_charges", 0);
		_skillInterrupt = set.getBool("skillInterrupt", false);
		_lethal1 = set.getDouble("lethal1", 0.);
		_decreaseOnNoPole = set.getDouble("decreaseOnNoPole", 0.);
		_increaseOnPole = set.getDouble("increaseOnPole", 0.);
		_lethal2 = set.getDouble("lethal2", 0.);
		_lethal2Addon = set.getDouble("lethal2DepensencyAddon", 0.);
		_lethal2SkillDepencensyAddon = set.getInteger("lethal2SkillDepencensyAddon", 0);
		_lethal1Addon = set.getDouble("lethal1DepensencyAddon", 0.);
		_lethal1SkillDepencensyAddon = set.getInteger("lethal1SkillDepencensyAddon", 0);
		_icon = set.getString("icon", "");
		_canUseWhileAbnormal = set.getBool("canUseWhileAbnormal", false);
		_defenceIgnorePercent = set.getDouble("defence_ignore_percent", 0.);

		AddedSkill[] addedSkills = AddedSkill.EMPTY_ARRAY;

		StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st.hasMoreTokens())
		{
			int id = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			if(level == -1)
				level = _level;
			addedSkills = ArrayUtils.add(addedSkills, new AddedSkill(SkillEntryType.NONE, id, level));
		}

		_addedSkills = addedSkills;

		_nextAction = NextActionType.valueOf(set.getString("next_action", "NONE").toUpperCase());

		_reuseSkillId = set.getInteger("reuse_skill_id", -1);
		_reuseHash = SkillHolder.getInstance().getHashCode(_reuseSkillId > 0 ? _reuseSkillId : _id, _level);
		_detectPcHide = set.getBool("detectPcHide", false);
		_hashCode = SkillHolder.getInstance().getHashCode(_id, _level);

		_isSelfDebuff = set.getBool("self_debuff", isDebuff());
		_applyEffectsOnSummon = set.getBool("apply_effects_on_summon", true);
		_applyEffectsOnPet = set.getBool("apply_effects_on_pet", true);

		// @Rivelia.
		// applyMinRange is to bypass CHARGE flytype minimal range requirements. False = no range requirements.
		_applyMinRange = set.getBool("applyMinRange", true);
		// masteryLevel corresponds to the mastery calculation found in Formulas. If value is -1, default rule will be applied.
		_masteryLevel = set.getInteger("masteryLevel", -1);
		// .

		_isItemSkill = set.getBool("is_item_skill", false);

		for(EffectUseType type : EffectUseType.VALUES)
			_effectTemplates.put(type.ordinal(), new ArrayList<EffectTemplate>(0));

		_percentDamageIfTargetDebuff = set.getDouble("percent_damage_if_target_debuff", 1.);
		_noFlagNoForce = set.getBool("noFlag_noForce", false);
		_renewal = set.getBool("renewal", true);
		_buffSlotType = set.getInteger("buff_slot_type", -2);

		_basicProperty = BasicProperty.valueOf(set.getString("basic_property", "none").toUpperCase());

		_isDouble = set.getBool("is_double", false);

		_onAttackCancelChance = set.getDouble("on_attack_cancel_chance", 0.);
		_onCritCancelChance = set.getDouble("on_crit_cancel_chance", 0.);

		if(isDebuff()) {
			showPlayerAbnormal = Config.SHOW_TARGET_PLAYER_DEBUFF_EFFECTS;
			showNpcAbnormal = Config.SHOW_TARGET_NPC_DEBUFF_EFFECTS;
		} else {
			showPlayerAbnormal = Config.SHOW_TARGET_PLAYER_BUFF_EFFECTS;
			showNpcAbnormal = Config.SHOW_TARGET_NPC_BUFF_EFFECTS;
		}
	}

	public void init()
	{
		if(!isPassive())
		{
			// Прописанные статты активным скиллам переводим в эффект.
			FuncTemplate[] funcs = removeAttachedFuncs();
			if(funcs.length > 0 || getAbnormalTime() > 0 && !hasEffects(EffectUseType.NORMAL))
			{
				EffectTemplate template = new EffectTemplate(this, StatsSet.EMPTY, EffectUseType.NORMAL, EffectTargetType.NORMAL);
				template.attachFuncs(funcs);
				attachEffect(template);
			}
		}

		if(!showPlayerAbnormal || !showNpcAbnormal)
		{
			// Для заглушки отображения кондишона требующий эффект у цели.
			// remove ?
			// for(Condition cond : getConditions())
			//	cond.init();
		}
	}

	public final boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return checkCondition(skillEntry, activeChar, target, forceUse, dontMove, first, true, false);
	}

	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
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

		if(!trigger && (player != null && player.isInZone(ZoneType.JUMPING) || target != null && target.isInZone(ZoneType.JUMPING)))
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_SKILLS_IN_THE_CORRESPONDING_REGION);
			return false;
		}

		//TODO: Если у предмета который использует данный скилл есть откат, то не учитываем откат умения.
		if(first && activeChar.isSkillDisabled(this))
		{
			if(sendMsg)
				activeChar.sendReuseMessage(this);
			return false;
		}

		// DS: Clarity не влияет на mpConsume1 
		if(first && activeChar.getCurrentMp() < _mpConsume1 + activeChar.getStat().getMpConsume(this))
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

		//recheck the sys messages, this are the suitible ones.
		if(getFameConsume() > 0)
		{
			if(player == null || player.getFame() < _fameConsume)
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.YOU_DONT_HAVE_ENOUGH_REPUTATION_TO_DO_THAT);
				return false;
			}
		}

		//must be in clan - no need to check it again
		if(getClanRepConsume() > 0)
		{
			if(player == null || player.getClan() == null || player.getClan().getReputationScore() < _cprConsume)
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
				return false;
			}
		}

		/* handled in TargetType.GROUND already. remove ?
		if(targetTypeNew == TargetType.GROUND)
		{
			if(!activeChar.isPlayer())
				return false;

			if(player.getGroundSkillLoc() == null)
				return false;
		}*/

		if(player != null)
		{
			if (player.getFlags().isControlBlocked() || (player.getStat().has(BooleanStat.BLOCK_ACTIONS) && !player.getStat().isBlockedActionsAllowedSkill(this)))
			{
				player.sendActionFailed();
				return false;
			}

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

			if (player.isInOlympiadMode() && getOlympiadUse() == 0) {
				if (sendMsg) {
					player.sendPacket(SystemMsg.YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_MATCH);
				}
				return false;
			}

			if(player.isInObserverMode())
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE);
				return false;
			}

			if(!isHandler() && activeChar.isPlayable() && first && getItemConsumeId() > 0 && getItemConsume() > 0)
			{
				if(ItemFunctions.getItemCount(isItemConsumeFromMaster() ? player : (Playable) activeChar, getItemConsumeId()) < getItemConsume())
				{
					if((isItemConsumeFromMaster() || activeChar == player) && sendMsg)
						player.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return false;
				}
			}

			if(player.isFishing() && !skillEntry.isAltUse() && !activeChar.isServitor())
			{
				if(activeChar == player && sendMsg)
					player.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
				return false;
			}

			if(player.isInTrainingCamp())
			{
				if(sendMsg)
					player.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
				return false;
			}
		}

		/* don't exist in classic
		switch(getFlyType())
		{
			case WARP_BACK:
			case WARP_FORWARD:
			case CHARGE:
			case DUMMY:
				if(activeChar.getStat().getValue(DoubleStat.BlockFly) == 1)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
		}*/

		// Warp (628) && Shadow Step (821) can be used while rooted
		/* TODO
		if(getFlyType() != FlyType.NONE && getId() != 628 && getId() != 821 && activeChar.isImmobilized())
		{
			if(sendMsg)
				activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}*/
		if (isFlyType() && activeChar.isMovementDisabled() && !activeChar.getStat().isBlockedActionsAllowedSkill(this))
		{
			activeChar.sendActionFailed();
			return false;
		}

		/*if(first && target != null && getFlyType() == FlyType.CHARGE)
		{
			boolean targetSelf = getTargetTypeNew() == TargetType.SELF || getTargetType() == SkillTargetType.TARGET_SELF;
			if(isApplyMinRange() && activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius())) && !targetSelf && !activeChar.isServitor())
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
				return false;
			}

			Location flyLoc = activeChar.getFlyLocation(target, this);
			if(flyLoc == null)
			{
				if(sendMsg)
					activeChar.sendPacket(SystemMsg.THE_TARGET_IS_LOCATED_WHERE_YOU_CANNOT_CHARGE);
				return false;
			}
		}*/

		if (isBad()) {
			if (!forceUse && target != null && target.isNpc() && !target.isAutoAttackable(activeChar)) {
				activeChar.sendActionFailed();
				return false;
			}
		}

		if (isGood()) {
			if (!forceUse && target != null && target.isNpc() && !target.isDead() && target.isAutoAttackable(activeChar)) {
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				return false;
			}
		}

		SystemMsg msg = checkTarget(skillEntry, activeChar, target, target, forceUse, first, trigger);
		if(msg != null && player != null)
		{
			if(sendMsg)
				player.sendPacket(msg);
			return false;
		}

		if (first) {
			// Check general conditions.
			if (!checkConditions(SkillConditionScope.GENERAL, activeChar, target) || !checkConditions(SkillConditionScope.TARGET, activeChar, target)) {
				final SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addSkillName(this);
				activeChar.sendPacket(sm);
				return false;
			}
		}

		return true;
	}

	public final SystemMsg checkTarget(SkillEntry skillEntry, Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		return checkTarget(skillEntry, activeChar, target, aimingTarget, forceUse, first, false);
	}

	public SystemMsg checkTarget(SkillEntry skillEntry, Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first, boolean trigger)
	{
		if (targetTypeNew != null) {
			if (target == null) {
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			}

			if (activeChar.equals(target) && isNotTargetAoE()) {
				return null;
			}

			/*if(!forceUse && !isForceUse() && !isNoFlagNoForce())
			{
				if(!isDebuff() && target.isAutoAttackable(activeChar))
					return SystemMsg.INVALID_TARGET;
				if(isDebuff() && !target.isAutoAttackable(activeChar))
					return SystemMsg.INVALID_TARGET;
			}*/

			Player player = activeChar.getPlayer();
			if (player != null) {
				Player pcTarget = target.getPlayer();
				if (pcTarget != null) {
					if (isBad()) {
						if (!pcTarget.equals(player)) {
							if (player.isInOlympiadMode()) {
								// Бой еще не начался
								if (!player.isOlympiadCompStart()) {
									return SystemMsg.INVALID_TARGET;
								}
								// Свою команду атаковать нельзя
								if (player.getOlympiadSide() == pcTarget.getOlympiadSide() && !forceUse) {
									return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
								}
							}
						}
					}
				}
			}

			if (affectObject.checkObject(activeChar, target, skillEntry.getTemplate(), forceUse)) {
				return null;
			}

			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		}

		if(target == activeChar && isNotTargetAoE())
			return null;
		if(target == null)
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(target == activeChar)
		{
			if(_targetType != null && _targetType != SkillTargetType.TARGET_SELF && isDebuff())
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			else if(targetTypeNew != null && targetTypeNew != TargetType.SELF && isDebuff())
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			else
				return null;
		}
		/*if(isPvpSkill() && target.isPeaceNpc()) // TODO: [Bonux] Запретить юзать только дебафф скиллы (оффлайк).
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;*/
		if(activeChar.getReflection() != target.getReflection())
			return SystemMsg.CANNOT_SEE_TARGET;
		if(target.isInvisible(activeChar))
			return SystemMsg.CANNOT_SEE_TARGET;
		// Попадает ли цель в радиус действия в конце каста
		if(!trigger) // TODO: Логично, но не вылазят ли косяки?
		{
			if(!first && target == aimingTarget && getCastRange() > 0 && !activeChar.isInRange(target.getLoc(), getCastRange() + (getCastRange() < 200 ? 400 : 500)))
				return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		}
		if(activeChar.isMyServitor(target.getObjectId()) && _targetType == SkillTargetType.TARGET_SERVITOR_AURA)
			return null;
		// Для этих скиллов дальнейшие проверки не нужны
		if(hasEffect("i_holything_possess"))
			return null;
		// Проверка на каст по трупу
		boolean isCorpseSkill = isCorpse() || (target == aimingTarget && _targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE);
		if(target.isDead() != isCorpseSkill || _isUndeadOnly && !target.isUndead())
			return SystemMsg.INVALID_TARGET;
		if(_targetType == SkillTargetType.TARGET_CORPSE || (target == aimingTarget && _targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE) || targetTypeNew == TargetType.NPC_BODY)
		{
			if(!target.isNpc() && !target.isSummon())
				return SystemMsg.INVALID_TARGET;
			return null;
		}
		// Для различных бутылок, и для скилла кормления, дальнейшие проверки не нужны
		if(skillEntry.isAltUse() || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST || targetTypeNew == TargetType.DOOR_TREASURE)
			return null;
		if(isDebuff() && target.isFakePlayer() && target.isInPeaceZone())
			return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
		Player player = activeChar.getPlayer();
		if(player != null)
		{
			// Запрет на атаку мирных NPC в осадной зоне на TW. Иначе таким способом набивают очки.
			//if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
			//	return SystemMsg.INVALID_TARGET;

			Player pcTarget = target.getPlayer();
			if(pcTarget != null)
			{
				/*if(isPvM())
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;*/

				if(pcTarget != player)
				{
					if(player.isInZone(ZoneType.epic) != pcTarget.isInZone(ZoneType.epic))
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

					if(pcTarget.isInOlympiadMode() && (!player.isInOlympiadMode() || player.getOlympiadGame() != pcTarget.getOlympiadGame())) // На всякий случай
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				}

				if(isDebuff())
				{
					if(pcTarget != player)
					{
						if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Бой еще не начался
							return SystemMsg.INVALID_TARGET;
						if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcTarget.getOlympiadSide() && !forceUse) // Свою команду атаковать нельзя
							return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
						if(pcTarget.isInNonPvpTime())
							return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
					}

					if(isAoE() && !GeoEngine.canSeeTarget(activeChar, target))
						return SystemMsg.CANNOT_SEE_TARGET;

					if(pcTarget != player)
					{
						if(activeChar.isInZoneBattle() != target.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack)
							return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
					}

					if((activeChar.isInPeaceZone() || target.isInPeaceZone()) && !player.getPlayerAccess().PeaceAttack)
						return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;

					if(pcTarget != player)
					{
						SystemMsg msg = null;
						for(Event e : activeChar.getEvents())
							if((msg = e.checkForAttack(target, activeChar, this, forceUse)) != null)
								return msg;

						for(Event e : activeChar.getEvents())
							if(e.canAttack(target, activeChar, this, forceUse, false))
								return null;

						if(activeChar.isInZoneBattle())
						{
							if(!forceUse && !isForceUse() && player.getParty() != null && player.getParty() == pcTarget.getParty())
								return SystemMsg.INVALID_TARGET;
							return null; // Остальные условия на аренах и на олимпиаде проверять не требуется
						}
					}

					if(/*isPvpSkill() || */!forceUse || isAoE())
					{
						if(player == pcTarget)
							return SystemMsg.INVALID_TARGET;

						if(player.getParty() != null && player.getParty() == pcTarget.getParty())
							return SystemMsg.INVALID_TARGET;
						if(player.isInParty() && player.getParty().getCommandChannel() != null && pcTarget.isInParty() && pcTarget.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel())
							return SystemMsg.INVALID_TARGET;

						if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
							return SystemMsg.INVALID_TARGET;
						if(player.getClan() != null && player.getClan().getAlliance() != null && pcTarget.getClan() != null && pcTarget.getClan().getAlliance() != null && player.getClan().getAlliance() == pcTarget.getClan().getAlliance())
							return SystemMsg.INVALID_TARGET;

						/*if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
							return SystemMsg.INVALID_TARGET;   */
					}

					if(pcTarget != player)
					{
						if(activeChar.isInSiegeZone() && target.isInSiegeZone())
							return null;

						if(player.atMutualWarWith(pcTarget))
							return null;
					}

					if(isForceUse())
						return null;

					// DS: Убрано. Защита от развода на флаг с копьем
					/*if(!forceUse && player.getPvpFlag() == 0 && pcTarget.getPvpFlag() != 0 && aimingTarget != target)
						return SystemMsg.INVALID_TARGET;*/

					if(pcTarget != player)
					{
						if(pcTarget.getPvpFlag() != 0)
							return null;
						if(pcTarget.isPK())
							return null;
					}

					if(forceUse /*&& !isPvpSkill()*/ && (!isAoE() || aimingTarget == target))
						return null;

					return SystemMsg.INVALID_TARGET;
				}

				if(pcTarget == player)
					return null;

				if(player.isInOlympiadMode() && !forceUse && player.getOlympiadSide() != pcTarget.getOlympiadSide()) // Чужой команде помогать нельзя
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				//TODO [VISTALL] что за?
				if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() != TeamType.NONE && player.getTeam() != pcTarget.getTeam()) // Чужой команде помогать нельзя
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				if(!activeChar.isInZoneBattle() && target.isInZoneBattle())
					return SystemMsg.INVALID_TARGET;
				// DS: на оффе можно использовать неатакующие скиллы из мирной зоны в поле.
				/*if(activeChar.isInPeaceZone() && !target.isInPeaceZone())
					return SystemMsg.INVALID_TARGET;*/

				if(forceUse || isForceUse())
					return null;

				/*if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
					return SystemMsg.INVALID_TARGET;
				if(player != pcTarget && player.getDuel() != null && pcTarget.getDuel() != null && pcTarget.getDuel() == pcTarget.getDuel())
					return SystemMsg.INVALID_TARGET;*/

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

				return null;
			}
		}

		if(!trigger || target != aimingTarget) // TODO: Логично, но не вылазят ли косяки?
		{
			if(isAoE() && isDebuff() && !GeoEngine.canSeeTarget(activeChar, target))
				return SystemMsg.CANNOT_SEE_TARGET;
		}
		if(!forceUse && !isForceUse() && !isNoFlagNoForce())
		{
			if(!isDebuff() && target.isAutoAttackable(activeChar))
				return SystemMsg.INVALID_TARGET;
			if(isDebuff() && !target.isAutoAttackable(activeChar))
				return SystemMsg.INVALID_TARGET;
		}
		if(!target.isAttackable(activeChar))
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	@Deprecated
	private final Creature getAimingTarget(Creature activeChar, GameObject obj)
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
				return activeChar;
			case TARGET_AURA:
			case TARGET_COMMCHANNEL:
			case TARGET_GROUND:
			case TARGET_FAN_PB:
			case TARGET_SQUARE_PB:
				return activeChar;
			case TARGET_HOLY:
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			case TARGET_FLAGPOLE:
				return activeChar;
			case TARGET_UNLOCKABLE:
				return target != null && target.isDoor() || target instanceof ChestInstance ? target : null;
			case TARGET_CHEST:
				return target instanceof ChestInstance ? target : null;
			case TARGET_SERVITORS:
			case TARGET_SELF_AND_SUMMON:
				return activeChar;
			case TARGET_ONE_SERVITOR:
			case TARGET_SERVITOR_AURA:
				return target != null && target.isServitor() && activeChar.isMyServitor(target.getObjectId()) && target.isDead() == isCorpse() ? target : null;
			case TARGET_ONE_SERVITOR_NO_TARGET:
				target = activeChar.getPlayer().getAnyServitor();
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_SUMMON:
				target = activeChar.isPlayer() ? activeChar.getPlayer().getSummon() : null;
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_PET:
				target = activeChar.isPlayer() ? activeChar.getPlayer().getPet() : null;
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_OWNER:
				if(activeChar.isServitor())
					target = activeChar.getPlayer();
				else
					return null;
				return target != null && target.isDead() == isCorpse() ? target : null;
			case TARGET_ENEMY_PET:
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isPet())
					return null;
				return target;
			case TARGET_ENEMY_SUMMON:
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isSummon())
					return null;
				return target;
			case TARGET_ENEMY_SERVITOR:
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isServitor())
					return null;
				return target;
			case TARGET_ONE:
				return target != null && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_CLAN_ONE:
//				return target != null && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()) && activeChar.getPlayer().isInSameClan(target.getPlayer()) ? target : null;
				if(target == null)
					return null;
				Player cplayer = activeChar.getPlayer();
				Player cptarget = target.getPlayer();
				// self or self pet.
				if(cptarget != null && cptarget == activeChar)
					return target;
				// olympiad party member or olympiad party member pet.
				if(cplayer != null && cplayer.isInOlympiadMode() && cptarget != null && cplayer.getOlympiadSide() == cptarget.getOlympiadSide() && cplayer.getOlympiadGame() == cptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(cptarget != null && cplayer != null && cplayer.getClan() != null && cplayer.isInSameClan(cptarget) && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_PARTY_ONE:
				if(target == null)
					return null;
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				// self or self pet.
				if(ptarget != null && ptarget == activeChar)
					return target;
				// olympiad party member or olympiad party member pet.
				if(player != null && player.isInOlympiadMode() && ptarget != null && player.getOlympiadSide() == ptarget.getOlympiadSide() && player.getOlympiadGame() == ptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_PARTY_ONE_WITHOUT_ME:
				if(target == null)
					return null;
				player = activeChar.getPlayer();
				ptarget = target.getPlayer();
				// self or self pet.
				if(ptarget != null && ptarget == activeChar)
					return null;
				// olympiad party member or olympiad party member pet.
				if(player != null && player.isInOlympiadMode() && ptarget != null && player.getOlympiadSide() == ptarget.getOlympiadSide() && player.getOlympiadGame() == ptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_AREA:
			case TARGET_FAN:
			case TARGET_SQUARE:
			case TARGET_RANGE:
			case TARGET_RING_RANGE:
				return target != null && target.isDead() == isCorpse() && !(target == activeChar && isDebuff()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_AREA_AIM_CORPSE:
				return target != null && target.isDead() ? target : null;
			case TARGET_CORPSE:
				if(target == null || !target.isDead())
					return null;
				if(target.isSummon() && !activeChar.isMyServitor(target.getObjectId())) // использовать собственного мертвого самона нельзя
					return target;
				return target.isNpc() ? target : null;
			case TARGET_CORPSE_PLAYER:
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			case TARGET_SIEGE:
				return target != null && !target.isDead() && target.isDoor() ? target : null;
			default:
				activeChar.sendMessage("Target type of skill is not currently handled");
				return null;
		}
	}

	public final Creature getAimingTarget(Creature caster, GameObject selectedTarget, Skill skill, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		if (targetTypeNew == null) {
			return getAimingTarget(caster, selectedTarget);
		}

		return targetTypeNew.getTarget(caster, selectedTarget, skill, forceUse, dontMove, sendMessage);
	}

	public List<Creature> getTargets(Creature caster, Creature selectedTarget, SkillEntry skillEntry, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		return affectScope.getAffected(caster, selectedTarget, skillEntry, forceUse, dontMove, sendMessage);

		/*for(Event e : caster.getEvents())
			e.checkTargetsForSkill(this, targets, caster, selectedTarget, forceUse);*/
	}

	public void checkTargetsEffectiveRange(Creature caster, List<Creature> targets)
	{
		if(targets == null)
			return;

		// TODO find what this values means
		if (getEffectiveRange() == 0 || getEffectiveRange() == -1 || getEffectiveRange() == -2) {
			return;
		}

		for(Iterator<Creature> iterator = targets.iterator(); iterator.hasNext();)
		{
			Creature target = iterator.next();
			if (!caster.isInRangeZ(target, getEffectiveRange())) {
				iterator.remove();
			}

			/* skills should work on invisible chars
			if(target.isInvisible(caster))
				iterator.remove();*/
		}
	}

	@Deprecated
	public boolean calcCriticalBlow(Creature caster, Creature target)
	{
		return false;
	}

	public final boolean getEffects(final Creature effector, final Creature effected)
	{
		return getEffects(effector, effected, true);
	}

	public final boolean getEffects(final Creature effector, final Creature effected, final boolean saveable)
	{
		double timeMult = 1.0;

		if(isMusic())
			timeMult = Config.SONGDANCETIME_MODIFIER;
		else if(getId() >= 4342 && getId() <= 4360)
			timeMult = Config.CLANHALL_BUFFTIME_MODIFIER;
		else if(Config.BUFFTIME_MODIFIER_SKILLS.length > 0)
		{
			for(int i : Config.BUFFTIME_MODIFIER_SKILLS)
			{
				if (i == getId()) {
					timeMult = Config.BUFFTIME_MODIFIER;
					break;
				}
			}
		}

		return getEffects(effector, effected, 0, timeMult, saveable);
	}

	public final boolean getEffects(final Creature effector, final Creature effected, final int timeConst, final double timeMult)
	{
		return getEffects(effector, effected, timeConst, timeMult, true);
	}

	public final boolean getEffects(final Creature effector,
									final Creature effected,
									final int timeConst,
									final double timeMult,
									final boolean saveable)
	{
		return getEffects(effector,
				effected,
				EffectUseType.NORMAL,
				true,
				timeConst,
				timeMult,
				saveable);
	}

	/**
	 * Применить эффекты скилла
	 *
	 * @param effector персонаж, со стороны которого идет действие скилла, кастующий
	 * @param effected персонаж, на которого действует скилл
	 * @param timeConst изменить время действия эффектов до данной константы (в миллисекундах)
	 * @param timeMult изменить время действия эффектов с учетом данного множителя
	 */
	public final boolean getEffects(final Creature effector,
									 final Creature effected,
									 final EffectUseType useType,
									 final boolean update,
									 final int timeConst,
									 final double timeMult,
									 final boolean saveable)
	{
		if(isPassive() || effector == null)
			return false;

		if(useType.isInstant())
		{
			_log.atWarning().log( "Cannot get effects from instant effect use type:" );
			Thread.dumpStack();
			return false;
		}

		if(!isToggle())
		{
			/*if(useType.isSelf() && !_operateType.isSelfContinuous())
				return false;
			if(!useType.isSelf() && !_operateType.isContinuous())
				return false;*/
		}

		/*TODO: Правильо ли? Скорее всего просто в эфффект нужно добавить кондишон.
		if(effector.isAlikeDead())
			return false;
		*/

		if(!hasEffects(useType))
			return true;

		if(effected == null || effected.isDoor() || effected.isDead() && !isPreservedOnDeath()) //why alike dead??
			return false;

		if(effector != effected)
		{
			if(useType == EffectUseType.NORMAL)
			{
				if(effected.isEffectImmune(effector))
					return false;
			}
		}

		/*boolean addContinuousEffects = _operateType.isToggle() || (_operateType.isContinuous() && Formulas.INSTANCE.calcEffectSuccess(effector, effected, this));
		if (!addContinuousEffects) {
			return false;
		}*/

		boolean reflected = false;
		if(useType == EffectUseType.NORMAL)
			reflected = effected.checkReflectDebuff(effector, this);

		Set<Creature> targets = new HashSet<Creature>(1);
		if(useType == EffectUseType.SELF)
			targets.add(effector);
		else
		{
			// TODO: При рефлекте должен ли накладываться эффект на цель?
			if(reflected)
				targets.add(effector);
			else
				targets.add(effected);
		}

		if(useType == EffectUseType.NORMAL)
		{
			if((applyEffectsOnSummon() || applyEffectsOnPet()) && !isBad() && !isToggle() && !isCubicSkill())
			{
				Creature owner;
				/*if(useType == EffectUseType.SELF) // TODO: Проверить, SELF эффекты тоже кидать на саммона?
					owner = effector;
				else
				{*/
				if(reflected)
					owner = effector;
				else
					owner = effected;
				//}

				if(owner.isPlayer())
				{
					for(Servitor servitor : owner.getPlayer().getServitors())
					{
						if(applyEffectsOnSummon() && servitor.isSummon())
							targets.add(servitor);
						else if(applyEffectsOnPet() && servitor.isPet())
							targets.add(servitor);
					}
				}
			}
		}

		boolean successOnEffected = false;

		for(Creature target : targets)
		{
			final Abnormal abnormal = new Abnormal(effector, target, this, useType, reflected, saveable);

			double abnormalTimeModifier = Math.max(1., timeMult);

			int duration = abnormal.getDuration();

			if(timeConst > 0)
				duration = timeConst / 1000; // TODO: Пределать, чтобы посылалось в секундах.
			else if(abnormalTimeModifier > 1.0)
				duration *= abnormalTimeModifier;

			abnormal.setDuration(duration);

			if(abnormal.apply(effected, update))
			{
				// Check for mesmerizing debuffs and increase resist level.
				if(isDebuff() && (getBasicProperty() != BasicProperty.NONE) && target.hasBasicPropertyResist())
					target.getBasicPropertyResist(getBasicProperty()).increaseResistLevel();

				if(target == effected)
					successOnEffected = true;
			}

			if(target == effected)
			{
				if(reflected)
				{
					target.sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(effector));
					effector.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(target));
				}
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

	public boolean hasEffect(EffectUseType useType, String name)
	{
		List<EffectTemplate> templates = getEffectTemplates(useType);
		for(EffectTemplate et : templates)
		{
			if(et.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public boolean hasEffect(EffectUseType useType, Class<? extends EffectHandler>... effectClasses)
	{
		List<EffectTemplate> templates = getEffectTemplates(useType);
		for(EffectTemplate et : templates)
		{
			for (Class<? extends EffectHandler> effectClass : effectClasses) {
				if(et.getName().equalsIgnoreCase(effectClass.getSimpleName()))
					return true;
			}
		}
		return false;
	}

	@Deprecated
	public boolean hasEffect(String... effectNames)
	{
		for (List<EffectTemplate> effectTemplates : _effectTemplates.valueCollection()) {
			for(EffectTemplate et : effectTemplates) {
				for (String effectName : effectNames) {
					if (et.getName().equalsIgnoreCase(effectName))
						return true;
				}
			}
		}

		return false;
	}

	public boolean hasEffect(Class<? extends EffectHandler>... effectClasses)
	{
		for (List<EffectTemplate> effectTemplates : _effectTemplates.valueCollection()) {
			for(EffectTemplate et : effectTemplates) {
				for (Class<? extends EffectHandler> effectClass : effectClasses) {
					if (et.getName().equalsIgnoreCase(effectClass.getSimpleName()))
						return true;
				}
			}
		}

		return false;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	/**
	 * @return Returns the id.
	 */
	@Override
	public final int getId()
	{
		return _id;
	}

	public int getOlympiadUse() {
		return olympiadUse;
	}

	public int getBuffProtectLevel() {
		return buffProtectLevel;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;

		if(!(obj instanceof Skill))
			return false;

		Skill skill = (Skill) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(getId(), skill.getId());
		builder.append(getLevel(), skill.getLevel());
		builder.append(getClass(), skill.getClass());
		return builder.isEquals();
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

	/**
	 * Adds a condition to the condition list for the given condition scope.
	 * @param skillConditionScope the condition scope
	 * @param skillCondition the condition
	 */
	public void addCondition(SkillConditionScope skillConditionScope, SkillCondition skillCondition)
	{
		conditions.computeIfAbsent(skillConditionScope, k -> new ArrayList<>())
				.add(skillCondition);
	}

	/**
	 * Checks the conditions of this skills for the given condition scope.
	 * @param skillConditionScope the condition scope
	 * @param caster the caster
	 * @param target the target
	 * @return {@code false} if at least one condition returns false, {@code true} otherwise
	 */
	public boolean checkConditions(SkillConditionScope skillConditionScope, Creature caster, Creature target)
	{
		return conditions.getOrDefault(skillConditionScope, Collections.emptyList())
				.stream()
				.allMatch(c -> c.canUse(caster, this, target));
	}

	public final boolean isAltUse(SkillEntryType entryType)
	{
		return (_altUse || _isItemHandler || entryType == SkillEntryType.CUNSUMABLE_ITEM) && _hitTime <= 0;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills;
	}

	/**
	 * @return Returns the castRange.
	 */
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

	public boolean isCorpse()
	{
		return _isCorpse || _targetType == SkillTargetType.TARGET_CORPSE || _targetType == SkillTargetType.TARGET_CORPSE_PLAYER || targetTypeNew == TargetType.PC_BODY || targetTypeNew == TargetType.NPC_BODY;
	}

	@Override
	public final int getDisplayId()
	{
		return _displayId;
	}

	@Override
	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	@Override
	public final Skill getTemplate() {
		return this;
	}

	public int getEffectPoint()
	{
		return _effectPoint;
	}

	public boolean isBad() {
		return _effectPoint < 0;
	}

	public boolean isGood() {
		return _effectPoint > 0;
	}

	public Abnormal getSameByAbnormalType(Collection<Abnormal> list)
	{
		for(Abnormal abnormal : list)
		{
			if(abnormal != null && AbnormalList.checkAbnormalType(abnormal.getSkill(), this))
				return abnormal;
		}
		return null;
	}

	public Abnormal getSameByAbnormalType(AbnormalList list)
	{
		return getSameByAbnormalType(list.values());
	}

	public Abnormal getSameByAbnormalType(Creature actor)
	{
		return getSameByAbnormalType(actor.getAbnormalList());
	}

	public final AttributeType getAttributeType()
	{
		return attributeType;
	}

	public final int getAttributePower()
	{
		return attributePower;
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

	public FlyType getFlyType()
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

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final long getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	@Deprecated
	public final boolean isItemConsumeFromMaster()
	{
		return false;
	}

	/**
	 * @return Возвращает ид предмета(талисмана)
	 * ману которого надо использовать
	 */
	public final int getReferenceItemId()
	{
		return _referenceItemId;
	}

	/**
	 * @return Возвращает используемое для каста количество маны
	 * предмета(талисмана) 
	 */
	public final int getReferenceItemMpConsume()
	{
		return _referenceItemMpConsume;
	}

	/**
	 * @return Returns the level.
	 */
	@Override
	public final int getLevel()
	{
		return _level;
	}

	public final int getMaxLevel()
	{
		return SkillHolder.getInstance().getMaxSkillLevel(getId());
	}

	public final int getLevelBonusRate()
	{
		return _levelBonusRate;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public PledgeRank getMinPledgeRank()
	{
		return _minPledgeRank;
	}

	public boolean clanLeaderOnly()
	{
		return _clanLeaderOnly;
	}

	/**
	 * @return Returns the mpConsume as _mpConsume1 + _mpConsume2.
	 */
	public final double getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}

	/**
	 * @return Returns the mpConsume1.
	 */
	public final double getMpConsume1()
	{
		return _mpConsume1;
	}

	/**
	 * @return Returns the mpConsume2.
	 */
	public final double getMpConsume2()
	{
		return _mpConsume2;
	}

	/**
	 * @return Returns the mpConsumeTick.
	 */
	public final double getMpConsumeTick()
	{
		return _mpConsumeTick;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	public final String getName(Player player)
	{
		String name = SkillNameHolder.getInstance().getSkillName(player, this);
		return name == null ? _name : name;
	}

	public final NextActionType getNextAction()
	{
		return _nextAction;
	}

	public final int getChargeConsume()
	{
		return chargeConsume;
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
		return _powerPvP != 0 ? _powerPvP : _power;
	}

	public final double getPowerPvE()
	{
		return _powerPvE != 0 ? _powerPvE : _power;
	}

	public final int getReuseDelay()
	{
		return _reuseDelay;
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

	public final int getHitCancelTime()
	{
		return _hitCancelTime;
	}

	public final AddedSkill getAttachedSkill()
	{
		return _attachedSkill;
	}

	public final int getChannelingStart()
	{
		return _channelingStart;
	}

	public final int getAffectRange()
	{
		return _affectRange;
	}

	public final int[] getFanRange()
	{
		return _fanRange;
	}

	public final int getAffectLimit()
	{
		if(_affectLimit[0] == 0 && _affectLimit[1] == 0)
			return Integer.MAX_VALUE;
		if(_affectLimit[0] > 0 || _affectLimit[1] > 0)
			return _affectLimit[0] + (_affectLimit[1] > 0 ? Rnd.get(_affectLimit[1]) : 0);
		return 0;
	}

	public final int getEffectiveRange()
	{
		return _effectiveRange;
	}

	@Deprecated
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public TargetType getTargetTypeNew() {
		return targetTypeNew;
	}

	public AffectScope getAffectScope() {
		return affectScope;
	}

	public AffectObject getAffectObject() {
		return affectObject;
	}

	public final TraitType getTraitType()
	{
		return _traitType;
	}

	/**
	 * @return {@code true} if skill effects should be removed on damage
	 */
	public final boolean isRemovedOnDamage()
	{
		// TODO add ids
		return abnormalTypeList.contains(AbnormalType.SLEEP);
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
			return 0.;

		if(_lethal2Addon == 0. || _lethal2SkillDepencensyAddon == 0)
			return 0.;

		if(player.getAbnormalList().contains(_lethal2SkillDepencensyAddon))
			return _lethal2Addon;

		return 0.;
	}

	private double getAddedLethal1(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return 0.;

		if(_lethal1Addon == 0. || _lethal1SkillDepencensyAddon == 0)
			return 0.;

		if(player.getAbnormalList().contains(_lethal1SkillDepencensyAddon))
			return _lethal1Addon;

		return 0.;
	}

	@Deprecated
	public final boolean isCancelable()
	{
		return canBeDispelled() && _isSelfDispellable && !hasEffect(EffectUseType.NORMAL, "Transformation") && !isToggle() && !isAura();
	}

	public final boolean isSelfDispellable()
	{
		return _isSelfDispellable && !hasEffect(EffectUseType.NORMAL, "Transformation")
				&& !isToggle() && !isBad() && (!isMusic() || Config.SONGDANCE_SELF_DISPELLABLE);
	}

	public boolean canBeDispelled()
	{
		return !undeletable && _magicLevel >= 0;
	}

	/**
	 * Verify if the skill can be stolen.
	 * @return {@code true} if skill can be stolen, {@code false} otherwise
	 */
	public boolean canBeStolen()
	{
		return !isPassive() && !isToggle() && !isDebuff() && !isIrreplaceableBuff() && !isHeroSkill() && !isGMSkill() && !(isSpecial() && (getId() != CommonSkill.CARAVANS_SECRET_MEDICINE.getId())) && canBeDispelled();
	}

	public final double getMagicCriticalRate()
	{
		return _criticalRate;
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
		if(_magicType == SkillMagicType.UNK_MAG_TYPE_21) // TODO: Check.
			return true;
		return _magicType == SkillMagicType.PHYSIC || _magicType == SkillMagicType.MUSIC || _magicType == SkillMagicType.TRIGGER;
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

	public final boolean isPreservedOnDeath()
	{
		return buffProtectLevel == 31 || isNecessaryToggle();
	}

	public final boolean isOverhit()
	{
		return _isOverhit;
	}

	public boolean isSaveable()
	{
		if(!Config.ALT_SAVE_UNSAVEABLE && (isMusic() || isAbnormalInstant()))
			return false;
		return _isSaveable || (isToggle() && isNecessaryToggle());
	}

	/**
	 * На некоторые скиллы и хендлеры предметов скорости каста/атаки не влияет
	 */
	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || isHandler() || _name.contains("Talisman") || isChanneling();
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
		return _hideStartMessage || isHidingMesseges();
	}

	public boolean isHideUseMessage()
	{
		return _hideUseMessage || isHidingMesseges();
	}

	/**
	 * Может ли скилл тратить шоты, для хендлеров всегда false
	 */
	public boolean isSSPossible()
	{
		if (useSoulShot()) {
			return true;
		}

		if (useSpiritShot()) {
			return true;
		}

		return false;
	}

	public boolean useSoulShot()
	{
		return _magicType == SkillMagicType.PHYSIC;
	}

	public boolean useSpiritShot()
	{
		return _magicType == SkillMagicType.MAGIC && getOperateType() != SkillOperateType.T;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public SkillOperateType getOperateType()
	{
		return _operateType;
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

	public boolean isToggleGrouped()
	{
		return _operateType.isToggleGrouped();
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

	public boolean isSynergy()
	{
		return _operateType.isSynergy();
	}

	public boolean isFlyType()
	{
		return _operateType.isFlyType();
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
		return String.format("%s[id=%d, lvl=%d, hash_code=%d]", _name, _id, _level, hashCode());
	}

	private final boolean checkCastTarget(Creature target)
	{
		//Фильтруем неуязвимые цели
		/*TODO: Переделать.
		if(isDebuff() && target.isInvulnerable())
		{
			Player player = target.getPlayer();
			if((!isIgnoreInvul() || player != null && player.isGM()) && !target.isArtefact())
				return false;
		}*/

		return !target.isIgnoredSkill(this);
	}

	private final boolean applyEffectPoint(Creature activeChar, Creature target)
	{
		if(getEffectPoint() < 0)
		{
			activeChar.getAI().notifyEvent(CtrlEvent.EVT_ATTACK, target, this, -getEffectPoint());
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, this, -getEffectPoint());
			return true;
		}
		return false;
	}

	public final void onStartCast(SkillEntry skillEntry, Creature activeChar, Creature target)
	{
		if(target == null)
			return;

		if(isPassive())
			return;

		if(!hasEffects(EffectUseType.START))
			return;

		boolean startAttackStance = false;

		if(!checkCastTarget(target))
			return;

		if (skillEntry.getEntryType() != SkillEntryType.CUBIC) {
			if (applyEffectPoint(activeChar, target)) {
				startAttackStance = true;
			}
		}

		AtomicBoolean soulShotUsed = new AtomicBoolean(false);

		for(EffectTemplate et : getEffectTemplates(EffectUseType.START))
			useInstantEffect(et, activeChar, target, soulShotUsed, false);

		// TODO: Проверить, должно оно ли при старте каста юзать соски.
		if(skillEntry.getEntryType() != SkillEntryType.CUBIC) {
			if (useSpiritShot()) {
				activeChar.unChargeShots(true);
			}

			if (soulShotUsed.get()) {
				activeChar.unChargeShots(false);
			}
		}

		if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public final void onTickCast(SkillEntry skillEntry, Creature activeChar, List<Creature> targets)
	{
		if(isPassive())
			return;

		if(!isChanneling())
			return;

		boolean startAttackStance = false;

		AtomicBoolean soulShotUsed = new AtomicBoolean(false);

		AddedSkill attachedSkill = getAttachedSkill();
		if(attachedSkill != null)
		{
			//TODO: Уровень скилла должен зависит от количества кастующих данный тик.

			SkillEntry attackedSkillEntry = attachedSkill.getSkill();
			if(attackedSkillEntry == null)
			{
				// TODO: Надо ли аборт атаки?
				return;
			}

			Skill skill = attackedSkillEntry.getTemplate();

			for(Creature target : targets)
			{
				if(target == null)
					continue;

				if(!skill.checkCastTarget(target))
					continue;

				if (attackedSkillEntry.getEntryType() != SkillEntryType.CUBIC) {
					if (skill.applyEffectPoint(activeChar, target)) {
						startAttackStance = true;
					}
				}

				final boolean reflected = target.checkReflectSkill(activeChar, skill);

				for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL_INSTANT))
					skill.useInstantEffect(et, activeChar, target, soulShotUsed, reflected);

				if(skill.hasEffects(EffectUseType.NORMAL) && Formulas.INSTANCE.calcEffectSuccess(activeChar, target, this))
				{
					skill.getEffects(activeChar, target);
				}
			}
		}
		else if(hasEffects(EffectUseType.TICK))
		{
			for(Creature target : targets)
			{
				if(target == null)
					continue;

				if(!checkCastTarget(target))
					continue;

				if (skillEntry.getEntryType() != SkillEntryType.CUBIC) {
					if (applyEffectPoint(activeChar, target)) {
						startAttackStance = true;
					}
				}

				for(EffectTemplate et : getEffectTemplates(EffectUseType.TICK))
					useInstantEffect(et, activeChar, target, soulShotUsed, false);
			}
		}

		// TODO: Проверить, должно оно ли при тике каста юзать соски.
		if(skillEntry.getEntryType() != SkillEntryType.CUBIC) {
			if (useSpiritShot()) {
				activeChar.unChargeShots(true);
			}

			if (soulShotUsed.get()) {
				activeChar.unChargeShots(false);
			}
		}

		if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public void onEndCast(SkillEntry skillEntry, Creature activeChar, List<Creature> targets, Cubic cubic)
	{
		if(isPassive())
			return;

		AtomicBoolean soulShotUsed = new AtomicBoolean(false);

		// Особое условие для атакующих аура-скиллов (Vengeance 368):
		// если ни одна цель не задета то селфэффекты не накладываются
		if(!(isNotTargetAoE() && isBad() && targets.size() == 0))	// TODO: Check this.
		{
			for(EffectTemplate et : getEffectTemplates(EffectUseType.SELF_INSTANT))
				useInstantEffect(et, activeChar, activeChar, soulShotUsed, false);

			getEffects(activeChar, activeChar, EffectUseType.SELF, true, 0, 1.0, true);
		}

		boolean startAttackStance = false;

		List<SkillTarget> skillTargets = new ArrayList<>(targets.size());
		for (Creature target : targets) {
			if (target == null)
				continue;

			if (!checkCastTarget(target))
				continue;

			if (skillEntry.getEntryType() != SkillEntryType.CUBIC) {
				if (applyEffectPoint(activeChar, target)) {
					startAttackStance = true;
				}
			}

			final boolean reflected = target.checkReflectSkill(activeChar, this);

			skillTargets.add(new SkillTarget(target, reflected));
		}

		for (EffectTemplate et : getEffectTemplates(EffectUseType.NORMAL_INSTANT)) {
			useInstantEffect(et, activeChar, skillTargets, soulShotUsed, cubic);
		}

		if (hasEffects(EffectUseType.NORMAL)) {
			for (SkillTarget skillTarget : skillTargets) {
				Creature target = skillTarget.getTarget();
				if (Formulas.INSTANCE.calcEffectSuccess(activeChar, target, this)) {
					getEffects(activeChar, target);
				}
			}
		}

		if(skillEntry.getEntryType() != SkillEntryType.CUBIC) {
			if (useSpiritShot()) {
				activeChar.unChargeShots(true);
			}

			if (soulShotUsed.get()) {
				activeChar.unChargeShots(false);
			}
		}

		if(isSuicideAttack()) // TODO: Переделать на селф эффект.
			activeChar.doDie(null);
		else if(startAttackStance)
			activeChar.startAttackStanceTask();
	}

	public void onFinishCast(Creature aimingTarget, Creature activeChar, List<Creature> targets)
	{
		if(isBad())
		{
			if(getTargetTypeNew() == TargetType.NPC_BODY || getTargetTypeNew() == TargetType.PC_BODY)
			{
				if(aimingTarget.isNpc())
					((NpcInstance) aimingTarget).endDecayTask();
				else if(aimingTarget.isSummon())
					((SummonInstance) aimingTarget).endDecayTask();
			}
			if(getTargetType() == SkillTargetType.TARGET_AREA_AIM_CORPSE)
			{
				if(aimingTarget.isNpc())
					((NpcInstance) aimingTarget).endDecayTask();
				else if(aimingTarget.isSummon())
					((SummonInstance) aimingTarget).endDecayTask();
			}
			else if(getTargetType() == SkillTargetType.TARGET_CORPSE)
			{
				for(Creature target : targets)
				{
					if(target.isNpc())
						((NpcInstance) target).endDecayTask();
					else if(target.isSummon())
						((SummonInstance) target).endDecayTask();
				}
			}
		}
	}

	/**
	 * Срабатывает по завершению времени действия эффекта.
	 *
	 * @param activeChar
	 * @param target
	 */
	public void onAbnormalTimeEnd(Creature activeChar, Creature target)
	{
		if(!checkCastTarget(target))
			return;

		AtomicBoolean soulShotUsed = new AtomicBoolean(false);

		for(EffectTemplate et : getEffectTemplates(EffectUseType.END))
			// need ? if (calcEffectsSuccess(activeChar, target, false))
				useInstantEffect(et, activeChar, target, soulShotUsed, false);

		if (soulShotUsed.get()) {
			activeChar.unChargeShots(false);
		}
	}

	/**
	 * Срабатывает по завершению эффекта (любым способом: время вышло, принудительно и т.д.).
	 *
	 * @param activeChar
	 * @param target
	 */
	public void onSkillEnd(Creature activeChar, Creature target)
	{
	}

	private boolean useInstantEffect(EffectTemplate et,
									 Creature activeChar,
									 List<SkillTarget> targets,
									 AtomicBoolean soulShotUsed,
									 Cubic cubic)
	{
		if(!et.isInstant())
			return false;

		final EffectHandler handler = et.getHandler();
		// need ?
		// if(!handler.checkPumpConditionImpl(null, activeChar, target))
		//	return false;

		handler.instantUse(activeChar, targets, soulShotUsed, cubic);
		return true;
	}

	@Deprecated
	private boolean useInstantEffect(EffectTemplate et,
									 Creature activeChar,
									 Creature target,
									 AtomicBoolean soulShotUsed,
									 boolean reflected)
	{
		if(!et.isInstant())
			return false;

		if(!et.getTargetType().checkTarget(target))
			return false;

		//if(et.getChance() >= 0 && !Rnd.chance(et.getChance()))
		//	return false;

		final EffectHandler handler = et.getHandler();
		// need ?
		// if(!handler.checkPumpConditionImpl(null, activeChar, target))
		//	return false;

		if (!handler.calcSuccess(activeChar, target, et.getSkill()))
			return false;

		handler.instantUse(activeChar, target, soulShotUsed, reflected, null);

		return true;
	}

	@Deprecated
	public boolean isAoE()
	{
		if (affectScope != AffectScope.SINGLE) {
			return true;
		}

		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AURA:
			case TARGET_SERVITOR_AURA:
			case TARGET_GROUND:
			case TARGET_FAN:
			case TARGET_FAN_PB:
			case TARGET_SQUARE:
			case TARGET_SQUARE_PB:
			case TARGET_RANGE:
			case TARGET_RING_RANGE:
				return true;
			default:
				return false;
		}
	}

	@Deprecated
	public boolean isNotTargetAoE()
	{
		if (targetTypeNew == TargetType.SELF && affectScope != AffectScope.SINGLE) {
			return true;
		}

		return false;
	}

	public boolean isDebuff()
	{
		return debuff > 0;
	}

	public final boolean isForceUse()
	{
		return _isForceUse;
	}

	public boolean isTrigger()
	{
		return _magicType == SkillMagicType.TRIGGER;
	}

	public boolean oneTarget()
	{
		if (affectScope != null) {
			return affectScope == AffectScope.SINGLE;
		}

		// TODO remove
		switch(_targetType)
		{
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_HOLY:
			case TARGET_FLAGPOLE:
			case TARGET_ITEM:
			case TARGET_NONE:
			case TARGET_ONE:
			case TARGET_CLAN_ONE:
			case TARGET_PARTY_ONE:
			case TARGET_PARTY_ONE_WITHOUT_ME:
			case TARGET_ONE_SERVITOR:
			case TARGET_ONE_SERVITOR_NO_TARGET:
			case TARGET_SUMMON:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_ENEMY_SUMMON:
			case TARGET_ENEMY_SERVITOR:
			case TARGET_SELF:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_SIEGE:
				return true;
			default:
				return false;
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

	/**
	 * Игнорирование резистов
	 */
	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}

	/**
	 * Игнорирование неуязвимости
	 */
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
			// магический урон
			double mAtk = attacker.getMAtk(target, this);
			double mdef = target.getMDef(null, this);
			double power = getPower();
			double shotPower = (100 + (isSSPossible() ? attacker.getChargedSpiritshotPower() : 0)) / 100.;
			return 91 * power * Math.sqrt(shotPower * mAtk) / mdef;
		}
		// физический урон
		double pAtk = attacker.getPAtk(target);
		double pdef = target.getPDef(attacker);
		double power = getPower();
		double shotPower = (100 + (isSSPossible() ? attacker.getChargedSoulshotPower() : 0)) / 100.;
		return shotPower * (pAtk + power) * 70. / pdef;
	}

	public String getIcon()
	{
		return _icon;
	}

	public int getEnergyConsume()
	{
		return _energyConsume;
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

	public boolean isCanUseWhileAbnormal()
	{
		return _canUseWhileAbnormal;
	}

	public int getToggleGroupId()
	{
		return _toggleGroupId;
	}

	/**
	 * Используется TOGGLE-скиллами. Отключает возможность отключения тугла.
	**/
	public boolean isNecessaryToggle()
	{
		return isToggle() && _isNecessaryToggle;
	}

	public int getAbnormalTime()
	{
		return _abnormalTime;
	}

	public int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public AbnormalTypeList getAbnormalTypeList()
	{
		return abnormalTypeList;
	}

	public AbnormalVisualEffect[] getAbnormalEffects()
	{
		return abnormalVisualEffects;
	}

	public boolean isAbnormalHideTime()
	{
		return _abnormalHideTime || _operateType.isAura();
	}

	public boolean isAbnormalCancelOnAction()
	{
		// hardcode
		return getId() == 442 || getId() == 443 || getId() == 837 || getId() == 922 || getId() == 963
				|| getId() == 1418 || getId() == 1427 || getId() == 1496 || getId() == 1505
				|| getId() == 3158 || getId() == 6093;
	}

	public boolean isIrreplaceableBuff()
	{
		return _irreplaceableBuff;
	}

	public boolean isAbnormalInstant()
	{
		return _abnormalInstant;
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

	// @Rivelia.
	public final boolean isApplyMinRange()
	{
		return _applyMinRange;
	}
	public final int getMasteryLevel()
	{
		return _masteryLevel;
	}
	// .

	public final boolean isSelfDebuff()
	{
		return _isSelfDebuff;
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

	public boolean isRenewal()
	{
		return _renewal;
	}

	public int getBuffSlotType()
	{
		return _buffSlotType;
	}

	public BasicProperty getBasicProperty()
	{
		return _basicProperty;
	}

	public boolean isDouble()
	{
		return _isDouble;
	}

	/**
	 * Return custom minimum skill/effect chance.
	 * @return
	 */
	public double getMinChance()
	{
		return _minChance;
	}

	/**
	 * Return custom maximum skill/effect chance.
	 * @return
	 */
	public double getMaxChance()
	{
		return _maxChance;
	}

	public double getOnAttackCancelChance()
	{
		return _onAttackCancelChance;
	}

	public double getOnCritCancelChance()
	{
		return _onCritCancelChance;
	}

	public void setShowPlayerAbnormal(boolean value)
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		showPlayerAbnormal = value;
	}

	public boolean isShowPlayerAbnormal()
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		return showPlayerAbnormal;
	}

	public void setShowNpcAbnormal(boolean value)
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		showNpcAbnormal = value;
	}

	public boolean isShowNpcAbnormal()
	{
		// Для заглушки отображения кондишона требующий эффект у цели.
		return showNpcAbnormal;
	}

	public boolean isAutoUsable() {
		return autoUsable;
	}

	public Set<AbnormalType> getAbnormalResists()
	{
		return _abnormalResists;
	}

	public boolean isHeroSkill()
	{
		return SkillAcquireHolder.getInstance().containsInTree(this, AcquireType.HERO);
	}

	public boolean isGMSkill()
	{
		return SkillAcquireHolder.getInstance().containsInTree(this, AcquireType.GM);
	}

	/**
	 * Verify if this is a healing potion skill.
	 * @return {@code true} if this is a healing potion skill, {@code false} otherwise
	 */
	public boolean isHealingPotionSkill()
	{
		return getAbnormalTypeList().contains(AbnormalType.HP_RECOVER);
	}

	public SkillBuffType getBuffType()
	{
		// TODO cache
		if (isTrigger()) {
			return SkillBuffType.TRIGGER;
		}
		if (isToggle()) {
			return SkillBuffType.TOGGLE;
		}
		if (isMusic()) {
			return SkillBuffType.DANCE;
		}
		if (isDebuff() || isBad()) {
			return SkillBuffType.DEBUFF;
		}
		if (isHealingPotionSkill()) {
			return SkillBuffType.NONE;
		}
		return SkillBuffType.BUFF;
	}
}