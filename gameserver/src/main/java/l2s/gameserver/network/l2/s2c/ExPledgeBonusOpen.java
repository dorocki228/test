package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.ClanRewardManager;
import l2s.gameserver.model.Player;

public class ExPledgeBonusOpen extends L2GameServerPacket
{
	private static final int[] logins = { 0, 55168, 55169, 55170, 55171 };
	private static final int[] exps = { 0, 70020, 70021, 70022, 70023 };
	private static final int maxLogin = 30;
	private static final int maxExp = 1542857;
	private int currentLogin;
	private int currentExp;
	private int yesterdayLoginLevel;
	private int yesterdayExpLevel;
	private boolean clanRewardHuntingAvailable = false;
	private boolean clanRewardLoginAvailable = false;
	private boolean isNewClanMember;

	public ExPledgeBonusOpen(Player player)
	{

		if(!player.isInClan())
			return;

		currentLogin = ClanRewardManager.getInstance().getLogin(player.getClanId());
		currentExp = ClanRewardManager.getInstance().getExp(player.getClanId());
		int yesterdayLogin = ClanRewardManager.getInstance().getYesterdayLogin(player.getClanId());
		int yesterdayExp = ClanRewardManager.getInstance().getYesterdayExp(player.getClanId());
		int temp = maxLogin / 5;
		yesterdayLoginLevel = yesterdayLogin >= temp * 5 ? 4 : (yesterdayLogin >= temp * 4 ? 3 : (yesterdayLogin >= temp * 3 ? 2 : (yesterdayLogin >= temp * 2 ? 1 : 0)));
		temp = maxExp / 5;
		yesterdayExpLevel = yesterdayExp >= temp * 5 ? 4 : (yesterdayExp >= temp * 4 ? 3 : (yesterdayExp >= temp * 3 ? 2 : (yesterdayExp >= temp * 2 ? 1 : 0)));
		if(yesterdayLoginLevel >= 1)
			clanRewardLoginAvailable = player.getVarBoolean("ClanRewardLoginAvailable", true);
		if(yesterdayExpLevel >= 1)
			clanRewardHuntingAvailable = player.getVarBoolean("ClanRewardHuntingAvailable", true);
		isNewClanMember = player.isNewClanMember();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(maxLogin);
		writeD(currentLogin);
		writeD(logins[yesterdayLoginLevel]);
		writeC(yesterdayLoginLevel);
		writeC(yesterdayLoginLevel > 0 && clanRewardLoginAvailable && !isNewClanMember ? 1 : 0);
		writeD(maxExp);
		writeD(currentExp);
		writeD(exps[yesterdayExpLevel]);
		writeC(yesterdayExpLevel);
		writeC(yesterdayExpLevel > 0 && clanRewardHuntingAvailable && !isNewClanMember ? 1 : 0);
	}
}
