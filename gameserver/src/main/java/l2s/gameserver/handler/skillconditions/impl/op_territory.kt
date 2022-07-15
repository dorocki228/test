package l2s.gameserver.handler.skillconditions.impl

import l2s.commons.geometry.Point2D
import l2s.commons.geometry.Polygon
import l2s.gameserver.geometry.Territory
import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.World
import l2s.gameserver.templates.StatsSet
import kotlin.math.max
import kotlin.math.min

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_territory(params: StatsSet) : SkillCondition(params) {

    private val territory = Territory()
    private val applyTo: AffectType

    init {
        val locs = params.getString("op_territory_param1")
        if (locs != null) {
            val polygon = Polygon()
            polygon.zmin = World.MAP_MIN_Z
            polygon.zmax = World.MAP_MAX_Z
            locRegex.findAll(locs)
                .map { it.groupValues[1] }
                .forEach {
                    val temp = it.split(";").map { it.toInt() }
                    polygon.add(Point2D(temp[0], temp[1]))
                    polygon.zmin = min(polygon.zmin, temp[2])
                    polygon.zmax = max(polygon.zmax, temp[3])
                }
            territory.add(polygon)
        } else {
            error("Can't parse locs param: $locs")
        }

        val applyToTemp = params.getString("op_territory_param2")
        applyTo = when (applyToTemp) {
            "t_all" -> AffectType.ALL
            "t_self" -> AffectType.SELF
            else -> error("Can't parse applyTo param: $applyToTemp")
        }
    }

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return when (applyTo) {
            AffectType.SELF -> territory.isInside(caster)
            else -> territory.isInside(caster) && territory.isInside(target)
        }
    }

    enum class AffectType {
        SELF,
        ALL
    }

    companion object {

        val locRegex = """\{(-?\d+;+-?\d+;+-?\d+;+-?\d+)\}""".toRegex()

    }

}
