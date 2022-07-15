package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExSetPledgeEmblemAck;

/**
 * @author Bonux
**/
public class RequestExSetPledgeCrestLarge implements IClientIncomingPacket
{
	private int _crestPart, _crestLeght, _length;
	private byte[] _data;

	/**
	 * format: chd(b)
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_crestPart = packet.readD();
		_crestLeght = packet.readD();
		_length = packet.readD();
		if(_length <= CrestCache.LARGE_CREST_PART_SIZE && _length == packet.getReadableBytes())
		{
			_data = packet.readB(_length);
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & Clan.CP_CL_EDIT_CREST) == Clan.CP_CL_EDIT_CREST)
		{
			if(clan.isPlacedForDisband())
			{
				activeChar.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST);
				return;
			}

			int crestId = 0;
			if(_data != null)
			{
				crestId = CrestCache.getInstance().savePledgeCrestLarge(clan.getClanId(), _crestPart, _crestLeght, _data);
				if(crestId > 0)
				{
					activeChar.sendPacket(SystemMsg.THE_CLAN_CREST_WAS_SUCCESSFULLY_REGISTERED);
					clan.setCrestLargeId(crestId);
					clan.broadcastClanStatus(false, true, false);
				}
				activeChar.sendPacket(new ExSetPledgeEmblemAck(_crestPart));
			}
			else if(clan.hasCrestLarge())
			{
				CrestCache.getInstance().removePledgeCrestLarge(clan.getClanId());
				clan.setCrestLargeId(crestId);
				clan.broadcastClanStatus(false, true, false);
			}
		}
	}
}