package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class EffectsDAO
{
	private static final int SUMMON_SKILL_OFFSET = 100000;
	private static final Logger _log;
	private static final EffectsDAO _instance;

	public static EffectsDAO getInstance()
	{
		return _instance;
	}

	public void restoreEffects(Playable playable)
	{
		int objectId;
		int id;
		if(playable.isPlayer())
		{
			objectId = playable.getObjectId();
			id = ((Player) playable).getActiveClassId();
		}
		else
		{
			if(!playable.isServitor())
				return;
			objectId = playable.getPlayer().getObjectId();
			id = ((Servitor) playable).getEffectIdentifier();
			if(playable.isSummon())
			{
				id += 100000;
				id *= 10;
				id += playable.getPlayer().getSummonsCount();
			}
		}
		Connection con = null;
        try
		{
			con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`duration`,`left_time`,`is_self` FROM `character_effects_save` WHERE `object_id`=? AND `id`=?");
            statement.setInt(1, objectId);
			statement.setInt(2, id);
            ResultSet rset = statement.executeQuery();
            while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLvl);
				if(skill == null)
					continue;
				boolean isSelf = rset.getInt("is_self") > 0;
				int duration = rset.getInt("duration");
				int leftTime = rset.getInt("left_time");
				List<EffectTemplate> effectTemplates = skill.getEffectTemplates(isSelf ? EffectUseType.SELF : EffectUseType.NORMAL);
				for(EffectTemplate et : effectTemplates)
				{
					Abnormal effect = et.getEffect(playable, playable, skill);
					if(effect != null && effect.isSaveable())
					{
						if(effect.getTemplate().isInstant())
							continue;
						effect.setDuration(duration);
						effect.setTimeLeft(leftTime);
						playable.getAbnormalList().addEffect(effect);
					}
				}
			}
			DbUtils.closeQuietly(statement, rset);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id = ? AND id=?");
			statement.setInt(1, objectId);
			statement.setInt(2, id);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(Exception e)
		{
			_log.error("Could not restore active effects data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	public void insert(Playable playable)
	{
		int objectId;
		int id;
		if(playable.isPlayer())
		{
			objectId = playable.getObjectId();
			id = ((Player) playable).getActiveClassId();
		}
		else
		{
			if(!playable.isServitor())
				return;
			objectId = playable.getPlayer().getObjectId();
			id = ((Servitor) playable).getEffectIdentifier();
			if(playable.isSummon())
			{
				id += 100000;
				id *= 10;
				id += playable.getPlayer().getSummonsCount();
			}
		}
		Abnormal[] effects = playable.getAbnormalList().getFirstEffects();
		if(effects.length == 0)
			return;
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_effects_save` (`object_id`,`skill_id`,`skill_level`,`duration`,`left_time`,`id`,`is_self`) VALUES");
			for(Abnormal effect : effects)
				if(effect != null)
					if(effect.isOfUseType(EffectUseType.SELF) || effect.isOfUseType(EffectUseType.NORMAL))
					{
						if(effect.isSaveable())
						{
							StringBuilder sb = new StringBuilder("(");
							sb.append(objectId).append(",");
							sb.append(effect.getSkill().getId()).append(",");
							sb.append(effect.getSkill().getLevel()).append(",");
							sb.append(effect.getDuration()).append(",");
							sb.append(effect.getTimeLeft()).append(",");
							sb.append(id).append(",");
							sb.append(effect.isOfUseType(EffectUseType.SELF) ? 1 : 0).append(")");
							b.write(sb.toString());
						}
						while((effect = effect.getNext()) != null && effect.isSaveable())
						{
							StringBuilder sb = new StringBuilder("(");
							sb.append(objectId).append(",");
							sb.append(effect.getSkill().getId()).append(",");
							sb.append(effect.getSkill().getLevel()).append(",");
							sb.append(effect.getDuration()).append(",");
							sb.append(effect.getTimeLeft()).append(",");
							sb.append(id).append(",");
							sb.append(effect.isOfUseType(EffectUseType.SELF) ? 1 : 0).append(")");
							b.write(sb.toString());
						}
					}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.error("Could not store active effects data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(EffectsDAO.class);
		_instance = new EffectsDAO();
	}
}
