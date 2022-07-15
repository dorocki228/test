package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import l2s.gameserver.network.l2.s2c.ExChangeClientEffectInfo;
import l2s.gameserver.network.l2.s2c.ExSendUIEvent;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.utils.Functions;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.StringTokenizer;

public class AdminAdmin implements IAdminCommandHandler
{
	private static enum Commands
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
		admin_uievent,
		admin_forcenpcinfo,
		admin_undying,
		admin_heading,
		admin_distance,
		admin_add_premium_points,
		admin_reduce_premium_points
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		StringTokenizer st;

		if(activeChar.getPlayerAccess().Menu)
		{
			GameObject target = activeChar.getTarget();
			switch(command)
			{
				case admin_admin:
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/admin.htm"));
					break;
				case admin_play_sounds:
					if(wordList.length == 1)
						activeChar.sendPacket(new HtmlMessage(5).setFile("admin/songs/songs.htm"));
					else
						try
						{
							activeChar.sendPacket(new HtmlMessage(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
						}
						catch(StringIndexOutOfBoundsException e)
						{}
					break;
				case admin_play_sound:
					try
					{
						playAdminSound(activeChar, wordList[1]);
					}
					catch(StringIndexOutOfBoundsException e)
					{}
					break;
				case admin_silence:
					if(activeChar.getMessageRefusal()) // already in message refusal
					// mode
					{
						activeChar.unsetVar("gm_silence");
						activeChar.setMessageRefusal(false);
						activeChar.sendPacket(SystemMsg.MESSAGE_ACCEPTANCE_MODE);
						activeChar.sendEtcStatusUpdate();
					}
					else
					{
						if(Config.SAVE_GM_EFFECTS)
							activeChar.setVar("gm_silence", "true", -1);
						activeChar.setMessageRefusal(true);
						activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
						activeChar.sendEtcStatusUpdate();
					}
					break;
				case admin_tradeoff:
					try
					{
						if(wordList[1].equalsIgnoreCase("on"))
						{
							activeChar.setTradeRefusal(true);
							Functions.sendDebugMessage(activeChar, "tradeoff enabled");
						}
						else if(wordList[1].equalsIgnoreCase("off"))
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
				case admin_show_html:
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
				case admin_setnpcstate:
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
				case admin_setareanpcstate:
					try
					{
						final String val = fullString.substring(15).trim();

						String[] vals = val.split(" ");
						int range = NumberUtils.toInt(vals[0], 0);
						int astate = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

						for(NpcInstance n : activeChar.getAroundNpc(range, 200))
							n.setNpcState(astate);
					}
					catch(Exception e)
					{
						Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
					}
					break;
				case admin_showmovie:
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
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.startScenePlayer(id);
					break;
				case admin_setzoneinfo:
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
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new ExChangeClientEffectInfo(stateid));
					break;
				case admin_et:
				case admin_eventtrigger:
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
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new EventTriggerPacket(triggerid, true));
					Functions.sendDebugMessage(activeChar, "Event Trigger ID[" + triggerid + "] activated!");
					break;
				case admin_uievent:
					if(wordList.length < 5)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
						return false;
					}
					int hide;
					int increase;
					int startTime;
					int endTime;
					int unk1;
					int unk2;
					int unk3;
					int unk4;
					String text;
					try
					{
						hide = Integer.parseInt(wordList[1]);
						increase = Integer.parseInt(wordList[2]);
						startTime = Integer.parseInt(wordList[3]);
						endTime = Integer.parseInt(wordList[4]);
						unk1 = Integer.parseInt(wordList[5]);
						unk2 = Integer.parseInt(wordList[6]);
						unk3 = Integer.parseInt(wordList[7]);
						unk4 = Integer.parseInt(wordList[8]);
						text = wordList[9];
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "Invalid format");
						return false;
					}
					activeChar.broadcastPacket(new ExSendUIEvent(activeChar, hide, increase, startTime, endTime, NpcString.NONE, text));
					break;
				case admin_forcenpcinfo:
					if(!target.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
						return false;
					}
					((NpcInstance) target).broadcastCharInfo();
					break;
				case admin_undying:
					if(activeChar.isGMUndying())
					{
						activeChar.setGMUndying(false);
						Functions.sendDebugMessage(activeChar, "Undying state has been disabled.");
					}
					else
					{
						activeChar.setGMUndying(true);
						Functions.sendDebugMessage(activeChar, "Undying state has been enabled.");
					}
					break;
				case admin_heading:
					if(target == null)
						target = activeChar;
					activeChar.sendMessage("Target heading: " + target.getHeading());
					break;
				case admin_distance:
					if(target == null || activeChar == target)
						activeChar.sendMessage("Target not selected!");
					else
						activeChar.sendMessage("Target distance: " + activeChar.getDistance(target));
					break;
			}
			return true;
		}

		if(activeChar.getPlayerAccess().CanTeleport)
		{
			switch(command)
			{
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							if(html.startsWith("tele"))
								activeChar.sendPacket(new HtmlMessage(5).setFile("admin/" + html));
							else
								activeChar.sendMessage("Access denied");
						else
							activeChar.sendMessage("Html page not found");
					}
					catch(Exception npe)
					{
						activeChar.sendMessage("Html page not found");
					}
					break;
			}
			return true;
		}

		if(activeChar.getPlayerAccess().UseGMShop)
		{
			GameObject target = activeChar.getTarget();
			if(target == null)
				target = activeChar;

			if(!target.isPlayer())
			{
				Functions.sendDebugMessage(activeChar, "Only player target is allowed");
				return false;
			}

			Player player = target.getPlayer();

			switch(command)
			{
				case admin_add_premium_points:
					try
					{
						player.addPremiumPoints(Integer.parseInt(wordList[1]));
					}
					catch(Exception npe)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //add_premium_points [COUNT]");
					}
					break;
				case admin_reduce_premium_points:
					try
					{
						player.reducePremiumPoints(Integer.parseInt(wordList[1]));
					}
					catch(Exception npe)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //reduce_premium_points [COUNT]");
					}
					break;
			}
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
}