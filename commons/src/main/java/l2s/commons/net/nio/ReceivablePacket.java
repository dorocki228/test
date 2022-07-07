package l2s.commons.net.nio;

import org.apache.commons.lang3.StringUtils;

import java.nio.BufferUnderflowException;

public abstract class ReceivablePacket<T> extends AbstractPacket<T> implements Runnable
{
	protected int getAvaliableBytes()
	{
		return getByteBuffer().remaining();
	}

	protected void readB(byte[] dst)
	{
		getByteBuffer().get(dst);
	}

	protected void readB(byte[] dst, int offset, int len)
	{
		getByteBuffer().get(dst, offset, len);
	}

	protected int readC()
	{
		return getByteBuffer().get() & 0xFF;
	}

	protected int readH()
	{
		return getByteBuffer().getShort() & 0xFFFF;
	}

	protected int readD()
	{
		return getByteBuffer().getInt();
	}

	protected long readQ()
	{
		return getByteBuffer().getLong();
	}

	protected double readF()
	{
		return getByteBuffer().getDouble();
	}

	protected String readS()
	{
		if (getByteBuffer().remaining() < 2) {
			return StringUtils.EMPTY;
		}

		StringBuilder sb = new StringBuilder();

		try
		{
			while(true) {
				char ch = getByteBuffer().getChar();
				if (ch == 0) {
					break;
				}

				sb.append(ch);

				if (getByteBuffer().remaining() < 2) {
					break;
				}
			}
		}
		catch(BufferUnderflowException e)
		{
			LOGGER.error("Can't read string.", e);

			return StringUtils.EMPTY;
		}

		return sb.toString();
	}

	protected abstract boolean read();
}
