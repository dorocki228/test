package l2s.gameserver.punishment

import com.google.common.collect.HashBasedTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import com.google.common.collect.Table as GuavaTable

/**
 * @author Java-man
 * @since 07.06.2019
 */
object PunishmentDao {

    private val logger = LoggerFactory.getLogger(PunishmentDao::class.java)

    fun onInit() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(PunishmentTable)
        }
    }

    /**
     * Сохранение информации о бане. Момент сохранения бана будет являтся временем бана.
     *
     * @param data
     */
    fun insert(
        type: PunishmentType,
        target: String,
        endDate: ZonedDateTime,
        punishedBy: String,
        reason: String
    ) {
        val temp = endDate.toDateTime()

        transaction {
            PunishmentTable.insert {
                it[dateColumn] = DateTime.now()
                it[typeColumn] = type
                it[targetColumn] = target
                it[endDateColumn] = temp
                it[reasonColumn] = reason
                it[punishedByColumn] = punishedBy
            }
        }
    }

    /**
     * Удаление наказания указанного аккаунта.
     *
     * @param type
     * @param target
     */
    fun delete(type: PunishmentType, target: String, endDate: ZonedDateTime) {
        val temp = endDate.toDateTime()

        transaction {
            PunishmentTable.deleteWhere {
                PunishmentTable.typeColumn eq type and (PunishmentTable.targetColumn eq target) and (PunishmentTable.endDateColumn eq temp)
            }
        }
    }

    /**
     * Удаление наказания указанного аккаунта.
     *
     * @param type
     * @param target
     */
    fun deleteAll(type: PunishmentType, target: String) {
        transaction {
            PunishmentTable.deleteWhere {
                PunishmentTable.typeColumn eq type and (PunishmentTable.targetColumn eq target)
            }
        }
    }

    fun load(reload: Boolean): GuavaTable<PunishmentType, String, MutableList<PunishmentData>> {
        val temp = transaction {
            PunishmentTable.selectAll().groupByTo(
                mutableMapOf(),
                {
                    it[PunishmentTable.typeColumn]
                },
                {
                    val target = it[PunishmentTable.targetColumn]
                    val endDate = it[PunishmentTable.endDateColumn].toZonedDateTime()
                    val reason = it[PunishmentTable.reasonColumn]
                    val punishedBy = it[PunishmentTable.punishedByColumn]
                    target to PunishmentData(endDate, punishedBy, reason)
                }
            ).entries.groupByTo(mutableMapOf(), { it.key },
                { it.value.groupByTo(mutableMapOf(), { it.first }, { it.second }) })
        }

        val result = HashBasedTable.create<PunishmentType, String, MutableList<PunishmentData>>()
        temp.forEach { entry ->
            val now = ZonedDateTime.now()
            entry.value.forEach { listOfMaps ->
                listOfMaps.forEach { map ->
                    val bans = map.value
                        .filterNot { it.endDate.isBefore(now) }
                        .toMutableList()
                    result.put(entry.key, map.key, bans)
                }
            }
        }

        if (!reload) {
            PunishmentType.values().forEach {
                val count = result.row(it).keys.size
                logger.info("loaded $count $it punishments.")
            }
        }

        return result
    }

    private object PunishmentTable : Table("punishments") {
        val dateColumn = datetime("date")
        val typeColumn = enumerationByName("type", 50, PunishmentType::class)
        val targetColumn = varchar("target", 64)
        val endDateColumn = datetime("end_date")
        val reasonColumn = varchar("reason", 255)
        val punishedByColumn = varchar("punished_by", 255)
    }

}

fun DateTime.toZonedDateTime(): ZonedDateTime {
    val instant = Instant.ofEpochMilli(this.millis)
    val zoneId = ZoneId.of(this.zone.id, ZoneId.SHORT_IDS)
    return ZonedDateTime.ofInstant(instant, zoneId)
}

fun ZonedDateTime.toDateTime(): DateTime {
    return DateTime(this.toInstant().toEpochMilli(), DateTimeZone.forID(this.offset.id))
}