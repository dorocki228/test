package l2s.gameserver;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.Phantoms.objects.ConfigLevelGroup;
import l2s.commons.configuration.PropertiesParser;
import l2s.commons.net.nio.impl.SelectorConfig;
import l2s.commons.reflect.FieldHelper;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.config.*;
import l2s.gameserver.config.xml.ConfigParsers;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.network.authcomm.ServerType;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;
import l2s.gameserver.utils.velocity.VelocityVariable;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config
{
	private static final Logger _log;

	public static final GveConfig GVE = ConfigFactory.create(GveConfig.class);
	public static final LeaderConfig LEADER = ConfigFactory.create(LeaderConfig.class);
	public static final GveTimeDiscountConfig GVE_TIME_DISCOUNT = ConfigFactory.create(GveTimeDiscountConfig.class);
	public static final ActiveAnticheatConfig ACTIVE_ANTICHEAT = ConfigFactory.create(ActiveAnticheatConfig.class);
	public static final PrivateBufferConfig PRIVATE_BUFFER = ConfigFactory.create(PrivateBufferConfig.class);
	public static final MultisellConfig MULTISELL = ConfigFactory.create(MultisellConfig.class);
	public static final GveStagesConfig GVE_STAGES = ConfigFactory.create(GveStagesConfig.class);

	public static final int NCPUS;

	public static final String ANTIFLOOD_CONFIG_FILE = "config/antiflood.properties";
	public static final String CUSTOM_CONFIG_FILE = "config/custom.properties";
	public static final String OTHER_CONFIG_FILE = "config/other.properties";
	public static final String RESIDENCE_CONFIG_FILE = "config/residence.properties";
	public static final String SPOIL_CONFIG_FILE = "config/spoil.properties";
	public static final String ALT_SETTINGS_FILE = "config/altsettings.properties";
	public static final String FORMULAS_CONFIGURATION_FILE = "config/formulas.properties";
	public static final String PVP_CONFIG_FILE = "config/pvp.properties";
	public static final String TELNET_CONFIGURATION_FILE = "config/telnet.properties";
	public static final String CONFIGURATION_FILE = "config/server.properties";
	public static final String AI_CONFIG_FILE = "config/ai.properties";
	public static final String GEODATA_CONFIG_FILE = "config/geodata.properties";
	public static final String SERVICES_FILE = "config/services.properties";
	public static final String DEVELOP_FILE = "config/develop.properties";
	public static final String EXT_FILE = "config/ext.properties";
	public static final String BBS_FILE = "config/bbs.properties";
	public static final String FAKE_PLAYERS_LIST = "config/fake_players.list";
	public static final String PVP_MANAGER_FILE = "config/pvp_manager.properties";
	public static final String BOT_FILE = "config/anti_bot_system.properties";
	public static final String BUFFER_FILE = "config/buffer.properties";
	public static final String ANUSEWORDS_CONFIG_FILE = "config/abusewords.txt";
	public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
	public static final String GM_ACCESS_FILES_DIR = "config/GMAccess.d/";
	public static final String ARTIFACT_FILE = "config/artifact.properties";
	public static final String BOOST_MORALE_FILE = "config/boost_morale.properties";
	public static final String FACTION_WAR_FILE = "config/faction_war.properties";
	public static final String FACTION_LEADER_FILE = "config/leader.properties";
	public static final String SUBSKILLS_FILE = "config/subskills.properties";

	public static double SERVITOR_PVP_DAMAGE_MODIFIER;
	public static double SERVITOR_OLYMPIAD_DAMAGE_MODIFIER;

	public static boolean ALT_DEBUG_ENABLED;
	public static boolean ALT_DEBUG_GM_ONLY;
	public static int CUSTOM_NEXT_TARGET_RADIUS;

	public static boolean ARTIFACT_ENABLED = false;

	public static boolean ENABLE_ANTI_BOT_SYSTEM;
	public static int MINIMUM_TIME_QUESTION_ASK;
	public static int MAXIMUM_TIME_QUESTION_ASK;
	public static int MINIMUM_BOT_POINTS_TO_STOP_ASKING;
	public static int MAXIMUM_BOT_POINTS_TO_STOP_ASKING;
	public static int MAX_BOT_POINTS;
	public static int MINIMAL_BOT_RATING_TO_BAN;
	public static int AUTO_BOT_BAN_JAIL_TIME;
	public static boolean ANNOUNCE_AUTO_BOT_BAN;
	public static boolean ON_WRONG_QUESTION_KICK;

	public static boolean HTM_SHAPE_ARABIC;
	public static int SHUTDOWN_ANN_TYPE;

	public static boolean AUTOSAVE;
	public static long USER_INFO_INTERVAL;
	public static boolean BROADCAST_STATS_INTERVAL;
	public static long BROADCAST_CHAR_INFO_INTERVAL;
	public static int MIN_HIT_TIME;
	public static int SUB_START_LEVEL;
	public static int START_CLAN_LEVEL;
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean ENABLE_L2_TOP_OVERONLINE;
	public static int L2TOP_MAX_ONLINE;
	public static int MIN_ONLINE_0_5_AM;
	public static int MAX_ONLINE_0_5_AM;
	public static int MIN_ONLINE_6_11_AM;
	public static int MAX_ONLINE_6_11_AM;
	public static int MIN_ONLINE_12_6_PM;
	public static int MAX_ONLINE_12_6_PM;
	public static int MIN_ONLINE_7_11_PM;
	public static int MAX_ONLINE_7_11_PM;
	public static int ADD_ONLINE_ON_SIMPLE_DAY;
	public static int ADD_ONLINE_ON_WEEKEND;
	public static int L2TOP_MIN_TRADERS;
	public static int L2TOP_MAX_TRADERS;

	public static double ONLINE_MULTIPLIER;

	public static boolean REFLECT_DAMAGE_CAPPED_BY_PDEF;
	public static double MIN_SAVEVS_REDUCTION;
	public static double MAX_SAVEVS_REDUCTION;
	public static int EFFECT_TASK_MANAGER_COUNT;
	public static int SKILLS_CAST_TIME_MIN_PHYSICAL;
	public static int SKILLS_CAST_TIME_MIN_MAGICAL;
	public static boolean ENABLE_CRIT_HEIGHT_BONUS;
	public static int MAXIMUM_ONLINE_USERS;
	public static int CLAN_WAR_MINIMUM_CLAN_LEVEL;
	public static int CLAN_WAR_MINIMUM_PLAYERS_DECLARE;
	public static int CLAN_WAR_PREPARATION_DAYS_PERIOD;
	public static int CLAN_WAR_REPUTATION_SCORE_PER_KILL;
	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;
	public static int MAX_REFLECTIONS_COUNT;
	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;

	public static Duration ACCEPT_TIME;
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean ABUSEWORD_BANCHAT;
	public static int[] BAN_CHANNEL_LIST;
	public static boolean ABUSEWORD_REPLACE;
	public static String ABUSEWORD_REPLACE_STRING;
	public static int ABUSEWORD_BANTIME;
	public static Pattern ABUSEWORD_PATTERN;
	public static boolean BANCHAT_ANNOUNCE;
	public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean BANCHAT_ANNOUNCE_NICK;
	public static boolean ALLOW_REFFERAL_SYSTEM;
	public static int REF_SAVE_INTERVAL;
	public static int MAX_REFFERALS_PER_CHAR;
	public static int MIN_ONLINE_TIME;
	public static int MIN_REFF_LEVEL;
	public static double REF_PERCENT_GIVE;
	public static boolean PREMIUM_ACCOUNT_ENABLED;
	public static boolean PREMIUM_ACCOUNT_BASED_ON_GAMESERVER;
	public static int FREE_PA_TYPE;
	public static int FREE_PA_DELAY;
	public static boolean ENABLE_FREE_PA_NOTIFICATION;
	public static int CATALYST_POWER_W_D;
	public static int CATALYST_POWER_W_C;
	public static int CATALYST_POWER_W_B;
	public static int CATALYST_POWER_W_A;
	public static int CATALYST_POWER_W_S;
	public static int CATALYST_POWER_A_D;
	public static int CATALYST_POWER_A_C;
	public static int CATALYST_POWER_A_B;
	public static int CATALYST_POWER_A_A;
	public static int CATALYST_POWER_A_S;
	public static boolean ALT_SELL_ITEM_ONE_ADENA;
	public static int MAX_SIEGE_CLANS;
	public static List<Integer> ITEM_LIST;
	public static TIntSet DROP_ONLY_THIS;
	public static boolean INCLUDE_RAID_DROP;
	public static boolean SAVING_SPS;
	public static boolean MANAHEAL_SPS_BONUS;
	public static int ALT_ADD_RECIPES;
	public static int ALT_MAX_ALLY_SIZE;
	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static double[] ALT_PARTY_BONUS;
	public static double[] ALT_PARTY_CLAN_BONUS;
	public static int[] ALT_PARTY_LVL_DIFF_PENALTY;
	public static boolean ALT_ALL_PHYS_SKILLS_OVERHIT;
	public static double ALT_POLE_DAMAGE_MODIFIER;
	public static double ALT_M_SIMPLE_DAMAGE_MOD;
	public static double ALT_P_DAMAGE_MOD;
	
	public static double ALT_P_DAMAGE_MOD_ARCHER;
	public static double ALT_P_CRIT_DAMAGE_MOD_ARCHER;
	
	public static double ALT_M_CRIT_DAMAGE_MOD;
	public static double ALT_P_CRIT_DAMAGE_MOD;
	public static double ALT_P_CRIT_CHANCE_MOD;
	public static double ALT_M_CRIT_CHANCE_MOD;
	public static double ALT_BLOW_DAMAGE_MOD;
	public static double ALT_BLOW_CRIT_RATE_MODIFIER;
	public static double ALT_VAMPIRIC_CHANCE;
	public static double ALT_VAMPIRIC_MP_CHANCE;
	public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
	public static double ALT_VITALITY_RATE;
	public static double ALT_VITALITY_PA_RATE;
	public static double ALT_VITALITY_CONSUME_RATE;
	public static int ALT_VITALITY_POTIONS_LIMIT;
	public static int ALT_VITALITY_POTIONS_PA_LIMIT;
	public static Calendar CASTLE_VALIDATION_DATE;
	public static int LIGHT_CASTLE_SELL_TAX_PERCENT;
	public static int DARK_CASTLE_SELL_TAX_PERCENT;
	public static int LIGHT_CASTLE_BUY_TAX_PERCENT;
	public static int DARK_CASTLE_BUY_TAX_PERCENT;
	public static boolean ALT_PCBANG_POINTS_ENABLED;
	public static boolean PC_BANG_POINTS_BY_ACCOUNT;
	public static boolean ALT_PCBANG_POINTS_ONLY_PREMIUM;
	public static double ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int ALT_PCBANG_POINTS_BONUS;
	public static int ALT_PCBANG_POINTS_DELAY;
	public static int ALT_PCBANG_POINTS_MIN_LVL;
	public static TIntSet ALT_ALLOWED_MULTISELLS_IN_PCBANG;
	public static boolean ALT_DEBUG_PVP_ENABLED;
	public static boolean ALT_DEBUG_PVP_DUEL_ONLY;
	public static boolean ALT_DEBUG_PVE_ENABLED;
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;
	public static int FULL_RANGE_PACKET_LIMIT;
	public static int SHORT_RANGE_PACKET_LIMIT;
	public static SelectorConfig SELECTOR_CONFIG;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_ONLY_ADENA;
	public static boolean AUTO_LOOT_INDIVIDUAL;
	public static boolean AUTO_LOOT_FROM_RAIDS;
	public static TIntSet AUTO_LOOT_ITEM_ID_LIST;
	public static boolean AUTO_LOOT_PK;
	public static String CNAME_TEMPLATE;
	public static int CNAME_MAXLEN;
	public static String CLAN_NAME_TEMPLATE;
	public static String APASSWD_TEMPLATE;
	public static String CLAN_TITLE_TEMPLATE;
	public static String ALLY_NAME_TEMPLATE;
	public static boolean GLOBAL_SHOUT;
	public static int CHAT_RANGE;
	public static int SHOUT_SQUARE_OFFSET;
	public static boolean ALLOW_FRACTION_WORLD_CHAT;
	public static int WORLD_CHAT_POINTS_PER_DAY;
	public static int WORLD_CHAT_POINTS_PER_DAY_PA;
	public static int WORLD_CHAT_USE_MIN_LEVEL;
	public static int WORLD_CHAT_USE_MIN_LEVEL_PA;
	public static boolean BAN_FOR_CFG_USAGE;
	public static boolean ALLOW_TOTAL_ONLINE;
	public static int FIRST_UPDATE;
	public static int DELAY_UPDATE;
	public static int EXCELLENT_SHIELD_BLOCK_CHANCE;
	public static int EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE;
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static double ALT_RAID_RESPAWN_MULTIPLIER;
	public static boolean ALT_GAME_UNREGISTER_RECIPE;
	public static int SS_ANNOUNCE_PERIOD;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALLOW_NPC_SHIFTCLICK;
	public static boolean ALLOW_VOICED_COMMANDS;

	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;
	public static int[] ALT_ALLOWED_PET_POTIONS;
	public static double SKILLS_CHANCE_MOD;
	public static double SKILLS_CHANCE_MIN;
	public static double SKILLS_CHANCE_POW;
	public static double SKILLS_CHANCE_CAP;
	public static boolean ALT_SAVE_UNSAVEABLE;
	public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean ALT_DELETE_SA_BUFFS;
	public static int SKILLS_CAST_TIME_MIN;
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static boolean ALT_SOCIAL_ACTION_REUSE;
	public static boolean ALT_DISABLE_SPELLBOOKS;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALLOW_DELEVEL_COMMAND;
	public static boolean ALT_ARENA_EXP;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_ERTHEIA_DUALCLASS_WITHOUT_QUESTS;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static boolean ALT_ALLOW_AWAKE_ON_SUB_CLASS;
	public static boolean ALT_NO_LASTHIT;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;
	public static int ALT_BUFF_LIMIT;
	public static int MULTISELL_SIZE;
	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;
	public static boolean SERVICES_CHANGE_PASSWORD;
	public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CHANGE_CLAN_NAME_ITEM;
	public static boolean SERVICES_CLAN_REPUTATION_ENABLE;
	public static int SERVICES_CLAN_REPUTATION_ITEM_ID;
	public static int SERVICES_CLAN_REPUTATION_ITEM_COUNT;
	public static int SERVICES_CLAN_REPUTATION_AMOUNT;
	public static boolean SERVICES_CLANSKILL_SELL_ENABLED;
	public static int SERVICES_CLAN_SKILL_SELL_ITEM;
	public static int SERVICES_CLAN_SKILL_SELL_PRICE;
	public static int SERVICES_CLANSKIL_SELL_MIN_LEVEL;
	public static boolean SERVICES_CLANLEVEL_SELL_ENABLED;
	public static long[] SERVICES_CLANLEVEL_SELL_PRICE;
	public static int[] SERVICES_CLANLEVEL_SELL_ITEM;
	public static int SERVICES_CLAN_MAX_SELL_LEVEL;
	public static boolean SERVICES_DELEVEL_SELL_ENABLED;
	public static int SERVICES_DELEVEL_SELL_PRICE;
	public static int SERVICES_DELEVEL_SELL_ITEM;
	public static boolean SERVICES_KARMA_CLEAN_ENABLED;
	public static int SERVICES_KARMA_CLEAN_SELL_ITEM;
	public static long SERVICES_KARMA_CLEAN_SELL_PRICE;
	public static boolean SERVICES_PK_CLEAN_ENABLED;
	public static int SERVICES_PK_CLEAN_SELL_ITEM;
	public static long SERVICES_PK_CLEAN_SELL_PRICE;
	public static String CUSTOM_CNAME_TEMPLATE;

	@VelocityVariable
	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;
	@VelocityVariable
	public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
	public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
	public static int SERVICES_EXCHANGE_BABY_PET_ITEM;
	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;
	public static boolean SERVICES_CHANGE_BASE_ENABLED;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;
	public static boolean SERVICES_SEPARATE_SUB_ENABLED;
	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;
	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;
	public static boolean SERVICES_CHANGE_TITLE_COLOR_ENABLED;
	public static int SERVICES_CHANGE_TITLE_COLOR_PRICE;
	public static int SERVICES_CHANGE_TITLE_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_TITLE_COLOR_LIST;
	@VelocityVariable
	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;
	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;
	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static int SERVICES_EXPAND_INVENTORY_MAX;
	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;
	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static int SERVICES_OFFLINE_TRADE_ALLOW_ZONE;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static AbnormalEffect[] SERVICES_OFFLINE_TRADE_ABNORMAL_EFFECT;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static int SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	@VelocityVariable
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	@VelocityVariable
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static boolean SERVICES_PARNASSUS_NOTAX;
	public static long SERVICES_PARNASSUS_PRICE;
	@VelocityVariable
	public static boolean SERVICES_RIDE_HIRE_ENABLED;
	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_ALT_LOTTERY_PRICE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static double SERVICES_LOTTERY_5_NUMBER_RATE;
	public static double SERVICES_LOTTERY_4_NUMBER_RATE;
	public static double SERVICES_LOTTERY_3_NUMBER_RATE;
	public static int SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;
	@VelocityVariable
	public static boolean SERVICES_ALLOW_ROULETTE;
	public static long SERVICES_ROULETTE_MIN_BET;
	public static long SERVICES_ROULETTE_MAX_BET;
	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALLOW_IP_LOCK;
	public static boolean ALLOW_HWID_LOCK;
	public static int HWID_LOCK_MASK;
	public static long NONOWNER_ITEM_PICKUP_DELAY;

	public static Map<Integer, PlayerAccess> gmlist;
	public static double[] RATE_XP_BY_LVL;
	public static double[] RATE_SP_BY_LVL;
	public static int MAX_DROP_ITEMS_FROM_ONE_GROUP;
	public static double[] RATE_DROP_ADENA_BY_LVL;
	public static double[] RATE_DROP_ITEMS_BY_LVL;
	public static double DROP_CHANCE_MODIFIER;
	public static double[] RATE_DROP_SPOIL_BY_LVL;
	public static double SPOIL_CHANCE_MODIFIER;
	public static double RATE_QUESTS_REWARD;
	public static boolean RATE_QUEST_REWARD_EXP_SP_ADENA_ONLY;
	public static double QUESTS_REWARD_LIMIT_MODIFIER;
	public static boolean EX_USE_QUEST_REWARD_PENALTY_PER;
	public static int EX_F2P_QUEST_REWARD_PENALTY_PER;
	public static TIntSet EX_F2P_QUEST_REWARD_PENALTY_QUESTS;
	public static double RATE_QUESTS_DROP;
	public static double RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static double RATE_DROP_COMMON_ITEMS;
	public static double RATE_XP_RAIDBOSS_MODIFIER;
	public static double RATE_DROP_RAIDBOSS;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_EQUIPMENT;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;
	public static double RATE_DROP_SIEGE_GUARD;
	public static double RATE_MANOR;
	public static int RATE_FISH_DROP_COUNT;
	public static double RATE_FISH_EXP;
	public static boolean RATE_PARTY_MIN;
	public static int RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_NEEDED_TO_DROP;
	public static int RATE_KARMA_LOST_STATIC;
	public static int KARMA_DROP_ITEM_LIMIT;
	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;
	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;
	public static int CHARACTER_DELETE_AFTER_HOURS;
	public static int PURGE_BYPASS_TASK_FREQUENCY;

	public static File DATAPACK_ROOT;
	public static File GEODATA_ROOT;
	public static Path DATAPACK_ROOT_PATH;
	public static Path SCRIPTS_PATH;

	public static double BUFFTIME_MODIFIER;
	public static int[] BUFFTIME_MODIFIER_SKILLS;
	public static double CLANHALL_BUFFTIME_MODIFIER;
	public static double SONGDANCETIME_MODIFIER;
	public static double MAXLOAD_MODIFIER;
	public static double GATEKEEPER_MODIFIER;
	@VelocityVariable
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;
	public static double ALT_CHAMPION_CHANCE1;
	public static double ALT_CHAMPION_CHANCE2;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
	public static int ALT_CHAMPION_MIN_LEVEL;
	public static int ALT_CHAMPION_TOP_LEVEL;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;
	public static boolean ALLOW_ITEMS_REFUND;
	public static int SWIMING_SPEED;
	public static TIntSet AVAILABLE_PROTOCOL_REVISIONS;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static boolean USE_CLIENT_LANG;
	public static Language DEFAULT_LANG;
	public static String RESTART_AT_TIME;
	public static boolean RETAIL_MULTISELL_ENCHANT_TRANSFER;
	public static int REQUEST_ID;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int PORT_GAME;
	public static boolean EX_SECOND_AUTH_ENABLED;
	public static int EX_SECOND_AUTH_MAX_ATTEMPTS;
	public static int EX_SECOND_AUTH_BAN_TIME;
	public static boolean EX_USE_PREMIUM_HENNA_SLOT;
	public static boolean EX_USE_AUTO_SOUL_SHOT;
	public static boolean EX_USE_TO_DO_LIST;
	public static boolean ALT_EASY_RECIPES;
	public static boolean ALT_USE_TRANSFORM_IN_EPIC_ZONE;
	public static boolean ALT_ANNONCE_RAID_BOSSES_REVIVAL;
	public static boolean SPAWN_VITAMIN_MANAGER;
	public static TIntObjectMap<int[]> ALLOW_CLASS_MASTERS_LIST;
	public static boolean ALLOW_EVENT_GATEKEEPER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int QUEST_INVENTORY_MAXIMUM;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static double BASE_SPOIL_RATE;
	public static double MINIMUM_SPOIL_RATE;
	public static boolean SHOW_HTML_WELCOME;
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static int MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static int MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;
	public static int KARMA_MIN_KARMA_PC;
	public static int KARMA_MIN_KARMA_PET;
	public static int KARMA_RATE_KARMA_LOST;
	public static int KARMA_LOST_BASE;
	public static int KARMA_PENALTY_START_KARMA;
	public static int KARMA_PENALTY_DURATION_DEFAULT;
	public static double KARMA_PENALTY_DURATION_INCREASE;
	public static int KARMA_DOWN_TIME_MULTIPLE;
	public static int KARMA_CRIMINAL_DURATION_MULTIPLE;
	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS;
	public static List<RaidGlobalDrop> RAID_GLOBAL_DROP;

	public static int PVP_TIME;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;

	public static boolean REGEN_SIT_WAIT;
	public static double RATE_RAID_REGEN;
	public static double RATE_RAID_DEFENSE;
	public static double RATE_RAID_ATTACK;
	public static double RATE_EPIC_DEFENSE;
	public static double RATE_EPIC_ATTACK;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;
	public static int STARTING_LVL;
	public static long STARTING_SP;
	public static String[] STARTING_ITEM;
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static int DEEPBLUE_DROP_PERCENT_EACH_LVL;
	public static boolean UNSTUCK_SKILL;
	public static boolean IS_TELNET_ENABLED;
	public static String TELNET_DEFAULT_ENCODING;
	public static String TELNET_PASSWORD;
	public static String TELNET_HOSTNAME;
	public static int TELNET_PORT;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;
	public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;
	public static double SENDSTATUS_TRADE_MOD;
	public static boolean ALLOW_CH_DOOR_OPEN_ON_CLICK;
	public static boolean ALT_CH_SIMPLE_DIALOG;
	public static boolean ALT_CH_UNLIM_MP;
	public static boolean ALT_NO_FAME_FOR_DEAD;
	public static int CH_BID_GRADE1_MINCLANLEVEL;
	public static int CH_BID_GRADE1_MINCLANMEMBERS;
	public static int CH_BID_GRADE1_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE2_MINCLANLEVEL;
	public static int CH_BID_GRADE2_MINCLANMEMBERS;
	public static int CH_BID_GRADE2_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE3_MINCLANLEVEL;
	public static int CH_BID_GRADE3_MINCLANMEMBERS;
	public static int CH_BID_GRADE3_MINCLANMEMBERSLEVEL;
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	public static double RESIDENCE_LEASE_MULTIPLIER;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static int GM_NAME_COLOUR;
	public static boolean GM_HERO_AURA;
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;
	public static int AI_TASK_MANAGER_COUNT;
	public static long AI_TASK_ATTACK_DELAY;
	public static long AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;
	public static int AGGRO_CHECK_INTERVAL;
	public static long NONAGGRO_TIME_ONTELEPORT;
	public static long NONPVP_TIME_ONTELEPORT;
	public static int MAX_DRIFT_RANGE;
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;
	public static boolean ALLOW_DEATH_PENALTY;
	public static int ALT_DEATH_PENALTY_CHANCE;
	public static int ALT_DEATH_PENALTY_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_KARMA_PENALTY;
	public static boolean HIDE_GM_STATUS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS;
	public static boolean AUTO_LEARN_SKILLS;
	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;
	public static boolean DAMAGE_FROM_FALLING;
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static int FOLLOW_RANGE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALT_CUSTOM_ITEM_AUCTION_ENABLED;
	public static boolean ALT_ITEM_AUCTION_CAN_REBID;
	public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
	public static long ALT_ITEM_AUCTION_MAX_BID;
	public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;
	public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
	public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
	public static boolean ALT_HBCE_FAIR_PLAY;
	public static int ALT_PET_INVENTORY_LIMIT;
	public static int LIM_PATK;
	public static int LIM_MATK;
	public static int LIM_PDEF;
	public static int LIM_MDEF;
	public static int LIM_MATK_SPD;
	public static int LIM_PATK_SPD;
	public static int LIM_CRIT_DAM;
	public static int LIM_CRIT;
	public static int LIM_MCRIT;
	public static int LIM_ACCURACY;
	public static int LIM_EVASION;
	public static int LIM_MOVE;
	public static int LIM_FAME;
	public static int LIM_RAID_POINTS;
	public static int HP_LIMIT;
	public static int MP_LIMIT;
	public static int CP_LIMIT;
	public static double ALT_NPC_PATK_MODIFIER;
	public static double ALT_NPC_MATK_MODIFIER;
	public static double ALT_NPC_MAXHP_MODIFIER;
	public static double ALT_NPC_MAXMP_MODIFIER;
	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static double FESTIVAL_RATE_PRICE;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static boolean ALLOW_TALK_WHILE_SITTING;
	public static int MAXIMUM_MEMBERS_IN_PARTY;
	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
	public static boolean ALLOW_CLANSKILLS;
	public static boolean ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;
	public static boolean ALLOW_MANOR;
	public static int MANOR_REFRESH_TIME;
	public static int MANOR_REFRESH_MIN;
	public static int MANOR_APPROVE_TIME;
	public static int MANOR_APPROVE_MIN;
	public static int MANOR_MAINTENANCE_PERIOD;
	public static int ENCHANT_CHANCE_MASTER_YOGI_STAFF;
	public static int ENCHANT_MAX_MASTER_YOGI_STAFF;
	public static int SAFE_ENCHANT_MASTER_YOGI_STAFF;
	public static long REFLECT_MIN_RANGE;
	public static double REFLECT_AND_BLOCK_DAMAGE_CHANCE_CAP;
	public static double REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE_CAP;
	public static double REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE_CAP;
	public static double REFLECT_DAMAGE_PERCENT_CAP;
	public static double REFLECT_BOW_DAMAGE_PERCENT_CAP;
	public static double REFLECT_PSKILL_DAMAGE_PERCENT_CAP;
	public static double REFLECT_MSKILL_DAMAGE_PERCENT_CAP;
	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static double SERVICES_TRADE_TAX;
	public static double SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_MIN_LEVEL;
	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
	public static int SERVICES_NO_CARRIER_MAX_TIME;
	public static int SERVICES_NO_CARRIER_MIN_TIME;
	public static boolean ALT_SHOW_SERVER_TIME;
	public static int GEO_X_FIRST;
	public static int GEO_Y_FIRST;
	public static int GEO_X_LAST;
	public static int GEO_Y_LAST;
	public static boolean ALLOW_GEODATA;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int CLIENT_Z_SHIFT;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static int REGION_EDGE_MAX_Z_DIFF;
	public static int PATHFIND_BOOST;
	public static int PATHFIND_MAP_MUL;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;
	public static boolean DEBUG;
	public static int WEAR_DELAY;
	public static boolean ALLOW_FAKE_PLAYERS;
	public static int FAKE_PLAYERS_PERCENT;
	public static boolean DISABLE_CRYSTALIZATION_ITEMS;
	public static int[] SERVICES_ENCHANT_VALUE;
	public static int[] SERVICES_ENCHANT_COAST;
	public static int[] SERVICES_ENCHANT_RAID_VALUE;
	public static int[] SERVICES_ENCHANT_RAID_COAST;
	public static boolean GOODS_INVENTORY_ENABLED;
	public static boolean EX_NEW_PETITION_SYSTEM;
	public static boolean EX_JAPAN_MINIGAME;
	public static boolean EX_LECTURE_MARK;
	public static boolean AUTH_SERVER_GM_ONLY;
	public static boolean AUTH_SERVER_BRACKETS;
	public static boolean AUTH_SERVER_IS_PVP;
	public static int AUTH_SERVER_AGE_LIMIT;
	public static int AUTH_SERVER_SERVER_TYPE;
	public static boolean ONLINE_GENERATOR_ENABLED;
	public static int ONLINE_GENERATOR_DELAY;
	public static boolean ALLOW_MONSTER_RACE;
	public static boolean ONLY_ONE_SIEGE_PER_CLAN;
	public static double SPECIAL_CLASS_BOW_CROSS_BOW_PENALTY;
	public static boolean ALLOW_USE_DOORMANS_IN_SIEGE_BY_OWNERS;
	public static boolean DISABLE_VAMPIRIC_VS_MOB_ON_PVP;
	public static boolean NPC_RANDOM_ENCHANT;
	public static boolean ENABLE_PARTY_SEARCH;
	public static boolean MENTOR_ONLY_PA;
	public static boolean ALLOW_PVP_REWARD;
	public static boolean PVP_REWARD_SEND_SUCC_NOTIF;
	public static int[] PVP_REWARD_REWARD_IDS;
	public static long[] PVP_REWARD_COUNTS;
	public static boolean PVP_REWARD_RANDOM_ONE;
	public static int PVP_REWARD_DELAY_ONE_KILL;
	public static int PVP_REWARD_MIN_PL_PROFF;
	public static int PVP_REWARD_MIN_PL_UPTIME_MINUTE;
	public static int PVP_REWARD_MIN_PL_LEVEL;
	public static boolean PVP_REWARD_PK_GIVE;
	public static boolean PVP_REWARD_ON_EVENT_GIVE;
	public static boolean PVP_REWARD_ONLY_BATTLE_ZONE;
	public static boolean PVP_REWARD_ONLY_NOBLE_GIVE;
	public static boolean PVP_REWARD_SAME_PARTY_GIVE;
	public static boolean PVP_REWARD_SAME_CLAN_GIVE;
	public static boolean PVP_REWARD_SAME_ALLY_GIVE;
	public static boolean PVP_REWARD_SAME_HWID_GIVE;
	public static boolean PVP_REWARD_SAME_IP_GIVE;
	public static boolean PVP_REWARD_SPECIAL_ANTI_TWINK_TIMER;
	public static int PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM;
	public static boolean PVP_REWARD_CHECK_EQUIP;
	public static int PVP_REWARD_WEAPON_GRADE_TO_CHECK;
	public static boolean DISALLOW_MSG_TO_PL;
	public static int ALL_CHAT_USE_MIN_LEVEL;
	public static int ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int ALL_CHAT_USE_DELAY;
	public static int SHOUT_CHAT_USE_MIN_LEVEL;
	public static int SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int SHOUT_CHAT_USE_DELAY;
	public static int FRACTION_SHOUT_CHAT_USE_MIN_LEVEL;
	public static int FRACTION_SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int FRACTION_SHOUT_CHAT_USE_DELAY;
	public static int HERO_CHAT_USE_MIN_LEVEL;
	public static int HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int HERO_CHAT_USE_DELAY;
	public static int PRIVATE_CHAT_USE_MIN_LEVEL;
	public static int PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int PRIVATE_CHAT_USE_DELAY;
	public static int MAIL_USE_MIN_LEVEL;
	public static int MAIL_USE_MIN_LEVEL_WITHOUT_PA;
	public static int MAIL_USE_DELAY;
	public static int IM_PAYMENT_ITEM_ID;
	public static int IM_MAX_ITEMS_IN_RECENT_LIST;
	public static boolean ALT_SHOW_MONSTERS_LVL;
	public static boolean ALT_SHOW_MONSTERS_AGRESSION;
	public static int BEAUTY_SHOP_COIN_ITEM_ID;
	public static boolean ALT_TELEPORT_TO_TOWN_DURING_SIEGE;
	public static int ALT_CLAN_LEAVE_PENALTY_TIME;
	public static int ALT_CLAN_CREATE_PENALTY_TIME;
	public static int ALT_EXPELLED_MEMBER_PENALTY_TIME;
	public static int ALT_LEAVED_ALLY_PENALTY_TIME;
	public static int ALT_DISSOLVED_ALLY_PENALTY_TIME;
	public static boolean RAID_DROP_GLOBAL_ITEMS;
	public static int MIN_RAID_LEVEL_TO_DROP;
	public static int NPC_DIALOG_PLAYER_DELAY;
	public static double PHYSICAL_MIN_CHANCE_TO_HIT;
	public static double PHYSICAL_MAX_CHANCE_TO_HIT;
	public static double MAGIC_MIN_CHANCE_TO_HIT_MISS;
	public static double MAGIC_MAX_CHANCE_TO_HIT_MISS;
	public static boolean ENABLE_CRIT_DMG_REDUCTION_ON_MAGIC;
	public static double MAX_BLOW_RATE_ON_BEHIND;
	public static double MAX_BLOW_RATE_ON_FRONT_AND_SIDE;
	public static double BLOW_SKILL_CHANCE_MOD_ON_BEHIND;
	public static double BLOW_SKILL_CHANCE_MOD_ON_FRONT;
	public static boolean ENABLE_MATK_SKILL_LANDING_MOD;
	public static boolean ENABLE_WIT_SKILL_LANDING_MOD;
	public static double BLOW_SKILL_DEX_CHANCE_MOD;
	public static double NORMAL_SKILL_DEX_CHANCE_MOD;
	public static double CRIT_STUN_BREAK_CHANCE_ON_MAGICAL_SKILL;
	public static double NORMAL_STUN_BREAK_CHANCE_ON_MAGICAL_SKILL;
	public static double CRIT_STUN_BREAK_CHANCE_ON_PHYSICAL_SKILL;
	public static double NORMAL_STUN_BREAK_CHANCE_ON_PHYSICAL_SKILL;
	public static double CRIT_STUN_BREAK_CHANCE_ON_REGULAR_HIT;
	public static double NORMAL_STUN_BREAK_CHANCE_ON_REGULAR_HIT;
	public static String CLAN_DELETE_TIME;
	public static String CLAN_CHANGE_LEADER_TIME;
	public static int CLAN_CHANGE_LEADER_TIME_SECOND;
	public static int CLAN_MAX_LEVEL;
	public static int[] CLAN_LVL_UP_SP_COST;
	public static int[] CLAN_LVL_UP_RP_COST;
	public static int[] CLAN_LVL_UP_MIN_MEMBERS;
	public static long[][][][] CLAN_LVL_UP_ITEMS_REQUIRED;
	public static boolean[] CLAN_LVL_UP_NEED_CASTLE;
	public static int ALT_MUSIC_LIMIT;
	public static int ALT_DEBUFF_LIMIT;
	public static int ALT_TRIGGER_LIMIT;
	public static int SPECIAL_ITEM_ID;
	public static long SPECIAL_ITEM_COUNT;
	public static double SPECIAL_ITEM_DROP_CHANCE;
	public static int ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL;
	public static boolean ALT_PETS_NOT_STARVING;
	public static Set<Language> AVAILABLE_LANGUAGES;
	public static int MAX_ACTIVE_ACCOUNTS_ON_ONE_IP;
	public static String[] MAX_ACTIVE_ACCOUNTS_IGNORED_IP;
	public static int MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID;
	public static int CHECK_HWID_MASK;
	public static int CHECK_HWID_MASK_MATCHES;
	public static double[] MONSTER_LEVEL_DIFF_EXP_PENALTY;
	public static boolean SHOW_TARGET_EFFECTS;
	public static long SP_LIMIT;
	public static int ELEMENT_ATTACK_LIMIT;
	public static boolean ALLOW_AWAY_STATUS;
	public static boolean AWAY_ONLY_FOR_PREMIUM;
	public static int AWAY_TIMER;
	public static int BACK_TIMER;
	public static int AWAY_TITLE_COLOR;
	public static boolean AWAY_PLAYER_TAKE_AGGRO;
	public static boolean AWAY_PEACE_ZONE;
	public static double[] PERCENT_LOST_ON_DEATH;
	public static double PERCENT_LOST_ON_DEATH_MOD_IN_PEACE_ZONE;
	public static double PERCENT_LOST_ON_DEATH_MOD_IN_PVP;
	public static double PERCENT_LOST_ON_DEATH_MOD_IN_WAR;
	public static double PERCENT_LOST_ON_DEATH_MOD_FOR_PK;
	public static boolean BOTREPORT_ENABLED;
	public static int BOTREPORT_REPORT_DELAY;
	public static String BOTREPORT_REPORTS_RESET_TIME;
	public static boolean BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS;
	public static boolean VIP_ATTENDANCE_REWARDS_ENABLED;
	public static boolean VIP_ATTENDANCE_REWARDS_REWARD_BY_ACCOUNT;

	public static boolean SPAM_FILTER_ENABLED;
	public static boolean SPAM_FILTER_BLOC_SPAM;
	public static boolean SPAM_FILTER_DUMMY_SPAM;
	public static boolean SPAM_FILTER_NOTIFY_GM;
	public static boolean SPAM_FILTER_LOGGING;

	public static int SPAM_FILTER_MESSAGES_TO_SPAM;
	public static int SPAM_FILTER_PENALTIES_TO_SPAM;

    public static boolean ACP_ENABLED;
    public static boolean ACP_ONLY_PREMIUM;
    public static int[] ACP_POTIONS_CP;
    public static int[] ACP_POTIONS_HP;
    public static int[] ACP_POTIONS_MP;
    public static int[] ACP_CERTIFICATES_FOR_USE;
    public static int[] ACP_RESTRICT_WITH_EFFECTS;

	public static boolean ENABLE_OBT_COMMAND;

	public static boolean RESTRICTED_CHAR_CLAN_NAME_ENABLE;
	public static boolean WHO_AM_GM_ONLY;
    public static Pattern RESTRICTED_CHAR_CLAN_NAME;
	public static int MAX_ARTIFACTS_FOR_FACTION;
	public static boolean GVE_FARM_ENABLED;
	public static int GVE_UPGRADING_EVENT_CAPTURE_REWARD_RADIUS;
	public static String[] GVE_PARTY_CLASS_LIMITS_GROUPS;
	public static boolean GVE_CASINO_ENABLED;
	public static int GVE_CASINO_NPC_ID;
	public static int GVE_CASINO_MIN_BED;
	public static int GVE_CASINO_MAX_BED;
	public static int GVE_CASINO_TAX_PERCENT;
	public static int GVE_CASINO_MIN_ADENA_LEFT;

	public static int[] GVE_CLUB_CARD_ITEMS;
	public static int RAID_BOSS_BBS_TELEPORT_PRICE;

	public static boolean PRE_REGISTRATION_STAGE;
	public static List<String> PRE_REGISTRATION_STAGE_WHITE_LIST_ACCOUNTS;

	public static boolean CHANGE_BASE_CLASS_ENABLE;
	public static int[] CHANGE_BASE_CLASS_FIRST_PRICE;
	public static int[] CHANGE_BASE_CLASS_SECOND_PRICE;

	public static int MOVE_TASK_QUANTUM_PC;
	public static int MOVE_TASK_QUANTUM_NPC;
	public static boolean MOVE_OFFLOAD_MTL_PC;
	public static int FOLLOW_ARRIVE_DISTANCE = 150;
	
	public static void loadServerConfig()
	{
		final PropertiesParser serverSettings = load("config/server.properties");
		
		MOVE_TASK_QUANTUM_PC = serverSettings.getProperty("MoveTaskQuantumForPC", 400);
		MOVE_TASK_QUANTUM_NPC = serverSettings.getProperty("MoveTaskQuantumForNPC", 800);
		MOVE_OFFLOAD_MTL_PC = serverSettings.getProperty("OffloadMTLForPC", false);
		
		AUTH_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
		AUTH_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
		AUTH_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
		AUTH_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
		for(final String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
			if(!a.trim().isEmpty())
			{
				final ServerType t = ServerType.valueOf(a.toUpperCase());
				AUTH_SERVER_SERVER_TYPE |= t.getMask();
			}
		EVERYBODY_HAS_ADMIN_RIGHTS = serverSettings.getProperty("EverybodyHasAdminRights", false);
		HIDE_GM_STATUS = serverSettings.getProperty("HideGMStatus", false);
		SHOW_GM_LOGIN = serverSettings.getProperty("ShowGMLogin", true);
		SAVE_GM_EFFECTS = serverSettings.getProperty("SaveGMEffects", false);
		CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
		ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		GLOBAL_SHOUT = serverSettings.getProperty("GlobalShout", false);
		CHAT_RANGE = serverSettings.getProperty("ChatRange", 1250);
		SHOUT_SQUARE_OFFSET = serverSettings.getProperty("ShoutOffset", 0);
		SHOUT_SQUARE_OFFSET *= SHOUT_SQUARE_OFFSET;
		ALLOW_FRACTION_WORLD_CHAT = serverSettings.getProperty("ALLOW_FRACTION_WORLD_CHAT", false);
		WORLD_CHAT_POINTS_PER_DAY = serverSettings.getProperty("WORLD_CHAT_POINTS_PER_DAY", 10);
		WORLD_CHAT_POINTS_PER_DAY_PA = serverSettings.getProperty("WORLD_CHAT_POINTS_PER_DAY_PA", 20);
		WORLD_CHAT_USE_MIN_LEVEL = serverSettings.getProperty("WORLD_CHAT_USE_MIN_LEVEL", 40);
		WORLD_CHAT_USE_MIN_LEVEL_PA = serverSettings.getProperty("WORLD_CHAT_USE_MIN_LEVEL_PA", 10);

		RATE_FISH_EXP  = serverSettings.getProperty("RateFishExp", 1.0);
		final double RATE_XP = serverSettings.getProperty("RateXp", 1.0);
		RATE_XP_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateXp = RATE_XP;
		for(int i = 1; i < RATE_XP_BY_LVL.length; ++i)
		{
			final double rate = serverSettings.getProperty("RateXpByLevel" + i, prevRateXp);
			RATE_XP_BY_LVL[i] = rate;
			if(rate != prevRateXp)
				prevRateXp = rate;
		}
		final double RATE_SP = serverSettings.getProperty("RateSp", 1.0);
		RATE_SP_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateSp = RATE_SP;
		for(int j = 1; j < RATE_SP_BY_LVL.length; ++j)
		{
			final double rate2 = serverSettings.getProperty("RateSpByLevel" + j, prevRateSp);
			RATE_SP_BY_LVL[j] = rate2;
			if(rate2 != prevRateSp)
				prevRateSp = rate2;
		}
		MAX_DROP_ITEMS_FROM_ONE_GROUP = serverSettings.getProperty("MAX_DROP_ITEMS_FROM_ONE_GROUP", 1);
		final double RATE_DROP_ADENA = serverSettings.getProperty("RateDropAdena", 1.0);
		RATE_DROP_ADENA_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateAdena = RATE_DROP_ADENA;
		for(int k = 1; k < RATE_DROP_ADENA_BY_LVL.length; ++k)
		{
			final double rate3 = serverSettings.getProperty("RateDropAdenaByLevel" + k, prevRateAdena);
			RATE_DROP_ADENA_BY_LVL[k] = rate3;
			if(rate3 != prevRateAdena)
				prevRateAdena = rate3;
		}
		final double RATE_DROP_ITEMS = serverSettings.getProperty("RateDropItems", 1.0);
		RATE_DROP_ITEMS_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateItems = RATE_DROP_ITEMS;
		for(int l = 1; l < RATE_DROP_ITEMS_BY_LVL.length; ++l)
		{
			final double rate4 = serverSettings.getProperty("RateDropItemsByLevel" + l, prevRateItems);
			RATE_DROP_ITEMS_BY_LVL[l] = rate4;
			if(rate4 != prevRateItems)
				prevRateItems = rate4;
		}
		DROP_CHANCE_MODIFIER = serverSettings.getProperty("DROP_CHANCE_MODIFIER", 1.0);
		final double RATE_DROP_SPOIL = serverSettings.getProperty("RateDropSpoil", 1.0);
		RATE_DROP_SPOIL_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateSpoil = RATE_DROP_SPOIL;
		for(int m = 1; m < RATE_DROP_SPOIL_BY_LVL.length; ++m)
		{
			final double rate5 = serverSettings.getProperty("RateDropSpoilByLevel" + m, prevRateSpoil);
			RATE_DROP_SPOIL_BY_LVL[m] = rate5;
			if(rate5 != prevRateSpoil)
				prevRateSpoil = rate5;
		}
		SPOIL_CHANCE_MODIFIER = serverSettings.getProperty("SPOIL_CHANCE_MODIFIER", 1.0);
		RATE_QUESTS_REWARD = serverSettings.getProperty("RateQuestsReward", 1.0);
		RATE_QUEST_REWARD_EXP_SP_ADENA_ONLY = serverSettings.getProperty("RATE_QUEST_REWARD_EXP_SP_ADENA_ONLY", true);
		QUESTS_REWARD_LIMIT_MODIFIER = serverSettings.getProperty("QUESTS_REWARD_LIMIT_MODIFIER", RATE_QUESTS_REWARD);
		RATE_QUESTS_DROP = serverSettings.getProperty("RateQuestsDrop", 1.0);
		RATE_CLAN_REP_SCORE = serverSettings.getProperty("RateClanRepScore", 1.0);
		RATE_CLAN_REP_SCORE_MAX_AFFECTED = serverSettings.getProperty("RateClanRepScoreMaxAffected", 2);
		RATE_XP_RAIDBOSS_MODIFIER = serverSettings.getProperty("RATE_XP_RAIDBOSS_MODIFIER", 1.0);
		RATE_DROP_RAIDBOSS = serverSettings.getProperty("RATE_DROP_RAIDBOSS", 1.0);
		NO_RATE_ITEMS = serverSettings.getProperty("NoRateItemIds", new int[] {
				6660,
				6662,
				6661,
				6659,
				6656,
				6658,
				8191,
				6657,
				10170,
				10314,
				16025,
				16026 });
		NO_RATE_EQUIPMENT = serverSettings.getProperty("NoRateEquipment", true);
		NO_RATE_KEY_MATERIAL = serverSettings.getProperty("NoRateKeyMaterial", true);
		NO_RATE_RECIPES = serverSettings.getProperty("NoRateRecipes", true);
		RATE_DROP_SIEGE_GUARD = serverSettings.getProperty("RateSiegeGuard", 1.0);
		RATE_MANOR = serverSettings.getProperty("RateManor", 1.0);
		RATE_PARTY_MIN = serverSettings.getProperty("RatePartyMin", false);
		final String[] ignoreAllDropButThis = serverSettings.getProperty("IgnoreAllDropButThis", "-1").split(";");
		for(final String dropId : ignoreAllDropButThis)
			if(dropId != null)
				if(!dropId.isEmpty())
					try
					{
						final int itemId = Integer.parseInt(dropId);
						if(itemId > 0)
							DROP_ONLY_THIS.add(itemId);
					}
					catch(NumberFormatException e)
					{
						_log.error("", e);
					}
		INCLUDE_RAID_DROP = serverSettings.getProperty("RemainRaidDropWithNoChanges", false);
		RATE_MOB_SPAWN = serverSettings.getProperty("RateMobSpawn", 1);
		RATE_MOB_SPAWN_MIN_LEVEL = serverSettings.getProperty("RateMobMinLevel", 1);
		RATE_MOB_SPAWN_MAX_LEVEL = serverSettings.getProperty("RateMobMaxLevel", 100);
		RATE_RAID_REGEN = serverSettings.getProperty("RateRaidRegen", 1.0);
		RATE_RAID_DEFENSE = serverSettings.getProperty("RateRaidDefense", 1.0);
		RATE_RAID_ATTACK = serverSettings.getProperty("RateRaidAttack", 1.0);
		RATE_EPIC_DEFENSE = serverSettings.getProperty("RateEpicDefense", RATE_RAID_DEFENSE);
		RATE_EPIC_ATTACK = serverSettings.getProperty("RateEpicAttack", RATE_RAID_ATTACK);
		RAID_MAX_LEVEL_DIFF = serverSettings.getProperty("RaidMaxLevelDiff", 8);
		PARALIZE_ON_RAID_DIFF = serverSettings.getProperty("ParalizeOnRaidLevelDiff", true);
		AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
		AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
		CHARACTER_DELETE_AFTER_HOURS = serverSettings.getProperty("DeleteCharAfterHours", 168);
		PURGE_BYPASS_TASK_FREQUENCY = serverSettings.getProperty("PurgeTaskFrequency", 60);

		try
		{
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
			DATAPACK_ROOT_PATH = DATAPACK_ROOT.toPath();
		}
		catch(IOException e2)
		{
			_log.error("", e2);
		}

        SCRIPTS_PATH = serverSettings.getProperty("ScriptsPath", Paths.get("."));

		ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
		ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
		ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
		ALLOW_WATER = serverSettings.getProperty("AllowWater", true);
		ALLOW_ITEMS_REFUND = serverSettings.getProperty("ALLOW_ITEMS_REFUND", true);
		(AVAILABLE_PROTOCOL_REVISIONS = new TIntHashSet()).addAll(serverSettings.getProperty("AvailableProtocolRevisions", new int[0]));
		MIN_NPC_ANIMATION = serverSettings.getProperty("MinNPCAnimation", 5);
		MAX_NPC_ANIMATION = serverSettings.getProperty("MaxNPCAnimation", 90);
		AUTOSAVE = serverSettings.getProperty("Autosave", true);
		MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);

		USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
		BROADCAST_STATS_INTERVAL = serverSettings.getProperty("BroadcastStatsInterval", true);
		BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);
		EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);

		SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
		EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);
		FULL_RANGE_PACKET_LIMIT = serverSettings.getProperty("FullRangePacketLimit", 25);
		SHORT_RANGE_PACKET_LIMIT = serverSettings.getProperty("ShortRangePacketLimit", 25);

		SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
		SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
		SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
		SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
		SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
		SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);

		ACCEPT_TIME = serverSettings.getProperty("AcceptTime", Duration.ofSeconds(30));
		CHAT_MESSAGE_MAX_LEN = serverSettings.getProperty("ChatMessageLimit", 1000);
		ABUSEWORD_BANCHAT = serverSettings.getProperty("ABUSEWORD_BANCHAT", false);
		int counter = 0;
		for (final int id : serverSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[]{0})) {
			BAN_CHANNEL_LIST[counter] = id;
			++counter;
		}
		ABUSEWORD_REPLACE = serverSettings.getProperty("ABUSEWORD_REPLACE", false);
		ABUSEWORD_REPLACE_STRING = serverSettings.getProperty("ABUSEWORD_REPLACE_STRING", "_-_");
		BANCHAT_ANNOUNCE = serverSettings.getProperty("BANCHAT_ANNOUNCE", true);
		BANCHAT_ANNOUNCE_FOR_ALL_WORLD = serverSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
		BANCHAT_ANNOUNCE_NICK = serverSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
		ABUSEWORD_BANTIME = serverSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);
		USE_CLIENT_LANG = serverSettings.getProperty("UseClientLang", false);
		DEFAULT_LANG = Language.valueOf(serverSettings.getProperty("DefaultLang", "ENGLISH").toUpperCase());
		RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");
		SHIFT_BY = serverSettings.getProperty("HShift", 12);
		RETAIL_MULTISELL_ENCHANT_TRANSFER = serverSettings.getProperty("RetailMultisellItemExchange", true);
		SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
		MAP_MIN_Z = serverSettings.getProperty("MapMinZ", -32768);
		MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", 32767);
		MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
		ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);
		DAMAGE_FROM_FALLING = serverSettings.getProperty("DamageFromFalling", true);
		ALLOW_WEDDING = serverSettings.getProperty("AllowWedding", false);
		WEDDING_PRICE = serverSettings.getProperty("WeddingPrice", 500000);
		WEDDING_PUNISH_INFIDELITY = serverSettings.getProperty("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT = serverSettings.getProperty("WeddingTeleport", true);
		WEDDING_TELEPORT_PRICE = serverSettings.getProperty("WeddingTeleportPrice", 500000);
		WEDDING_TELEPORT_INTERVAL = serverSettings.getProperty("WeddingTeleportInterval", 120);
		WEDDING_SAMESEX = serverSettings.getProperty("WeddingAllowSameSex", true);
		WEDDING_FORMALWEAR = serverSettings.getProperty("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = serverSettings.getProperty("WeddingDivorceCosts", 20);
		DONTLOADSPAWN = serverSettings.getProperty("StartWithoutSpawn", false);
		DONTLOADQUEST = serverSettings.getProperty("StartWithoutQuest", false);
		MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);
		WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);

		HTM_SHAPE_ARABIC = serverSettings.getProperty("HtmShapeArabic", false);
		SHUTDOWN_ANN_TYPE = serverSettings.getProperty("ShutdownAnnounceType", 1);
		APASSWD_TEMPLATE = serverSettings.getProperty("PasswordTemplate", "[A-Za-z0-9]{4,16}");
		ALLOW_MONSTER_RACE = serverSettings.getProperty("AllowMonsterRace", false);
		(AVAILABLE_LANGUAGES = new HashSet<>()).add(Language.ENGLISH);
		AVAILABLE_LANGUAGES.add(Language.RUSSIAN);
		AVAILABLE_LANGUAGES.add(DEFAULT_LANG);
		final String[] availableLanguages = serverSettings.getProperty("AVAILABLE_LANGUAGES", new String[0], ";");
		for(final String availableLanguage : availableLanguages)
			AVAILABLE_LANGUAGES.add(Language.valueOf(availableLanguage.toUpperCase()));
		MAX_ACTIVE_ACCOUNTS_ON_ONE_IP = serverSettings.getProperty("MAX_ACTIVE_ACCOUNTS_ON_ONE_IP", -1);
		MAX_ACTIVE_ACCOUNTS_IGNORED_IP = serverSettings.getProperty("MAX_ACTIVE_ACCOUNTS_IGNORED_IP", new String[0], ";");
		MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID = serverSettings.getProperty("MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID", -1);
		CHECK_HWID_MASK = serverSettings.getProperty("CHECK_HWID_MASK", 16);
		CHECK_HWID_MASK_MATCHES = serverSettings.getProperty("CHECK_HWID_MASK_MATCHES", 2);
		RESTRICTED_CHAR_CLAN_NAME_ENABLE = serverSettings.getProperty("RestrictedCharClanNameEnable", false);
		RESTRICTED_CHAR_CLAN_NAME = Pattern.compile(serverSettings.getProperty("RestrictedCharClanName", ""));
		CUSTOM_NEXT_TARGET_RADIUS = serverSettings.getProperty("CustomNextTargetRadius", 600);
		WHO_AM_GM_ONLY = serverSettings.getProperty("WhoAmGMOnly", false);
		PRE_REGISTRATION_STAGE = serverSettings.getProperty("PreRegistrationStage", false);
		PRE_REGISTRATION_STAGE_WHITE_LIST_ACCOUNTS = new ArrayList<>();
		for (String account : serverSettings.getProperty("PreRegistrationStageWhiteListAccounts", new String[0], ",")) {
			String login = account.trim();
			if (!login.isEmpty()) {
				PRE_REGISTRATION_STAGE_WHITE_LIST_ACCOUNTS.add(login.toLowerCase());
			}
		}
	}

	public static void loadTelnetConfig()
	{
		final PropertiesParser telnetSettings = load("config/telnet.properties");
		IS_TELNET_ENABLED = telnetSettings.getProperty("EnableTelnet", false);
		TELNET_DEFAULT_ENCODING = telnetSettings.getProperty("TelnetEncoding", "UTF-8");
		TELNET_PORT = telnetSettings.getProperty("Port", 7000);
		TELNET_HOSTNAME = telnetSettings.getProperty("BindAddress", "127.0.0.1");
		TELNET_PASSWORD = telnetSettings.getProperty("Password", "");
	}

	public static TIntObjectHashMap<LinkedHashMap<Integer, Skill>> BUFFER_PAGE_CONTENT = new TIntObjectHashMap<>();
	public static TIntObjectHashMap<Skill> BUFFER_BUFFS = new TIntObjectHashMap<>();
	public static int BUFFER_PREMIUM_PAGE;
	public static List<Skill> BUFFER_FIGHTER_SET = new ArrayList<>();
	public static List<Skill> BUFFER_MAGE_SET = new ArrayList<>();
	public static int BUFFER_MAX_PROFILES;
	public static int[] BUFFER_PREMIUM_ITEMS;

	public static void loadBufferConfig()
	{
		BUFFER_PAGE_CONTENT.clear();
		BUFFER_BUFFS.clear();
		BUFFER_MAGE_SET.clear();
		BUFFER_FIGHTER_SET.clear();

		final PropertiesParser bufferSettings = load(BUFFER_FILE);

		int count = bufferSettings.getProperty("PagesCount", 8);

		for(int i = 1; i <= count; i++)
		{
			String[] pageContent = bufferSettings.getProperty("Page" + i, "").replace(" ", "").split(";");

			LinkedHashMap<Integer, Skill> content = new LinkedHashMap<>();

			for(String data : pageContent)
			{
				int id = Integer.parseInt(data.split(",")[0]);
				int level = Integer.parseInt(data.split(",")[1]);

				Skill s = SkillHolder.getInstance().getSkill(id, level);
				if(s != null)
					content.put(s.getId(), s);
			}
			BUFFER_BUFFS.putAll(content);
			BUFFER_PAGE_CONTENT.put(i, content);
		}

		BUFFER_PREMIUM_PAGE = bufferSettings.getProperty("PremiumPage", 8);

		for(String data : bufferSettings.getProperty("FighterSet").replace(" ", "").split(";"))
		{
			int id = Integer.parseInt(data.split(",")[0]);
			int level = Integer.parseInt(data.split(",")[1]);

			Skill s = SkillHolder.getInstance().getSkill(id, level);
			if(s != null)
				BUFFER_FIGHTER_SET.add(s);
		}

		for(String data : bufferSettings.getProperty("MageSet").replace(" ", "").split(";"))
		{
			int id = Integer.parseInt(data.split(",")[0]);
			int level = Integer.parseInt(data.split(",")[1]);

			Skill s = SkillHolder.getInstance().getSkill(id, level);
			if(s != null)
				BUFFER_MAGE_SET.add(s);
		}

		BUFFER_MAX_PROFILES = bufferSettings.getProperty("MaxProfiles", 4);
		BUFFER_PREMIUM_ITEMS = bufferSettings.getProperty("PremiumItems", new int[] { 0 });
	}

	public static void loadResidenceConfig()
	{
		final PropertiesParser residenceSettings = load("config/residence.properties");
		CH_BID_GRADE1_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanLevel", 2);
		CH_BID_GRADE1_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembers", 1);
		CH_BID_GRADE1_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE2_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanLevel", 2);
		CH_BID_GRADE2_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembers", 1);
		CH_BID_GRADE2_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE3_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanLevel", 2);
		CH_BID_GRADE3_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembers", 1);
		CH_BID_GRADE3_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
		RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.0);
		RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1.0);
		final int[] tempCastleValidatonTime = residenceSettings.getProperty("CastleValidationDate", new int[] { 2, 4, 2003 });
		(CASTLE_VALIDATION_DATE = Calendar.getInstance()).set(5, tempCastleValidatonTime[0]);
		CASTLE_VALIDATION_DATE.set(2, tempCastleValidatonTime[1] - 1);
		CASTLE_VALIDATION_DATE.set(1, tempCastleValidatonTime[2]);
		CASTLE_VALIDATION_DATE.set(11, 0);
		CASTLE_VALIDATION_DATE.set(12, 0);
		CASTLE_VALIDATION_DATE.set(13, 0);
		CASTLE_VALIDATION_DATE.set(14, 0);
		LIGHT_CASTLE_SELL_TAX_PERCENT = residenceSettings.getProperty("LIGHT_CASTLE_SELL_TAX_PERCENT", 0);
		DARK_CASTLE_SELL_TAX_PERCENT = residenceSettings.getProperty("DARK_CASTLE_SELL_TAX_PERCENT", 15);
		LIGHT_CASTLE_BUY_TAX_PERCENT = residenceSettings.getProperty("LIGHT_CASTLE_BUY_TAX_PERCENT", 5);
		DARK_CASTLE_BUY_TAX_PERCENT = residenceSettings.getProperty("DARK_CASTLE_BUY_TAX_PERCENT", 10);
	}

	public static void loadAntiFloodConfig()
	{
		final PropertiesParser properties = load("config/antiflood.properties");
		ALL_CHAT_USE_MIN_LEVEL = properties.getProperty("ALL_CHAT_USE_MIN_LEVEL", 1);
		ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		ALL_CHAT_USE_DELAY = properties.getProperty("ALL_CHAT_USE_DELAY", 0);
		SHOUT_CHAT_USE_MIN_LEVEL = properties.getProperty("SHOUT_CHAT_USE_MIN_LEVEL", 1);
		SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		SHOUT_CHAT_USE_DELAY = properties.getProperty("SHOUT_CHAT_USE_DELAY", 0);
		FRACTION_SHOUT_CHAT_USE_MIN_LEVEL = properties.getProperty("FRACTION_SHOUT_CHAT_USE_MIN_LEVEL", 1);
		FRACTION_SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("FRACTION_SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		FRACTION_SHOUT_CHAT_USE_DELAY = properties.getProperty("FRACTION_SHOUT_CHAT_USE_DELAY", 0);
		HERO_CHAT_USE_MIN_LEVEL = properties.getProperty("HERO_CHAT_USE_MIN_LEVEL", 1);
		HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		HERO_CHAT_USE_DELAY = properties.getProperty("HERO_CHAT_USE_DELAY", 0);
		PRIVATE_CHAT_USE_MIN_LEVEL = properties.getProperty("PRIVATE_CHAT_USE_MIN_LEVEL", 1);
		PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		PRIVATE_CHAT_USE_DELAY = properties.getProperty("PRIVATE_CHAT_USE_DELAY", 0);
		MAIL_USE_MIN_LEVEL = properties.getProperty("MAIL_USE_MIN_LEVEL", 1);
		MAIL_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("MAIL_USE_MIN_LEVEL_WITHOUT_PA", 1);
		MAIL_USE_DELAY = properties.getProperty("MAIL_USE_DELAY", 0);
	}

	public static void loadCustomConfig()
	{
		final PropertiesParser customSettings = load("config/custom.properties");
		ONLINE_GENERATOR_ENABLED = customSettings.getProperty("OnlineGeneratorEnabled", false);
		ONLINE_GENERATOR_DELAY = customSettings.getProperty("OnlineGeneratorDelay", 1);
	}

	public static void loadOtherConfig()
	{
		final PropertiesParser otherSettings = load("config/other.properties");
		DEEPBLUE_DROP_RULES = otherSettings.getProperty("UseDeepBlueDropRules", true);
		DEEPBLUE_DROP_MAXDIFF = otherSettings.getProperty("DeepBlueDropMaxDiff", 8);
		DEEPBLUE_DROP_RAID_MAXDIFF = otherSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);
		DEEPBLUE_DROP_PERCENT_EACH_LVL = otherSettings.getProperty("DeepBluePercentEachLvl", 20);
		SWIMING_SPEED = otherSettings.getProperty("SwimingSpeedTemplate", 50);
		INVENTORY_MAXIMUM_NO_DWARF = otherSettings.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = otherSettings.getProperty("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_GM = otherSettings.getProperty("MaximumSlotsForGMPlayer", 250);
		QUEST_INVENTORY_MAXIMUM = otherSettings.getProperty("MaximumSlotsForQuests", 100);
		MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 40);
		WAREHOUSE_SLOTS_NO_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = otherSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
		FREIGHT_SLOTS = otherSettings.getProperty("MaximumFreightSlots", 10);
		REGEN_SIT_WAIT = otherSettings.getProperty("RegenSitWait", false);
		UNSTUCK_SKILL = otherSettings.getProperty("UnstuckSkill", true);
		RESPAWN_RESTORE_CP = otherSettings.getProperty("RespawnRestoreCP", 0.0) / 100.0;
		RESPAWN_RESTORE_HP = otherSettings.getProperty("RespawnRestoreHP", 65.0) / 100.0;
		RESPAWN_RESTORE_MP = otherSettings.getProperty("RespawnRestoreMP", 0.0) / 100.0;
		MAX_PVTSTORE_SLOTS_DWARF = otherSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = otherSettings.getProperty("MaxPvtStoreSlotsOther", 4);
		MAX_PVTCRAFT_SLOTS = otherSettings.getProperty("MaxPvtManufactureSlots", 20);
		SENDSTATUS_TRADE_JUST_OFFLINE = otherSettings.getProperty("SendStatusTradeJustOffline", false);
		SENDSTATUS_TRADE_MOD = otherSettings.getProperty("SendStatusTradeMod", 1.0);
		ANNOUNCE_MAMMON_SPAWN = otherSettings.getProperty("AnnounceMammonSpawn", true);
		GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "FFFFFF"));
		GM_HERO_AURA = otherSettings.getProperty("GMHeroAura", false);
		NORMAL_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("NormalNameColour", "FFFFFF"));
		CLANLEADER_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("ClanleaderNameColour", "FFFFFF"));
		SHOW_HTML_WELCOME = otherSettings.getProperty("ShowHTMLWelcome", false);
		MONSTER_LEVEL_DIFF_EXP_PENALTY = otherSettings.getProperty("MONSTER_LEVEL_DIFF_EXP_PENALTY", new double[] {
				0,
				0,
				0,
				0,
				0,
				16.77,
				30.66,
				42.17,
				51.77,
				59.81,
				100 });
	}

	public static void loadSpoilConfig()
	{
		final PropertiesParser spoilSettings = load("config/spoil.properties");
		BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.0);
		MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.0);
		MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.0);
		MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.0);
		MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.0);
		MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
		MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.0);
		MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
		MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.0);
		ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
		MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
		MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 0);
		MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
		MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 0);
		MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
	}
	public static double DEATH_LINK_MOD;
	
	public static void loadFormulasConfig()
	{
		final PropertiesParser formulasSettings = load("config/formulas.properties");
		SKILLS_CHANCE_POW = formulasSettings.getProperty("SkillsChancePow", 0.5);
		DEATH_LINK_MOD = formulasSettings.getProperty("DeathLinkMod", 1.8);

		SKILLS_CHANCE_MOD = formulasSettings.getProperty("SkillsChanceMod", 11.0);
		SKILLS_CHANCE_POW = formulasSettings.getProperty("SkillsChancePow", 0.5);
		SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 10.0);
		SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 100.0);
		SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);
		LIM_PATK = formulasSettings.getProperty("LimitPatk", 20000);
		LIM_MATK = formulasSettings.getProperty("LimitMAtk", 25000);
		LIM_PDEF = formulasSettings.getProperty("LimitPDef", 15000);
		LIM_MDEF = formulasSettings.getProperty("LimitMDef", 15000);
		LIM_PATK_SPD = formulasSettings.getProperty("LimitPatkSpd", 1500);
		LIM_MATK_SPD = formulasSettings.getProperty("LimitMatkSpd", 1999);
		LIM_CRIT_DAM = formulasSettings.getProperty("LimitCriticalDamage", 500);
		LIM_CRIT = formulasSettings.getProperty("LimitCritical", 500);
		LIM_MCRIT = formulasSettings.getProperty("LimitMCritical", 20);
		LIM_ACCURACY = formulasSettings.getProperty("LimitAccuracy", 200);
		LIM_EVASION = formulasSettings.getProperty("LimitEvasion", 200);
		LIM_MOVE = formulasSettings.getProperty("LimitMove", 250);
		HP_LIMIT = formulasSettings.getProperty("HP_LIMIT", 150000);
		MP_LIMIT = formulasSettings.getProperty("MP_LIMIT", -1);
		CP_LIMIT = formulasSettings.getProperty("CP_LIMIT", -1);
		LIM_FAME = formulasSettings.getProperty("LimitFame", 50000);
		LIM_RAID_POINTS = formulasSettings.getProperty("LIM_RAID_POINTS", 50000);
		ALT_NPC_PATK_MODIFIER = formulasSettings.getProperty("NpcPAtkModifier", 1.0);
		ALT_NPC_MATK_MODIFIER = formulasSettings.getProperty("NpcMAtkModifier", 1.0);
		ALT_NPC_MAXHP_MODIFIER = formulasSettings.getProperty("NpcMaxHpModifier", 1.0);
		ALT_NPC_MAXMP_MODIFIER = formulasSettings.getProperty("NpcMapMpModifier", 1.0);
		ALT_POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("PoleDamageModifier", 1.0);
		ALT_M_SIMPLE_DAMAGE_MOD = formulasSettings.getProperty("mDamSimpleModifier", 1.0);
		ALT_P_DAMAGE_MOD = formulasSettings.getProperty("pDamMod", 1.0);
		ALT_M_CRIT_DAMAGE_MOD = formulasSettings.getProperty("mCritModifier", 1.0);
		ALT_P_CRIT_DAMAGE_MOD = formulasSettings.getProperty("pCritModifier", 1.0);
		
		ALT_P_DAMAGE_MOD_ARCHER = formulasSettings.getProperty("pDamModArcher", 1.0);
		ALT_P_CRIT_DAMAGE_MOD_ARCHER = formulasSettings.getProperty("pCritModifierArcher", 1.0);
		
		
		ALT_P_CRIT_CHANCE_MOD = formulasSettings.getProperty("pCritModifierChance", 1.0);
		ALT_M_CRIT_CHANCE_MOD = formulasSettings.getProperty("mCritModifierChance", 1.0);
		ALT_BLOW_DAMAGE_MOD = formulasSettings.getProperty("blowDamageModifier", 1.0);
		ALT_BLOW_CRIT_RATE_MODIFIER = formulasSettings.getProperty("blowCritRateModifier", 1.0);
		ALT_VAMPIRIC_CHANCE = formulasSettings.getProperty("vampiricChance", 20.0);
		ALT_VAMPIRIC_MP_CHANCE = formulasSettings.getProperty("vampiricMpChance", 20.0);
		REFLECT_MIN_RANGE = formulasSettings.getProperty("ReflectMinimumRange", 600);
		REFLECT_AND_BLOCK_DAMAGE_CHANCE_CAP = formulasSettings.getProperty("reflectAndBlockDamCap", 60.0);
		REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE_CAP = formulasSettings.getProperty("reflectAndBlockPSkillDamCap", 60.0);
		REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE_CAP = formulasSettings.getProperty("reflectAndBlockMSkillDamCap", 60.0);
		REFLECT_DAMAGE_PERCENT_CAP = formulasSettings.getProperty("reflectDamCap", 60.0);
		REFLECT_BOW_DAMAGE_PERCENT_CAP = formulasSettings.getProperty("reflectBowDamCap", 60.0);
		REFLECT_PSKILL_DAMAGE_PERCENT_CAP = formulasSettings.getProperty("reflectPSkillDamCap", 60.0);
		REFLECT_MSKILL_DAMAGE_PERCENT_CAP = formulasSettings.getProperty("reflectMSkillDamCap", 60.0);
		SPECIAL_CLASS_BOW_CROSS_BOW_PENALTY = formulasSettings.getProperty("specialClassesWeaponMagicSpeedPenalty", 1.0);
		DISABLE_VAMPIRIC_VS_MOB_ON_PVP = formulasSettings.getProperty("disableVampiricAndDrainPvEInPvp", false);
		MIN_HIT_TIME = formulasSettings.getProperty("MinimumHitTime", -1);
		PHYSICAL_MIN_CHANCE_TO_HIT = formulasSettings.getProperty("PHYSICAL_MIN_CHANCE_TO_HIT", 27.5);
		PHYSICAL_MAX_CHANCE_TO_HIT = formulasSettings.getProperty("PHYSICAL_MAX_CHANCE_TO_HIT", 98.0);
		MAGIC_MIN_CHANCE_TO_HIT_MISS = formulasSettings.getProperty("MAGIC_MIN_CHANCE_TO_HIT_MISS", 5.0);
		MAGIC_MAX_CHANCE_TO_HIT_MISS = formulasSettings.getProperty("MAGIC_MAX_CHANCE_TO_HIT_MISS", 95.0);
		ENABLE_CRIT_DMG_REDUCTION_ON_MAGIC = formulasSettings.getProperty("ENABLE_CRIT_DMG_REDUCTION_ON_MAGIC", true);
		MAX_BLOW_RATE_ON_BEHIND = formulasSettings.getProperty("MAX_BLOW_RATE_ON_BEHIND", 100.0);
		MAX_BLOW_RATE_ON_FRONT_AND_SIDE = formulasSettings.getProperty("MAX_BLOW_RATE_ON_FRONT_AND_SIDE", 80.0);
		BLOW_SKILL_CHANCE_MOD_ON_BEHIND = formulasSettings.getProperty("BLOW_SKILL_CHANCE_MOD_ON_BEHIND", 5.0);
		BLOW_SKILL_CHANCE_MOD_ON_FRONT = formulasSettings.getProperty("BLOW_SKILL_CHANCE_MOD_ON_FRONT", 4.0);
		ENABLE_MATK_SKILL_LANDING_MOD = formulasSettings.getProperty("ENABLE_MATK_SKILL_LANDING_MOD", false);
		ENABLE_WIT_SKILL_LANDING_MOD = formulasSettings.getProperty("ENABLE_WIT_SKILL_LANDING_MOD", false);
		BLOW_SKILL_DEX_CHANCE_MOD = formulasSettings.getProperty("BLOW_SKILL_DEX_CHANCE_MOD", 1.0);
		NORMAL_SKILL_DEX_CHANCE_MOD = formulasSettings.getProperty("NORMAL_SKILL_DEX_CHANCE_MOD", 1.0);
		EXCELLENT_SHIELD_BLOCK_CHANCE = formulasSettings.getProperty("ExcellentShieldBlockChance", 5);
		EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE = formulasSettings.getProperty("ExcellentShieldBlockDamage", 1);
		SKILLS_CAST_TIME_MIN_PHYSICAL = formulasSettings.getProperty("MinCastTimePhysical", 396);
		SKILLS_CAST_TIME_MIN_MAGICAL = formulasSettings.getProperty("MinCastTimeMagical", 333);
		ENABLE_CRIT_HEIGHT_BONUS = formulasSettings.getProperty("EnableCritHeightBonus", true);
		CRIT_STUN_BREAK_CHANCE_ON_MAGICAL_SKILL = formulasSettings.getProperty("CritStunBreakChanceOnMagicSkill", 66.67);
		NORMAL_STUN_BREAK_CHANCE_ON_MAGICAL_SKILL = formulasSettings.getProperty("NormalStunBreakChanceOnMagicSkill", 33.33);
		CRIT_STUN_BREAK_CHANCE_ON_PHYSICAL_SKILL = formulasSettings.getProperty("CritStunBreakChanceOnPhysSkill", 66.67);
		NORMAL_STUN_BREAK_CHANCE_ON_PHYSICAL_SKILL = formulasSettings.getProperty("NormalStunBreakChanceOnPhysSkill", 33.33);
		CRIT_STUN_BREAK_CHANCE_ON_REGULAR_HIT = formulasSettings.getProperty("CritStunBreakOnRegularHit", 33.33);
		NORMAL_STUN_BREAK_CHANCE_ON_REGULAR_HIT = formulasSettings.getProperty("NormalStunBreakOnRegularHit", 16.67);
		SP_LIMIT = formulasSettings.getProperty("SP_LIMIT", 5000000000L);
		ELEMENT_ATTACK_LIMIT = formulasSettings.getProperty("ELEMENT_ATTACK_LIMIT", 999);
		SERVITOR_PVP_DAMAGE_MODIFIER = formulasSettings.getProperty("ServitorPvPDamageModifier", 1.0);
		SERVITOR_OLYMPIAD_DAMAGE_MODIFIER = formulasSettings.getProperty("ServitorOlympiadDamageModifier", 1.0);
	}

	public static void loadExtSettings()
	{
		final PropertiesParser properties = load("config/ext.properties");
		EX_NEW_PETITION_SYSTEM = properties.getProperty("NewPetitionSystem", false);
		EX_JAPAN_MINIGAME = properties.getProperty("JapanMinigame", false);
		EX_LECTURE_MARK = properties.getProperty("LectureMark", false);
		EX_SECOND_AUTH_ENABLED = properties.getProperty("SecondAuthEnabled", false);
		EX_SECOND_AUTH_MAX_ATTEMPTS = properties.getProperty("SecondAuthMaxAttempts", 5);
		EX_SECOND_AUTH_BAN_TIME = properties.getProperty("SecondAuthBanTime", 480);
		EX_USE_QUEST_REWARD_PENALTY_PER = properties.getProperty("UseQuestRewardPenaltyPer", false);
		EX_F2P_QUEST_REWARD_PENALTY_PER = properties.getProperty("F2PQuestRewardPenaltyPer", 0);
		(EX_F2P_QUEST_REWARD_PENALTY_QUESTS = new TIntHashSet()).addAll(properties.getProperty("F2PQuestRewardPenaltyQuests", new int[0]));
		EX_USE_PREMIUM_HENNA_SLOT = properties.getProperty("UsePremiumHennaSlot", false);
		VIP_ATTENDANCE_REWARDS_ENABLED = properties.getProperty("UseVIPAttendance", false);
		EX_USE_AUTO_SOUL_SHOT = properties.getProperty("UseAutoSoulShot", false);
		EX_USE_TO_DO_LIST = properties.getProperty("UseToDoList", true);
	}

	public static boolean COMMUNITYBOARD_ENABLED;
	public static String BBS_DEFAULT;
	public static String BBS_PATH;
	public static boolean BBS_CHECK_IN_COMBAT;
	public static boolean BBS_CHECK_DEATH;
	public static boolean BBS_CHECK_MOVEMENT_DISABLE;
	public static boolean BBS_CHECK_ON_SIEGE_FIELD;
	public static boolean BBS_CHECK_ATTACKING_NOW;
	public static boolean BBS_CHECK_IN_OLYMPIAD_MODE;
	public static boolean BBS_CHECK_FLYING;
	public static boolean BBS_CHECK_IN_DUEL;
	public static boolean BBS_CHECK_IN_INSTANCE;
	public static boolean BBS_CHECK_IN_JAILED;
	public static boolean BBS_CHECK_OUT_OF_CONTROL;
	public static boolean BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM;
	public static boolean BBS_CHECK_IN_EVENT;

	public static void loadBBSSettings()
	{
		PropertiesParser board = load(BBS_FILE);
		COMMUNITYBOARD_ENABLED = board.getProperty("Allow", true);
		BBS_DEFAULT = board.getProperty("Link", "_bbshome");
		BBS_PATH = board.getProperty("Path", "community");
		BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM = board.getProperty("OutOfTownForPremium", false);
		BBS_CHECK_MOVEMENT_DISABLE = board.getProperty("MovementDisabled", false);
		BBS_CHECK_IN_COMBAT = board.getProperty("InCombat", false);
		BBS_CHECK_DEATH = board.getProperty("Death", false);
		BBS_CHECK_ON_SIEGE_FIELD = board.getProperty("OnSiegeField", false);
		BBS_CHECK_ATTACKING_NOW = board.getProperty("AttackingNow", false);
		BBS_CHECK_IN_OLYMPIAD_MODE = board.getProperty("InOlympiadMode", false);
		BBS_CHECK_FLYING = board.getProperty("Flying", false);
		BBS_CHECK_IN_DUEL = board.getProperty("InDuel", false);
		BBS_CHECK_IN_INSTANCE = board.getProperty("InInstance", false);
		BBS_CHECK_IN_JAILED = board.getProperty("InJailed", false);
		BBS_CHECK_OUT_OF_CONTROL = board.getProperty("OutOfControl", false);
		BBS_CHECK_IN_EVENT = board.getProperty("InEvent", false);
	}

	public static void loadAltSettings()
	{
		final PropertiesParser altSettings = load("config/altsettings.properties");
		STARTING_LVL = altSettings.getProperty("StartingLvl", 1);
		STARTING_SP = altSettings.getProperty("StartingSP", 0L);
		STARTING_ITEM = altSettings.getProperty("StartingItem", "").trim().replaceAll(" ", "").split(";");
		ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
		ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
		ALLOW_DELEVEL_COMMAND = altSettings.getProperty("AllowDelevelCommand", false);
		ALT_SAVE_UNSAVEABLE = altSettings.getProperty("AltSaveUnsaveable", false);
		ALT_SAVE_EFFECTS_REMAINING_TIME = altSettings.getProperty("AltSaveEffectsRemainingTime", 5);
		ALT_SHOW_REUSE_MSG = altSettings.getProperty("AltShowSkillReuseMessage", true);
		ALT_DELETE_SA_BUFFS = altSettings.getProperty("AltDeleteSABuffs", false);
		AUTO_LOOT = altSettings.getProperty("AutoLoot", false);
		AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_ONLY_ADENA = altSettings.getProperty("AutoLootOnlyAdena", false);
		AUTO_LOOT_INDIVIDUAL = altSettings.getProperty("AutoLootIndividual", false);
		AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
		final String[] autoLootItemIdList = altSettings.getProperty("AutoLootItemIdList", "-1").split(";");
		for(final String item : autoLootItemIdList)
			if(item != null)
				if(!item.isEmpty())
					try
					{
						final int itemId = Integer.parseInt(item);
						if(itemId > 0)
							AUTO_LOOT_ITEM_ID_LIST.add(itemId);
					}
					catch(NumberFormatException e)
					{
						_log.error("", e);
					}
		AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", false);
		SAVING_SPS = altSettings.getProperty("SavingSpS", false);
		MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
		ALT_RAID_RESPAWN_MULTIPLIER = altSettings.getProperty("AltRaidRespawnMultiplier", 1.0);
		ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
		ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
		ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
		ALLOW_VOICED_COMMANDS = altSettings.getProperty("AllowVoicedCommands", true);

		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubClassWithoutQuest", false);
		ALT_GAME_ERTHEIA_DUALCLASS_WITHOUT_QUESTS = altSettings.getProperty("ALT_GAME_ERTHEIA_DUALCLASS_WITHOUT_QUESTS", false);
		ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
		ALT_MAX_LEVEL = Math.min(altSettings.getProperty("AltMaxLevel", 127), Experience.LEVEL.length - 2);
		ALT_MAX_SUB_LEVEL = Math.min(altSettings.getProperty("AltMaxSubLevel", 75), Experience.LEVEL.length - 2);
		ALT_ALLOW_AWAKE_ON_SUB_CLASS = altSettings.getProperty("AltAllowAwakeOnSubClass", false);
		ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);
		ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = altSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);
		BAN_FOR_CFG_USAGE = altSettings.getProperty("BanForCfgUsageAgainsBots", false);
		ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
		SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
		PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
		AUTO_LEARN_SKILLS = altSettings.getProperty("AutoLearnSkills", false);
		ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);
		ALT_DISABLE_SPELLBOOKS = altSettings.getProperty("AltDisableSpellbooks", false);
		ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
		ALT_MUSIC_LIMIT = altSettings.getProperty("ALT_MUSIC_LIMIT", 12);
		ALT_DEBUFF_LIMIT = altSettings.getProperty("ALT_DEBUFF_LIMIT", 12);
		ALT_TRIGGER_LIMIT = altSettings.getProperty("ALT_TRIGGER_LIMIT", 12);
		ALLOW_DEATH_PENALTY = altSettings.getProperty("EnableDeathPenalty", true);
		ALT_DEATH_PENALTY_CHANCE = altSettings.getProperty("DeathPenaltyChance", 10);
		ALT_DEATH_PENALTY_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyRateExpPenalty", 1);
		ALT_DEATH_PENALTY_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
		NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;
		ALT_NO_LASTHIT = altSettings.getProperty("NoLasthitOnRaid", false);
		ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = altSettings.getProperty("KamalokaNightmaresPremiumOnly", false);
		ALT_PET_HEAL_BATTLE_ONLY = altSettings.getProperty("PetsHealOnlyInBattle", true);
		CHAR_TITLE = altSettings.getProperty("CharTitle", false);
		ADD_CHAR_TITLE = altSettings.getProperty("CharAddTitle", "");
		ALT_DISABLED_MULTISELL = altSettings.getProperty("DisabledMultisells", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_PRICE_LIMITS = altSettings.getProperty("ShopPriceLimits", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_UNALLOWED_ITEMS = altSettings.getProperty("ShopUnallowedItems", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_ALLOWED_PET_POTIONS = altSettings.getProperty("AllowedPetPotions", new int[] { 735, 1060, 1061, 1062, 1374, 1375, 1539, 1540, 6035, 6036 });
		FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
		FESTIVAL_RATE_PRICE = altSettings.getProperty("FestivalRatePrice", 1.0);
		RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
		RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 10000);
		RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
		RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
		RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);
		RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
		RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
		RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
		RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
		RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
		RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
		ALLOW_CLANSKILLS = altSettings.getProperty("AllowClanSkills", true);
		ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = altSettings.getProperty("AllowLearnTransSkillsWOQuest", false);
		MAXIMUM_MEMBERS_IN_PARTY = altSettings.getProperty("MAXIMUM_MEMBERS_IN_PARTY", 9);
		PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
		ALLOW_TALK_WHILE_SITTING = altSettings.getProperty("AllowTalkWhileSitting", true);
		ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);
		BUFFTIME_MODIFIER = altSettings.getProperty("BuffTimeModifier", 1.0);
		BUFFTIME_MODIFIER_SKILLS = altSettings.getProperty("BuffTimeModifierSkills", new int[0]);
		CLANHALL_BUFFTIME_MODIFIER = altSettings.getProperty("ClanHallBuffTimeModifier", 1.0);
		SONGDANCETIME_MODIFIER = altSettings.getProperty("SongDanceTimeModifier", 1.0);
		MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0);
		GATEKEEPER_MODIFIER = altSettings.getProperty("GkCostMultiplier", 1.0);
		GATEKEEPER_FREE = altSettings.getProperty("GkFree", 40);
		CRUMA_GATEKEEPER_LVL = altSettings.getProperty("GkCruma", 65);
		ALT_CHAMPION_CHANCE1 = altSettings.getProperty("AltChampionChance1", 0.0);
		ALT_CHAMPION_CHANCE2 = altSettings.getProperty("AltChampionChance2", 0.0);
		ALT_CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("AltChampionAggro", false);
		ALT_CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("AltChampionSocial", false);
		ALT_CHAMPION_MIN_LEVEL = altSettings.getProperty("AltChampionMinLevel", 40);
		ALT_CHAMPION_TOP_LEVEL = altSettings.getProperty("AltChampionTopLevel", 75);
		SPECIAL_ITEM_ID = altSettings.getProperty("ChampionSpecialItem", 0);
		SPECIAL_ITEM_COUNT = altSettings.getProperty("ChampionSpecialItemCount", 1);
		SPECIAL_ITEM_DROP_CHANCE = altSettings.getProperty("ChampionSpecialItemDropChance", 100.0);
		ALT_VITALITY_RATE = altSettings.getProperty("ALT_VITALITY_RATE", 200) / 100;
		ALT_VITALITY_PA_RATE = altSettings.getProperty("ALT_VITALITY_PA_RATE", 300) / 100;
		ALT_VITALITY_CONSUME_RATE = altSettings.getProperty("ALT_VITALITY_CONSUME_RATE", 1.0);
		ALT_VITALITY_POTIONS_LIMIT = altSettings.getProperty("ALT_VITALITY_POTIONS_LIMIT", 5);
		ALT_VITALITY_POTIONS_PA_LIMIT = altSettings.getProperty("ALT_VITALITY_POTIONS_PA_LIMIT", 10);
		ALT_PCBANG_POINTS_ENABLED = altSettings.getProperty("AltPcBangPointsEnabled", false);
		PC_BANG_POINTS_BY_ACCOUNT = altSettings.getProperty("PC_BANG_POINTS_BY_ACCOUNT", false);
		ALT_PCBANG_POINTS_ONLY_PREMIUM = altSettings.getProperty("AltPcBangPointsOnlyPA", false);
		ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE = altSettings.getProperty("AltPcBangPointsDoubleChance", 10.0);
		ALT_PCBANG_POINTS_BONUS = altSettings.getProperty("AltPcBangPointsBonus", 0);
		ALT_PCBANG_POINTS_DELAY = altSettings.getProperty("AltPcBangPointsDelay", 20);
		ALT_PCBANG_POINTS_MIN_LVL = altSettings.getProperty("AltPcBangPointsMinLvl", 1);
		ALT_ALLOWED_MULTISELLS_IN_PCBANG.addAll(altSettings.getProperty("ALT_ALLOWED_MULTISELLS_IN_PCBANG", new int[0]));
		ALT_DEBUG_PVP_ENABLED = altSettings.getProperty("AltDebugPvPEnabled", false);
		ALT_DEBUG_PVP_DUEL_ONLY = altSettings.getProperty("AltDebugPvPDuelOnly", true);
		ALT_DEBUG_PVE_ENABLED = altSettings.getProperty("AltDebugPvEEnabled", false);
		ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
		ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
		ALT_PARTY_BONUS = altSettings.getProperty("AltPartyBonus", new double[] { 1.0, 1.3, 1.35, 1.4, 1.55, 1.6, 1.7, 1.8, 2.0 });
		ALT_PARTY_CLAN_BONUS = altSettings.getProperty("ALT_PARTY_CLAN_BONUS", new double[] { 1.0 });
		ALT_PARTY_LVL_DIFF_PENALTY = altSettings.getProperty("ALT_PARTY_LVL_DIFF_PENALTY", new int[] {
				100,
				98,
				95,
				93,
				91,
				88,
				86,
				83,
				81,
				78,
				23,
				22,
				21,
				20,
				19,
				0 });
		ALT_ALL_PHYS_SKILLS_OVERHIT = altSettings.getProperty("AltAllPhysSkillsOverhit", true);
		ALT_REMOVE_SKILLS_ON_DELEVEL = altSettings.getProperty("AltRemoveSkillsOnDelevel", true);
		ALLOW_CH_DOOR_OPEN_ON_CLICK = altSettings.getProperty("AllowChDoorOpenOnClick", true);
		ALT_CH_SIMPLE_DIALOG = altSettings.getProperty("AltChSimpleDialog", false);
		ALT_CH_UNLIM_MP = altSettings.getProperty("ALT_CH_UNLIM_MP", true);
		ALT_NO_FAME_FOR_DEAD = altSettings.getProperty("AltNoFameForDead", false);
		AUGMENTATION_NG_SKILL_CHANCE = altSettings.getProperty("AugmentationNGSkillChance", 15);
		AUGMENTATION_NG_GLOW_CHANCE = altSettings.getProperty("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_SKILL_CHANCE = altSettings.getProperty("AugmentationMidSkillChance", 30);
		AUGMENTATION_MID_GLOW_CHANCE = altSettings.getProperty("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_SKILL_CHANCE = altSettings.getProperty("AugmentationHighSkillChance", 45);
		AUGMENTATION_HIGH_GLOW_CHANCE = altSettings.getProperty("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_SKILL_CHANCE = altSettings.getProperty("AugmentationTopSkillChance", 60);
		AUGMENTATION_TOP_GLOW_CHANCE = altSettings.getProperty("AugmentationTopGlowChance", 100);
		AUGMENTATION_BASESTAT_CHANCE = altSettings.getProperty("AugmentationBaseStatChance", 1);
		AUGMENTATION_ACC_SKILL_CHANCE = altSettings.getProperty("AugmentationAccSkillChance", 10);
		ALT_SHOW_SERVER_TIME = altSettings.getProperty("ShowServerTime", false);
		FOLLOW_RANGE = altSettings.getProperty("FollowRange", 100);
		ALT_ITEM_AUCTION_ENABLED = altSettings.getProperty("AltItemAuctionEnabled", true);
		ALT_CUSTOM_ITEM_AUCTION_ENABLED = ALT_ITEM_AUCTION_ENABLED && altSettings.getProperty("AltCustomItemAuctionEnabled", "").hashCode() == -538745924;
		ALT_ITEM_AUCTION_CAN_REBID = altSettings.getProperty("AltItemAuctionCanRebid", false);
		ALT_ITEM_AUCTION_START_ANNOUNCE = altSettings.getProperty("AltItemAuctionAnnounce", true);
		ALT_ITEM_AUCTION_MAX_BID = altSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
		ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = altSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);
		ALT_ENABLE_BLOCK_CHECKER_EVENT = altSettings.getProperty("EnableBlockCheckerEvent", true);
		ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Math.min(Math.max(altSettings.getProperty("BlockCheckerMinOlympiadMembers", 1), 1), 6);
		ALT_RATE_COINS_REWARD_BLOCK_CHECKER = altSettings.getProperty("BlockCheckerRateCoinReward", 1.0);
		ALT_HBCE_FAIR_PLAY = altSettings.getProperty("HBCEFairPlay", false);
		ALT_PET_INVENTORY_LIMIT = altSettings.getProperty("AltPetInventoryLimit", 12);
		ALLOW_FAKE_PLAYERS = altSettings.getProperty("AllowFake", false);
		FAKE_PLAYERS_PERCENT = altSettings.getProperty("FakePercent", 0);
		DISABLE_CRYSTALIZATION_ITEMS = altSettings.getProperty("DisableCrystalizationItems", false);
		SUB_START_LEVEL = altSettings.getProperty("SubClassStartLevel", 40);
		START_CLAN_LEVEL = altSettings.getProperty("ClanStartLevel", 0);
		NEW_CHAR_IS_NOBLE = altSettings.getProperty("IsNewCharNoble", false);
		ENABLE_L2_TOP_OVERONLINE = altSettings.getProperty("EnableL2TOPFakeOnline", false);
		L2TOP_MAX_ONLINE = altSettings.getProperty("L2TOPMaxOnline", 3000);
		MIN_ONLINE_0_5_AM = altSettings.getProperty("MinOnlineFrom00to05", 500);
		MAX_ONLINE_0_5_AM = altSettings.getProperty("MaxOnlineFrom00to05", 700);
		MIN_ONLINE_6_11_AM = altSettings.getProperty("MinOnlineFrom06to11", 700);
		MAX_ONLINE_6_11_AM = altSettings.getProperty("MaxOnlineFrom06to11", 1000);
		MIN_ONLINE_12_6_PM = altSettings.getProperty("MinOnlineFrom12to18", 1000);
		MAX_ONLINE_12_6_PM = altSettings.getProperty("MaxOnlineFrom12to18", 1500);
		MIN_ONLINE_7_11_PM = altSettings.getProperty("MinOnlineFrom19to23", 1500);
		MAX_ONLINE_7_11_PM = altSettings.getProperty("MaxOnlineFrom19to23", 2500);
		ADD_ONLINE_ON_SIMPLE_DAY = altSettings.getProperty("AddOnlineIfSimpleDay", 50);
		ADD_ONLINE_ON_WEEKEND = altSettings.getProperty("AddOnlineIfWeekend", 300);
		L2TOP_MIN_TRADERS = altSettings.getProperty("L2TOPMinTraders", 80);
		L2TOP_MAX_TRADERS = altSettings.getProperty("L2TOPMaxTraders", 190);

		ONLINE_MULTIPLIER = altSettings.getProperty("OnlineMultiplier", 1.0D);

		ALT_SELL_ITEM_ONE_ADENA = altSettings.getProperty("AltSellItemOneAdena", false);
		MAX_SIEGE_CLANS = altSettings.getProperty("MaxSiegeClans", 20);
		ONLY_ONE_SIEGE_PER_CLAN = altSettings.getProperty("OneClanCanRegisterOnOneSiege", false);
		CLAN_WAR_MINIMUM_CLAN_LEVEL = altSettings.getProperty("CLAN_WAR_MINIMUM_CLAN_LEVEL", 3);
		CLAN_WAR_MINIMUM_PLAYERS_DECLARE = altSettings.getProperty("CLAN_WAR_MINIMUM_PLAYERS_DECLARE", 15);
		CLAN_WAR_PREPARATION_DAYS_PERIOD = altSettings.getProperty("CLAN_WAR_PREPARATION_DAYS_PERIOD", 3);
		CLAN_WAR_REPUTATION_SCORE_PER_KILL = altSettings.getProperty("CLAN_WAR_REPUTATION_SCORE_PER_KILL", 1);

		ALLOW_USE_DOORMANS_IN_SIEGE_BY_OWNERS = altSettings.getProperty("AllowUseDoormansInSiegeByOwners", true);
		NPC_RANDOM_ENCHANT = altSettings.getProperty("NpcRandomEnchant", false);
		ENABLE_PARTY_SEARCH = altSettings.getProperty("AllowPartySearch", false);
		MENTOR_ONLY_PA = altSettings.getProperty("MentorServiceOnlyForPremium", false);
		ALT_SHOW_MONSTERS_AGRESSION = altSettings.getProperty("AltShowMonstersAgression", false);
		ALT_SHOW_MONSTERS_LVL = altSettings.getProperty("AltShowMonstersLvL", false);
		ALT_TELEPORT_TO_TOWN_DURING_SIEGE = altSettings.getProperty("ALT_TELEPORT_TO_TOWN_DURING_SIEGE", true);
		ALT_CLAN_LEAVE_PENALTY_TIME = altSettings.getProperty("ALT_CLAN_LEAVE_PENALTY_TIME", 24);
		ALT_CLAN_CREATE_PENALTY_TIME = altSettings.getProperty("ALT_CLAN_CREATE_PENALTY_TIME", 240);
		ALT_EXPELLED_MEMBER_PENALTY_TIME = altSettings.getProperty("ALT_EXPELLED_MEMBER_PENALTY_TIME", 24);
		ALT_LEAVED_ALLY_PENALTY_TIME = altSettings.getProperty("ALT_LEAVED_ALLY_PENALTY_TIME", 24);
		ALT_DISSOLVED_ALLY_PENALTY_TIME = altSettings.getProperty("ALT_DISSOLVED_ALLY_PENALTY_TIME", 24);
		MIN_RAID_LEVEL_TO_DROP = altSettings.getProperty("MinRaidLevelToDropItem", 0);
		RAID_DROP_GLOBAL_ITEMS = altSettings.getProperty("AltEnableGlobalRaidDrop", false);
		final String[] infos = altSettings.getProperty("RaidGlobalDrop", new String[0], ";");
		for(final String info : infos)
			if(!info.isEmpty())
			{
				final String[] data = info.split(",");
				final int id2 = Integer.parseInt(data[0]);
				final long count = Long.parseLong(data[1]);
				final double chance = Double.parseDouble(data[2]);
				RAID_GLOBAL_DROP.add(new RaidGlobalDrop(id2, count, chance));
			}
		NPC_DIALOG_PLAYER_DELAY = altSettings.getProperty("NpcDialogPlayerDelay", 0);
		CLAN_DELETE_TIME = altSettings.getProperty("CLAN_DELETE_TIME", "0 5 * * 2");
		CLAN_CHANGE_LEADER_TIME = altSettings.getProperty("CLAN_CHANGE_LEADER_TIME", "0 5 * * 2");
		CLAN_CHANGE_LEADER_TIME_SECOND = Math.max(5, altSettings.getProperty("CLAN_CHANGE_LEADER_TIME_SECOND", 5));

		CLAN_MAX_LEVEL = altSettings.getProperty("CLAN_MAX_LEVEL", 11);
		CLAN_LVL_UP_SP_COST = new int[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_SP_COST.length; ++i)
			CLAN_LVL_UP_SP_COST[i] = altSettings.getProperty("CLAN_LVL_UP_SP_COST_" + i, 0);
		CLAN_LVL_UP_RP_COST = new int[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_RP_COST.length; ++i)
			CLAN_LVL_UP_RP_COST[i] = altSettings.getProperty("CLAN_LVL_UP_RP_COST_" + i, 0);
		CLAN_LVL_UP_MIN_MEMBERS = new int[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_MIN_MEMBERS.length; ++i)
			CLAN_LVL_UP_MIN_MEMBERS[i] = altSettings.getProperty("CLAN_LVL_UP_MIN_MEMBERS_" + i, 1);
		CLAN_LVL_UP_ITEMS_REQUIRED = new long[CLAN_MAX_LEVEL + 1][][][];
		for(int i = 1; i < CLAN_LVL_UP_ITEMS_REQUIRED.length; ++i)
		{
			final String[] itemsByLvlVariations = altSettings.getProperty("CLAN_LVL_UP_ITEMS_REQUIRED_" + i, "0-0").split("\\|");
			CLAN_LVL_UP_ITEMS_REQUIRED[i] = new long[itemsByLvlVariations.length][][];
			for(int j = 0; j < itemsByLvlVariations.length; ++j)
				CLAN_LVL_UP_ITEMS_REQUIRED[i][j] = StringArrayUtils.stringToLong2X(itemsByLvlVariations[j], ";", "-");
		}
		CLAN_LVL_UP_NEED_CASTLE = new boolean[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_NEED_CASTLE.length; ++i)
			CLAN_LVL_UP_NEED_CASTLE[i] = altSettings.getProperty("CLAN_LVL_UP_NEED_CASTLE_" + i, false);
		REFLECT_DAMAGE_CAPPED_BY_PDEF = altSettings.getProperty("ReflectDamageCappedByPDef", false);
		MIN_SAVEVS_REDUCTION = altSettings.getProperty("MinSaveVSReduction", 2.0);
		MAX_SAVEVS_REDUCTION = altSettings.getProperty("MaxSaveVSReduction", 0.0);
		ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL = altSettings.getProperty("ALT_DELEVEL_ON_DEATH_PENALTY_MIN_LEVEL", 10);
		ALT_PETS_NOT_STARVING = altSettings.getProperty("ALT_PETS_NOT_STARVING", false);
		SHOW_TARGET_EFFECTS = altSettings.getProperty("SHOW_TARGET_EFFECTS", false);
		PERCENT_LOST_ON_DEATH = new double[Experience.LEVEL.length];
		double prevPercentLost = 0.0;
		for(int k = 1; k < PERCENT_LOST_ON_DEATH.length; ++k)
		{
			final double percent = altSettings.getProperty("PERCENT_LOST_ON_DEATH_LVL_" + k, prevPercentLost);
			PERCENT_LOST_ON_DEATH[k] = percent;
			if(percent != prevPercentLost)
				prevPercentLost = percent;
		}
		PERCENT_LOST_ON_DEATH_MOD_IN_PEACE_ZONE = altSettings.getProperty("PERCENT_LOST_ON_DEATH_MOD_IN_PEACE_ZONE", 0.0);
		PERCENT_LOST_ON_DEATH_MOD_IN_PVP = altSettings.getProperty("PERCENT_LOST_ON_DEATH_MOD_IN_PVP", 1.0);
		PERCENT_LOST_ON_DEATH_MOD_IN_WAR = altSettings.getProperty("PERCENT_LOST_ON_DEATH_MOD_IN_WAR", 0.25);
		PERCENT_LOST_ON_DEATH_MOD_FOR_PK = altSettings.getProperty("PERCENT_LOST_ON_DEATH_MOD_FOR_PK", 1.0);
		ALT_EASY_RECIPES = altSettings.getProperty("EasyRecipiesExtraFeature", false);
		ALT_USE_TRANSFORM_IN_EPIC_ZONE = altSettings.getProperty("ALT_USE_TRANSFORM_IN_EPIC_ZONE", true);
		ALT_ANNONCE_RAID_BOSSES_REVIVAL = altSettings.getProperty("ALT_ANNONCE_RAID_BOSSES_REVIVAL", false);
		ALT_DEBUG_ENABLED = altSettings.getProperty("AltDebugEnabled", false);
		ALT_DEBUG_GM_ONLY = altSettings.getProperty("AltDebugGMOnly", false);
	}

	public static void loadServicesSettings() {
        final PropertiesParser servicesSettings = load("config/services.properties");
        SPAWN_VITAMIN_MANAGER = servicesSettings.getProperty("SPAWN_VITAMIN_MANAGER", true);
        ALLOW_CLASS_MASTERS_LIST.clear();
        final String allowClassMasters = servicesSettings.getProperty("AllowClassMasters", "false");
        if (!"false".equalsIgnoreCase(allowClassMasters)) {
            final String[] allowClassLvls = allowClassMasters.split(";");
            for (final String allowClassLvl : allowClassLvls) {
                final String[] allosClassLvlInfo = allowClassLvl.split(",");
                final int classLvl = Integer.parseInt(allosClassLvlInfo[0]);
                if (!ALLOW_CLASS_MASTERS_LIST.containsKey(classLvl)) {
                    int[] needItemInfo = {0, 0};
                    if (allosClassLvlInfo.length >= 3)
                        needItemInfo = new int[]{Integer.parseInt(allosClassLvlInfo[1]), Integer.parseInt(allosClassLvlInfo[2])};
                    ALLOW_CLASS_MASTERS_LIST.put(classLvl, needItemInfo);
                }
            }
        }

        SERVICES_CLAN_REPUTATION_ENABLE = servicesSettings.getProperty("ClanReputationSellEnable", false);
        SERVICES_CLAN_REPUTATION_ITEM_ID = servicesSettings.getProperty("ClanReputationItemId", 4037);
        SERVICES_CLAN_REPUTATION_ITEM_COUNT = servicesSettings.getProperty("ClanReputationItemCount", 1000);
        SERVICES_CLAN_REPUTATION_AMOUNT = servicesSettings.getProperty("ClanReputationAmountAdd", 10);
        SERVICES_CLANSKILL_SELL_ENABLED = servicesSettings.getProperty("ClanSkillsSellEnable", false);
        SERVICES_CLAN_SKILL_SELL_ITEM = servicesSettings.getProperty("ClanSkillsSellItemId", 4037);
        SERVICES_CLAN_SKILL_SELL_PRICE = servicesSettings.getProperty("ClanSkillsSellItemCount", 1000);
        SERVICES_CLANSKIL_SELL_MIN_LEVEL = servicesSettings.getProperty("ClanSkillMinLevel", 8);
        SERVICES_CLANLEVEL_SELL_ENABLED = servicesSettings.getProperty("ClanLevelSellEnabled", false);
        SERVICES_CLANLEVEL_SELL_ITEM = servicesSettings.getProperty("ClanLevelSellItem", new int[]{4037, 4037, 4037, 4037, 4037, 4037, 4037});
        SERVICES_CLANLEVEL_SELL_PRICE = servicesSettings.getProperty("ClanLevelSellPrice", new long[]{100L, 200L, 300L, 400L, 500L, 600L, 700L});
        SERVICES_CLAN_MAX_SELL_LEVEL = servicesSettings.getProperty("ClanMaxLevelForSell", 5);
        SERVICES_DELEVEL_SELL_ENABLED = servicesSettings.getProperty("DelevelSellEnabled", false);
        SERVICES_DELEVEL_SELL_PRICE = servicesSettings.getProperty("DelevelSellPrice", 1000);
        SERVICES_DELEVEL_SELL_ITEM = servicesSettings.getProperty("DelevelSellItem", 4037);
        SERVICES_KARMA_CLEAN_ENABLED = servicesSettings.getProperty("KarmaCleanServiceEnabled", false);
        SERVICES_KARMA_CLEAN_SELL_ITEM = servicesSettings.getProperty("KarmaCleanItemId", 4037);
        SERVICES_KARMA_CLEAN_SELL_PRICE = servicesSettings.getProperty("KarmaCleanPrice", 1000);
        SERVICES_PK_CLEAN_ENABLED = servicesSettings.getProperty("PKCleanEnabled", false);
        SERVICES_PK_CLEAN_SELL_ITEM = servicesSettings.getProperty("PKCleanItemId", 4037);
        SERVICES_PK_CLEAN_SELL_PRICE = servicesSettings.getProperty("PKCleanPrice", 1000);
        CUSTOM_CNAME_TEMPLATE = servicesSettings.getProperty("NickNameCustomTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");

        SERVICES_CHANGE_NICK_ENABLED = servicesSettings.getProperty("NickChangeEnabled", false);
        SERVICES_CHANGE_NICK_PRICE = servicesSettings.getProperty("NickChangePrice", 100);
        SERVICES_CHANGE_NICK_ITEM = servicesSettings.getProperty("NickChangeItem", 4037);
        SERVICES_CHANGE_PASSWORD = servicesSettings.getProperty("ChangePassword", true);
        SERVICES_CHANGE_CLAN_NAME_ENABLED = servicesSettings.getProperty("ClanNameChangeEnabled", false);
        SERVICES_CHANGE_CLAN_NAME_PRICE = servicesSettings.getProperty("ClanNameChangePrice", 100);
        SERVICES_CHANGE_CLAN_NAME_ITEM = servicesSettings.getProperty("ClanNameChangeItem", 4037);
        ALLOW_TOTAL_ONLINE = servicesSettings.getProperty("AllowVoiceCommandOnline", false);
        FIRST_UPDATE = servicesSettings.getProperty("FirstOnlineUpdate", 1);
        DELAY_UPDATE = servicesSettings.getProperty("OnlineUpdate", 5);
        SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
        SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
        SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);
        SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesSettings.getProperty("BabyPetExchangeEnabled", false);
        SERVICES_EXCHANGE_BABY_PET_PRICE = servicesSettings.getProperty("BabyPetExchangePrice", 100);
        SERVICES_EXCHANGE_BABY_PET_ITEM = servicesSettings.getProperty("BabyPetExchangeItem", 4037);
        SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
        SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
        SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);
        SERVICES_CHANGE_BASE_ENABLED = servicesSettings.getProperty("BaseChangeEnabled", false);
        SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
        SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 4037);
        SERVICES_SEPARATE_SUB_ENABLED = servicesSettings.getProperty("SeparateSubEnabled", false);
        SERVICES_SEPARATE_SUB_PRICE = servicesSettings.getProperty("SeparateSubPrice", 100);
        SERVICES_SEPARATE_SUB_ITEM = servicesSettings.getProperty("SeparateSubItem", 4037);
        SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
        SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
        SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
        SERVICES_CHANGE_NICK_COLOR_LIST = servicesSettings.getProperty("NickColorChangeList", new String[]{"00FF00"});
        SERVICES_CHANGE_TITLE_COLOR_ENABLED = servicesSettings.getProperty("TitleColorChangeEnabled", false);
        SERVICES_CHANGE_TITLE_COLOR_PRICE = servicesSettings.getProperty("TitleColorChangePrice", 100);
        SERVICES_CHANGE_TITLE_COLOR_ITEM = servicesSettings.getProperty("TitleColorChangeItem", 4037);
        SERVICES_CHANGE_TITLE_COLOR_LIST = servicesSettings.getProperty("TitleColorChangeList", new String[]{"00FF00"});
        SERVICES_BASH_ENABLED = servicesSettings.getProperty("BashEnabled", false);
        SERVICES_BASH_SKIP_DOWNLOAD = servicesSettings.getProperty("BashSkipDownload", false);
        SERVICES_BASH_RELOAD_TIME = servicesSettings.getProperty("BashReloadTime", 24);
        SERVICES_NOBLESS_SELL_ENABLED = servicesSettings.getProperty("NoblessSellEnabled", false);
        SERVICES_NOBLESS_SELL_PRICE = servicesSettings.getProperty("NoblessSellPrice", 1000);
        SERVICES_NOBLESS_SELL_ITEM = servicesSettings.getProperty("NoblessSellItem", 4037);
        SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
        SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
        SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
        SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);
        SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesSettings.getProperty("ExpandWarehouseEnabled", false);
        SERVICES_EXPAND_WAREHOUSE_PRICE = servicesSettings.getProperty("ExpandWarehousePrice", 1000);
        SERVICES_EXPAND_WAREHOUSE_ITEM = servicesSettings.getProperty("ExpandWarehouseItem", 4037);
        SERVICES_EXPAND_CWH_ENABLED = servicesSettings.getProperty("ExpandCWHEnabled", false);
        SERVICES_EXPAND_CWH_PRICE = servicesSettings.getProperty("ExpandCWHPrice", 1000);
        SERVICES_EXPAND_CWH_ITEM = servicesSettings.getProperty("ExpandCWHItem", 4037);
        SERVICES_OFFLINE_TRADE_ALLOW = servicesSettings.getProperty("AllowOfflineTrade", false);
        SERVICES_OFFLINE_TRADE_ALLOW_ZONE = servicesSettings.getProperty("AllowOfflineTradeZone", 0);
        SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesSettings.getProperty("OfflineMinLevel", 0);
        SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesSettings.getProperty("OfflineTradeNameColor", "B0FFFF"));

        String[] array = servicesSettings.getProperty("OfflineTradeAbnormalEffect", "NONE").split(";");
        SERVICES_OFFLINE_TRADE_ABNORMAL_EFFECT = new AbnormalEffect[array.length];

        for (int i = 0; i < array.length; i++)
            SERVICES_OFFLINE_TRADE_ABNORMAL_EFFECT[i] = AbnormalEffect.valueOf(array[i].toUpperCase());

        SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesSettings.getProperty("OfflineTradePriceItem", 0);
        SERVICES_OFFLINE_TRADE_PRICE = servicesSettings.getProperty("OfflineTradePrice", 0);
        SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesSettings.getProperty("OfflineTradeDaysToKick", 14) * 86400;
        SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesSettings.getProperty("OfflineRestoreAfterRestart", true);
        SERVICES_NO_TRADE_ONLY_OFFLINE = servicesSettings.getProperty("NoTradeOnlyOffline", false);
        SERVICES_TRADE_TAX = servicesSettings.getProperty("TradeTax", 0.0);
        SERVICES_OFFSHORE_TRADE_TAX = servicesSettings.getProperty("OffshoreTradeTax", 0.0);
        SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesSettings.getProperty("TradeTaxOnlyOffline", false);
        SERVICES_OFFSHORE_NO_CASTLE_TAX = servicesSettings.getProperty("NoCastleTaxInOffshore", false);
        SERVICES_TRADE_ONLY_FAR = servicesSettings.getProperty("TradeOnlyFar", false);
        SERVICES_TRADE_MIN_LEVEL = servicesSettings.getProperty("MinLevelForTrade", 0);
        SERVICES_TRADE_RADIUS = servicesSettings.getProperty("TradeRadius", 30);
        SERVICES_GIRAN_HARBOR_ENABLED = servicesSettings.getProperty("GiranHarborZone", false);
        SERVICES_PARNASSUS_ENABLED = servicesSettings.getProperty("ParnassusZone", false);
        SERVICES_PARNASSUS_NOTAX = servicesSettings.getProperty("ParnassusNoTax", false);
        SERVICES_PARNASSUS_PRICE = servicesSettings.getProperty("ParnassusPrice", 500000);
        SERVICES_ALLOW_LOTTERY = servicesSettings.getProperty("AllowLottery", false);
        SERVICES_LOTTERY_PRIZE = servicesSettings.getProperty("LotteryPrize", 50000);
        SERVICES_ALT_LOTTERY_PRICE = servicesSettings.getProperty("AltLotteryPrice", 2000);
        SERVICES_LOTTERY_TICKET_PRICE = servicesSettings.getProperty("LotteryTicketPrice", 2000);
        SERVICES_LOTTERY_5_NUMBER_RATE = servicesSettings.getProperty("Lottery5NumberRate", 0.6);
        SERVICES_LOTTERY_4_NUMBER_RATE = servicesSettings.getProperty("Lottery4NumberRate", 0.4);
        SERVICES_LOTTERY_3_NUMBER_RATE = servicesSettings.getProperty("Lottery3NumberRate", 0.2);
        SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesSettings.getProperty("Lottery2and1NumberPrize", 200);
        SERVICES_ALLOW_ROULETTE = servicesSettings.getProperty("AllowRoulette", false);
        SERVICES_ROULETTE_MIN_BET = servicesSettings.getProperty("RouletteMinBet", 1L);
        SERVICES_ROULETTE_MAX_BET = servicesSettings.getProperty("RouletteMaxBet", Long.MAX_VALUE);
        SERVICES_ENABLE_NO_CARRIER = servicesSettings.getProperty("EnableNoCarrier", false);
        SERVICES_NO_CARRIER_MIN_TIME = servicesSettings.getProperty("NoCarrierMinTime", 0);
        SERVICES_NO_CARRIER_MAX_TIME = servicesSettings.getProperty("NoCarrierMaxTime", 90);
        SERVICES_NO_CARRIER_DEFAULT_TIME = servicesSettings.getProperty("NoCarrierDefaultTime", 60);
        ALLOW_EVENT_GATEKEEPER = servicesSettings.getProperty("AllowEventGatekeeper", false);
        SERVICES_ENCHANT_VALUE = servicesSettings.getProperty("EnchantValue", new int[]{0});
        SERVICES_ENCHANT_COAST = servicesSettings.getProperty("EnchantCoast", new int[]{0});
        SERVICES_ENCHANT_RAID_VALUE = servicesSettings.getProperty("EnchantRaidValue", new int[]{0});
        SERVICES_ENCHANT_RAID_COAST = servicesSettings.getProperty("EnchantRaidCoast", new int[]{0});
        ALLOW_IP_LOCK = servicesSettings.getProperty("AllowLockIP", false);
        ALLOW_HWID_LOCK = servicesSettings.getProperty("AllowLockHwid", false);
        HWID_LOCK_MASK = servicesSettings.getProperty("HwidLockMask", 10);
        SERVICES_RIDE_HIRE_ENABLED = servicesSettings.getProperty("SERVICES_RIDE_HIRE_ENABLED", false);
        ALLOW_AWAY_STATUS = servicesSettings.getProperty("AllowAwayStatus", false);
        AWAY_ONLY_FOR_PREMIUM = servicesSettings.getProperty("AwayOnlyForPremium", true);
        AWAY_PLAYER_TAKE_AGGRO = servicesSettings.getProperty("AwayPlayerTakeAggro", false);
        AWAY_TITLE_COLOR = Integer.decode("0x" + servicesSettings.getProperty("AwayTitleColor", "0000FF"));
        AWAY_TIMER = servicesSettings.getProperty("AwayTimer", 30);
        BACK_TIMER = servicesSettings.getProperty("BackTimer", 30);
        AWAY_PEACE_ZONE = servicesSettings.getProperty("AwayOnlyInPeaceZone", false);

        ACP_ENABLED = servicesSettings.getProperty("AcpEnabled", false);
        ACP_ONLY_PREMIUM = servicesSettings.getProperty("AcpPremiumOnly", false);
        ACP_CERTIFICATES_FOR_USE = servicesSettings.getProperty("CertificateId", new int[]{});
        ACP_RESTRICT_WITH_EFFECTS = servicesSettings.getProperty("RestrictedEffects", new int[]{}, ",");
        ACP_POTIONS_CP = servicesSettings.getProperty("PotionsCP", new int[]{});
        ACP_POTIONS_HP = servicesSettings.getProperty("PotionsHP", new int[]{});
        ACP_POTIONS_MP = servicesSettings.getProperty("PotionsMP", new int[]{});

		ENABLE_OBT_COMMAND = servicesSettings.getProperty("EnableObtCommand", false);
    }

	public static void loadPvPSettings()
	{
		final PropertiesParser pvpSettings = load("config/pvp.properties");
		KARMA_MIN_KARMA_PC = pvpSettings.getProperty("MinKarmaPc", 720);
		KARMA_MIN_KARMA_PET = pvpSettings.getProperty("MinKarmaPet", 120);
		KARMA_RATE_KARMA_LOST = pvpSettings.getProperty("RateKarmaLost", -1);
		KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 1200);
		KARMA_DROP_GM = pvpSettings.getProperty("CanGMDropEquipment", false);
		KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
		DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", true);
		DROP_ITEMS_AUGMENTED = pvpSettings.getProperty("DropAugmented", false);
		KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
		MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 4);
		KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);
		KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20.0);
		KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKsDropMod", 1.0);
		NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1.0);
		DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
		DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquippment", 17);
		DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);
		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
		for(final int id : pvpSettings.getProperty("ListOfNonDroppableItems", new int[] {
				57,
				1147,
				425,
				1146,
				461,
				10,
				2368,
				7,
				6,
				2370,
				2369,
				3500,
				3501,
				3502,
				4422,
				4423,
				4424,
				2375,
				6648,
				6649,
				6650,
				6842,
				6834,
				6835,
				6836,
				6837,
				6838,
				6839,
				6840,
				5575,
				7694,
				6841,
				8181 }))
			KARMA_LIST_NONDROPPABLE_ITEMS.add(id);
		PVP_TIME = pvpSettings.getProperty("PvPTime", 40000);
		RATE_KARMA_LOST_STATIC = pvpSettings.getProperty("KarmaLostStaticValue", -1);
	}

	public static void loadAISettings()
	{
		final PropertiesParser aiSettings = load("config/ai.properties");
		AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
		AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
		AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
		BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
		ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);
		RND_WALK = aiSettings.getProperty("RndWalk", true);
		RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
		RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);
		AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
		NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);
		NONPVP_TIME_ONTELEPORT = aiSettings.getProperty("NonPvPTimeOnTeleport", 0);
		MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
		MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
		MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
		MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
	}

	public static int PLAYABLE_PATH_FIND_MAX_HEIGHT;
	public static int NPC_PATH_FIND_MAX_HEIGHT;
	
	public static void loadGeodataSettings()
	{
		final PropertiesParser geodataSettings = load("config/geodata.properties");
		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);
		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);
		try
		{
			GEODATA_ROOT = new File(geodataSettings.getProperty("GeodataRoot", "./geodata/")).getCanonicalFile();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}
		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		CLIENT_Z_SHIFT = geodataSettings.getProperty("ClientZShift", 16);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATHFIND_MAP_MUL = geodataSettings.getProperty("PathFindMapMul", 2);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		REGION_EDGE_MAX_Z_DIFF = geodataSettings.getProperty("RegionEdgeMaxZDiff", 128);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
		
	    NPC_PATH_FIND_MAX_HEIGHT = geodataSettings.getProperty("NpcPathFindMaxHeight", 1024);
	    PLAYABLE_PATH_FIND_MAX_HEIGHT = geodataSettings.getProperty("PlayablePathFindMaxHeight", 256);
	}

	public static void pvpManagerSettings()
	{
		final PropertiesParser pvp_manager = load("config/pvp_manager.properties");
		ALLOW_PVP_REWARD = pvp_manager.getProperty("AllowPvPManager", true);
		PVP_REWARD_SEND_SUCC_NOTIF = pvp_manager.getProperty("SendNotification", true);
		PVP_REWARD_REWARD_IDS = pvp_manager.getProperty("PvPRewardsIDs", new int[] { 57, 6673 });
		PVP_REWARD_COUNTS = pvp_manager.getProperty("PvPRewardsCounts", new long[] { 1L, 2L });
		if(PVP_REWARD_REWARD_IDS.length != PVP_REWARD_COUNTS.length)
			_log.warn("pvp_manager.properties: PvPRewardsIDs array length != PvPRewardsCounts array length");
		PVP_REWARD_RANDOM_ONE = pvp_manager.getProperty("GiveJustOneRandom", true);
		PVP_REWARD_DELAY_ONE_KILL = pvp_manager.getProperty("DelayBetweenKillsOneCharSec", 60);
		PVP_REWARD_MIN_PL_PROFF = pvp_manager.getProperty("ToRewardMinProff", 0);
		PVP_REWARD_MIN_PL_UPTIME_MINUTE = pvp_manager.getProperty("ToRewardMinPlayerUptimeMinutes", 60);
		PVP_REWARD_MIN_PL_LEVEL = pvp_manager.getProperty("ToRewardMinPlayerLevel", 75);
		PVP_REWARD_PK_GIVE = pvp_manager.getProperty("RewardPK", false);
		PVP_REWARD_ON_EVENT_GIVE = pvp_manager.getProperty("ToRewardIfInEvent", false);
		PVP_REWARD_ONLY_BATTLE_ZONE = pvp_manager.getProperty("ToRewardOnlyIfInBattleZone", false);
		PVP_REWARD_ONLY_NOBLE_GIVE = pvp_manager.getProperty("ToRewardOnlyIfNoble", false);
		PVP_REWARD_SAME_PARTY_GIVE = pvp_manager.getProperty("ToRewardIfInSameParty", false);
		PVP_REWARD_SAME_CLAN_GIVE = pvp_manager.getProperty("ToRewardIfInSameClan", false);
		PVP_REWARD_SAME_ALLY_GIVE = pvp_manager.getProperty("ToRewardIfInSameAlly", false);
		PVP_REWARD_SAME_HWID_GIVE = pvp_manager.getProperty("ToRewardIfInSameHWID", false);
		PVP_REWARD_SAME_IP_GIVE = pvp_manager.getProperty("ToRewardIfInSameIP", false);
		PVP_REWARD_SPECIAL_ANTI_TWINK_TIMER = pvp_manager.getProperty("SpecialAntiTwinkCharCreateDelay", false);
		PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM = pvp_manager.getProperty("SpecialAntiTwinkDelayInHours", 24);
		PVP_REWARD_CHECK_EQUIP = pvp_manager.getProperty("EquipCheck", false);
		PVP_REWARD_WEAPON_GRADE_TO_CHECK = pvp_manager.getProperty("MinimumGradeToCheck", 0);
		DISALLOW_MSG_TO_PL = pvp_manager.getProperty("DoNotShowMessagesToPlayers", false);
	}


	// XXX fantom
	public static int FAKE_WALK_CHANCE;
	public static boolean ALLOW_PK_COUNT_PHANTOM;
	public static List <int[]> PHANTOM_REWARD_ITEMS;
	public static boolean ALLOW_DROP_PHANTOM;
	public static boolean ALLOW_PHANTOMS_PARTY;
	public static int ALLOW_PHANTOMS_PARTY_CHANCE;
	public static int PHANTOM_BUFF_TIME;
	public static List <int[]> PHANTOM_TOP_BUFF;
	public static List <int[]> PHANTOM_TOP_BUFF_MAGE;
	
	public static List <int[]> PHANTOM_OLY_BUFF;
	public static List <int[]> PHANTOM_OLY_BUFF_MAGE;
	public static int CHANCE_SET_COLOR_NAME_PHANTOM;
	public static List <int[]> PHANTOMS_ITEMS_ID;
	public static boolean EnablePlayerKiller;
	public static boolean EnablePhantomKiller;
	
	public static int FakeChanceTOTalkDead;
	public static int CHANCE_ADD_NOBLE_PHANTOM;
	public static int ADD_SUB_SKILL_PHANTOM_LEVEL;
	public static boolean ALLOW_PHANTOM_SETS;
	public static boolean ALLOW_PHANTOM_CHAT;
	public static int PHANTOM_CHAT_CHANSE;
	public static String PHANTOM_PLAYERS_AKK;
	
	public static int PHANTOM_PLAYERS_DELAY_SPAWN;
	public static int PHANTOM_PLAYERS_DELAY_CHECK_COUNT;
	
	public static int PHANTOM_PLAYERS_ENCHANT_WEAPON_ATT_MIN;
	public static int PHANTOM_PLAYERS_ENCHANT_WEAPON_ATT_MAX;
	public static int PHANTOM_PLAYERS_ENCHANT_WEAPON_ATT_CHANCE;
	
	public static int PHANTOM_PLAYERS_AUGMENTATION_CHANCE;
	public static int PHANTOM_PLAYERS_AUGMENTATION_GRADE;
	
	public static List <String> PHANTOM_PLAYERS_TITLE_CLOLORS = new ArrayList <String>();
	public static List <String> PHANTOM_PLAYERS_NAME_CLOLORS = new ArrayList <String>();
	
	public static int CHANCE_ADD_SUB_SKILL_PHANTOM;
	public static int CHANCE_SET_TITLE_PHANTOM;
	
	public static boolean SET_PHANTOM_BONUS;
	public static boolean ALL_PHANTOM_DROP_TO_THE_PLAYER;
	
	public static int PHANTOM_AI_DELAY;
	public static int CHANCE_PHANTOM_SET_CLAN;
	public static int LEVEL_PHANTOM_SET_CLAN;
	public static int PHANTOM_SKILL_ENCHANT_ITEM_ID;
	public static int PHANTOM_SKILL_ENCHANT_ITEM_COUNT;
	
	public static int PHANTOM_ENHANCEMENT_ITEM_ID;
	public static int PHANTOM_ENHANCEMENT_SET_6_COUNT;
	public static int PHANTOM_ENHANCEMENT_SET_8_COUNT;
	public static int PHANTOM_ENHANCEMENT_WEAPON_4_COUNT;
	public static int PHANTOM_ENHANCEMENT_WEAPON_6_COUNT;
	public static int PHANTOM_ENHANCEMENT_WEAPON_8_COUNT;
	public static int PHANTOM_ENHANCEMENT_WEAPON_10_COUNT;
	
	public static int PHANTOM_ATTRIBUTE_IMPROVEMENT_ITEM_ID;
	public static int PHANTOM_ATTRIBUTE_SET_60_COUNT;
	public static int PHANTOM_ATTRIBUTE_SET_120_COUNT;
	public static int PHANTOM_ATTRIBUTE_WEAPON_150_COUNT;
	public static int PHANTOM_ATTRIBUTE_WEAPON_300_COUNT;
	
	public static int PHANTOM_ARMOR_IMPROVEMENT_ITEM_ID;
	public static int PHANTOM_RENT_EXTEND_ITEM_COUNT;
	public static int PHANTOM_RENT_EXTEND_ITEM_ID;
	
	public static int PHANTOM_SET_PRICE_C;
	public static int PHANTOM_SET_PRICE_B;
	public static int PHANTOM_SET_PRICE_A;
	public static int PHANTOM_SET_PRICE_S;
	public static int PHANTOM_SET_PRICE_S80;
	public static int PHANTOM_SET_PRICE_S84;
	
	public static int PHANTOM_WEAPON_PRICE_C;
	public static int PHANTOM_WEAPON_PRICE_B;
	public static int PHANTOM_WEAPON_PRICE_A;
	public static int PHANTOM_WEAPON_PRICE_S;
	public static int PHANTOM_WEAPON_PRICE_S80;
	public static int PHANTOM_WEAPON_PRICE_S84;
	
	public static int PHANTOM_BIJOUTERIE_PRICE_C;
	public static int PHANTOM_BIJOUTERIE_PRICE_B;
	public static int PHANTOM_BIJOUTERIE_PRICE_A;
	public static int PHANTOM_BIJOUTERIE_PRICE_S;
	public static int PHANTOM_BIJOUTERIE_PRICE_S80;
	public static int PHANTOM_BIJOUTERIE_PRICE_S84;
	
	public static int[] NG;
	public static int[] Profa1;
	public static int[] Profa2;
	public static int[] Profa3;
	public static int[] Crafter;
	public static double CHANCE_PHANTOM_PK;
	public static double CHANCE_PHANTOM_PVP;
	
	public static List <ConfigLevelGroup> PHANTOM_PLAYERS_ENCHANT_accessory = new ArrayList <ConfigLevelGroup>();
	public static List <ConfigLevelGroup> PHANTOM_PLAYERS_ENCHANT_ARMOR = new ArrayList <ConfigLevelGroup>();
	public static List <ConfigLevelGroup> PHANTOM_PLAYERS_ENCHANT_WEAPON = new ArrayList <ConfigLevelGroup>();
	public static List <ConfigLevelGroup> PHANTOM_PLAYERS_ENCHANT_UNDERWEAR = new ArrayList <ConfigLevelGroup>();
	public static List <ConfigLevelGroup> PHANTOM_PLAYERS_DESPAWN_DELAY = new ArrayList <ConfigLevelGroup>();
	
	public static String[] Phantom_locked_zones=
		{"[mage_start_peace_1]"
				,"[mage_start_peace_2]"
				,"[mage_start_peace_3]"
				,"[mage_start_peace_4]"
				,"[mage_start_peace_5]"
				,"[fighter_start_peace_1]"
				,"[fighter_start_peace_2]"
				,"[fighter_start_peace_3]"
				,"[fighter_start_peace_4]"
				,"[dwarf_start]"
				,"[orc_start_peace]"
				,"[darkelf_start_peace_1]"
				,"[darkelf_start_peace_2]"
				,"[elf_start_peace1]"
				,"[elf_start_peace2]"
				,"[elf_start_peace3]"
				,"[kamael_start]"};
	
	public static final String PHANTOM_FILE = "config/Phantom/Phantoms.properties";

	public static boolean RECORDING_ROUTE;
	public static boolean PHANTOM_SPAWN_ENABLED;

	public static void loadPhantomsConfig()
	{
		final PropertiesParser PhantomsSettings = load(PHANTOM_FILE);

		PHANTOM_SPAWN_ENABLED = PhantomsSettings.getProperty("SpawnEnabled", false);
		RECORDING_ROUTE = PhantomsSettings.getProperty("RecordingRoute", false);

		NG = PhantomsSettings.getProperty("NG", ArrayUtils.EMPTY_INT_ARRAY,",");
		Profa1 = PhantomsSettings.getProperty("Profa1", ArrayUtils.EMPTY_INT_ARRAY,",");
		Profa2 = PhantomsSettings.getProperty("Profa2", ArrayUtils.EMPTY_INT_ARRAY,",");
		Profa3 = PhantomsSettings.getProperty("Profa3", ArrayUtils.EMPTY_INT_ARRAY,",");
		Crafter = PhantomsSettings.getProperty("Crafter", ArrayUtils.EMPTY_INT_ARRAY,",");
		
		String[] DespawnDelay = PhantomsSettings.getProperty("DespawnDelay", "1-19,5-10;20-39,5-10").split(";");
		String[] EnchantWeapon = PhantomsSettings.getProperty("EnchantWeapon", "1-19,5-10;20-39,5-10").split(";");
		String[] EnchantArmor = PhantomsSettings.getProperty("EnchantArmor", "1-19,5-10;20-39,5-10").split(";");
		String[] EnchantAccessory = PhantomsSettings.getProperty("EnchantAccessory", "1-19,5-10;20-39,5-10").split(";");
		String[] EnchantUnderwear = PhantomsSettings.getProperty("EnchantUnderwear", "1-19,5-10;20-39,5-10").split(";");
		
		for(String reward : DespawnDelay)
		{
			String[] rewardSplit = reward.split(",");
			try
			{
				String[] levelSplit = rewardSplit[0].split("-");
				String[] delaySplit = rewardSplit[1].split("-");
				PHANTOM_PLAYERS_DESPAWN_DELAY.add(new ConfigLevelGroup(Integer.parseInt(levelSplit[0]), Integer.parseInt(levelSplit[1]), (int) TimeUnit.MINUTES.toMillis(Integer.parseInt(delaySplit[0])), (int) TimeUnit.MINUTES.toMillis(Integer.parseInt(delaySplit[1]))));
				
			}catch(NumberFormatException ignored)
			{
				_log.error(" "+ignored);
			}
		}
		
		for(String reward : EnchantUnderwear)
		{
			String[] rewardSplit = reward.split(",");
			try
			{
				String[] levelSplit = rewardSplit[0].split("-");
				String[] enlevelSplit = rewardSplit[1].split("-");
				PHANTOM_PLAYERS_ENCHANT_UNDERWEAR.add(new ConfigLevelGroup(Integer.parseInt(levelSplit[0]), Integer.parseInt(levelSplit[1]), Integer.parseInt(enlevelSplit[0]), Integer.parseInt(enlevelSplit[1])));
				
			}catch(NumberFormatException ignored)
			{
				_log.error(" "+ignored);
			}
		}
		
		for(String reward : EnchantWeapon)
		{
			String[] rewardSplit = reward.split(",");
			try
			{
				String[] levelSplit = rewardSplit[0].split("-");
				String[] enlevelSplit = rewardSplit[1].split("-");
				PHANTOM_PLAYERS_ENCHANT_WEAPON.add(new ConfigLevelGroup(Integer.parseInt(levelSplit[0]), Integer.parseInt(levelSplit[1]), Integer.parseInt(enlevelSplit[0]), Integer.parseInt(enlevelSplit[1])));
				
			}catch(NumberFormatException ignored)
			{
				_log.error(" "+ignored);
			}
		}
		
		for(String reward : EnchantArmor)
		{
			String[] rewardSplit = reward.split(",");
			try
			{
				String[] levelSplit = rewardSplit[0].split("-");
				String[] enlevelSplit = rewardSplit[1].split("-");
				PHANTOM_PLAYERS_ENCHANT_ARMOR.add(new ConfigLevelGroup(Integer.parseInt(levelSplit[0]), Integer.parseInt(levelSplit[1]), Integer.parseInt(enlevelSplit[0]), Integer.parseInt(enlevelSplit[1])));
				
			}catch(NumberFormatException ignored)
			{
				_log.error(" "+ignored);
			}
		}
		for(String reward : EnchantAccessory)
		{
			String[] rewardSplit = reward.split(",");
			try
			{
				String[] levelSplit = rewardSplit[0].split("-");
				String[] enlevelSplit = rewardSplit[1].split("-");
				PHANTOM_PLAYERS_ENCHANT_accessory.add(new ConfigLevelGroup(Integer.parseInt(levelSplit[0]), Integer.parseInt(levelSplit[1]), Integer.parseInt(enlevelSplit[0]), Integer.parseInt(enlevelSplit[1])));
				
			}catch(NumberFormatException ignored)
			{
				_log.error(" "+ignored);
			}
		}
		
		CHANCE_PHANTOM_SET_CLAN = PhantomsSettings.getProperty("ChancePhantomsSetClan", 50);
		LEVEL_PHANTOM_SET_CLAN = PhantomsSettings.getProperty("LevelPhantomsSetClan", 50);

		ALL_PHANTOM_DROP_TO_THE_PLAYER = PhantomsSettings.getProperty("AllPhantomDropToPlayer", false);
		SET_PHANTOM_BONUS = PhantomsSettings.getProperty("SetPhantomBonus", false);
		
		ALLOW_PHANTOMS_PARTY = PhantomsSettings.getProperty("AllowPhantomsParty", false);
		ALLOW_PHANTOMS_PARTY_CHANCE = PhantomsSettings.getProperty("AllowPhantomsPartyChance", 50);
		
		ALLOW_PK_COUNT_PHANTOM = PhantomsSettings.getProperty("AllowPkCountPhantom", false);
		
		ALLOW_PHANTOM_SETS = PhantomsSettings.getProperty("AllowPhantomSets", false);
		ALLOW_PHANTOM_CHAT = PhantomsSettings.getProperty("AllowPhantomPlayersChat", false);
		PHANTOM_CHAT_CHANSE = PhantomsSettings.getProperty("PhantomPlayersChatChance", 1);
		
		PHANTOM_PLAYERS_AKK = PhantomsSettings.getProperty("PhantomPlayerAccounts", "rebellion");
		
		PHANTOM_PLAYERS_DELAY_SPAWN = PhantomsSettings.getProperty("DelaySpawn", 1);
		PHANTOM_PLAYERS_DELAY_CHECK_COUNT = (int) TimeUnit.MINUTES.toMillis(PhantomsSettings.getProperty("CheckCountDelay", 1));
		
		PHANTOM_PLAYERS_ENCHANT_WEAPON_ATT_CHANCE = PhantomsSettings.getProperty("FakeEnchantAttChance", 100);
		PHANTOM_PLAYERS_ENCHANT_WEAPON_ATT_MIN = PhantomsSettings.getProperty("FakeEnchantAttMin", 150);
		PHANTOM_PLAYERS_ENCHANT_WEAPON_ATT_MAX = PhantomsSettings.getProperty("FakeEnchantAttMax", 300);
		
		PHANTOM_PLAYERS_AUGMENTATION_CHANCE = PhantomsSettings.getProperty("FakeAugmentationChance", 90);
		PHANTOM_PLAYERS_AUGMENTATION_GRADE = PhantomsSettings.getProperty("FakeAugmentationGrade", 5);
		
		ADD_SUB_SKILL_PHANTOM_LEVEL = PhantomsSettings.getProperty("LevelAddSubSkill", 76);
		CHANCE_SET_TITLE_PHANTOM = PhantomsSettings.getProperty("ChanceSetRandomTitle", 100);
		
		CHANCE_SET_COLOR_NAME_PHANTOM = PhantomsSettings.getProperty("ChanceSetColorName", 100);
		
		CHANCE_ADD_SUB_SKILL_PHANTOM = PhantomsSettings.getProperty("ChanceAddSubSkill", 100);
		CHANCE_ADD_NOBLE_PHANTOM = PhantomsSettings.getProperty("ChanceAddNoblePhantom", 90);
		for(String prop : PhantomsSettings.getProperty("NameColors", "FFFF77,FFFF77").split(","))
			PHANTOM_PLAYERS_NAME_CLOLORS.add(prop);
		for(String prop : PhantomsSettings.getProperty("TitleColors", "FFFF77,FFFF77").split(","))
			PHANTOM_PLAYERS_TITLE_CLOLORS.add(prop);
		
		FAKE_WALK_CHANCE = PhantomsSettings.getProperty("FakeWalkChance", 6);
		
		FakeChanceTOTalkDead = PhantomsSettings.getProperty("FakeChanceToTalkDead", 6);
		
		EnablePlayerKiller = PhantomsSettings.getProperty("EnablePlayerKiller", false);
		EnablePhantomKiller = PhantomsSettings.getProperty("EnablePhantomKiller", false);
		
		PHANTOM_AI_DELAY = PhantomsSettings.getProperty("PhantomAiDelay", 6);
		
		PHANTOMS_ITEMS_ID = new ArrayList <>();
		String[] propertySplit = PhantomsSettings.getProperty("PhantomItemsId", "20034,3;20338,1;20344,1").split(";");
		for(String reward : propertySplit)
		{
			String[] rewardSplit = reward.split(",");
			if (rewardSplit.length != 2)
			{}
			else
			{
				try
				{
					PHANTOMS_ITEMS_ID.add(new int[]
					{Integer.parseInt(rewardSplit[0]),Integer.parseInt(rewardSplit[1])});
				}catch(NumberFormatException ignored)
				{
					
				}
			}
		}
		PHANTOM_OLY_BUFF = new ArrayList <>();
		String[] PhantomOlyBuffSplit = PhantomsSettings.getProperty("PhantomOlyBuff", "1062,2").split(";");
		for(String reward : PhantomOlyBuffSplit)
		{
			String[] rewardSplit = reward.split(",");
			if (rewardSplit.length != 2)
			{}
			else
			{
				try
				{
					PHANTOM_OLY_BUFF.add(new int[]
					{Integer.parseInt(rewardSplit[0]),Integer.parseInt(rewardSplit[1])});
				}catch(NumberFormatException ignored)
				{
					
				}
			}
		}
		
		PHANTOM_OLY_BUFF_MAGE = new ArrayList <>();
		String[] PhantomOlyBuffMage = PhantomsSettings.getProperty("PhantomOlyBuffMage", "1062,2").split(";");
		for(String reward : PhantomOlyBuffMage)
		{
			String[] rewardSplit = reward.split(",");
			if (rewardSplit.length != 2)
			{}
			else
			{
				try
				{
					PHANTOM_OLY_BUFF_MAGE.add(new int[]
					{Integer.parseInt(rewardSplit[0]),Integer.parseInt(rewardSplit[1])});
				}catch(NumberFormatException ignored)
				{
					
				}
			}
		}
		
		PHANTOM_TOP_BUFF = new ArrayList <>();
		String[] PhantomBuffSplit = PhantomsSettings.getProperty("PhantomBuff", "1062,2").split(";");
		for(String reward : PhantomBuffSplit)
		{
			String[] rewardSplit = reward.split(",");
			if (rewardSplit.length != 2)
			{}
			else
			{
				try
				{
					PHANTOM_TOP_BUFF.add(new int[]
					{Integer.parseInt(rewardSplit[0]),Integer.parseInt(rewardSplit[1])});
				}catch(NumberFormatException ignored)
				{
					
				}
			}
		}
		
		PHANTOM_TOP_BUFF_MAGE = new ArrayList <>();
		String[] PhantomBuffMage = PhantomsSettings.getProperty("PhantomBuffMage", "1062,2").split(";");
		for(String reward : PhantomBuffMage)
		{
			String[] rewardSplit = reward.split(",");
			if (rewardSplit.length != 2)
			{}
			else
			{
				try
				{
					PHANTOM_TOP_BUFF_MAGE.add(new int[]
					{Integer.parseInt(rewardSplit[0]),Integer.parseInt(rewardSplit[1])});
				}catch(NumberFormatException ignored)
				{
					
				}
			}
		}
		
		PHANTOM_BUFF_TIME = PhantomsSettings.getProperty("PhantomBuffTime", 100);
		ALLOW_DROP_PHANTOM = PhantomsSettings.getProperty("AllowDropPhantom", false);
		
		PHANTOM_REWARD_ITEMS = new ArrayList <>();
		String[] PhantomRewardItems = PhantomsSettings.getProperty("PhantomRewardItems", "57,2").split(";");
		for(String reward : PhantomRewardItems)
		{
			String[] rewardSplit = reward.split(",");
			if (rewardSplit.length != 2)
			{}
			else
			{
				try
				{
					PHANTOM_REWARD_ITEMS.add(new int[]
					{Integer.parseInt(rewardSplit[0]),Integer.parseInt(rewardSplit[1])});
				}catch(NumberFormatException ignored)
				{
					
				}
			}
		}
		PHANTOM_SKILL_ENCHANT_ITEM_ID = PhantomsSettings.getProperty("PhantomSkillEnchantItemId", 6);
		PHANTOM_SKILL_ENCHANT_ITEM_COUNT = PhantomsSettings.getProperty("PhantomSkillEnchantItemCount", 6);
		
		PHANTOM_ENHANCEMENT_ITEM_ID = PhantomsSettings.getProperty("PhantomEnhancementItemId", 6);
		PHANTOM_ENHANCEMENT_SET_6_COUNT = PhantomsSettings.getProperty("PhantomEnhancementSet6ItemCount", 6);
		PHANTOM_ENHANCEMENT_SET_8_COUNT = PhantomsSettings.getProperty("PhantomEnhancementSet8ItemCount", 6);
		PHANTOM_ENHANCEMENT_WEAPON_4_COUNT = PhantomsSettings.getProperty("PhantomEnhancementWeapon4ItemCount", 6);
		PHANTOM_ENHANCEMENT_WEAPON_6_COUNT = PhantomsSettings.getProperty("PhantomEnhancementWeapon6ItemCount", 6);
		PHANTOM_ENHANCEMENT_WEAPON_8_COUNT = PhantomsSettings.getProperty("PhantomEnhancementWeapon8ItemCount", 6);
		PHANTOM_ENHANCEMENT_WEAPON_10_COUNT = PhantomsSettings.getProperty("PhantomEnhancementWeapon10ItemCount", 6);
		
		PHANTOM_ATTRIBUTE_IMPROVEMENT_ITEM_ID = PhantomsSettings.getProperty("PhantomAttributeImprovementItemId", 6);
		
		PHANTOM_ATTRIBUTE_SET_60_COUNT = PhantomsSettings.getProperty("PhantomAttributeSet60ItemCount", 6);
		PHANTOM_ATTRIBUTE_SET_120_COUNT = PhantomsSettings.getProperty("PhantomAttributeSet120ItemCount", 6);
		PHANTOM_ATTRIBUTE_WEAPON_150_COUNT = PhantomsSettings.getProperty("PhantomAttributeWeapon150ItemCount", 6);
		PHANTOM_ATTRIBUTE_WEAPON_300_COUNT = PhantomsSettings.getProperty("PhantomAttributeWeapon300ItemCount", 6);
		
		PHANTOM_ARMOR_IMPROVEMENT_ITEM_ID = PhantomsSettings.getProperty("PhantomArmorImprovementItemId", 6);
		PHANTOM_RENT_EXTEND_ITEM_COUNT = PhantomsSettings.getProperty("PhantomRentExtenditemCount", 6);
		PHANTOM_RENT_EXTEND_ITEM_ID = PhantomsSettings.getProperty("PhantomRentExtenditemId", 6);
		
		PHANTOM_WEAPON_PRICE_C = PhantomsSettings.getProperty("PhantomWeaponPriceC", 6);
		PHANTOM_WEAPON_PRICE_B = PhantomsSettings.getProperty("PhantomWeaponPriceB", 6);
		PHANTOM_WEAPON_PRICE_A = PhantomsSettings.getProperty("PhantomWeaponPriceA", 6);
		PHANTOM_WEAPON_PRICE_S = PhantomsSettings.getProperty("PhantomWeaponPriceS", 6);
		PHANTOM_WEAPON_PRICE_S80 = PhantomsSettings.getProperty("PhantomWeaponPriceS80", 6);
		PHANTOM_WEAPON_PRICE_S84 = PhantomsSettings.getProperty("PhantomWeaponPriceS84", 6);
		
		PHANTOM_SET_PRICE_C = PhantomsSettings.getProperty("PhantomSetPriceC", 6);
		PHANTOM_SET_PRICE_B = PhantomsSettings.getProperty("PhantomSetPriceB", 6);
		PHANTOM_SET_PRICE_A = PhantomsSettings.getProperty("PhantomSetPriceA", 6);
		PHANTOM_SET_PRICE_S = PhantomsSettings.getProperty("PhantomSetPriceS", 6);
		PHANTOM_SET_PRICE_S80 = PhantomsSettings.getProperty("PhantomSetPriceS80", 6);
		PHANTOM_SET_PRICE_S84 = PhantomsSettings.getProperty("PhantomSetPriceS84", 6);
		
		PHANTOM_BIJOUTERIE_PRICE_C = PhantomsSettings.getProperty("PhantomBijouteriePriceC", 6);
		PHANTOM_BIJOUTERIE_PRICE_B = PhantomsSettings.getProperty("PhantomBijouteriePriceB", 6);
		PHANTOM_BIJOUTERIE_PRICE_A = PhantomsSettings.getProperty("PhantomBijouteriePriceA", 6);
		PHANTOM_BIJOUTERIE_PRICE_S = PhantomsSettings.getProperty("PhantomBijouteriePriceS", 6);
		PHANTOM_BIJOUTERIE_PRICE_S80 = PhantomsSettings.getProperty("PhantomBijouteriePriceS80", 6);
		PHANTOM_BIJOUTERIE_PRICE_S84 = PhantomsSettings.getProperty("PhantomBijouteriePriceS84", 6);
		
	}
	public static void load()
	{
		loadPhantomsConfig();
		loadServerConfig();
		loadTelnetConfig();
		loadResidenceConfig();
		loadAntiFloodConfig();
		loadCustomConfig();
		loadOtherConfig();
		loadSpoilConfig();
		loadFormulasConfig();
		loadAltSettings();
		loadServicesSettings();
		loadPvPSettings();
		loadOlympiadSettings();
		loadAISettings();
		loadGeodataSettings();
		loadExtSettings();
		loadBBSSettings();
		abuseLoad();
		loadGMAccess();
		pvpManagerSettings();
		loadAntiBotSettings();
		loadBossSettings();
		loadGveSettings();
		loadArtifact();
		loadBoostMorale();
		loadFactionWar();
		loadSubSkillsSettings();

		ConfigParsers.reload();
	}

	public static void abuseLoad()
	{
		final StringBuilder abuses = new StringBuilder();
		try(LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream("config/abusewords.txt"), "UTF-8")))
		{
			int count = 0;
			String line;
			while((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
				{
					abuses.append(st.nextToken());
					abuses.append("|");
					++count;
				}
			}
			if(count > 0)
			{
				String abusesGroup = abuses.toString();
				abusesGroup = abusesGroup.substring(0, abusesGroup.length() - 1);
				ABUSEWORD_PATTERN = Pattern.compile(".*(" + abusesGroup + ").*", 98);
			}
			_log.info("Abuse: Loaded " + count + " abuse words.");
		}
		catch(IOException e1)
		{
			_log.warn("Error reading abuse: " + e1);
		}
	}

	public static void loadAntiBotSettings()
	{
		final PropertiesParser botSettings = load("config/anti_bot_system.properties");
		ENABLE_ANTI_BOT_SYSTEM = botSettings.getProperty("EnableAntiBotSystem", false);
		MINIMUM_TIME_QUESTION_ASK = botSettings.getProperty("MinimumTimeQuestionAsk", 60);
		MAXIMUM_TIME_QUESTION_ASK = botSettings.getProperty("MaximumTimeQuestionAsk", 120);
		MINIMUM_BOT_POINTS_TO_STOP_ASKING = botSettings.getProperty("MinimumBotPointsToStopAsking", 10);
		MAXIMUM_BOT_POINTS_TO_STOP_ASKING = botSettings.getProperty("MaximumBotPointsToStopAsking", 15);
		MAX_BOT_POINTS = botSettings.getProperty("MaxBotPoints", 15);
		MINIMAL_BOT_RATING_TO_BAN = botSettings.getProperty("MinimalBotPointsToBan", -5);
		AUTO_BOT_BAN_JAIL_TIME = botSettings.getProperty("AutoBanJailTime", 24);
		ANNOUNCE_AUTO_BOT_BAN = botSettings.getProperty("AnounceAutoBan", true);
		ON_WRONG_QUESTION_KICK = botSettings.getProperty("IfWrongKick", true);
	}

	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static boolean CLASSED_GAMES_ENABLED;
	public static long OLYMPIAD_REGISTRATION_DELAY;
	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;
	public static SchedulingPattern OLYMIAD_END_PERIOD_TIME;
	public static SchedulingPattern OLYMPIAD_START_TIME;
	public static int OLYMPIAD_MIN_LEVEL;
	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int GAME_MAX_LIMIT;
	public static int GAME_CLASSES_COUNT_LIMIT;
	public static int GAME_NOCLASSES_COUNT_LIMIT;
	public static int ALT_OLY_REG_DISPLAY;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int OLYMPIAD_CLASSED_WINNER_REWARD_COUNT;
	public static int OLYMPIAD_NONCLASSED_WINNER_REWARD_COUNT;
	public static int OLYMPIAD_CLASSED_LOOSER_REWARD_COUNT;
	public static int OLYMPIAD_NONCLASSED_LOOSER_REWARD_COUNT;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int OLYMPIAD_ALL_LOOSE_POINTS_BONUS;
	public static int OLYMPIAD_1_OR_MORE_WIN_POINTS_BONUS;
	public static int OLYMPIAD_STADIAS_COUNT;
	public static int OLYMPIAD_BATTLES_FOR_REWARD;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static boolean OLYMPIAD_OLDSTYLE_STAT;
	public static int OLYMPIAD_BEGINIG_DELAY;
	public static int ALT_OLY_BY_SAME_BOX_NUMBER;
	public static boolean OLYMPIAD_ENABLE_ENCHANT_LIMIT;
	public static int OLYMPIAD_WEAPON_ENCHANT_LIMIT;
	public static int OLYMPIAD_ARMOR_ENCHANT_LIMIT;
	public static int OLYMPIAD_JEWEL_ENCHANT_LIMIT;

	public static void loadOlympiadSettings()
	{
		PropertiesParser olympSettings = Config.load("config/olympiad.properties");
		ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
		ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);
		OLYMIAD_END_PERIOD_TIME = new SchedulingPattern(olympSettings.getProperty("OLYMIAD_END_PERIOD_TIME", "00 00 01 * *"));
		OLYMPIAD_START_TIME = new SchedulingPattern(olympSettings.getProperty("OLYMPIAD_START_TIME", "00 20 * * 5,6"));
		ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 14400000);
		ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000);
		CLASSED_GAMES_ENABLED = olympSettings.getProperty("CLASSED_GAMES_ENABLED", false);
		OLYMPIAD_REGISTRATION_DELAY = olympSettings.getProperty("OLYMPIAD_REGISTRATION_DELAY", 1200000);
		OLYMPIAD_MIN_LEVEL = Math.max(40, olympSettings.getProperty("OLYMPIAD_MIN_LEVEL", 55));
		CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 10);
		NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 20);
		GAME_MAX_LIMIT = olympSettings.getProperty("GameMaxLimit", 30);
		GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClassesCountLimit", 30);
		GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClassesCountLimit", 30);
		ALT_OLY_REG_DISPLAY = olympSettings.getProperty("AltOlyRegistrationDisplayNumber", 100);
		ALT_OLY_BATTLE_REWARD_ITEM = olympSettings.getProperty("AltOlyBattleRewItem", 45584);
		OLYMPIAD_CLASSED_WINNER_REWARD_COUNT = olympSettings.getProperty("OLYMPIAD_CLASSED_WINNER_REWARD_COUNT", 0);
		OLYMPIAD_NONCLASSED_WINNER_REWARD_COUNT = olympSettings.getProperty("OLYMPIAD_NONCLASSED_WINNER_REWARD_COUNT", 0);
		OLYMPIAD_CLASSED_LOOSER_REWARD_COUNT = olympSettings.getProperty("OLYMPIAD_CLASSED_LOOSER_REWARD_COUNT", 0);
		OLYMPIAD_NONCLASSED_LOOSER_REWARD_COUNT = olympSettings.getProperty("OLYMPIAD_NONCLASSED_LOOSER_REWARD_COUNT", 0);
		ALT_OLY_COMP_RITEM = olympSettings.getProperty("AltOlyCompRewItem", 45584);
		ALT_OLY_GP_PER_POINT = olympSettings.getProperty("AltOlyGPPerPoint", 20);
		ALT_OLY_HERO_POINTS = olympSettings.getProperty("AltOlyHeroPoints", 100);
		ALT_OLY_RANK1_POINTS = olympSettings.getProperty("AltOlyRank1Points", 200);
		ALT_OLY_RANK2_POINTS = olympSettings.getProperty("AltOlyRank2Points", 80);
		ALT_OLY_RANK3_POINTS = olympSettings.getProperty("AltOlyRank3Points", 50);
		ALT_OLY_RANK4_POINTS = olympSettings.getProperty("AltOlyRank4Points", 30);
		ALT_OLY_RANK5_POINTS = olympSettings.getProperty("AltOlyRank5Points", 15);
		OLYMPIAD_ALL_LOOSE_POINTS_BONUS = olympSettings.getProperty("OLYMPIAD_ALL_LOOSE_POINTS_BONUS", 0);
		OLYMPIAD_1_OR_MORE_WIN_POINTS_BONUS = olympSettings.getProperty("OLYMPIAD_1_OR_MORE_WIN_POINTS_BONUS", 10);
		OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("OlympiadStadiasCount", 160);
		OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("OlympiadBattlesForReward", 10);
		OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 10);
		OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 10);
		OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OlympiadOldStyleStat", false);
		OLYMPIAD_BEGINIG_DELAY = olympSettings.getProperty("OlympiadBeginingDelay", 120);
		ALT_OLY_BY_SAME_BOX_NUMBER = olympSettings.getProperty("OlympiadSameBoxesNumberLimitation", 0);
		OLYMPIAD_ENABLE_ENCHANT_LIMIT = olympSettings.getProperty("ENABLE_ENCHANT_LIMIT", false);
		OLYMPIAD_WEAPON_ENCHANT_LIMIT = olympSettings.getProperty("WEAPON_ENCHANT_LIMIT", 0);
		OLYMPIAD_ARMOR_ENCHANT_LIMIT = olympSettings.getProperty("ARMOR_ENCHANT_LIMIT", 0);
		OLYMPIAD_JEWEL_ENCHANT_LIMIT = olympSettings.getProperty("JEWEL_ENCHANT_LIMIT", 0);
	}

	public static void loadArtifact()
	{
		final PropertiesParser properties = load(ARTIFACT_FILE);
		ARTIFACT_ENABLED = properties.getProperty("Enabled", false);
		MAX_ARTIFACTS_FOR_FACTION = properties.getProperty("MaxArtifactsForFaction", 4);
	}

	public static boolean CASTLE_SUCCESS_ATTACK_BOOST;
	public static boolean CASTLE_SUCCESS_DEFENSE_BOOST;
	public static boolean FORTRESS_SUCCESS_ATTACK_BOOST;
	public static boolean FORTRESS_SUCCESS_DEFENSE_BOOST;
	public static boolean OUTPOST_DESTROY_BOOST;
	public static boolean ARTIFACT_CAPTURE_BOOST;
	public static boolean BOSS_KILL_BOOST;
	public static boolean BOOST_MORALE_ENABLED;
	public static int[][] MORALE_EFFECT;

	public static void loadBoostMorale()
	{
		final PropertiesParser properties = load(BOOST_MORALE_FILE);
		BOOST_MORALE_ENABLED = properties.getProperty("Enabled", true);
		CASTLE_SUCCESS_ATTACK_BOOST = properties.getProperty("CastleSuccessAttackBoost", false);
		CASTLE_SUCCESS_DEFENSE_BOOST = properties.getProperty("CastleSuccessDefenseBoost", false);
		FORTRESS_SUCCESS_ATTACK_BOOST = properties.getProperty("FortressSuccessAttackBoost", false);
		FORTRESS_SUCCESS_DEFENSE_BOOST = properties.getProperty("FortressSuccessDefenseBoost", false);
		OUTPOST_DESTROY_BOOST = properties.getProperty("AvanpostDestroyBoost", false);
		ARTIFACT_CAPTURE_BOOST = properties.getProperty("ArtifactCaptureBoost", false);
		BOSS_KILL_BOOST  = properties.getProperty("BossKillBoost", false);
        MORALE_EFFECT = properties.getProperty("MoraleEffect", new int[0][]);
	}

	public static boolean FACTION_WAR_ENABLED;
	public static boolean FACTION_WAR_PVP_HWID_ENABLE;
	public static int FACTION_WAR_START_POINTS;
	public static SchedulingPattern FACTION_WAR_START_PERIOD_PATTERN ;
	public static int FACTION_WAR_MIN_LEVEL;
	public static int FACTION_WAR_PVP_KILL_POINTS;
	public static int FACTION_WAR_PVP_DEATH_POINTS;
	public static int FACTION_WAR_OUTPOST_KILL_POINTS;
	public static int FACTION_WAR_FORTRESS_SIEGE_ZONE_POINTS;
	public static int FACTION_WAR_CASTLE_SIEGE_ZONE_POINTS;
	public static int FACTION_WAR_ARTIFACT_CAPTURE_POINTS;
	public static int FACTION_WAR_EPIC_BOSS_KILL_POINTS;
	public static int FACTION_WAR_UPGRADING_ARTIFACT_CAPTURE_POINTS;

	public static int[] FACTION_WAR_TRUST_LEVEL_POINTS;
	public static long[][] FACTION_WAR_TOP_1_REWARD;
	public static long[][] FACTION_WAR_TOP_10_REWARD;
	public static long[][] FACTION_WAR_TOP_50_REWARD;
	public static long[][] FACTION_WAR_TOP_100_REWARD;
	public static long[][] FACTION_WAR_TOP_300_REWARD;
	public static long[][] FACTION_WAR_NOT_REWARD;

	public static void loadFactionWar()
	{
		final PropertiesParser properties = load(FACTION_WAR_FILE);
		FACTION_WAR_ENABLED = properties.getProperty("Enabled", true);
		FACTION_WAR_START_PERIOD_PATTERN = new SchedulingPattern(properties.getProperty("StartPeriodPattern", "30 20 * * *"));
		FACTION_WAR_MIN_LEVEL = properties.getProperty("MinLevel", 80);
		FACTION_WAR_PVP_KILL_POINTS = properties.getProperty("PvPKillPoints", 1);
		FACTION_WAR_PVP_DEATH_POINTS = properties.getProperty("PvPDeathPoints", 1);
		FACTION_WAR_OUTPOST_KILL_POINTS = properties.getProperty("AvanpostKillPoints", 25);
		FACTION_WAR_ARTIFACT_CAPTURE_POINTS = properties.getProperty("ArtifactCapturePoints", 25);
		FACTION_WAR_FORTRESS_SIEGE_ZONE_POINTS = properties.getProperty("FortressSiegeZonePoints", 25);
		FACTION_WAR_CASTLE_SIEGE_ZONE_POINTS = properties.getProperty("CastleSiegeZonePoints", 25);
		FACTION_WAR_EPIC_BOSS_KILL_POINTS = properties.getProperty("EpicBossKillPoints", 25);
		FACTION_WAR_UPGRADING_ARTIFACT_CAPTURE_POINTS = properties.getProperty("UpgradingArtifactCapturePoints", 45);
		FACTION_WAR_TRUST_LEVEL_POINTS = properties.getProperty("TrustLevelPoints", new int[]{1000, 2000, 3000, 4000, 5000, 6000});
		FACTION_WAR_TOP_1_REWARD = properties.getProperty("Top1Reward", new long[][]{{57, 1000},{57, 1000}});
		FACTION_WAR_TOP_10_REWARD = properties.getProperty("Top10Reward", new long[][]{{57, 1000},{57, 1000}});
		FACTION_WAR_TOP_50_REWARD = properties.getProperty("Top50Reward", new long[][]{{57, 1000},{57, 1000}});
		FACTION_WAR_TOP_100_REWARD = properties.getProperty("Top100Reward", new long[][]{{57, 1000},{57, 1000}});
		FACTION_WAR_TOP_300_REWARD = properties.getProperty("Top300Reward", new long[][]{{57, 1000},{57, 1000}});
		FACTION_WAR_NOT_REWARD = properties.getProperty("NotTopReward", new long[][]{{57, 1000},{57, 1000}});
		FACTION_WAR_PVP_HWID_ENABLE = properties.getProperty("PvPHwidEnable", true);
		FACTION_WAR_START_POINTS = properties.getProperty("StartPoints", 0);
	}

	public static void loadGMAccess()
	{
		gmlist.clear();
		loadGMAccess(new File("config/GMAccess.xml"));
		final File dir = new File("config/GMAccess.d/");
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists.");
			return;
		}
		for(final File f : dir.listFiles())
			if(!f.isDirectory() && f.getName().endsWith(".xml"))
				loadGMAccess(f);
	}

	public static void loadGMAccess(final File file)
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final Document doc = factory.newDocumentBuilder().parse(file);
			for(Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
				for(Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
					if("char".equalsIgnoreCase(n.getNodeName()))
					{
						final PlayerAccess pa = new PlayerAccess();
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final Class<?> cls = pa.getClass();
							final String node = d.getNodeName();
							if(!"#text".equalsIgnoreCase(node))
							{
								Field fld;
								try
								{
									fld = cls.getField(node);
								}
								catch(NoSuchFieldException e2)
								{
									_log.info("Not found desclarate ACCESS name: " + node + " in XML Player access Object");
									continue;
								}
								if("boolean".equalsIgnoreCase(fld.getType().getName()))
									fld.setBoolean(pa, Boolean.parseBoolean(d.getAttributes().getNamedItem("set").getNodeValue()));
								else if("int".equalsIgnoreCase(fld.getType().getName()))
									fld.setInt(pa, Integer.valueOf(d.getAttributes().getNamedItem("set").getNodeValue()));
								else {
									String value = d.getAttributes().getNamedItem("set").getNodeValue();
									FieldHelper.setObjectField(pa, fld.getName(), value, ",");
								}
							}
						}
						gmlist.put(pa.PlayerID, pa);
					}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getField(final String fieldName)
	{
		final Field field = FieldUtils.getField(Config.class, fieldName);
		if(field == null)
			return null;
		try
		{
			return String.valueOf(field.get(null));
		}
		catch(IllegalArgumentException ex)
		{}
		catch(IllegalAccessException ex2)
		{}
		return null;
	}

	public static boolean setField(final String fieldName, final String value)
	{
		final Field field = FieldUtils.getField(Config.class, fieldName);
		if(field == null)
			return false;
		try
		{
			if(field.getType() == Boolean.TYPE)
				field.setBoolean(null, BooleanUtils.toBoolean(value));
			else if(field.getType() == Integer.TYPE)
				field.setInt(null, NumberUtils.toInt(value));
			else if(field.getType() == Long.TYPE)
				field.setLong(null, NumberUtils.toLong(value));
			else if(field.getType() == Double.TYPE)
				field.setDouble(null, NumberUtils.toDouble(value));
			else
			{
				if(field.getType() != String.class)
					return false;
				field.set(null, value);
			}
		}
		catch(IllegalArgumentException e)
		{
			return false;
		}
		catch(IllegalAccessException e2)
		{
			return false;
		}
		return true;
	}

	public static PropertiesParser load(final String filename)
	{
		return load(new File(filename));
	}

	public static PropertiesParser load(final File file)
	{
		final PropertiesParser result = new PropertiesParser();
		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!");
		}
		return result;
	}

	public static boolean containsAbuseWord(final String s)
	{
		return ABUSEWORD_PATTERN != null && ABUSEWORD_PATTERN.matcher(s).matches();
	}

	public static String replaceAbuseWords(String text, final String censore)
	{
		if(ABUSEWORD_PATTERN == null)
			return text;
		final Matcher m = ABUSEWORD_PATTERN.matcher(text);
		while(m.find())
			text = text.replace(m.group(1), censore);
		return text;
	}

	static
	{
		_log = LoggerFactory.getLogger(Config.class);
		NCPUS = Runtime.getRuntime().availableProcessors();
		BAN_CHANNEL_LIST = new int[18];
		ABUSEWORD_PATTERN = null;
		ITEM_LIST = new ArrayList<>();
		DROP_ONLY_THIS = new TIntHashSet();
		ALT_ALLOWED_MULTISELLS_IN_PCBANG = new TIntHashSet();
		SELECTOR_CONFIG = new SelectorConfig();
		AUTO_LOOT_ITEM_ID_LIST = new TIntHashSet();
		CNAME_MAXLEN = 32;
		gmlist = new HashMap<>();
		ALLOW_CLASS_MASTERS_LIST = new TIntObjectHashMap<>();
		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
		RAID_GLOBAL_DROP = new ArrayList<>();
		GOODS_INVENTORY_ENABLED = false;
		VIP_ATTENDANCE_REWARDS_REWARD_BY_ACCOUNT = true;
	}


	public static boolean ALLOW_PAWN_PATHFIND = true;

	public static int MIN_LEVEL_TO_JOIN;
	public static String SET_HERO;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static boolean ALT_OLYMPIAD_EVERY_DAY;
	public static boolean ALLOW_CLASS_BATTLE;
	public static boolean FISHING_ONLY_PREMIUM_ACCOUNTS;

	public static long BAIUM_CLEAR_ZONE_IF_ALL_DIE;
	public static String BAIUM_FIX_TIME_PATTERN;
	public static int BAIUM_FIX_INTERVAL;
	public static int BAIUM_RANDOM_INTERVAL;
	public static long BAIUM_LIMIT_UNTIL_SLEEP;

	public static Duration GVE_GIVE_ASSIST_ATTACK_DELAY;
	public static double GVE_ASSIST_REWARD_PERCENT;

	public static int GVE_KILL_PENALTY_COUNT;
	public static Duration GVE_KILL_PENALTY_TIME;
	public static Duration GVE_KILL_PENALTY_REMOVE_TIME;
	public static int GVE_DEATH_PENALTY_COUNT;
	public static Duration GVE_DEATH_PENALTY_TIME;
	public static Duration GVE_DEATH_PENALTY_REMOVE_TIME;
	public static int GVE_SERIES_KILL_COUNT;
	public static Duration GVE_SERIES_KILL_PENALTY_TIME;
	public static Duration GVE_SERIES_KILL_PENALTY_REMOVE_TIME;

	public static int GVE_FORTRESS_ZONE_ENEMY_COUNT;
	public static long GVE_FORTRESS_ZONE_ENEMY_INTERVAL;
	public static Duration GVE_FORTRESS_SIEGE_INTERVAL;
	public static int GVE_FORTRESS_REWARD_MAX;
	public static long GVE_FORTRESS_REWARD_INTERVAL;
	public static int GVE_FORTRESS_REWARD_INCREASE;
	public static Duration GVE_FORTRESS_REWARD_TIME_IN_ZONE;
	public static Duration GVE_CASTLE_REWARD_TIME_IN_ZONE;
	public static int GVE_CASTLE_REWARD_COUNT_IN_ZONE;
	public static Map<Integer, Integer> GVE_FORTRESS_REWARD_WINNER = new HashMap<>();
	public static Map<Integer, Integer> GVE_FORTRESS_REWARD_LOSER = new HashMap<>();

	public static Pair<String, Location>[] GVE_PORTALS_BUTTON_DATA;

	public static String GVE_FORTRESS_REWARD_EFFECTS;
	public static String GVE_CASTLE_REWARD_EFFECTS;

	public static int GVE_FORTRESS_OWNER_REPUTATION_INCREASE_AMOUNT;
	public static int GVE_CASTLE_OWNER_REPUTATION_INCREASE_AMOUNT;

	public static TIntObjectHashMap<String> GVE_COMBO_KILL_MESSAGES = new TIntObjectHashMap<>();

	public static boolean MAX_LOAD_LOCATIONS_SYSTEM;
	public static boolean FACTION_BALANCE_IN_LOCATIONS;
	public static int FACTION_BALANCE_IN_LOCATION_MIN_COUNT;
	public static int FACTION_BALANCE_IN_LOCATION_MAX_PERCENT;
	public static int FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_PLAYER;
	public static int FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_CLAN;

	public static Map<String, Integer> BALANCE_LOCATIONS = new HashMap<>();
	public static Map<ItemGrade, Integer> ON_ENCHANT_FAIL = new HashMap<>();

	public static int GVE_FRACTION_CHANGE_PERSONAL;
	public static int GVE_FRACTION_CHANGE_CLAN;

	public static List<Integer> GVE_LOW_ZONE_RESTRICTED_ITEMS;
	public static List<Integer> GVE_MID_ZONE_RESTRICTED_ITEMS;

	public static int EPIC_BOSSES_DAMAGE_FOR_REWARD_AMOUNT;
	public static int RAID_BOSSES_DAMAGE_PERCENT_FOR_REWARD_AMOUNT;
	public static int RAID_BOSSES_SPECIAL_REWARD_TIME;
	public static int RAID_BOSSES_SPECIAL_REWARD_ID;
	public static int RAID_BOSSES_SPECIAL_REWARD_MAX_FACTION_DAMAGE_PERCENT;
	public static int RAID_BOSSES_SPECIAL_REWARD_REFRESH_TIMER_DELAY;
	public static int GROUP_BOSSES_DAMAGE_PERCENT_FOR_REWARD_AMOUNT;
	public static double FACTION_REWARD_ADENA_PENALTY;
	public static int[] MERCENARY_TIME;
	public static int[] DONATE_FACTION_COLORS_PRICE;
	public static int[] ADENA_FACTION_COLORS_PRICE;
	public static Map<Fraction, Integer> DONATE_FACTION_COLOR = new HashMap<>();

	public static int SUBS_SKILLS_REWARD_RECEIVE_LEVEL;
	public static int SUBS_SKILLS_REWARD_MAX_COUNT;
	public static int SUBS_SKILLS_REWARD_ITEM;
	public static List<Integer> SUBS_SKILLS_LIST;
	public static int SUBS_SKILLS_CURRENCY;
	public static int SUBS_SKILLS_PRICE;
	public static int SUBS_SKILLS_RESET_PRICE;

	public static int GVE_TELEPORT_PAYED_LEVEL;
	public static int GVE_TELEPORT_PRICE;

	public static void loadGveSettings()
	{
		GVE_COMBO_KILL_MESSAGES.clear();

		PropertiesParser gveSettings = load("config/gve.properties");

		GVE_GIVE_ASSIST_ATTACK_DELAY = gveSettings.getProperty("GiveAssistAttackDelay", Duration.ofMinutes(3));

		GVE_ASSIST_REWARD_PERCENT = gveSettings.getProperty("AssistRewardPercent", 0.4);

		GVE_KILL_PENALTY_COUNT = gveSettings.getProperty("KillPenaltyCount", 3);
		GVE_KILL_PENALTY_TIME = gveSettings.getProperty("KillPenaltyTime", Duration.ofMinutes(5));
		GVE_KILL_PENALTY_REMOVE_TIME = gveSettings.getProperty("KillPenaltyRemoveTime", Duration.ofMinutes(5));
		GVE_DEATH_PENALTY_COUNT = gveSettings.getProperty("DeathPenaltyCount", 5);
		GVE_DEATH_PENALTY_TIME = gveSettings.getProperty("DeathPenaltyTime", Duration.ofMinutes(5));
		GVE_DEATH_PENALTY_REMOVE_TIME = gveSettings.getProperty("DeathPenaltyRemoveTime", Duration.ofMinutes(5));
		GVE_SERIES_KILL_COUNT = gveSettings.getProperty("SeriesKillCount", 4);
		GVE_SERIES_KILL_PENALTY_TIME = gveSettings.getProperty("SeriesKillPenaltyTime", Duration.ofMinutes(5));
		GVE_SERIES_KILL_PENALTY_REMOVE_TIME = gveSettings.getProperty("SeriesKillPenaltyRemoveTime", Duration.ofMinutes(5));

		GVE_FORTRESS_ZONE_ENEMY_COUNT = gveSettings.getProperty("FortressZoneEnemyCount", 5);
		GVE_FORTRESS_ZONE_ENEMY_INTERVAL = TimeUnit.MINUTES.toMillis(gveSettings.getProperty("FortressZoneEnemyInterval", 5));
		GVE_FORTRESS_SIEGE_INTERVAL = gveSettings.getProperty("FortressSiegeInterval", Duration.ofHours(2));
		GVE_FORTRESS_REWARD_MAX = gveSettings.getProperty("FortressRewardMax", 450);
		GVE_FORTRESS_REWARD_INTERVAL = TimeUnit.MINUTES.toMillis(gveSettings.getProperty("FortressRewardInterval", 10));
		GVE_FORTRESS_REWARD_INCREASE = gveSettings.getProperty("FortressRewardIncrease", 15);
		GVE_FORTRESS_REWARD_TIME_IN_ZONE = gveSettings.getProperty("FortressRewardTimeInZone", Duration.ofMinutes(10));
		GVE_CASTLE_REWARD_TIME_IN_ZONE = gveSettings.getProperty("CastleRewardTimeInZone", Duration.ofMinutes(10));
		GVE_CASTLE_REWARD_COUNT_IN_ZONE = gveSettings.getProperty("CastleRewardCountInZone", 100);

		for (String reward : gveSettings.getProperty("FortressRewardWinner", new String[0], ";")) {
			String[] split = reward.split(":");
			GVE_FORTRESS_REWARD_WINNER.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		}

		for (String reward : gveSettings.getProperty("FortressRewardLoser", new String[0], ";")) {
			String[] split = reward.split(":");
			GVE_FORTRESS_REWARD_LOSER.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		}

		int len = gveSettings.getProperty("ZonesLen", 0);

		GVE_PORTALS_BUTTON_DATA = new Pair[len];

		for(int i = 0; i < len; i++)
		{
			String[] data = gveSettings.getProperty("Zone" + (i + 1), "test;0;0;0;0").split(";");

			GVE_PORTALS_BUTTON_DATA[i] = Pair.of(data[0], new Location(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]), Integer.valueOf(data[4])));
		}

		GVE_FORTRESS_REWARD_EFFECTS = gveSettings.getProperty("FortressRewardEffect", "");

		GVE_CASTLE_REWARD_EFFECTS = gveSettings.getProperty("CastleRewardEffect", "");

		GVE_FORTRESS_OWNER_REPUTATION_INCREASE_AMOUNT = gveSettings.getProperty("FortressOwnerReputationIncreaseAmount", 60);
		GVE_CASTLE_OWNER_REPUTATION_INCREASE_AMOUNT = gveSettings.getProperty("CastleOwnerReputationIncreaseAmount", 150);

		for(String data : gveSettings.getProperty("ComboKillSystem").split(";"))
			GVE_COMBO_KILL_MESSAGES.put(Integer.parseInt(data.split(",")[0]), data.split(",")[1]);

		GVE_FRACTION_CHANGE_PERSONAL = gveSettings.getProperty("FractionChangePersonal", 100);
		GVE_FRACTION_CHANGE_CLAN = gveSettings.getProperty("FractionChangeClan", 1000);

		GVE_LOW_ZONE_RESTRICTED_ITEMS = gveSettings.getProperty("LowZoneRestrictedItems", Integer.class);
		GVE_MID_ZONE_RESTRICTED_ITEMS = gveSettings.getProperty("MidZoneRestrictedItems", Integer.class);

		MAX_LOAD_LOCATIONS_SYSTEM = gveSettings.getProperty("MaxLoadLocationsSystem", false);
		FACTION_BALANCE_IN_LOCATIONS = gveSettings.getProperty("FactionBalanceInLocations", false);
		FACTION_BALANCE_IN_LOCATION_MIN_COUNT = gveSettings.getProperty("FactionBalanceInLocationMinCount", 10);
		FACTION_BALANCE_IN_LOCATION_MAX_PERCENT = gveSettings.getProperty("FactionBalanceInLocationMaxPercent", 55);
		FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_PLAYER = gveSettings.getProperty("FactionBalanceMinPercentToChangePlayer", 49);
		FACTION_BALANCE_MIN_PERCENT_TO_CHANGE_CLAN = gveSettings.getProperty("FactionBalanceMinPercentToChangeClan", 55);
		for(String data : gveSettings.getProperty("BalanceLocations").split(";")) {
			String[] split = data.split(":");
			BALANCE_LOCATIONS.put(split[0], Integer.parseInt(split[1]));
		}

		EPIC_BOSSES_DAMAGE_FOR_REWARD_AMOUNT = gveSettings.getProperty("EpicBossesDamageForRewardAmount", 10000);
		RAID_BOSSES_DAMAGE_PERCENT_FOR_REWARD_AMOUNT = gveSettings.getProperty("RaidBossesDamagePercentForRewardAmount", 1);
		RAID_BOSSES_SPECIAL_REWARD_TIME = gveSettings.getProperty("RaidBossesSpecialRewardTime", 60);
		RAID_BOSSES_SPECIAL_REWARD_ID = gveSettings.getProperty("RaidBossesSpecialRewardId", 57);
		RAID_BOSSES_SPECIAL_REWARD_MAX_FACTION_DAMAGE_PERCENT = gveSettings.getProperty("RaidBossesSpecialRewardMaxFactionDamagePercent", 60);
		RAID_BOSSES_SPECIAL_REWARD_REFRESH_TIMER_DELAY = gveSettings.getProperty("RaidBossesSpecialRewardRefreshTimerDelay", 10);
		GROUP_BOSSES_DAMAGE_PERCENT_FOR_REWARD_AMOUNT = gveSettings.getProperty("GroupBossesDamagePercentForRewardAmount", 1);
		MERCENARY_TIME = gveSettings.getProperty("MercenaryTime", new int[0], ",");

		for(String data : gveSettings.getProperty("OnEnchantFail").split(";")) {
			String[] split = data.split(":");
			ItemGrade itemGrade = ItemGrade.valueOf(split[0]);
			ON_ENCHANT_FAIL.put(itemGrade, Integer.parseInt(split[1]));
		}

		DONATE_FACTION_COLORS_PRICE = gveSettings.getProperty("donateFactionColorsPrice", new int[]{75040, 300}, ":");
		ADENA_FACTION_COLORS_PRICE = gveSettings.getProperty("adenaFactionColorsPrice", new int[]{57, 10000}, ":");
		for(String data : gveSettings.getProperty("donateFactionColors").split(";")) {
			String[] split = data.split(":");
			Fraction fraction = Fraction.valueOf(split[0]);
			int color = Integer.decode("0x" + Util.RGBtoBGR(split[1]));
			DONATE_FACTION_COLOR.put(fraction, color);
		}

		FACTION_REWARD_ADENA_PENALTY = gveSettings.getProperty("FactionRewardAdenaPenalty", 0d);

		GVE_FARM_ENABLED = gveSettings.getProperty("GveFarmEnabled", false);
		GVE_UPGRADING_EVENT_CAPTURE_REWARD_RADIUS = gveSettings.getProperty("UpgradingEventCaptureRewardRadius", 2000);
		GVE_PARTY_CLASS_LIMITS_GROUPS = gveSettings.getProperty("PartyClassLimitGroups", new String[]{}, ";");

		GVE_CASINO_ENABLED = gveSettings.getProperty("GveCasinoEnabled", false);
		GVE_CASINO_NPC_ID = gveSettings.getProperty("GveCasinoNpcId", 0);
		GVE_CASINO_MIN_BED = gveSettings.getProperty("GveCasinoMinBed", 1);
		GVE_CASINO_MAX_BED = gveSettings.getProperty("GveCasinoMaxBed", 100000);
		GVE_CASINO_TAX_PERCENT = gveSettings.getProperty("GveCasinoTaxPercent", 3);
		GVE_CASINO_MIN_ADENA_LEFT = gveSettings.getProperty("GveCasinoMinAdenaLeft", 3);

		GVE_TELEPORT_PAYED_LEVEL = gveSettings.getProperty("GveTeleportPayedLevel", 78);
		GVE_TELEPORT_PRICE = gveSettings.getProperty("GveTeleportPrice", 1);

		GVE_CLUB_CARD_ITEMS = gveSettings.getProperty("GveClubCardItems", new int[]{});
		RAID_BOSS_BBS_TELEPORT_PRICE = gveSettings.getProperty("RaidBossBBSTeleportPrice", 0);

		CHANGE_BASE_CLASS_ENABLE = gveSettings.getProperty("ChangeBaseClassEnable", true);
		CHANGE_BASE_CLASS_FIRST_PRICE = gveSettings.getProperty("ChangeBaseClassFirstPrice", new int[]{0, 0});
		CHANGE_BASE_CLASS_SECOND_PRICE = gveSettings.getProperty("ChangeBaseClassSecondPrice", new int[]{0, 0});
	}

	public static void loadBossSettings()
	{
		PropertiesParser bossSettings = load("config/bosses.properties");
		BAIUM_LIMIT_UNTIL_SLEEP = bossSettings.getProperty("BaiumSleepTime", 30) * 60000;
		BAIUM_FIX_INTERVAL = bossSettings.getProperty("BaiumSpawnTimeInterval", 120) * 3600000;
		BAIUM_FIX_TIME_PATTERN = bossSettings.getProperty("BaiumRespawnTimePattern", "");
		BAIUM_CLEAR_ZONE_IF_ALL_DIE = bossSettings.getProperty("BaiumClearZoneifAllDie", 1) * 60000;
		BAIUM_RANDOM_INTERVAL = bossSettings.getProperty("BaiumRandomSpawnAddTime", 8) * 3600000;
	}

	public static void loadSubSkillsSettings() {
		PropertiesParser subSkillsSettings = load(SUBSKILLS_FILE);
		SUBS_SKILLS_REWARD_RECEIVE_LEVEL = subSkillsSettings.getProperty("SubsSkillsRewardReceiveLevel", 79);
		SUBS_SKILLS_REWARD_MAX_COUNT = subSkillsSettings.getProperty("SubsSkillsRewardMaxCount", 5);
		SUBS_SKILLS_REWARD_ITEM = subSkillsSettings.getProperty("SubsSkillsRewardItem", 57);
		SUBS_SKILLS_LIST = new ArrayList<>();
		for (String skillId : subSkillsSettings.getProperty("SubsSkillsList").split(";")) {
			if (!skillId.isEmpty()) {
				SUBS_SKILLS_LIST.add(Integer.parseInt(skillId));
			}
		}
		SUBS_SKILLS_CURRENCY = subSkillsSettings.getProperty("SubsSkillsCurrency", 57);
		SUBS_SKILLS_PRICE = subSkillsSettings.getProperty("SubsSkillsPrice", 1);
		SUBS_SKILLS_RESET_PRICE = subSkillsSettings.getProperty("SubsSkillsResetPrice", 500);
	}

	public static class RaidGlobalDrop
	{
		int _id;
		long _count;
		double _chance;

		public RaidGlobalDrop(final int id, final long count, final double chance)
		{
			_id = id;
			_count = count;
			_chance = chance;
		}

		public int getId()
		{
			return _id;
		}

		public long getCount()
		{
			return _count;
		}

		public double getChance()
		{
			return _chance;
		}
	}
}
