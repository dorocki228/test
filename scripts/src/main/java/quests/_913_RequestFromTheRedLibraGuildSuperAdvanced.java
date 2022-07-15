package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author SanyaDC
 */
public class _913_RequestFromTheRedLibraGuildSuperAdvanced extends Quest
{
	public final int SINEY = 34214;
	public final int VEPR = 27207;	
	public static final String A_LIST = "a_list";
	
	public _913_RequestFromTheRedLibraGuildSuperAdvanced()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(SINEY);		
		addKillId(VEPR);
		addKillNpcWithLog(1, 91305, A_LIST, 50, VEPR);
		
		addLevelCheck("nolvl.htm", 61);	
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
						 			
				st.addExpAndSp(440000, 13000);
				st.giveItems(46642, 2);
				st.giveItems(46647, 1);				
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