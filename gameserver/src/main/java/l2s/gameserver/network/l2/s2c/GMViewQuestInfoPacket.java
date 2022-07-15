package l2s.gameserver.network.l2.s2c;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.OutgoingPackets;

public class GMViewQuestInfoPacket implements IClientOutgoingPacket
{
	private final String _characterName;
	private final TIntIntMap _quests = new TIntIntHashMap();

	public GMViewQuestInfoPacket(Player targetCharacter)
	{
		_characterName = targetCharacter.getName();
		for(QuestState quest : targetCharacter.getAllQuestsStates())
		{
			if(quest.getQuest().isVisible(targetCharacter) && quest.isStarted())
				_quests.put(quest.getQuest().getId(), quest.getCondsMask());
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GM_VIEW_QUEST_INFO.writeId(packetWriter);
		packetWriter.writeS(_characterName);
		packetWriter.writeH(_quests.size());
		for(TIntIntIterator iterator = _quests.iterator(); iterator.hasNext();)
		{
			iterator.advance();

			packetWriter.writeD(iterator.key());
			packetWriter.writeD(iterator.value());
		}
		packetWriter.writeH(0); //количество элементов типа: ddQd , как-то связано с предметами

		return true;
	}
}