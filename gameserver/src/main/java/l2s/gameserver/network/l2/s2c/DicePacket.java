package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class DicePacket implements IClientOutgoingPacket
{
	private int _playerId;
	private int _itemId;
	private int _number;
	private int _x;
	private int _y;
	private int _z;

	/**
	 * 0xd4 DicePacket         dddddd
	 * @param _characters
	 */
	public DicePacket(int playerId, int itemId, int number, int x, int y, int z)
	{
		_playerId = playerId;
		_itemId = itemId;
		_number = number;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.DICE.writeId(packetWriter);
		packetWriter.writeD(_playerId); // object id of player
		packetWriter.writeD(_itemId); //	item id of dice (spade)  4625,4626,4627,4628
		packetWriter.writeD(_number); // number rolled
		packetWriter.writeD(_x); // x
		packetWriter.writeD(_y); // y
		packetWriter.writeD(_z); // z
		return true;
	}
}