package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public final class _104_SpiritOfMirror extends Quest
{
	static final int GALLINS_OAK_WAND = 748;
	static final int WAND_SPIRITBOUND1 = 1135;
	static final int WAND_SPIRITBOUND2 = 1136;
	static final int WAND_SPIRITBOUND3 = 1137;
	static final int WAND_OF_ADEPT = 49044;

	static final SystemMessagePacket CACHE_SYSMSG_GALLINS_OAK_WAND = SystemMessagePacket.removeItems(GALLINS_OAK_WAND, 1);

	public _104_SpiritOfMirror()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(30017);
		addKillId(27003, 27004, 27005);
		addAttackId(27003, 27004, 27005);
		addQuestItem(1135, 1136, 1137);
		addLevelCheck("gallin_q0104_06.htm", 10, 15);
		addRaceCheck("gallin_q0104_00.htm", Race.HUMAN);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if("gallin_q0104_03.htm".equalsIgnoreCase(event))
		{
			st.setCond(1);
			st.giveItems(GALLINS_OAK_WAND, 3);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30017)
		{
			if(cond == 0)
				htmltext = "gallin_q0104_02.htm";
			else if(cond == 1)
				htmltext = "gallin_q0104_04.htm";
			else if(cond == 3)
			{
				st.takeAllItems(WAND_SPIRITBOUND1, WAND_SPIRITBOUND2, WAND_SPIRITBOUND3);
				st.giveItems(WAND_OF_ADEPT, 1);
				htmltext = "gallin_q0104_05.htm";
				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		boolean withOakWand = st.getPlayer().getActiveWeaponInstance() != null && st.getPlayer().getActiveWeaponInstance().getItemId() == GALLINS_OAK_WAND;
		boolean correctNpc = st.getInt(String.valueOf(npcId)) != npc.getObjectId();

		if(cond == 1 && withOakWand && correctNpc)
			st.set(String.valueOf(npcId), npc.getObjectId(), false);

		return null;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(cond == 1)
		{

			int WAND = getWandId(npcId);
			int objId = st.getInt(String.valueOf(npcId));
			if(st.getQuestItemsCount(WAND) == 0 && npc.getObjectId() == objId && st.getPlayer().getInventory().destroyItemByItemId(GALLINS_OAK_WAND, 1))
			{
				st.giveItems(WAND, 1);
				st.getPlayer().sendPacket(CACHE_SYSMSG_GALLINS_OAK_WAND);
				long Collect = st.getQuestItemsCount(WAND_SPIRITBOUND1) + st.getQuestItemsCount(WAND_SPIRITBOUND2) + st.getQuestItemsCount(WAND_SPIRITBOUND3);
				if(Collect == 3)
					st.setCond(3);
				else
					st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}

	private int getWandId(int npcId)
	{
		switch(npcId)
		{
			case 27003:
				return WAND_SPIRITBOUND1;
			case 27004:
				return WAND_SPIRITBOUND2;
			case 27005:
				return WAND_SPIRITBOUND3;
		}
		return 0;
	}
}