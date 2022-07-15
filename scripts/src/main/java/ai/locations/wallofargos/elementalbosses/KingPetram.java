package ai.locations.wallofargos.elementalbosses;

import l2s.gameserver.ai.Mystic;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.LostItems;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.AbnormalVisualEffect;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.NpcUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * @author Java-man
 */
public class KingPetram extends Mystic<NpcInstance> {

    private final SkillEntry SHIELD_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.NONE, SkillHolder.getInstance().getSkill(4214, 1));

    private final List<Action> actions = List.of(
            new GuardsSummonAction(75),
            new GuardsSummonAction(50),
            new GuardsSummonAction(25));

    private final AtomicInteger minionsAlive = new AtomicInteger(0);

    public KingPetram(NpcInstance actor) {
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
    protected void onEvtAttacked(Creature attacker, Skill skill, int damage) {
        actions.forEach(action -> {
            if (action.check(getActor(), attacker, skill, damage)) {
                action.execute(getActor(), attacker, skill, damage);
            }
        });

        super.onEvtAttacked(attacker, skill, damage);
    }

    @Override
    protected void onEvtDead(Creature killer, LostItems lostItems) {
        cleanUp(getActor());
        super.onEvtDead(killer, lostItems);
    }

    private void cleanUp(NpcInstance actor) {
        for (NpcInstance n : actor.getReflection().getNpcs())
            n.deleteMe();
    }

    public void stopShield(NpcInstance actor)
    {
        for(Abnormal e : actor.getAbnormalList())
        {
            if(e.getSkill().equals(SHIELD_SKILL.getTemplate()))
            {
                e.exit();
            }
        }
    }

    private abstract class Action {
        private final AtomicBoolean executed = new AtomicBoolean(false);

        void execute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
            if (executed.compareAndSet(false, true)) {
                doExecute(actor, attacker, skill, damage);
            }
        }

        boolean check(NpcInstance actor, Creature attacker, Skill skill, int damage) {
            if (!executed.get()) {
                return canExecute(actor, attacker, skill, damage);
            }

            return false;
        }

        abstract boolean canExecute(NpcInstance actor, Creature attacker, Skill skill, int damage);

        abstract void doExecute(NpcInstance actor, Creature attacker, Skill skill, int damage);
    }

    private class GuardsSummonAction extends Action {
        private final int hpPercent;
        private final List<Location> pieceSpawns;
        private final List<Location> fragmentSpawns;

        private GuardsSummonAction(int hpPercent) {
            this.hpPercent = hpPercent;
            if (hpPercent == 75) {
                pieceSpawns = List.of(
                        new Location(222904, 191496, -15448),
                        new Location(222088, 190568, -15448)
                );
				fragmentSpawns = List.of(
						new Location(221160, 191528, -15448),
						new Location(222072, 192184, -15448)
				);
            } else if (hpPercent == 50) {
                pieceSpawns = List.of(
                        new Location(222904, 191496, -15448),
                        new Location(222088, 190568, -15448),
                        new Location(221160, 191528, -15448),
                        new Location(222072, 192184, -15448)
                );
				fragmentSpawns = List.of(
						new Location(222712, 190888, -15448),
						new Location(221432, 190888, -15448),
						new Location(221464, 192136, -15448),
						new Location(222712, 192168, -15448)
				);
            } else if (hpPercent == 25) {
                pieceSpawns = List.of(
                        new Location(222904, 191496, -15448),
                        new Location(222088, 190568, -15448),
                        new Location(221160, 191528, -15448),
                        new Location(222072, 192184, -15448),
                        new Location(222712, 190888, -15448),
                        new Location(221432, 190888, -15448)
                );
				fragmentSpawns = List.of(
						new Location(221464, 192136, -15448),
						new Location(222712, 192168, -15448),
						new Location(222680, 191272, -15448),
						new Location(222664, 191752, -15448),
						new Location(221832, 192008, -15448),
						new Location(221464, 191320, -15448)
				);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        boolean canExecute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
            return actor.getCurrentHpPercents() <= hpPercent;
        }

        @Override
        void doExecute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			pieceSpawns.forEach(location -> {
                addSpawn(actor, location, 29116);
            });
			fragmentSpawns.forEach(location -> {
                addSpawn(actor, location, 29117);
            });

			actor.getFlags().getDamageBlocked().start(this);
            actor.doCast(SHIELD_SKILL, actor, true);
			actor.startAbnormalEffect(AbnormalVisualEffect.INVINCIBILITY);
            actor.startAbnormalEffect(AbnormalVisualEffect.ULTIMATE_DEFENCE);
        }

        private void addSpawn(NpcInstance actor, Location location, int npcId) {
            NpcInstance npc = NpcUtils.spawnSingle(npcId, location, actor.getReflection());
            minionsAlive.incrementAndGet();
            npc.addListener((OnDeathListener) (minion, killer) -> {
                if (minionsAlive.decrementAndGet() <= 0) {
                    actor.stopAbnormalEffect(AbnormalVisualEffect.INVINCIBILITY);
                    actor.stopAbnormalEffect(AbnormalVisualEffect.ULTIMATE_DEFENCE);
                    stopShield(actor);
                    actor.getFlags().getDamageBlocked().stop(this);
                }
            });
        }
    }
}