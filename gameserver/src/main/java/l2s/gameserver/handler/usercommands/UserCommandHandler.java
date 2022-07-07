package l2s.gameserver.handler.usercommands;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.handler.usercommands.impl.*;

public class UserCommandHandler extends AbstractHolder
{
	private static final UserCommandHandler _instance;
	private final TIntObjectHashMap<IUserCommandHandler> _datatable;

	public static UserCommandHandler getInstance()
	{
		return _instance;
	}

	private UserCommandHandler()
	{
		_datatable = new TIntObjectHashMap();
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new CommandChannel());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new MyBirthday());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new InstanceZone());
		registerUserCommandHandler(new Time());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new TargetNextNpc());
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] userCommandList;
		int[] ids = userCommandList = handler.getUserCommandList();
		for(int element : userCommandList)
			_datatable.put(element, handler);
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return _datatable.get(userCommand);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}

	static
	{
		_instance = new UserCommandHandler();
	}
}
