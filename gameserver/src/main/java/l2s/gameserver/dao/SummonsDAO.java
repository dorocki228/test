package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SummonsDAO
{
	private static final Logger _log;
	private static final SummonsDAO _instance;

	public static SummonsDAO getInstance()
	{
		return _instance;
	}

	public List<SummonInstance.RestoredSummon> restore(Player player)
	{
		List<SummonInstance.RestoredSummon> result = new ArrayList<>();
		Connection con = null;
        try
		{
			int objectId = player.getObjectId();
			con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`curHp`,`curMp`,`time` FROM `character_summons_save` WHERE `owner_obj_id`=? ORDER BY `summon_index` ASC");
            statement.setInt(1, objectId);
            ResultSet rset = statement.executeQuery();
            while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int curHp = rset.getInt("curHp");
				int curMp = rset.getInt("curMp");
				int time = rset.getInt("time");
				result.add(new SummonInstance.RestoredSummon(skillId, skillLvl, curHp, curMp, time));
			}
			DbUtils.closeQuietly(statement, rset);
			statement = con.prepareStatement("DELETE FROM character_summons_save WHERE owner_obj_id = ?");
			statement.setInt(1, objectId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(Exception e)
		{
			_log.error("Could not restore active summon data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return result;
	}

	public void insert(SummonInstance summon)
	{
		if(!summon.isSaveable())
			return;
		Player owner = summon.getPlayer();
		if(owner == null)
			return;
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_summons_save` (`owner_obj_id`,`skill_id`,`summon_index`,`skill_level`,`curHp`,`curMp`,`time`) VALUES");
			StringBuilder sb = new StringBuilder("(");
			sb.append(owner.getObjectId()).append(",");
			sb.append(summon.getSkillId()).append(",");
			sb.append(owner.getSummonsCount()).append(",");
			sb.append(summon.getSkillLvl()).append(",");
			sb.append(summon.getCurrentHp()).append(",");
			sb.append(summon.getCurrentMp()).append(",");
			sb.append(summon.getConsumeCountdown()).append(")");
			b.write(sb.toString());
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.error("Could not store active summon data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(SummonsDAO.class);
		_instance = new SummonsDAO();
	}
}
