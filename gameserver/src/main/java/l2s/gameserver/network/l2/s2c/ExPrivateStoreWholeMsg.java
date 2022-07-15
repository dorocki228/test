package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.model.Player;

public class ExPrivateStoreWholeMsg implements IClientOutgoingPacket
{
	private final int _objId;
	private final String _name;

	/**
	 * Название личного магазина продажи
	 * @param player
	 */
	public ExPrivateStoreWholeMsg(Player player, boolean showName)
	{
		_objId = player.getObjectId();
		_name = showName ? StringUtils.defaultString(player.getPackageSellStoreName()) : StringUtils.EMPTY;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PRIVATE_STORE_WHOLE_MSG.writeId(packetWriter);
		packetWriter.writeD(_objId);
		packetWriter.writeS(_name);

		return true;
	}
}