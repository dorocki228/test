package l2s.gameserver.templates.item.data;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class ResourcesData
{
    private final int hitChanceToDie;
    private final int maxHitsToDie;
    private final ImmutableMultimap<Integer, RewardItemData> resources;

    public ResourcesData(int hitChanceToDie, int maxHitsToDie, Multimap<Integer, RewardItemData> resources)
    {
        this.hitChanceToDie = hitChanceToDie;
        this.maxHitsToDie = maxHitsToDie;
        this.resources = ImmutableMultimap.copyOf(resources);
    }

    public int getHitChanceToDie()
    {
        return hitChanceToDie;
    }

    public int getMaxHitsToDie()
    {
        return maxHitsToDie;
    }

    public ImmutableMultimap<Integer, RewardItemData> getResources()
    {
        return resources;
    }
}
