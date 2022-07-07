package l2s.gameserver.model.entity;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CustomHeroDAO;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.StatsSet;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Hero
{
	private static final Logger _log = LoggerFactory.getLogger(Hero.class);
	private static Hero _instance;
	private static final String GET_HEROES = "SELECT h.char_id AS char_id, h.count AS count, h.active AS active, c.char_name AS char_name, cs.class_id AS class_id FROM heroes AS h LEFT JOIN characters AS c ON c.obj_Id = h.char_id LEFT JOIN character_subclasses AS cs ON cs.char_obj_id = h.char_id AND cs.type=? WHERE char_name IS NOT NULL AND class_id IS NOT NULL AND played = 1";
	private static final String GET_ALL_HEROES = "SELECT h.char_id AS char_id, h.count AS count, h.active AS active, h.played AS played, c.char_name AS char_name, cs.class_id AS class_id FROM heroes AS h LEFT JOIN characters AS c ON c.obj_Id = h.char_id LEFT JOIN character_subclasses AS cs ON cs.char_obj_id = h.char_id AND cs.type=? WHERE char_name IS NOT NULL AND class_id IS NOT NULL";
	private static IntObjectMap<StatsSet> _heroes;
	private static IntObjectMap<StatsSet> _completeHeroes;
	private static IntObjectMap<List<HeroDiary>> _herodiary;
	private static IntObjectMap<String> _heroMessage;
	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	public static final String ACTIVE = "active";
	public static final String MESSAGE = "message";

	public static Hero getInstance()
	{
		if(_instance == null)
			_instance = new Hero();

		return _instance;
	}

	public Hero()
	{
		init();
	}

	private static void HeroSetClanAndAlly(int charId, StatsSet hero)
	{
		Map.Entry<Clan, Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(charId);
		hero.set("clan_crest", e.getKey() == null ? 0 : e.getKey().getCrestId());
		hero.set("clan_name", e.getKey() == null ? "" : e.getKey().getName());
		hero.set("ally_crest", e.getValue() == null ? 0 : e.getValue().getAllyCrestId());
		hero.set("ally_name", e.getValue() == null ? "" : e.getValue().getAllyName());
		e = null;
	}

	private void init()
	{
		_heroes = new CHashIntObjectMap<>();
		_completeHeroes = new CHashIntObjectMap<>();
		_herodiary = new CHashIntObjectMap<>();
		_heroMessage = new CHashIntObjectMap<>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{

			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_HEROES);
			statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt("char_id");
				hero.set("char_name", rset.getString("char_name"));
				hero.set("class_id", Olympiad.convertParticipantClassId(rset.getInt("class_id")));
				hero.set("count", rset.getInt("count"));
				hero.set("played", 1);
				hero.set("active", rset.getInt("active"));

				HeroSetClanAndAlly(charId, hero);

				loadDiary(charId);
				loadMessage(charId);

				_heroes.put(charId, hero);
			}

			DbUtils.close(statement, rset);

			statement = con.prepareStatement(GET_ALL_HEROES);
			statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
			rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt("char_id");
				hero.set("char_name", rset.getString("char_name"));
				hero.set("class_id", Olympiad.convertParticipantClassId(rset.getInt("class_id")));
				hero.set("count", rset.getInt("count"));
				hero.set("played", rset.getInt("played"));
				hero.set("active", rset.getInt("active"));

				HeroSetClanAndAlly(charId, hero);

				_completeHeroes.put(charId, hero);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt load Heroes", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		_log.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
		_log.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
	}

	public IntObjectMap<StatsSet> getHeroes()
	{
		return _heroes;
	}

	public synchronized void clearHeroes()
	{
		mysql.set("UPDATE heroes SET played = 0, active = 0");
		for(IntObjectPair<StatsSet> entry : _heroes.entrySet())
		{
			Player player = GameObjectsStorage.getPlayer(entry.getKey());
			if(entry.getValue().getInteger("active") == 0 || player == null)
				continue;

			player.setHero(CustomHeroDAO.getInstance().isCustomHero(player.getObjectId()));
			player.checkAndDeleteOlympiadItems();
			player.updatePledgeRank();
			player.broadcastUserInfo(true);
		}
		_heroes.clear();
		_herodiary.clear();
	}

	public synchronized boolean computeNewHeroes(List<StatsSet> newHeroes)
	{
		if(newHeroes.isEmpty()){ return true; }
		CHashIntObjectMap<StatsSet> heroes = new CHashIntObjectMap<>();
		for(StatsSet hero : newHeroes)
		{
			int charId = hero.getInteger("char_id");
			if(_completeHeroes != null && _completeHeroes.containsKey(charId))
			{
				StatsSet oldHero = _completeHeroes.get(charId);
				int count = oldHero.getInteger("count");
				oldHero.set("count", count + 1);
				oldHero.set("played", 1);
				oldHero.set("active", 0);
				heroes.put(charId, oldHero);
			}
			else
			{
				StatsSet newHero = new StatsSet();
				newHero.set("char_name", hero.getString("char_name"));
				newHero.set("class_id", hero.getInteger("class_id"));
				newHero.set("count", 1);
				newHero.set("played", 1);
				newHero.set("active", 0);
				heroes.put(charId, newHero);
			}
			addHeroDiary(charId, 2, 0);
			loadDiary(charId);
		}
		_heroes.putAll(heroes);
		heroes.clear();
		updateHeroes(0);
		return false;
	}

	public void updateHeroes(int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO heroes (char_id, count, played, active) VALUES (?,?,?,?)");
			for(int heroId : _heroes.keySet().toArray())
			{
				if(id > 0 && heroId != id)
					continue;

				StatsSet hero = _heroes.get(heroId);
				statement.setInt(1, heroId);
				statement.setInt(2, hero.getInteger("count"));
				statement.setInt(3, hero.getInteger("played"));
				statement.setInt(4, hero.getInteger("active"));
				statement.execute();

				if(_completeHeroes == null || _completeHeroes.containsKey(heroId))
					continue;

				HeroSetClanAndAlly(heroId, hero);
				_completeHeroes.put(heroId, hero);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt update Heroes");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isHero(int id)
	{
		if(_heroes == null || _heroes.isEmpty())
			return false;
		if(_heroes.containsKey(id) && _heroes.get(id).getInteger("active") == 1)
			return true;
		return false;
	}

	public boolean isInactiveHero(int id)
	{
		if(_heroes == null || _heroes.isEmpty())
			return false;
		if(_heroes.containsKey(id) && _heroes.get(id).getInteger("active") == 0)
			return true;
		return false;
	}

	public void activateHero(Player player)
	{
		StatsSet hero = _heroes.get(player.getObjectId());
		if(hero == null)
			return;

		hero.set("active", 1);
		player.checkHeroSkills();
		player.setHero(true);
		player.updatePledgeRank();
		player.broadcastPacket(new SocialActionPacket(player.getObjectId(), 20016));

		if(player.getClan() != null && player.getClan().getLevel() >= 5)
		{
			player.getClan().incReputation(2000, true, "Hero:activateHero:" + player);
			player.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1776).addString(player.getName()).addNumber(Math.round(2000.0 * Config.RATE_CLAN_REP_SCORE)), player);
		}

		player.broadcastUserInfo(true);
		updateHeroes(player.getObjectId());
	}

	public void loadDiary(int charId)
	{
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
            ArrayList<HeroDiary> diary = new ArrayList<>();
            while(rset.next())
			{
				long time = rset.getLong("time");
				int action = rset.getInt("action");
				int param = rset.getInt("param");
				HeroDiary d = new HeroDiary(action, time, param);
				diary.add(d);
			}
			_herodiary.put(charId, diary);
			if(Config.DEBUG)
			{
				_log.info("Hero System: Loaded " + diary.size() + " diary entries for Hero(object id: #" + charId + ")");
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt load Hero Diary for CharId: " + charId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void showHeroDiary(Player activeChar, int heroclass, int charid, int page)
	{
		StatsSet hero = _heroes.get(charid);

		if(hero == null)
			return;

        List<?> mainlist = _herodiary.get(charid);
		if(mainlist != null)
		{
			HtmlMessage html = new HtmlMessage(5);
			html.setFile("olympiad/monument_hero_info.htm");
			html.replace("%title%", StringsHolder.getInstance().getString(activeChar, "hero.diary"));
			html.replace("%heroname%", hero.getString("char_name"));
			String message = _heroMessage.get(charid);
			html.replace("%message%", message == null ? "" : message);
			ArrayList<?> list = new ArrayList<Object>(mainlist);
			Collections.reverse(list);
			boolean color = true;
			StringBuilder fList = new StringBuilder(500);
			int counter = 0;
			int breakat = 0;
            int perpage = 10;
            for(int i = (page - 1) * perpage; i < list.size(); ++i)
			{
				breakat = i;
				HeroDiary diary = (HeroDiary) list.get(i);
				Map.Entry<String, String> entry = diary.toString(activeChar);
				fList.append("<tr><td>");
				if(color)
				{
					fList.append("<table width=270 bgcolor=\"131210\">");
				}
				else
				{
					fList.append("<table width=270>");
				}
				fList.append("<tr><td width=270><font color=\"LEVEL\">" + entry.getKey() + "</font></td></tr>");
				fList.append("<tr><td width=270>" + entry.getValue() + "</td></tr>");
				fList.append("<tr><td>&nbsp;</td></tr></table>");
				fList.append("</td></tr>");
				color = !color;
				if(++counter >= 10)
					break;
			}
			if(breakat < list.size() - 1)
			{
				html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.replace("%prev_bypass%", "_diary?class=" + heroclass + "&page=" + (page + 1));
			}
			else
			{
				html.replace("%buttprev%", "");
			}
			if(page > 1)
			{
				html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.replace("%next_bypass%", "_diary?class=" + heroclass + "&page=" + (page - 1));
			}
			else
			{
				html.replace("%buttnext%", "");
			}
			html.replace("%list%", fList.toString());
			activeChar.sendPacket(html);
		}
	}

	public void addHeroDiary(int playerId, int id, int param)
	{
		insertHeroDiary(playerId, id, param);
		List<HeroDiary> list = _herodiary.get(playerId);
		if(list != null)
			list.add(new HeroDiary(id, System.currentTimeMillis(), param));
	}

	private void insertHeroDiary(int charId, int action, int param)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
			statement.setInt(1, charId);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, action);
			statement.setInt(4, param);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.error("SQL exception while saving DiaryData.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void loadMessage(int charId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
            con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT message FROM heroes WHERE char_id=?");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
			rset.next();
            String message = rset.getString("message");
            _heroMessage.put(charId, message);
		}
		catch(SQLException e)
		{
			_log.error("Hero System: Couldnt load Hero Message for CharId: " + charId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void setHeroMessage(int charId, String message)
	{
		_heroMessage.put(charId, message);
	}

	public void saveHeroMessage(int charId)
	{
		if(_heroMessage.get(charId) == null)
			return;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE heroes SET message=? WHERE char_id=?;");
			statement.setString(1, _heroMessage.get(charId));
			statement.setInt(2, charId);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.error("SQL exception while saving HeroMessage.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void shutdown()
	{
		for(int charId : _heroMessage.keySet().toArray())
		{
			saveHeroMessage(charId);
		}
	}

	public int getHeroByClass(int classid)
	{
		if(!_heroes.isEmpty())
		{
			for(int heroId : _heroes.keySet().toArray())
			{
				StatsSet hero = _heroes.get(heroId);
				if(hero.getInteger("class_id") != classid)
					continue;
				return heroId;
			}
		}
		return 0;
	}

	public IntObjectPair<StatsSet> getHeroStats(int classId)
	{
		for(IntObjectPair<StatsSet> entry : _heroes.entrySet())
		{
			if(entry.getValue().getInteger("class_id") != classId)
				continue;
			return entry;
		}
		return null;
	}
}
