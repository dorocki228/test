package l2s.gameserver.model.entity.olympiad;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.OlympiadParticipantsDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import org.napile.primitive.maps.impl.HashIntIntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OlympiadDatabase
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadDatabase.class);

	public static synchronized void loadParticipantsRank()
	{
		HashIntIntMap tmpPlace = new HashIntIntMap();

		Olympiad._participantRank.clear();
		for(int heroId : Hero.getInstance().getHeroes().keySet().toArray())
			Olympiad._participantRank.put(heroId, 1);

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OlympiadParticipantsDAO.GET_ALL_CLASSIFIED_PARTICIPANTS);
			rset = statement.executeQuery();
			int place = 1;
			while(rset.next())
			{
				int charId = rset.getInt("char_id");
				if(Olympiad._participantRank.containsKey(charId))
					continue;

				tmpPlace.put(charId, place++);
			}
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Error!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.1);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.5);

		if(rank1 == 0)
		{
			rank1 = 1;
			++rank2;
			++rank3;
			++rank4;
		}

		for(int charId : tmpPlace.keySet().toArray())
		{
			if(tmpPlace.get(charId) <= rank1)
			{
				Olympiad._participantRank.put(charId, 2);
				continue;
			}

			if(tmpPlace.get(charId) <= rank2)
			{
				Olympiad._participantRank.put(charId, 3);
				continue;
			}

			if(tmpPlace.get(charId) <= rank3)
			{
				Olympiad._participantRank.put(charId, 4);
				continue;
			}

			if(tmpPlace.get(charId) <= rank4)
			{
				Olympiad._participantRank.put(charId, 5);
				continue;
			}

			Olympiad._participantRank.put(charId, 6);
		}
	}

	public static synchronized void cleanupParticipants()
	{
		_log.info("Olympiad: Calculating last period...");

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OlympiadParticipantsDAO.OLYMPIAD_CALCULATE_LAST_PERIOD);
			statement.setInt(1, Config.OLYMPIAD_BATTLES_FOR_REWARD);
			statement.execute();

			DbUtils.close(statement);

			statement = con.prepareStatement(OlympiadParticipantsDAO.OLYMPIAD_CLEANUP_PARTICIPANTS);
			statement.setInt(1, Config.OLYMPIAD_POINTS_DEFAULT);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Couldn't calculate last period!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		for(OlympiadParticipiantData participantsInfo : Olympiad.getParticipantsMap().values())
		{
			int points = participantsInfo.getPoints();
			int compDone = participantsInfo.getCompDone();
			int compWin = participantsInfo.getCompWin();

			participantsInfo.setPoints(Config.OLYMPIAD_POINTS_DEFAULT);

			if(compDone >= Config.OLYMPIAD_BATTLES_FOR_REWARD)
			{
				points = compWin > 0 ? (points += Config.OLYMPIAD_1_OR_MORE_WIN_POINTS_BONUS) : (points += Config.OLYMPIAD_ALL_LOOSE_POINTS_BONUS);
				participantsInfo.setPointsPast(points);
				participantsInfo.setPointsPastStatic(points);
			}
			else
			{
				participantsInfo.setPointsPast(0);
				participantsInfo.setPointsPastStatic(0);
			}

			participantsInfo.setCompDone(0);
			participantsInfo.setCompWin(0);
			participantsInfo.setCompLoose(0);
			participantsInfo.setClassedGamesCount(0);
			participantsInfo.setNonClassedGamesCount(0);
		}
	}

	public static synchronized List<StatsSet> computeHeroesToBe()
	{
        if(Olympiad._period != 1)
			return Collections.emptyList();

        ArrayList<StatsSet> heroesToBe = new ArrayList<>();
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(ClassId cid : ClassId.VALUES)
			{
				ClassId parent = cid.getParent();
				if(!cid.isOfLevel(ClassLevel.THIRD) || !parent.isOfLevel(ClassLevel.SECOND))
					continue;

				statement = con.prepareStatement(OlympiadParticipantsDAO.OLYMPIAD_GET_HEROS);
				statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
				statement.setInt(2, parent.getId());
				statement.setInt(3, cid.getId());
				statement.setInt(4, Config.OLYMPIAD_BATTLES_FOR_REWARD);
				rset = statement.executeQuery();
				if(rset.next())
				{
					StatsSet hero = new StatsSet();
					hero.set("class_id", rset.getInt("class_id"));
					hero.set("char_id", rset.getInt("char_id"));
					hero.set("char_name", rset.getString("char_name"));
					heroesToBe.add(hero);
				}
				DbUtils.close(statement, rset);
			}
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Couldnt heros from db!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return heroesToBe;
	}

	public static List<String> getClassLeaderBoard(int classId)
	{
		ArrayList<String> names = new ArrayList<>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OlympiadParticipantsDAO.GET_EACH_CLASS_LEADER);
			statement.setInt(1, SubClassType.BASE_CLASS.ordinal());
			statement.setInt(2, classId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				names.add(rset.getString("char_name"));
			}
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Couldnt get heros from db!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return names;
	}

	public static synchronized void saveParticipantData(int participantId)
	{
		OlympiadParticipantsDAO.getInstance().replace(participantId);
	}

	public static synchronized void saveParticipantsData()
	{
		for(int participantId : Olympiad.getParticipantsMap().keySet().toArray())
			saveParticipantData(participantId);
	}

	public static synchronized void deleteParticipantData(int participantId)
	{
		OlympiadParticipantsDAO.getInstance().delete(participantId);
	}

	public static synchronized void setNewOlympiadStartTime()
	{
		Announcements.announceToAll(new SystemMessage(SystemMsg.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(Olympiad._currentCycle));
		Olympiad.setOlympiadPeriodStartTime(System.currentTimeMillis());
		Olympiad.setWeekStartTime(System.currentTimeMillis());
		Olympiad._isOlympiadEnd = false;
	}

	public static void save()
	{
		saveParticipantsData();
		ServerVariables.set("Olympiad_CurrentCycle", Olympiad._currentCycle);
		ServerVariables.set("Olympiad_Period", Olympiad._period);
		ServerVariables.set("olympiad_period_start_time", Olympiad.getOlympiadPeriodStartTime());
		ServerVariables.set("olympiad_validation_start_time", Olympiad.getValidationStartTime());
		ServerVariables.set("olympiad_week_start_time", Olympiad.getWeekStartTime());
	}
}
