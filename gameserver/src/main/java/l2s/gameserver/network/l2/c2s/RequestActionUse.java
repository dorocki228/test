package l2s.gameserver.network.l2.c2s;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.instancemanager.BotReportManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetBabyInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.instances.WildHohCannonInstance;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExAskCoupleAction;
import l2s.gameserver.network.l2.s2c.ExInzoneWaitingInfo;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyManageList;
import l2s.gameserver.network.l2.s2c.PrivateStoreManageList;
import l2s.gameserver.network.l2.s2c.RecipeShopManageListPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.ActionUseService;
import l2s.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestActionUse extends L2GameClientPacket
{
	private static final Logger _log;
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	public enum ActionType
	{
		Action,
		PetAction,
		PetSkill,
		Social,
		CoupleSocial
	}

	public enum Action
	{

		// Действия персонажей
		ACTION0(0, ActionType.Action, 0), // Сесть/встать
		ACTION1(1, ActionType.Action, 0), // Изменить тип передвижения, шаг/бег
		ACTION7(7, ActionType.Action, 0), // Next Target
		ACTION10(10, ActionType.Action, 0), // Запрос на создание приватного магазина продажи
		ACTION28(28, ActionType.Action, 0), // Запрос на создание приватного магазина покупки
		ACTION37(37, ActionType.Action, 0), // Создание магазина Common Craft
		ACTION38(38, ActionType.Action, 0), // Mount
		ACTION51(51, ActionType.Action, 0), // Создание магазина Dwarven Craft
		ACTION61(61, ActionType.Action, 0), // Запрос на создание приватного магазина продажи (Package)
		ACTION96(96, ActionType.Action, 0), // Quit Party Command Channel?
		ACTION97(97, ActionType.Action, 0), // Request Party Command Channel Info?

		ACTION67(67, ActionType.Action, 0), // Steer. Allows you to control the Airship.
		ACTION68(68, ActionType.Action, 0), // Cancel Control. Relinquishes control of the Airship.
		ACTION69(69, ActionType.Action, 0), // Destination Map. Choose from pre-designated locations.
		ACTION70(70, ActionType.Action, 0), // Exit Airship. Disembarks from the Airship.

		ACTION65(65, ActionType.Action, 0),
		ACTION76(76, ActionType.Action, 0),
		ACTION78(78, ActionType.Action, 0),
		ACTION79(79, ActionType.Action, 0),
		ACTION80(80, ActionType.Action, 0),
		ACTION81(81, ActionType.Action, 0),
		ACTION82(82, ActionType.Action, 0),
		ACTION83(83, ActionType.Action, 0),
		ACTION84(84, ActionType.Action, 0),
		ACTION85(85, ActionType.Action, 0),
		ACTION86(86, ActionType.Action, 0),
		ACTION90(90, ActionType.Action, 0),

		// Действия петов
		ACTION15(15, ActionType.PetAction, 0), // Pet Follow
		ACTION16(16, ActionType.PetAction, 0), // Атака петом
		ACTION17(17, ActionType.PetAction, 0), // Отмена действия у пета
		ACTION19(19, ActionType.PetAction, 0), // Отзыв пета
		ACTION21(21, ActionType.PetAction, 0), // Pet Follow
		ACTION22(22, ActionType.PetAction, 0), // Атака петом
		ACTION23(23, ActionType.PetAction, 0), // Отмена действия у пета
		ACTION32(32, ActionType.PetAction, 0), // Wild Hog Cannon - Mode Change
		ACTION52(52, ActionType.PetAction, 0), // Отзыв саммона
		ACTION53(53, ActionType.PetAction, 0), // Передвинуть пета к цели
		ACTION54(54, ActionType.PetAction, 0), // Передвинуть пета к цели
		ACTION1099(1099, ActionType.PetAction, 0), // All Attack
		ACTION1100(1100, ActionType.PetAction, 0), // All Move to target
		ACTION1101(1101, ActionType.PetAction, 0), // All Stop
		ACTION1102(1102, ActionType.PetAction, 0), //
		ACTION1103(1103, ActionType.PetAction, 0), // All Passive mode
		ACTION1104(1104, ActionType.PetAction, 0), // All Defending mode

		// Действия петов со скиллами
		ACTION36(36, ActionType.PetSkill, 4259), // Soulless - Toxic Smoke
		ACTION39(39, ActionType.PetSkill, 4138), // Soulless - Parasite Burst
		ACTION41(41, ActionType.PetSkill, 4230), // Wild Hog Cannon - Attack
		ACTION42(42, ActionType.PetSkill, 4378), // Kai the Cat - Self Damage Shield
		ACTION43(43, ActionType.PetSkill, 4137), // Unicorn Merrow - Hydro Screw
		ACTION44(44, ActionType.PetSkill, 4139), // Big Boom - Boom Attack
		ACTION45(45, ActionType.PetSkill, 4025), // Unicorn Boxer - Master Recharge
		ACTION46(46, ActionType.PetSkill, 4261), // Mew the Cat - Mega Storm Strike
		ACTION47(47, ActionType.PetSkill, 4260), // Silhouette - Steal Blood
		ACTION48(48, ActionType.PetSkill, 4068), // Mechanic Golem - Mech. Cannon
		ACTION1000(1000, ActionType.PetSkill, 4079), // Siege Golem - Siege Hammer
		//ACTION1001(1001,ActionType.PetSkill, ), // Sin Eater - Ultimate Bombastic Buster
		ACTION1003(1003, ActionType.PetSkill, 4710), // Wind Hatchling/Strider - Wild Stun
		ACTION1004(1004, ActionType.PetSkill, 4711), // Wind Hatchling/Strider - Wild Defense
		ACTION1005(1005, ActionType.PetSkill, 4712), // Star Hatchling/Strider - Bright Burst
		ACTION1006(1006, ActionType.PetSkill, 4713), // Star Hatchling/Strider - Bright Heal
		ACTION1007(1007, ActionType.PetSkill, 4699), // Cat Queen - Blessing of Queen
		ACTION1008(1008, ActionType.PetSkill, 4700), // Cat Queen - Gift of Queen
		ACTION1009(1009, ActionType.PetSkill, 4701), // Cat Queen - Cure of Queen
		ACTION1010(1010, ActionType.PetSkill, 4702), // Unicorn Seraphim - Blessing of Seraphim
		ACTION1011(1011, ActionType.PetSkill, 4703), // Unicorn Seraphim - Gift of Seraphim
		ACTION1012(1012, ActionType.PetSkill, 4704), // Unicorn Seraphim - Cure of Seraphim
		ACTION1013(1013, ActionType.PetSkill, 4705), // Nightshade - Curse of Shade
		ACTION1014(1014, ActionType.PetSkill, 4706), // Nightshade - Mass Curse of Shade
		ACTION1015(1015, ActionType.PetSkill, 4707), // Nightshade - Shade Sacrifice
		ACTION1016(1016, ActionType.PetSkill, 4709), // Cursed Man - Cursed Blow
		ACTION1017(1017, ActionType.PetSkill, 4708), // Cursed Man - Cursed Strike/Stun
		ACTION1031(1031, ActionType.PetSkill, 5135), // Feline King - Slash
		ACTION1032(1032, ActionType.PetSkill, 5136), // Feline King - Spin Slash
		ACTION1033(1033, ActionType.PetSkill, 5137), // Feline King - Hold of King
		ACTION1034(1034, ActionType.PetSkill, 5138), // Magnus the Unicorn - Whiplash
		ACTION1035(1035, ActionType.PetSkill, 5139), // Magnus the Unicorn - Tridal Wave
		ACTION1036(1036, ActionType.PetSkill, 5142), // Spectral Lord - Corpse Kaboom
		ACTION1037(1037, ActionType.PetSkill, 5141), // Spectral Lord - Dicing Death
		ACTION1038(1038, ActionType.PetSkill, 5140), // Spectral Lord - Force Curse
		ACTION1039(1039, ActionType.PetSkill, 5110), // Swoop Cannon - Cannon Fodder
		ACTION1040(1040, ActionType.PetSkill, 5111), // Swoop Cannon - Big Bang
		ACTION1041(1041, ActionType.PetSkill, 5442), // Great Wolf - 5442 - Bite Attack
		ACTION1042(1042, ActionType.PetSkill, 5444), // Great Wolf - 5444 - Moul
		ACTION1043(1043, ActionType.PetSkill, 5443), // Great Wolf - 5443 - Cry of the Wolf
		ACTION1044(1044, ActionType.PetSkill, 5445), // Great Wolf - 5445 - Awakening 70
		ACTION1045(1045, ActionType.PetSkill, 5584), // Wolf Howl
		ACTION1046(1046, ActionType.PetSkill, 5585), // Strider - Roar
		ACTION1047(1047, ActionType.PetSkill, 5580), // Divine Beast - Bite
		ACTION1048(1048, ActionType.PetSkill, 5581), // Divine Beast - Stun Attack
		ACTION1049(1049, ActionType.PetSkill, 5582), // Divine Beast - Fire Breath
		ACTION1050(1050, ActionType.PetSkill, 5583), // Divine Beast - Roar
		ACTION1051(1051, ActionType.PetSkill, 5638), // Feline Queen - Bless The Body
		ACTION1052(1052, ActionType.PetSkill, 5639), // Feline Queen - Bless The Soul
		ACTION1053(1053, ActionType.PetSkill, 5640), // Feline Queen - Haste
		ACTION1054(1054, ActionType.PetSkill, 5643), // Unicorn Seraphim - Acumen
		ACTION1055(1055, ActionType.PetSkill, 5647), // Unicorn Seraphim - Clarity
		ACTION1056(1056, ActionType.PetSkill, 5648), // Unicorn Seraphim - Empower
		ACTION1057(1057, ActionType.PetSkill, 5646), // Unicorn Seraphim - Wild Magic
		ACTION1058(1058, ActionType.PetSkill, 5652), // Nightshade - Death Whisper
		ACTION1059(1059, ActionType.PetSkill, 5653), // Nightshade - Focus
		ACTION1060(1060, ActionType.PetSkill, 5654), // Nightshade - Guidance
		ACTION1061(1061, ActionType.PetSkill, 5745), // (Wild Beast Fighter, White Weasel) Death Blow - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1062(1062, ActionType.PetSkill, 5746), // (Wild Beast Fighter) Double Attack - Rapidly attacks the enemy twice.
		ACTION1063(1063, ActionType.PetSkill, 5747), // (Wild Beast Fighter) Spin Attack - Inflicts shock and damage to the enemy at the same time with a powerful spin attack.
		ACTION1064(1064, ActionType.PetSkill, 5748), // (Wild Beast Fighter) Meteor Shower - Attacks nearby enemies with a doll heap attack.
		ACTION1065(1065, ActionType.PetSkill, 5753), // (Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess) Awakening - Awakens a hidden ability.
		ACTION1066(1066, ActionType.PetSkill, 5749), // (Fox Shaman, Spirit Shaman) Thunder Bolt - Attacks the enemy with the power of thunder.
		ACTION1067(1067, ActionType.PetSkill, 5750), // (Fox Shaman, Spirit Shaman) Flash - Inflicts a swift magic attack upon contacted enemies nearby.
		ACTION1068(1068, ActionType.PetSkill, 5751), // (Fox Shaman, Spirit Shaman) Lightning Wave - Attacks nearby enemies with the power of lightning.
		ACTION1069(1069, ActionType.PetSkill, 5752), // (Fox Shaman, Fairy Princess) Flare - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1070(1070, ActionType.PetSkill, 5771), // (White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar) Buff Control - Controls to prevent a buff upon the master. Lasts for 5 minutes.
		ACTION1071(1071, ActionType.PetSkill, 5761), // (Tigress) Power Striker - Powerfully attacks the target.
		ACTION1072(1072, ActionType.PetSkill, 6046), // (Toy Knight) Piercing attack
		ACTION1073(1073, ActionType.PetSkill, 6047), // (Toy Knight) Whirlwind
		ACTION1074(1074, ActionType.PetSkill, 6048), // (Toy Knight) Lance Smash
		ACTION1075(1075, ActionType.PetSkill, 6049), // (Toy Knight) Battle Cry
		ACTION1076(1076, ActionType.PetSkill, 6050), // (Turtle Ascetic) Power Smash
		ACTION1077(1077, ActionType.PetSkill, 6051), // (Turtle Ascetic) Energy Burst
		ACTION1078(1078, ActionType.PetSkill, 6052), // (Turtle Ascetic) Shockwave
		ACTION1079(1079, ActionType.PetSkill, 6053), // (Turtle Ascetic) Howl
		ACTION1080(1080, ActionType.PetSkill, 6041), // Phoenix Rush
		ACTION1081(1081, ActionType.PetSkill, 6042), // Phoenix Cleanse
		ACTION1082(1082, ActionType.PetSkill, 6043), // Phoenix Flame Feather
		ACTION1083(1083, ActionType.PetSkill, 6044), // Phoenix Flame Beak
		ACTION1084(1084, ActionType.PetSkill, 6054), // (Spirit Shaman, Toy Knight, Turtle Ascetic) Switch State - Toggles you between Attack and Support modes.
		ACTION1086(1086, ActionType.PetSkill, 6094), // Panther Cancel
		ACTION1087(1087, ActionType.PetSkill, 6095), // Panther Dark Claw
		ACTION1088(1088, ActionType.PetSkill, 6096), // Panther Fatal Claw
		ACTION1089(1089, ActionType.PetSkill, 6199), // (Deinonychus) Tail Strike
		ACTION1090(1090, ActionType.PetSkill, 6205), // (Guardian's Strider) Strider Bite
		ACTION1091(1091, ActionType.PetSkill, 6206), // (Guardian's Strider) Strider Fear
		ACTION1092(1092, ActionType.PetSkill, 6207), // (Guardian's Strider) Strider Dash
		ACTION1093(1093, ActionType.PetSkill, 6618),
		ACTION1094(1094, ActionType.PetSkill, 6681),
		ACTION1095(1095, ActionType.PetSkill, 6619),
		ACTION1096(1096, ActionType.PetSkill, 6682),
		ACTION1097(1097, ActionType.PetSkill, 6683),
		ACTION1098(1098, ActionType.PetSkill, 6684),
		ACTION5000(5000, ActionType.PetSkill, 23155), // Baby Rudolph - Reindeer Scratch 
		ACTION5001(5001, ActionType.PetSkill, 23167),
		ACTION5002(5002, ActionType.PetSkill, 23168),
		ACTION5003(5003, ActionType.PetSkill, 5749),
		ACTION5004(5004, ActionType.PetSkill, 5750),
		ACTION5005(5005, ActionType.PetSkill, 5751),
		ACTION5006(5006, ActionType.PetSkill, 5771),
		ACTION5007(5007, ActionType.PetSkill, 6046),
		ACTION5008(5008, ActionType.PetSkill, 6047),
		ACTION5009(5009, ActionType.PetSkill, 6048),
		ACTION5010(5010, ActionType.PetSkill, 6049),
		ACTION5011(5011, ActionType.PetSkill, 6050),
		ACTION5012(5012, ActionType.PetSkill, 6051),
		ACTION5013(5013, ActionType.PetSkill, 6052),
		ACTION5014(5014, ActionType.PetSkill, 6053),
		ACTION5015(5015, ActionType.PetSkill, 6054),

		// Социальные действия
		ACTION12(12, ActionType.Social, SocialActionPacket.GREETING),
		ACTION13(13, ActionType.Social, SocialActionPacket.VICTORY),
		ACTION14(14, ActionType.Social, SocialActionPacket.ADVANCE),
		ACTION24(24, ActionType.Social, SocialActionPacket.YES),
		ACTION25(25, ActionType.Social, SocialActionPacket.NO),
		ACTION26(26, ActionType.Social, SocialActionPacket.BOW),
		ACTION29(29, ActionType.Social, SocialActionPacket.UNAWARE),
		ACTION30(30, ActionType.Social, SocialActionPacket.WAITING),
		ACTION31(31, ActionType.Social, SocialActionPacket.LAUGH),
		ACTION33(33, ActionType.Social, SocialActionPacket.APPLAUD),
		ACTION34(34, ActionType.Social, SocialActionPacket.DANCE),
		ACTION35(35, ActionType.Social, SocialActionPacket.SORROW),
		ACTION62(62, ActionType.Social, SocialActionPacket.CHARM),
		ACTION66(66, ActionType.Social, SocialActionPacket.SHYNESS),
		ACTION87(87, ActionType.Social, SocialActionPacket.PROPOSE),
		ACTION88(88, ActionType.Social, SocialActionPacket.PROVOKE),
		ACTION89(89, ActionType.Social, SocialActionPacket.BOASTING),
		// Парные социальные действия
		ACTION71(71, ActionType.CoupleSocial, SocialActionPacket.COUPLE_BOW),
		ACTION72(72, ActionType.CoupleSocial, SocialActionPacket.COUPLE_HIGH_FIVE),
		ACTION73(73, ActionType.CoupleSocial, SocialActionPacket.COUPLE_DANCE),

		ACTION1001(1001, ActionType.PetAction, 0),
		ACTION5016(5016, ActionType.PetAction, 6054),
		ACTION1002(1002, ActionType.PetSkill, 0),
		ACTION1018(1018, ActionType.PetSkill, 0),
		ACTION1019(1019, ActionType.PetSkill, 0),
		ACTION1020(1020, ActionType.PetSkill, 0),
		ACTION1021(1021, ActionType.PetSkill, 0),
		ACTION1022(1022, ActionType.PetSkill, 0),
		ACTION1023(1023, ActionType.PetSkill, 0),
		ACTION1024(1024, ActionType.PetSkill, 0),
		ACTION1025(1025, ActionType.PetSkill, 0),
		ACTION1026(1026, ActionType.PetSkill, 0),
		ACTION1027(1027, ActionType.PetSkill, 0),
		ACTION1028(1028, ActionType.PetSkill, 0),
		ACTION1029(1029, ActionType.PetSkill, 0),
		ACTION1030(1030, ActionType.PetSkill, 0),
		ACTION1113(1113, ActionType.PetSkill, 10051),
		ACTION1114(1114, ActionType.PetSkill, 10052),
		ACTION1115(1115, ActionType.PetSkill, 10053),
		ACTION1116(1116, ActionType.PetSkill, 10054),
		ACTION1117(1117, ActionType.PetSkill, 10794),
		ACTION1118(1118, ActionType.PetSkill, 10795),
		ACTION1120(1120, ActionType.PetSkill, 10797),
		ACTION1121(1121, ActionType.PetSkill, 10798),
		ACTION1122(1122, ActionType.PetSkill, 11806),
		ACTION1123(1123, ActionType.PetSkill, 14767),
		ACTION1142(1142, ActionType.PetSkill, 10087),
		ACTION1143(1143, ActionType.PetSkill, 10088),
		ACTION999(999, ActionType.Action, 0),
		ACTION10000(10000, ActionType.Action, 0),
		ACTION10001(10001, ActionType.Action, 0);

		public int id;
		public ActionType type;
		public int value;

		Action(int id, ActionType type, int value)
		{
			this.id = id;
			this.type = type;
			this.value = value;
		}

		public static Action find(int id)
		{
			for(Action action : values())
				if(action.id == id)
					return action;
			return null;
		}

		@Override
		public String toString()
		{
			return "id=" + id + " type=" + type.name() + " val=" + value;
		}
	}

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		Action action = Action.find(_actionId);
		if(action == null)
		{
			_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isTransformed() && !activeChar.getTransform().haveAction(action.id))
		{
			activeChar.sendActionFailed();
			return;
		}

		boolean usePet = action.type == ActionType.PetAction || action.type == ActionType.PetSkill;

		// dont do anything if player is dead or confused
		if(!usePet && (activeChar.isOutOfControl() || activeChar.isActionsDisabled()) && !(activeChar.isFakeDeath() && _actionId == 0) && !(_actionId >= 78 && _actionId <= 85))
		{
			activeChar.sendActionFailed();
			return;
		}

		GameObject target = activeChar.getTarget();

		if(action.type == ActionType.Social)
		{
			if(activeChar.isOutOfControl() || activeChar.isTransformed() || activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.getPrivateStoreType() != 0 || activeChar.isProcessingRequest())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isFishing())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
				return;
			}

			activeChar.broadcastPacket(new SocialActionPacket(activeChar.getObjectId(), action.value));

			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600L);
				activeChar.startParalyzed();
			}

			activeChar.getListeners().onSocialAction(action);

			if(target != null && target.isNpc())
			{
				NpcInstance npc = (NpcInstance) target;
				if(npc.checkInteractionDistance(activeChar))
					npc.onSeeSocialAction(activeChar, action.value);
			}

			for(QuestState state : activeChar.getAllQuestsStates())
				state.getQuest().notifySocialActionUse(state, action.value);

			return;
		}

		if(action.type == ActionType.CoupleSocial)
		{

			if(activeChar.isOutOfControl() || activeChar.isActionsDisabled() || activeChar.isSitting())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(target == null || !target.isPlayer())
			{
				activeChar.sendActionFailed();
				return;
			}

			Player pcTarget = target.getPlayer();
			if(pcTarget.isProcessingRequest() && pcTarget.getRequest().isTypeOf(Request.L2RequestType.COUPLE_ACTION))
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION).addName(pcTarget));
				return;
			}

			if(pcTarget.isProcessingRequest())
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(pcTarget));
				return;
			}

			if(!activeChar.isInRange(pcTarget, 300L) || activeChar.isInRange(pcTarget, 25L) || activeChar.getTargetId() == activeChar.getObjectId() || !GeoEngine.canSeeTarget(activeChar, pcTarget, false))
			{
				activeChar.sendPacket(SystemMsg.THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS);
				return;
			}

			if(!activeChar.checkCoupleAction(pcTarget))
				return;

			new Request(Request.L2RequestType.COUPLE_ACTION, activeChar, pcTarget).setTimeout(10000L);
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_REQUESTED_A_COUPLE_ACTION_WITH_C1).addName(pcTarget));
			pcTarget.sendPacket(new ExAskCoupleAction(activeChar.getObjectId(), action.value));

			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600L);
				activeChar.startParalyzed();
			}

			return;
		}

		Servitor pet = activeChar.getFirstServitor();

		if(usePet)
		{
			if(pet == null)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_A_SERVITOR);
				return;
			}

			if(pet.isOutOfControl() || activeChar.isDead())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(pet.isDepressed())
			{
				activeChar.sendPacket(SystemMsg.YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				return;
			}
		}

		if(action.type == ActionType.PetSkill)
		{
			// TODO перенести эти условия в скиллы
			if(action.id == 1000 && target != null && !target.isDoor()) // Siege Golem - Siege Hammer
			{
				activeChar.sendActionFailed();
				return;
			}

			if((action.id == 1039 || action.id == 1040) && ((target != null && target.isDoor()) || target instanceof SiegeFlagInstance)) // Swoop Cannon (не может атаковать двери и флаги)
			{
				activeChar.sendActionFailed();
				return;
			}

			servitorUseSkill(activeChar, pet, action.value, action.id);
			return;
		}

		switch(action)
		{
			case ACTION0:
			{
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}

				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
					break;
				}

				if(activeChar.isSitting())
				{
					activeChar.standUp();
					break;
				}

				if(target != null && target instanceof StaticObjectInstance && ((StaticObjectInstance) target).getType() == 1 && activeChar.checkInteractionDistance(target))
				{
					activeChar.sitDown((StaticObjectInstance) target);
					break;
				}

				activeChar.sitDown(null);
				break;
			}
			case ACTION1:
			{
				if(activeChar.isRunning())
					activeChar.setWalking();
				else
					activeChar.setRunning();

				activeChar.sendUserInfo(true);
				break;
			}
			case ACTION10:
			case ACTION61:
			{
				if (activeChar.isPrivateBuffer()) {
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}

				else if(!TradeHelper.checksIfCanOpenStore(activeChar, _actionId == 61 ? 8 : 1))
				{
					activeChar.sendActionFailed();
					return;
				}

				activeChar.sendPacket(new PrivateStoreManageList(activeChar, _actionId == 61));
				break;
			}
			case ACTION28:
			{
				if (activeChar.isPrivateBuffer()) {
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}

				else if(!TradeHelper.checksIfCanOpenStore(activeChar, 3))
				{
					activeChar.sendActionFailed();
					return;
				}

				activeChar.sendPacket(new PrivateStoreBuyManageList(activeChar));
				break;
			}
			case ACTION37:
			{
				if (activeChar.isPrivateBuffer()) {
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.getDwarvenRecipeBook().isEmpty())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, 5))
				{
					activeChar.sendActionFailed();
					return;
				}

				activeChar.sendPacket(new RecipeShopManageListPacket(activeChar, true));
				break;
			}
			case ACTION51:
			{
				if (activeChar.isPrivateBuffer()) {
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.getCommonRecipeBook().isEmpty())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, 5))
				{
					activeChar.sendActionFailed();
					return;
				}

				activeChar.sendPacket(new RecipeShopManageListPacket(activeChar, false));
				break;
			}
			case ACTION96:
			{
				_log.info("96 Accessed");
				break;
			}
			case ACTION97:
			{
				_log.info("97 Accessed");
				break;
			}
			case ACTION38:
			{

				if(activeChar.isTransformed())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}

				if(pet == null || !pet.isMountable())
				{
					if(!activeChar.isMounted())
						break;

					if(activeChar.getMount().isHungry())
						return;

					if(activeChar.isFlying() && !activeChar.checkLandingState())
					{
						activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_IN_THIS_LOCATION, ActionFailPacket.STATIC);
						return;
					}

					activeChar.setMount(null);
					break;
				}
				else
				{
					if(activeChar.isMounted() || activeChar.isInBoat())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.isDead())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(pet.isDead())
					{
						activeChar.sendPacket(SystemMsg.A_DEAD_STRIDER_CANNOT_BE_RIDDEN);
						break;
					}

					if(activeChar.isInDuel())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.isInCombat() || pet.isInCombat())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.isFishing())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.isSitting())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.getActiveWeaponFlagAttachment() != null)
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.isCastingNow())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(activeChar.isDecontrolled())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
						break;
					}

					if(pet.isHungry())
					{
						activeChar.sendPacket(SystemMsg.A_HUNGRY_STRIDER_CANNOT_BE_MOUNTED_OR_DISMOUNTED);
						break;
					}

					activeChar.getAbnormalList().stopEffects(5239);
					activeChar.setMount(pet.getControlItemObjId(), pet.getNpcId(), pet.getLevel(), pet.getCurrentFed());
					pet.unSummon(false);
					break;
				}
			}
			case ACTION65:
			{
				BotReportManager.getInstance().reportBot(activeChar);
				break;
			}
			case ACTION76:
			{
				if(target == null)
					return;

				IBroadcastPacket msg = activeChar.getFriendList().requestFriendInvite(target);

				if(msg != null)
				{
					activeChar.sendPacket(msg);
					activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
					break;
				}
				break;
			}
			case ACTION78:
			{
				changeTacticalSign(activeChar, 1, target);
				break;
			}
			case ACTION79:
			{
				changeTacticalSign(activeChar, 2, target);
				break;
			}
			case ACTION80:
			{
				changeTacticalSign(activeChar, 3, target);
				break;
			}
			case ACTION81:
			{
				changeTacticalSign(activeChar, 4, target);
				break;
			}
			case ACTION82:
			{
				findTacticalTarget(activeChar, 1);
				break;
			}
			case ACTION83:
			{
				findTacticalTarget(activeChar, 2);
				break;
			}
			case ACTION84:
			{
				findTacticalTarget(activeChar, 3);
				break;
			}
			case ACTION85:
			{
				findTacticalTarget(activeChar, 4);
				break;
			}
			case ACTION90:
			{
				activeChar.sendPacket(new ExInzoneWaitingInfo(activeChar));
				break;
			}
			case ACTION15:
			case ACTION21:
			{
				pet.setFollowMode(!pet.isFollowMode());
				break;
			}
			case ACTION32:
			{
				if(pet instanceof WildHohCannonInstance)
				{
					((WildHohCannonInstance) pet).changeMod();
				}
				break;
			}
			case ACTION16:
			case ACTION22:
			case ACTION1099:
			{
				if(target == null || !target.isCreature() || target == activeChar || pet == target || pet.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInOlympiadMode() && !activeChar.isOlympiadCompStart())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(pet.isNotControlled())
				{
					activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}

				pet.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			}
			case ACTION17:
			case ACTION23:
			case ACTION1101:
			{
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			}
			case ACTION19:
			{
				if(pet.isDead())
				{
					activeChar.sendPacket(SystemMsg.DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM, ActionFailPacket.STATIC);
					return;
				}

				if(pet.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE, ActionFailPacket.STATIC);
					break;
				}

				if(pet.isHungry())
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_RESTORE_A_HUNGRY_PET, ActionFailPacket.STATIC);
					break;
				}

				pet.unSummon(false);
				break;
			}
			case ACTION52:
			case ACTION1102:
			{
				if(pet.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE);
					activeChar.sendActionFailed();
				}
				else
					pet.unSummon(false);
				break;
			}
			case ACTION53:
			case ACTION54:
			case ACTION1100:
			{
				if(target != null && pet != target && !pet.isMovementDisabled())
				{
					pet.setFollowMode(false);
					pet.moveToLocation(target.getLoc(), 100, true);
					break;
				}
				break;
			}
			case ACTION1070:
			{
				if(pet instanceof PetBabyInstance)
					((PetBabyInstance) pet).triggerBuff();

				break;
			}
			case ACTION999: {
				Comparator<Creature> comparator = Comparator.comparingDouble(activeChar::getDistance);
/*
				GameObject currentTarget = activeChar.getTarget();
				if (currentTarget == null) {
					Optional<Playable> nextTarget = World.getAroundPlayables(activeChar, Config.CUSTOM_NEXT_TARGET_RADIUS, 100).stream()
							.filter(playable -> ActionUseService.getInstance().isNextTargetAttackable(activeChar, playable))
							.filter(playable -> !playable.isDead())
							.filter(playable -> !playable.isInvisible(activeChar))
							.filter(GameObject::isVisible)
							.min(comparator);
					nextTarget.ifPresent(activeChar::setTarget);
				} else {
					List<Playable> list = World.getAroundPlayables(activeChar, Config.CUSTOM_NEXT_TARGET_RADIUS, 100).stream()
							.filter(playable -> ActionUseService.getInstance().isNextTargetAttackable(activeChar, playable))
							.filter(playable -> !playable.isDead())
							.filter(playable -> !playable.isInvisible(activeChar))
							.filter(GameObject::isVisible)
							.sorted(comparator)
							.collect(Collectors.toUnmodifiableList());

					if (list.isEmpty()) {
						return;
					}

					int nextIndex = 0;
					if(currentTarget instanceof Playable) {
						nextIndex = list.indexOf(currentTarget) + 1;
						if(nextIndex >= list.size()) {
							nextIndex = 0;
						}
					}
					activeChar.setTarget(null);
					activeChar.setTarget(list.get(nextIndex));
				}*/
				Optional<Playable> nextTarget = World.getAroundPlayables(activeChar, Config.CUSTOM_NEXT_TARGET_RADIUS, 400).stream()
						.filter(playable -> ActionUseService.getInstance().isNextTargetAttackable(activeChar, playable))
						.filter(playable -> !playable.isDead())
						.filter(playable -> !playable.isInvisible(activeChar))
						.filter(GameObject::isVisible)
						.min(comparator);
				nextTarget.ifPresent(activeChar::setTarget);
				break;
			}
			/*
			 * If you want to change this logic, please don't forget
			 * to change it also in {@link l2s.gameserver.handler.usercommands.impl.TargetNextMonster}
			 */
			case ACTION10000: {
				Comparator<Creature> comparator = Comparator.comparingDouble(activeChar::getDistance);
				GameObject currentTarget = activeChar.getTarget();
				if(currentTarget == null) {
					Optional<NpcInstance> nextTarget = World.getAroundNpc(activeChar, Config.CUSTOM_NEXT_TARGET_RADIUS, 100).stream()
							.filter(npc -> ActionUseService.getInstance().isNextTargetNpc(activeChar, npc))
							.filter(npc -> !npc.isDead())
							.filter(npc -> !npc.isInvisible(activeChar))
							.filter(GameObject::isVisible)
							.min(comparator);
					nextTarget.ifPresent(activeChar::setTarget);
				}
				else {
					List<NpcInstance> list = World.getAroundNpc(activeChar, Config.CUSTOM_NEXT_TARGET_RADIUS, 100).stream()
							.filter(npc -> ActionUseService.getInstance().isNextTargetNpc(activeChar, npc))
							.filter(npc -> !npc.isDead())
							.filter(npc -> !npc.isInvisible(activeChar))
							.filter(GameObject::isVisible)
							.sorted(comparator)
							.collect(Collectors.toUnmodifiableList());

					if(list.isEmpty()) {
						return;
					}
					int nextIndex = 0;
					if(currentTarget instanceof NpcInstance) {
						nextIndex = list.indexOf(currentTarget) + 1;
						if(nextIndex >= list.size()) {
							nextIndex = 0;
						}
					}
					activeChar.setTarget(null);
					activeChar.setTarget(list.get(nextIndex));
				}
				break;
			}
			case ACTION10001: {
				if (!Config.PRIVATE_BUFFER.enabled()) {
					activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.Disabled"));
					break;
				}
				if (Config.PRIVATE_BUFFER.onlyInSpecialZone() && !activeChar.isInZone(Zone.ZoneType.private_buffer)) {
					activeChar.sendMessage(new CustomMessage("services.privatebuffer.error.zone"));
					break;
				}
				if (Config.PRIVATE_BUFFER.availableClass()[0] > 0) {
					if (!ArrayUtils.contains(Config.PRIVATE_BUFFER.availableClass(), activeChar.getActiveClassId())) {
						activeChar.sendMessage(new CustomMessage("services.privatebuffer.error.class"));
						break;
					}
				}
				if (activeChar.isPrivateBuffer()) {
					activeChar.cancelPrivateBuffer();
				}

				HtmlMessage message = new HtmlMessage(0).setFile("buffer/private/index.htm");
				message.addVar("minPrice", Config.PRIVATE_BUFFER.minPrice());
				message.addVar("maxPrice", Config.PRIVATE_BUFFER.maxPrice());
				message.addVar("tax", Config.PRIVATE_BUFFER.taxPercent());
				activeChar.sendPacket(message);
				break;
			}
			default:
			{
				_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
				break;
			}
		}
	}

	private void summonsUseSkill(Player player, int skillId, int actionId)
	{
		if(player.hasSummon())
		{
			for(SummonInstance s : player.getSummons())
				if(s != null)
				{
					if(s.isOutOfControl())
						continue;
					if(s.isDepressed())
						player.sendPacket(SystemMsg.YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
					else
						servitorUseSkill(player, s, skillId, actionId);
				}
		}
		else
			player.sendActionFailed();
	}

	private boolean servitorUseSkill(Player player, Servitor servitor, int skillId, int actionId)
	{
		if(servitor == null)
			return false;

		int skillLevel = servitor.getSkillLevel(skillId, 0);
		if(skillLevel == 0)
			return false;

		Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);
		if(skill == null)
			return false;

		if(servitor.isNotControlled())
		{
			player.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return false;
		}
		Creature aimingTarget = skill.getAimingTarget(servitor, player.getTarget());
		if(!skill.checkCondition(servitor, aimingTarget, _ctrlPressed, _shiftPressed, true))
			return false;
		servitor.setUsedSkill(skill, actionId);
		servitor.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		return true;
	}

	private void changeTacticalSign(Player player, int sign, GameObject target)
	{
		if(!player.isInParty())
			return;
		if(target == null || !target.isCreature() || !target.isTargetable(player))
			return;
		player.getParty().changeTacticalSign(player, sign, (Creature) target);
	}

	private void findTacticalTarget(Player player, int sign)
	{
		if(!player.isInParty())
			return;
		Creature target = player.getParty().findTacticalTarget(player, sign);
		if(target == null || target.isAlikeDead() || !target.isTargetable(player))
			return;
		player.setNpcTarget(target);
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestActionUse.class);
	}

	static class SocialTask implements Runnable
	{
		Player _player;

		SocialTask(Player player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			_player.stopParalyzed();
		}
	}
}
