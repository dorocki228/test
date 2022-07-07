package l2s.gameserver.dao;

import l2s.gameserver.statistics.DelayedItemsStatistics;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface DelayedItemsDAO
{
    @SqlQuery("SELECT characters.account_name as login, sum(items_delayed.price) as donateAmount" +
            " FROM lin2world.characters left JOIN" +
            " items_delayed ON characters.obj_Id = items_delayed.owner_id where items_delayed.payment_status = 1" +
            " GROUP BY characters.account_name")
    @RegisterConstructorMapper(DelayedItemsStatistics.class)
    List<DelayedItemsStatistics> getDelayedItemsStatistics();
}
