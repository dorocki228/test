package l2s.commons.net.nio.impl;

public interface IMMOExecutor<T extends MMOClient>
{
	void execute(Runnable p0);
}
