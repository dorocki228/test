package l2s.commons.dao;

import java.io.Serializable;

public interface JdbcDAO<K extends Serializable, E extends JdbcEntity>
{
	E load(K p0);

	void save(E p0);

	void update(E p0);

	void saveOrUpdate(E p0);

	void delete(E p0);

	JdbcEntityStats getStats();
}
