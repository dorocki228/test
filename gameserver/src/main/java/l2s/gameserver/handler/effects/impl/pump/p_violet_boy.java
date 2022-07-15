package l2s.gameserver.handler.effects.impl.pump;

import l2s.gameserver.Config;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 **/
public final class p_violet_boy extends EffectHandler {
    public p_violet_boy(EffectTemplate template) {
        super(template);
    }

    @Override
    protected boolean checkPumpCondition(Abnormal abnormal, Creature caster, Creature target) {
        return target.isPlayer();
    }

    @Override
    public void pumpStart(Abnormal abnormal, Creature caster, Creature target) {
        Player player = target.getPlayer();
        if (player != null) {
            player.getFlags().getVioletBoy().start(this);
            player.sendStatusUpdate(true, true, StatusUpdatePacket.PVP_FLAG);
            player.broadcastRelation();
        }
    }

    @Override
    public void pumpEnd(Abnormal abnormal, Creature caster, Creature target) {
        Player player = target.getPlayer();
        if (player != null) {
            player.getFlags().getVioletBoy().stop(this);
            player.startPvPFlag(null);
            player.setLastPvPAttack(System.currentTimeMillis() - Config.PVP_TIME + 20000);
        }
    }
}