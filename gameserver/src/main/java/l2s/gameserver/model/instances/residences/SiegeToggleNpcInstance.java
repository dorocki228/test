package l2s.gameserver.model.instances.residences;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.Set;

public abstract class SiegeToggleNpcInstance extends NpcInstance
{
	private NpcInstance _fakeInstance;
	private int _maxHp;

	public SiegeToggleNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		setHasChatWindow(false);
	}

	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}

	public void setZoneList(Set<String> set)
	{}

	public void register(Spawner spawn)
	{}

	public void initFake(int fakeNpcId)
	{
		(_fakeInstance = NpcHolder.getInstance().getTemplate(fakeNpcId).getNewInstance()).setCurrentHpMp(1.0, _fakeInstance.getMaxMp());
		_fakeInstance.setHasChatWindow(false);
	}

	public abstract void onDeathImpl(Creature p0);

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
        setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);
		if(getCurrentHp() < 0.5)
		{
			doDie(attacker);
			onDeathImpl(attacker);
			decayMe();
			_fakeInstance.spawnMe(getLoc());
		}
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
			return false;
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent == null || !siegeEvent.isInProgress())
			return false;

		Residence residence = siegeEvent.getResidence();
		Fraction owner = residence.getFraction();

		return owner.canAttack(attacker.getFraction());
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
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

	public void decayFake()
	{
		_fakeInstance.decayMe();
	}

	@Override
	public int getMaxHp()
	{
		return _maxHp;
	}

	@Override
	protected void onDecay()
	{
		decayMe();
		_spawnAnimation = 2;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	@Override
	public Fraction getFraction()
	{
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent == null)
			return Fraction.NONE;

		Residence residence = siegeEvent.getResidence();
		return residence.getFraction();
	}
}
