package l2s.gameserver.network.l2.s2c;

import l2s.commons.bitmask.BitMask;
import l2s.commons.bitmask.BitmaskGlobalKt;
import l2s.commons.net.nio.impl.SendablePacket;
import l2s.gameserver.GameServer;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.items.CommissionItem;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemListType;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.Ensoul;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class L2GameServerPacket extends SendablePacket<GameClient> implements IBroadcastPacket
{
	private static final Logger _log = LogManager.getLogger(L2GameServerPacket.class);

	@Override
	public final boolean write()
	{
		try
		{
			if(writeOpcodes())
			{
				writeImpl();
				return true;
			}
		}
		catch(Exception e)
		{
			_log.error("Client: " + getClient() + " - Failed writing: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}
		return false;
	}

	protected ServerPacketOpcodes getOpcodes()
	{
		try
		{
			return ServerPacketOpcodes.valueOf(getClass().getSimpleName());
		}
		catch(Exception e)
		{
			_log.error("Cannot find serverpacket opcode: " + getClass().getSimpleName() + "!");
			return null;
		}
	}

	protected boolean writeOpcodes()
	{
		ServerPacketOpcodes opcodes = getOpcodes();
		if(opcodes == null)
			return false;
		int opcode = opcodes.getId();
		writeC(opcode);
		if(opcode == 254)
			writeH(opcodes.getExId());
		return true;
	}

	protected abstract void writeImpl();

	protected void writeD(boolean b)
	{
		writeD(b ? 1 : 0);
	}

	protected void writeH(boolean b)
	{
		writeH(b ? 1 : 0);
	}

	protected void writeC(boolean b)
	{
		writeC(b ? 1 : 0);
	}

	protected void writeDD(int[] values, boolean sendCount)
	{
		if(sendCount)
			getByteBuffer().putInt(values.length);
		for(int value : values)
			getByteBuffer().putInt(value);
	}

	protected void writeDD(int[] values)
	{
		writeDD(values, false);
	}

	protected void writeItemInfo(ItemInstance item)
	{
		writeItemInfo(null, item, item.getCount());
	}

	protected void writeItemInfo(Player player, ItemInstance item)
	{
		writeItemInfo(player, item, item.getCount());
	}

	protected void writeItemInfo(ItemInstance item, long count)
	{
		writeItemInfo(null, item, count);
	}

	protected void writeItemInfo(Player player, ItemInstance item, long count)
	{
		BitMask flags = calculateMask(item);

		writeC(flags.getValue());
		writeD(item.getObjectId());
		writeD(item.getItemId());
		writeC(item.isEquipped() ? -1 : item.getEquipSlot());
		writeQ(count);
		writeC(item.getTemplate().getType2());
		writeC(item.getCustomType1());
		writeH(item.isEquipped() ? 1 : 0);
		writeQ(item.getBodyPart());
		writeC(item.getEnchantLevel());
		writeC(item.getCustomType2());
		writeD(item.getShadowLifeTime());
		writeD(item.getTemporalLifeTime());

		if(player != null)
			writeC(!item.getTemplate().isBlocked(player, item));
		else
			writeC(1);

		if(BitmaskGlobalKt.hasFlag(flags, ItemListType.AUGMENT_BONUS))
		{
			writeItemAugment(item);
		}

		if(BitmaskGlobalKt.hasFlag(flags, ItemListType.ENCHANT_EFFECT))
		{
			writeD(item.getEnchantOptions()[0]);
			writeD(item.getEnchantOptions()[1]);
			writeD(item.getEnchantOptions()[2]);
		}

		if (BitmaskGlobalKt.hasFlag(flags, ItemListType.VISUAL_ID))
		{
			writeD(item.getVisualId()); // Item remodel visual ID
		}

		if(BitmaskGlobalKt.hasFlag(flags, ItemListType.SOUL_CRYSTAL))
		{
			Ensoul[] normalEnsouls = item.getNormalEnsouls();
			writeC(normalEnsouls.length);
			for(Ensoul ensoul : normalEnsouls)
				writeD(ensoul.getId());

			Ensoul[] specialEnsouls = item.getSpecialEnsouls();
			writeC(specialEnsouls.length);
			for(Ensoul ensoul : specialEnsouls)
				writeD(ensoul.getId());
		}
	}

	protected void writeItemAugment(ItemInstance item) {
		writeD(item.getAugmentations()[0]);
		writeD(item.getAugmentations()[1]);
	}

	protected void writeItemInfo(ItemInfo item)
	{
		writeItemInfo(item, item.getCount());
	}

	protected void writeItemInfo(ItemInfo item, long count)
	{
		BitMask flags = calculateMask(item);

		writeC(flags.getValue());
		writeD(item.getObjectId());
		writeD(item.getItemId());
		writeC(item.getItem().isQuest() || item.isEquipped() ? -1 : item.getEquipSlot());
		writeQ(count);
		writeC(item.getItem().getType2());
		writeC(item.getCustomType1());
		writeH(item.isEquipped() ? 1 : 0);
		writeQ(item.getItem().getBodyPart());
		writeC(item.getEnchantLevel());
		writeC(item.getCustomType2());
		writeD(item.getShadowLifeTime());
		writeD(item.getTemporalLifeTime());
		writeC(!item.isBlocked());

		if(BitmaskGlobalKt.hasFlag(flags, ItemListType.AUGMENT_BONUS))
		{
			writeItemAugment(item);
		}

		if(BitmaskGlobalKt.hasFlag(flags, ItemListType.ENCHANT_EFFECT))
		{
			writeD(item.getEnchantOptions()[0]);
			writeD(item.getEnchantOptions()[1]);
			writeD(item.getEnchantOptions()[2]);
		}

		if (BitmaskGlobalKt.hasFlag(flags, ItemListType.VISUAL_ID))
		{
			writeD(item.getVisualId()); // Item remodel visual ID
		}

		if(BitmaskGlobalKt.hasFlag(flags, ItemListType.SOUL_CRYSTAL))
		{
			Ensoul[] normalEnsouls = item.getNormalEnsouls();
			writeC(normalEnsouls.length);
			for(Ensoul ensoul : normalEnsouls)
				writeD(ensoul.getId());

			Ensoul[] specialEnsouls = item.getSpecialEnsouls();
			writeC(specialEnsouls.length);
			for(Ensoul ensoul : specialEnsouls)
				writeD(ensoul.getId());
		}
	}

	protected void writeItemAugment(ItemInfo item) {
		writeD(item.getAugmentations()[0]);
		writeD(item.getAugmentations()[1]);
	}

	protected void writeCommissionItem(CommissionItem item)
	{
		writeD(item.getItemId());
		writeC(item.getEquipSlot());
		writeQ(item.getCount());
		writeH(item.getItem().getType2());
		writeQ(item.getItem().getBodyPart());
		writeH(item.getEnchantLevel());
		writeH(item.getCustomType2());
		writeH(item.getAttackElement());
		writeH(item.getAttackElementValue());
		writeH(item.getDefenceFire());
		writeH(item.getDefenceWater());
		writeH(item.getDefenceWind());
		writeH(item.getDefenceEarth());
		writeH(item.getDefenceHoly());
		writeH(item.getDefenceUnholy());
		writeD(item.getEnchantOptions()[0]);
		writeD(item.getEnchantOptions()[1]);
		writeD(item.getEnchantOptions()[2]);
	}

	protected void writeItemElements(MultiSellIngredient item)
	{
		if(item.getItemId() <= 0)
		{
			writeItemElements();
			return;
		}
		ItemTemplate i = ItemHolder.getInstance().getTemplate(item.getItemId());
		if(item.getItemAttributes().getValue() > 0)
		{
			if(i.isWeapon())
			{
				Element e = item.getItemAttributes().getElement();
				writeH(e.getId());
				writeH(item.getItemAttributes().getValue(e) + i.getBaseAttributeValue(e));
				writeH(0);
				writeH(0);
				writeH(0);
				writeH(0);
				writeH(0);
				writeH(0);
			}
			else if(i.isArmor())
			{
				writeH(-1);
				writeH(0);
				for(Element e2 : Element.VALUES)
					writeH(item.getItemAttributes().getValue(e2) + i.getBaseAttributeValue(e2));
			}
			else
				writeItemElements();
		}
		else
			writeItemElements();
	}

	protected void writeItemElements()
	{
		writeH(-1);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
	}

	private BitMask calculateMask(ItemInstance item) {
		BitMask flags = ItemListType.NONE.toBitMask();

		if (item.isAugmented()) {
			flags = BitmaskGlobalKt.plus(flags, ItemListType.AUGMENT_BONUS);
		}

		for (int enchantOption : item.getEnchantOptions()) {
			if (enchantOption > 0) {
				flags = BitmaskGlobalKt.plus(flags, ItemListType.ENCHANT_EFFECT);
				break;
			}
		}

		if (item.getVisualId() > 0) {
			flags = BitmaskGlobalKt.plus(flags, ItemListType.VISUAL_ID);
		}

		if (item.getNormalEnsouls().length > 0 || item.getSpecialEnsouls().length > 0) {
			flags = BitmaskGlobalKt.plus(flags, ItemListType.SOUL_CRYSTAL);
		}

		return flags;
	}

	private BitMask calculateMask(ItemInfo item) {
		BitMask flags = ItemListType.NONE.toBitMask();

		if (item.getAugmentationMineralId() != 0) {
			flags = BitmaskGlobalKt.plus(flags, ItemListType.AUGMENT_BONUS);
		}

		for (int enchantOption : item.getEnchantOptions()) {
			if (enchantOption > 0) {
				flags = BitmaskGlobalKt.plus(flags, ItemListType.ENCHANT_EFFECT);
				break;
			}
		}

		if (item.getVisualId() > 0)
		{
			flags = BitmaskGlobalKt.plus(flags, ItemListType.VISUAL_ID);
		}

		if (item.getNormalEnsouls().length > 0 || item.getSpecialEnsouls().length > 0) {
			flags = BitmaskGlobalKt.plus(flags, ItemListType.SOUL_CRYSTAL);
        }

        return flags;
    }

    public String getType() {
        return "[S] " + getClass().getSimpleName();
    }

    protected static boolean containsMask(int masks, IUpdateTypeComponent type) {
        return (masks & type.getMask()) == type.getMask();
    }

    @Override
    public L2GameServerPacket packet(Player player) {
        return this;
    }

    public void onSendPacket(Player player) {
    }
}
