package ai.kamaloka;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.kamaloka.LostCaptainInstance;


/**
 * @author IOException
 */
public class LabyrinthLostWatcher extends Fighter {
    private static final int buff_id = 5699;
    private static final int buff_level = 1;

    public LabyrinthLostWatcher(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();
        Reflection r = actor.getReflection();
        if(!r.isDefault()) {
            if(checkMates()) {
                NpcInstance captain;
                if((captain = findLostCaptain()) != null) {
                    SkillHolder.getInstance().getSkillEntry(buff_id, buff_level).getEffects(captain, captain);
                }
            }
        }
        super.onEvtDead(killer);
    }

    private boolean checkMates() {
        for (NpcInstance n : getActor().getReflection().getNpcs())
            if((n.getAI() instanceof LabyrinthLostWatcher) && !n.isDead())
                return false;
        return true;
    }

    private NpcInstance findLostCaptain() {
        for (NpcInstance n : getActor().getReflection().getNpcs())
            if(n instanceof LostCaptainInstance)
                return n;
        return null;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}