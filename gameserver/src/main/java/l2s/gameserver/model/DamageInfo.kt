package l2s.gameserver.model

import com.google.common.collect.EvictingQueue
import l2s.commons.network.PacketWriter
import l2s.gameserver.model.instances.NpcInstance
import l2s.gameserver.templates.skill.SkillClassId
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * @author Java-man
 * @since 17.08.2019
 *
 * TODO add entries removal by time
 */
class DamageInfo() {

    private val entries: Queue<DamageInfoEntry> = CircularFifoQueue(MAX_SIZE)

    private val lock = ReentrantLock()

    constructor(entries: Queue<DamageInfoEntry>) : this() {
        lock.withLock { this.entries.addAll(entries) }
    }

    fun add(damageInfoEntry: DamageInfoEntry) {
        lock.withLock { entries.add(damageInfoEntry) }
    }

    fun clear() {
        lock.withLock { entries.clear() }
    }

    fun copy(): DamageInfo {
        return lock.withLock {
            DamageInfo(entries)
        }
    }

    fun write(packetWriter: PacketWriter) {
        lock.withLock {
            packetWriter.writeH(entries.size) // damage taken list size
            entries.forEach {
                it.write(packetWriter)
            }
        }
    }

    companion object {

        private const val MAX_SIZE = 10

    }

}

enum class AttackerType {
    PLAYABLE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(0)
        }
    },
    NPC {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(1)
        }
    };

    abstract fun write(packetWriter: PacketWriter)
}

data class DamageInfoEntry(
    val attackerType: AttackerType, val attackerName: AttackerName,
    val pledgeName: PledgeName, val skillClassId: SkillClassId,
    val damage: DamageAmount, val damageType: DamageType
) {

    fun write(packetWriter: PacketWriter) {
        attackerType.write(packetWriter) // attacker type
        attackerName.write(packetWriter)
        pledgeName.write(packetWriter)
        skillClassId.write(packetWriter)
        damage.write(packetWriter)
        damageType.write(packetWriter)
    }

}

sealed class AttackerName {

    companion object {

        fun create(creature: Creature): AttackerName {
            if (creature.isNpc) {
                val npc = creature as NpcInstance
                return NpcAttackerName(npc.npcId + 1000000)
            }

            return PlayerAttackerName(creature.name)
        }

        fun create(playable: Playable): AttackerName {
            return PlayerAttackerName(playable.player.name)
        }

        fun create(npc: NpcInstance): AttackerName {
            return NpcAttackerName(npc.npcId + 1000000)
        }

    }

    abstract fun write(packetWriter: PacketWriter)

}

private data class PlayerAttackerName(val value: String) : AttackerName() {

    override fun write(packetWriter: PacketWriter) {
        packetWriter.writeS(value) // attacker name
    }

}

private data class NpcAttackerName(val value: Int) : AttackerName() {

    override fun write(packetWriter: PacketWriter) {
        packetWriter.writeD(value) // attacker name
    }

}

data class DamageAmount(private val value: Double) {

    fun write(packetWriter: PacketWriter) {
        packetWriter.writeF(value)
    }

}

enum class DamageType {
    NORMAL_DAMAGE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(1)
        }
    },
    FALL_DAMAGE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(2)
        }
    },
    DROWN {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(3)
        }
    },
    OTHER_DAMAGE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(4)
        }
    },
    DAMAGE_ZONE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(6)
        }
    },
    POISON_FIELD {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(8)
        }
    },
    TRANSFERRED_DAMAGE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(9)
        }
    },
    REFLECTED_DAMAGE {
        override fun write(packetWriter: PacketWriter) {
            packetWriter.writeH(14)
        }
    };

    abstract fun write(packetWriter: PacketWriter)
}