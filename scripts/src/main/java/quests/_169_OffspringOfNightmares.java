package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _169_OffspringOfNightmares extends Quest
{
	//NPC
	private static final int Vlasty = 30145;
	//QuestItem
	private static final int CrackedSkull = 1030;
	private static final int PerfectSkull = 1031;
	//Item
	private static final int BoneGaiters = 31;
	//MOB
	private static final int DarkHorror = 20105;
	private static final int LesserDarkHorror = 20025;

	public _169_OffspringOfNightmares()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Vlasty);

		addTalkId(Vlasty);

		addKillId(DarkHorror);
		addKillId(LesserDarkHorror);

		addQuestItem(CrackedSkull, PerfectSkull);

		addLevelCheck("30145-02.htm", 15, 20);
		addRaceCheck("30145-00.htm", Race.DARKELF);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
        if("30145-04.htm".equalsIgnoreCase(event))
		{
			st.setCond(1);
		}
		else if("30145-08.htm".equalsIgnoreCase(event))
		{
			st.takeItems(PerfectSkull, -1);
			st.giveItems(BoneGaiters, 1);
			st.giveItems(ADENA_ID, 3000);
			if(st.haveQuestItem(CrackedSkull))
				st.giveItems(ADENA_ID, st.getQuestItemsCount(CrackedSkull) * 10, 5000);
			st.finishQuest();
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
			case Vlasty:
				if(cond == 0)
					htmltext = "30145-03.htm";
				else if(cond == 1)
				{
					if(st.getQuestItemsCount(CrackedSkull) == 0)
						htmltext = "30145-05.htm";
					else
						htmltext = "30145-06.htm";
				}
				else if(cond == 2)
					htmltext = "30145-07.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(Rnd.chance(10) && st.getQuestItemsCount(PerfectSkull) == 0)
			{
				st.giveItems(PerfectSkull, 1);
				st.setCond(2);
			}
			if(Rnd.chance(70))
			{
				st.giveItems(CrackedSkull, 1, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}