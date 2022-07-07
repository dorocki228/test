package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author KRonst
 */
public class Valakas extends DefaultAI {

    // Self skills
    private final Skill
        lavaSkin = getSkill(4680, 1),
        fear = getSkill(4689, 1),
        defenceDown = getSkill(5864, 1),
        berserk = getSkill(5865, 1),
        regen = getSkill(4691, 1);

    // Offensive damage skills
    private final Skill
        trempleLeft = getSkill(4681, 1),
        trempleRight = getSkill(4682, 1),
        tailStompA = getSkill(4685, 1),
        tailLash = getSkill(4688, 1),
        meteor = getSkill(4690, 1),
        breathLow = getSkill(4683, 1),
        breathHigh = getSkill(4684, 1);

    // Offensive percentage skills
    private final Skill
        destroyBody = getSkill(5860, 1),
        destroySoul = getSkill(5861, 1),
        destroyBody2 = getSkill(5862, 1),
        destroySoul2 = getSkill(5863, 1);

    // Timer reuses
    private final long defenceDownReuse = 120000L;

    // Timers
    private long defenceDownTimer = Long.MAX_VALUE;

    // Vars
    private final List<NpcInstance> minions = new ArrayList<>();
    private final Zone zone;
    private double rangedAttacksIndex, counterAttackIndex, attacksIndex;
    private int hpStage = 0;


    public Valakas(NpcInstance actor) {
        super(actor);
        zone = ReflectionUtils.getZone("[valakas_p]");
    }

    @Override
    protected void onEvtAttacked(Creature attacker, Skill skill, int damage) {
        NpcInstance actor = getActor();
        if (damage > 100) {
            if (attacker.getDistance(actor) > 400) {
                rangedAttacksIndex += damage / 1000D;
            } else {
                counterAttackIndex += damage / 1000D;
            }
        }
        attacksIndex += damage / 1000D;
        super.onEvtAttacked(attacker, skill, damage);
    }

    @Override
    protected boolean createNewTask() {
        clearTasks();
        Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }

        NpcInstance actor = getActor();
        if (actor.isDead()) {
            return false;
        }

        double distance = actor.getDistance(target);

        // Buffs and stats
        double chp = actor.getCurrentHpPercents();
        if (hpStage == 0) {
            actor.altOnMagicUse(actor, getSkill(4691, 1));
            hpStage = 1;
        } else if (chp < 80 && hpStage == 1) {
            actor.altOnMagicUse(actor, getSkill(4691, 2));
            defenceDownTimer = System.currentTimeMillis();
            hpStage = 2;
        } else if (chp < 50 && hpStage == 2) {
            actor.altOnMagicUse(actor, getSkill(4691, 3));
            hpStage = 3;
        } else if (chp < 30 && hpStage == 3) {
            actor.altOnMagicUse(actor, getSkill(4691, 4));
            hpStage = 4;
        } else if (chp < 10 && hpStage == 4) {
            actor.altOnMagicUse(actor, getSkill(4691, 5));
            hpStage = 5;
        }

