package l2s.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class MMOClient<T extends MMOConnection<?>>
{
	private T _connection;
	private boolean isAuthed;

	public MMOClient(T con)
	{
        _connection = con;
	}

	protected void setConnection(T con)
	{
        _connection = con;
	}

	public T getConnection()
	{
		return _connection;
	}

	public boolean isAuthed()
	{
		return isAuthed;
	}

	public void setAuthed(boolean isAuthed)
	{
		this.isAuthed = isAuthed;
	}

	public void closeNow(boolean error)
	{
		T conn = _connection;
		if(conn != null && !conn.isClosed())
			conn.closeNow();
	}

	public void closeLater()
	{
		T conn = _connection;
		if(conn != null && !conn.isClosed())
			conn.closeLater();
	}

	public boolean isConnected()
	{
		T conn = _connection;
		return conn != null && !conn.isClosed();
	}

    public abstract boolean isValid();

	public abstract boolean decrypt(ByteBuffer p0, int p1);

	public abstract boolean encrypt(ByteBuffer p0, int p1);

	protected void onDisconnection()
	{}

	protected void onForcedDisconnection()
	{}
}
