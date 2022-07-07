package l2s.gameserver.logging;

/**
 * @author Java-man
 * @since 23.04.2018
 */
public enum ItemLogProcess
{
    Create,
    Delete,
    Drop,
    PvPDrop,
    Crystalize,
    EnchantFail,
    Pickup,
    PetPickup,
    PartyPickup,
    PrivateStoreBuy,
    PrivateStoreSell,
    RecipeShopBuy, //TODO
    RecipeShopSell, //TODO
    CraftCreate, //TODO
    CraftDelete, //TODO
    TradeBuy,
    TradeSell,
    FromPet,
    ToPet,
    PostRecieve,
    PostSend,
    PostCancel,
    PostExpire,
    PostPrice,
    RefundSell, //TODO
    RefundReturn, //TODO
    WarehouseDeposit,
    WarehouseWithdraw,
    FreightWithdraw,
    FreightDeposit,
    ClanWarehouseDeposit,
    ClanWarehouseWithdraw,
    ExtractCreate, //TODO
    ExtractDelete, //TODO
    NpcBuy,
    NpcCreate, //TODO
    NpcDelete, //TODO
    MultiSellIngredient,
    MultiSellProduct,
    QuestCreate, //TODO
    QuestDelete, //TODO
    EventCreate, //TODO
    EventDelete, //TODO
    ItemMallBuy,
    DelayedItemReceive
}
