package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import l2s.gameserver.network.l2.s2c.ExChangeClientEffectInfo;
import l2s.gameserver.network.l2.s2c.ExSendUIEventPacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.utils.Functions;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminAdmin implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(activeChar.getPlayerAccess().Menu)
		{
			GameObject target = activeChar.getTarget();
			switch(command)
			{
				case admin_admin:
				{
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/admin.htm"));
					break;
				}
				case admin_play_sounds:
				{
					if(wordList.length == 1)
					{
						activeChar.sendPacket(new HtmlMessage(5).setFile("admin/songs/songs.htm"));
						break;
					}
					try
					{
						activeChar.sendPacket(new HtmlMessage(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
					}
					catch(StringIndexOutOfBoundsException ex2)
					{}
					break;
				}
				case admin_play_sound:
				{
					try
					{
						playAdminSound(activeChar, wordList[1]);
					}
					catch(StringIndexOutOfBoundsException ex3)
					{}
					break;
				}
				case admin_silence:
				{
					if(activeChar.getMessageRefusal())
					{
						activeChar.unsetVar("gm_silence");
						activeChar.setMessageRefusal(false);
						activeChar.sendPacket(SystemMsg.MESSAGE_ACCEPTANCE_MODE);
						activeChar.sendEtcStatusUpdate();
						break;
					}
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_silence", "true", -1L);
					activeChar.setMessageRefusal(true);
					activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
					activeChar.sendEtcStatusUpdate();
					break;
				}
				case admin_tradeoff:
				{
					try
					{
						if("on".equalsIgnoreCase(wordList[1]))
						{
							activeChar.setTradeRefusal(true);
							Functions.sendDebugMessage(activeChar, "tradeoff enabled");
						}
						else if("off".equalsIgnoreCase(wordList[1]))
						{
							activeChar.setTradeRefusal(false);
							Functions.sendDebugMessage(activeChar, "tradeoff disabled");
						}
					}
					catch(Exception ex)
					{
						if(activeChar.getTradeRefusal())
							Functions.sendDebugMessage(activeChar, "tradeoff currently enabled");
						else
							Functions.sendDebugMessage(activeChar, "tradeoff currently disabled");
					}
					break;
				}
				case admin_show_html:
				{
					String html = wordList[1];
					try
					{
						if(html != null)
							activeChar.sendPacket(new HtmlMessage(5).setFile("admin/" + html));
						else
							Functions.sendDebugMessage(activeChar, "Html page not found");
					}
					catch(Exception npe)
					{
						Functions.sendDebugMessage(activeChar, "Html page not found");
					}
					break;
				}
				case admin_setnpcstate:
				{
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setnpcstate state");
						return false;
					}
					int state;
					try
					{
						state = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify state");
						return false;
					}
					if(!target.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "You must target an NPC");
						return false;
					}
					NpcInstance npc = (NpcInstance) target;
					npc.setNpcState(state);
					break;
				}
				case admin_setareanpcstate:
				{
					try
					{
						String val = fullString.substring(15).trim();
						String[] vals = val.split(" ");
						int range = NumberUtils.toInt(vals[0], 0);
						int astate = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;
						for(NpcInstance n : activeChar.getAroundNpc(range, 200))
							n.setNpcState(astate);
					}
					catch(Exception e2)
					{
						Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
					}
					break;
				}
				case admin_showmovie:
				{
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //showmovie id");
						return false;
					}
					int id;
					try
					{
						id = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e3)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.startScenePlayer(id);
					break;
				}
				case admin_setzoneinfo:
				{
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo id");
						return false;
					}
					int stateid;
					try
					{
						stateid = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e4)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new ExChangeClientEffectInfo(stateid));
					break;
				}
				case admin_et:
				case admin_eventtrigger:
				{
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //eventtrigger id");
						return false;
					}
					int triggerid;
					try
					{
						triggerid = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e5)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new EventTriggerPacket(triggerid, true));
					Functions.sendDebugMessage(activeChar, "Event Trigger ID[" + triggerid + "] activated!");
					break;
				}
				case admin_debug:
				{
					if(!target.isPlayer())
					{
						Functions.sendDebugMessage(activeChar, "Only player target is allowed");
						return false;
					}
					Player pl = target.getPlayer();
					List<String> _s = new ArrayList<>();
					_s.add("==========TARGET STATS:");
					_s.add("==Magic Resist: " + pl.calcStat(Stats.MAGIC_RESIST, null, null));
					_s.add("==Magic Power: " + pl.calcStat(Stats.MAGIC_POWER, 1.0, null, null));
					_s.add("==P. Skill Power: " + pl.calcStat(Stats.P_SKILL_POWER, 1.0, null, null));
					_s.add("==Cast Break Rate: " + pl.calcStat(Stats.CAST_INTERRUPT, 1.0, null, null));
					_s.add("==========Powers:");
					_s.add("==Bleed: " + pl.calcStat(Stats.BLEED_POWER, 1.0, null, null));
					_s.add("==Poison: " + pl.calcStat(Stats.POISON_POWER, 1.0, null, null));
					_s.add("==Stun: " + pl.calcStat(Stats.STUN_POWER, 1.0, null, null));
					_s.add("==Root: " + pl.calcStat(Stats.ROOT_POWER, 1.0, null, null));
					_s.add("==Mental: " + pl.calcStat(Stats.MENTAL_POWER, 1.0, null, null));
					_s.add("==Sleep: " + pl.calcStat(Stats.SLEEP_POWER, 1.0, null, null));
					_s.add("==Paralyze: " + pl.calcStat(Stats.PARALYZE_POWER, 1.0, null, null));
					_s.add("==Cancel: " + pl.calcStat(Stats.CANCEL_POWER, 1.0, null, null));
					_s.add("==Debuff: " + pl.calcStat(Stats.DEBUFF_POWER, 1.0, null, null));
					_s.add("==========PvP Stats:");
					_s.add("==Phys Attack Dmg: " + pl.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0, null, null));
					_s.add("==Phys Skill Dmg: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0, null, null));
					_s.add("==Magic Skill Dmg: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0, null, null));
					_s.add("==Phys Attack Def: " + pl.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0, null, null));
					_s.add("==Phys Skill Def: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0, null, null));
					_s.add("==Magic Skill Def: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0, null, null));
					_s.add("==========Reflects:");
					_s.add("==Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, null, null));
					_s.add("==Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, null, null));
					_s.add("==Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, null, null));
					_s.add("==Counterattack: Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_DAMAGE_PERCENT, null, null));
					_s.add("==Counterattack: Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, null, null));
					_s.add("==Counterattack: Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, null, null));
					_s.add("==========MP Consume Rate:");
					_s.add("==Magic Skills: " + pl.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 1.0, null, null));
					_s.add("==Phys Skills: " + pl.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 1.0, null, null));
					_s.add("==Music: " + pl.calcStat(Stats.MP_DANCE_SKILL_CONSUME, 1.0, null, null));
					_s.add("==========Shield:");
					_s.add("==Shield Defence: " + pl.calcStat(Stats.SHIELD_DEFENCE, null, null));
					_s.add("==Shield Defence Rate: " + pl.calcStat(Stats.SHIELD_RATE, null, null));
					_s.add("==Shield Defence Angle: " + pl.calcStat(Stats.SHIELD_ANGLE, null, null));
					_s.add("==========Etc:");
					_s.add("==Fatal Blow Rate: " + pl.calcStat(Stats.FATALBLOW_RATE, null, null));
					_s.add("==Phys Skill Evasion Rate: " + pl.calcStat(Stats.P_SKILL_EVASION, null, null));
					_s.add("==Counterattack Rate: " + pl.calcStat(Stats.COUNTER_ATTACK, null, null));
					_s.add("==Pole Attack Angle: " + pl.calcStat(Stats.POLE_ATTACK_ANGLE, null, null));
					_s.add("==Pole Target Count: " + pl.calcStat(Stats.POLE_TARGET_COUNT, 1.0, null, null));
					_s.add("==========DONE.");
					for(String s : _s)
						Functions.sendDebugMessage(activeChar, s);
					break;
				}
				case admin_uievent:
				{
					if(wordList.length < 5)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
						return false;
					}
					int hide;
					int increase;
					int startTime;
					int endTime;
					String text;
					try
					{
						hide = Integer.parseInt(wordList[1]);
						increase = Integer.parseInt(wordList[2]);
						startTime = Integer.parseInt(wordList[3]);
						endTime = Integer.parseInt(wordList[4]);
						int unk1 = Integer.parseInt(wordList[5]);
						int unk2 = Integer.parseInt(wordList[6]);
						int unk3 = Integer.parseInt(wordList[7]);
						int unk4 = Integer.parseInt(wordList[8]);
						text = wordList[9];
					}
					catch(NumberFormatException e6)
					{
						Functions.sendDebugMessage(activeChar, "Invalid format");
						return false;
					}
					activeChar.broadcastPacket(new ExSendUIEventPacket(activeChar, hide, increase, startTime, endTime, NpcString.NONE, text));
					break;
				}
				case admin_forcenpcinfo:
				{
					if(!target.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
						return false;
					}
					((NpcInstance) target).broadcastCharInfo();
					break;
				}
				case admin_undying:
				{
					if(activeChar.isUndying())
					{
						activeChar.setUndying(SpecialEffectState.FALSE);
						Functions.sendDebugMessage(activeChar, "Undying state has been disabled.");
						break;
					}
					activeChar.setUndying(SpecialEffectState.GM);
					Functions.sendDebugMessage(activeChar, "Undying state has been enabled.");
					break;
				}
				case admin_heading:
				{
					if(target == null)
						target = activeChar;
					activeChar.sendMessage("Target heading: " + target.getHeading());
					break;
				}
				case admin_distance:
				{
					if(target == null || activeChar == target)
					{
						activeChar.sendMessage("Target not selected!");
						break;
					}
					activeChar.sendMessage("Target distance: " + activeChar.getDistance(target));
					break;
				}
			}
			return true;
		}
		if(activeChar.getPlayerAccess().CanTeleport)
		{
			switch(command)
			{
				case admin_show_html:
				{
					String html2 = wordList[1];
					try
					{
						if(html2 != null)
						{
							if(html2.startsWith("tele"))
								activeChar.sendPacket(new HtmlMessage(5).setFile("admin/" + html2));
							else
								activeChar.sendMessage("Access denied");
						}
						else
							activeChar.sendMessage("Html page not found");
					}
					catch(Exception npe2)
					{
						activeChar.sendMessage("Html page not found");
					}
					break;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void playAdminSound(Player activeChar, String sound)
	{
		activeChar.broadcastPacket(new PlaySoundPacket(sound));
		activeChar.sendPacket(new HtmlMessage(5).setFile("admin/admin.htm"));
		activeChar.sendMessage("Playing " + sound + ".");
	}

	private enum Commands
	{
		admin_admin,
		admin_play_sounds,
		admin_play_sound,
		admin_silence,
		admin_tradeoff,
		admin_cfg,
		admin_config,
		admin_show_html,
		admin_setnpcstate,
		admin_setareanpcstate,
		admin_showmovie,
		admin_setzoneinfo,
		admin_et,
		admin_eventtrigger,
		admin_debug,
		admin_uievent,
		admin_forcenpcinfo,
		admin_undying,
		admin_heading,
		admin_distance
    }
}
