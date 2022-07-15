package l2s.gameserver.model.items

import l2s.gameserver.model.Player
import l2s.gameserver.utils.CraftHelper

/**
 * @author Java-man
 * @since 28.08.2019
 */
class DynamicManufactureItem(
    recipeId: Int,
    cost: Long,
    buyer: Player,
    manufacturer: Player
) : ManufactureItem(recipeId, cost) {

    val canCraft: Boolean
    val canBeCriticalCrafted: Boolean

    init {
        val recipe = CraftHelper.findRecipeByIdAndPrice(manufacturer.createList, recipeId, cost)
        if (recipe == null) {
            canCraft = false
            canBeCriticalCrafted = false
        } else {
            val check1 = CraftHelper.checkConditions(recipe, manufacturer)
            val check2 = CraftHelper.checkItems(recipe, buyer, cost)
            canCraft = check1 == null && check2 == null
            canBeCriticalCrafted = recipe.isCanBeCriticalCrafted
        }
    }

}