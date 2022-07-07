package l2s.commons.net.nio.impl;

public interface IClientFactory<T extends MMOClient>
{
	T create(MMOConnection<T> p0);
}
