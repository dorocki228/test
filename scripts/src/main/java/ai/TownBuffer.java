package ai;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;

/**
 * @author KRonst
 */
public class TownBuffer extends DefaultAI {

    private static final int BUFF_ID = 58115;
    private static final int BLOCK_BUFF_ID = 6088;
    private static final long BUFF_DELAY = 2000L;
    private long lastBuff = 0;

    public TownBuffer(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();

        if (lastBuff + BUFF_DELAY < System.currentTimeMillis()) {
            lastBuff = System.currentTimeMillis();
            World.getAroundPlayers(actor, 300, 300)
                .stream()
                .filter(p -> !p.getAbnormalList().containsEffects(BUFF_ID) && !p.getAbnormalList().containsEffects(BLOCK_BUFF_ID))
                .findFirst()
                .ifPresent(p -> {
                    final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(BUFF_ID, 1);
                    if (skillEntry != null) {
                        actor.stopMove();
                        actor.doCast(skillEntry, p, false);
                    }
                });
        }

        return super.thinkActive();
    }
}
