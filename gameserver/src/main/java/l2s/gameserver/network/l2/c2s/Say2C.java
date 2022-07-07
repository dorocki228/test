package l2s.gameserver.network.l2.c2s;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.ItemInfoCache;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ChatLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.service.AntiFloodService;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.utils.ChatUtils;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.spamfilter.SpamFilterManager;
import l2s.gameserver.utils.spamfilter.SpamFilterManager.SpamType;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);

	private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("ID=([0-9]+)");
	private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");

	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = (ChatType) ArrayUtils.valid((Object[]) ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_type == null || _text == null || _text.isEmpty())
		{
			activeChar.sendActionFailed();
			return;
		}
		_text = _text.replaceAll("\\\\n", "\n");
		if(_text.contains("\n"))
		{
			String[] lines = _text.split("\n");
			_text = "";
			for(int i = 0; i < lines.length; ++i)
			{
				lines[i] = lines[i].trim();
				if(!lines[i].isEmpty())
				{
					if(!_text.isEmpty())
						_text += "\n  >";
					_text += lines[i];
				}
			}
		}
		if(_text.isEmpty())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(Config.BAN_FOR_CFG_USAGE && (_text.startsWith("//cfg") || _text.startsWith("///cfg") || _text.startsWith("////cfg")))
			activeChar.kick();
		if(_text.startsWith("."))
		{
			if(Config.ALLOW_VOICED_COMMANDS)
			{
				String fullcmd = _text.substring(1).trim();
				String command = fullcmd.split("\\s+")[0];
                if(!command.isEmpty())
				{
					IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					if(vch != null)
					{
                        String args = fullcmd.substring(command.length()).trim();
                        vch.useVoicedCommand(command, activeChar, args);
						return;
					}
				}
				activeChar.sendMessage(new CustomMessage("common.command404"));
				return;
			}
		} else if(_text.startsWith("_")) {
			if(FactionLeaderService.getInstance().isFactionLeader(activeChar)) {
				if(!FactionLeaderService.getInstance().isAllOnlineLeaders() || !FactionLeaderService.getInstance().isLeaderTime()) {
					activeChar.sendMessage(new CustomMessage("faction.leader.14"));
					return;
				}
				String message = _text.substring(1);
				SayPacket2 cs = new SayPacket2(activeChar.getObjectId(), ChatType.COMMANDCHANNEL_ALL, activeChar.getName(), message);
				GameObjectsStorage.getFractionStream(activeChar.getFraction()).forEach(p-> p.sendPacket(cs));
				return;
			}
		}
		if(activeChar.isChatBlocked())
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_A_CONTACT_WHILE_A_CHATTING_BLOCK_IS_IMPOSED);
			return;
		}
		boolean globalchat = _type != ChatType.ALLIANCE && _type != ChatType.CLAN && _type != ChatType.PARTY;
		boolean checkForBan = globalchat || org.apache.commons.lang3.ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type.ordinal());
		if(checkForBan && PunishmentService.INSTANCE.isPunished(PunishmentType.CHAT, String.valueOf(activeChar.getObjectId())))
		{
			Duration remainingTime = PunishmentService.INSTANCE.remainingTime(PunishmentType.CHAT, String.valueOf(activeChar.getObjectId()));
			if(remainingTime != null)
			{
				int timeRemained = Math.toIntExact(remainingTime.toMinutes());
				activeChar.sendMessage(new CustomMessage("common.ChatBanned").addNumber(timeRemained));
			}
			else
				activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently"));

			activeChar.sendActionFailed();
			return;
		}
		if(globalchat)
			if(Config.ABUSEWORD_REPLACE)
				_text = Config.replaceAbuseWords(_text, Config.ABUSEWORD_REPLACE_STRING);
			else if(Config.ABUSEWORD_BANCHAT && Config.containsAbuseWord(_text))
			{
				activeChar.sendMessage(new CustomMessage("common.ChatBanned").addNumber(Config.ABUSEWORD_BANTIME * 60));

				String messagePattern = "{}: {}";
				ParameterizedMessage message = new ParameterizedMessage(messagePattern, activeChar, _text);
				LogService.getInstance().log(LoggerType.ABUSE, message);

				var bannedUntil = ZonedDateTime.now().plus(Config.ABUSEWORD_BANTIME, ChronoUnit.MINUTES);
				PunishmentService.INSTANCE.addPunishment(PunishmentType.CHAT, String.valueOf(activeChar.getObjectId()),
						bannedUntil, "Say2C", "abuse");
				activeChar.sendActionFailed();
				return;
			}
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
		while(m.find())
		{
			int objectId = Integer.parseInt(m.group(1));
			ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
			if(item == null)
			{
				activeChar.sendActionFailed();
				break;
			}
			if(HidenItemsDAO.isHidden(item))
			{
				activeChar.sendActionFailed();
				return;
			}
			ItemInfoCache.getInstance().put(item);
		}
		String translit = activeChar.getVar("translit");
		if(translit != null)
		{
			m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), "tl".equals(translit) ? 1 : 2));
				sb.append(_text, end, end = m.end());
			}
			_text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), "tl".equals(translit) ? 1 : 2)).toString();
		}

		Player receiver = _target != null ? GameObjectsStorage.getPlayer(_target) : null;

		ChatLogMessage message = new ChatLogMessage(_type, activeChar, receiver, _text);
		LogService.getInstance().log(LoggerType.CHAT, message);

		activeChar.broadcastSnoop(_type, activeChar.getName(), _text);
		SayPacket2 cs = new SayPacket2(activeChar.getObjectId(), _type, activeChar.getName(), _text);

		boolean isSpam = false;

		if(_type == ChatType.ALL || _type == ChatType.SHOUT || _type == ChatType.TELL
				|| _type == ChatType.FRACTION_SHOUT || _type == ChatType.FRACTION_WORLD)
			isSpam = SpamFilterManager.getInstance().isSpam(activeChar, _text, SpamType.getFromChatType(_type));

		switch(_type)
		{
			case TELL:
			{
				if(receiver == null)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), ActionFailPacket.STATIC);
					return;
				}

				if(receiver != null && receiver.isInOfflineMode())
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), ActionFailPacket.STATIC);
					break;
				}
				if(receiver != null && !receiver.getBlockList().contains(activeChar) && !receiver.isBlockAll())
				{
					if(receiver.getMessageRefusal())
					{
						activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
						break;
					}
					if(!activeChar.getAntiFlood().canTell(receiver.getObjectId(), _text))
						return;
					if(activeChar.canTalkWith(receiver) && (!isSpam || Config.SPAM_FILTER_DUMMY_SPAM && HwidUtils.INSTANCE.isSameHWID(activeChar, receiver)))
					{
						cs.setSenderInfo(activeChar, receiver);
						receiver.sendPacket(cs);
						receiver.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
					}
					cs = new SayPacket2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text);
					cs.setSenderInfo(activeChar, receiver);
					activeChar.sendPacket(cs);
					activeChar.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
					break;
				}
				else
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT, ActionFailPacket.STATIC);
					break;
				}
			}
			case SHOUT:
			{
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}
				if(!activeChar.getAntiFlood().canShout(_text))
					return;
				if(Config.GLOBAL_SHOUT)
					ChatUtils.announce(activeChar, cs, isSpam);
				else
					ChatUtils.shout(activeChar, cs, isSpam);
				activeChar.sendPacket(cs);
				break;
			}
			case FRACTION_SHOUT:
			{
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.getAntiFlood().canUseFractionShoutChat(_text))
					return;

				ChatUtils.fractionalGlobalShout(activeChar,cs, isSpam);
				activeChar.sendPacket(cs);
				break;
			}
			case ALL:
			{
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.getAntiFlood().canAll(_text))
					return;

				OlympiadGame game = activeChar.getOlympiadGame();

				if(activeChar.isInOlympiadMode() && game != null)
				{
					ChatUtils.say(activeChar, game.getAllPlayers(), cs);
					break;
				}

				ChatUtils.say(activeChar, cs, isSpam);
				cs.setCharName(activeChar.getVisibleName(activeChar));
				activeChar.sendPacket(cs);
				break;
			}
			case CLAN:
			{
				if(activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineMembers(cs);
					break;
				}
				break;
			}
			case ALLIANCE:
			{
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
				{
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
					break;
				}
				break;
			}
			case PARTY:
			{
				if(activeChar.isInParty())
				{
					activeChar.getParty().broadCast(cs);
					break;
				}
				break;
			}
			case PARTY_ROOM:
			{
				MatchingRoom room = activeChar.getMatchingRoom();
				if(room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
					return;
				for(Player roomMember : room.getPlayers())
					if(activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				break;
			}
			case COMMANDCHANNEL_ALL:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
				{
					activeChar.getParty().getCommandChannel().broadCast(cs);
					break;
				}
				activeChar.sendPacket(SystemMsg.ONLY_THE_COMMAND_CHANNEL_CREATOR_CAN_USE_THE_RAID_LEADER_TEXT);
				break;
			}
			case COMMANDCHANNEL_COMMANDER:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
				{
					activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
					break;
				}

				activeChar.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
				break;
			}
			case HERO_VOICE:
			{
				if(!activeChar.isHero() && !activeChar.isCustomHero())
					return;

				if(!activeChar.getPlayerAccess().CanAnnounce && !activeChar.getAntiFlood().canHero(_text))
					return;

				ChatUtils.announce(activeChar, cs, isSpam);
				activeChar.sendPacket(cs);
				break;
			}
			case PETITION_PLAYER:
			case PETITION_GM:
			{
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT));
					return;
				}
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			}
			case BATTLEFIELD:
			{
				if(activeChar.getBattlefieldChatId() == 0)
					return;
				for(Player player : GameObjectsStorage.getPlayers())
					if(!player.getBlockList().contains(activeChar) && !player.isBlockAll() && activeChar.canTalkWith(player) && player.getBattlefieldChatId() == activeChar.getBattlefieldChatId())
						player.sendPacket(cs);
				break;
			}
			case MPCC_ROOM:
			{
				MatchingRoom mpccRoom = activeChar.getMatchingRoom();
				if(mpccRoom == null || mpccRoom.getType() != MatchingRoom.CC_MATCHING)
					return;
				for(Player roomMember2 : mpccRoom.getPlayers())
					if(activeChar.canTalkWith(roomMember2))
						roomMember2.sendPacket(cs);
				break;
			}
			case FRACTION_WORLD:
			{
				if(!Config.ALLOW_FRACTION_WORLD_CHAT)
					return;

				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.hasClubCard()){
					activeChar.sendMessage(new CustomMessage("clubcard.s1"));
					return;
				}

				if(!activeChar.getAntiFlood().canShout(_text))
					return;

				long l = AntiFloodService.getInstance().canSay(_type, activeChar);
				if(l > 0) {
					activeChar.sendMessage(new CustomMessage("clubcard.s2").addNumber(Duration.ofMillis(l).toSeconds()));
					return;
				}
