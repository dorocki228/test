package l2s.gameserver.model.base

import l2s.gameserver.model.Creature

enum class DamageByAttackType {
    NONE {
        override fun check(creature: Creature): Boolean {
            return false
        }
    },
    // Players and summons.
    PK {
        override fun check(creature: Creature): Boolean {
            return creature.isPlayable
        }
    },
    // Regular monsters.
    MOB {
        override fun check(creature: Creature): Boolean {
            return creature.isMonster
        }
    },
    // Boss monsters
    BOSS {
        override fun check(creature: Creature): Boolean {
            return creature.isBoss
        }
    },
    // All
    ENEMY_ALL {
        override fun check(creature: Creature): Boolean {
            return true
        }
    };

    abstract fun check(creature: Creature): Boolean
}