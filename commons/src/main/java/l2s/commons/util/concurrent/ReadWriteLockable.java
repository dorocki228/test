package l2s.commons.util.concurrent;

public interface ReadWriteLockable
{
	void writeLock();

	void writeUnlock();

	void readLock();

	void readUnlock();
}
