package handler.bbs.custom;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;

import java.util.StringTokenizer;

public class CommunityBeta extends CustomCommunityHandler
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

    @Override
    public void onInit()
    {
        if (!Config.BETA_SERVER) {
            return;
        }

        super.onInit();

		_log.atInfo().log( "CommunityBoard: Beta service loaded." );
    }

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"cbbsobt"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		if(!Config.BETA_SERVER)
		{
			player.sendMessage(player.isLangRus() ? "Данная функция не доступна." : "This feature is not available.");
			player.sendPacket(ShowBoardPacket.CLOSE);
			return;
		}

		StringTokenizer st = new StringTokenizer(bypass, ":");
		String cmd = st.nextToken();
		String html = "";

		if("cbbsobt".equals(cmd))
		{
			if(!st.hasMoreTokens())
			{
				player.sendMessage(player.isLangRus() ? "Данная функция еще не реализована." : "This feature is not yet implemented.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			String cmd2 = st.nextToken();
			if("level_up".equals(cmd2))
			{
				setLevel(player, player.getLevel() + 1);
			}
			else if("level_down".equals(cmd2))
			{
				setLevel(player, player.getLevel() - 1);
			}
			else if("add_col".equals(cmd2))
			{
				ItemFunctions.addItem(player, 4037, 10000);
			}
			else if("add_adena".equals(cmd2))
			{
				ItemFunctions.addItem(player, ItemTemplate.ITEM_ID_ADENA, 100_000_000L);
			}
			else if("add_skill_points".equals(cmd2))
			{
				player.addExpAndSp(0, 100_000_000L);
			}

			IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler("_bbspage:betamanager");
			if(handler != null)
				handler.onBypassCommand(player, "_bbspage:betamanager");
		}
	}
	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

	private static void setLevel(GameObject target, int level) {
		if (target == null || !target.isPlayer()) {
			return;
		}

		Player player = target.getPlayer();
		level = Math.min(level, player.getMaxLevel() + 1);
		level = Math.max(level, 1);

		long expAdd = Experience.getExpForLevel(level) - player.getExp();
		player.addExpAndSp(expAdd, 0, true);
	}
}