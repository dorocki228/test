package l2s.authserver.network.l2.c2s;


import com.google.common.flogger.FluentLogger;
import l2s.authserver.network.l2.L2LoginClient;
import l2s.commons.net.nio.impl.ReceivablePacket;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
			return false;
		}
	}

	@Override
	public void run()
	{
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
	}

	protected abstract boolean readImpl();

	protected abstract void runImpl() throws Exception;
}
