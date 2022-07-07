package l2s.gameserver.handler.chat;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.network.l2.components.ChatType;

public class ChatHandler extends AbstractHolder
{
	private static final ChatHandler _instance;
	private final IChatHandler[] _handlers;

	public static ChatHandler getInstance()
	{
		return _instance;
	}

	private ChatHandler()
	{
		_handlers = new IChatHandler[ChatType.VALUES.length];
	}

	public void register(IChatHandler chatHandler)
	{
		_handlers[chatHandler.getType().ordinal()] = chatHandler;
	}

	public IChatHandler getHandler(ChatType type)
	{
		return _handlers[type.ordinal()];
	}

	@Override
	public int size()
	{
		return _handlers.length;
	}

	@Override
	public void clear()
	{}

	static
	{
		_instance = new ChatHandler();
	}
}
