package l2s.gameserver.data.xml.parser

import l2s.commons.data.xml.AbstractParser
import l2s.commons.lang.ArrayUtils
import l2s.commons.math.random.RndSelector
import l2s.commons.math.random.RndSelector.RndNode
import l2s.gameserver.Config
import l2s.gameserver.data.xml.holder.AugmentationDataHolder
import l2s.gameserver.data.xml.holder.ItemHolder
import l2s.gameserver.model.reward.RewardList
import l2s.gameserver.templates.augmentation.AugmentationInfo
import l2s.gameserver.templates.augmentation.OptionGroup
import org.dom4j.Element
import org.napile.primitive.lists.impl.ArrayIntList
import org.napile.primitive.maps.impl.HashIntObjectMap
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author VISTALL
 * @date 15:10/14.03.2012
 */
object AugmentationDataParser:
    AbstractParser<AugmentationDataHolder>(AugmentationDataHolder) {

    override fun getXMLPath(): File {
        return File(Config.DATAPACK_ROOT, "data/augmentation_data.xml")
    }

    override fun getDTDFileName(): String {
        return "augmentation_data.dtd"
    }

    @Throws(Exception::class)
    override fun readData(rootElement: Element) {
        val items = HashMap<String, IntArray>()
        val variants = HashIntObjectMap<Array<Array<RndSelector<OptionGroup>>>>()

        run {
            val iterator = rootElement.elementIterator("item_group")
            while (iterator.hasNext()) {
                val element = iterator.next()

                val name = element.attributeValue("name")

                val itemElements = element.elements()
                val list = ArrayIntList()
                for (itemElement in itemElements) {
                    val itemId = Integer.parseInt(itemElement.attributeValue("id"))

                    val itemTemplate = ArrayUtils.valid(ItemHolder.getInstance().allTemplates, itemId)
                    if (itemTemplate == null) {
                        warn("Not found item: $itemId; item group: $name")
                        continue
                    }

                    list.add(itemId)
                }
                items[name] = list.toArray()
            }
        }


        run {
            val iterator = rootElement.elementIterator("variants")
            while (iterator.hasNext()) {
                val element = iterator.next()

                val itemId = Integer.parseInt(element.attributeValue("mineral_id"))

                val warriorVariation = readVariation(element.element("warrior_variation"))
                val mageVariation = readVariation(element.element("mage_variation"))
                val ar = arrayOf(warriorVariation, mageVariation)

                variants.put(itemId, ar)
            }
        }

        val iterator = rootElement.elementIterator("augmentation_data")
        while (iterator.hasNext()) {
            val augmentElement = iterator.next()

            val mineralId = Integer.parseInt(augmentElement.attributeValue("mineral_id"))
            val feeItemId = Integer.parseInt(augmentElement.attributeValue("fee_item_id"))
            val feeItemCount = Integer.parseInt(augmentElement.attributeValue("fee_item_count")).toLong()
            val cancelFee = Integer.parseInt(augmentElement.attributeValue("cancel_fee")).toLong()
            val itemGroup = augmentElement.attributeValue("item_group")
            val rndSelectors = variants.get(mineralId)
            if (rndSelectors == null) {
                warn("Not find variants for mineral: $mineralId")
                continue
            }

            if (rndSelectors.size < 2) {
                warn("Less than 2 variants for mineral: $mineralId")
                continue
            }

            val augmentationInfo = AugmentationInfo(mineralId, feeItemId, feeItemCount, cancelFee, rndSelectors)
            holder.addAugmentationInfo(augmentationInfo)

            require(items.contains(itemGroup))
            for (i in items[itemGroup]!!)
                ItemHolder.getInstance().getTemplate(i).addAugmentationInfo(augmentationInfo)
        }
    }

    private fun readVariation(warElement: Element?): Array<RndSelector<OptionGroup>> {
        if (warElement == null)
            return emptyArray()

        val sel = ArrayList<RndSelector<OptionGroup>>(AugmentationInfo.MAX_AUGMENTATION_COUNT)

        //<variant>
        for (variantElement in warElement.elements()) {
            // <group>
            var allGroupChance = 0

            val groupRndNodes = ArrayList<RndNode<OptionGroup>>()

            for (groupElement in variantElement.elements()) {
                val chance = (java.lang.Double.parseDouble(groupElement.attributeValue("chance")) * 10000).toInt()
                allGroupChance += chance

                val optionRndNodes = ArrayList<RndNode<Int>>()

                var allSubGroupChance = 0
                // <option>
                for (optionElement in groupElement.elements()) {
                    val optionId = Integer.parseInt(optionElement.attributeValue("id"))
                    val optionChance =
                        (java.lang.Double.parseDouble(optionElement.attributeValue("chance")) * 10000).toInt()
                    allSubGroupChance += optionChance

                    optionRndNodes.add(RndNode.create(optionId, optionChance))
                }

                val optionGroup = OptionGroup(RndSelector.create(optionRndNodes))
                groupRndNodes.add(RndNode.create(optionGroup, chance))

                if (allSubGroupChance != RewardList.MAX_CHANCE && optionRndNodes.size != 2)
                // со второй вариации - мы игнорируем шанс
                    error(
                        "Sum of subgroups is not max, element: " + warElement.name + ", mineral: " + warElement.parent.attributeValue(
                            "mineral_id"
                        )
                    )
            }

            sel.add(RndSelector.create(groupRndNodes))

            if (allGroupChance != RewardList.MAX_CHANCE)
                error(
                    "Sum of groups is not max, element: " + warElement.name + ", mineral: " + warElement.parent.attributeValue(
                        "mineral_id"
                    )
                )
        }

        return sel.toTypedArray()
    }

}