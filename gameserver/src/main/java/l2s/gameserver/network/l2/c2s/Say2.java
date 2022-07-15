package l2s.gameserver.network.l2.c2s;

import com.google.common.flogger.FluentLogger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2s.commons.ban.BanBindType;
import l2s.commons.ban.BanInfo;
import l2s.gameserver.Config;
import l2s.gameserver.cache.ItemInfoCache;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.GameBanManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.*;
import l2s.gameserver.utils.loggers.ChatLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Say2 implements IClientIncomingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	/** RegExp для кэширования ссылок на предметы, пример ссылки: \b\tType=1 \tID=268484598 \tColor=0 \tUnderline=0 \tTitle=\u001BAdena\u001B\b */
	private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tClassID=[0-9]+[\\s]+\tTitle=\u001B(.[^\u001B]*)[^\b]");
	private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");

	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_text = packet.readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = l2s.commons.lang.ArrayUtils.valid(ChatType.VALUES, packet.readD());
		_target = _type == ChatType.TELL ? packet.readS(Config.CNAME_MAXLEN) : null;
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		writeToChat(activeChar, _text, _type, _target);
	}

	public static void writeToChat(Player activeChar, String text, ChatType type, String target)
	{
		if(type == null || text == null || text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		text = text.replaceAll("\\\\n", "\n");

		if(text.contains("\n"))
		{
			String[] lines = text.split("\n");
			text = StringUtils.EMPTY;
			for(int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(text.length() > 0)
					text += "\n  >";
				text += lines[i];
			}
		}

		if(text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if(Config.BAN_FOR_CFG_USAGE)
			if(text.startsWith("//cfg") || text.startsWith("///cfg") || text.startsWith("////cfg"))
				activeChar.kick();
		
		if(text.startsWith("."))
		{
			if(Config.ALLOW_VOICED_COMMANDS)
			{
				String fullcmd = text.substring(1).trim();
				String command = fullcmd.split("\\s+")[0];
				String args = fullcmd.substring(command.length()).trim();

				if(command.length() > 0)
				{
					// then check for VoicedCommands
					IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					if(vch != null)
					{
						vch.useVoicedCommand(command, activeChar, args);
						return;
					}
				}
				activeChar.sendMessage(new CustomMessage("common.command404"));
				return;
			}
		}

		if(activeChar.isChatBlocked())
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_A_CONTACT_WHILE_A_CHATTING_BLOCK_IS_IMPOSED);
			return;
		}

		boolean globalchat = type != ChatType.ALLIANCE && type != ChatType.CLAN && type != ChatType.PARTY;
		if(globalchat || ArrayUtils.contains(Config.BAN_CHANNEL_LIST, type.ordinal())) {
			BanInfo banInfo = GameBanManager.getInstance().getBanInfoIfBanned(BanBindType.CHAT, activeChar.getObjectId());
			if (banInfo != null) {
				if (banInfo.getEndTime() == - 1)
					activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently"));
				else
					activeChar.sendMessage(new CustomMessage("common.ChatBanned").addString(TimeUtils.toSimpleFormat(banInfo.getEndTime() * 1000L)));
				activeChar.sendActionFailed();
				return;
			}

			if (activeChar.getNoChannel() != 0L) {
				if (activeChar.getNoChannelRemained() > 0L || activeChar.getNoChannel() < 0L) {
					if (activeChar.getNoChannel() > 0L)
						activeChar.sendMessage(new CustomMessage("common.ChatBanned").addString(TimeUtils.toSimpleFormat(System.currentTimeMillis() + activeChar.getNoChannelRemained())));
					else
						activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently"));
					activeChar.sendActionFailed();
					return;
				}
				activeChar.updateNoChannel(0L);
			}
		}

		if(globalchat)
		{
			if(Config.ABUSEWORD_REPLACE)
				text = Config.replaceAbuseWords(text, Config.ABUSEWORD_REPLACE_STRING);
			else if(Config.ABUSEWORD_BANCHAT && Config.containsAbuseWord(text))
			{
				activeChar.sendMessage(new CustomMessage("common.ChatBanned").addNumber(Config.ABUSEWORD_BANTIME * 60));
				Log.add(activeChar + ": " + text, "abuse");
				activeChar.updateNoChannel(Config.ABUSEWORD_BANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}
		}

		// Кэширование линков предметов
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(text);
		ItemInstance item;
		int objectId;

		while(m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);

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
			//Исключаем из транслитерации ссылки на предметы
			m = SKIP_ITEM_LINK_PATTERN.matcher(text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				sb.append(Strings.fromTranslit(text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
				sb.append(text.substring(end, end = m.end()));
			}

			text = sb.append(Strings.fromTranslit(text.substring(end, text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}

		ChatLogger.INSTANCE.log(type, activeChar, target, text);

		SayPacket2 cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), text);

		switch(type)
		{
			case TELL:
				Player receiver = World.getPlayer(target);
				if(receiver != null && receiver.isInOfflineMode())
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(target), ActionFailPacket.STATIC);
				else if(receiver != null && !receiver.getBlockList().contains(activeChar) && !receiver.isBlockAll())
				{
					if(!receiver.getMessageRefusal())
					{
						if(!activeChar.getAntiFlood().canTell(receiver.getObjectId(), text))
							return;

						if(activeChar.canTalkWith(receiver))
						{
							cs.setSenderInfo(activeChar, receiver);
							if(receiver.isFakePlayer())
								receiver.getListeners().onChatMessageReceive(type, activeChar.getName(), text);
							else
								receiver.sendPacket(cs);
							receiver.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
						}

						cs = new SayPacket2(activeChar.getObjectId(), type, "->" + receiver.getName(), text);
						cs.setSenderInfo(activeChar, receiver);
						activeChar.sendPacket(cs);
						activeChar.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
					}
					else
						activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
				}
				else if(receiver == null)
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(target), ActionFailPacket.STATIC);
				else
					activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT, ActionFailPacket.STATIC);
				break;
			case SHOUT:
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.getAntiFlood().canShout(text))
					return;

				if(Config.GLOBAL_SHOUT)
					ChatUtils.announce(activeChar, cs);
				else
					ChatUtils.shout(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case TRADE:
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.getAntiFlood().canTrade(text))
					return;

				if(Config.GLOBAL_TRADE_CHAT)
					ChatUtils.announce(activeChar, cs);
				else
					ChatUtils.shout(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case ALL:
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.getAntiFlood().canAll(text))
					return;

				if(activeChar.isInOlympiadMode())
				{
					OlympiadGame game = activeChar.getOlympiadGame();
					if(game != null)
					{
						ChatUtils.say(activeChar, game.getAllPlayers(), cs);
						break;
					}
				}

				ChatUtils.say(activeChar, cs);

				cs.setCharName(activeChar.getVisibleName(activeChar));

				activeChar.sendPacket(cs);
				break;
			case CLAN:
				if(activeChar.getClan() != null)
					activeChar.getClan().broadcastToOnlineMembers(cs);
				break;
			case ALLIANCE:
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
				break;
			case PARTY:
				if(activeChar.isInParty())
					activeChar.getParty().broadCast(cs);
				break;
			case PARTY_ROOM:
				MatchingRoom room = activeChar.getMatchingRoom();
				if(room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
					return;

				for(Player roomMember : room.getPlayers())
				{
					if(activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				}
				break;
			case COMMANDCHANNEL_ALL:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
					activeChar.getParty().getCommandChannel().broadCast(cs);
				else
					activeChar.sendPacket(SystemMsg.ONLY_THE_COMMAND_CHANNEL_CREATOR_CAN_USE_THE_RAID_LEADER_TEXT);
				break;
			case COMMANDCHANNEL_COMMANDER:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
					activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
				else
					activeChar.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
				break;
			case HERO_VOICE:
				if(!activeChar.isHero() && !activeChar.getPlayerAccess().CanAnnounce)
					return;

				// Ограничение только для героев, гм-мы пускай говорят.
				if(!activeChar.getPlayerAccess().CanAnnounce)
				{
					if(!activeChar.getAntiFlood().canHero(text))
						return;
				}

				ChatUtils.announce(activeChar, cs);

				activeChar.sendPacket(cs);
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT));
					return;
				}

				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
				break;
			case BATTLEFIELD:
				if(activeChar.getBattlefieldChatId() == 0)
					return;

				for(Player player : GameObjectsStorage.getPlayers(false, false))
					if(!player.getBlockList().contains(activeChar) && !player.isBlockAll() && activeChar.canTalkWith(player) && player.getBattlefieldChatId() == activeChar.getBattlefieldChatId())
						player.sendPacket(cs);
				break;
			case MPCC_ROOM:
				MatchingRoom mpccRoom = activeChar.getMatchingRoom();
				if(mpccRoom == null || mpccRoom.getType() != MatchingRoom.CC_MATCHING)
					return;

				for(Player roomMember : mpccRoom.getPlayers())
				{
					if(activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				}
				break;
			case WORLD:
				/*if(text.equals("servershowyourrealonline"))
				{
					int total = GameObjectsStorage.getPlayers(true, true).size();
					int online = GameObjectsStorage.getPlayers(false, false).size();
					int offtrade = GameObjectsStorage.getOfflinePlayers().size();
					activeChar.sendMessage("Online: " + online + ", offtrade: " + offtrade + ", fake: " + (total - online - offtrade));
					return;
				}*/

				if(!Config.ALLOW_WORLD_CHAT)
					return;

				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}

				if(!activeChar.getAntiFlood().canWorld(text))
					return;

				ChatUtils.announce(activeChar, cs);

				activeChar.sendPacket(cs);
				activeChar.setUsedWorldChatPoints(activeChar.getUsedWorldChatPoints() + 1);
				break;
			default:
				_log.atWarning().log( "Character %s used unknown chat type: %s.", activeChar.getName(), type.ordinal() );
		}
	}
}