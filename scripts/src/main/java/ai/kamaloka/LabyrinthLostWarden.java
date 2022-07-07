package ai.kamaloka;


import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.kamaloka.LostCaptainInstance;

/**
 * @author pchayka
 */
public class LabyrinthLostWarden extends Fighter {
    private static final int buff_id = 5701;
    private static final int buff_level = 1;

    public LabyrinthLostWarden(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();
        Reflection r = actor.getReflection();
        if(!r.isDefault())
            if(checkMates(actor.getNpcId())) {
                NpcInstance captain;
                if((captain = findLostCaptain()) != null) {
                    SkillHolder.getInstance().getSkillEntry(buff_id, buff_level).getEffects(captain, captain);
                }
            }
        super.onEvtDead(killer);
    }

    private boolean checkMates(int id) {
        for (NpcInstance n : getActor().getReflection().getNpcs())
            if(n.getNpcId() == id && !n.isDead())
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