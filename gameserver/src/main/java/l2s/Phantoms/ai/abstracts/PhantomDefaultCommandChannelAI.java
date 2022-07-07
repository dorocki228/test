package  l2s.Phantoms.ai.abstracts;


import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.ai.tasks.party.PartyTask;
import  l2s.gameserver.ThreadPoolManager;
import  l2s.gameserver.templates.StatsSet;

public abstract class PhantomDefaultCommandChannelAI
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomDefaultCommandChannelAI.class);
	protected ScheduledFuture <?> _mainTask = null;
	protected ScheduledFuture <?> _subTask = null;
	
	public PhantomDefaultCommandChannelAI(StatsSet set)
	{
		
	}
	
	public abstract void doAction();
	
	public void startAITask(long delay)
	{
		try
		{
			abortMainTask();
			_mainTask = ThreadPoolManager.getInstance().PhantomAiScheduleAtFixedRate(new PartyTask(1), delay, delay);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initSubTask(Runnable r, long delay)
	{
		try
		{
			abortSubTask();
			_subTask = ThreadPoolManager.getInstance().schedule(r, delay);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void abortMainTask()
	{
		if (_mainTask != null)
		{
			_mainTask.cancel(true);
			_mainTask = null;
		}
	}
	
	public boolean getSubTask()
	{
		if (_subTask != null)
			return true;
		return false;
	}
	
	public void abortSubTask()
	{
		if (_subTask != null)
		{
			_subTask.cancel(true);
			_subTask = null;
		}
	}
	
}
