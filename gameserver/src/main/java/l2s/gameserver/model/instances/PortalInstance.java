package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.gve.GvePortalManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.bbs.PortalCommunityBoardEntry;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.Pair;

public class PortalInstance extends MonsterInstance
{
	private String name;
	private String nameForCommunityBoard;
	private int teleportsLeft;
	private long spawnTime;

	private PortalCommunityBoardEntry communityBoardEntry;

	public PortalInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		setHasChatWindow(false);
	}

	public int getTeleportsLeft()
	{
		return teleportsLeft;
	}

	public void decreaseTeleportsLeft()
	{
		teleportsLeft--;

		if(teleportsLeft <= 0)
			deleteMe();
	}

	@Override
	public String getTitle()
	{
		return _title;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
			return false;

		return getFraction().canAttack(attacker.getFraction());
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String getNameForCommunityBoard()
	{
		return nameForCommunityBoard;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		int distance = Integer.MAX_VALUE;
		Location loc = getLoc();
		for(Pair<String, Location> p : Config.GVE_PORTALS_BUTTON_DATA)
		{
			if(loc.getZ() < p.getRight().getZ() || loc.getZ() > p.getRight().getHeading())
				continue;
			double d = loc.distance(p.getRight());
			if(d < distance)
			{
				distance = (int) d;
				name = p.getLeft();
				String playerName = getPlayer().getName();
				playerName = playerName.length() > 6 ? playerName.substring(0, 6) : playerName;
				if (isPersonalPortal()) {
					nameForCommunityBoard = name + " [Personal]";
				} else {
					nameForCommunityBoard = name + " [FP] [" + playerName + ']';
				}
			}
		}

		switch(getNpcId())
		{
			case 40045:
			case 40047:
			{
				teleportsLeft = 10;
				break;
			}
			case 40046:
			case 40048:
			{
				teleportsLeft = 50;
				break;
			}
		}

		GvePortalManager.getInstance().addPortal(this);
		communityBoardEntry = new PortalCommunityBoardEntry(this);
		communityBoardEntry.register();
	}

	@Override
	protected void onDespawn()
	{
		GvePortalManager.getInstance().removePortal(this);
		if(communityBoardEntry != null) {
			communityBoardEntry.unregister();
			communityBoardEntry = null;
		}

		super.onDespawn();
	}

	@Override
	protected void onDeath(Creature killer)
	{
		GvePortalManager.getInstance().removePortal(this);
        if(communityBoardEntry != null)
            communityBoardEntry.unregister();

		super.onDeath(killer);
	}

	@Override
	public void startDeleteTask(long delay)
	{
		super.startDeleteTask(delay);
		spawnTime = System.currentTimeMillis();
	}

	@Override
	public boolean isPortal()
	{
		return true;
	}

	public boolean isPersonalPortal()
	{
		return getNpcId() == 40045 || getNpcId() == 40047;
	}

	public boolean isFractionalPortal()
	{
		return getNpcId() == 40046 || getNpcId() == 40048;
	}

	public long getTimeToDelete()
	{
		return spawnTime;
	}

	@Override
	public boolean isThrowAndKnockImmune() {
		return true;
	}

	@Override
	public boolean isFearImmune() {
		return true;
	}
}
