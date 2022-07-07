package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	private int _unk;
	private String _bypass;

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		for(QuestState qs : player.getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("LINK", _bypass, qs);
	}
}
