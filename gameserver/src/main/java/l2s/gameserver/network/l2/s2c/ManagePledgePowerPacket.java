package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.RankPrivs;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ManagePledgePowerPacket implements IClientOutgoingPacket
{
	private int _action, _clanId, privs;

	public ManagePledgePowerPacket(Player player, int action, int rank)
	{
		_clanId = player.getClanId();
		_action = action;
		RankPrivs temp = player.getClan().getRankPrivs(rank);
		privs = temp == null ? 0 : temp.getPrivs();
		player.sendPacket(new ExUpdatePledgePower(privs));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MANAGE_PLEDGE_POWER.writeId(packetWriter);
		packetWriter.writeD(_clanId);
		packetWriter.writeD(_action);
		packetWriter.writeD(privs);

		return true;
	}
}