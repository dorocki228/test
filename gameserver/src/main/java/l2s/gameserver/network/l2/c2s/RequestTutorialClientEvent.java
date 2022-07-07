package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	private int _event;

	public RequestTutorialClientEvent()
	{
		_event = 0;
	}

	@Override
	protected void readImpl()
	{
		_event = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		for(QuestState qs : player.getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("CE", String.valueOf(_event), qs);
	}
}
