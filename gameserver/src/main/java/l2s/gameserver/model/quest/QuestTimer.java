package l2s.gameserver.model.quest;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QuestTimer implements Runnable
{
	private final String _name;
	private final NpcInstance _npc;
	private long _time;
	private QuestState _qs;
	private ScheduledFuture<?> _schedule;

	public QuestTimer(String name, long time, NpcInstance npc)
	{
		_name = name;
		_time = time;
		_npc = npc;
	}

	void setQuestState(QuestState qs)
	{
		_qs = qs;
	}

	QuestState getQuestState()
	{
		return _qs;
	}

	void start()
	{
		_schedule = ThreadPoolManager.getInstance().schedule(this, _time);
	}

	@Override
	public void run()
	{
		QuestState qs = getQuestState();
		if(qs != null)
		{
			qs.removeQuestTimer(getName());
			qs.getQuest().notifyEvent(getName(), qs, getNpc());
		}
	}

	void pause()
	{
		if(_schedule != null)
		{
			_time = _schedule.getDelay(TimeUnit.SECONDS);
			_schedule.cancel(false);
		}
	}

	void stop()
	{
		if(_schedule != null)
			_schedule.cancel(false);
	}

	public boolean isActive()
	{
		return _schedule != null && !_schedule.isDone();
	}

	public String getName()
	{
		return _name;
	}

	public long getTime()
	{
		return _time;
	}

	public NpcInstance getNpc()
	{
		return _npc;
	}

	@Override
	public final String toString()
	{
		return _name;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o != null && o.getClass() == getClass() && ((QuestTimer) o).getName().equals(getName());
	}

	@Override
	public int hashCode()
	{
		return 3 * getName().hashCode() + 17570;
	}
}
