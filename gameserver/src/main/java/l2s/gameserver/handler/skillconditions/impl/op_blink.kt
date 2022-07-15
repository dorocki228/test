package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.geodata.GeoEngine
import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.utils.PositionUtils
import l2s.gameserver.utils.PositionUtils.Position
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Sdw
 * @author Java-man
 */
class op_blink(params: StatsSet) : SkillCondition(params) {

    private val _angle: Int
    private val _range = params.getInteger("op_blink_param2")

    init {
        _angle = when (params.getEnum(
                "op_blink_param1",
                Position::class.java,
                true
        )) {
            Position.BACK -> 0
            Position.FRONT -> 180
            else -> -1
        }
    }

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val angle: Double = PositionUtils.convertHeadingToDegree(caster.heading)
        val radian = Math.toRadians(angle)
        val course = Math.toRadians(_angle.toDouble())
        val x1 = cos(Math.PI + radian + course) * _range
        val y1 = sin(Math.PI + radian + course) * _range

        val x = caster.x + x1
        val y = caster.y + y1
        val z = caster.z.toDouble()

        return GeoEngine.canMoveToCoord(
                caster.x,
                caster.y,
                caster.z,
                x.toInt(),
                y.toInt(),
                z.toInt(),
                caster.geoIndex
        )
    }

}