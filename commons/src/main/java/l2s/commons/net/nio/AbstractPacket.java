package l2s.commons.net.nio;

import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

import static org.apache.logging.log4j.LogManager.getLogger;

public abstract class AbstractPacket<T>
{
	protected static final Logger LOGGER = getLogger(AbstractPacket.class);

	protected abstract ByteBuffer getByteBuffer();

	public abstract T getClient();
}
