package l2s.gameserver.network.l2.s2c;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestRepeatType;
import l2s.gameserver.model.quest.QuestState;

public class QuestListPacket extends L2GameServerPacket
{
	private static final byte[] _completedQuestsMask;
	private final TIntIntMap _quests;

	public QuestListPacket(Player player)
	{
		_quests = new TIntIntHashMap();
		for(QuestState quest : player.getAllQuestsStates())
			if(quest.getQuest().isVisible(player) && quest.isStarted())
				_quests.put(quest.getQuest().getId(), quest.getCondsMask());
			else if(quest.isCompleted() && quest.getQuest().getRepeatType() == QuestRepeatType.ONETIME)
			{
				int questId = quest.getQuest().getId();
				if(questId >= 10000)
					questId -= 10000;
				int byteIndex = questId / 8;
				byte[] completedQuestsMask = _completedQuestsMask;
				int n = byteIndex;
				completedQuestsMask[n] |= (byte) (1 << questId - byteIndex * 8);
			}
	}

	@Override
	protected final void writeImpl()
	{
        writeH(_quests.size());
		TIntIntIterator iterator = _quests.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
            writeD(iterator.key());
            writeD(iterator.value());
		}
		writeB(_completedQuestsMask);
	}

	static
	{
		_completedQuestsMask = new byte[128];
	}
}
