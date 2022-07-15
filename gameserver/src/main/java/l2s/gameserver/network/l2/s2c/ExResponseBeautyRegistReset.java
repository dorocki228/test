package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.utils.ItemFunctions;
 
public class ExResponseBeautyRegistReset implements IClientOutgoingPacket
{
	public static final int FAILURE = 0;
	public static final int SUCCESS = 1;

	public static final int CHANGE = 0;
	public static final int RESTORE = 1;

	private final int _type;
	private final int _result;
	private final int _hairStyle;
	private final int _hairColor;
	private final int _face;
	private final long _adena;
	private final long _coins;
 
	public ExResponseBeautyRegistReset(Player player, int type, int result)
	{
		_type = type;
		_result = result;
		_hairStyle = player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle();
		_hairColor = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		_face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		_adena = player.getAdena();
		_coins = ItemFunctions.getItemCount(player, Config.BEAUTY_SHOP_COIN_ITEM_ID);
	}
 
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESPONSE_BEAUTY_REGIST_RESET.writeId(packetWriter);
		packetWriter.writeQ(_adena);
		packetWriter.writeQ(_coins);
		packetWriter.writeD(_type);
		packetWriter.writeD(_result);
		packetWriter.writeD(_hairStyle);
		packetWriter.writeD(_face);
		packetWriter.writeD(_hairColor);

		return true;
	}
}