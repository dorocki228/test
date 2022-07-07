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

public class _10607_Trinkets extends Quest {

    //NPC
    private static final int QUEST_MANAGER = 40012;
    private static final int[] OF_MONSTERS = {21261, 20574, 21263, 20161, 20575, 20576}; //Outlaw Forest
    private static final int[] SOS_MONSTERS = {20157, 20230, 20232, 20234, 20797, 20796}; //Swamp of Screams
    private static final int[] HS_MONSTERS = {21314, 21318, 21320, 21322, 21323, 21321, 21316}; //Hot Springs
    private static final int[] AC_MONSTERS = {20063, 20436, 20439, 20066, 20061}; //Abandoned Camp
    private static final int[] VOS_MONSTERS = {21544, 21538, 21539, 21537, 21540, 21535, 21533, 21532}; //Valley of Saints
    private static final int[] AL_MONSTERS = {22053, 22054, 22056, 22057, 22059, 22063, 22064, 22075}; //Archaic Laboratory
    private static final int[] KETRA_MONSTERS = {21017, 21019, 21020, 21022, 21258, 21259}; //Ketra Village
    private static final int[] GC_MONSTERS = {20654, 20656, 22671, 22672, 22673, 22674, 22676}; //Giant Cave
    private static final int[] SOL_MONSTERS = {21306, 21309, 21310, 21311, 21650, 21649, 21651}; //Shrine of Loyalty
    private static final int[] ANTHARAS_MONSTERS = {20761, 20625, 20626, 20627, 21087, 21090, 21091}; //Antharas Lair
    private static final int[] IT_MONSTERS = {21400, 21407, 21412, 21419, 21421, 21427, 21430, 21431}; //Imperial Tomb
    private static final int[] ELVEN_MONSTERS = {20021, 20027, 20030, 20033, 20035, 20039, 20049, 20070, 20119, 20142}; //Elven Village
    private static final int[] OREN_MONSTERS = {20162, 20235, 20311, 20347, 20351, 20480, 20494, 20496, 20604, 20667}; //Oren Town

    //ITEMS
    private static final int OF_PROOF = 75050;
    private static final int SOS_PROOF = 75051;
    private static final int HS_PROOF = 75052;
    private static final int AC_PROOF = 75053;
    private static final int VOS_PROOF = 75054;
    private static final int AL_PROOF = 75055;
    private static final int KETRA_PROOF = 75056;
    private static final int GC_PROOF = 75057;
    private static final int SOL_PROOF = 75058;
    private static final int ANTHARAS_PROOF = 75059;
    private static final int IT_PROOF = 75060;
    private static final int ELVEN_PROOF = 75061;
    private static final int OREN_PROOF = 75062;
    private static final int MANA = 75025;
    private static final int MA = 1890;
    private static final int VOP = 1887;

    private static final int[][] Ingredients = {
            {MANA, 1000},
            {MA, 1},
            {VOP, 3}
    };

    private static final int[][] Proofs = {
            {OF_PROOF, 30},
            {SOS_PROOF, 30},
            {HS_PROOF, 30},
            {AC_PROOF, 30},
            {VOS_PROOF, 30},
            {AL_PROOF, 30},
            {KETRA_PROOF, 30},
            {GC_PROOF, 30},
            {SOL_PROOF, 30},
            {ANTHARAS_PROOF, 30},
            {IT_PROOF, 30},
            {ELVEN_PROOF, 30},
            {OREN_PROOF, 30},
    };

