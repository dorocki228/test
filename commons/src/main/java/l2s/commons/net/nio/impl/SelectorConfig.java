package l2s.commons.net.nio.impl;

import java.nio.ByteOrder;

public class SelectorConfig
{
	public int READ_BUFFER_SIZE;
	public int WRITE_BUFFER_SIZE;
	public int MAX_SEND_PER_PASS;
	public long SLEEP_TIME;
	public long INTEREST_DELAY;
	public int HEADER_SIZE;
	public int PACKET_SIZE;
	public int HELPER_BUFFER_COUNT;
	public long AUTH_TIMEOUT;
	public long CLOSEWAIT_TIMEOUT;
	public int BACKLOG;
	public ByteOrder BYTE_ORDER;

	public SelectorConfig()
	{
		READ_BUFFER_SIZE = 65536;
		WRITE_BUFFER_SIZE = 131072;
		MAX_SEND_PER_PASS = 32;
		SLEEP_TIME = 10L;
		INTEREST_DELAY = 30L;
		HEADER_SIZE = 2;
		PACKET_SIZE = 32768;
		HELPER_BUFFER_COUNT = 64;
		AUTH_TIMEOUT = 30000L;
		CLOSEWAIT_TIMEOUT = 10000L;
		BACKLOG = 1024;
		BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	}
}
