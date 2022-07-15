package l2s.gameserver.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.flogger.FluentLogger;
import l2s.commons.dao.JdbcDAO;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dao.JdbcEntityStats;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MailDAO implements JdbcDAO<Integer, Mail>
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final static String RESTORE_MAIL = "SELECT sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread, returned, system_topic, system_body, system_params  FROM mail WHERE message_id = ?";
	private final static String STORE_MAIL = "INSERT INTO mail(sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread, returned, system_topic, system_body, system_params) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final static String UPDATE_MAIL = "UPDATE mail SET sender_id = ?, sender_name = ?, receiver_id = ?, receiver_name = ?, expire_time = ?, topic = ?, body = ?, price = ?, type = ?, unread = ?, returned = ?, system_topic = ?, system_body = ?, system_params = ? WHERE message_id = ?";
	private final static String REMOVE_MAIL = "DELETE FROM mail WHERE message_id = ?";

	private final static String RESTORE_EXPIRED_MAIL = "SELECT message_id FROM mail WHERE expire_time <= ?";

	private final static String RESTORE_OWN_MAIL = "SELECT message_id FROM character_mail WHERE char_id = ? AND is_sender = ?";
	private final static String STORE_OWN_MAIL = "INSERT INTO character_mail(char_id, message_id, is_sender) VALUES (?,?,?)";
	private final static String REMOVE_OWN_MAIL = "DELETE FROM character_mail WHERE char_id = ? AND message_id = ? AND is_sender = ?";
	private final static String REMOVE_OWN_MAIL2 = "DELETE FROM character_mail WHERE message_id = ?";

	private final static String RESTORE_MAIL_ATTACHMENTS = "SELECT item_id FROM mail_attachments WHERE message_id = ?";
	private final static String STORE_MAIL_ATTACHMENT = "INSERT INTO mail_attachments(message_id, item_id) VALUES (?,?)";
	private final static String REMOVE_MAIL_ATTACHMENTS = "DELETE FROM mail_attachments WHERE message_id = ?";

	private final static MailDAO instance = new MailDAO();

	public static MailDAO getInstance()
	{
		return instance;
	}

	private AtomicLong load = new AtomicLong();
	private AtomicLong insert = new AtomicLong();
	private AtomicLong update = new AtomicLong();
	private AtomicLong delete = new AtomicLong();

	private final LoadingCache<Integer, Mail> cache;

	private final JdbcEntityStats stats = new JdbcEntityStats()
	{
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

	private MailDAO()
	{
		cache = Caffeine.newBuilder()
				.maximumSize(100_000)
				.expireAfterAccess(1, TimeUnit.HOURS)
				.expireAfterWrite(8, TimeUnit.HOURS)
				.build(key ->
				{
					Mail mail;
					try
					{
						mail = load0(key);
					}
					catch(SQLException e)
					{
						_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while restoring mail : %s", key );
						return null;
					}

					if(mail == null)
						return null;

					mail.setJdbcState(JdbcEntityState.STORED);
					return mail;
				});
	}

	public LoadingCache<Integer, Mail> getCache()
	{
		return cache;
	}

	@Override
	public JdbcEntityStats getStats()
	{
		return stats;
	}

	private void save0(Mail mail) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(STORE_MAIL, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, mail.getSenderId());
			statement.setString(2, mail.getSenderName());
			statement.setInt(3, mail.getReceiverId());
			statement.setString(4, mail.getReceiverName());
			statement.setInt(5, mail.getExpireTime());
			statement.setString(6, mail.getTopic());
			statement.setString(7, mail.getBody());
			statement.setLong(8, mail.getPrice());
			statement.setInt(9, mail.getType().ordinal());
			statement.setBoolean(10, mail.isUnread());
			statement.setBoolean(11, mail.isReturned());
			statement.setInt(12, mail.getSystemTopic());
			statement.setInt(13, mail.getSystemBody());
			statement.setString(14, mail.getSystemParamsToString());
			statement.execute();

			rset = statement.getGeneratedKeys();
			rset.next();

			mail.setMessageId(rset.getInt(1));

			if(!mail.getAttachments().isEmpty())
			{
				DbUtils.close(statement);

				statement = con.prepareStatement(STORE_MAIL_ATTACHMENT);

				for(final ItemInstance localItemInstance : mail.getAttachments())
				{
					statement.setInt(1, mail.getMessageId());
					statement.setInt(2, localItemInstance.getObjectId());
					statement.addBatch();
				}

				statement.executeBatch();
			}

			DbUtils.close(statement);

			if(mail.getType() == Mail.SenderType.NORMAL)
			{
				statement = con.prepareStatement(STORE_OWN_MAIL);
				statement.setInt(1, mail.getSenderId());
				statement.setInt(2, mail.getMessageId());
				statement.setBoolean(3, true);
				statement.execute();
			}

			DbUtils.close(statement);

			statement = con.prepareStatement(STORE_OWN_MAIL);
			statement.setInt(1, mail.getReceiverId());
			statement.setInt(2, mail.getMessageId());
			statement.setBoolean(3, false);
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		insert.incrementAndGet();
	}

	private Mail load0(int messageId) throws SQLException
	{
		Mail mail = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_MAIL);
			statement.setInt(1, messageId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				mail = new Mail();
				mail.setMessageId(messageId);
				mail.setSenderId(rset.getInt(1));
				mail.setSenderName(rset.getString(2));
				mail.setReceiverId(rset.getInt(3));
				mail.setReceiverName(rset.getString(4));
				mail.setExpireTime(rset.getInt(5));
				mail.setTopic(rset.getString(6));
				mail.setBody(rset.getString(7));
				mail.setPrice(rset.getLong(8));
				mail.setType(Mail.SenderType.VALUES[rset.getInt(9)]);
				mail.setUnread(rset.getBoolean(10));
				mail.setReturned(rset.getBoolean(11));
				mail.setSystemTopic(rset.getInt(12));
				mail.setSystemBody(rset.getInt(13));
				mail.setSystemParams(rset.getString(14));

				DbUtils.close(statement, rset);

				statement = con.prepareStatement(RESTORE_MAIL_ATTACHMENTS);
				statement.setInt(1, messageId);
				rset = statement.executeQuery();

				ItemInstance item;
				int objectId;
				while(rset.next())
				{
					objectId = rset.getInt(1);
					item = ItemsDAO.getInstance().load(objectId);
					if(item != null)
						mail.addAttachment(item);
				}
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		load.incrementAndGet();

		return mail;
	}

	private void update0(Mail mail) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_MAIL);
			statement.setInt(1, mail.getSenderId());
			statement.setString(2, mail.getSenderName());
			statement.setInt(3, mail.getReceiverId());
			statement.setString(4, mail.getReceiverName());
			statement.setInt(5, mail.getExpireTime());
			statement.setString(6, mail.getTopic());
			statement.setString(7, mail.getBody());
			statement.setLong(8, mail.getPrice());
			statement.setInt(9, mail.getType().ordinal());
			statement.setBoolean(10, mail.isUnread());
			statement.setBoolean(11, mail.isReturned());
			statement.setInt(12, mail.getSystemTopic());
			statement.setInt(13, mail.getSystemBody());
			statement.setString(14, mail.getSystemParamsToString());
			statement.setInt(15, mail.getMessageId());
			statement.execute();

			if(mail.getAttachments().isEmpty())
			{
				DbUtils.close(statement);

				statement = con.prepareStatement(REMOVE_MAIL_ATTACHMENTS);
				statement.setInt(1, mail.getMessageId());
				statement.execute();
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		update.incrementAndGet();
	}

	private void delete0(Mail mail) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_MAIL);
			statement.setInt(1, mail.getMessageId());
			statement.execute();
			if(mail.getAttachments().isEmpty())
			{
				DbUtils.close(statement);

				statement = con.prepareStatement(REMOVE_MAIL_ATTACHMENTS);
				statement.setInt(1, mail.getMessageId());
				statement.execute();
			}

			DbUtils.close(statement);

			statement = con.prepareStatement(REMOVE_OWN_MAIL2);
			statement.setInt(1, mail.getMessageId());
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		delete.incrementAndGet();
	}

	private List<Mail> getMailByOwnerId(int ownerId, boolean sent)
	{
		List<Integer> messageIds = Collections.emptyList();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_OWN_MAIL);
			statement.setInt(1, ownerId);
			statement.setBoolean(2, sent);
			rset = statement.executeQuery();
			messageIds = new ArrayList<Integer>();
			while(rset.next())
				messageIds.add(rset.getInt(1));
		}
		catch(SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while restore mail of owner : %s", ownerId );
			messageIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return load(messageIds);
	}

	private boolean deleteMailByOwnerIdAndMailId(int ownerId, int messageId, boolean sent)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_OWN_MAIL);
			statement.setInt(1, ownerId);
			statement.setInt(2, messageId);
			statement.setBoolean(3, sent);
			return statement.execute();
		}
		catch(SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while deleting mail of owner : %s", ownerId );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public List<Mail> getReceivedMailByOwnerId(int receiverId)
	{
		return getMailByOwnerId(receiverId, false);
	}

	public List<Mail> getSentMailByOwnerId(int senderId)
	{
		return getMailByOwnerId(senderId, true);
	}

	public Mail getReceivedMailByMailId(int receiverId, int messageId)
	{
		List<Mail> list = getMailByOwnerId(receiverId, false);

		for(Mail mail : list)
			if(mail.getMessageId() == messageId)
				return mail;

		return null;
	}

	public Mail getSentMailByMailId(int senderId, int messageId)
	{
		List<Mail> list = getMailByOwnerId(senderId, true);

		for(Mail mail : list)
			if(mail.getMessageId() == messageId)
				return mail;

		return null;
	}

	public boolean deleteReceivedMailByMailId(int receiverId, int messageId)
	{
		return deleteMailByOwnerIdAndMailId(receiverId, messageId, false);
	}

	public boolean deleteSentMailByMailId(int senderId, int messageId)
	{
		return deleteMailByOwnerIdAndMailId(senderId, messageId, true);
	}

	public List<Mail> getExpiredMail(int expireTime)
	{
		List<Integer> messageIds = Collections.emptyList();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_EXPIRED_MAIL);
			statement.setInt(1, expireTime);
			rset = statement.executeQuery();
			messageIds = new ArrayList<Integer>();
			while(rset.next())
				messageIds.add(rset.getInt(1));
		}
		catch(SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while restore expired mail!" );
			messageIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return load(messageIds);
	}

	@Override
	public Mail load(Integer id)
	{
		return cache.get(id);
	}

	public List<Mail> load(Collection<Integer> messageIds)
	{
		if(messageIds.isEmpty())
			return Collections.emptyList();

		List<Mail> list = new ArrayList<Mail>(messageIds.size());

		Mail mail;
		for(Integer messageId : messageIds)
		{
			mail = load(messageId);
			if(mail != null)
				list.add(mail);
		}

		return list;
	}

	@Override
	public void save(Mail mail)
	{
		if(!mail.getJdbcState().isSavable())
			return;

		try
		{
			save0(mail);
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch(SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while saving mail!" );
			return;
		}

		cache.put(mail.getMessageId(), mail);
	}

	@Override
	public void update(Mail mail)
	{
		if(!mail.getJdbcState().isUpdatable())
			return;

		try
		{
			update0(mail);
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch(SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while updating mail : %s", mail.getMessageId() );
			return;
		}

		cache.get(mail.getMessageId(), (id) -> mail);
	}

	@Override
	public void saveOrUpdate(Mail mail)
	{
		if(mail.getJdbcState().isSavable())
			save(mail);
		else if(mail.getJdbcState().isUpdatable())
			update(mail);
	}

	@Override
	public void delete(Mail mail)
	{
		if(!mail.getJdbcState().isDeletable())
			return;

		try
		{
			delete0(mail);
			mail.setJdbcState(JdbcEntityState.DELETED);
		}
		catch(SQLException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while deleting mail : %s", mail.getMessageId() );
			return;
		}

		cache.invalidate(mail.getExpireTime());
	}
}
