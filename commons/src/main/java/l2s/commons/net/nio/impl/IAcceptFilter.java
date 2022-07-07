package l2s.commons.net.nio.impl;

import java.nio.channels.SocketChannel;

public interface IAcceptFilter
{
	boolean accept(SocketChannel p0);
}
