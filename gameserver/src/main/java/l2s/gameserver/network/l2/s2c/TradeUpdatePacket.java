package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.templates.item.support.Ensoul;

public class TradeUpdatePacket extends L2GameServerPacket
{
	private int flags = 0;
	private final ItemInfo _item;
	private final long _amount;

	public TradeUpdatePacket(ItemInfo item, long amount)
	{
		_item = item;
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
		writeH(_amount > 0 && _item.getItem().isStackable() ? 3 : 2);
		writeC(flags);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		writeC(_item.getEquipSlot());
		writeQ(_amount);
		writeC(_item.getType2());
		writeC(0);
		writeH(_item.isEquipped());
		writeQ(_item.getBodyPart());
		writeC(_item.getEnchantLevel());
		writeC(0);
		writeD(_item.getShadowLifeTime());
		writeD(_item.getTemporalLifeTime());
		writeC(!_item.isBlocked());

		if((flags & 4) == 4)
		{
			writeD(_item.getEnchantOptions()[0]);
			writeD(_item.getEnchantOptions()[1]);
			writeD(_item.getEnchantOptions()[2]);
		}

		if((flags & 16) == 16)
		{
			writeC(_item.getNormalEnsouls().length);
			for(Ensoul ensoul : _item.getNormalEnsouls())
				writeD(ensoul.getId());

			writeC(_item.getSpecialEnsouls().length);
			for(Ensoul ensoul : _item.getSpecialEnsouls())
				writeD(ensoul.getId());

		}
	}
}
