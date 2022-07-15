package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.npc.BuyListTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ExBuySellListPacket extends AbstractItemPacket {
    public static class BuyList extends ExBuySellListPacket {
        private final int _listId;
        private final List<TradeItem> _buyList;
        private final long _adena;
        private final double _taxRate;
        private final int _inventoryUsedSlots;

        public BuyList(BuyListTemplate buyList, Player activeChar, double taxRate) {
            _adena = activeChar.getAdena();
            _taxRate = taxRate;
            _inventoryUsedSlots = activeChar.getInventory().getSize();

            if (buyList != null) {
                _listId = buyList.getListId();
                _buyList = buyList.getItems();
                activeChar.setBuyListId(_listId);
            } else {
                _listId = 0;
                _buyList = Collections.emptyList();
                activeChar.setBuyListId(0);
            }
        }

        @Override
        public boolean write(l2s.commons.network.PacketWriter packetWriter) {
            OutgoingExPackets.EX_BUY_SELL_LIST.writeId(packetWriter);
            packetWriter.writeD(0x00); // BUY LIST TYPE
            packetWriter.writeQ(_adena); // current money
            packetWriter.writeD(_listId);
            packetWriter.writeD(_inventoryUsedSlots); //TODO [Bonux] Awakening
            packetWriter.writeH(_buyList.size());
            for (TradeItem item : _buyList) {
            	if (item.isCountLimited() && item.getCurrentValue() <= 0)
            		continue;

                writeItem(packetWriter, item, item.getCurrentValue());
                packetWriter.writeQ((long) (item.getOwnersPrice() * (1.0 + _taxRate)));
            }

            return true;
        }
    }

    public static class SellRefundList extends ExBuySellListPacket {
        private final List<TradeItem> _sellList;
        private final List<TradeItem> _refundList;
        private int _done;
        private final double _taxRate;
        private final int _inventoryUsedSlots;

        public SellRefundList(Player activeChar, boolean done, double taxRate) {
            _done = done ? 1 : 0;
            _taxRate = taxRate;
            _inventoryUsedSlots = activeChar.getInventory().getSize();
            if (done) {
                _refundList = Collections.emptyList();
                _sellList = Collections.emptyList();
            } else {
                ItemInstance[] items = activeChar.getRefund().getItems();
                if (Config.ALLOW_ITEMS_REFUND) {
                    _refundList = new ArrayList<TradeItem>(items.length);
                    for (ItemInstance item : items)
                        _refundList.add(new TradeItem(null, item));
                } else
                    _refundList = new ArrayList<TradeItem>(0);

                items = activeChar.getInventory().getItems();
                _sellList = new ArrayList<TradeItem>(items.length);
                for (ItemInstance item : items)
                    if (item.canBeSold(activeChar))
                        _sellList.add(new TradeItem(activeChar, item, item.getTemplate().isBlocked(activeChar, item)));
            }
        }

        @Override
        public boolean write(l2s.commons.network.PacketWriter packetWriter) {
            OutgoingExPackets.EX_BUY_SELL_LIST.writeId(packetWriter);
            packetWriter.writeD(0x01); // SELL/REFUND LIST TYPE
            packetWriter.writeD(_inventoryUsedSlots); //TODO [Bonux] Awakening
            packetWriter.writeH(_sellList.size());
            for (TradeItem item : _sellList) {
                writeItem(packetWriter, item);
                if (Config.ALT_SELL_ITEM_ONE_ADENA)
                    packetWriter.writeQ(1);
                else
                    packetWriter.writeQ(item.getReferencePrice() / 2);
            }
            packetWriter.writeH(_refundList.size());
            for (TradeItem item : _refundList) {
                writeItem(packetWriter, item);
                packetWriter.writeD(item.getObjectId());
                if (Config.ALT_SELL_ITEM_ONE_ADENA)
                    packetWriter.writeQ(item.getCount());
                else
                    packetWriter.writeQ((long) ((item.getCount() * item.getReferencePrice() / 2) * (1. - _taxRate)));
            }
            packetWriter.writeC(_done);

            return true;
        }
    }
}