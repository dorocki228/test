package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.model.Player;

public class PrivateStoreMsg implements IClientOutgoingPacket
{
	private final int _objId;
	private final String _name;

	/**
	 * Название личного магазина продажи
	 * @param player
	 */
	public PrivateStoreMsg(Player player, boolean showName)
	{
		_objId = player.getObjectId();
		_name = showName ? StringUtils.defaultString(player.getSellStoreName()) : StringUtils.EMPTY;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PRIVATE_STORE_MSG.writeId(packetWriter);
		packetWriter.writeD(_objId);
		packetWriter.writeS(_name);

		return true;
	}
}