package l2s.gameserver.utils

import l2s.gameserver.data.xml.holder.RecipeHolder
import l2s.gameserver.model.Player
import l2s.gameserver.model.Skill
import l2s.gameserver.model.items.ManufactureItem
import l2s.gameserver.network.l2.components.IBroadcastPacket
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.ActionFailPacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.item.RecipeTemplate

/**
 * @author Java-man
 * @since 28.08.2019
 */
object CraftHelper {

    fun findRecipeByIdAndPrice(
        createList: MutableMap<Int, ManufactureItem>,
        recipeId: Int,
        price: Long
    ):
            RecipeTemplate? {
        val haveRecipe = createList.values
            .filter { mi -> mi.recipeId == recipeId }
            .any { mi -> price == mi.cost }
        return when {
            haveRecipe -> RecipeHolder.getInstance().getRecipeByRecipeId(recipeId)
            else -> null
        }
    }

    fun checkConditions(recipe: RecipeTemplate, manufacturer: Player): IBroadcastPacket? {
        if (recipe.materials.isEmpty() || recipe.products.isEmpty()) {
            return SystemMsg.THE_RECIPE_IS_INCORRECT
        }

        val level = if (recipe.isCommon) {
            manufacturer.getSkillLevel(Skill.SKILL_COMMON_CRAFTING)
        } else {
            manufacturer.stat.createItemLevel
        }
        if (recipe.level > level) {
            //TODO: Должно ли быть сообщение?
            return ActionFailPacket.STATIC
        }

        if (!manufacturer.findRecipe(recipe.id)) {
            return ActionFailPacket.STATIC
        }

        if (manufacturer.currentMp < recipe.mpConsume) {
            return SystemMsg.NOT_ENOUGH_MP
        }

        return null
    }

    fun checkItems(
        recipe: RecipeTemplate,
        buyer: Player,
        price: Long
    ): IBroadcastPacket? {
        if (buyer.adena < price) {
            return SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA
        }

        for (material in recipe.materials) {
            if (material.count == 0L)
                continue

            val item = buyer.inventory.getItemByItemId(material.id)
            if (item == null || material.count > item.count) {
                return SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION
            }
        }

        return null
    }

    fun craft(
        recipe: RecipeTemplate,
        buyer: Player,
        manufacturer: Player,
        price: Long,
        priceWithTax: Long
    ): IBroadcastPacket? {
        buyer.inventory.writeLock()
        try {
            val packet2 = CraftHelper.checkItems(
                recipe, buyer,
                price
            )
            if (packet2 != null) {
                return packet2
            }

            if (!buyer.reduceAdena(price, false)) {
                return SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA
            }

            for (material in recipe.materials) {
                if (material.count == 0L)
                    continue

                if (!buyer.inventory.destroyItemByItemId(material.id, material.count)) {
                    return SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION
                }
                //TODO audit
                buyer.sendPacket(SystemMessagePacket.removeItems(material.id, material.count))
            }

            manufacturer.addAdena(priceWithTax)
        } finally {
            buyer.inventory.writeUnlock()
        }

        return null
    }

}