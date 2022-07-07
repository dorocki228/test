package l2s.gameserver.templates.augmentation

import l2s.commons.math.random.RndSelector
import l2s.gameserver.model.reward.RewardList

/**
 * @author VISTALL
 * @date 15:31/14.03.2012
 */
class OptionGroup(private val options: RndSelector<Int>) {

    fun random(): Int? {
        return options.chance(RewardList.MAX_CHANCE)
    }

}
