package l2s.commons.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract database factory with usage of HikariCP.
 *
 * @author Java-man
 */
public abstract class AbstractDatabaseFactory
{
    protected final Logger logger = LogManager.getLogger(getClass());

    private final HikariDataSource connectionPool;

    private final Jdbi jdbi;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public AbstractDatabaseFactory()
    {
        try
        {
            String configPath = getConfigPath();
            HikariConfig config = new HikariConfig(configPath);
            connectionPool = new HikariDataSource(config);

            logger.info("Database connection working.");
        }
        catch(RuntimeException e)
        {
            throw new IllegalArgumentException("Could not init database connection.", e);
        }

        jdbi = Jdbi.create(connectionPool);
        jdbi.installPlugin(new SqlObjectPlugin());

        jdbcTemplate = createJdbcTemplate();
        transactionTemplate = createTransactionTemplate();
    }

    private JdbcTemplate createJdbcTemplate()
    {
        return new JdbcTemplate(connectionPool);
    }

    private SimpleJdbcInsert createJdbcInsertTemplate() {
        return new SimpleJdbcInsert(connectionPool);
    }

    private TransactionTemplate createTransactionTemplate()
    {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(connectionPool);
        return new TransactionTemplate(transactionManager);
    }

    public void shutdown()
    {
        connectionPool.close();
    }

    public Connection getConnection()
    {
        try
        {
            return connectionPool.getConnection();
        }
        catch(SQLException e)
        {
            logger.warn("Can't get connection from database", e);
        }

        return null;
    }

    public HikariDataSource getDataSource() {
        return connectionPool;
    }

    public Jdbi getJdbi()
    {
        return jdbi;
    }

    @Deprecated
    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    public SimpleJdbcInsert getJdbcInsert() {
        return createJdbcInsertTemplate();
    }

    @Deprecated
    public TransactionTemplate getTransactionTemplate()
    {
        return transactionTemplate;
    }

    protected abstract String getConfigPath();
}
