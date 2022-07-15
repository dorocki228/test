package ai.locations.wallofargos.elementalbosses;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import java.util.Optional;

/*
 * @author Java-man
 */
public class PetramMinion extends Mystic<NpcInstance> {

    private static final SkillEntry HEAL_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.NONE, SkillHolder.getInstance().getSkill(4780, 1));

    public PetramMinion(NpcInstance actor) {
        super(actor);
        actor.setRandomWalk(false);
    }

    @Override
    protected boolean randomWalk()
    {
        return false;
    }

    @Override
    protected boolean hasRandomWalk()
    {
        return false;
    }

    @Override
    public boolean getIsMobile()
    {
        return false;
    }

    @Override
    protected void onEvtSpawn()
    {
        castHeal();

        super.onEvtSpawn();
    }

    private void castHeal() {
        if (getActor().isDead()) {
            return;
        }

        Optional<NpcInstance> petram = getActor().getReflection().getNpcs().stream()
                .filter(n -> n.getNpcId() == 29108)
                .findFirst();
        petram.ifPresent(leader -> {
            MagicSkillUse packet = new MagicSkillUse(getActor(), leader, HEAL_SKILL.getId(), HEAL_SKILL.getLevel(), 1000, 0L);
            getActor().broadcastPacket(packet);

            leader.setCurrentHp(leader.getCurrentHp() + 10000, false);

            ThreadPoolManager.getInstance().schedule(this::castHeal, 1000L);
        });
    }

}