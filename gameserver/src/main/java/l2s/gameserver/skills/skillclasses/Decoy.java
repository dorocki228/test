package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

import java.util.List;

public class Decoy extends Skill
{
	private final int _npcId;
	private final int _lifeTime;
	private final int _numbersOfDecoys;

	public Decoy(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
		_numbersOfDecoys = set.getInteger("decoyCount", 1);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return super.checkCondition(activeChar, target, forceUse, dontMove, first) && !activeChar.isAlikeDead() && activeChar.isPlayer() && activeChar == target && _npcId > 0;
	}

	@Override
	public void onEndCast(Creature activeChar, List<Creature> targets)
	{
		super.onEndCast(activeChar, targets);
		if(!activeChar.isPlayer())
			return;
		Player player = activeChar.getPlayer();
		NpcTemplate DecoyTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
		for(int i = 0; i < _numbersOfDecoys; ++i)
		{
			DecoyInstance decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, player, _lifeTime);
			decoy.setCurrentHp(decoy.getMaxHp(), false);
			decoy.setCurrentMp(decoy.getMaxMp());
			decoy.setHeading(player.getHeading());
			decoy.setReflection(player.getReflection());
			player.addDecoy(decoy);
			decoy.spawnMe(Location.findAroundPosition(player, 50, 70));
			decoy.transferOwnerBuffs();
		}
	}
}
