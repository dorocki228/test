package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.component.fraction.FractionTreasure;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.utils.NpcUtils;
import org.apache.commons.lang3.ArrayUtils;

public class RequestSellItem extends L2GameClientPacket {
    private int _listId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
        _listId = readD();
        _count = readD();
        if (_count * 16 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            readD();
            _itemQ[i] = readQ();
            if (_itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
                _count = 0;
                break;
            }
        }
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0)
            return;
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.isPK() && !activeChar.isGM()) {
            activeChar.sendActionFailed();
            return;
        }
        NpcInstance merchant = NpcUtils.canPassPacket(activeChar, this);
        if (merchant == null && !activeChar.isGM()) {
            activeChar.sendActionFailed();
            return;
        }
        double taxRate = 0.0;
        Castle castle = null;
        if (merchant != null) {
            castle = merchant.getCastle(activeChar);
            if (castle != null)
                taxRate = castle.getSellTaxRate(activeChar);
        }
        Inventory inventory = activeChar.getInventory();
        inventory.writeLock();
        activeChar.getRefund().writeLock();
        long totalPrice = 0L;
        try {
            for (int i = 0; i < _count; ++i) {
                int objectId = _items[i];
                long count = _itemQ[i];
                if (count > 0L) {
                    ItemInstance item = inventory.getItemByObjectId(objectId);
                    if (item != null && item.getCount() >= count)
                        if (item.canBeSold(activeChar)) {
                            totalPrice = SafeMath.addAndCheck(totalPrice, Config.ALT_SELL_ITEM_ONE_ADENA ? 0L : SafeMath.mulAndCheck(item.getReferencePrice(), count) / 2L);
                            if (Config.ALLOW_ITEMS_REFUND) {
                                ItemInstance refund = inventory.removeItemByObjectId(objectId, count);

                                ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.RefundSell, refund);
                                LogService.getInstance().log(LoggerType.ITEM, message);

                                activeChar.getRefund().addItem(refund);
                            } else {
                                inventory.destroyItemByObjectId(objectId, count);

                                ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.RefundSell,
                                        item, count);
                                LogService.getInstance().log(LoggerType.ITEM, message);
                            }
                        }
                }
            }
        } catch (ArithmeticException ae) {
            activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        } finally {
            inventory.writeUnlock();
            activeChar.getRefund().writeUnlock();
        }
        long tax = (long) (totalPrice * taxRate);
        totalPrice -= tax;
        activeChar.addAdena(totalPrice);
        if (castle != null && tax > 0L && castle.getOwnerId() > 0 && activeChar.getReflection().isMain())
            castle.addToTreasury(tax, true, false);

        if (merchant != null && tax > 0L && merchant.getFraction() != null && merchant.getFraction() != Fraction.NONE && activeChar.getReflection().isMain())
            FractionTreasure.getInstance().update(merchant.getFraction(), tax);
        activeChar.sendPacket(new ExBuySellListPacket.SellRefundList(activeChar, true, 0.0));
        activeChar.sendChanges();
    }
}
