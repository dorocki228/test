package l2s.commons.dao;

import java.io.Serializable;

public interface JdbcEntity extends Serializable
{
	void setJdbcState(JdbcEntityState p0);

	JdbcEntityState getJdbcState();

	void save();

	void update();

	void delete();
}
