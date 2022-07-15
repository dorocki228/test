package l2s.gameserver.network.l2.s2c

import l2s.gameserver.model.Creature
import l2s.gameserver.model.LostItems
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.RestartType
import l2s.gameserver.model.instances.MonsterInstance
import l2s.gameserver.model.pledge.Clan
import l2s.gameserver.network.l2.OutgoingPackets
import java.util.*

class DiePacket(cha: Creature, private val hideDieAnimation: Boolean, private val lostItems: LostItems) :
    IClientOutgoingPacket {

    private val objectId: Int = cha.objectId
    private var sweepable = false
    private var delayToUseRebirthItem = 0
    private var availableCountRebirthItem = 0

    private val restartTypes = EnumMap<RestartType, Boolean>(RestartType::class.java)
        .withDefault { false }

    init {
        if (cha.isMonster)
            sweepable = (cha as MonsterInstance).isSweepActive
        else if (cha.isPlayer) {
            val player = cha as Player

            restartTypes[RestartType.VILLAGE] = true

            var clan: Clan? = null
            if (restartTypes.getValue(RestartType.VILLAGE))
                clan = player.clan
            if (clan != null) {
                if (clan.hasHideout != 0) {
                    restartTypes[RestartType.AGIT] = true
                }

                if (clan.castle != 0) {
                    restartTypes[RestartType.CASTLE] = true
                }

                if (false) {
                    restartTypes[RestartType.FORTRESS] = true
                }
            }
            if (player.canFixedRessurect()) {
                restartTypes[RestartType.ORIGINAL] = true
            }

            if (player.isAgathionResAvailable) {
                restartTypes[RestartType.ORIGINAL] = true
            }

            if (player.abnormalList.contains(22410) || player.abnormalList.contains(22411)) {
                restartTypes[RestartType.ADVENTURERS_SONG] = true
            }

            for (e in cha.getEvents()) {
                e.checkRestartLocs(player, restartTypes)
            }

            for (effect in player.abnormalList) {
                if (effect.skill.id == 7008) {
                    delayToUseRebirthItem = effect.timeLeft
                    // TODO count items
                    availableCountRebirthItem = 1
                    break
                }
            }
        }
    }

    @JvmOverloads
    constructor(cha: Creature, lostItems: LostItems = LostItems.EMPTY) : this(cha, false, lostItems)

    override fun write(packetWriter: l2s.commons.network.PacketWriter): Boolean {
        OutgoingPackets.DIE.writeId(packetWriter)

        packetWriter.writeD(objectId)

        packetWriter.writeD(restartTypes.getValue(RestartType.VILLAGE)) // to nearest village
        packetWriter.writeD(restartTypes.getValue(RestartType.AGIT)) // to hide away
        packetWriter.writeD(restartTypes.getValue(RestartType.CASTLE)) // to castle
        packetWriter.writeD(restartTypes.getValue(RestartType.BATTLE_CAMP))// to siege HQ

        packetWriter.writeD(if (sweepable) 0x01 else 0x00) // sweepable glow effect

        packetWriter.writeD(restartTypes.getValue(RestartType.ORIGINAL))// ORIGINAL
        packetWriter.writeD(restartTypes.getValue(RestartType.FORTRESS))// fortress

        packetWriter.writeD(delayToUseRebirthItem) // Disables use Feather button for X seconds
        packetWriter.writeD(restartTypes.getValue(RestartType.ADVENTURERS_SONG)) // adventurers song

        packetWriter.writeC(if (hideDieAnimation) 0x01 else 0x00) // hide die animation

        packetWriter.writeD(0x00) // TODO PenaltyTime
        packetWriter.writeD(0x00) // Arena
        packetWriter.writeD(0x00) // CostItemClassID (only for arena)
        packetWriter.writeD(0x00) // CostItemAmount (only for arena)

        // TODO
        //for (d) {
        //	d L"Npc"
        //}

        return true
    }
}