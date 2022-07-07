package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author KRonst
 */
public class SpecialMinionInstance extends MonsterInstance {

    public SpecialMinionInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public boolean isFearImmune()
    {
        return true;
    }

    @Override
    public boolean isParalyzeImmune()
    {
        return true;
    }

    @Override
    public boolean isLethalImmune()
    {
        return true;
    }

    @Override
    public boolean canChampion()
    {
        return false;
    }

    @Override
    public void onRandomAnimation()
    {

    }
}
