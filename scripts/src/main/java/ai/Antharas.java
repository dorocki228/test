package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author KRonst
 */
public class Antharas extends DefaultAI {

    // debuffs
    private final Skill
        fear1 = getSkill(4108, 1),
        fear2 = getSkill(5092, 1),
        curse = getSkill(4109, 1),
        paralyze = getSkill(4111, 1);

    // damage skills
    private final Skill
        shock1 = getSkill(4106, 1),
        shock2 = getSkill(4107, 1),
        ordinaryAttack = getSkill(4112, 1),
        ordinaryAttack2 = getSkill(4113, 1),
        meteor = getSkill(5093, 1),
        breath = getSkill(4110, 1);

    // regen skills
    private final Skill
        regen1 = getSkill(4239, 1),
        regen2 = getSkill(4240, 1),
        regen3 = getSkill(4241, 1);

    // Vars
    private final List<NpcInstance> minions = new ArrayList<>();
    private long minionsSpawnDelay = 0;
    private int hpStage = 0;
    private int damageCounter = 0;

    public Antharas(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, Skill skill, int damage) {
        NpcInstance actor = getActor();
        if (damageCounter == 0) {
            actor.getAI().startAITask();
        }
        damageCounter++;
        super.onEvtAttacked(attacker, skill, damage);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        minionsSpawnDelay = System.currentTimeMillis() + 120000L;
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
            actor.altOnMagicUse(actor, regen1);
            hpStage = 1;
        } else if (chp < 75 && hpStage == 1) {
            actor.altOnMagicUse(actor, regen2);
            hpStage = 2;
        } else if (chp < 50 && hpStage == 2) {
            actor.altOnMagicUse(actor, regen3);
            hpStage = 3;
        } else if (chp < 30 && hpStage == 3) {
            actor.altOnMagicUse(actor, regen3);
            hpStage = 4;
        }

        // Minions spawn
        if (minionsSpawnDelay < System.currentTimeMillis() && getAliveMinionsCount() < 30 && Rnd.chance(5)) {
            NpcInstance minion = NpcUtils.spawnSingle(Rnd.chance(50) ? 29190 : 29069,
                Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()));
            minions.add(minion);
        }

        // Basic Attack
        if (Rnd.chance(50)) {
            return chooseTaskAndTargets(Rnd.chance(50) ? ordinaryAttack : ordinaryAttack2, target, distance);
        }

        // Stage based skill attacks
        Map<Skill, Integer> d_skill = new HashMap<>();
        switch (hpStage) {
            case 1:
                addDesiredSkill(d_skill, target, distance, curse);
                addDesiredSkill(d_skill, target, distance, paralyze);
                addDesiredSkill(d_skill, target, distance, meteor);
                break;
            case 2:
                addDesiredSkill(d_skill, target, distance, curse);
                addDesiredSkill(d_skill, target, distance, paralyze);
                addDesiredSkill(d_skill, target, distance, meteor);
                addDesiredSkill(d_skill, target, distance, fear2);
                break;
            case 3:
                addDesiredSkill(d_skill, target, distance, curse);
                addDesiredSkill(d_skill, target, distance, paralyze);
                addDesiredSkill(d_skill, target, distance, meteor);
                addDesiredSkill(d_skill, target, distance, fear2);
                addDesiredSkill(d_skill, target, distance, shock2);
                addDesiredSkill(d_skill, target, distance, breath);
                break;
            case 4:
                addDesiredSkill(d_skill, target, distance, curse);
                addDesiredSkill(d_skill, target, distance, paralyze);
                addDesiredSkill(d_skill, target, distance, meteor);
                addDesiredSkill(d_skill, target, distance, fear2);
                addDesiredSkill(d_skill, target, distance, shock2);
                addDesiredSkill(d_skill, target, distance, fear1);
                addDesiredSkill(d_skill, target, distance, shock1);
                addDesiredSkill(d_skill, target, distance, breath);
                break;
            default:
                break;
        }

        Skill r_skill = selectTopSkill(d_skill);
        if (r_skill != null && !r_skill.isOffensive()) {
            target = actor;
        }

        return chooseTaskAndTargets(r_skill, target, distance);
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

    private Skill getSkill(int id, int level) {
        return SkillHolder.getInstance().getSkill(id, level);
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
}
