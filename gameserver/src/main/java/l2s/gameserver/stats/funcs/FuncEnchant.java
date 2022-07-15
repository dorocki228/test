package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.item.ItemQuality;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class FuncEnchant extends Func
{
	public FuncEnchant(DoubleStat stat, int order, Object owner, double value)
	{
		super(stat, order, owner);
	}

	@Override
	public double calc(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, double value)
	{
		ItemInstance item = (ItemInstance) getOwner();

		int enchant = actor.isPlayer() ? item.getFixedEnchantLevel(actor.getPlayer()) : item.getEnchantLevel();
		int overenchant = Math.max(0, enchant - 3);
		int overenchantR1 = Math.max(0, enchant - 6);
		int overenchantR2 = Math.max(0, enchant - 9);
		int overenchantR3 = Math.max(0, enchant - 12);
		boolean isBlessed = item.getTemplate().getQuality() == ItemQuality.BLESSED;

		switch(getStat())
		{
			case SOULSHOT_POWER:
			case SPIRITSHOT_POWER:
			{
				return value + Math.min(30, enchant) * 0.7;
			}
		}

		return value;
	}
}