package l2s.gameserver.network.l2.s2c;

import gnu.trove.set.hash.TIntHashSet;
import l2s.Phantoms.objects.TrafficScheme.PCastSkill;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class MagicSkillUse extends L2GameServerPacket {
    private static final TIntHashSet INGORE_PACKET_RANGE = new TIntHashSet(new int[]{443, 454, 455, 456, 457, 458, 459, 460, 1419, 1420, 1421, 1422, 1423, 1424});
    public static final int NONE = -1;
    private final int _targetId;
    private final int _skillId;
    private final int _skillLevel;
    private final int _hitTime;
    private final int _reuseDelay;
    private final int _chaId;
    private final int _x;
    private final int _y;
    private final int _z;
	private final int _tx;
	private final int _ty;
	private final int _tz;
	private int _reuseSkillId;
	private boolean _isServitorSkill;
	private int _actionId;
	private Location _groundLoc;
	private boolean _criticalBlow;

	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay, boolean isServitorSkill, int actionId)
	{
		if (cha.isPlayer()&& !cha.getPlayer().isPhantom()&& cha.getPlayer().isInPeaceZoneOld()&&cha.getPlayer().tScheme_record.isLogging())
			cha.getPlayer().tScheme_record.addCastSkill(new PCastSkill(skillId,skillLevel),target); 
		
		_groundLoc = null;
		_criticalBlow = false;
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		_reuseSkillId = skillId;
		_isServitorSkill = isServitorSkill;
		_actionId = actionId;
	}

	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, false, 0);
	}

	public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, hitTime, reuseDelay, false, 0);
	}

	public MagicSkillUse setReuseSkillId(int id)
	{
		_reuseSkillId = id;
		return this;
	}

	public MagicSkillUse setServitorSkillInfo(int actionId)
	{
		_isServitorSkill = true;
		_actionId = actionId;
		return this;
	}

	public MagicSkillUse setGroundLoc(Location loc)
	{
		_groundLoc = loc;
		return this;
	}

	public MagicSkillUse setCriticalBlow(boolean value)
	{
		_criticalBlow = value;
		return this;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(0);
        writeD(_chaId);
        writeD(_targetId);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_hitTime);
        writeD(_reuseSkillId);
        writeD(_reuseDelay);
        writeD(_x);
        writeD(_y);
        writeD(_z);
		if(_criticalBlow)
		{
            writeH(2);
			for(int i = 0; i < 2; ++i)
                writeH(0);
		}
		else
            writeH(0);
		if(_groundLoc != null)
		{
            writeH(1);
            writeD(_groundLoc.x);
            writeD(_groundLoc.y);
            writeD(_groundLoc.z);
        } else
            writeH(0);
        writeD(_tx);
        writeD(_ty);
        writeD(_tz);
        writeD(_isServitorSkill ? 1 : 0);
        writeD(_actionId);
    }

    @Override
    public L2GameServerPacket packet(Player player) {
        if (player != null && player.isNotShowBuffAnim())
            return _chaId == player.getObjectId() ? super.packet(player) : null;
        return super.packet(player);
    }

    @Override
    public boolean isInPacketRange(final Creature sender, final Player recipient) {
        if (INGORE_PACKET_RANGE.contains(_skillId))
            return true;
        if (_targetId == recipient.getObjectId())
            return true;

        int limit = recipient.getPacketThrottler().getPacketRange().range();
        if (limit == 0)
            return false;
        return limit * limit > sender.getXYDeltaSq(recipient.getX(), recipient.getY());
    }

    @Override
    public void onSendPacket(Player player) {
        player.getPacketThrottler().onSendPacket();
    }
}
