package ai.custom;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.LostItems;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.utils.NpcUtils;

import java.util.List;

/**
 * @author IOException
 */
public class LabyrinthLostWatcher extends Fighter<NpcInstance>
{
    private static final int buff_id = 5699;
    private int buff_level = 1;

    public LabyrinthLostWatcher(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected void onEvtSpawn() {
        NpcInstance actor = getActor();
        Reflection r = actor.getReflection();
        if(!r.isDefault())
            initReal(actor, r);
        super.onEvtSpawn();
    }

    private synchronized void initReal(NpcInstance actor, Reflection r) {
        int count = r.getNpcs(true, actor.getNpcId()).size();
        if(count != 9 || r.getVariable("spawned", false) || realIsAlive())
            return;

        buff_level = r.getInstancedZoneId() - 72;
        if(buff_level > 7)
            buff_level = 7;
        else if(buff_level < 1)
            buff_level = 1;

        int mob_id = Rnd.get(9);
        List<NpcInstance> list = r.getNpcs(true, actor.getNpcId());
        for(int i = 0; i < list.size(); i++) {
            NpcInstance npc = list.get(i);
            if(i == mob_id) {
                npc.setParameter("isReal", true);
                npc.setParameter("silhouette", false);
            } else {
                npc.setParameter("silhouette", true);
                npc.setParameter("isReal", false);
            }
        }

        r.setVariable("spawned", true);
    }

    @Override
    protected void onEvtDead(Creature killer, LostItems lostItems)
    {
        NpcInstance actor = getActor();
        Reflection r = actor.getReflection();
        if(!r.isDefault()) {
            if(actor.getParameter("isReal", false)) {
                NpcInstance captain;
                if((captain = findLostCaptain()) != null) {
                    SkillHolder.getInstance().getSkill(buff_id, buff_level).getEffects(captain, captain);
                }
            }
        }
        super.onEvtDead(killer, lostItems);


        if(actor.getParameter("silhouette", false) && realIsAlive()) {
            ThreadPoolManager.getInstance().schedule(new RespawnTask(actor.getNpcId(), actor.getSpawnedLoc(), actor.getReflection()), 15000);
        }
    }

    private synchronized boolean realIsAlive()
    {
        NpcInstance actor = getActor();
        return actor.getReflection().getNpcs(true, actor.getNpcId()).stream().anyMatch(npc -> npc.getParameter("isReal", false));
    }

    private NpcInstance findLostCaptain()
    {
        return getActor().getReflection().getNpcs().stream()
                .filter(n -> n instanceof ReflectionBossInstance).findFirst()
                .orElse(null);
    }

    private class RespawnTask implements Runnable {

        private int npcId;
        private Location loc;
        private Reflection ref;

        private RespawnTask(int npcId, Location loc, Reflection ref) {
            this.npcId = npcId;
            this.loc = loc;
            this.ref = ref;
        }

        @Override
        public void run() {
            if(!realIsAlive())
                return;

            NpcInstance npc = NpcUtils.spawnSingle(npcId, loc, ref);

            npc.setParameter("silhouette", true);
            npc.setParameter("isReal", false);
        }
    }

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}