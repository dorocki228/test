package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public final class _306_CrystalOfFireice extends Quest
{
	//NPCs
	private static final int Katerina = 30004;
	//Mobs
	private static final int Salamander = 20109;
	private static final int Undine = 20110;
	private static final int Salamander_Elder = 20112;
	private static final int Undine_Elder = 20113;
	private static final int Salamander_Noble = 20114;
	private static final int Undine_Noble = 20115;
	//Quest Items
	private static final int Flame_Shard = 1020;
	private static final int Ice_Shard = 1021;
	//Chances
	private static final int Chance = 30;
	private static final int Elder_Chance = 40;
	private static final int Noble_Chance = 50;

	public _306_CrystalOfFireice()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Katerina);
		addKillId(Salamander);
		addKillId(Undine);
		addKillId(Salamander_Elder);
		addKillId(Undine_Elder);
		addKillId(Salamander_Noble);
		addKillId(Undine_Noble);
		addQuestItem(Flame_Shard);
		addQuestItem(Ice_Shard);

		addLevelCheck("katrine_q0306_02.htm", 17, 23);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if("katrine_q0306_04.htm".equalsIgnoreCase(event))
		{
			st.setCond(1);
		}
		else if("katrine_q0306_08.htm".equalsIgnoreCase(event))
		{
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
			case Katerina:
				if(cond == 0)
					htmltext = "katrine_q0306_03.htm";
				else if(cond == 1)
				{
					long Shrads_count = st.getQuestItemsCount(Flame_Shard) + st.getQuestItemsCount(Ice_Shard);
					long Reward = Shrads_count * 15;

					if(Reward > 0)
					{
						htmltext = "katrine_q0306_07.htm";
						st.takeItems(Flame_Shard, -1);
						st.takeItems(Ice_Shard, -1);
						st.giveItems(ADENA_ID, Reward, 5000);
					}
					else
						htmltext = "katrine_q0306_05.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		int npcId = npc.getNpcId();
		if(qs.getCond() == 1)
		{
			if(npcId == Salamander || npcId == Undine)
				qs.rollAndGive(npcId == Salamander ? Flame_Shard : Ice_Shard, 1, Chance);
			else if(npcId == Salamander_Elder || npcId == Undine_Elder)
				qs.rollAndGive(npcId == Salamander_Elder ? Flame_Shard : Ice_Shard, 1, Elder_Chance);
			else if(npcId == Salamander_Noble || npcId == Undine_Noble)
				qs.rollAndGive(npcId == Salamander_Noble ? Flame_Shard : Ice_Shard, 1, Noble_Chance);
		}
		return null;
	}
}