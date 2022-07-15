package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

/**
 * @author SanyaDC
 */
public class _910_RequestFromTheRedLibraGuildBasic extends Quest
{
	public final int SINEY = 34214;
	public final int UPI = 27204;	
	public static final String A_LIST = "a_list";
	
	public _910_RequestFromTheRedLibraGuildBasic()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(SINEY);		
		addKillId(UPI);
		addKillNpcWithLog(1, 91005, A_LIST, 50, UPI);
		
		addLevelCheck("nolvl.htm", 1, 20);	
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
						 			
				st.addExpAndSp(30000, 1000);
				st.giveItems(46642, 1);
				st.giveItems(46644, 1);				
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