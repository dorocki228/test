package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;

public class EtcStatusUpdatePacket extends L2GameServerPacket
{
	private static final int NO_CHAT_FLAG = 1;
	private static final int DANGER_AREA_FLAG = 2;
	private static final int CHARM_OF_COURAGE_FLAG = 4;
	private final int _increasedForce;
	private final int _weightPenalty;
	private final int _weaponPenalty;
	private final int _armorPenalty;
	private final int _consumedSouls;
	private int _flags;

	public EtcStatusUpdatePacket(Player player)
	{
		_increasedForce = player.getIncreasedForce();
		_weightPenalty = player.getWeightPenalty();
		_weaponPenalty = player.getWeaponsExpertisePenalty();
		_armorPenalty = player.getArmorsExpertisePenalty();
		_consumedSouls = player.getConsumedSouls();
		if(player.getMessageRefusal() || player.isBlockAll()
				|| PunishmentService.INSTANCE.isPunished(PunishmentType.CHAT, player))
			_flags |= 0x1;
		if(player.isInDangerArea())
			_flags |= 0x2;
		if(player.isCharmOfCourage())
			_flags |= 0x4;
	}

	@Override
	protected final void writeImpl()
	{
        writeC(_increasedForce);
        writeD(_weightPenalty);
        writeC(_weaponPenalty);
        writeC(_armorPenalty);
        writeC(0);
        writeC(_consumedSouls);
        writeC(_flags);
	}
}