    public _10607_Trinkets() {
        super(QuestPartyType.PARTY_ONE, QuestRepeatType.ONETIME);
        addStartNpc(QUEST_MANAGER);
        addQuestItem(OF_PROOF, SOS_PROOF, HS_PROOF, AC_PROOF, VOS_PROOF, AL_PROOF, KETRA_PROOF, GC_PROOF, SOL_PROOF, ANTHARAS_PROOF, IT_PROOF, ELVEN_PROOF, OREN_PROOF);
        addKillId(OF_MONSTERS);
        addKillId(SOS_MONSTERS);
        addKillId(HS_MONSTERS);
        addKillId(AC_MONSTERS);
        addKillId(VOS_MONSTERS);
        addKillId(AL_MONSTERS);
        addKillId(KETRA_MONSTERS);
        addKillId(GC_MONSTERS);
        addKillId(SOL_MONSTERS);
        addKillId(ANTHARAS_MONSTERS);
        addKillId(IT_MONSTERS);
        addKillId(ELVEN_MONSTERS);
        addKillId(OREN_MONSTERS);
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        if (st.isCompleted())
            return "noquest";
        String htmltext = "noquest";
        int cond = st.getCond();
        if (npc.getNpcId() == QUEST_MANAGER) {
            if (cond == 0)
                htmltext = "qm_10607_01.htm";
            else if (cond == 1)
                htmltext = "qm_10607_03.htm";
            else if (cond == 2)
                htmltext = "10607_done.htm";
        }

        return htmltext;
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        if ("qm_10607_02.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
        } else if ("check_items".equalsIgnoreCase(event)) {
            if (CheckIngredients(st, Ingredients) && CheckIngredients(st, Proofs)) {
                event = "10607_done.htm";
                TakeIngredients(st, Ingredients);
                TakeIngredients(st, Proofs);
                st.setCond(2);
            } else event = "10607_not_done.htm";
        }
        else if ("reward_1".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(5808, 1); // Party Mask
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_2".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(6394, 1); // Red Party Mask
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_3".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(6843, 1); // Cat Ears
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_4".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8185, 1); // Chapeau
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_5".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8186, 1); // Artisan`s Goggles
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_6".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8187, 1); // Red Horn of Victory
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_7".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8559, 1); // Coronet
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_8".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8560, 1); // Teddy Bear Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_9".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8561, 1); // Piggy Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_10".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8562, 1); // Jester Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_11".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8563, 1); // Wizard Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_12".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8564, 1); // Dapper Cap
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_13".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(8569, 1); // Half Face Mask
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_14".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(9138, 1); // Santa Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_15".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(10250, 1); // Adventurer Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_16".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(13501, 1); // Refined Romantic Chapeau
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_17".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(20020, 1); // Uniform Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_18".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(20021, 1); // Assassin`s Bamboo Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_19".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(20022, 1); // Ruthless Tribe Mask
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_20".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(20023, 1); // Ribbon Hairband
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_21".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(20666, 1); // Valkyrie Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        else if ("reward_22".equalsIgnoreCase(event)){
            event = "qm_10607_04.htm";
            st.giveItems(20678, 1); // Gatekeeper Hat
            st.playSound(SOUND_FINISH);
            st.setCond(-1);
            st.finishQuest();
        }
        return event;
    }

    @Override
    public String onKill(NpcInstance npc, QuestState st) {
        if (st.getCond() == 1) {
            if (ArrayUtils.contains(OF_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(OF_PROOF) < 30) {
                    st.giveItems(OF_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(SOS_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(SOS_PROOF) < 30) {
                    st.giveItems(SOS_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(HS_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(HS_PROOF) < 30) {
                    st.giveItems(HS_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(AC_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(AC_PROOF) < 30) {
                    st.giveItems(AC_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(VOS_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(VOS_PROOF) < 30) {
                    st.giveItems(VOS_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(AL_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(AL_PROOF) < 30) {
                    st.giveItems(AL_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(KETRA_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(KETRA_PROOF) < 30) {
                    st.giveItems(KETRA_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(GC_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(GC_PROOF) < 30) {
                    st.giveItems(GC_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(SOL_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(SOL_PROOF) < 30) {
                    st.giveItems(SOL_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(ANTHARAS_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(ANTHARAS_PROOF) < 30) {
                    st.giveItems(ANTHARAS_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(IT_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(IT_PROOF) < 30) {
                    st.giveItems(IT_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(ELVEN_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(ELVEN_PROOF) < 30) {
                    st.giveItems(ELVEN_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
            if (ArrayUtils.contains(OREN_MONSTERS, npc.getNpcId()))
                if (st.getQuestItemsCount(OREN_PROOF) < 30) {
                    st.giveItems(OREN_PROOF, 1);
                    st.playSound(SOUND_ITEMGET);
                }
        }
        return null;
    }

    private static boolean CheckIngredients(QuestState st, int[][] item_list) {
        for (int[] _item : item_list)
            if (st.getQuestItemsCount(_item[0]) < _item[1])
                return false;
        return true;
    }

    private static void TakeIngredients(QuestState st, int[][] item_list) {
        for (int[] _item : item_list)
            st.takeItems(_item[0], _item[1]);
    }
}
