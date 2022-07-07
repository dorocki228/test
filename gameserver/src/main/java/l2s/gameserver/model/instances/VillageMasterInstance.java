package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.SiegeUtils;

import java.util.StringTokenizer;

public final class VillageMasterInstance extends NpcInstance
{
	private final MasterType _type;

	public VillageMasterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		_type = MasterType.valueOf(getParameter("master_type", "NONE").toUpperCase());
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		if("manage_clan".equals(command))
            showChatWindow(player, "pledge/pl001.htm", false);
		else if("manage_alliance".equals(command))
            showChatWindow(player, "pledge/al001.htm", false);
		else if("create_clan_check".equals(command))
		{
			if(player.getLevel() <= 9)
                showChatWindow(player, "pledge/pl002.htm", false);
			else if(player.isClanLeader())
                showChatWindow(player, "pledge/pl003.htm", false);
			else if(player.getClan() != null)
                showChatWindow(player, "pledge/pl004.htm", false);
			else
                showChatWindow(player, "pledge/pl005.htm", false);
		}
		else if("lvlup_clan_check".equals(command))
		{
			if(!player.isClanLeader())
			{
                showChatWindow(player, "pledge/pl014.htm", false);
				return;
			}
            showChatWindow(player, "pledge/pl013.htm", false);
		}
		else if("disband_clan_check".equals(command))
		{
			if(!player.isClanLeader())
			{
                showChatWindow(player, "pledge/pl_err_master.htm", false);
				return;
			}
            showChatWindow(player, "pledge/pl007.htm", false);
		}
		else if("restore_clan_check".equals(command))
		{
			if(!player.isClanLeader())
			{
                showChatWindow(player, "pledge/pl011.htm", false);
				return;
			}
            showChatWindow(player, "pledge/pl010.htm", false);
		}
		else if("change_leader_check".equals(command))
            showChatWindow(player, "pledge/pl_master.htm", false);
		else if(command.startsWith("request_change_leader_check"))
		{
			if(!player.isClanLeader())
			{
                showChatWindow(player, "pledge/pl_err_master.htm", false);
				return;
			}
            showChatWindow(player, "pledge/pl_transfer_master.htm", false);
		}
		else if(command.startsWith("cancel_change_leader_check"))
		{
			if(!player.isClanLeader())
			{
                showChatWindow(player, "pledge/pl_err_master.htm", false);
				return;
			}
            showChatWindow(player, "pledge/pl_cancel_master.htm", false);
		}
		else if(command.startsWith("create_clan"))
		{
			if(command.length() > 12)
			{
				String val = command.substring(12);
				VillageMasterPledgeBypasses.createClan(this, player, val);
			}
		}
		else if(command.startsWith("change_leader"))
		{
			StringTokenizer tokenizer = new StringTokenizer(command);
			if(tokenizer.countTokens() != 3)
				return;
			tokenizer.nextToken();
			VillageMasterPledgeBypasses.changeLeader(this, player, Integer.parseInt(tokenizer.nextToken()), tokenizer.nextToken());
		}
		else if(command.startsWith("cancel_change_leader"))
			VillageMasterPledgeBypasses.cancelLeaderChange(this, player);
		else if(command.startsWith("check_create_ally"))
            showChatWindow(player, "pledge/al005.htm", false);
		else if(command.startsWith("create_ally"))
		{
			if(command.length() > 12)
			{
				String val = command.substring(12);
				if(VillageMasterPledgeBypasses.createAlly(player, val))
                    showChatWindow(player, "pledge/al006.htm", false);
			}
		}
		else if(command.startsWith("dissolve_clan"))
			VillageMasterPledgeBypasses.dissolveClan(this, player);
		else if(command.startsWith("restore_clan"))
			VillageMasterPledgeBypasses.restoreClan(this, player);
		else if(command.startsWith("increase_clan_level"))
			VillageMasterPledgeBypasses.levelUpClan(this, player);
		else if(command.startsWith("learn_clan_skills"))
			VillageMasterPledgeBypasses.showClanSkillList(this, player);
		else if("changeclass".equalsIgnoreCase(cmd))
		{
			int classListId = getClassListId(player);
			String html = null;
			if(classListId == -1)
				html = getParameter("fnYouAreFirstClass", null);
			else if(classListId == 0)
				html = getParameter("fnClassMismatch", null);
			else
				html = getParameter("fnClassList" + classListId, null);
			if(html != null)
                showChatWindow(player, "villagemaster/occupation/" + html, false);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "villagemaster/";
	}

