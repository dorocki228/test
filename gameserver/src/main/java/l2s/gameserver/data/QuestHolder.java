package l2s.gameserver.data;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.quest.Quest;

import java.util.Collection;

public final class QuestHolder extends AbstractHolder
{
	private static final QuestHolder _instance;
	private final TIntObjectMap<Quest> _quests;

	public QuestHolder()
	{
		_quests = new TIntObjectHashMap<>();
	}

	public static QuestHolder getInstance()
	{
		return _instance;
	}

	public Quest getQuest(int id)
	{
		return _quests.get(id);
	}

	public void addQuest(Quest quest)
	{
		if(_quests.containsKey(quest.getId()))
		{
            warn("Cannot added quest (ID[" + quest.getId() + "], CLASS[" + quest.getClass().getSimpleName() + ".java]). Quets with this ID already have!");
			return;
		}
		_quests.put(quest.getId(), quest);
	}

	public Collection<Quest> getQuests()
	{
		return _quests.valueCollection();
	}

	@Override
	public int size()
	{
		return _quests.size();
	}

	@Override
	public void clear()
	{
		_quests.clear();
	}

	static
	{
		_instance = new QuestHolder();
	}
}
