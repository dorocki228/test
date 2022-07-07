package l2s.gameserver.model.quest;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.logging.LogUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.startcondition.ICheckStartCondition;
import l2s.gameserver.model.quest.startcondition.impl.*;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExQuestNpcLogList;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Quest implements OnInitScriptListener
{
	private static final Logger _log;
	public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
	public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
	public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
	public static final String SOUND_FINISH = "ItemSound.quest_finish";
	public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
	public static final String SOUND_TUTORIAL = "ItemSound.quest_tutorial";
	public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
	public static final String SOUND_HORROR2 = "SkillSound5.horror_02";
	public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
	public static final String SOUND_FANFARE_MIDDLE = "ItemSound.quest_fanfare_middle";
	public static final String SOUND_FANFARE2 = "ItemSound.quest_fanfare_2";
	public static final String SOUND_BROKEN_KEY = "ItemSound2.broken_key";
	public static final String SOUND_ENCHANT_SUCESS = "ItemSound3.sys_enchant_sucess";
	public static final String SOUND_ENCHANT_FAILED = "ItemSound3.sys_enchant_failed";
	public static final String SOUND_ED_CHIMES05 = "AmdSound.ed_chimes_05";
	public static final String SOUND_ARMOR_WOOD_3 = "ItemSound.armor_wood_3";
	public static final String SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH = "ItemSound.item_drop_equip_armor_cloth";
	public static final String NO_QUEST_DIALOG = "no-quest";
	public static final String COMPLETED_DIALOG = "completed";
	private static final String FONT_QUEST_AVAILABLE = "<font color=\"bbaa88\">";
	private static final String FONT_QUEST_DONE = "<font color=\"787878\">";
	private static final String FONT_QUEST_IN_PROGRESS = "<font color=\"ffdd66\">";
	protected static final String TODO_FIND_HTML = "<font color=\"6699ff\">TODO:<br>Find this dialog";
	public static final int ADENA_ID = 57;
	public static final QuestPartyType PARTY_NONE;
	public static final QuestPartyType PARTY_ONE;
	public static final QuestPartyType PARTY_ALL;
	public static final QuestPartyType COMMAND_CHANNEL;
	public static final QuestRepeatType ONETIME;
	public static final QuestRepeatType REPEATABLE;
	public static final QuestRepeatType DAILY;
	private final IntObjectMap<Map<String, QuestTimer>> _pausedQuestTimers;
	private final IntSet _startNpcs;
	private final IntSet _questItems;
	private final TIntObjectMap<List<QuestNpcLogInfo>> _npcLogList;
	private final TIntObjectMap<List<QuestNpcLogInfo>> _itemsLogList;
	private final TIntObjectMap<List<QuestNpcLogInfo>> _customLogList;
	private final Map<ICheckStartCondition, ConditionMessage> _startConditions;
	private final double _rewardRate;
	private final String _name;
	private final int _id;
	private final QuestPartyType _partyType;
	private final QuestRepeatType _repeatType;
	private final boolean _abortable;

	public void addQuestItem(int... ids)
	{
		for(int id : ids)
			if(id != 0)
			{
				ItemTemplate i = ItemHolder.getInstance().getTemplate(id);
				if(i == null)
					_log.warn("Item ID[" + id + "] is null in quest drop in " + getName());
				else
					_questItems.add(id);
			}
	}

	public void addQuestItemWithLog(int cond, int npcStringId, int max, int... ids)
	{
		if(ids.length == 0)
			throw new IllegalArgumentException("Items list cant be empty!");
		addQuestItem(ids);
		List<QuestNpcLogInfo> vars = _itemsLogList.get(cond);
		if(vars == null)
			_itemsLogList.put(cond, vars = new ArrayList<>(5));
		vars.add(new QuestNpcLogInfo(ids, null, max, npcStringId));
	}

	public void updateItems(ItemInstance item, QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
			return;
		List<QuestNpcLogInfo> vars = getItemsLogList(st.getCond());
		if(vars == null)
			return;
		for(QuestNpcLogInfo info : vars)
			if(ArrayUtils.contains(info.getNpcIds(), item.getItemId()))
			{
				player.sendPacket(new ExQuestNpcLogList(st));
				break;
			}
	}

	public int[] getItems()
	{
		return _questItems.toArray();
	}

	public boolean isQuestItem(int id)
	{
		return _questItems.contains(id);
	}

	public void addCustomLog(int cond, String varName, int npcStringId, int max)
	{
		List<QuestNpcLogInfo> vars = _customLogList.get(cond);
		if(vars == null)
			_customLogList.put(cond, vars = new ArrayList<>(5));
		vars.add(new QuestNpcLogInfo(null, varName, max, npcStringId));
	}

	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		Player player = qs.getPlayer();
		if(player == null)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_quests (char_id,id,var,value) VALUES (?,?,?,?)");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setInt(2, qs.getQuest().getId());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void deleteQuestInDb(QuestState qs)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND id=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setInt(2, qs.getQuest().getId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND id=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setInt(2, qs.getQuest().getId());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void restoreQuestStates(Player player)
	{
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
            IntSet questsToDelete = new HashIntSet();
            con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id,var,value FROM character_quests WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int questId = rset.getInt("id");
				String var = rset.getString("var");
				String value = rset.getString("value");
				QuestState qs = player.getQuestState(questId);
				if(qs == null)
				{
					Quest q = QuestHolder.getInstance().getQuest(questId);
					if(q == null)
					{
						if(!Config.DONTLOADQUEST && !questsToDelete.contains(questId))
						{
							questsToDelete.add(questId);
							_log.warn("Unknown quest " + questId + " for player " + player.getName());
							continue;
						}
						continue;
					}
					else
						qs = new QuestState(q, player);
				}
				qs.set(var, value, false);
			}
			if(!questsToDelete.isEmpty())
			{
				DbUtils.close(statement);
				statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND id=?");
				for(int questId2 : questsToDelete.toArray())
				{
					statement.setInt(1, player.getObjectId());
					statement.setInt(2, questId2);
					statement.addBatch();
				}
				statement.executeBatch();
			}
		}
		catch(Exception e)
		{
			_log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public Quest(QuestPartyType partyType, QuestRepeatType repeatType, boolean abortable)
	{
		_pausedQuestTimers = new CHashIntObjectMap<>();
		_startNpcs = new HashIntSet();
		_questItems = new HashIntSet();
		_npcLogList = new TIntObjectHashMap<>(5);
		_itemsLogList = new TIntObjectHashMap<>(5);
		_customLogList = new TIntObjectHashMap<>(5);
		_startConditions = new HashMap<>();
		_name = getClass().getSimpleName();
		_id = Integer.parseInt(_name.split("_")[1]);
		_partyType = partyType;
		_repeatType = repeatType;
		_abortable = abortable;
		if(!Config.EX_USE_QUEST_REWARD_PENALTY_PER || !Config.EX_F2P_QUEST_REWARD_PENALTY_QUESTS.contains(_id))
			_rewardRate = 1.0;
		else
			_rewardRate = Config.EX_F2P_QUEST_REWARD_PENALTY_PER * 0.01;
	}

	public Quest(QuestPartyType partyType, QuestRepeatType repeatType)
	{
		this(partyType, repeatType, true);
	}

	public QuestRepeatType getRepeatType()
	{
		return _repeatType;
	}

	public boolean isAbortable()
	{
		return _abortable;
	}

	public List<QuestNpcLogInfo> getNpcLogList(int cond)
	{
		return _npcLogList.get(cond);
	}

	public List<QuestNpcLogInfo> getItemsLogList(int cond)
	{
		return _itemsLogList.get(cond);
	}

	public List<QuestNpcLogInfo> getCustomLogList(int cond)
	{
		return _customLogList.get(cond);
	}

	public void addAttackId(int... attackIds)
	{
		for(int attackId : attackIds)
			addEventId(attackId, QuestEventType.ATTACKED_WITH_QUEST);
	}

	public NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			NpcTemplate t = NpcHolder.getInstance().getTemplate(npcId);
			if(t != null)
				t.addQuestEvent(eventType, this);
			return t;
		}
		catch(Exception e)
		{
			_log.error("", e);
			return null;
		}
	}

	public void addKillId(int... killIds)
	{
		for(int killid : killIds)
			addEventId(killid, QuestEventType.MOB_KILLED_WITH_QUEST);
	}

	public void addKillNpcWithLog(int cond, int npcStringId, String varName, int max, int... killIds)
	{
		if(killIds.length == 0)
			throw new IllegalArgumentException("Npc list cant be empty!");
        addKillId(killIds);
		List<QuestNpcLogInfo> vars = _npcLogList.get(cond);
		if(vars == null)
			_npcLogList.put(cond, vars = new ArrayList<>(5));
		vars.add(new QuestNpcLogInfo(killIds, varName, max, npcStringId));
	}

	public void addKillNpcWithLog(int cond, String varName, int max, int... killIds)
	{
        addKillNpcWithLog(cond, 0, varName, max, killIds);
	}

	public boolean updateKill(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
			return false;
		List<QuestNpcLogInfo> vars = getNpcLogList(st.getCond());
		if(vars == null)
			return false;
		boolean done = true;
		boolean find = false;
		boolean update = false;
		for(QuestNpcLogInfo info : vars)
		{
			int count = st.getInt(info.getVarName());
			if(!find && ArrayUtils.contains(info.getNpcIds(), npc.getNpcId()))
			{
				find = true;
				if(count < info.getMaxCount())
				{
					st.set(info.getVarName(), ++count);
					update = true;
				}
			}
			if(count != info.getMaxCount())
				done = false;
		}
		if(update)
			player.sendPacket(new ExQuestNpcLogList(st));
		return done;
	}

	public boolean updateCustomLog(String var, QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
			return false;
		List<QuestNpcLogInfo> vars = getCustomLogList(st.getCond());
		if(vars == null)
			return false;
		boolean done = true;
		boolean update = false;
		for(QuestNpcLogInfo info : vars)
		{
			if(!var.equalsIgnoreCase(info.getVarName()))
				continue;
			int count = st.getInt(var);
			if(count < info.getMaxCount())
			{
				st.set(info.getVarName(), ++count);
				update = true;
			}
			if(count == info.getMaxCount())
				continue;
			done = false;
		}
		if(update)
			player.sendPacket(new ExQuestNpcLogList(st));
		return done;
	}

	public void addKillId(Collection<Integer> killIds)
	{
		for(int killid : killIds)
            addKillId(killid);
	}

	public NpcTemplate addSkillUseId(int npcId)
	{
		return addEventId(npcId, QuestEventType.MOB_TARGETED_BY_SKILL);
	}

	public void addStartNpc(int... npcIds)
	{
		for(int talkId : npcIds)
            addStartNpc(talkId);
	}

	public NpcTemplate addStartNpc(int npcId)
	{
		_startNpcs.add(npcId);
        addTalkId(npcId);
		return addEventId(npcId, QuestEventType.QUEST_START);
	}

	public void addFirstTalkId(int... npcIds)
	{
		for(int npcId : npcIds)
			addEventId(npcId, QuestEventType.NPC_FIRST_TALK);
	}

	public void addTalkId(int... talkIds)
	{
		for(int talkId : talkIds)
			addEventId(talkId, QuestEventType.QUEST_TALK);
	}

	public void addTalkId(Collection<Integer> talkIds)
	{
		for(int talkId : talkIds)
            addTalkId(talkId);
	}

	public void addLevelCheck(int npcId, String message, int min)
	{
        addLevelCheck(message, min, Experience.getMaxLevel());
	}

	public void addLevelCheck(String message, int min)
	{
        addLevelCheck(-1, message, min);
	}

	public void addLevelCheck(int npcId, String message, int min, int max)
	{
		_startConditions.put(new PlayerLevelCondition(min, max), new ConditionMessage(npcId, message));
	}

	public void addLevelCheck(String message, int min, int max)
	{
        addLevelCheck(-1, message, min, max);
	}

	public void addRaceCheck(int npcId, String message, boolean classRace, Race... races)
	{
		_startConditions.put(new PlayerRaceCondition(classRace, races), new ConditionMessage(npcId, message));
	}

	public void addRaceCheck(String message, boolean classRace, Race... races)
	{
        addRaceCheck(-1, message, classRace, races);
	}

	public void addRaceCheck(int npcId, String message, Race... races)
	{
        addRaceCheck(npcId, message, false, races);
	}

	public void addRaceCheck(String message, Race... races)
	{
        addRaceCheck(-1, message, false, races);
	}

	public void addQuestCompletedCheck(int npcId, String message, int questId)
	{
		_startConditions.put(new QuestCompletedCondition(questId), new ConditionMessage(npcId, message));
	}

	public void addQuestCompletedCheck(String message, int questId)
	{
        addQuestCompletedCheck(-1, message, questId);
	}

	public void addClassLevelCheck(int npcId, String message, ClassLevel... classLevels)
	{
		_startConditions.put(new ClassLevelCondition(classLevels), new ConditionMessage(npcId, message));
	}

	public void addClassLevelCheck(String message, ClassLevel... classLevels)
	{
        addClassLevelCheck(-1, message, classLevels);
	}

	public void addClassIdCheck(int npcId, String message, int... classIds)
	{
		_startConditions.put(new ClassIdCondition(classIds), new ConditionMessage(npcId, message));
	}

	public void addClassIdCheck(String message, int... classIds)
	{
        addClassIdCheck(-1, message, classIds);
	}

	public void addClassIdCheck(int npcId, String message, ClassId... classIds)
	{
		_startConditions.put(new ClassIdCondition(classIds), new ConditionMessage(npcId, message));
	}

	public void addClassIdCheck(String message, ClassId... classIds)
	{
        addClassIdCheck(-1, message, classIds);
	}

	public void addItemHaveCheck(int npcId, String message, int itemId, long count)
	{
		_startConditions.put(new ItemHaveCondition(itemId, count), new ConditionMessage(npcId, message));
	}

	public void addItemHaveCheck(String message, int itemId, long count)
	{
        addItemHaveCheck(-1, message, itemId, count);
	}

	public void addClassTypeCheck(int npcId, String message, ClassType type)
	{
		_startConditions.put(new ClassTypeCondition(type), new ConditionMessage(npcId, message));
	}

	public void addClassTypeCheck(String message, ClassType type)
	{
        addClassTypeCheck(-1, message, type);
	}

	public String getDescr(NpcInstance npc, Player player, boolean isStartNpc)
	{
		if(!isVisible(player))
			return null;
		int state = getDescrState(npc, player, isStartNpc);
		String font = "<font color=\"bbaa88\">";
		switch(state)
		{
			case 2:
			{
				font = "<font color=\"ffdd66\">";
				break;
			}
			case 3:
			{
				font = "<font color=\"787878\">";
				break;
			}
		}
		int fStringId = getId();
		if(fStringId >= 10000)
			fStringId -= 5000;
		fStringId = fStringId * 100 + state;
		return font + HtmlUtils.htmlNpcString(fStringId, new Object[0]) + "</font>";
	}

	public final int getDescrState(NpcInstance npc, Player player, boolean isStartNpc)
	{
		QuestState qs = player.getQuestState(this);
		int state = 1;
		if(qs != null && qs.isStarted())
			state = 2;
		else if(qs != null && qs.isCompleted())
			state = 3;
		return state;
	}

	public String getName()
	{
		return _name;
	}

	public int getId()
	{
		return _id;
	}

	public QuestPartyType getPartyType()
	{
		return _partyType;
	}

	public QuestState newQuestState(Player player)
	{
		return new QuestState(this, player);
	}

	public void notifyAttack(NpcInstance npc, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onAttack(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
        showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public void notifyDeath(Creature killer, Creature victim, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
        showResult(null, qs.getPlayer(), res, showQuestInfo);
	}

	public void notifyEvent(String event, QuestState qs, NpcInstance npc)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onEvent(event, qs, npc);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
        showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public void notifyMenuSelect(int reply, QuestState qs, NpcInstance npc)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onMenuSelect(reply, qs, npc);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
        showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public void notifyKill(NpcInstance npc, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onKill(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
        showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public void notifyKill(Player target, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onKill(target, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
        showResult(null, qs.getPlayer(), res, showQuestInfo);
	}

	public final boolean notifyFirstTalk(NpcInstance npc, Player player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch(Exception e)
		{
			showError(player, e);
			return true;
		}
		return showResult(npc, player, res, true, false);
	}

	public boolean notifyTalk(NpcInstance npc, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			if(qs.isNotAccepted())
			{
				Set<Quest> quests = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
				if(quests != null && quests.contains(this))
					res = checkStartCondition(npc, qs.getPlayer());
			}
			if(res == null || res.isEmpty())
				res = onTalk(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
		return showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public boolean notifyCompleted(NpcInstance npc, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onCompleted(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
		return showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public boolean notifySkillUse(NpcInstance npc, Skill skill, QuestState qs)
	{
		boolean showQuestInfo = canShowQuestInfo(qs.getPlayer());
		String res = null;
		try
		{
			res = onSkillUse(npc, skill, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		showQuestInfo = showQuestInfo && canShowQuestInfo(qs.getPlayer());
		return showResult(npc, qs.getPlayer(), res, showQuestInfo);
	}

	public void notifySocialActionUse(QuestState qs, int actionId)
	{
		try
		{
			onSocialActionUse(qs, actionId);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
		}
	}

	public void notifyUpdateItem(ItemInstance item, QuestState qs)
	{
		try
		{
			updateItems(item, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
		}
	}

	public void notifyTutorialEvent(String event, String value, QuestState qs)
	{
		String res;
		try
		{
			res = onTutorialEvent(event, value, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}

		showTutorialResult(qs.getPlayer(), res);
	}

	public void onSocialActionUse(QuestState qs, int actionId)
	{}

	public void onRestore(QuestState qs)
	{}

	public void onAccept(QuestState qs)
	{}

	public void onExit(QuestState qs)
	{}

	public void onAbort(QuestState qs)
	{}

	public void onFinish(QuestState qs)
	{}

	public String onAttack(NpcInstance npc, QuestState qs)
	{
		return null;
	}

	public String onDeath(Creature killer, Creature victim, QuestState qs)
	{
		return null;
	}

	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		return null;
	}

	public String onMenuSelect(int reply, QuestState qs, NpcInstance npc)
	{
		return null;
	}

	public String onKill(NpcInstance npc, QuestState qs)
	{
		return null;
	}

	public String onKill(Player killed, QuestState st)
	{
		return null;
	}

	public String onFirstTalk(NpcInstance npc, Player player)
	{
		return null;
	}

	public String onTalk(NpcInstance npc, QuestState qs)
	{
		return null;
	}

	public String onCompleted(NpcInstance npc, QuestState qs)
	{
		return "completed";
	}

	public String onSkillUse(NpcInstance npc, Skill skill, QuestState qs)
	{
		return null;
	}

	public String onTutorialEvent(String event, String value, QuestState qs)
	{
		return null;
	}

	public boolean canAbortByPacket()
	{
		return true;
	}

	public void showError(Player player, Throwable t)
	{
		_log.error("", t);
		if(player != null && player.isGM())
		{
			String res = "<html><body><title>Script error</title>" + LogUtils.dumpStack(t).replace("\n", "<br>") + "</body></html>";
            showResult(null, player, res, false);
		}
	}

	protected void showHtmlFile(Player player, NpcInstance npc, String fileName, boolean showQuestInfo)
	{
        showHtmlFile(player, npc, fileName, showQuestInfo, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	protected void showHtmlFile(Player player, NpcInstance npc, String fileName, boolean showQuestInfo, Object... arg)
	{
		if(player == null)
			return;
		HtmlMessage npcReply = new HtmlMessage(npc == null ? 5 : npc.getObjectId());
		if(showQuestInfo)
			npcReply.setQuestId(getId());
		npcReply.setFile("quests/" + getClass().getSimpleName() + "/" + fileName);
		if(arg.length % 2 == 0)
			for(int i = 0; i < arg.length; i += 2)
				npcReply.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
		player.sendPacket(npcReply);
	}

	protected void showSimpleHtmFile(Player player, String fileName)
	{
		if(player == null)
			return;
		HtmlMessage npcReply = new HtmlMessage(5);
		npcReply.setFile(fileName);
		player.sendPacket(npcReply);
	}

	protected void showTutorialHtmlFile(Player player, String fileName, Object... arg)
	{
		if(player == null)
			return;
		String text = HtmCache.getInstance().getHtml("quests/" + getClass().getSimpleName() + "/tutorial/" + fileName, player);
		if (text == null)
			return;
		if(arg.length % 2 == 0)
			for(int i = 0; i < arg.length; i += 2)
				text = text.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
		player.sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.NORMAL_WINDOW, text));
	}

	private boolean showResult(NpcInstance npc, Player player, String res, boolean showQuestInfo)
	{
		return showResult(npc, player, res, false, showQuestInfo);
	}

	private boolean showResult(NpcInstance npc, Player player, String res, boolean isFirstTalk, boolean showQuestInfo)
	{
		if(res == null)
			return true;
		if(res.isEmpty())
			return false;
		if(res.startsWith("no_quest") || "noquest".equalsIgnoreCase(res) || "no-quest".equalsIgnoreCase(res))
			showSimpleHtmFile(player, "no-quest.htm");
		else if("completed".equalsIgnoreCase(res))
			showSimpleHtmFile(player, "completed-quest.htm");
		else if(res.endsWith(".htm"))
            showHtmlFile(player, npc, res, showQuestInfo);
		else
		{
			HtmlMessage npcReply = new HtmlMessage(npc == null ? 5 : npc.getObjectId()).setPlayVoice(isFirstTalk);
			npcReply.setHtml(res);
			if(showQuestInfo)
				npcReply.setQuestId(getId());
			player.sendPacket(npcReply);
		}
		return true;
	}

	public void showTutorialResult(Player player, String res)
	{
		if(res == null || res.isEmpty())
			return;
		if(res.endsWith(".htm"))
			showTutorialHtmlFile(player, res);
		else
			player.sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.NORMAL_WINDOW, res));
	}

	private boolean canShowQuestInfo(Player player)
	{
		QuestState qs = player.getQuestState(this);
		return isVisible(player) && (qs == null || qs.isNotAccepted());
	}

	void pauseQuestTimers(QuestState qs)
	{
		if(qs.getTimers().isEmpty())
			return;
		for(QuestTimer timer : qs.getTimers().values())
		{
			timer.setQuestState(null);
			timer.pause();
		}
		_pausedQuestTimers.put(qs.getPlayer().getObjectId(), qs.getTimers());
	}

	void resumeQuestTimers(QuestState qs)
	{
		Map<String, QuestTimer> timers = _pausedQuestTimers.remove(qs.getPlayer().getObjectId());
		if(timers == null)
			return;
		qs.getTimers().putAll(timers);
		for(QuestTimer timer : qs.getTimers().values())
		{
			timer.setQuestState(qs);
			timer.start();
		}
	}

	protected String str(long i)
	{
		return String.valueOf(i);
	}

	public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, int randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, new Location(x, y, z, heading), randomOffset, despawnDelay);
	}

	public NpcInstance addSpawn(int npcId, Location loc, int randomOffset, int despawnDelay)
	{
		return NpcUtils.spawnSingle(npcId, randomOffset > 50 ? Location.findPointToStay(loc, 50, randomOffset, ReflectionManager.MAIN.getGeoIndex()) : loc, despawnDelay);
	}

	public static NpcInstance addSpawnToInstance(int npcId, int x, int y, int z, int heading, int randomOffset, int refId)
	{
		return addSpawnToInstance(npcId, new Location(x, y, z, heading), randomOffset, refId);
	}

	public static NpcInstance addSpawnToInstance(int npcId, Location loc, int randomOffset, int refId)
	{
		try
		{
			NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
			if(template != null)
			{
				NpcInstance npc = NpcHolder.getInstance().getTemplate(npcId).getNewInstance();
				npc.setReflection(refId);
				npc.setSpawnedLoc(randomOffset > 50 ? Location.findPointToStay(loc, 50, randomOffset, npc.getGeoIndex()) : loc);
				npc.spawnMe(npc.getSpawnedLoc());
				return npc;
			}
		}
		catch(Exception e1)
		{
			_log.warn("Could not spawn Npc " + npcId);
		}
		return null;
	}

	public boolean isVisible(Player player)
	{
		return true;
	}

	public final boolean enterInstance(QuestState st, Reflection reflection, int instancedZoneId, Object... args)
	{
		Player player = st.getPlayer();
		if(player == null)
			return false;
		Optional<Reflection> activeReflection = player.getActiveReflection();
		if(activeReflection.isPresent())
		{
			if(player.canReenterInstance(instancedZoneId))
			{
				Reflection temp = activeReflection.get();
				player.teleToLocation(temp.getTeleportLoc(), temp);
				onReenterInstance(st, temp, args);
				return true;
			}
		}
		else if(player.canEnterInstance(instancedZoneId))
		{
			Reflection newReflection = ReflectionUtils.enterReflection(player, reflection, instancedZoneId);
			if(newReflection != null)
			{
				onEnterInstance(st, newReflection, args);
				return true;
			}
		}
		return false;
	}

	public final boolean enterInstance(QuestState st, int instancedZoneId, Object... args)
	{
		return enterInstance(st, new Reflection(), instancedZoneId, args);
	}

	public void onEnterInstance(QuestState st, Reflection reflection, Object[] args)
	{}

	public void onReenterInstance(QuestState st, Reflection reflection, Object[] args)
	{}

	public String checkStartCondition(int npcId, Player player)
	{
		for(Map.Entry<ICheckStartCondition, ConditionMessage> entry : _startConditions.entrySet())
		{
			ConditionMessage condMsg = entry.getValue();
			if((condMsg.getNpcId() == -1 || condMsg.getNpcId() == npcId) && !entry.getKey().checkCondition(player))
				return condMsg.getMessage();
		}
		return null;
	}

	public String checkStartCondition(NpcInstance npc, Player player)
	{
		if(npc != null)
			return checkStartCondition(npc.getNpcId(), player);
		String msg = checkStartCondition(-1, player);
		if(msg == null)
		{
			for(int npcId : _startNpcs.toArray())
			{
				msg = checkStartCondition(npcId, player);
				if(msg == null)
					return null;
			}
			return msg;
		}
		return msg;
	}

	public boolean checkStartNpc(NpcInstance npc, Player player)
	{
		return true;
	}

	public boolean checkMaxLevelCondition(Player player)
	{
		for(ICheckStartCondition startCondition : _startConditions.keySet())
		{
			if(!(startCondition instanceof PlayerLevelCondition))
				continue;
			if(!startCondition.checkCondition(player))
				return false;
		}
		return true;
	}

	public boolean checkTalkNpc(NpcInstance npc, QuestState st)
	{
		return true;
	}

	public double getRewardRate()
	{
		return _rewardRate;
	}

	public void onHaosBattleEnd(Player player, boolean isWinner)
	{}

	@Override
	public void onInit()
	{
		if(!Config.DONTLOADQUEST)
			QuestHolder.getInstance().addQuest(this);
	}

	static
	{
		_log = LoggerFactory.getLogger(Quest.class);
		PARTY_NONE = QuestPartyType.PARTY_NONE;
		PARTY_ONE = QuestPartyType.PARTY_ONE;
		PARTY_ALL = QuestPartyType.PARTY_ALL;
		COMMAND_CHANNEL = QuestPartyType.COMMAND_CHANNEL;
		ONETIME = QuestRepeatType.ONETIME;
		REPEATABLE = QuestRepeatType.REPEATABLE;
		DAILY = QuestRepeatType.DAILY;
	}

	private static class ConditionMessage
	{
		private final int _npcId;
		private final String _message;

		public ConditionMessage(int npcId, String message)
		{
			_npcId = npcId;
			_message = message;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public String getMessage()
		{
			return _message;
		}
	}
}
