package l2s.gameserver.network.l2.c2s;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.utils.BypassStorage.ValidBypass;
import l2s.gameserver.utils.Util;

/**
 *
 * @author n0nam3
 * @date 22/08/2010 15:16
 */

public class RequestLinkHtml implements IClientIncomingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	//Format: cS
	private String _link;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_link = packet.readS();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		ValidBypass bp = player.getBypassStorage().validate(_link);
		if(bp == null)
		{
			_log.atWarning().log( " RequestLinkHtml: Unexpected link : %s!", _link );
			return;
		}

		String link = bp.bypass;
		int itemId = 0;

		String[] params = link.split(".htm#");
		if(params.length >= 2)
		{
			link = params[0] + ".htm";
			itemId = !Util.isDigit(params[1]) ? -1 : Integer.parseInt(params[1]);
		}

		if(link.contains("..") || !link.endsWith(".htm") || itemId == -1)
		{
			_log.atWarning().log( "RequestLinkHtml: hack? link contains prohibited characters: \'%s\'!", link );
			return;
		}

		HtmlMessage msg;
		if(itemId == 0)
		{
			NpcInstance npc = player.getLastNpc();
			if(npc != null)
			{
				if(!player.checkInteractionDistance(npc))
					return;

				link = npc.correctBypassLink(player, link);

				msg = new HtmlMessage(npc);
			}
			else
				msg = new HtmlMessage(0);
		}
		else
			msg = new HtmlMessage(0).setItemId(itemId);

		msg.setFile(link);
		player.sendPacket(msg);
	}
}