package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ClanRewardManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.ExPledgeBonusOpen;
import l2s.gameserver.utils.ItemFunctions;

public class RequestPledgeBonusReward extends L2GameClientPacket
{
	private static final int[] logins = { 0, 55168, 55169, 55170, 55171 };
	private static final int[] exps = { 0, 70020, 70021, 70022, 70023 };
	private static final int maxLogin = 30;
	private static final int maxExp = 1542857;
	private int type;

	@Override
	protected void readImpl() throws Exception
	{
		type = readC();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();

		if(player == null)
			return;

		int yesterdayLogin = ClanRewardManager.getInstance().getYesterdayLogin(player.getClanId());
		int yesterdayExp = ClanRewardManager.getInstance().getYesterdayExp(player.getClanId());
		int temp = maxLogin / 5;
		int yesterdayLoginLevel = yesterdayLogin >= temp * 5 ? 4 : (yesterdayLogin >= temp * 4 ? 3 : (yesterdayLogin >= temp * 3 ? 2 : (yesterdayLogin >= temp * 2 ? 1 : 0)));
		temp = maxExp / 5;
		int yesterdayExpLevel = yesterdayExp >= temp * 5 ? 4 : (yesterdayExp >= temp * 4 ? 3 : (yesterdayExp >= temp * 3 ? 2 : (yesterdayExp >= temp * 2 ? 1 : 0)));
		switch(type)
		{
			case 0:
			{
				if(yesterdayLoginLevel == 0)
					return;

				if(!player.getVarBoolean("ClanRewardLoginAvailable", true) || player.isNewClanMember())
					return;

				Skill skill = SkillHolder.getInstance().getSkill(logins[yesterdayLoginLevel], 1);
				if(skill == null)
					break;

				player.setVar("ClanRewardLoginAvailable", false);
				skill.getEffects(player, player);
				player.sendPacket(new ExPledgeBonusOpen(player));
				break;
			}
			case 1:
			{
				if(yesterdayExpLevel == 0)
					return;

				if(!player.getVarBoolean("ClanRewardHuntingAvailable", true) || player.isNewClanMember())
					return;

				player.setVar("ClanRewardHuntingAvailable", false);
				ItemFunctions.addItem(player, exps[yesterdayExpLevel], 1);
				player.sendPacket(new ExPledgeBonusOpen(player));
			}
		}
	}
}
