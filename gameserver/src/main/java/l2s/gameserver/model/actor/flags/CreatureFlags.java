package l2s.gameserver.model.actor.flags;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.flags.flag.DefaultFlag;
import l2s.gameserver.model.actor.flags.flag.DefaultMapFlag;
import l2s.gameserver.model.actor.flags.flag.UndyingFlag;
import l2s.gameserver.stats.BooleanStat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
**/
public class CreatureFlags
{
	private final Creature _owner;

	private final DefaultFlag _sleeping = new DefaultFlag();
	private final DefaultFlag _stunned = new DefaultFlag();
	private final DefaultFlag _immobilized = new DefaultFlag();
	private final DefaultFlag _confused = new DefaultFlag();
	private final DefaultFlag _knockDowned = new DefaultFlag();
	private final DefaultFlag _knockBacked = new DefaultFlag();
	private final DefaultFlag _flyUp = new DefaultFlag();
	private final DefaultFlag _healBlocked = new DefaultFlag();
	private final DefaultFlag _damageBlocked = new DefaultFlag();
	private final DefaultFlag _buffImmunity = new DefaultFlag(); // Иммунитет к бафам
	private final DefaultFlag _debuffImmunity = new DefaultFlag(); // Иммунитет к дебафам
	private final DefaultFlag _effectImmunity = new DefaultFlag(); // Иммунитет ко всем эффектам
	private final DefaultFlag _deathImmunity = new DefaultFlag();
	private final DefaultFlag _distortedSpace = new DefaultFlag();
	private final DefaultFlag _invisible = new DefaultFlag();
	private final DefaultFlag _invulnerable = new DefaultFlag();
	private final DefaultFlag _weaponEquipBlocked = new DefaultFlag();
	private final UndyingFlag _undying = new UndyingFlag();
	private final DefaultMapFlag untargetableList = new DefaultMapFlag();

	private AtomicBoolean _blockActions = new AtomicBoolean();
	private AtomicBoolean controlBlocked = new AtomicBoolean();
	private AtomicBoolean _allSkillsDisabled = new AtomicBoolean();

	public CreatureFlags(Creature owner)
	{
		_owner = owner;
	}

	public DefaultFlag getSleeping()
	{
		return _sleeping;
	}

	public DefaultFlag getStunned()
	{
		return _stunned;
	}

	public DefaultFlag getImmobilized()
	{
		return _immobilized;
	}

	public DefaultFlag getConfused()
	{
		return _confused;
	}

	public DefaultFlag getKnockDowned()
	{
		return _knockDowned;
	}

	public DefaultFlag getKnockBacked()
	{
		return _knockBacked;
	}

	public DefaultFlag getFlyUp()
	{
		return _flyUp;
	}

	public DefaultFlag getHealBlocked()
	{
		return _healBlocked;
	}

	public DefaultFlag getDamageBlocked()
	{
		return _damageBlocked;
	}

	public DefaultFlag getBuffImmunity()
	{
		return _buffImmunity;
	}

	public DefaultFlag getDebuffImmunity()
	{
		return _debuffImmunity;
	}

	public DefaultFlag getEffectImmunity()
	{
		return _effectImmunity;
	}

	public DefaultFlag getDeathImmunity()
	{
		return _deathImmunity;
	}

	public DefaultFlag getDistortedSpace()
	{
		return _distortedSpace;
	}

	public DefaultFlag getInvisible()
	{
		return _invisible;
	}

	public DefaultFlag getInvulnerable()
	{
		return _invulnerable;
	}

	public DefaultFlag getWeaponEquipBlocked()
	{
		return _weaponEquipBlocked;
	}

	public UndyingFlag getUndying()
	{
		return _undying;
	}

	public DefaultMapFlag getUntargetableList() {
		return untargetableList;
	}

	public final boolean hasBlockActions()
	{
		return _blockActions.get() || _owner.getStat().has(BooleanStat.BLOCK_ACTIONS);
	}

	public final void setBlockActions(boolean blockActions)
	{
		_blockActions.set(blockActions);
	}

	public final boolean isControlBlocked()
	{
		return controlBlocked.get() || _owner.getStat().has(BooleanStat.BLOCK_CONTROL);
	}

	public final void setControlBlocked(boolean controlBlocked)
	{
		this.controlBlocked.set(controlBlocked);
	}

	/**
	 * @return True if the L2Character can't use its skills (ex : stun, sleep...).
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled.get() || hasBlockActions();
	}

	/**
	 * Disables all skills.
	 */
	public void disableAllSkills()
	{
		_allSkillsDisabled.set(true);
	}

	/**
	 * Enables all skills, except those under reuse time or previously disabled.
	 */
	public void enableAllSkills()
	{
		_allSkillsDisabled.set(false);
	}
}