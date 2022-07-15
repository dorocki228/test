package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.base.ClassId;

public class _914_RequestFromTheRedLibraGuildLv5 extends Quest
{
	public final int SINEY = 34214;
	public final int VEPR = 27330;
	public static final String A_LIST = "a_list";
	public _914_RequestFromTheRedLibraGuildLv5()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(SINEY);
		addKillId(VEPR);
		addKillNpcWithLog(1, 91405, A_LIST, 50, VEPR);
		
		addLevelCheck("nolvl.htm", 79);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("siney02.htm"))
			{
				st.setCond(1);
			}		
		else if(event.equalsIgnoreCase("end.htm"))
			{
				st.addExpAndSp(300000, 10000);
				st.giveItems(46642, 3);
				st.giveItems(46647, 2);
				st.finishQuest();
			}
			return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		

		if(npcId == SINEY) {
			if(cond == 0)
				{
						htmltext = "siney01.htm";
				}
			if(cond ==1){
						htmltext = "siney02.htm";
				}	
			if(cond ==2){
						htmltext = "siney03.htm";
				}
				 
					 }
			return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		
		if(qs.getCond() == 1)
		{
		boolean doneKill = updateKill(npc, qs);
		if(doneKill)
		{
			qs.unset(A_LIST);
			qs.setCond(2);
		}}
			
		return null;
	}
}