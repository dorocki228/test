package l2s.gameserver.model.instances;

import com.google.common.base.Preconditions;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.data.ResourcesData;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class ResourceNpcInstance extends MonsterInstance
{
    private final ResourcesData resources;

    public ResourceNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);
        resources = getTemplate().getResources();
        Preconditions.checkArgument(resources != null,
                "ResourceNpc %s don't have resources in template.", getNpcId());

        setHasChatWindow(false);
    }

    @Override
    public int getMaxHp()
    {
        return resources.getMaxHitsToDie();
    }

    @Override
    public boolean isDamageBlocked(Creature attacker, Skill skill)
    {
        if(attacker == null)
            return false;

        if(super.isDamageBlocked(attacker, skill))
            return true;

        if(!attacker.isPlayable())
            return true;

        ItemInstance weaponInstance = attacker.getActiveWeaponInstance();
        if(weaponInstance == null)
            return true;

        if(!resources.getResources().containsKey(weaponInstance.getItemId()))
            return true;

        return false;
    }

    @Override
    protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp,
                                     boolean directHp, boolean isDot)
    {
        if(!attacker.isPlayable())
            return;

        ItemInstance weaponInstance = attacker.getActiveWeaponInstance();
        if(weaponInstance == null)
            return;

        var resourceList = resources.getResources().get(weaponInstance.getItemId());
        if(resourceList == null)
            return;

        if(!isDot) {
            for(RewardItemData resource : resourceList) {
                if(Rnd.chance(resource.getChance())) {
                    ItemFunctions.addItem(attacker.getPlayer(), resource.getId(), resource.getRandomCount());
                }
            }
        }

        if(Rnd.chance(resources.getHitChanceToDie()))
        {
            doDie(attacker);
            return;
        }

        super.onReduceCurrentHp(1, attacker, skill, awake, standUp, directHp, isDot);
    }

    @Override
    public boolean isAttackable(Creature attacker)
    {
        return true;
    }

    @Override
    public boolean isAutoAttackable(Creature attacker)
    {
        return true;
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
    public boolean isThrowAndKnockImmune()
    {
        return true;
    }
}
