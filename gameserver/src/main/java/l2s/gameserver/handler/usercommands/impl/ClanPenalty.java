package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.utils.HtmlUtils;

import java.text.SimpleDateFormat;

public class ClanPenalty implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS;

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;
		long leaveClan = 0L;
		if(activeChar.getLeaveClanTime() != 0L)
			leaveClan = activeChar.getLeaveClanTime() + 86400000L;
		long deleteClan = 0L;
		if(activeChar.getDeleteClanTime() != 0L)
			deleteClan = activeChar.getDeleteClanTime() + 864000000L;
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		String html = HtmCache.getInstance().getHtml("command/penalty.htm", activeChar);
		if(activeChar.getClanId() == 0)
		{
			if(leaveClan == 0L && deleteClan == 0L)
			{
				html = html.replaceFirst("%reason%", new CustomMessage("l2s.gameserver.handler.usercommands.impl.ClanPenalty.PenaltyImposed").toString(activeChar));
				html = html.replaceFirst("%expiration%", " ");
			}
			else if(leaveClan > 0L && deleteClan == 0L)
			{
				html = html.replaceFirst("%reason%", new CustomMessage("l2s.gameserver.handler.usercommands.impl.ClanPenalty.PenaltyLeaving").toString(activeChar));
				html = html.replaceFirst("%expiration%", format.format(leaveClan));
			}
			else if(deleteClan > 0L)
			{
				html = html.replaceFirst("%reason%", new CustomMessage("l2s.gameserver.handler.usercommands.impl.ClanPenalty.PenaltyDissolving").toString(activeChar));
				html = html.replaceFirst("%expiration%", format.format(deleteClan));
			}
		}
		else if(activeChar.getClan().canInvite())
		{
			html = html.replaceFirst("%reason%", new CustomMessage("l2s.gameserver.handler.usercommands.impl.ClanPenalty.PenaltyImposed").toString(activeChar));
			html = html.replaceFirst("%expiration%", " ");
		}
		else
		{
			html = html.replaceFirst("%reason%", new CustomMessage("l2s.gameserver.handler.usercommands.impl.ClanPenalty.PenaltyExpelling").toString(activeChar));
			html = html.replaceFirst("%expiration%", format.format(activeChar.getClan().getExpelledMemberTime()));
		}
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(HtmlUtils.bbParse(html));
		activeChar.sendPacket(msg);
		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	static
	{
		COMMAND_IDS = new int[] { 100, 114 };
	}
}
