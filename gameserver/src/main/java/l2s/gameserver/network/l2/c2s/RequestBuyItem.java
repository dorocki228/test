package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.component.fraction.FractionTreasure;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket.SellRefundList;
import l2s.gameserver.templates.npc.BuyListTemplate;
import l2s.gameserver.utils.NpcUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestBuyItem extends L2GameClientPacket {
    private int _listId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
        _listId = readD();
        _count = readD();
        if (_count * 12 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            _itemQ[i] = readQ();
            if (_itemQ[i] < 1L) {
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

        if (activeChar.getBuyListId() != _listId)
            return;

        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }

        if (activeChar.isInStoreMode() || activeChar.isPrivateBuffer()) {
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

        BuyListTemplate list = null;

        NpcInstance merchant = NpcUtils.canPassPacket(activeChar, this);

        if (merchant != null)
            list = merchant.getBuyList(_listId);
        //		if(activeChar.isGM() && (merchant == null || list == null || merchant.getNpcId() != list.getNpcId()))
        if ((merchant == null || list == null || merchant.getNpcId() != list.getNpcId()))
            list = BuyListHolder.getInstance().getBuyList(_listId);

        if (list == null) {
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

        List<TradeItem> buyList = new ArrayList<>(_count);
        List<TradeItem> tradeList = list.getItems();

        try {
            long totalPrice = 0L;
            long weight = 0L;
            int slots = 0;
            loop:
            for (int i = 0; i < _count; i++) {
                int itemId = _items[i];
                long count = _itemQ[i];
                long price = 0;

                for (TradeItem ti : tradeList) {
                    if (ti.getItemId() == itemId) {
                        if (ti.isCountLimited() && ti.getCurrentValue() < count) {
                            continue loop;
                        }
                        price = ti.getOwnersPrice();
                    }
                }

//				if(price == 0L && (!activeChar.isGM() || !activeChar.getPlayerAccess().UseGMShop))
//				{
//					activeChar.sendActionFailed();
//					return;
//				}

                totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));

                TradeItem ti = new TradeItem();
                ti.setItemId(itemId);
                ti.setCount(count);
                ti.setOwnersPrice(price);

                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, ti.getItem().getWeight()));
                if (!ti.getItem().isStackable() || activeChar.getInventory().getItemByItemId(itemId) == null) {
                    slots++;
                }

                buyList.add(ti);
            }

            long tax = (long) (totalPrice * taxRate);
            totalPrice = SafeMath.addAndCheck(totalPrice, tax);

            if (!activeChar.getInventory().validateWeight(weight)) {
                activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }

            if (!activeChar.getInventory().validateCapacity(slots)) {
                activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
                return;
            }

            if (!activeChar.reduceAdena(totalPrice)) {
                activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }

            for (TradeItem ti : buyList) {
                activeChar.getInventory().addItem(ti.getItemId(), ti.getCount());

                ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.NpcBuy, ti.getItemId(), ti.getCount());
                LogService.getInstance().log(LoggerType.ITEM, message);
            }

            list.updateItems(buyList);

            if (castle != null && tax > 0L && castle.getOwnerId() > 0 && activeChar.getReflection().isMain())
                castle.addToTreasury(tax, true, false);

            if (merchant != null && tax > 0L && merchant.getFraction() != null && merchant.getFraction() != Fraction.NONE && activeChar.getReflection().isMain())
                FractionTreasure.getInstance().update(merchant.getFraction(), tax);

        } catch (ArithmeticException ae) {
            activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }

        sendPacket(new SellRefundList(activeChar, true, 0.0));
        activeChar.sendChanges();
    }
}
