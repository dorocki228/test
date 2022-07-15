package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShowBeautyMenu implements IClientOutgoingPacket
{
	public static final int CHANGE_STYLE = 0x00;
	public static final int RESTORE_STYLE = 0x01;

	private final int _type;
	private final int _hairStyle;
	private final int _hairColor;
	private final int _face;

	public ExShowBeautyMenu(Player player, int type)
	{
		_type = type;
		_hairStyle = player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle();
		_hairColor = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		_face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_BEAUTY_MENU.writeId(packetWriter);
		packetWriter.writeD(_type);  // 0x00 - изменение стиля, 0x01 отмена стиля
		packetWriter.writeD(_hairStyle);
		packetWriter.writeD(_hairColor);
		packetWriter.writeD(_face);

		return true;
	}
}