/*
				if(activeChar.hasPremiumAccount())
				{
					if(activeChar.getLevel() < Config.WORLD_CHAT_USE_MIN_LEVEL_PA)
					{
						activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_CAN_USE_THE_WORLD_CHAT_WITH_S1_LEVEL).addNumber(Config.WORLD_CHAT_USE_MIN_LEVEL_PA));
						return;
					}
				}
				else if(activeChar.getLevel() < Config.WORLD_CHAT_USE_MIN_LEVEL)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_CAN_USE_THE_WORLD_CHAT_WITH_S1_LEVEL).addNumber(Config.WORLD_CHAT_USE_MIN_LEVEL));
					return;
				}

				*//*if(activeChar.getWorldChatPoints() <= 0)
				{
					activeChar.sendPacket(SystemMsg.TODAY_YOU_REACHED_THE_LIMIT_OF_USE_OF_THE_WORLD_CHAT__RESET_OF_THE_WORLD_USE_CHAT_IS_DONE_DAILY_AT_6_30_AM);
					return;
				}*/

				//ChatUtils.fractionalAnnounce(activeChar, cs, isSpam);
				ChatUtils.globalAnnounce(activeChar, cs, isSpam);
				activeChar.sendPacket(cs);
				//activeChar.setUsedWorldChatPoints(activeChar.getUsedWorldChatPoints() + 1);
				break;
			}
			default:
			{
				_log.warn("Character " + activeChar.getName() + " used unknown chat type: " + _type.ordinal() + ".");
				break;
			}
		}
	}
}
