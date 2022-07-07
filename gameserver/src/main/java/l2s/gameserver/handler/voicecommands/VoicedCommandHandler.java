package l2s.gameserver.handler.voicecommands;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.handler.voicecommands.impl.*;

import java.util.HashMap;
import java.util.Map;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	private final Map<String, IVoicedCommandHandler> _datatable;

	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}

	private VoicedCommandHandler()
	{
		_datatable = new HashMap<>();
//		registerVoicedCommandHandler(new Away());
//		registerVoicedCommandHandler(new Help());
		registerVoicedCommandHandler(new Cfg());
		registerVoicedCommandHandler(new Offline());
		//registerVoicedCommandHandler(new Repair());
		//		registerVoicedCommandHandler(new ServerInfo());
		registerVoicedCommandHandler(new Wedding());
		//		registerVoicedCommandHandler(new Delevel());
		//		registerVoicedCommandHandler(new Online());
//		registerVoicedCommandHandler(new Password());
		registerVoicedCommandHandler(new Security());
		registerVoicedCommandHandler(new FactionLeaderCommands());
		registerVoicedCommandHandler(new WhoAmI());
		registerVoicedCommandHandler(new Debug());
		registerVoicedCommandHandler(Costumes.INSTANCE);
		registerVoicedCommandHandler(new Mercenary());
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] voicedCommandList = handler.getVoicedCommandList();
		for(String element : voicedCommandList)
			_datatable.put(element, handler);
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.contains(" "))
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		return _datatable.get(command);
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
}
