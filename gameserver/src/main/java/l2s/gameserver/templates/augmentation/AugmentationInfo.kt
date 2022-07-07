package l2s.gameserver.templates.augmentation

import l2s.commons.math.random.RndSelector
import l2s.gameserver.model.reward.RewardList
import l2s.gameserver.templates.item.ItemTemplate

/**
 * @author VISTALL
 * @date 15:14/14.03.2012
 */
class AugmentationInfo(
    val mineralId: Int,
    val feeItemId: Int,
    val feeItemCount: Long,
    val cancelFee: Long,
    private val optionGroups: Array<Array<RndSelector<OptionGroup>>>
) {

    fun randomOption(itemTemplate: ItemTemplate): IntArray? {
        val rnd = optionGroups[if (itemTemplate.isMagicWeapon) 1 else 0]
        val size = rnd.size

        if (size == 1) {
            return intArrayOf(selectOption(rnd[0]), 0)
        }

        return IntArray(size) {
            selectOption(rnd[it])
        }
    }

    private fun selectOption(groupSelector: RndSelector<OptionGroup>): Int {
        val randomGroup = groupSelector.chance(RewardList.MAX_CHANCE) ?: return 0
        return randomGroup.random() ?: 0
    }

    companion object {
        const val MAX_AUGMENTATION_COUNT = 2
    }

}
