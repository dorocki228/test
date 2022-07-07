package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _156_MillenniumLove extends Quest
{
	int LILITHS_LETTER = 1022;
	int THEONS_DIARY = 1023;
	int ROUNDSHIELD = 102;

	public _156_MillenniumLove()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30368);

		addTalkId(30368);
		addTalkId(30368);
		addTalkId(30368);
		addTalkId(30369);

		addQuestItem(LILITHS_LETTER, THEONS_DIARY);

		addLevelCheck("30368-05.htm", 15, 19);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if("30368-06.htm".equals(event))
		{
			st.giveItems(LILITHS_LETTER, 1);
			st.setCond(1);
		}
		else if("156_1".equals(event))
		{
			st.takeItems(LILITHS_LETTER, -1);
			if(st.getQuestItemsCount(THEONS_DIARY) == 0)
			{
				st.giveItems(THEONS_DIARY, 1);
				st.setCond(2);
			}
			htmltext = "30369-03.htm";
		}
		else if("156_2".equals(event))
		{
			st.takeItems(LILITHS_LETTER, -1);
			htmltext = "30369-04.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		switch(npcId)
		{
			case 30368:
				if(cond == 0)
					htmltext = "30368-02.htm";
				else if(cond == 1)
					htmltext = "30368-07.htm";
				else if(cond == 2)
				{
					st.takeItems(THEONS_DIARY, -1);
					st.giveItems(ROUNDSHIELD, 1);
					htmltext = "30368-08.htm";
					st.finishQuest();
				}
				break;

			case 30369:
				if(cond == 1)
					htmltext = "30369-02.htm";
				else if(cond == 2)
					htmltext = "30369-05.htm";
				break;
		}
		return htmltext;
	}
}