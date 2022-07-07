package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestPartyType;
import l2s.gameserver.model.quest.QuestRepeatType;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author KRonst 08.02.2018
 */

public class _10606_CPIsNotSuperflous extends Quest
{
    //NPCS
    private static final int QUEST_MANAGER = 40012;
    private static final int[] MONSTERS = {20761, 20625, 20626, 20627, 21087, 21090, 21091}; //loc: Antharas Lair


    //ITEMS
    private static final int PROOF = 75059; //
    private static final int CP_POTION = 75038;

    public _10606_CPIsNotSuperflous()
    {
        super(QuestPartyType.PARTY_ONE, QuestRepeatType.REPEATABLE);
        addStartNpc(QUEST_MANAGER);
        addKillId(MONSTERS);
        addQuestItem(PROOF);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc)
    {
        if("qm_10606_02.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
        }
        else if("qm_10606_04.htm".equalsIgnoreCase(event)) {
            st.giveItems(CP_POTION, 2);
            st.playSound(SOUND_FINISH);
            st.finishQuest();
        }
        return event;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st)
    {
        String htmltext = "noquest";
        int cond = st.getCond();
        if(npc.getNpcId() == QUEST_MANAGER) {
            if(cond == 0)
                htmltext = "qm_10606_01.htm";
            else if (cond == 1)
                htmltext = "10606_not_all.htm";
            else if(cond == 2)
                htmltext = "qm_10606_03.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(NpcInstance npc, QuestState st) {
        if(st.getCond() == 1) {
            st.giveItems(PROOF, 1);
            st.playSound(SOUND_ITEMGET);
            if (st.getQuestItemsCount(PROOF) >= 40){
                st.setCond(2);
                st.playSound(SOUND_MIDDLE);
            }
        }
        return null;
    }
}