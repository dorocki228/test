package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.time.GameTimeService;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS;
	private static final NumberFormat df;
	private static final SimpleDateFormat sf;

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;
		int h = GameTimeService.INSTANCE.getGameHour();
		int m = GameTimeService.INSTANCE.getGameMin();
		SystemMessagePacket sm;
		if(GameTimeService.INSTANCE.isNowNight())
			sm = new SystemMessagePacket(SystemMsg.THE_CURRENT_TIME_IS_S1S2_);
		else
			sm = new SystemMessagePacket(SystemMsg.THE_CURRENT_TIME_IS_S1S2);
		sm.addString(df.format(h)).addString(df.format(m));
		activeChar.sendPacket(sm);
		if(Config.ALT_SHOW_SERVER_TIME)
			activeChar.sendMessage(new CustomMessage("usercommandhandlers.Time.ServerTime").addString(sf.format(new Date(System.currentTimeMillis()))));
		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	static
	{
		COMMAND_IDS = new int[] { 77 };
		df = NumberFormat.getInstance(Locale.ENGLISH);
		sf = new SimpleDateFormat("H:mm");
		df.setMinimumIntegerDigits(2);
	}
}
