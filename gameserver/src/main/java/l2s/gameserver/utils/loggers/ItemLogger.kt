package l2s.gameserver.utils.loggers

import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.MetadataKey
import l2s.gameserver.model.Creature
import l2s.gameserver.model.items.ItemInstance

/**
 * @author Java-man
 * @since 26.08.2019
 *
 * TODO move to items module
 */
object ItemLogger {

    private val logger = GoogleLogger.forEnclosingClass()

    private val processKey = MetadataKey.single("process", ItemProcess::class.java)
    private val actorKey = MetadataKey.single("actor", Creature::class.java)
    private val receiverKey = MetadataKey.single("receiver", PlayerInfo::class.java)
    private val itemIdKey = MetadataKey.single("itemId", Int::class.java)
    private val countKey = MetadataKey.single("count", Long::class.java)
    private val descriptionKey = MetadataKey.single("description", String::class.java)

    fun log(
        process: ItemProcess,
        actor: Creature,
        item: ItemInstance
    ) {
        log(process, actor, null, item.itemId, item.count, "")
    }

    fun log(
        process: ItemProcess,
        actor: Creature,
        item: ItemInstance,
        description: String
    ) {
        log(process, actor, null, item.itemId, item.count, description)
    }

    fun log(
        process: ItemProcess,
        actor: Creature,
        receiver: PlayerInfo,
        item: ItemInstance
    ) {
        log(process, actor, receiver, item.itemId, item.count, "")
    }

    fun log(
        process: ItemProcess,
        actor: Creature,
        receiver: PlayerInfo? = null,
        item: ItemInstance,
        description: String = ""
    ) {
        log(process, actor, receiver, item.itemId, item.count, description)
    }

    fun log(
        process: ItemProcess,
        actor: Creature,
        receiver: PlayerInfo? = null,
        itemId: Int,
        count: Long,
        description: String
    ) {
        var temp = logger.atInfo()
            .with(processKey, process)
            .with(actorKey, actor)
        if (receiver != null) {
            temp = temp.with(receiverKey, receiver)
        }
        temp = temp
            .with(itemIdKey, itemId)
            .with(countKey, count)
            .with(descriptionKey, description)
        temp.log()
    }

    enum class ItemProcess {
        Create,
        Delete,
        Drop,
        PvPPlayerDieDrop,
        PvEPlayerDieDrop,
        Crystalize,
        EnchantFail,
        Pickup,
        PartyPickup,
        PrivateStoreBuy,
        PrivateStoreSell,
        TradeBuy,
        TradeSell,
        PostRecieve,
        SafePostRecieve,
        PostPaymentRecieve,
        PostSend,
        PostCancel,
        PostExpire,
        RefundSell,
        RefundReturn,
        WarehouseDeposit,
        WarehouseWithdraw,
        FreightWithdraw,
        FreightDeposit,
        ClanWarehouseDeposit,
        ClanWarehouseWithdraw,
        CommissionBuy,
        CommissionSell,
        CommissionRegistered,
        CommissionUnregister,
        CommissionExpiredReturn,
        ClanChangeLeaderRequestAdd,
        ClanChangeLeaderRequestDone,
        ClanChangeLeaderRequestCancel,
        ItemMallBuy,
        DelayedItemReceive,
        ClanWar,
    }

}