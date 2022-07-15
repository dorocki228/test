package l2s.gameserver.handler.pccoupon;

import static com.google.common.flogger.LazyArgs.lazy;

import com.google.common.flogger.FluentLogger;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author Bonux
**/
public class PCCouponHandler
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	
	private static final PCCouponHandler _instance = new PCCouponHandler();

	private final IntObjectMap<IPCCouponHandler> _handlers = new HashIntObjectMap<IPCCouponHandler>();

	public static PCCouponHandler getInstance()
	{
		return _instance;
	}

	private PCCouponHandler()
	{
		//
	}

	public void registerHandler(IPCCouponHandler handler)
	{
		if(_handlers.containsKey(handler.getType()))
		{
			_log.atWarning().log( "%s: dublicate bypass registered! First handler: %s second: %s", getClass().getSimpleName(), _handlers.get(handler.getType()).getClass().getSimpleName(), handler.getClass().getSimpleName() );
			return;
		}
		_handlers.put(handler.getType(), handler);
	}

	public void removeHandler(IPCCouponHandler handler)
	{
		if(_handlers.remove(handler.getType()) != null)
			_log.atInfo().log( "%s: %s unloaded.", lazy(() -> getClass().getSimpleName()), lazy(() -> handler.getClass().getSimpleName()) );
	}

	public IPCCouponHandler getHandler(int type)
	{
		return _handlers.get(type);
	}
}
