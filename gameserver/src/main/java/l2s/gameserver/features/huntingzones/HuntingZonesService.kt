package l2s.gameserver.features.huntingzones

import com.charleskorn.kaml.Yaml
import l2s.gameserver.Config
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Java-man
 * @since 01.07.2019
 */
object HuntingZonesService {

    private val logger = LoggerFactory.getLogger(HuntingZonesService::class.java)

    private val data: Map<Int, HuntingZone>

    init {
        val input = Files.readString(Path.of(Config.DATAPACK_ROOT.toURI()).resolve("data/hunting_zones.yaml"))
        val result = Yaml.default.decodeFromString(HuntingZones.serializer(), input)
        data = result.zones.associateBy { it.id }
    }

    fun load() {
        logger.info("loaded ${data.size}(s) hunting zones.")
    }

    fun getZone(id: Int): HuntingZone? = data[id]

}