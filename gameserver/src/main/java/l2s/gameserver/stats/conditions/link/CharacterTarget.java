package l2s.gameserver.stats.conditions.link;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.stats.conditions.Condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CharacterTarget extends Condition {
    protected static final String NPC_CLASS = "NPC_CLASS";
    protected static final String SAME_FACTION = "SAME_FACTION";
    protected static final String PLAYABLE = "PLAYABLE";
    protected static final String PLAYER = "PLAYER";
    protected static final String DEAD = "DEAD";
    protected static final String COMBAT = "COMBAT";

    private Map<String, Object> map;
    private String state;
    private String operate;
    private String value;

    private boolean canBeThisForTarget = false;

    @Override
    protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value) {
        if(creature == null)
            return false;
        Creature lastTarget = null;
        if(Objects.equals(operate, THIS))
            lastTarget = creature;
        else if(Objects.equals(operate, TARGET)) {
            lastTarget = target;
            if(creature == lastTarget)
                return canBeThisForTarget;
        }
        if(lastTarget == null)
            return false;
        switch (state) {
            case NPC_CLASS:
                return lastTarget.getClass() == map.getOrDefault(NPC_CLASS, Class.class);
            case SAME_FACTION:
                return lastTarget.getFraction() == creature.getFraction();
            case PLAYABLE:
                return lastTarget.isPlayable();
            case PLAYER:
                return lastTarget.isPlayer();
            case DEAD:
                return lastTarget.isDead();
            case COMBAT:
                return lastTarget.isInCombat();
        }
        return false;
    }

    @Override
    public void init() {
        if(state.equals(NPC_CLASS)) {
            initMap();
            Class<NpcInstance> classType;
            try {
                classType = (Class<NpcInstance>) Class.forName("l2s.gameserver.model.instances." + value + "Instance");
            } catch (ClassNotFoundException e) {
                classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + value + "Instance");
            }
            if(classType == null)
                throw new IllegalArgumentException("Not found type class for type: " + value + ".");
            map.put(NPC_CLASS, classType);
        }
    }

    private void initMap() {
        if(map == null)
            map = new HashMap<>();
    }
}
