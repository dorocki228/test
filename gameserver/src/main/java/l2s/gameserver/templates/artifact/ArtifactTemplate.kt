package l2s.gameserver.templates.artifact

import l2s.gameserver.model.base.Fraction
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.item.data.ItemData
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.utils.Location

import java.util.*

class ArtifactTemplate(
    val id: Int,
    val npc: NpcTemplate,
    val location: Location,
    val protectTime: Int,
    val stringName: String
) {

    val skillEntryList = ArrayList<SkillEntry>()
    val teleportLocations = ArrayList<Location>()
    val rewardItems = ArrayList<ItemData>()
    val spawnGroups = HashMap<Fraction, MutableSet<String>>()
    val params = StatsSet()

    fun addSkill(skillEntry: SkillEntry) {
        skillEntryList.add(skillEntry)
    }

    fun addRewardItem(itemData: ItemData) {
        rewardItems.add(itemData)
    }

    fun addParam(key: String, value: Any) {
        params[key] = value
    }

    fun addSpawnGroup(fraction: Fraction, name: String) {
        val set = spawnGroups.computeIfAbsent(fraction) { HashSet() }
        set.add(name)
    }

    fun addTeleportLocation(location: Location) {
        teleportLocations.add(location)
    }

}
