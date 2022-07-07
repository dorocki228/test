package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;
import l2s.gameserver.utils.Language;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();

		if (Config.PRE_REGISTRATION_STAGE) {
			String login = client.getLogin();
			if (!Config.PRE_REGISTRATION_STAGE_WHITE_LIST_ACCOUNTS.contains(login.toLowerCase())) {
				String html = HtmCache.getInstance().getHtml("gve/pre-registration.htm", Language.ENGLISH);
				if (html != null) {
					client.sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.NORMAL_WINDOW, html));
				}
				return;
			}
		}

        client.playerSelected(_charSlot);
	}
}
