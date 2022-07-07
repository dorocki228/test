package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	private int _number;

	public RequestTutorialQuestionMark()
	{
		_number = 0;
	}

	@Override
	protected void readImpl()
	{
		_number = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		for(QuestState qs : player.getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("QM", String.valueOf(_number), qs);
	}
}
