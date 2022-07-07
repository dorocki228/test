package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;

public class ExMagicAttackInfo extends L2GameServerPacket
{
	public enum MagicAttackType
	{
		CRITICAL,
		CRITICAL_HEAL,
		OVERHIT,
		EVADED,
		BLOCKED,
		RESISTED,
		IMMUNE
	}

	private final int _attackerId;
	private final int _targetId;
	private final int _info;

	private ExMagicAttackInfo(int attackerId, int targetId, int info)
	{
		_attackerId = attackerId;
		_targetId = targetId;
		_info = info;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_attackerId);
        writeD(_targetId);
        writeD(_info);
	}

	public static void packet(Creature attacker, Creature target, MagicAttackType type)
	{
		ExMagicAttackInfo p = new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), type.ordinal() + 1);

		Player attackerPlayer = attacker.getPlayer();
		Player targetPlayer = target.getPlayer();

		if(attackerPlayer != null)
			attackerPlayer.sendPacket(p);
		if(targetPlayer != null && attacker != target)
			targetPlayer.sendPacket(p);
	}
}
