package l2s.gameserver.network.telnet;

import org.apache.commons.lang3.ArrayUtils;

public abstract class TelnetCommand implements Comparable<TelnetCommand>
{
	private final String command;
	private final String[] acronyms;

	public TelnetCommand(String command)
	{
		this(command, ArrayUtils.EMPTY_STRING_ARRAY);
	}

	public TelnetCommand(String command, String... acronyms)
	{
		this.command = command;
		this.acronyms = acronyms;
	}

	public String getCommand()
	{
		return command;
	}

	public String[] getAcronyms()
	{
		return acronyms;
	}

	public abstract String getUsage();

	public abstract String handle(String[] p0);

	public boolean equals(String command)
	{
		for(String acronym : acronyms)
			if(command.equals(acronym))
				return true;
		return this.command.equalsIgnoreCase(command);
	}

	@Override
	public String toString()
	{
		return command;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o == null || o instanceof TelnetCommand && command.equals(((TelnetCommand) o).command);
	}

	@Override
	public int compareTo(TelnetCommand o)
	{
		return command.compareTo(o.command);
	}
}
