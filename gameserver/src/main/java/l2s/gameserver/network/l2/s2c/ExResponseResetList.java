package l2s.gameserver.network.l2.s2c;
 
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.utils.ItemFunctions;
 
public class ExResponseResetList implements IClientOutgoingPacket
{
	private int _hairStyle;
	private int _hairColor;
	private int _face;
	private long _adena; 
	private long _coins;
 
	public ExResponseResetList(Player player)
	{
		_hairStyle = player.getHairStyle();
		_hairColor = player.getHairColor();
		_face = player.getFace();
		_adena = player.getAdena();
		_coins = ItemFunctions.getItemCount(player, Config.BEAUTY_SHOP_COIN_ITEM_ID);
 
	}
 
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESPONSE_RESET_LIST.writeId(packetWriter);
		packetWriter.writeQ(_adena);
		packetWriter.writeQ(_coins);
		packetWriter.writeD(_hairStyle);
		packetWriter.writeD(_face);
		packetWriter.writeD(_hairColor);

		return true;
	}
}