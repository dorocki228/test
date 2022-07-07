package l2s.gameserver.model;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CloneAI;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.listener.actor.OnAttackListener;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.player.PlayerTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class FakePlayer extends Playable
{
	private static final long serialVersionUID = -7275714049223105460L;
	private final Player _owner;
	private final OwnerAttakListener _listener;
	private ScheduledFuture<?> _broadcastCharInfoTask;

	public FakePlayer(int objectId, PlayerTemplate template, Player owner)
	{
		super(objectId, template);
		_owner = owner;
		_ai = new CloneAI(this);
		owner.addListener(_listener = new OwnerAttakListener());
		ThreadPoolManager.getInstance().schedule(new DeleteMeTimer(this), 30000L);
	}

	@Override
	public Player getPlayer()
	{
		return _owner;
	}

	@Override
	public CloneAI getAI()
	{
		return (CloneAI) _ai;
	}

	@Override
	public int getLevel()
	{
		return _owner.getLevel();
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return _owner.getActiveWeaponInstance();
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		return _owner.getActiveWeaponTemplate();
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return _owner.getSecondaryWeaponInstance();
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		return _owner.getSecondaryWeaponTemplate();
	}

	public void setFollowMode(boolean state)
	{
		Player owner = getPlayer();
		if(getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, FakePlayer.class, this, true))
			return;
		Player owner = getPlayer();
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelectedPacket(getObjectId(), 0), makeStatusUpdate(9, 10, 11, 12));
			else
				player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(player == owner)
		{
			player.sendPacket(new CIPacket(this, player));
			player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
		{
			if(!shift)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
			else
				player.sendActionFailed();
		}
		else
			player.sendActionFailed();
	}

	@Override
	public void broadcastCharInfo()
	{
		if(!isVisible())
			return;
		if(_broadcastCharInfoTask != null)
			return;
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
		for(Player player : World.getAroundObservers(this))
			player.sendPacket(new CIPacket(this, player));
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(isInvisible(forPlayer) && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();
		List<L2GameServerPacket> list = new ArrayList<>();
		list.add(new CIPacket(this, forPlayer));
		if(isCastingNow())
		{
			Creature castingTarget = getCastingTarget();
			Skill castingSkill = getCastingSkill();
			long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && !castingSkill.isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0L)
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0L));
		}
		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));
		if(isMoving() || isFollowing())
			list.add(movePacket());
		return list;
	}

	public void notifyOwerStartAttak(Creature targets)
	{
		getAI().Attack(targets, true, false);
	}

	public void notifyOwerStartMagicUse(Creature targets, Skill skill)
	{
		if(SkillAcquireHolder.getInstance().isSkillPossible(getPlayer(), skill))
			doCast(skill.getEntry(), targets, true);
	}

	@Override
	public <E extends Event> E getEvent(Class<E> eventClass)
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvent(eventClass);
		return super.getEvent(eventClass);
	}

	@Override
	public Set<Event> getEvents()
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvents();
		return super.getEvents();
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}

	@Override
	public Inventory getInventory()
	{
		return null;
	}

	@Override
	public long getWearedMask()
	{
		return 0L;
	}

	@Override
	public void doPickupItem(GameObject object)
	{}

	@Override
	public double getCurrentHp()
	{
		return _owner.getCurrentHp();
	}

	@Override
	public double getCurrentMp()
	{
		return _owner.getCurrentMp();
	}

	@Override
	public int getINT()
	{
		return _owner.getINT();
	}

	@Override
	public int getSTR()
	{
		return _owner.getSTR();
	}

	@Override
	public int getCON()
	{
		return _owner.getCON();
	}

	@Override
	public int getMEN()
	{
		return _owner.getMEN();
	}

	@Override
	public int getDEX()
	{
		return _owner.getDEX();
	}

	@Override
	public int getWIT()
	{
		return _owner.getWIT();
	}

	@Override
	public int getPEvasionRate(Creature target)
	{
		return _owner.getPEvasionRate(target);
	}

	@Override
	public int getMEvasionRate(Creature target)
	{
		return _owner.getMEvasionRate(target);
	}

	@Override
	public int getPAccuracy()
	{
		return _owner.getPAccuracy();
	}

	@Override
	public int getMAccuracy()
	{
		return _owner.getMAccuracy();
	}

	@Override
	public int getPCriticalHit(Creature target)
	{
		return _owner.getPCriticalHit(target);
	}

	@Override
	public int getMCriticalHit(Creature target, Skill skill)
	{
		return _owner.getMCriticalHit(target, skill);
	}

	@Override
	public double getCurrentCp()
	{
		return _owner.getCurrentCp();
	}

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return _owner.getMAtk(target, skill);
	}

	@Override
	public int getMAtkSpd()
	{
		return _owner.getMAtkSpd();
	}

	@Override
	public int getMaxCp()
	{
		return _owner.getMaxCp();
	}

	@Override
	public int getMaxHp()
	{
		return _owner.getMaxHp();
	}

	@Override
	public int getMaxMp()
	{
		return _owner.getMaxMp();
	}

	@Override
	public int getMDef(Creature target, Skill skill)
	{
		return _owner.getMDef(target, skill);
	}

	@Override
	public int getPAtk(Creature target)
	{
		return _owner.getPAtk(target);
	}

	@Override
	public int getPAtkSpd()
	{
		return _owner.getPAtkSpd();
	}

	@Override
	public int getPDef(Creature target)
	{
		return _owner.getPDef(target);
	}

	@Override
	public int getPhysicalAttackRange()
	{
		return _owner.getPhysicalAttackRange();
	}

	@Override
	public int getRandomDamage()
	{
		return _owner.getRandomDamage();
	}

	@Override
	public TeamType getTeam()
	{
		return _owner.getTeam();
	}

	@Override
	public int getMoveSpeed()
	{
		return _owner.getMoveSpeed();
	}

	@Override
	public int getPvpFlag()
	{
		return _owner.getPvpFlag();
	}

	@Override
	public int getNameColor()
	{
		return _owner.getNameColor();
	}

	@Override
	public int getKarma()
	{
		return _owner.getKarma();
	}

	public class BroadcastCharInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	private class OwnerAttakListener implements OnAttackListener, OnMagicUseListener
	{
		@Override
		public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
		{
			if(target != null && target == getPlayer())
				return;
			if(target != null && target instanceof FakePlayer && target.getPlayer() == getPlayer())
				return;
			notifyOwerStartMagicUse(target, skill);
		}

		@Override
		public void onAttack(Creature actor, Creature target)
		{
			if(target != null && target == getPlayer())
				return;
			if(target != null && target instanceof FakePlayer && target.getPlayer() == getPlayer())
				return;
			notifyOwerStartAttak(target);
		}
	}

	private class DeleteMeTimer implements Runnable
	{
		private final FakePlayer _p;

		public DeleteMeTimer(FakePlayer p)
		{
			_p = p;
		}

		@Override
		public void run()
		{
			_p.deleteMe();
		}
	}
}
