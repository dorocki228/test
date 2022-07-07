package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.BossInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BaiumInstance extends BossInstance
{
	private static final long LOCK_RESET_INTERVAL = TimeUnit.MINUTES.toMillis(30);
	private static final int LOCK_CHANNEL_MEMBERS_COUNT = 90;
	private final AtomicBoolean _channelLocked = new AtomicBoolean();
	private ScheduledFuture<?> _channelLockCheckTask;
	private CommandChannel _lockedCommandChannel;
	private long _lastAttackTimeStamp;

	public BaiumInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	protected void onChannelLock(String leaderName)
	{
		broadcastPacket(new ExShowScreenMessage(NpcString.S1_S_COMMAND_CHANNEL_HAS_LOOTING_RIGHTS, 4000, ScreenMessageAlign.TOP_CENTER, true, 1, -1, false, leaderName));
	}

	protected void onChannelUnlock()
	{
		broadcastPacket(new ExShowScreenMessage(NpcString.LOOTING_RULES_ARE_NO_LONGER_ACTIVE, 4000, ScreenMessageAlign.TOP_CENTER, true, 1, -1, false));
	}

	private final class ChannelLockCheckTask implements Runnable
	{
		@Override
		public final void run()
		{
			long nextCheckInterval = LOCK_RESET_INTERVAL - (System.currentTimeMillis() - _lastAttackTimeStamp);
			if(nextCheckInterval < 1000L || _lockedCommandChannel == null || _lockedCommandChannel.getChannelLeader() == null)
			{
				_channelLockCheckTask = null;
				_lockedCommandChannel = null;
				_channelLocked.set(false);
				onChannelUnlock();
				return;
			}
			_channelLockCheckTask = ThreadPoolManager.getInstance().schedule(this, nextCheckInterval);
		}
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		_lastAttackTimeStamp = System.currentTimeMillis();

		Player activePlayer = attacker.getPlayer();
		if(activePlayer != null)
		{
			PlayerGroup pg = activePlayer.getPlayerGroup();
			if(pg instanceof CommandChannel)
			{
				CommandChannel cc = (CommandChannel) pg;
				Player leader = cc.getChannelLeader();
				if(!_channelLocked.get() && leader != null && cc.getMemberCount() >= LOCK_CHANNEL_MEMBERS_COUNT)
				{
					if(_channelLocked.compareAndSet(false, true))
					{
						_lockedCommandChannel = cc;
						_channelLockCheckTask = ThreadPoolManager.getInstance().schedule(new ChannelLockCheckTask(), LOCK_RESET_INTERVAL);
						onChannelLock(leader.getName());
					}
				}
			}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	@Override
	protected Creature lockDropTo(Creature topDamager)
	{
		CommandChannel cc = _lockedCommandChannel;
		if(cc != null)
		{
			Player leader = cc.getChannelLeader();
			if(leader != null)
				return leader;
		}

		return topDamager;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(_channelLockCheckTask != null)
		{
			_channelLockCheckTask.cancel(false);
			_channelLockCheckTask = null;
		}
		super.onDeath(killer);
	}

	@Override
	protected void onDecay()
	{
		if(_channelLockCheckTask != null)
		{
			_channelLockCheckTask.cancel(false);
			_channelLockCheckTask = null;
		}
		_lockedCommandChannel = null;
		_channelLocked.set(false);
		super.onDecay();
	}
}