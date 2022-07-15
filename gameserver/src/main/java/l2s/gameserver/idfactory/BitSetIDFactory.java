package l2s.gameserver.idfactory;

import com.google.common.flogger.FluentLogger;
import l2s.commons.math.PrimeFinder;
import l2s.gameserver.ThreadPoolManager;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.flogger.LazyArgs.lazy;

@Deprecated
public class BitSetIDFactory extends IdFactory
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private BitSet freeIds;
	private AtomicInteger freeIdCount;
	private AtomicInteger nextFreeId;

	public class BitSetCapacityCheck implements Runnable
	{
		@Override
		public void run()
		{
			if(reachingBitSetCapacity())
				increaseBitSetCapacity();
		}
	}

	protected BitSetIDFactory()
	{
		super();
		initialize();

		ThreadPoolManager.getInstance().scheduleAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
	}

	private void initialize()
	{
		try
		{
			freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			freeIds.clear();
			freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);

			for(int usedObjectId : extractUsedObjectIdTable())
			{
				int objectID = usedObjectId - FIRST_OID;
				if(objectID < 0)
				{
					_log.atWarning().log( "Object ID %s in DB is less than minimum ID of %s", usedObjectId, FIRST_OID );
					continue;
				}
				freeIds.set(usedObjectId - FIRST_OID);
				freeIdCount.decrementAndGet();
			}

			nextFreeId = new AtomicInteger(freeIds.nextClearBit(0));
			initialized = true;

			_log.atInfo().log( "IdFactory: %s id\'s available.", lazy(() -> freeIds.size()) );
		}
		catch(Exception e)
		{
			initialized = false;
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "BitSet ID Factory could not be initialized correctly!" );
		}
	}

	@Override
	public synchronized void releaseId(int objectID)
	{
		if(objectID - FIRST_OID > -1)
		{
			freeIds.clear(objectID - FIRST_OID);
			freeIdCount.incrementAndGet();
			super.releaseId(objectID);
		}
		else
			_log.atWarning().log( "BitSet ID Factory: release objectID %s failed (< %s)", objectID, FIRST_OID );
	}

	@Override
	public synchronized int getNextId()
	{
		int newID = nextFreeId.get();
		freeIds.set(newID);
		freeIdCount.decrementAndGet();

		int nextFree = freeIds.nextClearBit(newID);

		if(nextFree < 0)
			nextFree = freeIds.nextClearBit(0);
		if(nextFree < 0)
			if(freeIds.size() < FREE_OBJECT_ID_SIZE)
				increaseBitSetCapacity();
			else
				throw new NullPointerException("Ran out of valid Id's.");

		nextFreeId.set(nextFree);

		return newID + FIRST_OID;
	}

	@Override
	public synchronized int size()
	{
		return freeIdCount.get();
	}

	protected synchronized int usedIdCount()
	{
		return size() - FIRST_OID;
	}

	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > freeIds.size();
	}

	protected synchronized void increaseBitSetCapacity()
	{
		BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(freeIds);
		freeIds = newBitSet;
	}
}