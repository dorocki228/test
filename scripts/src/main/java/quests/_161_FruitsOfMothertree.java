package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _161_FruitsOfMothertree extends Quest
{
	private static final int ANDELLRIAS_LETTER_ID = 1036;
	private static final int MOTHERTREE_FRUIT_ID = 1037;

	public _161_FruitsOfMothertree()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30362);
		addTalkId(30371);

		addQuestItem(MOTHERTREE_FRUIT_ID, ANDELLRIAS_LETTER_ID);

		addLevelCheck("30362-02.htm", 3, 7);
		addRaceCheck("30362-00.htm", Race.ELF);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if("1".equals(event))
		{
			htmltext = "30362-04.htm";
			st.giveItems(ANDELLRIAS_LETTER_ID, 1);
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		switch(npcId)
		{
			case 30362:
				if(cond == 0)
					htmltext = "30362-03.htm";
				else if(cond == 1)
					htmltext = "30362-05.htm";
				else if(cond == 2)
				{
					htmltext = "30362-06.htm";
					st.giveItems(ADENA_ID, 100);
					st.takeItems(MOTHERTREE_FRUIT_ID, 1);
					st.finishQuest();
				}
				break;

			case 30371:
				if(cond == 1)
				{
					htmltext = "30371-01.htm";
					st.giveItems(MOTHERTREE_FRUIT_ID, 1);
					st.takeItems(ANDELLRIAS_LETTER_ID, 1);
					st.setCond(2);
				}
				else if(cond == 2)
					htmltext = "30371-02.htm";
				break;
		}
		return htmltext;
	}
}