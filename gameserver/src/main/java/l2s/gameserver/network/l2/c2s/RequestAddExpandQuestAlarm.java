package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.ExQuestNpcLogList;

/**
 * @author VISTALL
 * @date 14:47/26.02.2011
 */
public class RequestAddExpandQuestAlarm implements IClientIncomingPacket
{
	private int _questId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_questId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client) throws Exception
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		Quest quest = QuestHolder.getInstance().getQuest(_questId);
		if(quest == null)
			return;

		QuestState state = player.getQuestState(quest);
		if(state == null)
			return;

		player.sendPacket(new ExQuestNpcLogList(state));
	}
}
