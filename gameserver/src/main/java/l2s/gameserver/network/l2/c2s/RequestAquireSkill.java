package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.VillageMasterPledgeBypasses;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;

public class RequestAquireSkill extends L2GameClientPacket
{
	private AcquireType _type;
	private int _id;
	private int _level;
	private int _subUnit;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = AcquireType.getById(readD());
		if(_type == AcquireType.SUB_UNIT)
			_subUnit = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.isTransformed() || _type == null)
			return;
		if (player.isPrivateBuffer()) {
			player.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}
		NpcInstance trainer = player.getLastNpc();
		if((trainer == null || !player.checkInteractionDistance(trainer)) && !player.isGM())
			trainer = null;
		SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(_id, _level);
		if(skillEntry == null)
			return;
		if(!SkillAcquireHolder.getInstance().isSkillPossible(player, skillEntry.getTemplate(), _type))
			return;
		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);
		if(skillLearn == null)
			return;
		if(skillLearn.getMinLevel() > player.getLevel())
			return;
		if(!checkSpellbook(player, skillLearn))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
			return;
		}
		switch(_type)
		{
			case NORMAL:
			{
				learnSimpleNextLevel(player, skillLearn, skillEntry, true);
				break;
			}
			case FISHING:
			{
				if(trainer == null)
					break;
				learnSimpleNextLevel(player, skillLearn, skillEntry, false);
				NpcInstance.showFishingSkillList(player);
				break;
			}
			case CLAN:
			{
				if(trainer != null)
				{
					learnClanSkill(player, skillLearn, trainer, skillEntry);
					break;
				}
				break;
			}
			case SUB_UNIT:
			{
				if(trainer != null)
				{
					learnSubUnitSkill(player, skillLearn, trainer, skillEntry, _subUnit);
					break;
				}
				break;
			}
		}
	}

	private static void learnSimpleNextLevel(Player player, SkillLearn skillLearn, SkillEntry skillEntry, boolean normal)
	{
		int skillLevel = player.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1)
			return;
		learnSimple(player, skillLearn, skillEntry, normal);
	}

	private static void learnSimple(Player player, SkillLearn skillLearn, SkillEntry skillEntry, boolean normal)
	{
		if(player.getSp() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
			return;
		}
		if(skillLearn.getItemId() > 0 && !player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount(), true))
			return;
		player.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skillEntry.getId(), skillEntry.getLevel()));
		player.setSp(player.getSp() - skillLearn.getCost());
		player.addSkill(skillEntry, true);
		if(normal)
			player.rewardSkills(false);
		player.sendUserInfo();
		player.updateStats();
		player.sendSkillList(skillEntry.getId());
		player.updateSkillShortcuts(skillEntry.getId(), skillEntry.getLevel());
	}

	private static void learnClanSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, SkillEntry skillEntry)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		Clan clan = player.getClan();
		int skillLevel = clan.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1)
			return;
		if(clan.getReputationScore() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return;
		}
		if(skillLearn.getItemId() > 0 && !player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount(), true))
			return;
		clan.incReputation(-skillLearn.getCost(), false, "AquireSkill: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
		clan.addSkill(skillEntry, true);
		clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skillEntry.getTemplate()));
		VillageMasterPledgeBypasses.showClanSkillList(trainer, player);
	}

	private static void learnSubUnitSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, SkillEntry skillEntry, int id)
	{
		Clan clan = player.getClan();
		if(clan == null)
			return;
		SubUnit sub = clan.getSubUnit(id);
		if(sub == null)
			return;
		if((player.getClanPrivileges() & 0x200) != 0x200)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		int lvl = sub.getSkillLevel(skillLearn.getId(), 0);
		if(lvl >= skillLearn.getLevel())
		{
			player.sendPacket(SystemMsg.THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_ACQUIRED);
			return;
		}
		if(lvl != skillLearn.getLevel() - 1)
		{
			player.sendPacket(SystemMsg.THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED);
			return;
		}
		if(clan.getReputationScore() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return;
		}
		if(skillLearn.getItemId() > 0 && !player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount(), true))
			return;
		clan.incReputation(-skillLearn.getCost(), false, "AquireSkill2: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
		sub.addSkill(skillEntry, true);
		player.sendPacket(new SystemMessagePacket(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skillEntry.getTemplate()));
		if(trainer != null)
			NpcInstance.showSubUnitSkillList(player);
	}

	private static boolean checkSpellbook(Player player, SkillLearn skillLearn)
	{
		return Config.ALT_DISABLE_SPELLBOOKS || skillLearn.getItemId() == 0 || player.getInventory().getCountOf(skillLearn.getItemId()) >= skillLearn.getItemCount();
	}
}
