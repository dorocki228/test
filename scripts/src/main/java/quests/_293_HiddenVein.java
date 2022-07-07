package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _293_HiddenVein extends Quest
{
	// NPCs
	private static final int Filaur = 30535;
	private static final int Chichirin = 30539;
	// Mobs
	private static final int Utuku_Orc = 20446;
	private static final int Utuku_Orc_Archer = 20447;
	private static final int Utuku_Orc_Grunt = 20448;
	// Quest Items
	private static final int Chrysolite_Ore = 1488;
	private static final int Torn_Map_Fragment = 1489;
	private static final int Hidden_Ore_Map = 1490;
	// Chances
	private static final int Torn_Map_Fragment_Chance = 5;
	private static final int Chrysolite_Ore_Chance = 45;

	public _293_HiddenVein()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Filaur);
		addTalkId(Chichirin);
		addKillId(Utuku_Orc);
		addKillId(Utuku_Orc_Archer);
		addKillId(Utuku_Orc_Grunt);
		addQuestItem(Chrysolite_Ore);
		addQuestItem(Torn_Map_Fragment);
		addQuestItem(Hidden_Ore_Map);

		addLevelCheck("elder_filaur_q0293_01.htm", 6, 15);
		addRaceCheck("elder_filaur_q0293_00.htm", Race.DWARF);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if("elder_filaur_q0293_03.htm".equalsIgnoreCase(event))
		{
			st.setCond(1);
		}
		else if("elder_filaur_q0293_06.htm".equalsIgnoreCase(event))
		{
			st.finishQuest();
		}
		else if("chichirin_q0293_03.htm".equalsIgnoreCase(event))
		{
			st.takeItems(Torn_Map_Fragment, 4);
			st.giveItems(Hidden_Ore_Map, 1);
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
			case Filaur:
				if(cond == 0)
					htmltext = "elder_filaur_q0293_02.htm";
				if(cond == 1)
				{
					long reward = st.getQuestItemsCount(Chrysolite_Ore) * 5 + st.getQuestItemsCount(Hidden_Ore_Map) * 150;

					if(reward == 0)
						htmltext = "elder_filaur_q0293_04.htm";
					else
					{
						if(st.haveQuestItem(Chrysolite_Ore))
						{
							st.takeItems(Chrysolite_Ore, -1);
							htmltext = "elder_filaur_q0293_05.htm";
						}
						if(st.haveQuestItem(Hidden_Ore_Map))
						{
							st.takeItems(Hidden_Ore_Map, -1);
							if(Rnd.chance(50))
								htmltext = "elder_filaur_q0293_08.htm";
							else
								htmltext = "elder_filaur_q0293_09.htm";
						}
						st.giveItems(ADENA_ID, reward, 5000);
					}
				}
				break;

			case Chichirin:
				if(cond == 1)
				{
					if(st.getQuestItemsCount(Torn_Map_Fragment) < 4)
						htmltext = "chichirin_q0293_02.htm";
					else
						htmltext = "chichirin_q0293_01.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() == 1)
		{
			if(Rnd.chance(Torn_Map_Fragment_Chance))
			{
				qs.giveItems(Torn_Map_Fragment, 1, true);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(Rnd.chance(Chrysolite_Ore_Chance))
			{
				qs.giveItems(Chrysolite_Ore, 1, true);
				qs.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}