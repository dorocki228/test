package parsers

import com.charleskorn.kaml.Yaml
import com.google.common.io.Resources
import l2s.gameserver.features.huntingzones.HuntingZone
import l2s.gameserver.features.huntingzones.HuntingZoneLocation
import l2s.gameserver.features.huntingzones.HuntingZones
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Java-man
 * @since 02.08.2019
 */
object HuntingZonesParser {

    // example:
    // Hunt_begin	id=1	type=0	rc_level={0;0}	loc={0.0;0.0;0.0}	desc=[Territory of Dion. Because of the warm climate, these farm lands are fertile and are responsible for the food supply in Aden.]	search_zoneid=1	name=[Dion Territory]	regionid=0	npc_id=0	quest_id={}	instantzone_id=0	Hunt_end
    val regex = Regex("""Hunt_begin	id=(\d+)	type=(\d+)	rc_level=\{(\d+);(\d+)\}	loc=\{(-?\d+).0;(-?\d+).0;(-?\d+).0\}	desc=\[.*\]	search_zoneid=\d+	name=\[(.*)\]	regionid=\d+	npc_id=\d+	quest_id=\{.*\}	instantzone_id=\d+	Hunt_end""")

    @JvmStatic
    fun main(args: Array<String>) {
        var count = 0

        val result = mutableListOf<HuntingZone>()

        val path = Resources.getResource("HuntingZone_Classic-e.txt")
        Files.readAllLines(Path.of(path.toURI()), Charsets.ISO_8859_1).forEach { line ->
            val matchResult = regex.find(line)
            matchResult?.let {
                val (id, type, rc_level1, rc_level2,
                        x, y, z, name) = it.destructured
                println("id = $id, type = $type, x = $x, y = $y, z = $z, name = $name")

                result.add(HuntingZone(true, id.toInt(), name, 10_000,
                        HuntingZoneLocation(x.toInt(), y.toInt(), z.toInt())))

                count++
            }
        }

        val huntingZones = HuntingZones(result)
        val output = Yaml.default.encodeToString(HuntingZones.serializer(), huntingZones)

        println("count = $count")

        Files.writeString(Path.of("hunting_zones.yaml"), output)
    }

}