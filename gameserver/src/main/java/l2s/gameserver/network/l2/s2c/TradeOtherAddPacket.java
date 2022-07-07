package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.templates.item.support.Ensoul;

public class TradeOtherAddPacket extends L2GameServerPacket
{
	private final ItemInfo _temp;
	private final long _amount;
	private int flags = 0;

	public TradeOtherAddPacket(ItemInfo item, long amount)
	{
		_temp = item;
		_amount = amount;
		for(int enchantOption : item.getEnchantOptions())
		{
			if(enchantOption <= 0)
				continue;
			flags |= 4;
			break;
		}

		Ensoul[] normalEnsouls = item.getNormalEnsouls();
		Ensoul[] specialEnsouls = item.getSpecialEnsouls();
		if(normalEnsouls.length > 0 || specialEnsouls.length > 0)
			flags |= 16;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(1);
		writeC(flags);
		writeD(_temp.getObjectId());
		writeD(_temp.getItemId());
		writeC(_temp.getEquipSlot());
		writeQ(_amount);
		writeC(_temp.getType2());
		writeC(0);
		writeH(_temp.isEquipped());
		writeQ(_temp.getBodyPart());
		writeC(_temp.getEnchantLevel());
		writeC(0);
		writeD(_temp.getShadowLifeTime());
		writeD(_temp.getTemporalLifeTime());
		writeC(!_temp.isBlocked());

		if((flags & 4) == 4)
		{
			writeD(_temp.getEnchantOptions()[0]);
			writeD(_temp.getEnchantOptions()[1]);
			writeD(_temp.getEnchantOptions()[2]);
		}

		if((flags & 16) == 16)
		{
			writeC(_temp.getNormalEnsouls().length);
			for(Ensoul ensoul : _temp.getNormalEnsouls())
				writeD(ensoul.getId());

			writeC(_temp.getSpecialEnsouls().length);
			for(Ensoul ensoul : _temp.getSpecialEnsouls())
				writeD(ensoul.getId());
		}
	}
}
