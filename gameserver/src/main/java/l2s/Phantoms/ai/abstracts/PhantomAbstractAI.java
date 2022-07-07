package  l2s.Phantoms.ai.abstracts;


import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.ai.tasks.other.BuffTask;
import  l2s.gameserver.ThreadPoolManager;
import  l2s.gameserver.model.Player;

public abstract class PhantomAbstractAI
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomAbstractAI.class);
	
	protected ScheduledFuture <?> _aiTask = null;
	protected ScheduledFuture <?> _buffTask = null;

	public abstract void startAITask(long delay);
	
	protected Player actor;
	
	public void startAITask(Runnable r, long delay)
	{
		try
		{
			abortAITask();
			_aiTask = ThreadPoolManager.getInstance().PhantomAiScheduleAtFixedRate(r, delay, delay);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void abortAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
	}
	
	public boolean StatusAITask()
	{
		if (_aiTask != null)
			return true;
		return false;
	}
	
	
	
	/****************** end *********************/
	
	public boolean StatusBuffTask()
	{
		if (_buffTask != null && !_buffTask.isDone() && !_buffTask.isCancelled())
			return true;
		return false;
	}
	
	public void startBuffTask(long init_delay)
	{
		try
		{
			abortBuffTask();
			_buffTask = ThreadPoolManager.getInstance().PhantomScheduleAtFixedRate(new BuffTask(actor),init_delay, 1800000);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void abortBuffTask()
	{
		if (_buffTask != null)
		{
			_buffTask.cancel(true);
			_buffTask = null;
		}
	}
	
	public void setActor(Player ac)
	{
		actor = ac;
	}
	
	public Player getActor()
	{
		return actor;
	}
	
	public boolean isMelee()
	{
		return false;
	}
	
	public boolean isNuker()
	{
		return false;
	}
	
	public boolean isHealer()
	{
		return false;
	}
	
	public boolean isSupport()
	{
		return false;
	}
	
	public boolean isDisabler()
	{
		return false;
	}
	
	public boolean isTank()
	{
		return false;
	}
}
