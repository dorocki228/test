package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public class _355_FamilyHonor extends Quest
{
	//NPC
	private static final int GALIBREDO = 30181;

	//CHANCES
	private static final int CHANCE_FOR_GALFREDOS_BUST = 80;

	//ITEMS
	private static final int GALFREDOS_BUST = 4252;

	public _355_FamilyHonor()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(GALIBREDO);

		for(int mob = 20767; mob <= 20770; mob++)
			addKillId(mob);

		addLevelCheck("galicbredo_q0355_01.htm", 36, 49);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
        if("galicbredo_q0355_04.htm".equals(event))
			st.setCond(1);
		else if("galicbredo_q0355_09.htm".equals(event))
		{
			st.playSound(SOUND_FINISH);
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case GALIBREDO:
				if(cond == 0)
					htmltext = "galicbredo_q0355_02.htm";
				else if(cond == 1)
				{
					long count = st.getQuestItemsCount(GALFREDOS_BUST);
					if(count > 0)
					{
						st.takeItems(GALFREDOS_BUST, -1);
						st.giveItems(ADENA_ID, count * 20);
						htmltext = "galicbredo_q0355_07a.htm";
					}
					else
						htmltext = "galicbredo_q0355_08.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId >= 20767 && npcId <= 20770)
			if(cond == 1)
				st.rollAndGive(GALFREDOS_BUST, 1, 1, CHANCE_FOR_GALFREDOS_BUST);

		return null;
	}
}