package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;

public class RequestTutorialQuestionMark implements IClientIncomingPacket
{
	// format: ccd
	private boolean _quest = false;
	private int _tutorialId = 0;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_quest = packet.readC() > 0;
		_tutorialId = packet.readD(); // Type
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		for(QuestState qs : player.getAllQuestsStates())
			qs.getQuest().notifyTutorialEvent("QM", _quest, String.valueOf(_tutorialId), qs);
	}
}