	@Override
	public void onChangeClassBypass(Player player, int classId)
	{
		if(!player.isQuestContinuationPossible(true))
			return;
		String html = null;
		ClassId newClassId = ClassId.VALUES[classId];
		if(player.getClassId().isOfLevel(ClassLevel.FIRST) && newClassId.isOfLevel(ClassLevel.FIRST))
			html = getParameter("fnYouAreSecondClass", null);
		if(player.getClassId().isOfLevel(ClassLevel.SECOND) && (newClassId.isOfLevel(ClassLevel.FIRST) || newClassId.isOfLevel(ClassLevel.SECOND)))
			html = getParameter("fnYouAreThirdClass", null);
		if(html != null)
		{
            showChatWindow(player, "villagemaster/occupation/" + html, false);
			return;
		}
		if(!newClassId.childOf(player.getClassId()))
			return;
		if(newClassId.getClassLevel().ordinal() - player.getClassId().getClassLevel().ordinal() != 1)
			return;
		boolean noHaveItems = false;
		for(int itemId : newClassId.getChangeClassItemIds())
			if(ItemFunctions.getItemCount(player, itemId) == 0L)
			{
				noHaveItems = true;
				break;
			}
		int classListId = getClassListId(player);
		int classIdInList = 0;
		switch(newClassId)
		{
			case WARRIOR:
			case WIZARD:
			case ELVEN_KNIGHT:
			case ELVEN_WIZARD:
			case PALUS_KNIGHT:
			case DARK_WIZARD:
			case ORC_RAIDER:
			case ORC_SHAMAN:
			case SCAVENGER:
			case GLADIATOR:
			case PALADIN:
			case TREASURE_HUNTER:
			case SORCERER:
			case BISHOP:
			case TEMPLE_KNIGHT:
			case PLAIN_WALKER:
			case SPELLSINGER:
			case ELDER:
			case SHILLEN_KNIGHT:
			case ABYSS_WALKER:
			case SPELLHOWLER:
			case SHILLEN_ELDER:
			case DESTROYER:
			case OVERLORD:
			case BOUNTY_HUNTER:
			case WARSMITH:
			{
				classIdInList = 1;
				break;
			}
			case KNIGHT:
			case CLERIC:
			case ELVEN_SCOUT:
			case ORACLE:
			case ASSASIN:
			case SHILLEN_ORACLE:
			case ORC_MONK:
			case ARTISAN:
			case WARLORD:
			case DARK_AVENGER:
			case HAWKEYE:
			case NECROMANCER:
			case PROPHET:
			case SWORDSINGER:
			case SILVER_RANGER:
			case ELEMENTAL_SUMMONER:
			case BLADEDANCER:
			case PHANTOM_RANGER:
			case PHANTOM_SUMMONER:
			case TYRANT:
			case WARCRYER:
			{
				classIdInList = 2;
				break;
			}
			case ROGUE:
			case WARLOCK:
			{
				classIdInList = 3;
				break;
			}
		}
		if(player.getClassId().getClassMinLevel(true) > player.getLevel())
		{
			if(noHaveItems)
				html = getParameter("fnLowLevelNoProof" + classListId + classIdInList, null);
			else
				html = getParameter("fnLowLevel" + classListId + classIdInList, null);
		}
		else if(noHaveItems)
			html = getParameter("fnNoProof" + classListId + classIdInList, null);
		else
		{
			player.setClassId(newClassId.getId(), false);
			player.broadcastUserInfo(true);
            broadcastPacket(new MagicSkillUse(this, player, 5103, 1, 1000, 0L));
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER);
			for(int itemId2 : newClassId.getChangeClassItemIds())
				ItemFunctions.deleteItem(player, itemId2, 1L, true);
			html = getParameter("fnAfterClassChange" + classListId + classIdInList, null);
		}
		if(html != null)
            showChatWindow(player, "villagemaster/occupation/" + html, false);
	}

	private int getClassListId(Player player)
	{
		int classListId = 0;
		Label_1259:
		{
			switch(_type)
			{
				case HUMAN_1ST_CLASS_MAGE:
				{
					if(player.isMageClass() && player.getRace() == Race.HUMAN)
					{
						classListId = 1;
						break;
					}
					break;
				}
				case HUMAN_1ST_CLASS_FIGHTER:
				{
					if(!player.isMageClass() && player.getRace() == Race.HUMAN)
					{
						classListId = 1;
						break;
					}
					break;
				}
				case ELF_1ST_CLASS:
				{
					if(player.getRace() != Race.ELF)
						break;
					if(!player.isMageClass())
					{
						classListId = 1;
						break;
					}
					classListId = 2;
					break;
				}
				case HUMAN_ELF_1ST_CLASS_MAGE:
				{
					if(!player.isMageClass())
						break;
					if(player.getRace() == Race.HUMAN)
					{
						classListId = 1;
						break;
					}
					if(player.getRace() == Race.ELF)
					{
						classListId = 2;
						break;
					}
					break;
				}
				case HUMAN_ELF_1ST_CLASS_FIGHTER:
				{
					if(player.isMageClass())
						break;
					if(player.getRace() == Race.HUMAN)
					{
						classListId = 1;
						break;
					}
					if(player.getRace() == Race.ELF)
					{
						classListId = 2;
						break;
					}
					break;
				}
				case DARKELF_1ST_CLASS:
				{
					if(player.getRace() != Race.DARKELF)
						break;
					if(!player.isMageClass())
					{
						classListId = 1;
						break;
					}
					classListId = 2;
					break;
				}
				case ORC_1ST_CLASS:
				{
					if(player.getRace() != Race.ORC)
						break;
					if(!player.isMageClass())
					{
						classListId = 1;
						break;
					}
					classListId = 2;
					break;
				}
				case DWARVEN_1ST_CLASS:
				{
					if(player.getRace() == Race.DWARF)
					{
						classListId = 1;
						break;
					}
					break;
				}
				case HUMAN_ELF_2ND_CLASS_MYSTIC:
				{
					if(!player.isMageClass() || player.getRace() != Race.HUMAN && player.getRace() != Race.ELF)
						break;
					switch(player.getClassId())
					{
						case WIZARD:
						case SORCERER:
						case NECROMANCER:
						case WARLOCK:
						{
							classListId = 1;
							break Label_1259;
						}
						case ELVEN_WIZARD:
						case SPELLSINGER:
						case ELEMENTAL_SUMMONER:
						{
							classListId = 2;
							break Label_1259;
						}
						default:
						{
							classListId = -1;
							break Label_1259;
						}
					}
				}
				case HUMAN_ELF_2ND_CLASS_PRIEST:
				{
					if(!player.isMageClass() || player.getRace() != Race.HUMAN && player.getRace() != Race.ELF)
						break;
					switch(player.getClassId())
					{
						case BISHOP:
						case CLERIC:
						case PROPHET:
						{
							classListId = 1;
							break Label_1259;
						}
						case ELDER:
						case ORACLE:
						{
							classListId = 2;
							break Label_1259;
						}
						default:
						{
							classListId = -1;
							break Label_1259;
						}
					}
				}
				case HUMAN_ELF_2ND_CLASS_FIGHTER:
				{
					if(player.isMageClass() || player.getRace() != Race.HUMAN && player.getRace() != Race.ELF)
						break;
					switch(player.getClassId())
					{
						case WARRIOR:
						case GLADIATOR:
						case WARLORD:
						{
							classListId = 1;
							break Label_1259;
						}
						case PALADIN:
						case KNIGHT:
						case DARK_AVENGER:
						{
							classListId = 2;
							break Label_1259;
						}
						case TREASURE_HUNTER:
						case HAWKEYE:
						case ROGUE:
						{
							classListId = 3;
							break Label_1259;
						}
						case ELVEN_KNIGHT:
						case TEMPLE_KNIGHT:
						case SWORDSINGER:
						{
							classListId = 4;
							break Label_1259;
						}
						case PLAIN_WALKER:
						case ELVEN_SCOUT:
						case SILVER_RANGER:
						{
							classListId = 5;
							break Label_1259;
						}
						default:
						{
							classListId = -1;
							break Label_1259;
						}
					}
				}
				case DARKELF_2ND_CLASS:
				{
					if(player.getRace() != Race.DARKELF)
						break;
					switch(player.getClassId())
					{
						case PALUS_KNIGHT:
						case SHILLEN_KNIGHT:
						case BLADEDANCER:
						{
							classListId = 1;
							break Label_1259;
						}
						case ABYSS_WALKER:
						case ASSASIN:
						case PHANTOM_RANGER:
						{
							classListId = 2;
							break Label_1259;
						}
						case DARK_WIZARD:
						case SPELLHOWLER:
						case PHANTOM_SUMMONER:
						{
							classListId = 3;
							break Label_1259;
						}
						case SHILLEN_ELDER:
						case SHILLEN_ORACLE:
						{
							classListId = 4;
							break Label_1259;
						}
						default:
						{
							classListId = -1;
							break Label_1259;
						}
					}
				}
				case ORC_2ND_CLASS:
				{
					if(player.getRace() != Race.ORC)
						break;
					switch(player.getClassId())
					{
						case ORC_RAIDER:
						case DESTROYER:
						{
							classListId = 1;
							break Label_1259;
						}
						case ORC_MONK:
						case TYRANT:
						{
							classListId = 2;
							break Label_1259;
						}
						case ORC_SHAMAN:
						case OVERLORD:
						case WARCRYER:
						{
							classListId = 3;
							break Label_1259;
						}
						default:
						{
							classListId = -1;
							break Label_1259;
						}
					}
				}
				case DWARVEN_2ND_CLASS_WAREHOUSE:
				{
					if(player.getRace() == Race.DWARF)
					{
						switch(player.getClassId())
						{
							case SCAVENGER:
							case BOUNTY_HUNTER:
							{
								classListId = 1;
								break;
							}
							case DWARVEN_FIGHTER:
							{
								classListId = -1;
								break;
							}
						}
						break;
					}
					break;
				}
				case DWARVEN_2ND_CLASS_BLACKSMITH:
				{
					if(player.getRace() != Race.DWARF)
						break;
					switch(player.getClassId())
					{
						case WARSMITH:
						case ARTISAN:
						{
							classListId = 1;
							break Label_1259;
						}
						case DWARVEN_FIGHTER:
						{
							classListId = -1;
							break Label_1259;
						}
					}
					break;
				}
			}
		}
		return classListId;
	}

	public void setLeader(Player leader, String newLeader)
	{
		if(!leader.isClanLeader())
		{
			leader.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		if(leader.getEvent(SiegeEvent.class) != null)
		{
			leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow"));
			return;
		}
		Clan clan = leader.getClan();
		SubUnit mainUnit = clan.getSubUnit(0);
		UnitMember member = mainUnit.getUnitMember(newLeader);
		if(member == null)
		{
            showChatWindow(leader, "pledge/pl_err_man.htm", false);
			return;
		}
		if(member.isLeaderOf() != -128)
		{
			leader.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2VillageMasterInstance.CannotAssignUnitLeader"));
			return;
		}
		setLeader(leader, clan, mainUnit, member);
	}

	public static void setLeader(Player player, Clan clan, SubUnit unit, UnitMember newLeader)
	{
		player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2").addString(clan.getLeaderName()).addString(newLeader.getName()));
		if(clan.getLevel() >= 3)
		{
			if(clan.getLeader() != null)
			{
				Player oldLeaderPlayer = clan.getLeader().getPlayer();
				if(oldLeaderPlayer != null)
					SiegeUtils.removeSiegeSkills(oldLeaderPlayer);
			}
			Player newLeaderPlayer = newLeader.getPlayer();
			if(newLeaderPlayer != null)
				SiegeUtils.addSiegeSkills(newLeaderPlayer);
		}
		unit.setLeader(newLeader, true);
		clan.broadcastClanStatus(true, true, false);
	}

	private enum MasterType
	{
		NONE,
		HUMAN_1ST_CLASS_MAGE,
		HUMAN_1ST_CLASS_FIGHTER,
		ELF_1ST_CLASS,
		HUMAN_ELF_1ST_CLASS_MAGE,
		HUMAN_ELF_1ST_CLASS_FIGHTER,
		DARKELF_1ST_CLASS,
		ORC_1ST_CLASS,
		DWARVEN_1ST_CLASS,
		HUMAN_ELF_2ND_CLASS_MYSTIC,
		HUMAN_ELF_2ND_CLASS_PRIEST,
		HUMAN_ELF_2ND_CLASS_FIGHTER,
		DARKELF_2ND_CLASS,
		ORC_2ND_CLASS,
		DWARVEN_2ND_CLASS_WAREHOUSE,
		DWARVEN_2ND_CLASS_BLACKSMITH
    }
}
