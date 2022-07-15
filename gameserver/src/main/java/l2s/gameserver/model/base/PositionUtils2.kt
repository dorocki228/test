package l2s.gameserver.model.base

import l2s.gameserver.geodata.GeoEngine
import l2s.gameserver.geometry.ILocation
import l2s.gameserver.geometry.Location
import l2s.gameserver.model.Creature
import l2s.gameserver.model.World
import l2s.gameserver.network.l2.s2c.ExServerPrimitive
import l2s.gameserver.utils.PositionUtils
import l2s.gameserver.utils.PositionUtils.Position
import org.apache.commons.lang3.mutable.MutableInt
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Sdw
 * @since 16.10.2019
 */
object PositionUtils2 {

    /**
     * Position calculation based on the retail-like formulas:<br></br>
     *
     *  * heading: (unsigned short) abs(heading - (unsigned short)(int)floor(atan2(toY - fromY, toX - fromX) * 65535.0 / 6.283185307179586))
     *  * side: if (heading >= 0x2000 && heading <= 0x6000 || (unsigned int)(heading - 0xA000) <= 0x4000)
     *  * front: else if ((unsigned int)(heading - 0x2000) <= 0xC000)
     *  * back: otherwise.
     *
     * @param from
     * @param to
     * @return
     */
    fun getPosition(from: ILocation, to: ILocation): Position {
        var heading = PositionUtils.calculateHeadingFrom(from.x.toDouble(), from.y.toDouble(), to.x.toDouble(), to.y.toDouble())
        heading = abs(to.heading - heading)
        return if (heading >= 0x2000 && heading <= 0x6000
            || Integer.toUnsignedLong(heading - 0xA000) <= 0x4000) {
            Position.SIDE
        } else if (Integer.toUnsignedLong(heading - 0x2000) <= 0xC000) {
            Position.FRONT
        } else {
            Position.BACK
        }
    }

    fun drawPosition(activeChar: Creature) {
        val x = activeChar.x
        val y = activeChar.y
        val z = activeChar.z
        val heading = activeChar.heading
        val headingSides = intArrayOf(
            (heading + 0x2000) % 65535,
            (heading + 0x6000) % 65535,
            (heading + 0xA000) % 65535,
            (heading + 0xE000) % 65535
        )

        val packet =
            ExServerPrimitive(Position::class.java.simpleName + "-" + activeChar.objectId, x, y, World.MAP_MIN_Z)

        val radius = 100.0
        var locs = Array<ILocation>(headingSides.size) {
            val radian = PositionUtils.convertHeadingToRadian(headingSides[it])
            val tx = x + radius * cos(radian)
            val ty = y + radius * sin(radian)
            val tz = GeoEngine.getLowerHeight(tx.toInt(), ty.toInt(), z, activeChar.geoIndex)
            Location(tx.toInt(), ty.toInt(), tz)
        }

        packet.addLine("FRONT (0x2000)", Color.GREEN, true, activeChar, locs[0])
        packet.addLine("BACK (0x6000)", Color.RED, true, x, y, z, locs[1])
        packet.addLine("BACK (0xA000)", Color.RED, true, x, y, z, locs[2])
        packet.addLine("FRONT (0xE000)", Color.GREEN, true, x, y, z, locs[3])

        val maxPoints = 36
        val anglePoint = Math.PI * 2 / maxPoints
        locs = Array<ILocation>(maxPoints) {
            val tx = x + radius * cos(anglePoint * it)
            val ty = y + radius * sin(anglePoint * it)
            val tz = GeoEngine.getLowerHeight(tx.toInt(), ty.toInt(), z, activeChar.geoIndex)
            Location(tx.toInt(), ty.toInt(), tz)
        }

        for (loc in locs) {
            when (getPosition(loc, activeChar)) {
                Position.FRONT -> packet.addPoint(Color.GREEN, loc)
                Position.SIDE -> packet.addPoint(Color.YELLOW, loc)
                Position.BACK -> packet.addPoint(Color.red, loc)
            }
        }

        val pointsLeft = MutableInt(maxPoints)
        val finalRadius = radius.toInt() * 2
        World.getAroundObjects(activeChar, finalRadius, finalRadius).forEach {
            // Prevent packet overflow.
            if (pointsLeft.decrementAndGet() < 0) {
                return@forEach
            }

            when (val pos = getPosition(it, activeChar)) {
                Position.FRONT -> packet.addPoint(pos.toString(), Color.GREEN, true, it)
                Position.SIDE -> packet.addPoint(pos.toString(), Color.YELLOW, true, it)
                Position.BACK -> packet.addPoint(pos.toString(), Color.red, true, it)
            }
        }

        activeChar.sendPacket(packet)
    }

}