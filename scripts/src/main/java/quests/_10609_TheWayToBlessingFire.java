package quests;

import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestPartyType;
import l2s.gameserver.model.quest.QuestRepeatType;
import l2s.gameserver.model.quest.QuestState;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author KRonst 22.02.2018
 */

public class _10609_TheWayToBlessingFire extends Quest {
    //NPC
    private static final int QUEST_MANAGER = 40012;
    private static final int OREGON = 40019;
    private static final int PLON = 40017;
    //private static final int LILITH = 25283;
    private static final int ANAKIM = 25286;

    private static final int[] FIRE_MONSTERS = {20162, 20235, 20311, 20347, 20351};
    //private static final int[] WATER_MONSTERS = {20021, 20027, 20030, 20033, 20035};

    //ITEMS
    private static final int GLORIOUS_CLOAK = 30373;
    //private static final int WATER_CLOAK = 34925;
    private static final int FIRE_CLOAK = 34926;
    private static final int CLOAK_UP_STONE = 75000;
    private static final int MA = 1890;
    private static final int MOLD = 4041;
    private static final int GEM_A = 2133;
    private static final int FIRE_PROOF = 75062;

    private static final int[][] IngredientsOregon = {
            {MA, 15},
            {MOLD, 20}
    };

    private static final int[][] IngredientsPlon = {
            {GEM_A, 3}
    };

    public _10609_TheWayToBlessingFire() {
        super(QuestPartyType.PARTY_ALL, QuestRepeatType.ONETIME);
        addStartNpc(QUEST_MANAGER);
        addTalkId(OREGON, PLON);
        addKillId(FIRE_MONSTERS);
        addKillId(ANAKIM);
        addQuestItem(CLOAK_UP_STONE, FIRE_PROOF);
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        if (st.isCompleted())
            return "noquest";
        String htmltext = "noquest";
        if (st.getPlayer().getFraction() == Fraction.FIRE) {
            int cond = st.getCond();
            if (npc.getNpcId() == QUEST_MANAGER) {
                if (cond == 0) {
                    htmltext = "qm_10609_01.htm"; //accept
                } else if (cond == 2)
                    htmltext = "qm_10609_03.htm"; //killed
//                else if(cond == 11){
//                    htmltext = "qm_10609_05.htm"; //take_cloak
//                }
            } else if (npc.getNpcId() == OREGON) {
                if (cond == 3) {
                    htmltext = "oregon_10609_01.htm"; //accept_oregon
                }
                else if (cond == 4){
                    htmltext = "oregon_10609_03.htm"; //check_items_oregon
                }
                else if (cond == 10){
                    htmltext = "oregon_10609_05.htm"; //give_adena
                }
            } else if (npc.getNpcId() == PLON){
                if (cond == 5){
                    htmltext = "plon_10609_01.htm"; //accept_plon
                }
                else if (cond == 6){
                    htmltext = "plon_10609_03.htm"; //check_items_plon
                }
                else if (cond == 7){
                    htmltext = "plon_10609_05.htm"; //accept_kill_monsters
                }
                else if (cond == 9){
                    htmltext = "plon_10609_07.htm"; //monsters_killed
                }
            }
            else {
                htmltext = "qm_10609_nocloak.htm"; //Создай с таким названием HTML
            }
        }

        return htmltext;
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        if ("accept".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(GLORIOUS_CLOAK) >= 1 || st.getItemEquipped(Inventory.PAPERDOLL_BACK) == GLORIOUS_CLOAK) {
                event = "qm_10609_02.htm";
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            }
            else event = "qm_10609_nocloak.htm";
        }
        else if ("qm_10609_04.htm".equalsIgnoreCase(event)){
            st.setCond(3);
            st.playSound(SOUND_MIDDLE);
        }
        else if ("oregon_10609_02.htm".equalsIgnoreCase(event)){
            st.setCond(4);
            st.playSound(SOUND_ACCEPT);

        }
        else if ("check_items_oregon".equalsIgnoreCase(event)){
            if (CheckIngredients(st, IngredientsOregon) && st.getQuestItemsCount(ADENA_ID) >= 1000){
                event = "oregon_10609_04.htm"; //all items
                TakeIngredients(st, IngredientsOregon);
                st.takeItems(CLOAK_UP_STONE, 1);
                st.takeItems(ADENA_ID, 1000);
                st.setCond(5);
                st.playSound(SOUND_MIDDLE);
            }
            else{
                event = "oregon_10609_notall.htm";
            }
        }
        else if ("plon_10609_02.htm".equalsIgnoreCase(event)){
            st.setCond(6);
            st.playSound(SOUND_ACCEPT);
        }
        else if ("check_items_plon".equalsIgnoreCase(event)){
            if (CheckIngredients(st, IngredientsPlon) && st.getQuestItemsCount(ADENA_ID) >= 1000){
                event = "plon_10609_04.htm"; //all items
                TakeIngredients(st, IngredientsPlon);
                st.takeItems(ADENA_ID, 1000);
                st.setCond(7);
                st.playSound(SOUND_MIDDLE);
            }
            else {
                event = "plon_10609_notall.htm";
            }
        }
        else if ("plon_10609_06.htm".equalsIgnoreCase(event)){
            st.playSound(SOUND_ACCEPT);
            st.setCond(8);
        }
        else if ("plon_10609_08.htm".equalsIgnoreCase(event)){
            st.takeAllItems(FIRE_PROOF);
            st.setCond(10);
            st.playSound(SOUND_MIDDLE);
        }
        else if ("give_adena".equalsIgnoreCase(event)){
            if (st.getQuestItemsCount(ADENA_ID) >= 1000){
                st.takeItems(ADENA_ID, 1000);
                st.takeItems(GLORIOUS_CLOAK, 1);
                st.giveItems(FIRE_CLOAK, 1);
                event = "oregon_10609_06.htm";
                st.playSound(SOUND_FINISH);
                st.finishQuest();
            }
            else event = "oregon_10609_noadena.htm";
        }
//        else if (event.equalsIgnoreCase("qm_10609_06.htm")){
//            st.giveItems(FIRE_CLOAK, 1);
//            st.playSound(SOUND_FINISH);
//            st.finishQuest();
//        }
        return event;
    }

    @Override
    public String onKill(NpcInstance npc, QuestState st) {
        if (st.getCond() == 1) {
            if (npc.getNpcId() == ANAKIM){
                st.giveItems(CLOAK_UP_STONE, 1);
                st.playSound(SOUND_ITEMGET);
                st.setCond(2);
            }
        }
        if (st.getCond() == 8){
            if (ArrayUtils.contains(FIRE_MONSTERS, npc.getNpcId())){
                st.giveItems(FIRE_PROOF, 1);
                st.playSound(SOUND_ITEMGET);
                if (st.getQuestItemsCount(FIRE_PROOF) >= 30)
                    st.setCond(9);
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
