package l2s.gameserver.model.actor.recorder;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.skills.AbnormalEffect;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class CharStatsChangeRecorder<T extends Creature>
{
	public static final int BROADCAST_CHAR_INFO = 1;
	public static final int SEND_CHAR_INFO = 2;
	public static final int SEND_STATUS_INFO = 4;
	public static final int SEND_ABNORMAL_INFO = 8;
	public static final int SEND_TRANSFORMATION_INFO = 16;
	protected final T _activeChar;
	private final AtomicBoolean _blocked;
	protected int _level;
	protected int _pAccuracy;
	protected int _mAccuracy;
	protected int _attackSpeed;
	protected int _castSpeed;
	protected int _pCriticalHit;
	protected int _mCriticalHit;
	protected int _pEvasion;
	protected int _mEvasion;
	protected int _magicAttack;
	protected int _magicDefence;
	protected int _maxHp;
	protected int _maxMp;
	protected int _physicAttack;
	protected int _physicDefence;
	protected int _moveSpeed;
	protected int _visualTransformId;
	protected Set<AbnormalEffect> _abnormalEffects;
	protected TeamType _team;
	protected int _changes;

	public CharStatsChangeRecorder(T actor)
	{
        _blocked = new AtomicBoolean();
        _abnormalEffects = new CopyOnWriteArraySet<>();
        _activeChar = actor;
	}

	protected int set(int flag, int oldValue, int newValue)
	{
		if(oldValue != newValue)
            _changes |= flag;
		return newValue;
	}

	protected long set(int flag, long oldValue, long newValue)
	{
		if(oldValue != newValue)
            _changes |= flag;
		return newValue;
	}

	protected double set(int flag, double oldValue, double newValue)
	{
		if(oldValue != newValue)
            _changes |= flag;
		return newValue;
	}

	protected String set(int flag, String oldValue, String newValue)
	{
		if(!oldValue.equals(newValue))
            _changes |= flag;
		return newValue;
	}

	protected Set<AbnormalEffect> set(int flag, Set<AbnormalEffect> oldValue, Set<AbnormalEffect> newValue)
	{
		synchronized (oldValue)
		{
			if(oldValue.size() != newValue.size() || !newValue.equals(oldValue))
			{
                _changes |= flag;
				oldValue.clear();
				oldValue.addAll(newValue);
			}
		}
		return oldValue;
	}

	protected <E extends Enum<E>> E set(int flag, E oldValue, E newValue)
	{
		if(oldValue != newValue)
            _changes |= flag;
		return newValue;
	}

	protected void refreshStats()
	{
        _pAccuracy = set(2, _pAccuracy, _activeChar.getPAccuracy());
        _mAccuracy = set(2, _mAccuracy, _activeChar.getMAccuracy());
        _attackSpeed = set(1, _attackSpeed, _activeChar.getPAtkSpd());
        _castSpeed = set(1, _castSpeed, _activeChar.getMAtkSpd());
        _pCriticalHit = set(2, _pCriticalHit, _activeChar.getPCriticalHit(null));
        _mCriticalHit = set(2, _mCriticalHit, _activeChar.getMCriticalHit(null, null));
        _pEvasion = set(2, _pEvasion, _activeChar.getPEvasionRate(null));
        _mEvasion = set(2, _mEvasion, _activeChar.getMEvasionRate(null));
        _moveSpeed = set(1, _moveSpeed, _activeChar.getMoveSpeed());
        _physicAttack = set(2, _physicAttack, _activeChar.getPAtk(null));
        _physicDefence = set(2, _physicDefence, _activeChar.getPDef(null));
        _magicAttack = set(2, _magicAttack, _activeChar.getMAtk(null, null));
        _magicDefence = set(2, _magicDefence, _activeChar.getMDef(null, null));
        _maxHp = set(4, _maxHp, _activeChar.getMaxHp());
        _maxMp = set(4, _maxMp, _activeChar.getMaxMp());
        _level = set(2, _level, _activeChar.getLevel());
        _abnormalEffects = set(8, _abnormalEffects, _activeChar.getAbnormalEffects());
        _visualTransformId = set(16, _visualTransformId, _activeChar.getVisualTransformId());
        _team = set(1, _team, _activeChar.getTeam());
	}

	public final void sendChanges()
	{
		if(_blocked.get())
			return;
        refreshStats();
        onSendChanges();
        _changes = 0;
	}

	protected void onSendChanges()
	{
		if((_changes & 0x4) == 0x4)
            _activeChar.broadcastStatusUpdate();
	}

	public void block()
	{
        _blocked.compareAndSet(false, true);
	}

	public void unblock()
	{
        _blocked.compareAndSet(true, false);
	}
}
