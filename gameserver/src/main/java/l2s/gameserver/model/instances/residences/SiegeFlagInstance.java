package l2s.gameserver.model.instances.residences;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.geometry.Circle;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.zone.impl.HeadquarterEnterLeaveListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SiegeFlagInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	private SiegeClanObject _owner;
	private long _lastAnnouncedAttackedTime;

	private Zone _tempZone;

	public SiegeFlagInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		_lastAnnouncedAttackedTime = 0L;
		setHasChatWindow(false);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		Circle shape = new Circle(getX(), getY(), 300).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);

		Territory territory = new Territory();
		territory.add(shape);

		StatsSet zoneDat = new StatsSet();
		zoneDat.set("name", getName() + "-headquarter");
		zoneDat.set("type", "HEADQUARTER");
		zoneDat.set("territory", territory);

		ZoneTemplate template = new ZoneTemplate(zoneDat);

		_tempZone = new Zone(template);
		_tempZone.setReflection(ReflectionManager.MAIN);
		_tempZone.addListener(new HeadquarterEnterLeaveListener(_owner.getObjectId()));
		_tempZone.setActive(true);
	}

	@Override
	protected void onDelete()
	{
		_tempZone.setActive(false);
		super.onDelete();
	}

	@Override
	public String getName()
	{
		return _owner.getClan().getName();
	}

	@Override
	public Clan getClan()
	{
		return _owner.getClan();
	}

	@Override
	public String getTitle()
	{
		return "";
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null || isInvul())
			return false;
		Clan clan = player.getClan();
		return clan == null || _owner.getClan() != clan;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		_owner.setFlag(null);
		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if(System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000L)
		{
			_lastAnnouncedAttackedTime = System.currentTimeMillis();
			_owner.getClan().broadcastToOnlineMembers(SystemMsg.YOUR_BASE_IS_BEING_ATTACKED);
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isThrowAndKnockImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean isHealBlocked()
	{
		return true;
	}

	@Override
	public boolean isEffectImmune(Creature caster)
	{
		return true;
	}

	public void setClan(SiegeClanObject owner)
	{
		_owner = owner;
	}
}
