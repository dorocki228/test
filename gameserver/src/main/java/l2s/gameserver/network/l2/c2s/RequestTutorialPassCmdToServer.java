package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	private String _bypass;

	public RequestTutorialPassCmdToServer()
	{
		_bypass = null;
	}

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		for(QuestState qs : player.getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("BYPASS", _bypass, qs);
	}
}
