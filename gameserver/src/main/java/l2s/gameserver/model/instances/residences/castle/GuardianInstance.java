package l2s.gameserver.model.instances.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.component.fraction.FractionBoostBalancer;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;

public class GuardianInstance extends NpcInstance {

    public GuardianInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
        setHasChatWindow(false);
    }

    @Override
    public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp,
                                boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot,
                                boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss,
                                boolean shld, boolean magic)
    {
        CastleSiegeEvent event = isInSameEvent(attacker, CastleSiegeEvent.class);
        if (event != null) {
            int crystalsAlive = event.crystalsAlive();
            if (crystalsAlive == 2)
                damage *= 1.2;
            else if (crystalsAlive == 1)
                damage *= 1.4;
            else if (crystalsAlive == 0)
                damage *= 1.6;

            // Если одна из фракций, владеет сразу обеими замками, сделать следующее:
            // Атакующей фракции увеличить урон по гвардам замка на 50%
            if (FractionBoostBalancer.getInstance().getCount(attacker.getFraction().revert()) == 2)
                damage *= 1.50;
        } else
            damage = 0; // На всякий, если объекты не в одном событии

        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage,
                isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, magic);
    }

    @Override
    protected void onDeath(final Creature killer) {
        if (killer != null && killer.isPlayable()) {
            Player player = killer.getPlayer();
            final CastleSiegeEvent event = isInSameEvent(killer, CastleSiegeEvent.class);
            if (event != null) {
                if (player.getClan() != null)
                    event.broadcastTo(new SystemMessagePacket(SystemMsg.CLAN_S1_HAS_SUCCESSFULLY_ENGRAVED_THE_HOLY_ARTIFACT).addString(player.getClan().getName()), "attackers", "defenders");
                event.takeCastle(player);
            }
        }

        super.onDeath(killer);
    }

    @Override
    public boolean isDamageBlocked(Creature attacker, Skill skill)
    {
        if(attacker == null)
            return false;

        if(super.isDamageBlocked(attacker, skill))
            return true;

        CastleSiegeEvent event = isInSameEvent(attacker, CastleSiegeEvent.class);
        if(event != null) {
            int crystalsAlive = event.crystalsAlive();
            return crystalsAlive >= 4;
        }

        return true;
    }

    @Override
    public boolean isSiegeGuard()
    {
        return true;
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        Player player = attacker.getPlayer();
        if (player == null)
            return false;
        SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent == null)
            return false;
        Residence residence = siegeEvent.getResidence();

        Fraction owner = residence.getFraction();

        return owner.canAttack(attacker.getFraction());
    }

    @Override
    public boolean isAttackable(Creature attacker) {
        return isAutoAttackable(attacker);
    }

    @Override
    public Fraction getFraction() {
        SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent == null)
            return Fraction.NONE;

        Residence residence = siegeEvent.getResidence();
        return residence.getFraction();
    }

    @Override
    public boolean isPeaceNpc()
    {
        return false;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }
}