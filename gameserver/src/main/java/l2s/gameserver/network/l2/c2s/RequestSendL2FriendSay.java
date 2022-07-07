package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ChatLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2FriendSayPacket;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;

public class RequestSendL2FriendSay extends L2GameClientPacket
{
	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		_message = readS(2048);
		_reciever = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(PunishmentService.INSTANCE.isPunished(PunishmentType.CHAT, String.valueOf(activeChar.getObjectId())))
		{
			activeChar.sendPacket(SystemMsg.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER);
			return;
		}

		Player targetPlayer = GameObjectsStorage.getPlayer(_reciever);
		if(targetPlayer == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}
		if(targetPlayer.isBlockAll())
		{
			activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			return;
		}
		if(!activeChar.getFriendList().contains(targetPlayer.getObjectId()))
			return;
		if(activeChar.canTalkWith(targetPlayer))
		{
			targetPlayer.sendPacket(new L2FriendSayPacket(activeChar.getName(), _reciever, _message));

			ChatLogMessage message = new ChatLogMessage(ChatType.FRIENDTELL, activeChar, targetPlayer, _message);
			LogService.getInstance().log(LoggerType.CHAT, message);
		}
	}
}
