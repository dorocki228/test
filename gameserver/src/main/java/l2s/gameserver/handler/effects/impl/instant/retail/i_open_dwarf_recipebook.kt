package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.RecipeBookItemListPacket
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Open Dwarf Recipe Book effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_open_dwarf_recipebook(template: EffectTemplate?) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return

        if (casterPlayer.privateStoreType == Player.STORE_PRIVATE_MANUFACTURE) {
            casterPlayer.sendPacket(SystemMsg.YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING)
            return
        }

        if (casterPlayer.isInStoreMode) {
            casterPlayer.sendPacket(SystemMsg.ITEM_CREATION_IS_NOT_POSSIBLE_WHILE_ENGAGED_IN_A_TRADE)
            return
        }

        if (casterPlayer.isProcessingRequest) {
            casterPlayer.sendPacket(SystemMsg.ITEM_CREATION_IS_NOT_POSSIBLE_WHILE_ENGAGED_IN_A_TRADE)
            return
        }

        casterPlayer.sendPacket(RecipeBookItemListPacket(casterPlayer, true))
    }

}