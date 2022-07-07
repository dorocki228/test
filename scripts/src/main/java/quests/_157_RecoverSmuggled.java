package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _157_RecoverSmuggled extends Quest
{
	int ADAMANTITE_ORE_ID = 1024;
	int BUCKLER = 49042;

	public _157_RecoverSmuggled()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30005);
		addTalkId(30005);
		addKillId(20121);
		addQuestItem(ADAMANTITE_ORE_ID);
		addLevelCheck("30005-02.htm", 5, 9);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if("1".equals(event))
		{
			st.set("id", "0");
			st.setCond(1);
			htmltext = "30005-05.htm";
		}
		else if("157_1".equals(event))
		{
			htmltext = "30005-04.htm";
			return htmltext;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case 30005:
				if(cond == 0)
					htmltext = "30005-03.htm";
				else if(cond == 1)
					htmltext = "30005-06.htm";
				else if(cond == 2)
				{
					st.takeItems(ADAMANTITE_ORE_ID, st.getQuestItemsCount(ADAMANTITE_ORE_ID));
					st.giveItems(BUCKLER, 1);
					htmltext = "30005-07.htm";
					st.finishQuest();
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 20121)
		{
			st.set("id", "0");
			if(st.getCond() != 0 && st.getQuestItemsCount(ADAMANTITE_ORE_ID) < 20 && Rnd.chance(14))
			{
				st.giveItems(ADAMANTITE_ORE_ID, 1, true);
				if(st.getQuestItemsCount(ADAMANTITE_ORE_ID) >= 20)
					st.setCond(2);
				else
					st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}