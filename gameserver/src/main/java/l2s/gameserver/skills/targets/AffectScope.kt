package l2s.gameserver.skills.targets

import l2s.gameserver.geodata.GeoEngine
import l2s.gameserver.model.Creature
import l2s.gameserver.network.l2.s2c.ExShowTrace
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.utils.PositionUtils
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class AffectScope {
    /** Affects Valakas.  */
    BALAKAS_SCOPE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            TODO("not implemented")
        }
    },
    /** Affects dead party members.  */
    DEAD_PARTY {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            if (!target.isPlayable) {
                return emptyList()
            }

            val player = target.player
            val party = player.party

            val skill = skillEntry.template
            val affectRange: Int = skill.affectRange
            val affectLimit: Int = skill.affectLimit

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in target.getAroundPlayables(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                val p = c.player
                if (p == null || !p.isDead) {
                    continue
                }

                if (p != player) {
                    val targetParty = p.party
                    if (party == null || targetParty == null || party.groupLeader != targetParty.groupLeader) {
                        continue
                    }
                }

                if (!skill.affectObject.checkObject(caster, p, skill, forceUse)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                // continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects dead party and clan members.  */
    DEAD_PARTY_PLEDGE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            TODO("not implemented")
        }
    },
    /** Affects dead clan mates.  */
    DEAD_PLEDGE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            if (!target.isPlayable) {
                return emptyList()
            }

            val player = target.player

            val skill = skillEntry.template
            val affectRange: Int = skill.affectRange
            val affectLimit: Int = skill.affectLimit

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in target.getAroundPlayables(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                val p = c.player
                if (p == null || !p.isDead) {
                    continue
                }

                if (p != player && (p.clanId == 0 || p.clanId != player.clanId)) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, p, skill, forceUse)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                // continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects dead union (Command Channel?) members.  */
    DEAD_UNION {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            TODO("not implemented")
        }
    },
    /**
     * Fan affect scope implementation. Gathers objects in a certain angle of circular area around yourself (including origin itself).
     */
    FAN {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val headingAngle = PositionUtils.convertHeadingToDegree(caster.heading)
            val fanStartAngle: Int = skill.fanRange[1]
            val fanRadius: Int = skill.fanRange[2]
            val fanAngle: Int = skill.fanRange[3]
            val fanHalfAngle = fanAngle / 2.0 // Half left and half right.
            val affectLimit: Int = skill.affectLimit

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in caster.getAroundCharacters(fanRadius, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c == target) {
                    continue
                }

                if (c.isDead) {
                    continue
                }

                if (abs(caster.calculateAngleTo(c) - (headingAngle + fanStartAngle)) > fanHalfAngle) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                if (!GeoEngine.canSeeTarget(caster, c)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                //continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /**
     * Fan point blank affect scope implementation. Gathers objects in a certain angle of circular area around yourself without taking target into account.
     */
    FAN_PB {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val headingAngle = PositionUtils.convertHeadingToDegree(caster.heading)
            val fanStartAngle: Int = skill.fanRange[1]
            val fanRadius: Int = skill.fanRange[2]
            val fanAngle: Int = skill.fanRange[3]
            val fanHalfAngle = fanAngle / 2.0 // Half left and half right.
            val affectLimit: Int = skill.affectLimit

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)

            for (c in caster.getAroundCharacters(fanRadius, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                if (abs(caster.calculateAngleTo(c) - (headingAngle + fanStartAngle)) > fanHalfAngle) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                if (!GeoEngine.canSeeTarget(caster, c)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                //continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects nothing.  */
    NONE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            return emptyList()
        }
    },
    /** Affects party members.  */
    PARTY {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val affectRange: Int = skill.affectRange
            val affectLimit: Int = skill.affectLimit

            if (target.isPlayable) {
                val player = target.player
                val party = player.party

                // Target checks.
                var affectedCount = 0

                val affected = ArrayList<Creature>(8)
                // Always accept main target.
                affected.add(target)

                for (c in target.getAroundPlayables(affectRange, 300)) {
                    if (affectedCount >= affectLimit) {
                        break
                    }

                    if (c.isDead) {
                        continue
                    }

                    val p = c.player ?: continue

                    if (p != player) {
                        val targetParty = p.party
                        if (party == null || targetParty == null || party.groupLeader != targetParty.groupLeader) {
                            continue
                        }
                    }

                    if (!skill.affectObject.checkObject(caster, p, skill, forceUse)) {
                        continue
                    }

                    //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                    // continue;

                    affected.add(c)

                    val casterPlayer = caster.player
                    if (casterPlayer != null && casterPlayer.isDebug) {
                        casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                    }

                    affectedCount++
                }

                return affected
            } else if (target.isNpc) {
                val npc = target.asNpc()

                // Target checks.
                var affectedCount = 0

                val affected = ArrayList<Creature>(8)
                // Always accept main target.
                affected.add(target)

                for (n in npc.getAroundNpc(affectRange, 300)) {
                    if (affectedCount >= affectLimit) {
                        break
                    }

                    if (n == caster) {
                        continue
                    }

                    if (n.isDead) {
                        continue
                    }

                    if (n.isAutoAttackable(npc)) {
                        continue
                    }

                    if (!skill.affectObject.checkObject(caster, n, skill, forceUse)) {
                        continue
                    }

                    //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                    // continue;

                    affected.add(n)

                    val casterPlayer = caster.player
                    if (casterPlayer != null && casterPlayer.isDebug) {
                        casterPlayer.sendPacket(ExShowTrace(30000).addTrace(n))
                    }

                    affectedCount++
                }

                return affected
            }

            return emptyList()
        }
    },
    /** Affects party and clan mates.  */
    PARTY_PLEDGE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            if (!target.isPlayable) {
                return emptyList()
            }

            val skill = skillEntry.template
            val affectRange: Int = skill.affectRange
            val affectLimit: Int = skill.affectLimit

            val player = target.player
            val party = player.party

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in target.getAroundPlayables(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                val p = c.player ?: continue

                if (p != player) {
                    val targetParty = p.party

                    val clanCheck = player.clanId != 0 && player.clanId == p.clanId
                    val partyCheck = party != null && targetParty != null && party.groupLeader == targetParty.groupLeader
                    if (!clanCheck && !partyCheck) {
                        continue
                    }
                }

                if (!skill.affectObject.checkObject(caster, p, skill, forceUse)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                // continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects clan mates.  */
    PLEDGE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            if (!target.isPlayable) {
                return emptyList()
            }

            val skill = skillEntry.template
            val affectRange: Int = skill.affectRange
            val affectLimit: Int = skill.affectLimit

            val player = target.player

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in target.getAroundPlayables(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                val p = c.player ?: continue

                if (p != player) {
                    if (p.clanId == 0) {
                        continue
                    }
                    if (p.clanId != player.clanId) {
                        continue
                    }
                }

                if (!skill.affectObject.checkObject(caster, p, skill, forceUse)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                // continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Point Blank affect scope implementation. Gathers targets in specific radius except initial target.  */
    POINT_BLANK {
        override fun getAffected(caster: Creature, target: Creature, skillEntry: SkillEntry, forceUse: Boolean, dontMove: Boolean, sendMessage: Boolean): List<Creature> {
            val skill = skillEntry.template
            val affectRange = skill.affectRange
            val affectLimit = skill.affectLimit

            if (skill.targetTypeNew == TargetType.GROUND) {
                if (!caster.isPlayable) {
                    return emptyList()
                }

                val groundSkillLoc = caster.player.groundSkillLoc
                        ?: return emptyList()

                // Target checks.
                var affectedCount = 0

                val affected = ArrayList<Creature>(8)

                val newRange = (affectRange + caster.distance2d(groundSkillLoc)).toInt()
                for (c in caster.getAroundCharacters(newRange, 300)) {
                    if (!c.isInRadius3d(groundSkillLoc, affectRange.toDouble())) {
                        continue
                    }

                    if (affectedCount >= affectLimit) {
                        break
                    }

                    // XXX : Find a proper way to fix, if it's not proper.
                    if (c.isDead && skill.affectObject != AffectObject.OBJECT_DEAD_NPC_BODY) {
                        continue
                    }

                    if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                        continue
                    }

                    if (!GeoEngine.canSeeTarget(target, c)) {
                        continue
                    }

                    //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                    // continue;

                    affected.add(c)

                    val casterPlayer = caster.player
                    if (casterPlayer != null && casterPlayer.isDebug) {
                        casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                    }

                    affectedCount++
                }

                return affected
            } else {
                // Target checks.
                var affectedCount = 0

                val affected = ArrayList<Creature>(8)

                for (c in target.getAroundCharacters(affectRange, 300)) {
                    if (affectedCount >= affectLimit) {
                        break
                    }

                    // XXX : Find a proper way to fix, if it's not proper.
                    if (c.isDead && skill.affectObject != AffectObject.OBJECT_DEAD_NPC_BODY) {
                        continue
                    }

                    if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                        continue
                    }

                    if (!GeoEngine.canSeeTarget(target, c)) {
                        continue
                    }

                    //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                    // continue;

                    affected.add(c)

                    val casterPlayer = caster.player
                    if (casterPlayer != null && casterPlayer.isDebug) {
                        casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                    }

                    affectedCount++
                }

                return affected
            }

            return emptyList()
        }
    },
    /**
     * Range affect scope implementation. Gathers objects in area of target origin (including origin itself).
     */
    RANGE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val affectRange = skill.affectRange
            val affectLimit = skill.affectLimit

            if (skill.targetTypeNew == TargetType.GROUND) {
                if (!caster.isPlayable) {
                    return emptyList()
                }

                val groundSkillLoc = caster.player.groundSkillLoc
                        ?: return emptyList()

                // Target checks.
                var affectedCount = 0

                val affected = ArrayList<Creature>(8)

                val newRange = (affectRange + caster.distance2d(groundSkillLoc)).toInt()
                for (c in caster.getAroundCharacters(newRange, 300)) {
                    if (!c.isInRadius3d(groundSkillLoc, affectRange.toDouble())) {
                        continue
                    }

                    if (affectedCount >= affectLimit) {
                        break
                    }

                    // XXX : Find a proper way to fix, if it's not proper.
                    if (c.isDead && skill.affectObject != AffectObject.OBJECT_DEAD_NPC_BODY) {
                        continue
                    }

                    if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                        continue
                    }

                    if (!GeoEngine.canSeeTarget(target, c)) {
                        continue
                    }

                    //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                    // continue;

                    affected.add(c)

                    val casterPlayer = caster.player
                    if (casterPlayer != null && casterPlayer.isDebug) {
                        casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                    }

                    affectedCount++
                }

                return affected
            } else {
                // Target checks.
                var affectedCount = 0

                val affected = ArrayList<Creature>(8)
                // Always accept main target.
                affected.add(target)

                for (c in target.getAroundCharacters(affectRange, 300)) {
                    if (affectedCount >= affectLimit) {
                        break
                    }

                    // XXX : Find a proper way to fix, if it's not proper.
                    if (c.isDead && skill.affectObject != AffectObject.OBJECT_DEAD_NPC_BODY) {
                        continue
                    }

                    if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                        continue
                    }

                    if (!GeoEngine.canSeeTarget(target, c)) {
                        continue
                    }

                    //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                    // continue;

                    affected.add(c)

                    val casterPlayer = caster.player
                    if (casterPlayer != null && casterPlayer.isDebug) {
                        casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                    }

                    affectedCount++
                }

                return affected
            }
        }
    },
    /** Affects ranged targets, using selected target as point of origin sorted by lowest to highest HP.  */
    RANGE_SORT_BY_HP {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val affectRange: Int = skill.affectRange
            val affectLimit: Int = skill.affectLimit

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)

            for (c in target.getAroundCharacters(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                // continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            // Always accept main target.
            affected.add(target)

            // Sort from lowest hp to highest hp.
            val sorted = affected
                    .sortedBy { it.currentHpPercents }
            return if (affectLimit > 0 && affectLimit < Int.MAX_VALUE) {
                sorted.take(affectLimit)
            } else {
                sorted
            }
        }
    },
    /**
     * Ring Range affect scope implementation. Gathers objects in ring/donut shaped area with start and end range.
     */
    RING_RANGE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val affectRange = skill.affectRange
            val affectLimit = skill.affectLimit
            val startRange = skill.fanRange[2]

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)

            for (c in target.getAroundCharacters(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                // Targets before the start range are unaffected.
                if (c.isInRadius2d(target, startRange.toDouble())) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                if (!GeoEngine.canSeeTarget(caster, c)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                //continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects a single target.  */
    SINGLE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            if (target.isCreature) {
                val skill = skillEntry.template
                if (skill.affectObject.checkObject(caster, target.asCreature(), skill, forceUse)) {
                    // Return yourself to mark that effects can use your current skill's world position.
                    val affected = ArrayList<Creature>(1)
                    affected.add(target)
                    return affected
                }
            } else if (target.isItem) {
                // Return yourself to mark that effects can use your current skill's world position.
                val affected = ArrayList<Creature>(1)
                affected.add(target)
                return affected
            }

            return emptyList()
        }
    },
    /**
     * Square affect scope implementation (actually more like a rectangle).
     */
    SQUARE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val squareStartAngle = skill.fanRange[1]
            val squareLength = skill.fanRange[2]
            val squareWidth = skill.fanRange[3]
            val radius = sqrt(squareLength * squareLength + (squareWidth * squareWidth).toDouble()).toInt()
            val affectLimit = skill.affectLimit

            val x = caster.x.toDouble()
            val y = caster.y.toDouble()

            val rectX: Double = x
            val rectY: Double = y - squareWidth / 2.0
            val heading = Math.toRadians(squareStartAngle + PositionUtils.convertHeadingToDegree(caster.heading))
            val cos = cos(-heading)
            val sin = sin(-heading)

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in caster.getAroundCharacters(radius * 2, 300)) {
                if (c == target) {
                    continue
                }

                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                // Check if inside square.
                val xp: Double = c.x - x
                val yp: Double = c.y - y
                val xr: Double = x + xp * cos - yp * sin
                val yr: Double = y + xp * sin + yp * cos
                if (xr <= rectX || xr >= rectX + squareLength || yr <= rectY || yr >= rectY + squareWidth) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                if (!GeoEngine.canSeeTarget(caster, c)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                //continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /**
     * Square point blank affect scope implementation (actually more like a rectangle).
     * Gathers objects around yourself except target itself.
     */
    SQUARE_PB {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val squareStartAngle = skill.fanRange[1]
            val squareLength = skill.fanRange[2]
            val squareWidth = skill.fanRange[3]
            val radius = sqrt(squareLength * squareLength + (squareWidth * squareWidth).toDouble()).toInt()
            val affectLimit = skill.affectLimit

            val x = caster.x.toDouble()
            val y = caster.y.toDouble()

            val rectX: Double = x
            val rectY: Double = y - squareWidth / 2.0
            val heading = Math.toRadians(squareStartAngle + PositionUtils.convertHeadingToDegree(caster.heading))
            val cos = cos(-heading)
            val sin = sin(-heading)

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)

            for (c in caster.getAroundCharacters(radius /** 2*/, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                // Check if inside square.
                val xp: Double = c.x - x
                val yp: Double = c.y - y
                val xr: Double = x + xp * cos - yp * sin
                val yr: Double = y + xp * sin + yp * cos
                if (xr <= rectX || xr >= rectX + squareLength || yr <= rectY || yr >= rectY + squareWidth) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                if (!GeoEngine.canSeeTarget(caster, c)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                //continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects static object targets.  */
    STATIC_OBJECT_SCOPE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            val skill = skillEntry.template
            val affectRange = skill.affectRange
            val affectLimit = skill.affectLimit

            // Target checks.
            var affectedCount = 0

            val affected = ArrayList<Creature>(8)
            // Always accept main target.
            affected.add(target)

            for (c in caster.getAroundCharacters(affectRange, 300)) {
                if (affectedCount >= affectLimit) {
                    break
                }

                if (c.isDead) {
                    continue
                }

                if (!c.isDoor && !c.isStaticObject) {
                    continue
                }

                if (!skill.affectObject.checkObject(caster, c, skill, forceUse)) {
                    continue
                }

                if (!GeoEngine.canSeeTarget(caster, c)) {
                    continue
                }

                //if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && c.isNpc())
                //continue;

                affected.add(c)

                val casterPlayer = caster.player
                if (casterPlayer != null && casterPlayer.isDebug) {
                    casterPlayer.sendPacket(ExShowTrace(30000).addTrace(c))
                }

                affectedCount++
            }

            return affected
        }
    },
    /** Affects all summons except master.  */
    SUMMON_EXCEPT_MASTER {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            if (!target.isPlayable) {
                return emptyList()
            }

            val skill = skillEntry.template
            val affectRange = skill.affectRange
            val affectLimit = skill.affectLimit

            val player = target.player
            val affected = player.servitors
                    .filter {
                        if (it.isDead) {
                            return@filter false
                        }
                        if (affectRange > 0) {
                            if (!PositionUtils.checkIfInRange(affectRange, it, target, true)) {
                                return@filter false
                            }
                        }
                        if (!skill.affectObject.checkObject(caster, it, skill, forceUse)) {
                            return@filter false
                        }

                        return@filter true
                    }

            return if (affectLimit > 0 && affectLimit < Int.MAX_VALUE) {
                affected.take(affectLimit)
            } else {
                affected
            }
        }
    },
    /** Affects wyverns.  */
    WYVERN_SCOPE {
        override fun getAffected(
                caster: Creature,
                target: Creature,
                skillEntry: SkillEntry,
                forceUse: Boolean,
                dontMove: Boolean,
                sendMessage: Boolean
        ): List<Creature> {
            TODO("not implemented")
        }
    };

    abstract fun getAffected(
            caster: Creature,
            target: Creature,
            skillEntry: SkillEntry,
            forceUse: Boolean,
            dontMove: Boolean,
            sendMessage: Boolean
    ): List<Creature>

}