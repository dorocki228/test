package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Scripts;
import org.apache.commons.lang3.ClassUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class AdminScripts implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanReload)
			return false;
		switch(command)
		{
			case admin_run_script:
			case admin_runs:
			{
				if(wordList.length < 2)
					return false;
				String param = wordList[1];
				if(!run(param))
				{
					activeChar.sendMessage("Can't run script.");
					break;
				}
				activeChar.sendMessage("Running script...");
				break;
			}
		}
		return true;
	}

	private boolean run(String target)
	{
		Path file = Config.SCRIPTS_PATH.resolve(target.replace(".", "/") + ".java");
		if(Files.notExists(file))
			return false;
		List<? extends Class<?>> classes = Scripts.getInstance().loadScripts(file);
		Iterator<? extends Class<?>> iterator = classes.iterator();
		if(!iterator.hasNext())
			return false;
		Class<?> clazz = iterator.next();
		if(!ClassUtils.isAssignable(clazz, Runnable.class))
			return false;
		Runnable r;
		try
		{
			r = (Runnable) clazz.getDeclaredConstructor().newInstance();
		}
		catch(Exception e)
		{
			return false;
		}
		ThreadPoolManager.getInstance().execute(r);
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private enum Commands
	{
		admin_run_script,
		admin_runs
    }
}
