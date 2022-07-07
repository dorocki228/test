package l2s.gameserver.model.instances;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket;
import l2s.gameserver.network.l2.s2c.CIPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class DecoyInstance extends MonsterInstance
{
	private static final long serialVersionUID = 1L;
	private static final Logger _log;
	private final HardReference<Player> _playerRef;
	private int _lifeTime;
	private int _timeRemaining;
	private ScheduledFuture<?> _decoyLifeTask;
	private ScheduledFuture<?> _hateSpam;

	public DecoyInstance(int objectId, NpcTemplate template, Player owner, int lifeTime)
	{
		super(objectId, template, StatsSet.EMPTY);
		_playerRef = owner.getRef();
		_lifeTime = lifeTime;
		_timeRemaining = _lifeTime;
		int skilllevel = getNpcId() < 13257 ? getNpcId() - 13070 : getNpcId() - 13250;
		_decoyLifeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DecoyLifetime(), 1000L, 1000L);
		_hateSpam = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HateSpam(SkillHolder.getInstance().getSkillEntry(5272, skilllevel)), 1000L, 3000L);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		if(_hateSpam != null)
		{
			_hateSpam.cancel(false);
			_hateSpam = null;
		}
		_lifeTime = 0;
	}

	public void unSummon()
	{
		if(_decoyLifeTask != null)
		{
			_decoyLifeTask.cancel(false);
			_decoyLifeTask = null;
		}
		if(_hateSpam != null)
		{
			_hateSpam.cancel(false);
			_hateSpam = null;
		}
		deleteMe();
	}

	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}

	public int getTimeRemaining()
	{
		return _timeRemaining;
	}

	public int getLifeTime()
	{
		return _lifeTime;
	}

	@Override
	public Player getPlayer()
	{
		return _playerRef.get();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player owner = getPlayer();
		return owner != null && owner.isAutoAttackable(attacker);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		Player owner = getPlayer();
		return owner != null && owner.isAttackable(attacker);
	}

	@Override
	protected void onDelete()
	{
		Player owner = getPlayer();
		if(owner != null)
			owner.removeDecoy(this);
		super.onDelete();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
			player.setTarget(this);
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
	}

	//	@Override
	//	public double getCollisionRadius()
	//	{
	//		final Player player = getPlayer();
	//		if(player == null)
	//			return 0.0;
	//		return player.getCollisionRadius();
	//	}
	//
	//	@Override
	//	public double getCollisionHeight()
	//	{
	//		final Player player = getPlayer();
	//		if(player == null)
	//			return 0.0;
	//		return player.getCollisionHeight();
	//	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(!isInCombat())
			return Collections.singletonList(new CIPacket(this, forPlayer));
		List<L2GameServerPacket> list = new ArrayList<>(2);
		list.add(new CIPacket(this, forPlayer));
		list.add(new AutoAttackStartPacket(objectId));
		return list;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	public void transferOwnerBuffs()
	{
		Collection<Abnormal> effects = getPlayer().getAbnormalList().getEffects();
		for(Abnormal e : effects)
		{
			Skill skill = e.getSkill();
			if(!e.isOffensive() && !skill.isToggle())
			{
				if(skill.isCubicSkill())
					continue;

				Abnormal effect = e.getTemplate().getEffect(e.getEffector(), this, skill);
				if(effect == null)
					continue;
				if(effect.getTemplate().isInstant())
					continue;
				effect.setDuration(e.getDuration());
				effect.setTimeLeft(e.getTimeLeft());
				getAbnormalList().addEffect(effect);
			}
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(DecoyInstance.class);
	}

	class DecoyLifetime implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				decTimeRemaining(1000);
				double newTimeRemaining = getTimeRemaining();
				if(newTimeRemaining < 0.0)
					unSummon();
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
	}

	class HateSpam implements Runnable
	{
		private final SkillEntry _skillEntry;

		HateSpam(SkillEntry skillEntry)
		{
			_skillEntry = skillEntry;
		}

		@Override
		public void run()
		{
			try
			{
				setTarget(DecoyInstance.this);
				doCast(_skillEntry, DecoyInstance.this, true);
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
	}
}