        // Minions spawn
        if (getAliveMinionsCount() < 100 && Rnd.chance(5)) {
            NpcInstance minion = NpcUtils
                .spawnSingle(29029, Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()));
            minions.add(minion);
        }

        // Tactical Movements
        if (counterAttackIndex > 2000) {

            broadcastScreenMessage(NpcString.VALAKAS_HEIGHTENED_BY_COUNTERATTACKS);
            counterAttackIndex = 0;
            return chooseTaskAndTargets(berserk, actor, 0);
        } else if (rangedAttacksIndex > 2000) {
            if (Rnd.chance(60)) {
                Creature randomHated = actor.getAggroList().getRandomHated(2000);
                if (randomHated != null) {
                    setAttackTarget(randomHated);
                    actor.startConfused();
                    ThreadPoolManager.getInstance().schedule(() -> {
                        NpcInstance npcInstance = getActor();
                        if (npcInstance != null) {
                            npcInstance.stopConfused();
                        }
                        _madnessTask = null;
                    }, 20000L);
                }
                broadcastScreenMessage(NpcString.VALAKAS_RANGED_ATTACKS_ENRAGED_TARGET_FREE);
                rangedAttacksIndex = 0;
            } else {
                broadcastScreenMessage(NpcString.VALAKAS_RANGED_ATTACKS_PROVOKED);
                rangedAttacksIndex = 0;
                return chooseTaskAndTargets(berserk, actor, 0);
            }
        } else if (attacksIndex > 3000) {
            broadcastScreenMessage(NpcString.VALAKAS_PDEF_ISM_DECREACED_SLICED_DASH);
            attacksIndex = 0;
            return chooseTaskAndTargets(defenceDown, actor, 0);
        } else if (defenceDownTimer < System.currentTimeMillis()) {
            broadcastScreenMessage(NpcString.VALAKAS_FINDS_YOU_ATTACKS_ANNOYING_SILENCE);
            defenceDownTimer = System.currentTimeMillis() + defenceDownReuse + Rnd.get(60) * 1000L;
            return chooseTaskAndTargets(fear, target, distance);
        }

        // Basic Attack
        if (Rnd.chance(50)) {
            return chooseTaskAndTargets(Rnd.chance(50) ? trempleLeft : trempleRight, target, distance);
        }

        // Stage based skill attacks
        Map<Skill, Integer> d_skill = new HashMap<>();
        switch (hpStage) {
            case 1:
                addDesiredSkill(d_skill, target, distance, breathLow);
                addDesiredSkill(d_skill, target, distance, tailStompA);
                addDesiredSkill(d_skill, target, distance, meteor);
                addDesiredSkill(d_skill, target, distance, fear);
                break;
            case 2:
            case 3:
                addDesiredSkill(d_skill, target, distance, breathLow);
                addDesiredSkill(d_skill, target, distance, tailStompA);
                addDesiredSkill(d_skill, target, distance, breathHigh);
                addDesiredSkill(d_skill, target, distance, tailLash);
                addDesiredSkill(d_skill, target, distance, destroyBody);
                addDesiredSkill(d_skill, target, distance, destroySoul);
                addDesiredSkill(d_skill, target, distance, meteor);
                addDesiredSkill(d_skill, target, distance, fear);
                break;
            case 4:
            case 5:
                addDesiredSkill(d_skill, target, distance, breathLow);
                addDesiredSkill(d_skill, target, distance, tailStompA);
                addDesiredSkill(d_skill, target, distance, breathHigh);
                addDesiredSkill(d_skill, target, distance, tailLash);
                addDesiredSkill(d_skill, target, distance, destroyBody);
                addDesiredSkill(d_skill, target, distance, destroySoul);
                addDesiredSkill(d_skill, target, distance, meteor);
                addDesiredSkill(d_skill, target, distance, fear);
                addDesiredSkill(d_skill, target, distance, Rnd.chance(60) ? destroySoul2 : destroyBody2);
                break;
        }

        Skill r_skill = selectTopSkill(d_skill);
        if (r_skill != null && !r_skill.isOffensive()) {
            target = actor;
        }

        return chooseTaskAndTargets(r_skill, target, distance);
    }

    @Override
    protected void thinkAttack() {
        NpcInstance actor = getActor();
        // Lava buff
        if (actor.isInZone(Zone.ZoneType.poison)) {
            if (actor.getAbnormalList() != null && !actor.getAbnormalList().containsEffects(lavaSkin)) {
                actor.altOnMagicUse(actor, lavaSkin);
            }
        }
        super.thinkAttack();
    }

    private Skill getSkill(int id, int level) {
        return SkillHolder.getInstance().getSkill(id, level);
    }

    private int getAliveMinionsCount() {
        int i = 0;
        for (NpcInstance n : minions) {
            if (n != null && !n.isDead()) {
                i++;
            }
        }
        return i;
    }

    @Override
    protected void onEvtDead(Creature killer) {
        if (minions != null && !minions.isEmpty()) {
            for (NpcInstance n : minions) {
                n.deleteMe();
            }
        }
        super.onEvtDead(killer);
    }

    private void broadcastScreenMessage(NpcString string) {
        ExShowScreenMessage sm = new ExShowScreenMessage(string, 8000, ScreenMessageAlign.TOP_CENTER, false);
        for (Player player : zone.getInsidePlayers()) {
            player.sendPacket(sm);
        }
    }
}
