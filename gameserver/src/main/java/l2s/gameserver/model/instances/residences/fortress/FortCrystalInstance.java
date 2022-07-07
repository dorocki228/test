package l2s.gameserver.model.instances.residences.fortress;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FortCrystalInstance extends NpcInstance
{
	public FortCrystalInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public boolean isAttackable(Creature attacker) {
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
		if(siege != null && !siege.isInProgress())
			return false;

		return getFraction().canAttack(attacker.getFraction());
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{}

	@Override
	public boolean isBlocked()
	{
		return true;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
		if(siege != null && siege.isInProgress())
		{
			setFraction(siege.getResidence().getFraction());
			broadcastCharInfo();
			siege.broadcastCrystalStatus();
		}
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();

		FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
		if(siege != null && siege.isInProgress())
			siege.broadcastCrystalStatus();
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isThrowAndKnockImmune()
	{
		return true;
	}
}