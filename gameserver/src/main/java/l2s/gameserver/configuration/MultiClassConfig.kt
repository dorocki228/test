package l2s.gameserver.configuration

import com.typesafe.config.ConfigFactory
import l2s.gameserver.templates.item.data.ItemData
import java.io.File

object MultiClassConfig {

    val rebirthRequireLevel: Int
    val rebirthRequireSp: Long
    val rebirthRequireItems: Map<Int, List<ItemData>>
    val rebirthRewardItems: List<ItemData>

    init {
        val conf = ConfigFactory.parseFile(File("config/multi_class.conf"))
        rebirthRequireLevel = conf.getInt("rebirth.require.level")
        rebirthRequireSp = conf.getLong("rebirth.require.sp")
        rebirthRequireItems = conf.getConfig("rebirth.require.items").entrySet().associate { entry ->
            val value = entry.value.render()
            entry.key.toInt() - 1 to value.split(";").map {
                ItemData(it)
            }
        }.let { rebirthRequireItems ->
            rebirthRequireItems.withDefault {
                rebirthRequireItems.entries.last().value
            }
        }
        rebirthRewardItems = conf.getConfig("rebirth.reward.items").entrySet().flatMap { entry ->
            val value = entry.value.render()
            value.split(";").map {
                ItemData(it)
            }
        }
    }

}