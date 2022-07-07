package l2s.gameserver.taskmanager;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.DelayedItemsDAO;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.statistics.DelayedItemsStatistics;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class DelayedItemsManager implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(DelayedItemsManager.class);

	private static DelayedItemsManager _instance;

	private final Jdbi jdbi = DatabaseFactory.getInstance().getJdbi();
	private final DelayedItemsDAO delayedItemsDAO = jdbi.onDemand(DelayedItemsDAO.class);

	private static final Object _lock = new Object();
	private int last_payment_id;

	public static DelayedItemsManager getInstance()
	{
		if(_instance == null)
			_instance = new DelayedItemsManager();
		return _instance;
	}

	public DelayedItemsManager()
	{
		last_payment_id = 0;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			last_payment_id = get_last_payment_id(con);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		ThreadPoolManager.getInstance().schedule(this, 10000L);
	}

	private int get_last_payment_id(Connection con)
	{
		PreparedStatement st = null;
		ResultSet rset = null;
		int result = last_payment_id;
		try
		{
			st = con.prepareStatement("SELECT MAX(payment_id) AS last FROM items_delayed");
			rset = st.executeQuery();
			if(rset.next())
				result = rset.getInt("last");
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(st, rset);
		}
		return result;
	}

	@Override
	public void run()
	{
        Connection con = null;
		PreparedStatement st = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			int last_payment_id_temp = get_last_payment_id(con);
			if(last_payment_id_temp != last_payment_id)
				synchronized (_lock)
				{
					st = con.prepareStatement("SELECT DISTINCT owner_id FROM items_delayed WHERE payment_status=0 AND payment_id > ?");
					st.setInt(1, last_payment_id);
					rset = st.executeQuery();
                    Player player = null;
                    while(rset.next())
						if((player = GameObjectsStorage.getPlayer(rset.getInt("owner_id"))) != null)
							loadDelayed(player, true);
					last_payment_id = last_payment_id_temp;
				}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rset);
		}
		ThreadPoolManager.getInstance().schedule(this, 10000L);
	}

	public static void addDelayed(int objectId, int itemId, long itemCount, int enchant, String desc)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO items_delayed (owner_id, item_id, count, enchant_level, description) VALUES (?, ?, ?, ?, ?)");
			statement.setInt(1, objectId);
			statement.setInt(2, itemId);
			statement.setLong(3, itemCount);
			statement.setInt(4, enchant);
			statement.setString(5, desc);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.info("DelayedItemsManager.addDelayed(int, int, long): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int loadDelayed(Player player, boolean notify)
	{
		if(player == null)
			return 0;
		int player_id = player.getObjectId();
		PcInventory inv = player.getInventory();
		if(inv == null)
			return 0;
		int restored_counter = 0;
        synchronized (_lock)
		{
            ResultSet rset = null;
            PreparedStatement st_delete = null;
            PreparedStatement st = null;
            Connection con = null;
            try
			{
				con = DatabaseFactory.getInstance().getConnection();
				st = con.prepareStatement("SELECT * FROM items_delayed WHERE owner_id=? AND payment_status=0");
				st.setInt(1, player_id);
				rset = st.executeQuery();
				st_delete = con.prepareStatement("UPDATE items_delayed SET payment_status=1 WHERE payment_id=?");
				while(rset.next())
				{
					int ITEM_ID = rset.getInt("item_id");
					int PAYMENT_ID = rset.getInt("payment_id");
					ItemTemplate ITEM_TEMPLATE = ItemHolder.getInstance().getTemplate(ITEM_ID);
					if(ITEM_TEMPLATE != null)
					{
						long ITEM_COUNT = rset.getLong("count");
						int ITEM_ENCHANT = rset.getInt("enchant_level");
						int FLAGS = rset.getInt("flags");
						int ATTRIBUTE = rset.getInt("attribute");
						int ATTRIBUTE_LEVEL = rset.getInt("attribute_level");
						String DESCRIPTION = rset.getString("description");
						boolean stackable = ITEM_TEMPLATE.isStackable();
						boolean success = false;
						for(int i = 0; i < (stackable ? 1L : ITEM_COUNT); ++i)
						{
							if(ITEM_COUNT > 0L)
							{
								ItemInstance item = ItemFunctions.createItem(ITEM_ID);
								if(item.isStackable())
									item.setCount(ITEM_COUNT);
								else
									item.setEnchantLevel(ITEM_ENCHANT);
								item.setLocation(ItemInstance.ItemLocation.INVENTORY);
								item.setCustomFlags(FLAGS);
								ItemInstance newItem = inv.addItem(item);
								if(newItem == null)
								{
									_log.warn("Unable to delayed create item " + ITEM_ID + " request " + PAYMENT_ID);
									continue;
								}
								if(notify)
									player.sendPacket(SystemMessagePacket.obtainItems(ITEM_ID, stackable ? ITEM_COUNT : 1L, ITEM_ENCHANT));

								ItemLogMessage message = new ItemLogMessage(player,
										ItemLogProcess.DelayedItemReceive, newItem, ITEM_COUNT, 0, DESCRIPTION);
								LogService.getInstance().log(LoggerType.ITEM, message);
							}
							success = true;
							++restored_counter;
						}
						if(!success)
							continue;
					}
					st_delete.setInt(1, PAYMENT_ID);
					st_delete.execute();
				}
			}
			catch(Exception e)
			{
				_log.error("Could not load delayed items for player " + player + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(st_delete);
				DbUtils.closeQuietly(con, st, rset);
			}
		}
		return restored_counter;
	}

	public List<DelayedItemsStatistics> getStatistics() {
		return delayedItemsDAO.getDelayedItemsStatistics();
	}
}
