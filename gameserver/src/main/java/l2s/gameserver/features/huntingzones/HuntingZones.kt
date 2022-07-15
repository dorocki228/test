package l2s.gameserver.features.huntingzones

import kotlinx.serialization.Serializable

/**
 * @author Java-man
 * @since 01.07.2019
 */
@Serializable
data class HuntingZones(val zones: List<HuntingZone>)

@Serializable
data class HuntingZone(val enabled: Boolean = true, val id: Int, val name: String, val price: Long, val location: HuntingZoneLocation)

@Serializable
data class HuntingZoneLocation(val x: Int, val y: Int, val z: Int)