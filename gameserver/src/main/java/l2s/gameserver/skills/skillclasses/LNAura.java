package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.taskmanager.EffectTaskManager;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Util;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Mangol
 */
public class LNAura extends Skill {
    private List<SkillEntry> skills;

    public LNAura(StatsSet set) {
        super(set);
    }

    @Override
    public void setup() {
        int[] skillIds = params.getIntegerArray("skillIds");
        int[] skillLevels = params.getIntegerArray("skillLevels");
        this.skills = convertIdsSkillToList(skillIds, skillLevels);
    }

    static List<SkillEntry> convertIdsSkillToList(int[] skillIds, int[] skillLevels) {
        return IntStream.range(0, skillIds.length).mapToObj(i -> {
            final int skillId = skillIds[i];
            final int level;
            if(skillLevels.length == 0)
                level = 1;
            else if(skillLevels.length == 1)
                level = skillLevels[0];
            else
                level = skillLevels[i];
            return SkillHolder.getInstance().getSkillEntry(skillId, level);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void addSkill(Player player) {
        if(skills.isEmpty())
            return;
        final ScheduledFuture<?> scheduledFuture = EffectTaskManager.getInstance().scheduleAtFixedRate(new BuffSkill(player), 5000, 10000);
        player.addTask(String.format("skill%s", getId()), scheduledFuture);
    }

    @Override
    public void removeSkill(Player player) {
        if(skills.isEmpty())
            return;
        player.removeTask(String.format("skill%s", getId()));
    }

    static void callSkill(final Creature actor, Creature target, SkillEntry skillEntry) {
        if(skillEntry == null)
            return;
        final Skill skill = skillEntry.getTemplate();
        final Creature aimTarget = skill.getAimingTarget(actor, target);
        if(skill.checkCondition(actor, aimTarget, false, true, true)) {
            List<Creature> targets = skill.getTargets(actor, aimTarget, false);
            skill.onEndCast(actor, targets);
            if (!skill.isNotBroadcastable()) {
                int[] objectIds = Util.objectToIntArray(targets);
                actor.broadcastPacket(new MagicSkillLaunchedPacket(actor.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), objectIds));
            }
        }
    }

    class BuffSkill implements Runnable {
        final Player player;
        //final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();
        //final OnChangeLocationListenerImpl listener;

        BuffSkill(Player player) {
            this.player = player;
            //this.listener = new OnChangeLocationListenerImpl(player, skills, getAffectRange());
        }

        @Override
        public void run() {
            if(player.isLogoutStarted())
                return;
/*            final List<Creature> list = World.getAroundCharacters(player, getAffectRange(), getAffectRange()).stream().filter(p -> p.getFraction() == player.getFraction()).filter(p-> p.isNpc() || p.isPlayable()).collect(Collectors.toList());
            list.forEach(p -> skills.forEach(i -> callSkill(player, p, i)));*/
            skills.forEach(i -> callSkill(player, player, i));
        }
    }
}
