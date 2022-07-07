package l2s.gameserver.network.l2.s2c;

public class EnchantResultPacket extends L2GameServerPacket
{
	private final int _resultId;
	private final int _crystalId;
	private final long _count;
	private final int _enchantLevel;
	public static final EnchantResultPacket CANCEL;
	public static final EnchantResultPacket BLESSED_FAILED;
	public static final EnchantResultPacket FAILED_NO_CRYSTALS;
	public static final EnchantResultPacket ANCIENT_FAILED;

	public EnchantResultPacket(int resultId, int crystalId, long count, int enchantLevel)
	{
		_resultId = resultId;
		_crystalId = crystalId;
		_count = count;
		_enchantLevel = enchantLevel;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_resultId);
        writeD(_crystalId);
		writeQ(_count);
        writeD(_enchantLevel);
        writeH(0);
        writeH(0);
        writeH(0);
	}

	static
	{
		CANCEL = new EnchantResultPacket(2, 0, 0L, 0);
		BLESSED_FAILED = new EnchantResultPacket(3, 0, 0L, 0);
		FAILED_NO_CRYSTALS = new EnchantResultPacket(4, 0, 0L, 0);
		ANCIENT_FAILED = new EnchantResultPacket(5, 0, 0L, 0);
	}
}
