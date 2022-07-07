package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.Config;
import l2s.gameserver.entity.ArtifactEntity;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.ArtifactInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.service.ArtifactService;
import l2s.gameserver.templates.StatsSet;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Artifact extends Skill {
    public Artifact(StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first) {
        if(activeChar == null || !activeChar.isPlayer())
            return false;
        if(!(target instanceof ArtifactInstance))
            return false;
        if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
            return false;
        ArtifactInstance artifact = ArtifactInstance.class.cast(target);
        if(artifact.getFraction() == activeChar.getFraction())
            return false;
        final Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
        if(!(parameter instanceof ArtifactEntity))
            return false;
        ArtifactEntity entity = (ArtifactEntity) parameter;
        final long currentTime = System.currentTimeMillis();
        if(entity.getEndProtect() > currentTime) {
            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(entity.getEndProtect() - currentTime);
            activeChar.sendMessage(new CustomMessage("artifact.s3").addNumber(minutes));
            return false;
        }
        if(ArtifactService.getInstance().getArtifactSizeFromFaction(activeChar.getFraction()) >= Config.MAX_ARTIFACTS_FOR_FACTION) {
            activeChar.sendMessage(new CustomMessage("artifact.s4"));
            return false;
        }
        return true;
    }

    @Override
    public void onFinishCast(Creature activeChar, Creature castingTarget, List<Creature> targets) {
        super.onFinishCast(activeChar, castingTarget, targets);

        if(!activeChar.isPlayer())
            return;
        if(targets.isEmpty())
            return;
        Creature target = targets.get(0);
        if(!(target instanceof ArtifactInstance))
            return;
        final ArtifactInstance artifact = ArtifactInstance.class.cast(target);
        final Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
        if(!(parameter instanceof ArtifactEntity))
            return;
        ArtifactEntity entity = (ArtifactEntity) parameter;
        World.getAroundPlayers(activeChar, getCastRange() * 2, getCastRange()).forEach(t -> {
            if(t.getCastingSkill() == this)
                t.abortCast(true, true);
        });
        entity.changeFaction(activeChar.getPlayer());
    }

    @Override
    protected void onStartCast(Creature activeChar, Creature castingTarget, List<Creature> targets) {
        if(!activeChar.isPlayer())
            return;
        if(targets.isEmpty())
            return;
        Creature target = targets.get(0);
        if(!(target instanceof ArtifactInstance))
            return;
        final ArtifactInstance artifact = ArtifactInstance.class.cast(target);
        final Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
        if(!(parameter instanceof ArtifactEntity))
            return;
        ArtifactEntity entity = (ArtifactEntity) parameter;
        entity.notifyCast(activeChar.getPlayer());
    }
}
