package l2s.gameserver.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import l2s.commons.dao.JdbcDAO;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dao.JdbcEntityStats;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ItemsDAO implements JdbcDAO<Integer, ItemInstance>
{
	private static final Logger _log = LoggerFactory.getLogger(ItemsDAO.class);

	private static final String RESTORE_ITEM = "SELECT object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, augmentation_mineral_id, augmentation_id1, augmentation_id2, visual_id, visual_item_obj_id FROM items WHERE object_id = ?";
	private static final String RESTORE_OWNER_ITEMS = "SELECT object_id FROM items WHERE owner_id = ? AND loc = ?";
	private static final String STORE_ITEM = "INSERT INTO items (object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, augmentation_mineral_id, augmentation_id1, augmentation_id2, visual_id, visual_item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_ITEM = "UPDATE items SET owner_id = ?, item_id = ?, count = ?, enchant_level = ?, loc = ?, loc_data = ?, custom_type1 = ?, custom_type2 = ?, life_time = ?, custom_flags = ?, augmentation_mineral_id = ?, augmentation_id1 = ?, augmentation_id2 = ?, visual_id = ?, visual_item_obj_id = ? WHERE object_id = ?";
	private static final String REMOVE_ITEM = "DELETE FROM items WHERE object_id = ?";
	private static final String INSERT_GLOBAL_REMOVE_ITEM = "REPLACE INTO items_to_delete (item_id,description) VALUES (?,?)";

	private static final ItemsDAO instance = new ItemsDAO();

    private final AtomicLong load;
	private final AtomicLong insert;
	private final AtomicLong update;
	private final AtomicLong delete;
	private final JdbcEntityStats stats;

    private final LoadingCache<Integer, ItemInstance> cache;

	public static final ItemsDAO getInstance()
	{
		return instance;
	}

	private ItemsDAO()
	{
		load = new AtomicLong();
		insert = new AtomicLong();
		update = new AtomicLong();
		delete = new AtomicLong();
		stats = new JdbcEntityStats(){
			@Override
			public long getLoadCount()
			{
				return load.get();
			}

			@Override
			public long getInsertCount()
			{
				return insert.get();
			}

			@Override
			public long getUpdateCount()
			{
				return update.get();
			}

			@Override
			public long getDeleteCount()
			{
				return delete.get();
			}
		};

        cache = Caffeine.newBuilder()
                .maximumSize(200000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(key ->
                {
                    ItemInstance item;
                    try
                    {
                        item = load0(key);
                        if(item == null)
                            return null;

                        item.setJdbcState(JdbcEntityState.STORED);
                    }
                    catch(SQLException e)
                    {
                        _log.error("Error while restoring item : " + key, e);
                        return null;
                    }

                    return item;
                });
	}

	public LoadingCache<Integer, ItemInstance> getCache()
	{
		return cache;
	}

	@Override
	public JdbcEntityStats getStats()
	{
		return stats;
	}

	private ItemInstance load0(int objectId) throws SQLException
	{
		ItemInstance item = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_ITEM);
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			item = load0(rset);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		load.incrementAndGet();
		return item;
	}

	private ItemInstance load0(ResultSet rset) throws SQLException
	{
		ItemInstance item = null;
		if(rset.next())
		{
			int objectId = rset.getInt(1);
			item = new ItemInstance(objectId);
			item.setOwnerId(rset.getInt(2));
			item.setItemId(rset.getInt(3));
			item.setCount(rset.getLong(4));
			item.setEnchantLevel(rset.getInt(5));
			item.setLocName(rset.getString(6));
			item.setLocData(rset.getInt(7));
			item.setCustomType1(rset.getInt(8));
			item.setCustomType2(rset.getInt(9));
			item.setLifeTime(rset.getInt(10));
			item.setCustomFlags(rset.getInt(11));
			item.setAugmentation(rset.getInt(12), new int[] {rset.getInt(13), rset.getInt(14)});
			item.setVisualId(rset.getInt(15));
			item.setVisualItemObjId(rset.getInt(16));
		}
		return item;
	}

	private void save0(ItemInstance item, PreparedStatement statement) throws SQLException
	{
		statement.setInt(1, item.getObjectId());
		statement.setInt(2, item.getOwnerId());
		statement.setInt(3, item.getItemId());
		statement.setLong(4, item.getCount());
		statement.setInt(5, item.getEnchantLevel());
		statement.setString(6, item.getLocName());
		statement.setInt(7, item.getLocData());
		statement.setInt(8, item.getCustomType1());
		statement.setInt(9, item.getCustomType2());
		statement.setInt(10, item.getLifeTime());
		statement.setInt(11, item.getCustomFlags());
		statement.setInt(12, item.getAugmentationMineralId());
		statement.setInt(13, item.getAugmentations()[0]);
		statement.setInt(14, item.getAugmentations()[1]);
		statement.setInt(15, item.getVisualId());
		statement.setInt(16, item.getVisualItemObjId());
	}

	private void save0(ItemInstance item) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(STORE_ITEM);
            save0(item, statement);
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		insert.incrementAndGet();
	}

	private void delete0(ItemInstance item, PreparedStatement statement) throws SQLException
	{
		statement.setInt(1, item.getObjectId());
	}

	private void delete0(ItemInstance item) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_ITEM);
            delete0(item, statement);
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		delete.incrementAndGet();
	}

	private void update0(ItemInstance item, PreparedStatement statement) throws SQLException
	{
		statement.setInt(1, item.getOwnerId());
		statement.setInt(2, item.getItemId());
		statement.setLong(3, item.getCount());
		statement.setInt(4, item.getEnchantLevel());
		statement.setString(5, item.getLocName());
		statement.setInt(6, item.getLocData());
		statement.setInt(7, item.getCustomType1());
		statement.setInt(8, item.getCustomType2());
		statement.setInt(9, item.getLifeTime());
		statement.setInt(10, item.getCustomFlags());
		statement.setInt(11, item.getAugmentationMineralId());
		statement.setInt(12, item.getAugmentations()[0]);
		statement.setInt(13, item.getAugmentations()[1]);
        statement.setInt(14, item.getVisualId());
        statement.setInt(15, item.getVisualItemObjId());
		statement.setInt(16, item.getObjectId());
	}

	private void update0(ItemInstance item) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_ITEM);
            update0(item, statement);
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		update.incrementAndGet();
	}

	@Override
	public ItemInstance load(Integer objectId)
	{
        return cache.get(objectId);
	}

	public Collection<ItemInstance> load(Collection<Integer> objectIds)
	{
		Collection<ItemInstance> list = Collections.emptyList();
		if(objectIds.isEmpty())
			return list;
		list = new ArrayList<>(objectIds.size());
		for(Integer objectId : objectIds)
		{
			ItemInstance item = load(objectId);
			if(item != null)
				list.add(item);
		}
		return list;
	}

	@Override
	public void save(ItemInstance item)
	{
		if(!item.getJdbcState().isSavable())
			return;

		try
		{
            save0(item);
			item.setJdbcState(JdbcEntityState.STORED);
		}
		catch(SQLException e)
		{
			_log.error("Error while saving item : " + item, e);
			return;
		}

		cache.put(item.getObjectId(), item);
	}

	public void save(Collection<ItemInstance> items)
	{
		if(items.isEmpty())
			return;
		for(ItemInstance item : items)
            save(item);
	}

	@Override
	public void update(ItemInstance item)
	{
		if(!item.getJdbcState().isUpdatable())
			return;

		try
		{
            update0(item);
			item.setJdbcState(JdbcEntityState.STORED);
		}
		catch(SQLException e)
		{
			_log.error("Error while updating item : " + item, e);
			return;
		}

		cache.put(item.getObjectId(), item);
	}

	public void update(Collection<ItemInstance> items)
	{
		if(items.isEmpty())
			return;

		for(ItemInstance item : items)
            update(item);
	}

	@Override
	public void saveOrUpdate(ItemInstance item)
	{
		if(item.getJdbcState().isSavable())
            save(item);
		else if(item.getJdbcState().isUpdatable())
            update(item);
	}

	public void saveOrUpdate(Collection<ItemInstance> items)
	{
		if(items.isEmpty())
			return;

		for(ItemInstance item : items)
            saveOrUpdate(item);
	}

	@Override
	public void delete(ItemInstance item)
	{
		if(!item.getJdbcState().isDeletable())
			return;

		try
		{
            delete0(item);
			item.setJdbcState(JdbcEntityState.DELETED);
		}
		catch(SQLException e)
		{
			_log.error("Error while deleting item : " + item, e);
			return;
		}

		cache.invalidate(item.getObjectId());
	}

	public void delete(Collection<ItemInstance> items)
	{
		if(items.isEmpty())
			return;

		for(ItemInstance item : items)
            delete(item);
	}

	public Collection<ItemInstance> getItemsByOwnerIdAndLoc(int ownerId, ItemInstance.ItemLocation loc)
	{
		Collection<Integer> objectIds = Collections.emptyList();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_OWNER_ITEMS);
			statement.setInt(1, ownerId);
			statement.setString(2, loc.name());
			rset = statement.executeQuery();
			objectIds = new ArrayList<>();
			while(rset.next())
				objectIds.add(rset.getInt(1));
		}
		catch(SQLException e)
		{
			_log.error("Error while restore items of owner : " + ownerId, e);
			objectIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return load(objectIds);
	}

	public void glovalRemoveItem(int itemId, String description)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_GLOBAL_REMOVE_ITEM);
			statement.setInt(1, itemId);
			statement.setString(2, description);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.error("Error while global remove item: " + itemId + "(" + description + ")", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

}
