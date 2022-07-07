package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestPartyType;
import l2s.gameserver.model.quest.QuestRepeatType;
import l2s.gameserver.model.quest.QuestState;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author KRonst 08.02.2018
 */

public class _10608_MyLittleFriend extends Quest{

    //NPC
    private static final int QUEST_MANAGER = 40012;
    private static final int DUMMY = 40011;
    private static final int[] GC_MONSTERS = {20654, 20656, 22671, 22672, 22673, 22674, 22676}; //Giants Cave

    //ITEMS
    private static final int DUMMY_PROOF = 75063;
    private static final int MONSTER_PROOF = 75057;

    private static final int [][] Items = {
            {DUMMY_PROOF, 10},
            {MONSTER_PROOF, 50}
    };

    public _10608_MyLittleFriend()
    {
        super(QuestPartyType.PARTY_ONE, QuestRepeatType.ONETIME);
        addStartNpc(QUEST_MANAGER);
        addQuestItem(DUMMY_PROOF, MONSTER_PROOF);
        addKillId(DUMMY);
        addKillId(GC_MONSTERS);
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        if(st.isCompleted())
            return "noquest";
        String htmltext = "noquest";
        int cond = st.getCond();
        if (npc.getNpcId() == QUEST_MANAGER){
            if (cond == 0)
                htmltext = "qm_10608_01.htm";
            else if (cond == 1)
                htmltext = "qm_10608_03.htm";
            else if (cond == 2)
                htmltext = "10608_done.htm";
        }

        return htmltext;
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc){
        if ("qm_10608_02.htm".equalsIgnoreCase(event)){
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
        }
        else if ("check_items".equalsIgnoreCase(event)){
            if (CheckIngredients(st, Items) && (st.getPlayer().getAdena() >= 100)) {
                event = "10608_done.htm";
                TakeIngredients(st, Items);
                st.takeItems(ADENA_ID, 100);
                st.setCond(2);
            }
            else event = "10608_not_done.htm";
        }
        else if ("reward_1".equalsIgnoreCase(event)){
            event = "qm_10608_04.htm";
            st.giveItems(29079, 1); //Agathion - Cancer
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_2".equalsIgnoreCase(event)){
            event = "qm_10608_04.htm";
            st.giveItems(29075, 1); //Agathion - Pisces
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_3".equalsIgnoreCase(event)){
            event = "qm_10608_04.htm";
            st.giveItems(29587, 1); //Agathion - Singer & Dancer
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_4".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(20662, 1); //Agathion - Uthanka
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        } else if ("reward_5".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(70406, 1); //Agathion - Phoenix
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        } else if ("reward_6".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(70401, 1); //Agathion - One-eyed Bat Drove
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        } else if ("reward_7".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(70402, 1); //Agathion - Zakens Spirit Swords
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        } else if ("reward_8".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(70405, 1); //Agathion - God of Fortune
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        } else if ("reward_9".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(70404, 1); //Agathion - Guangong
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        } else if ("reward_10".equalsIgnoreCase(event)) {
            event = "qm_10608_04.htm";
            st.giveItems(70411, 1); //Agathion - Miss Chipao
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        return event;
    }

    @Override
    public String onKill(NpcInstance npc, QuestState st) {
        if (st.getCond() == 1){
            if (npc.getNpcId() == DUMMY)
                if (st.getQuestItemsCount(DUMMY_PROOF) < 10)
                    st.giveItems(DUMMY_PROOF, 1);
            if (ArrayUtils.contains(GC_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(MONSTER_PROOF) < 50)
                    st.giveItems(MONSTER_PROOF, 1);
        }
        return null;
    }

    private static boolean CheckIngredients(QuestState st, int [][] item_list)
    {
        for (int[] _item : item_list)
            if(st.getQuestItemsCount(_item[0]) < _item[1])
                return false;
        return true;
    }

    private static void TakeIngredients(QuestState st, int[][] item_list)
    {
        for(int[] _item : item_list)
            st.takeItems(_item[0], _item[1]);
    }
}
