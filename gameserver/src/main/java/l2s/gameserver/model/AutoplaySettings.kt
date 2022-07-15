package l2s.gameserver.model

import l2s.commons.network.PacketWriter

data class AutoplaySettings(
    private val settingsSize: Int,
    private val enable: Short,
    private val pickup: Short,
    val nextTargetMode: NextTargetMode,
    val nearTargetMode: NearTargetMode,
    val useHpPercent: Int,
    val isMannerMode: Short
) {

    fun enabled(): Boolean {
        return enable == 1.toShort()
    }

    fun pickUpEnabled(): Boolean {
        return pickup == 1.toShort()
    }

    fun isInMannerMode(): Boolean {
        return isMannerMode == 1.toShort()
    }

    fun disabled(): AutoplaySettings {
        return copy(enable = 0x00)
    }

    fun write(packetWriter: PacketWriter) {
        packetWriter.writeH(settingsSize)
        packetWriter.writeC(enable.toInt())
        packetWriter.writeC(pickup.toInt())
        packetWriter.writeH(nextTargetMode.ordinal)
        packetWriter.writeC(nearTargetMode.ordinal)
    }

}

enum class NextTargetMode {
    TARGET_TO_TAUNT {
        override fun getTargets(player: Player, nearTargetMode: NearTargetMode, isMannerMode: Boolean): Sequence<Creature> {
            return World.getAroundCharacters(player, nearTargetMode.range, 300).asSequence()
                .filter { it.isAutoAttackable(player) && (!isMannerMode || !it.isInCombat)}
                .filter { !it.isDead && it.isVisible }
                .sortedBy { player.getRealDistance3D(it) }
                .take(50)
        }
    },
    MONSTER {
        override fun getTargets(player: Player, nearTargetMode: NearTargetMode, isMannerMode: Boolean): Sequence<Creature> {
            return World.getAroundNpc(player, nearTargetMode.range, 300).asSequence()
                .filter { it.isAutoAttackable(player) && (!isMannerMode || !it.isInCombat)}
                .filter { !it.isDead && it.isVisible }
                .sortedBy { player.getRealDistance3D(it) }
                .take(50)
        }
    },
    PC {
        override fun getTargets(player: Player, nearTargetMode: NearTargetMode, isMannerMode: Boolean): Sequence<Creature> {
            return World.getAroundPlayables(player, nearTargetMode.range, 300).asSequence()
                .filter { it.isAutoAttackable(player) }
                .filter { !it.isDead && it.isVisible }
                .sortedBy { player.getRealDistance3D(it) }
                .take(50)
        }
    },
    NPC {
        override fun getTargets(player: Player, nearTargetMode: NearTargetMode, isMannerMode: Boolean): Sequence<Creature> {
            return World.getAroundNpc(player, nearTargetMode.range, 300).asSequence()
                .filter { it.isPeaceNpc && player.isAutoAttackable(it) }
                .filter { !it.isDead && it.isVisible }
                .sortedBy { player.getRealDistance3D(it) }
                .take(50)
        }
    };

    abstract fun getTargets(player: Player, nearTargetMode: NearTargetMode, isMannerMode: Boolean): Sequence<Creature>

    companion object {

        val values = values()

        fun findByOrdinal(ordinal: Int): NextTargetMode? {
            return values.find { it.ordinal == ordinal }
        }

    }

}

enum class NearTargetMode(val range: Int) {
    LONG(1400),
    SHORT(600);

    companion object {

        val values = values()

        fun findByOrdinal(ordinal: Int): NearTargetMode? {
            return values.find { it.ordinal == ordinal }
        }

    }

}