package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExShowBeautyMenuPacket extends L2GameServerPacket
{
	public static final int CHANGE_STYLE = 0;
	public static final int RESTORE_STYLE = 1;
	private final int _type;
	private final int _hairStyle;
	private final int _hairColor;
	private final int _face;

	public ExShowBeautyMenuPacket(Player player, int type)
	{
		_type = type;
		_hairStyle = player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle();
		_hairColor = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		_face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
	}

	@Override
	protected void writeImpl()
	{
        writeD(_type);
        writeD(_hairStyle);
        writeD(_hairColor);
        writeD(_face);
	}
}
