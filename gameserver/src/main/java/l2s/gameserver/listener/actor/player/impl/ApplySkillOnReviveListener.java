package l2s.gameserver.listener.actor.player.impl;

import l2s.gameserver.listener.actor.OnReviveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public class ApplySkillOnReviveListener implements OnReviveListener
{
    private final Skill skill;
    private final int time;

    public ApplySkillOnReviveListener(Skill skill, int time)
    {
        this.skill = skill;
        this.time = time;
    }

    @Override
    public void onRevive(Creature creature)
    {
        skill.getEffects(creature, creature, time, 1);
        creature.removeListener(this);
    }
}
