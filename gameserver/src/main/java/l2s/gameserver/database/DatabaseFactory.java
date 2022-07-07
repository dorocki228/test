package l2s.gameserver.database;

import l2s.commons.database.AbstractDatabaseFactory;

public class DatabaseFactory extends AbstractDatabaseFactory {
	private static final DatabaseFactory INSTANCE = new DatabaseFactory();

	public static final DatabaseFactory getInstance() {
		return INSTANCE;
	}

	@Override
	protected String getConfigPath() {
		return "config/database.properties";
	}
}