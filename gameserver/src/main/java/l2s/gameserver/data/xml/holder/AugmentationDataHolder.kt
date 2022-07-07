package l2s.gameserver.data.xml.holder

import l2s.commons.data.xml.AbstractHolder
import l2s.gameserver.templates.augmentation.AugmentationInfo
import java.util.*

/**
 * @author VISTALL
 * @date 15:10/14.03.2012
 */
object AugmentationDataHolder : AbstractHolder() {
    private val augmentationInfo = HashSet<AugmentationInfo>()

    override fun size(): Int {
        return augmentationInfo.size
    }

    override fun clear() {
        augmentationInfo.clear()
    }

    fun addAugmentationInfo(augmentationInfo: AugmentationInfo) {
        this.augmentationInfo.add(augmentationInfo)
    }

}