package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.entity.ArtifactEntity;
import l2s.gameserver.model.*;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.events.impl.UpgradingEvent;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.model.entity.events.objects.SpawnExFortObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.ArtifactInstance;
import l2s.gameserver.model.instances.UpgradingArtifactInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.ArtifactService;
import l2s.gameserver.templates.StatsSet;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TakeFortress extends Skill
{
    public TakeFortress(StatsSet set)
    {
        super(set);
    }

    @Override
    public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
    {
        if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
            return false;

        if(activeChar == null || !activeChar.isPlayer())
            return false;

        if(target == null)
            return false;

        if(target instanceof ArtifactInstance)
        {
            ArtifactInstance artifact = (ArtifactInstance) target;
            if(artifact.getFraction() == activeChar.getFraction())
                return false;
            final Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
            if(!(parameter instanceof ArtifactEntity))
                return false;
            ArtifactEntity entity = (ArtifactEntity) parameter;
            final long currentTime = System.currentTimeMillis();
            if(entity.getEndProtect() > currentTime)
            {
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(entity.getEndProtect() - currentTime);
                activeChar.sendMessage(new CustomMessage("artifact.s3").addNumber(minutes));
                return false;
            }
            if(ArtifactService.getInstance().getArtifactSizeFromFaction(activeChar.getFraction()) >= Config.MAX_ARTIFACTS_FOR_FACTION)
            {
                activeChar.sendMessage(new CustomMessage("artifact.s4"));
                return false;
            }
            return true;
        }

        if (target instanceof UpgradingArtifactInstance) {
            return true;
        }

        if(!target.isArtefact())
        {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET);
            return false;
        }

        Player player = (Player) activeChar;

        if(player.isMounted())
        {
            activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
            return false;
        }

        if(getCastRange() > 0 && !player.isInRangeZ(target, target.isArtefact() ? 185 : getCastRange()))
        {
            player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
            return false;
        }

        if(target.getNpcId() == 35322)
        {
            FortressSiegeEvent siegeEvent = target.getEvent(FortressSiegeEvent.class);
            if(siegeEvent == null || !siegeEvent.isInProgress())
            {
                activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
                return false;
            }

            if(!siegeEvent.getResidence().getFraction().canAttack(player.getFraction()))
            {
                activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
                return false;
            }

            List<SpawnExFortObject> crystals = siegeEvent.getObjects("crystals");
            for(SpawnExFortObject c : crystals)
            {
                List<Spawner> spawns = c.getSpawns();
                for(Spawner spawn : spawns)
                    if(spawn.getCurrentCount() > 0)
                    {
                        activeChar.sendMessage(getName(player) + " cannot be used because one or more crystals are alive.");
                        return false;
                    }
            }
        }
        else
        {
            List<CastleSiegeEvent> targetEvents = target.getEvents(CastleSiegeEvent.class);
            if(targetEvents.isEmpty())
            {
                activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
                return false;
            }

            boolean success = false;
            for(CastleSiegeEvent event : targetEvents)
            {
                if(!event.isInProgress())
                    continue;

                Castle r = event.getResidence();
                if(r.getFraction() == player.getFraction())
                    continue;

                List<SiegeToggleNpcObject> crystals = event.getObjects("flame_towers");
                for(SiegeToggleNpcObject c : crystals)
                {
                    if(c.isAlive())
                    {
                        activeChar.sendMessage(getName(player) + " cannot be used because one or more crystals are alive.");
                        return false;
                    }
                }

                crystals = event.getObjects("control_towers");
                for(SiegeToggleNpcObject c : crystals)
                {
                    if(c.isAlive())
                    {
                        activeChar.sendMessage(getName(player) + " cannot be used because one or more crystals are alive.");
                        return false;
                    }
                }

                List<Creature> around = World.getAroundCharacters(target, 185 * 2, 100);
                for(Creature ch : around)
                {
                    if(ch.isCastingNow() && ch.getCastingSkill() == this)
                    {
                        first = false;
                        break;
                    }
                }

                if(first)
                {
                    event.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, "defenders");
                    Announcements.announceToAll("THE OPPOSING CLAN HAS STARTED TO ENGRAVE THE HOLY ARTIFACT_DEFENSE");
                }
                success = true;
            }

            if(!success)
            {
                activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
                return false;
            }
        }
        return true;
    }

    @Override
    public void onFinishCast(Creature activeChar, Creature castingTarget, List<Creature> targets)
    {
        super.onFinishCast(activeChar, castingTarget, targets);

        if(castingTarget == null)
            return;

        if(castingTarget instanceof ArtifactInstance)
        {
            final ArtifactInstance artifact = (ArtifactInstance) castingTarget;
            final Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
            if(!(parameter instanceof ArtifactEntity))
                return;
            ArtifactEntity entity = (ArtifactEntity) parameter;
            World.getAroundPlayers(activeChar, getCastRange() * 2, getCastRange()).forEach(t ->
            {
                if(t.getCastingSkill() == this)
                    t.abortCast(true, true);
            });
            entity.changeFaction(activeChar.getPlayer());
            return;
        }

        if (castingTarget instanceof UpgradingArtifactInstance) {
            World.getAroundPlayers(activeChar, getCastRange() * 2, getCastRange()).forEach(p -> {
                if (p.getCastingSkill() == this) {
                    p.abortCast(true, true);
                }
            });
            UpgradingEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1001);
            if (event != null) {
                event.setArtifactOwner(activeChar.getFraction());
            }
            return;
        }

        if(!castingTarget.isArtefact())
            return;

        if(castingTarget.getNpcId() == 35322)
        {
            FortressSiegeEvent siegeEvent = castingTarget.getEvent(FortressSiegeEvent.class);
            if(siegeEvent == null || !siegeEvent.isInProgress())
                return;

            List<SpawnExFortObject> crystals = siegeEvent.getObjects("crystals");
            for(SpawnExFortObject c : crystals)
            {
                List<Spawner> spawns = c.getSpawns();
                for(Spawner spawn : spawns)
                    if(spawn.getCurrentCount() > 0)
                        return;
            }

            SpawnExFortObject object = siegeEvent.getFirstObject("flag_pole");

            if(castingTarget.getNpcId() != object.getFirstSpawned().getNpcId())
                return;

            Player player = (Player) activeChar;

            if(!siegeEvent.getResidence().getFraction().canAttack(player.getFraction()))
                return;

            siegeEvent.stopEvent(player);
        }
        else
        {
            Player player = activeChar.getPlayer();

            List<CastleSiegeEvent> targetEvents = castingTarget.getEvents(CastleSiegeEvent.class);
            for(CastleSiegeEvent event : targetEvents)
            {
                if(!event.isInProgress())
                    continue;

                Castle r = event.getResidence();
                if(r.getFraction() == player.getFraction())
                    continue;

                List<SiegeToggleNpcObject> crystals = event.getObjects("flame_towers");
                for(SiegeToggleNpcObject c : crystals)
                {
                    if(c.isAlive())
                        return;
                }

                crystals = event.getObjects("control_towers");
                for(SiegeToggleNpcObject c : crystals)
                {
                    if(c.isAlive())
                        return;
                }

                event.takeCastle(player);
            }
        }

        List<Creature> around = World.getAroundCharacters(castingTarget, castingTarget.isArtefact() ? 185 : getCastRange() * 2, 100);
        for(Creature ch : around)
        {
            if(!Objects.equals(activeChar, ch) && ch.isCastingNow() && ch.getCastingSkill() == this)
                ch.abortCast(true, true);
        }
    }

    @Override
    protected void onStartCast(Creature activeChar, Creature castingTarget, List<Creature> targets)
    {
        if(!activeChar.isPlayer())
            return;

        if(castingTarget == null)
            return;
        if(castingTarget instanceof ArtifactInstance)
        {
            final ArtifactInstance artifact = (ArtifactInstance) castingTarget;
            final Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
            if(!(parameter instanceof ArtifactEntity))
                return;
            ArtifactEntity entity = (ArtifactEntity) parameter;
            entity.notifyCast(activeChar.getPlayer());
        }
    }

    @Override
    public void onAbortCast(Creature caster, Creature castingTarget)
    {
        if(!caster.isPlayer())
            return;

        if (castingTarget instanceof ArtifactInstance)
        {
            ArtifactInstance artifact = (ArtifactInstance) castingTarget;
            Object parameter = artifact.getParameter(ArtifactEntity.PARAM_ENTITY);
            if(!(parameter instanceof ArtifactEntity))
                return;

            ArtifactEntity entity = (ArtifactEntity) parameter;
            entity.notifyAbortCast(caster.getPlayer());
        }
    }
}
