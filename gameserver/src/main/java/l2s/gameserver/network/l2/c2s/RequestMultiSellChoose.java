package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;
import l2s.commons.math.SafeMath;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.component.fraction.FractionTreasure;
import l2s.gameserver.config.GveStagesConfig.GveStage;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.MultiSellListContainer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemAttributes;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.GveStageService;
import l2s.gameserver.service.MultisellLoggingService;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestMultiSellChoose extends L2GameClientPacket {
    private static final int BUY_DELAY = 200;
    private static final Logger _log = LoggerFactory.getLogger(RequestMultiSellChoose.class);
    private int _listId;
    private int _entryId;
    private long _amount;

    @Override
    protected void readImpl() {
        _listId = readD();
        _entryId = readD();
        _amount = readQ();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _amount < 1)
            return;

        MultiSellListContainer list1 = activeChar.getMultisell();
        if (list1 == null) {
            activeChar.sendActionFailed();
            activeChar.setMultisell(null);
            return;
        }

        // Проверяем, не подменили ли id
        if (list1.getListId() != _listId) {
            //TODO audit
            activeChar.sendActionFailed();
            activeChar.setMultisell(null);
            return;
        }

        if (list1.getNpcObjectId() > 0) {
            NpcInstance npc = activeChar.getLastNpc();
            GameObject target = activeChar.getTarget();
            if (npc == null && target != null && target.isNpc())
                npc = (NpcInstance) target;

            // Не тот NPC или слишком далеко
            if (npc == null || npc.getObjectId() != list1.getNpcObjectId() || !activeChar.isInRangeZ(npc, Creature.INTERACTION_DISTANCE)) {
                activeChar.sendActionFailed();
                activeChar.setMultisell(null);
                return;
            }
        } else
            // Запрещенный мультиселл из BBS (без NPC)
            if (!list1.isBBSAllowed()) {
                activeChar.sendActionFailed();
                activeChar.setMultisell(null);
                return;
            }

        if (list1.isDisabled()) {
            activeChar.sendMessage(new CustomMessage("multisell.Disabled"));
            activeChar.sendActionFailed();
            activeChar.setMultisell(null);
            return;
        }

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
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_);
            return;
        }

        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM() && !list1.isBBSAllowed()) {
            activeChar.sendActionFailed();
            return;
        }

        if (System.currentTimeMillis() <= activeChar.getLastMultisellBuyTime() + BUY_DELAY) {
            activeChar.sendActionFailed();
            return;
        }
        if(list1.isInvalidate()) {
            activeChar.sendMessage(new CustomMessage("multisell.discount.invalidate"));
            activeChar.sendActionFailed();
            activeChar.setMultisell(null);
            return;
        }

        MultiSellEntry entry = null;
        for (MultiSellEntry $entry : list1.getEntries())
            if ($entry.getEntryId() == _entryId) {
                entry = $entry;
                break;
            }

        if (entry == null)
            return;

        boolean keepenchant = list1.isKeepEnchant();
        boolean notax = list1.isNoTax();

        PcInventory inventory = activeChar.getInventory();

        NpcInstance merchant = activeChar.getLastNpc();
        Castle castle = merchant != null ? merchant.getCastle(activeChar) : null;

        inventory.writeLock();
        try {
            long tax = SafeMath.mulAndCheck(entry.getTax(), _amount);

            long slots = 0;
            long weight = 0;
            for (MultiSellIngredient i : entry.getProduction()) {
                if (i.getItemId() <= 0)
                    continue;
                ItemTemplate item = ItemHolder.getInstance().getTemplate(i.getItemId());
                if (item == null) {
                    _log.warn("Cannot find production item template ID[" + i.getItemId() + "] in multisell list ID[" + _listId + "]!");
                    return;
                }

                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(SafeMath.mulAndCheck(i.getItemCount(), _amount), item.getWeight()));
                if (item.isStackable()) {
                    if (inventory.getItemByItemId(i.getItemId()) == null)
                        slots++;
                } else
                    slots = SafeMath.addAndCheck(slots, _amount);
            }

            if (!inventory.validateWeight(weight)) {
                activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                activeChar.sendActionFailed();
                return;
            }

            if (!inventory.validateCapacity(slots)) {
                activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
                activeChar.sendActionFailed();
                return;
            }

            if (!entry.isFree() && entry.getIngredients().isEmpty()) {
                activeChar.sendActionFailed();
                activeChar.setMultisell(null);
                return;
            }

            for (MultiSellIngredient product : entry.getProduction()) {
                if (product.getActiveStage() > GveStageService.getInstance().getCurrentStageId()) {
                    GveStage stage = GveStageService.getInstance().getStageInfoById(product.getActiveStage());
                    if (stage != null) {
                        String message = new CustomMessage("services.gve.stages.product.unavailable")
                            .addNumber(stage.getId())
                            .addString(stage.getStartDate())
                            .toString(activeChar);
                        activeChar.sendMessage(message);
                    }
                    return;
                }
            }

            // Перебор всех ингридиентов, проверка наличия и создание списка забираемого
            long totalPrice = 0;
            List<ItemData> items = new ArrayList<>();
            for (MultiSellIngredient ingridient : entry.getIngredients()) {
                int ingridientItemId = ingridient.getItemId();
                long ingridientItemCount = ingridient.getItemCount();
                int ingridientEnchant = ingridient.getItemEnchant();
                long totalAmount = !ingridient.getMantainIngredient() ? SafeMath.mulAndCheck(ingridientItemCount, _amount) : ingridientItemCount;

                if (ingridientItemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE) {
                    if (activeChar.getClan() == null) {
                        activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
                        return;
                    }

                    if (activeChar.getClan().getReputationScore() < totalAmount) {
                        activeChar.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                        return;
                    }

                    if (activeChar.getClan().getLeaderId() != activeChar.getObjectId()) {
                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_IS_NOT_A_CLAN_LEADER).addName(activeChar));
                        return;
                    }
                    if (!ingridient.getMantainIngredient())
                        items.add(new ItemData(ingridientItemId, totalAmount, null));
                } else if (ingridientItemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS) {
                    if (activeChar.getPcBangPoints() < totalAmount) {
                        activeChar.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
                        return;
                    }
                    if (!ingridient.getMantainIngredient())
                        items.add(new ItemData(ingridientItemId, totalAmount, null));
                } else if (ingridientItemId == ItemTemplate.ITEM_ID_FAME) {
                    if (activeChar.getFame() < totalAmount) {
                        activeChar.sendPacket(SystemMsg.YOU_DONT_HAVE_ENOUGH_REPUTATION_TO_DO_THAT);
                        return;
                    }
                    if (!ingridient.getMantainIngredient())
                        items.add(new ItemData(ingridientItemId, totalAmount, null));
                } else {
                    ItemTemplate template = ItemHolder.getInstance().getTemplate(ingridientItemId);
                    if (template == null) {
                        _log.warn("Cannot find ingridient item template ID[" + ingridientItemId + "] in multisell list ID[" + _listId + "]!");
                        return;
                    }

                    if (!template.isStackable())
                        for (int i = 0; i < ingridientItemCount * _amount; i++) {
                            List<ItemInstance> list = inventory.getItemsByItemId(ingridientItemId);
                            // Если энчант имеет значение - то ищем вещи с точно таким энчантом
                            if (keepenchant) {
                                ItemInstance itemToTake = null;
                                for (ItemInstance item : list) {
                                    ItemData itmd = new ItemData(item.getItemId(), item.getCount(), item);
                                    // DS: позволяем обмен одетых вещей (не оффлайк) для редактора аугментации
                                    // при обычном обмене список будет сгенерирован уже без одетых вещей
                                    if(/*(item.getEnchantLevel() == ingridientEnchant || !item.getTemplate().isEquipment()) && */!items.contains(itmd) /*&& !item.isEquipped()*/ && item.canBeExchanged(activeChar))
                                    {
                                        itemToTake = item;
                                        break;
                                    }
                                }

                                if (itemToTake == null) {
                                    if (ingridientItemCount > 1)
                                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_NEED_S2_S1).addItemName(ingridientItemId).addNumber(ingridientItemCount));
                                    else
                                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_NEED_AN_S1).addItemName(ingridientItemId));
                                    return;
                                }

                                if (!ingridient.getMantainIngredient())
                                    items.add(new ItemData(itemToTake.getItemId(), 1, itemToTake));
                            }
                            // Если энчант не обрабатывается берется вещь с наименьшим энчантом
                            else {
                                ItemInstance itemToTake = null;
                                for (ItemInstance item : list) {
                                    ItemData itemData = new ItemData(item.getItemId(), item.getCount(), item);
                                    if(!items.contains(itemData) && (itemToTake == null || item.getEnchantLevel() < itemToTake.getEnchantLevel()) && !item.isEquipped() && !item.isShadowItem() && !item.isTemporalItem() && !item.isAugmented() && ItemFunctions.checkIfCanDiscard(activeChar, item))
                                    {
                                        itemToTake = item;
                                        if(itemToTake.getEnchantLevel() == 0)
                                            break;
                                    }
                                }

                                if (itemToTake == null) {
                                    if (ingridientItemCount > 1)
                                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_NEED_S2_S1).addItemName(ingridientItemId).addNumber(ingridientItemCount));
                                    else
                                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_NEED_AN_S1).addItemName(ingridientItemId));
                                    return;
                                }

                                if (!ingridient.getMantainIngredient())
                                    items.add(new ItemData(itemToTake.getItemId(), 1, itemToTake));
                            }
                        }
                    else {
                        if (ingridientItemId == ItemTemplate.ITEM_ID_ADENA)
                            totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(ingridientItemCount, _amount));
                        ItemInstance item = inventory.getItemByItemId(ingridientItemId);

                        if (item == null || item.getCount() < totalAmount) {
                            if (ingridientItemCount > 1)
                                activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_NEED_S2_S1).addItemName(ingridientItemId).addNumber(ingridientItemCount));
                            else
                                activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_NEED_AN_S1).addItemName(ingridientItemId));
                            return;
                        }

                        if (!ingridient.getMantainIngredient())
                            items.add(new ItemData(item.getItemId(), totalAmount, item));
                    }
                }

                if (activeChar.getAdena() < totalPrice) {
                    activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
            }

            boolean equipped = false;
            boolean enchantInitialize = true;
            int enchantLevel = -1;
            int mineralId = 0;
            ItemAttributes attributes = null;
            int[] augmentations = ItemInstance.EMPTY_AUGMENTATIONS;
            int lifeTime = 0;
            int customFlags = 0;
            for (ItemData id : items) {
                long count = id.getCount();
                if (count > 0)
                    if (id.getId() == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE) {
                        activeChar.getClan().incReputation((int) -count, false, "MultiSell");
                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLANS_REPUTATION).addNumber(count));
                    } else if (id.getId() == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
                        activeChar.reducePcBangPoints((int) count);
                    else if (id.getId() == ItemTemplate.ITEM_ID_FAME) {
                        activeChar.setFame(activeChar.getFame() - (int) count, "MultiSell", true);
                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S2_S1_HAS_DISAPPEARED).addNumber(count).addString("Fame"));
                    } else {
                        ItemInstance it = id.getItem();
                        if (it.isEquipped()) // обмен одетых вещей - снимаем перед удалением
                        {
                            equipped = true;
                            activeChar.getInventory().unEquipItem(it);
                        }

                        if (inventory.destroyItem(it, count)) {
                            if (keepenchant && it.canBeEnchanted()) {
                                enchantLevel = getMinEnchantLevel(enchantLevel, it.getEnchantLevel(), enchantInitialize);
                                attributes = it.getAttributes();
                                mineralId = it.getAugmentationMineralId();
                                augmentations = it.getAugmentations();
                            } else if (!Config.RETAIL_MULTISELL_ENCHANT_TRANSFER && it.canBeEnchanted()) {
                                if (it.getAttributes() != null)
                                    attributes = it.getAttributes();

                                if (it.getEnchantLevel() > 0)
                                    enchantLevel = getMinEnchantLevel(enchantLevel, it.getEnchantLevel(), enchantInitialize);

                                mineralId = it.getAugmentationMineralId();
                                augmentations = it.getAugmentations();
                            }
                            enchantInitialize = false;

                            if (id.getItem().isWeapon() || id.getItem().isArmor() || id.getItem().isAccessory()) {
                                customFlags = id.getItem().getCustomFlags();
                                lifeTime = id.getItem().getTemporalLifeTime();
                            }

                            ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.MultiSellIngredient,
                                    it, count, 0L, _listId);
                            LogService.getInstance().log(LoggerType.ITEM, message);

                            activeChar.sendPacket(SystemMessagePacket.removeItems(id.getId(), count));
                            continue;
                        }

                        //TODO audit
                        return;
                    }
            }

            if (tax > 0 && !notax)
                if (castle != null) {
                    if (merchant != null && merchant.getReflection().isMain()) {
                        castle.addToTreasury(tax, true, false);
                        FractionTreasure.getInstance().update(merchant.getFraction(), tax);
                    }
                }

            List<MultiSellIngredient> products = entry.getProduction();

            if (list1.getType() == MultiSellListContainer.MultisellType.CHANCED) {
                int chancesAmount = 0;
                List<MultiSellIngredient> productsTemp = new ArrayList<>();

                for (MultiSellIngredient in : products) {
                    int chance = in.getChance();

                    if (chance <= 0)
                        continue;

                    chancesAmount += chance;
                    productsTemp.add(in);
                }

                if (Rnd.chance(chancesAmount)) {
                    double chanceMod = (100.0 - chancesAmount) / productsTemp.size();
                    List<MultiSellIngredient> successProducts = new ArrayList<>();
                    int tryCount = 0;

                    while (successProducts.isEmpty()) {
                        ++tryCount;
                        for (MultiSellIngredient in2 : productsTemp) {
                            if (tryCount % 10 == 0)
                                ++chanceMod;

                            if (Rnd.chance(in2.getChance() + chanceMod))
                                successProducts.add(in2);
                        }
                    }

                    MultiSellIngredient[] productionsArray = successProducts.toArray(new MultiSellIngredient[0]);
                    products = new ArrayList<>(1);
                    products.add(productionsArray[Rnd.get(productionsArray.length)]);
                }
            }
            for (MultiSellIngredient in : products) {
                if (in.getItemId() <= 0) {
                    if (in.getItemId() == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE) {
                        activeChar.getClan().incReputation((int) (in.getItemCount() * _amount), false, "MultiSell");
                        activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_CLAN_HAS_ADDED_S1_POINTS_TO_ITS_CLAN_REPUTATION_SCORE).addNumber(in.getItemCount() * _amount));
                    } else if (in.getItemId() == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
                        activeChar.addPcBangPoints((int) (in.getItemCount() * _amount), false, true);
                    else if (in.getItemId() == ItemTemplate.ITEM_ID_FAME)
                        activeChar.setFame(activeChar.getFame() + (int) (in.getItemCount() * _amount), "MultiSell", true);
                } else if (ItemHolder.getInstance().getTemplate(in.getItemId()).isStackable()) {
                    long total = SafeMath.mulAndLimit(in.getItemCount(), _amount);
                    inventory.addItem(in.getItemId(), total);

                    ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.MultiSellProduct,
                            in.getItemId(), total, 0L, _listId);
                    LogService.getInstance().log(LoggerType.ITEM, message);

                    activeChar.sendPacket(SystemMessagePacket.obtainItems(in.getItemId(), total, 0));
                } else
                    for (int i = 0; i < _amount; i++) {
                        ItemInstance product = ItemFunctions.createItem(in.getItemId());

                        if (keepenchant) {
                            if (product.canBeEnchanted()) {
                                product.setEnchantLevel(Math.max(enchantLevel, 0));
                                if (attributes != null)
                                    product.setAttributes(attributes.clone());

                                final int option0 = in.getItemAugmentations()[0];
                                final int option1 = in.getItemAugmentations()[1];
                                if (option0 != 0 || option1 != 0)
                                {
                                    int[] newAugmentations = augmentations.clone();
                                    if (option0 != 0)
                                        newAugmentations[0] = option0;
                                    if (option1 != 0)
                                        newAugmentations[1] = option1;
                                    if (mineralId == 0)
                                        mineralId = -1; // затычка для генератора аугментации

                                    product.setAugmentation(mineralId, newAugmentations);
                                }
                                else if (mineralId != 0)
                                    product.setAugmentation(mineralId, augmentations);
                            }
                        } else {
                            product.setEnchantLevel(in.getItemEnchant());
                            product.setAttributes(in.getItemAttributes().clone());
                        }

                        if (customFlags > 0) {
                            product.setCustomFlags(customFlags);

                            if (lifeTime > 0)
                                product.setLifeTime(lifeTime);
                        }

                        inventory.addItem(product);

                        ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.MultiSellProduct,
                                product, product.getCount(), 0L, _listId);
                        LogService.getInstance().log(LoggerType.ITEM, message);

                        activeChar.sendPacket(SystemMessagePacket.obtainItems(product));

                        // Если ингредиент был одет то пытаемся одеть продукт
                        if (equipped && ItemFunctions.checkIfCanEquip(activeChar, product) == null) {
                            activeChar.getInventory().equipItem(product);
                            equipped = false;
                        }
                    }
            }

            boolean log = merchant != null &&
                ((merchant.getNpcId() == 40008 && items.stream().anyMatch(item -> item.getId() == 75040)) || ItemFunctions.needToLogProducts(products));
            if(log)
                MultisellLoggingService.getInstance().add(products, _amount);
        } catch (ArithmeticException ae) {
            //TODO audit
            activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        } finally {
            inventory.writeUnlock();
        }

        activeChar.sendChanges();

        activeChar.setLastMultisellBuyTime(System.currentTimeMillis());

        if (!list1.isShowAll()) // Если показывается только то, на что хватает материалов обновить окно у игрока
            MultiSellHolder.getInstance().SeparateAndSend(list1, activeChar, list1.getNpcObjectId(),
                    castle == null ? 0 : castle.getSellTaxRate(activeChar));
    }

    private int getMinEnchantLevel(int min, int current, boolean init) {
        if (init) {
            return current;
        }
        return Math.max(Math.min(current, min), 0);
    }

    public class ItemData {
        private final int _id;
        private final long _count;
        private final ItemInstance _item;

        public ItemData(int id, long count, ItemInstance item) {
            _id = id;
            _count = count;
            _item = item;
        }

        public int getId() {
            return _id;
        }

        public long getCount() {
            return _count;
        }

        public ItemInstance getItem() {
            return _item;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ItemData))
                return false;
            ItemData i = (ItemData) obj;
            return _id == i._id && _count == i._count && _item == i._item;
        }

        @Override
        public int hashCode() {
            int hash = _item.hashCode();
            hash = 76 * hash + _id;
            hash = 76 * hash + (int) (_count / 1757L);
            return hash;
        }
    }
}
