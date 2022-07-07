package quests;

import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.TutorialCloseHtmlPacket;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;

import java.util.List;
import java.util.StringTokenizer;

public final class _999_Tutorial extends Quest
{
	private static final int[][] ARMORS = {
			{ 398, 418, 2431, 2455, 2414, 919, 857, 857, 888, 888 }, //plate_leather
			{ 439, 471, 2430, 2454, 2414, 919, 857, 857, 888, 888 }, //karmian robe
			{ 441, 472, 2434, 2459, 2414, 919, 857, 857, 888, 888 }, //demons tunic
			{ 356, 2414, 2438, 2462, 919, 857, 857, 888, 888 } };//full plate heavy

	private static final int[][] WEAPONS = {
			{ 7891, 2497 }, //Homunculus Sword
			{ 7888, 2497 }, //Ecliptic Sword
			{ 206 }, //Demon's Staff
			{ 286 }, //Eminence Bow
			{ 266 }, //Great Pata
			{ 299 }, //Orcish Poleaxe
			{ 5286 }, //Berserker Blade
			{ 2599 }, //Raid Sword*Raid Sword
			{ 228 }, //Crystal Dagger
			{ 135, 2497 } };// Samurai Long Sword

	private class PlayerEnterListener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			QuestState st = player.getQuestState(_999_Tutorial.this);
			if(st != null) {
				String res;
				try
				{
					res = onTutorialEvent(ENTER_WORLD_EVENT, "", st);
				}
				catch(Exception e)
				{
					showError(st.getPlayer(), e);
					return;
				}

				showTutorialResult(st.getPlayer(), res);
			}
		}
	}

	// Var's
	private static final String QUESTION_MARK_STATE = "question_mark_state";

	// Events
	// Данные переменные не изменять, они вшиты в ядро.
	private static final String ENTER_WORLD_EVENT = "EW"; // Вход в мир.
	private static final String QUESTION_MARK_EVENT = "QM"; // Вопросытельный знак.
	private static final String CLIENT_EVENT = "CE"; // Дейтсвия клиента. (100 - Class Change, 200 - Death, 300 - Level UP)
	private static final String TUTORIAL_BYPASS_EVENT = "BYPASS"; // Использование байпасса в туториале.
	private static final String TUTORIAL_LINK_EVENT = "LINK"; // Использование ссылки в туториале.

	private final OnPlayerEnterListener _playerEnterListener = new PlayerEnterListener();

	public _999_Tutorial()
	{
		super(PARTY_NONE, REPEATABLE);

		addFirstTalkId(40005, 40017, 40015, 40016, 40004);

		CharListenerList.addGlobal(_playerEnterListener);
	}

	@Override
	public String onTutorialEvent(String event, String value, QuestState st)
	{
		//		st.getPlayer().sendMessage("onTutorialEvent: " + event + " " + value);
		if(event.equalsIgnoreCase(ENTER_WORLD_EVENT))
			return onEnterWorld(st);

		if(event.equalsIgnoreCase(QUESTION_MARK_EVENT))
			return onQuestionMark(Integer.parseInt(value), st);

		if(event.equalsIgnoreCase(TUTORIAL_LINK_EVENT))
			return onTutorialLink(value, st);

		return null;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState st = player.getQuestState(this);
		if(st == null)
		{
			npc.showChatWindow(player, 0, true);
			return null;
		}

		int qm_state = st.getInt(QUESTION_MARK_STATE);

		int npcId = npc.getNpcId();

		boolean add = false;
		if(npcId == 40005 && qm_state == 5)
			add = true;
		else if(npcId == 40017 && qm_state == 6)
			add = true;
		else if((npcId == 40015 || npcId == 40016) && qm_state == 7)
			add = true;
		else if(npcId == 40004 && qm_state == 11)
			add = true;

		if(add)
		{
			qm_state++;
			st.set(QUESTION_MARK_STATE, qm_state);
			st.showQuestionMark(qm_state);

			showTutorialHtmlFile(player, "tutorial_step_" + qm_state + ".htm");
		}

		npc.showChatWindow(player, 0, true);
		return null;
	}

	private String onEnterWorld(QuestState st)
	{
		Player player = st.getPlayer();
		int qm_state = st.getInt(QUESTION_MARK_STATE);

		if (qm_state == 0) {
            qm_state++;
			st.set(QUESTION_MARK_STATE, qm_state);
			st.playSound(SOUND_TUTORIAL);
			return "tutorial_step_" + qm_state + ".htm";
		} else if (qm_state == 1) {
            if (player.getClassLevel().ordinal() >= 2) {
                qm_state += 2;
                st.set(QUESTION_MARK_STATE, qm_state);
            } else if (player.getClassLevel().ordinal() == 1) {
                qm_state++;
                st.set(QUESTION_MARK_STATE, qm_state);
            }
        } else if (qm_state == 2) {
            if (player.getClassLevel().ordinal() >= 2) {
                qm_state++;
                st.set(QUESTION_MARK_STATE, qm_state);
            }
        }

		st.showQuestionMark(qm_state);

		st.playSound(SOUND_TUTORIAL);

        return "tutorial_step_" + qm_state + ".htm";
	}

	private String onQuestionMark(int markId, QuestState st)
	{
		int qm_state = st.getInt(QUESTION_MARK_STATE);

		Player player = st.getPlayer();

		if((player.getLevel() < player.getClassId().getClassMinLevel(true) || player.getClassId().isLast()) && qm_state == 1)
		{
			st.set(QUESTION_MARK_STATE, qm_state + 1);
			qm_state = st.getInt(QUESTION_MARK_STATE);
		}

		return "tutorial_step_" + qm_state + ".htm";
	}

	@Override
	protected void showTutorialHtmlFile(Player player, String fileName, Object... arg)
	{
		String fraction = player.getFraction() == Fraction.FIRE ? "Fire" : "Water";
		String classes = "";

		if(player.getLevel() >= player.getClassId().getClassMinLevel(true))
		{
			StringBuilder classList = new StringBuilder();
			ClassId classId = player.getClassId();
			for(ClassId cid : ClassId.VALUES)
			{
				if(cid.childOf(classId) && cid.getClassLevel().ordinal() == classId.getClassLevel().ordinal() + 1)
					classList.append("<button ALIGN=LEFT ICON=\"NORMAL\" action=\"link tutorial_classmaster_").append(cid.getId()).append("\">").append(HtmlUtils.htmlClassName(cid.getId())).append("</button>");
			}
			classes = classList.toString();
		}
		super.showTutorialHtmlFile(player, fileName, "%fraction%", fraction, "%classes%", classes);
	}

	private String onTutorialLink(String value, QuestState st)
	{
        StringTokenizer tokenizer = new StringTokenizer(value, "_");
		String cmd = tokenizer.nextToken();
		Player player = st.getPlayer();
		if("tutorial".equalsIgnoreCase(cmd))
		{
			String cmd2 = tokenizer.nextToken();
			if("step".equalsIgnoreCase(cmd2))
			{
				if(tokenizer.hasMoreTokens())
				{
					int step = Integer.parseInt(tokenizer.nextToken());
					st.set(QUESTION_MARK_STATE, step);

					switch(step)
					{
						case 1:
						{
							return "tutorial_step_1.htm";
						}
						case 5:
						{
							if(player.getFraction() == Fraction.FIRE)
								st.addRadar(81992, 53800, -1488);
							else
								st.addRadar(45480, 48376, -3064);

							break;
						}
						case 6:
						{
							if(player.getFraction() == Fraction.FIRE)
								st.addRadar(79528, 54920, -1544);
							else
								st.addRadar(42760, 50120, -2992);

							break;
						}
						case 7:
						{
							if(player.getFraction() == Fraction.FIRE)
								st.addRadar(79192, 53624, -1544);
							else
								st.addRadar(44616, 46952, -2992);

							break;
						}
						case 8:
						case 9:
						case 10:
						{
							step++;
							st.set(QUESTION_MARK_STATE, step);

							return "tutorial_step_" + step + ".htm";
						}
						case 11:
						{
							if(player.getFraction() == Fraction.FIRE)
								st.addRadar(82984, 53192, -1488);
							else
								st.addRadar( 46920, 51496, -2984);
							break;
						}
						case 12:
						{
							st.finishQuest();
							player.removeRadar();
							break;
						}
					}
				}
				st.getPlayer().sendPacket(TutorialCloseHtmlPacket.STATIC);
			}
			else if("classmaster".equalsIgnoreCase(cmd2))
			{
				if(tokenizer.hasMoreTokens())
				{
					int val = Integer.parseInt(tokenizer.nextToken());
					ClassId classId = ClassId.VALUES[val];
					int newClassLvl = classId.getClassLevel().ordinal();

					int step = st.getInt(QUESTION_MARK_STATE);

					if (newClassLvl == 1) {
						if (player.getClassLevel().ordinal() >= 2) {
							step += 2;
							st.set(QUESTION_MARK_STATE, step);
							return "tutorial_step_" + step + ".htm";
						} else if (player.getClassLevel().ordinal() == 1) {
							step++;
							st.set(QUESTION_MARK_STATE, step);
							return "tutorial_step_" + step + ".htm";
						} else if (!classId.childOf(player.getClassId())) {
							step++;
							st.set(QUESTION_MARK_STATE, step);
							return "tutorial_step_" + step + ".htm";
						}
					} else if (newClassLvl == 2) {
						if (player.getClassLevel().ordinal() >= 2) {
							step++;
							st.set(QUESTION_MARK_STATE, step);
							return "tutorial_step_" + step + ".htm";
						} else if (!classId.childOf(player.getClassId())) {
							step++;
							st.set(QUESTION_MARK_STATE, step);
							return "tutorial_step_" + step + ".htm";
						}
					}

					player.setClassId(val, false);
					player.broadcastUserInfo(true);

					if(player.getLevel() < player.getClassId().getClassMinLevel(true) || classId.isLast())
					{
						step++;
						st.set(QUESTION_MARK_STATE, step);
						return "tutorial_step_" + step + ".htm";
					}

					step++;
					st.set(QUESTION_MARK_STATE, step);
					return "tutorial_step_" + step + ".htm";
				}
			}
			else if("getitem".equalsIgnoreCase(cmd2))
			{
				if(tokenizer.hasMoreTokens())
				{
					int val = Integer.parseInt(tokenizer.nextToken());
					int step = st.getInt(QUESTION_MARK_STATE);

					int[] items;
					if(step == 3)
						items = ARMORS[val];
					else if(step == 4)
						items = WEAPONS[val];
					else
						return "tutorial_step_" + step + ".htm";

					for(int id : items)
					{
						List<ItemInstance> item = ItemFunctions.addItem(player, id, 1);
						if (id == 286) {
							ItemFunctions.addItem(player, 32251, 1); // Give arrows if bow selected
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						if (id == 7891) {
							ItemFunctions.addItem(player, 3949, 1000); // Дать 100 сосок ц маг
						}
						if (id == 7888) {
							ItemFunctions.addItem(player, 3949, 1000); // Дать 100 сосок ц маг
						}
						if (id == 206) {
							ItemFunctions.addItem(player, 3949, 1000); // Дать 100 сосок ц маг
						}
						if (id == 266) {
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						if (id == 299) {
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						if (id == 5286) {
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						if (id == 2599) {
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						if (id == 228) {
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						if (id == 135) {
							ItemFunctions.addItem(player, 1464, 1000); // Дать 100 сосок ц воин
						}
						for(ItemInstance i : item) {
                            player.getInventory().equipItem(i);
							player.tScheme_record.addEquipItem(i);
                        }
					}
					step++;
					st.set(QUESTION_MARK_STATE, step);
					return "tutorial_step_" + step + ".htm";
				}
			}
		}

		return null;
	}

	@Override
	public boolean isVisible(Player player)
	{
		return false;
	}
}
