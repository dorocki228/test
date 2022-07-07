package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author KRonst
 */
public class SprigantInstance extends MonsterInstance {

    private static final int BUFF_ID = 366;

    public SprigantInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    protected void onDeath(Creature killer) {
        if (killer != null) {
            final SkillEntry skill = SkillHolder.getInstance().getSkillEntry(BUFF_ID, 1);
            if (skill != null) {
                skill.getEffects(this, killer);
            }
        }
        super.onDeath(killer);
    }
}
