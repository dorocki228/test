package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author : Nami
 * @date : 20.06.2018
 * @time : 22:59
 * <p/>
 */
public class BRMonsterInstance extends MonsterInstance {
    public BRMonsterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public int getLevel() {
        int count = 0;
        int lvlSum = 0;
        for (Creature creature : getAroundCharacters(1200, 300)) {
            if (!creature.isPlayer()) {
                continue;
            }
            count++;
            lvlSum += creature.getLevel();
        }
        return lvlSum == 0 ? super.getLevel() : (int)Math.ceil(lvlSum / count);
    }

    @Override
    public void onDeath(Creature killer) {
        super.onDeath(killer);
        if (killer.isPlayable()) {
            Player player = killer.getPlayer();

            BREvent event = player.getEvent(BREvent.class);
            if(event == null)
                return;

            var playerObject = event.getEventPlayerObject(player);
            playerObject.ifPresent(temp -> temp.increasePoints("BATTLE_ROYAL_POINTS", 1));
        }
    }
}