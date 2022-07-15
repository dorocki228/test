package instances.custom;

import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.skills.AbnormalVisualEffect;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.utils.ItemFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Kamaloka extends Reflection
{
    private final ListenerImpl listener1 = new ListenerImpl("mini_boss_killed");

    @Override
    protected void onCreate()
    {
        super.onCreate();

        InstantZone iz = this.getInstancedZone();
        if (iz != null) {
            int miniBoss = iz.getAddParams().getInteger("mini_boss", -1);
            if (miniBoss != -1) {
                this.lockBoss(this, miniBoss, this.listener1);
            }

            int finalBoss = iz.getAddParams().getInteger("final_boss", -1);
            if (finalBoss != -1) {
                this.lockBoss(this, finalBoss, null);
            }
        }
    }

    @Override
    protected void onCollapse()
    {
        super.onCollapse();
    }

    @Override
    public void onPlayerEnter(Player player)
    {
        long itemCount = ItemFunctions.getItemCount(player, 7260);
        ItemFunctions.deleteItem(player, 7260, itemCount);

        super.onPlayerEnter(player);
    }

    @Override
    public void onPlayerExit(Player player)
    {
        long itemCount = ItemFunctions.getItemCount(player, 7260);
        ItemFunctions.deleteItem(player, 7260, itemCount);

        super.onPlayerExit(player);
    }

    private final void lockBoss(Reflection reflection, int npcId, ListenerImpl listener) {
        reflection.getNpcs(true, npcId).forEach(npc -> {
            npc.getFlags().getInvulnerable().start();
            npc.startAbnormalEffect(AbnormalVisualEffect.INVINCIBILITY);
            if (listener != null) {
                npc.addListener(listener);
            }
        });
    }

    public static final class ListenerImpl implements OnDeathListener {
        private final String variable;

        public void onDeath(@Nullable Creature actor, @Nullable Creature killer) {
            if (actor != null) {
                Reflection r = actor.getReflection();
                if (!r.isDefault()) {
                    r.setVariable(this.variable, true);
                }

                actor.removeListener(this);
            }
        }

        public ListenerImpl(@NotNull String variable) {
            this.variable = variable;
        }
    }
}