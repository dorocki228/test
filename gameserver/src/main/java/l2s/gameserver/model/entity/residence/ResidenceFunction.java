package l2s.gameserver.model.entity.residence;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.ResidenceFunctionType;
import l2s.gameserver.templates.residence.ResidenceFunctionTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

/**
 * @author Bonux
**/
public class ResidenceFunction
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final ResidenceFunctionTemplate _template;
	private final int _residenceId;
	private final Calendar _endDate = Calendar.getInstance();

	private boolean _inDebt = false;

	public ResidenceFunction(ResidenceFunctionTemplate template, int residenceId)
	{
		_template = template;
		_residenceId = residenceId;
	}

	public int getId()
	{
		return _template.getId();
	}

	public ResidenceFunctionType getType()
	{
		return _template.getType();
	}

	public int getLevel()
	{
		return _template.getLevel();
	}

	public ResidenceFunctionTemplate getTemplate()
	{
		return _template;
	}

	public int getResidenceId()
	{
		return _residenceId;
	}

	public long getEndTimeInMillis()
	{
		return _endDate.getTimeInMillis();
	}

	public void setEndTimeInMillis(long time)
	{
		_endDate.setTimeInMillis(time);
	}

	public void setInDebt(boolean inDebt)
	{
		_inDebt = inDebt;
	}

	public boolean isInDebt()
	{
		return _inDebt;
	}

	public void updateRentTime(boolean inDebt)
	{
		setEndTimeInMillis(System.currentTimeMillis() + 86400000);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE residence_functions SET end_time=?, in_debt=? WHERE residence_id=? AND type=? AND level=?");
			statement.setInt(1, (int) (getEndTimeInMillis() / 1000));
			statement.setInt(2, inDebt ? 1 : 0);
			statement.setInt(3, getResidenceId());
			statement.setInt(4, getType().ordinal());
			statement.setInt(5, getLevel());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Cannot update rent time: " );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}