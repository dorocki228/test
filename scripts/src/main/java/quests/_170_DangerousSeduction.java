package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _170_DangerousSeduction extends Quest
{
	//NPC
	private static final int Vellior = 30305;
	//Quest Items
	private static final int NightmareCrystal = 1046;
	//MOB
	private static final int Merkenis = 27022;

	//REWARD
	private static final int BONE_ARMOR = 25;

	public _170_DangerousSeduction()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Vellior);
		addTalkId(Vellior);
		addKillId(Merkenis);
		addQuestItem(NightmareCrystal);

		addLevelCheck("30305-02.htm", 21, 26);
		addRaceCheck("30305-00.htm", Race.DARKELF);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
        if("30305-04.htm".equalsIgnoreCase(event))
		{
			st.setCond(1);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();

		switch(npcId)
		{
			case Vellior:
				if(cond == 0)
					htmltext = "30305-03.htm";
				else if(cond == 1)
					htmltext = "30305-05.htm";
				else if(cond == 2)
				{
					st.takeItems(NightmareCrystal, -1);
					st.giveItems(BONE_ARMOR, 1);
					htmltext = "30305-06.htm";
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
		int cond = st.getCond();
		if(cond == 1 && npcId == Merkenis)
		{
			if(st.getQuestItemsCount(NightmareCrystal) == 0)
				st.giveItems(NightmareCrystal, 1);
			st.setCond(2);
		}
		return null;
	}
}