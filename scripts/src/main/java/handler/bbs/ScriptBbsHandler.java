package handler.bbs;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;

/**
 * @author VISTALL
 * @date 2:17/19.08.2011
 */
public abstract class ScriptBbsHandler implements OnInitScriptListener, IBbsHandler
{
	@Override
	public void onInit()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			BbsHandlerHolder.getInstance().registerHandler(this);
	}
}
